/**
 * 
 */
package tcsxml;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 *
 */
public class TcsTaxonRelationsIO extends CdmIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsTaxonRelationsIO.class);

	private static int modCount = 30000;

	public TcsTaxonRelationsIO(){
		super();
	}
	
	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for TaxonRelations not yet implemented");
		logger.warn("Creation of homotypic relations is still problematic");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	@Override
	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){ 
	
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		logger.info("start makeTaxonRelationships ...");
		boolean success =true;

		String xmlElementName;
		Namespace elementNamespace;
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
		TcsXmlImportConfigurator tcsConfig = (TcsXmlImportConfigurator)config;
		Element root = tcsConfig.getSourceRoot();
		Namespace taxonConceptNamespace = null;//tcsConfig.getTcNamespace();

		ITaxonService taxonService = config.getCdmAppController().getTaxonService();

		xmlElementName = "TaxonConcept";
		elementNamespace = taxonConceptNamespace;
		List<Element> elTaxonConcepts = root.getChildren(xmlElementName, elementNamespace);

		int i = 0;
		//for each taxonConcept
		for (Element elTaxonConcept : elTaxonConcepts){
			if ((i++ % modCount) == 0){ logger.info("Taxa handled: " + (i-1));}
			
			//TaxonConcept about
			xmlElementName = "about";
			elementNamespace = null;//tcsConfig.getRdfNamespace();
			String strTaxonAbout = elTaxonConcept.getAttributeValue(xmlElementName, elementNamespace);
			
			TaxonBase aboutTaxon = taxonMap.get(strTaxonAbout);
			
			if (aboutTaxon instanceof Taxon){
				makeHomotypicSynonymRelations((Taxon)aboutTaxon);
			}
			
			xmlElementName = "hasRelationship";
			elementNamespace = taxonConceptNamespace;
			List<Element> elHasRelationships = elTaxonConcept.getChildren(xmlElementName, elementNamespace);
			
			
			for (Element elHasRelationship: elHasRelationships){
				xmlElementName = "relationship";
				elementNamespace = taxonConceptNamespace;
				List<Element> elRelationships = elHasRelationship.getChildren(xmlElementName, elementNamespace);
				
				for (Element elRelationship: elRelationships){
					makeRelationship(elRelationship, strTaxonAbout, taxonMap, tcsConfig, taxonStore);
				}//relationship
			}//hasRelationships
		}//elTaxonConcept
		logger.info("Taxa to save: " + taxonStore.size());
		taxonService.saveTaxonAll(taxonStore);
		
		logger.info("end makeRelTaxa ...");
		return success;

	}
	
	private boolean makeRelationship(
				Element elRelationship, 
				String strTaxonAbout,
				MapWrapper<TaxonBase> taxonMap,
				TcsXmlImportConfigurator tcsConfig,
				Set<TaxonBase> taxonStore){
		boolean result = true;
		String xmlElementName;
		String xmlAttributeName;
		Namespace elementNamespace;
		Namespace attributeNamespace;
		//relationship
		xmlElementName = "relationshipCategory";
		elementNamespace = null;//tcsConfig.getTcNamespace();
		xmlAttributeName = "resource";
		attributeNamespace = null;//tcsConfig.getRdfNamespace();
		String strRelCategory = XmlHelp.getChildAttributeValue(elRelationship, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
		try {
			RelationshipTermBase relType = TcsXmlTransformer.tcsRelationshipCategory2Relationship(strRelCategory);
			boolean isReverse = TcsXmlTransformer.isReverseRelationshipCategory(strRelCategory);
			//toTaxon
			xmlElementName = "toTaxon";
			elementNamespace = null;//tcsConfig.getTcNamespace();
			xmlAttributeName = "resource";
			attributeNamespace = null;//tcsConfig.getRdfNamespace();
			String strToTaxon = XmlHelp.getChildAttributeValue(elRelationship, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
			TaxonBase toTaxon = taxonMap.get(strToTaxon);
			TaxonBase fromTaxon = taxonMap.get(strTaxonAbout);
			if (toTaxon != null && fromTaxon != null){
				//reverse
				if (isReverse == true ){
					TaxonBase tmp = toTaxon;
					toTaxon = fromTaxon;
					fromTaxon = tmp;
				}
				
				//Create relationship
				if (! (toTaxon instanceof Taxon)){
					logger.warn("TaxonBase toTaxon is not of Type 'Taxon'. Relationship is not added.");
					result = false;
				}else{
					Taxon taxonTo = (Taxon)toTaxon;
					ReferenceBase citation = null;
					String microReference = null;
					if (relType instanceof SynonymRelationshipType){
						SynonymRelationshipType synRelType = (SynonymRelationshipType)relType;
						if (! (fromTaxon instanceof Synonym )){
							logger.warn("TaxonBase fromTaxon is not of Type 'Synonym'. Relationship is not added.");
							result = false;
						}else{
							Synonym synonym = (Synonym)fromTaxon;
							TaxonNameBase synName = synonym.getName();
							TaxonNameBase accName = taxonTo.getName();
							if (synName != null && accName != null && synName.isHomotypic(accName)
										&& ( synRelType.equals(SynonymRelationshipType.SYNONYM_OF()))){
								synRelType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(); 
							}
							if (! relationExists(taxonTo, synonym, synRelType)){
								taxonTo.addSynonym(synonym, synRelType,  citation, microReference);	
							}else{
								//TODO citation, microReference
								//TODO different synRelTypes -> warning
								result = false;
							}
						}
					}else if (relType instanceof TaxonRelationshipType){
						TaxonRelationshipType taxRelType = (TaxonRelationshipType)relType;
						if (! (fromTaxon instanceof Taxon )){
							logger.warn("TaxonBase fromTaxon " + strTaxonAbout + "is not of Type 'Taxon'. Relationship is not added.");
							result = false;
						}else{
							Taxon taxonFrom = (Taxon)fromTaxon;
							taxonFrom.addTaxonRelation(taxonTo, taxRelType, citation, microReference);
						}
					}else{
						logger.warn("Unknown Relationshiptype");
						result = false;
					}
					taxonStore.add(toTaxon);
				}
			}else{
				if (toTaxon == null){
					logger.warn("toTaxon (" + strToTaxon + ") could  not be found in taxonMap. Relationship of type " + strRelCategory + " was not added to CDM");
				}
				if (fromTaxon == null){
					logger.warn("fromTaxon (" + strTaxonAbout + ") could not be found in taxonMap. Relationship was not added to CDM");
				}
				result = false;
			}
			
		} catch (UnknownCdmTypeException e) {
			//TODO
			logger.warn("tc:relationshipCategory " + strRelCategory + " not yet implemented");
			return false;
		}
		return result;
	}
	
	
	
	private boolean relationExists(Taxon taxonTo, Synonym synonym, SynonymRelationshipType synRelType){
		if (synonym == null){
			return false;
		}
		if (synonym.getRelationType(taxonTo).size() > 0){
			Set<SynonymRelationshipType> relTypeList = synonym.getRelationType(taxonTo);
			if (relTypeList.contains(synRelType)){
				return true;
			}else{
				logger.warn("Taxon-Synonym pair has 2 different SynonymRelationships. This is against the rules");
				return false;
			}
		}else{
			return false;
		}
	}

	private boolean makeHomotypicSynonymRelations(Taxon aboutTaxon){
		if (true)return false;
		TaxonNameBase aboutName = aboutTaxon.getName();
		if (aboutName != null){
			Set<TaxonNameBase> typifiedNames = aboutName.getHomotypicalGroup().getTypifiedNames();
			for (TaxonNameBase typifiedName : typifiedNames){
				//TODO check if name is part of this tcs file
				if (typifiedName.equals(aboutName)){
					continue;
				}
				Set<Synonym> syns = typifiedName.getSynonyms();
				for(Synonym syn:syns){
					aboutTaxon.addSynonym(syn, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
				}
			}
			
			
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoRelTaxa();
	}
	
}
