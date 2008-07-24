/**
 * 
 */
package eu.etaxonomy.cdm.io.tcs;

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
	public boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, Map<String, MapWrapper<? extends CdmBase>> stores){ 
	
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		logger.info("start makeTaxonRelationships ...");

		String xmlElementName;
		String xmlAttributeName;
		Namespace elementNamespace;
		Namespace attributeNamespace;
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
		TcsImportConfigurator tcsConfig = (TcsImportConfigurator)config;
		Element root = tcsConfig.getSourceRoot();

		ITaxonService taxonService = cdmApp.getTaxonService();
		boolean success =true;

		String prefix;
		Namespace rdfNamespace = root.getNamespace();
		prefix = "tc";
		Namespace taxonConceptNamespace = root.getNamespace(prefix);
		prefix = "tcom";
		Namespace commonNamespace = root.getNamespace(prefix);
		
		xmlElementName = "TaxonConcept";
		elementNamespace = taxonConceptNamespace;
		List<Element> elTaxonConcepts = root.getChildren(xmlElementName, elementNamespace);

		int i = 0;
		//for each taxonConcept
		for (Element elTaxonConcept : elTaxonConcepts){
			if ((i++ % modCount) == 0){ logger.info("Taxa handled: " + (i-1));}
			
			//TaxonConcept about
			xmlElementName = "about";
			elementNamespace = rdfNamespace;
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
					//relationship
					xmlElementName = "relationshipCategory";
					elementNamespace = taxonConceptNamespace;
					xmlAttributeName = "resource";
					attributeNamespace = rdfNamespace;
					String strRelCategory = XmlHelp.getChildAttributeValue(elRelationship, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
					try {
						RelationshipTermBase relType = TcsTransformer.tcsRelationshipCategory2Relationship(strRelCategory);
						boolean isReverse = TcsTransformer.isReverseRelationshipCategory(strRelCategory);
						//toTaxon
						xmlElementName = "toTaxon";
						elementNamespace = taxonConceptNamespace;
						xmlAttributeName = "resource";
						attributeNamespace = rdfNamespace;
						String strToTaxon = XmlHelp.getChildAttributeValue(elRelationship, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
						TaxonBase toTaxon = taxonMap.get(strToTaxon);
						TaxonBase fromTaxon = aboutTaxon;
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
							}else{
								Taxon taxonTo = (Taxon)toTaxon;
								ReferenceBase citation = null;
								String microReference = null;
								if (relType instanceof SynonymRelationshipType){
									SynonymRelationshipType synRelType = (SynonymRelationshipType)relType;
									if (! (fromTaxon instanceof Synonym )){
										logger.warn("TaxonBase fromTaxon is not of Type 'Synonym'. Relationship is not added.");
									}else{
										Synonym synonym = (Synonym)fromTaxon;
										TaxonNameBase synName = synonym.getName();
										TaxonNameBase accName = taxonTo.getName();
										if (synName != null && accName != null && synName.isHomotypic(accName)
													&& ( synRelType.equals(SynonymRelationshipType.SYNONYM_OF()))){
											synRelType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(); 
										}
										taxonTo.addSynonym(synonym, synRelType,  citation, microReference);
									}
								}else if (relType instanceof TaxonRelationshipType){
									TaxonRelationshipType taxRelType = (TaxonRelationshipType)relType;
									if (! (fromTaxon instanceof Taxon )){
										logger.warn("TaxonBase fromTaxon is not of Type 'Taxon'. Relationship is not added.");
									}else{
										Taxon taxonFrom = (Taxon)fromTaxon;
										taxonFrom.addTaxonRelation(taxonTo, taxRelType, citation, microReference);
									}
								}else{
									logger.warn("Unknown Relationshiptype");
								}
								taxonStore.add(toTaxon);
							}
						}else{
							if (toTaxon == null){
								logger.warn("toTaxon (" + strToTaxon + ") could  not be found in taxonMap. Relationship was not added to CDM");
							}
							if (fromTaxon == null){
								logger.warn("fromTaxon (" + strTaxonAbout + ") could not be found in taxonMap. Relationship was not added to CDM");
							}
						}
					} catch (UnknownCdmTypeException e) {
						//TODO
						logger.warn("tc:relationshipCategory " + strRelCategory + " not yet implemented");
					}
					
				}//relationship
			}//hasRelationships
		}//elTaxonConcept
		logger.info("Taxa to save: " + taxonStore.size());
		taxonService.saveTaxonAll(taxonStore);
		
		logger.info("end makeRelTaxa ...");
		return success;

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
