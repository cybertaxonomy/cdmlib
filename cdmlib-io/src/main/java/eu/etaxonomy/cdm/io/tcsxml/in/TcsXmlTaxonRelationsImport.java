/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.tcsxml.in;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.tcsxml.TcsXmlTransformer;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 *
 */
@Component
public class TcsXmlTaxonRelationsImport extends TcsXmlImportBase implements ICdmIO<TcsXmlImportState> {
	private static final Logger logger = Logger.getLogger(TcsXmlTaxonRelationsImport.class);

	private static int modCount = 30000;

	public TcsXmlTaxonRelationsImport(){
		super();
	}
	
	@Override
	public boolean doCheck(TcsXmlImportState state){
		boolean result = true;
		logger.warn("Checking for TaxonRelations not yet implemented");
		logger.warn("Creation of homotypic relations is still problematic");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	@Override
	public boolean doInvoke(TcsXmlImportState state){ 
	
		
		logger.info("start make taxon relations ...");
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<TaxonNameBase<?,?>> taxonNameMap = (MapWrapper<TaxonNameBase<?,?>>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);

		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonRelation";

		TcsXmlImportConfigurator config = state.getConfig();
		Element elDataSet = super.getDataSetElement(config);
		Namespace tcsNamespace = config.getTcsXmlNamespace();
		
		childName = "TaxonConcepts";
		obligatory = false;
		Element elTaxonConcepts = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
		
		childName = "TaxonConcept";
		List<Element> elTaxonConceptList = elTaxonConcepts.getChildren(childName, tcsNamespace);
		
		int i = 0;
		int taxonRelCount = 0;
		
		//for each taxonConcept
		for (Element elTaxonConcept : elTaxonConceptList){
			if ((i++ % modCount) == 0){ logger.info("Taxa handled: " + (i-1));}
			taxonRelCount += makeTaxonConcept(state, taxonMap, taxonStore, elTaxonConcept, tcsNamespace, success);	
		}//elTaxonConcept
	
		//TaxonRelationshipAssertions
		taxonRelCount += makeTaxonRelationshipAssertion(state, taxonMap, referenceMap, taxonStore, elDataSet, tcsNamespace, success);	
		
		logger.info("Taxa to save: " + taxonStore.size());
		getTaxonService().save(taxonStore);
		
		logger.info("end make taxon relations ...");
		return success.getValue();
	}
	
	private int makeTaxonConcept(TcsXmlImportState state, MapWrapper<TaxonBase> taxonMap, Set<TaxonBase> taxonStore, Element elTaxonConcept, Namespace tcsNamespace, ResultWrapper<Boolean> success){
		int taxonRelCount = 0;
		
		String childName = "TaxonRelationships";
		boolean obligatory = false;
		Element elTaxonRelationships = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
		
		if (elTaxonRelationships != null){
			//Relationships
			String tcsElementName = "TaxonRelationship";
			List<Element> elTaxonRelationshipList = elTaxonRelationships.getChildren(tcsElementName, tcsNamespace);

			for (Element elTaxonRelationship: elTaxonRelationshipList){
				taxonRelCount++;
				logger.debug("TaxonRelationship "+  taxonRelCount);
				
				String strId = elTaxonConcept.getAttributeValue("id");
				//TODO
//				String strConceptType = elTaxonConcept.getAttributeValue("type"); //original, revision, incomplete, aggregate, nominal
//				String strPrimary = elTaxonConcept.getAttributeValue("primary"); //If primary='true' the concept is the first level response to a query. If 'false' the concept may be a secondary concept linked directly or indirectly to the definition of a primary concept.
//				String strForm = elTaxonConcept.getAttributeValue("form");  //anamorph, teleomorph, hybrid
				
				TaxonBase fromTaxon = taxonMap.get(strId);
				makeRelationshipType(state, elTaxonRelationship, taxonMap, taxonStore, fromTaxon, success);
			
				if (fromTaxon instanceof Taxon){
					makeHomotypicSynonymRelations((Taxon)fromTaxon);
				}
			}// end Relationship
		}
		return taxonRelCount;
	}

	private int makeTaxonRelationshipAssertion(
			TcsXmlImportState state, 
			MapWrapper<TaxonBase> taxonMap,
			MapWrapper<ReferenceBase> referenceMap,
			Set<TaxonBase> taxonStore, 
			Element elDataSet, 
			Namespace tcsNamespace, 
			ResultWrapper<Boolean> success){
		
		int i = 0;
		String childName = "TaxonRelationshipAssertions";
		boolean obligatory = false;
		Element elTaxonRelationshipAssertions = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
		if(elTaxonRelationshipAssertions == null){
			return 0;
		}
		
		childName = "TaxonRelationshipAssertion";
		List<Element> elTaxonRelationshipAssertionList = elTaxonRelationshipAssertions.getChildren(childName, tcsNamespace);
		//for each taxon relationship assertion
		for (Element elTaxonRelationshipAssertion : elTaxonRelationshipAssertionList){
			if ((i++ % modCount) == 0){ logger.info("TaxonRelationshipAssertions handled: " + (i-1));}
			String strId = elTaxonRelationshipAssertion.getAttributeValue("id");
			//TODO id
			
			childName = "AccordingTo";
			obligatory = true;
			Element elAccordingTo = XmlHelp.getSingleChildElement(success, elTaxonRelationshipAssertion, childName, tcsNamespace, obligatory);
			ReferenceBase ref = makeAccordingTo(elAccordingTo, referenceMap, success);
			
			childName = "FromTaxonConcept";
			obligatory = true;
			Element elFromTaxonConcept = XmlHelp.getSingleChildElement(success, elTaxonRelationshipAssertion, childName, tcsNamespace, obligatory);

			Class<? extends TaxonBase> clazz = Taxon.class;
			//TODO if synonym
			TaxonBase fromTaxon = makeReferenceType(elFromTaxonConcept, clazz, taxonMap, success);
			
			makeRelationshipType(state, elTaxonRelationshipAssertion, taxonMap, taxonStore, fromTaxon, success);
		}//elTaxonRelationshipAssertion
		
		return i;
	}
	
	
	/**
	 * Handles the TCS RelationshipType element.
	 * @param tcsConfig
	 * @param elRelationship
	 * @param taxonMap
	 * @param taxonStore
	 * @param fromTaxon
	 * @param success
	 */
	private void makeRelationshipType(
			TcsXmlImportState state
			, Element elRelationship 
			, MapWrapper<TaxonBase> taxonMap
			, Set<TaxonBase> taxonStore
			, TaxonBase fromTaxon
			, ResultWrapper<Boolean> success
			){
	
		if (elRelationship == null){
			success.setValue(false);
		}
		String strRelType = elRelationship.getAttributeValue("type");
		
		
		try {
			ResultWrapper<Boolean> isInverse = new ResultWrapper<Boolean>();
			isInverse.setValue(false);
			RelationshipTermBase<?> relType = TcsXmlTransformer.tcsRelationshipType2Relationship(strRelType, isInverse);
			
			//toTaxon (should be part of relationshipType)
			boolean isSynonym = (relType instanceof SynonymRelationshipType);
			TaxonBase toTaxon = getToTaxon(elRelationship, taxonMap, isSynonym, success);
			
			if (toTaxon != null && fromTaxon != null){
				//exchange taxa if relationship is inverse
				if (isInverse.getValue() == true ){
					TaxonBase tmp = toTaxon;
					toTaxon = fromTaxon;
					fromTaxon = tmp;
				}
				
				//Create relationship
				if (! (toTaxon instanceof Taxon)){
					logger.warn("TaxonBase toTaxon is not of Type 'Taxon'. Relationship is not added.");
					success.setValue(false);
				}else{
					Taxon taxonTo = (Taxon)toTaxon;
					ReferenceBase citation = null;
					String microReference = null;
					if (relType instanceof SynonymRelationshipType){
						SynonymRelationshipType synRelType = (SynonymRelationshipType)relType;
						if (! (fromTaxon instanceof Synonym )){
							logger.warn("TaxonBase fromTaxon is not of Type 'Synonym'. Relationship is not added.");
							success.setValue(false);
						}else{
							Synonym synonym = (Synonym)fromTaxon;
							TaxonNameBase<?,?> synName = synonym.getName();
							TaxonNameBase<?,?> accName = taxonTo.getName();
							if (synName != null && accName != null && synName.isHomotypic(accName)
										&& ( synRelType.equals(SynonymRelationshipType.SYNONYM_OF()))){
								synRelType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(); 
							}
							if (! relationExists(taxonTo, synonym, synRelType)){
								taxonTo.addSynonym(synonym, synRelType,  citation, microReference);	
							}else{
								//TODO citation, microReference
								//TODO different synRelTypes -> warning
								success.setValue(false);
							}
						}
					}else if (relType instanceof TaxonRelationshipType){
						makeTaxonRelationship(state, (TaxonRelationshipType)relType, fromTaxon, taxonTo, citation, microReference, success);
					}else{
						logger.warn("Unknown Relationshiptype");
						success.setValue(false);
					}
					taxonStore.add(toTaxon);
				}
			}else{
				if (toTaxon == null){
					logger.warn("toTaxon (" + /*strToTaxon + */ ") could  not be found in taxonMap. Relationship of type " + strRelType + " was not added to CDM");
				}
				if (fromTaxon == null){
					logger.warn("fromTaxon (" + /*strTaxonAbout + */") could not be found in taxonMap. Relationship was not added to CDM");
				}
				success.setValue(false);
			}
			
		} catch (UnknownCdmTypeException e) {
			//TODO
			logger.warn("relationshipType " + strRelType + " not yet implemented");
			success.setValue(false);
		}
		return;
	}
	
	private void makeTaxonRelationship(TcsXmlImportState state, TaxonRelationshipType relType, TaxonBase fromTaxon, Taxon taxonTo, ReferenceBase citation, String microReference, ResultWrapper<Boolean> success){
		TaxonRelationshipType taxRelType = (TaxonRelationshipType)relType;
		if (! (fromTaxon instanceof Taxon )){
			logger.warn("TaxonBase fromTaxon " + /*strTaxonAbout +*/ "is not of Type 'Taxon'. Relationship is not added.");
			success.setValue(false);
		}else{
			Taxon taxonFrom = (Taxon)fromTaxon;
			if (relType.equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())){
				makeTaxonomicallyIncluded(state, taxonTo, taxonFrom, citation, microReference);
			}
			taxonFrom.addTaxonRelation(taxonTo, taxRelType, citation, microReference);
		}
	}
	
	private boolean makeTaxonomicallyIncluded(TcsXmlImportState state, Taxon toTaxon, Taxon fromTaxon, ReferenceBase citation, String microCitation){
		ReferenceBase sec = toTaxon.getSec();
		TaxonomicTree tree = state.getTree(sec);
		if (tree == null){
			tree = makeTree(state, sec);
		}
		TaxonNode childNode = tree.addParentChild(toTaxon, fromTaxon, citation, microCitation);
		return (childNode != null);
	}
	
	
	private TaxonBase getToTaxon(Element elTaxonRelationship, MapWrapper<TaxonBase> map, boolean isSynonym, ResultWrapper<Boolean> success){
		TaxonBase result = null;
		if (elTaxonRelationship == null || map == null){
			success.setValue(false);
		}else{
			String childName = "ToTaxonConcept";
			boolean obligatory = true;
			Element elToTaxonConcept = XmlHelp.getSingleChildElement(success, elTaxonRelationship, childName, elTaxonRelationship.getNamespace(), obligatory);
			
			String linkType = elToTaxonConcept.getAttributeValue("linkType");
			if (linkType == null || linkType.equals("local")){
				String ref = elToTaxonConcept.getAttributeValue("ref");
				if (ref != null){
					result = map.get(ref);
				}else{
					String title = elToTaxonConcept.getTextNormalize();
					//TODO synonym?
					TaxonNameBase<?,?> taxonName = NonViralName.NewInstance(null);
					taxonName.setTitleCache(title, true);
					logger.warn("Free text related taxon seems to be bug in TCS");
					if (isSynonym){
						result = Synonym.NewInstance(taxonName, TcsXmlTaxonImport.unknownSec());
					}else{
						result = Taxon.NewInstance(taxonName, TcsXmlTaxonImport.unknownSec());	
					}
					result.setTitleCache(title, true);
				}
			}else{
				logger.warn("External link types for synonym not yet implemented");
			}
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
		TaxonNameBase<?,?> aboutName = aboutTaxon.getName();
		if (aboutName != null){
			Set<TaxonNameBase> typifiedNames = aboutName.getHomotypicalGroup().getTypifiedNames();
			for (TaxonNameBase<?,?> typifiedName : typifiedNames){
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
	protected boolean isIgnore(TcsXmlImportState state){
		return ! state.getConfig().isDoRelTaxa();
	}
	
}
