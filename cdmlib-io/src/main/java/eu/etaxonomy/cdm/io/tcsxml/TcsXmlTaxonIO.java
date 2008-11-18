/**
 * 
 */
package eu.etaxonomy.cdm.io.tcsxml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
public class TcsXmlTaxonIO  extends TcsXmlIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsXmlTaxonIO.class);

	private static int modCount = 30000;
	
	public TcsXmlTaxonIO(){
		super();
	}
	
	
	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for Taxa not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	/**
	 * Computes a list of all TaxonConcept ids (ref-attribute) that are related as synonyms
	 * @param elTaxonConceptList
	 * @param success
	 * @return
	 */
	private Set<String> makeSynonymIds(List<Element> elTaxonConceptList, ResultWrapper<Boolean> success){
		//TODO use XPath
		
		 Set<String> result =  new HashSet<String>();
		
		Namespace tcsNamespace;
		//for each taxonConcept
		for (Element elTaxonConcept : elTaxonConceptList){
			tcsNamespace = elTaxonConcept.getNamespace();
			
			String childName = "TaxonRelationships";
			boolean obligatory = false;
			Element elTaxonRelationships = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);

			if (elTaxonRelationships == null){
				continue;
			}
			String tcsElementName = "TaxonRelationship";
			List<Element> elTaxonRelationshipList = elTaxonRelationships.getChildren(tcsElementName, tcsNamespace);
			for (Element elTaxonRelationship : elTaxonRelationshipList){
				
				String relationshipType = elTaxonRelationship.getAttributeValue("type");
				if ("has synonym".equalsIgnoreCase(relationshipType)){
					childName = "ToTaxonConcept";
					obligatory = true;
					Element elToTaxonConcept = XmlHelp.getSingleChildElement(success, elTaxonRelationship, childName, tcsNamespace, obligatory);
					
					String linkType = elToTaxonConcept.getAttributeValue("linkType");
					if (linkType == null || linkType.equals("local")){
						String ref = elToTaxonConcept.getAttributeValue("ref");
						result.add(ref);
					}else{
						logger.warn("External link types for synonym not yet implemented");
					}		
				}
			}
		}
		return result;
	}
	
	
	@Override
	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		logger.info("start make TaxonConcepts ...");
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		MapWrapper<TaxonNameBase<?,?>> taxonNameMap = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		ITaxonService taxonService = config.getCdmAppController().getTaxonService();

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonConcept";

		TcsXmlImportConfigurator tcsConfig = (TcsXmlImportConfigurator)config;
		Element elDataSet = getDataSetElement(tcsConfig);
		Namespace tcsNamespace = tcsConfig.getTcsXmlNamespace();
		
		childName = "TaxonConcepts";
		obligatory = false;
		Element elTaxonConcepts = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
		
		String tcsElementName = "TaxonConcept";
		List<Element> elTaxonConceptList = elTaxonConcepts.getChildren(tcsElementName, tcsNamespace);
		
		Set<String> synonymIdSet = makeSynonymIds(elTaxonConceptList, success);
		//TODO make the same for the Assertions
		
		int i = 0;
		
		//for each taxonConcept
		for (Element elTaxonConcept : elTaxonConceptList){
			if ((i++ % modCount) == 0 && i > 1){ logger.info("Taxa handled: " + (i-1));}
			List<String> elementList = new ArrayList<String>();
			
			//create TaxonName element
			String strId = elTaxonConcept.getAttributeValue("id");
			//TODO
			String strConceptType = elTaxonConcept.getAttributeValue("type"); //original, revision, incomplete, aggregate, nominal
			String strPrimary = elTaxonConcept.getAttributeValue("primary"); //If primary='true' the concept is the first level response to a query. If 'false' the concept may be a secondary concept linked directly or indirectly to the definition of a primary concept.
			String strForm = elTaxonConcept.getAttributeValue("form");  //anamorph, teleomorph, hybrid
			
			childName = "Name";
			obligatory = true;
			Element elName = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			TaxonNameBase<?,?> taxonName = makeName(elName, null, taxonNameMap, success);
			elementList.add(childName.toString());
			
			//TODO how to handle
			childName = "Rank";
			obligatory = false;
			Element elRank = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			Rank rank = TcsXmlTaxonNameIO.makeRank(elRank);
			elementList.add(childName.toString());
			
			childName = "AccordingTo";
			obligatory = false;
			Element elAccordingTo = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			ReferenceBase sec = makeAccordingTo(elAccordingTo, referenceMap, success);
			elementList.add(childName.toString());
	
			TaxonBase taxonBase;
			if (synonymIdSet.contains(strId)){
				taxonBase = Synonym.NewInstance(taxonName, sec);
			}else{
				taxonBase = Taxon.NewInstance(taxonName, sec);	
			}
			
			childName = "TaxonRelationships";
			obligatory = false;
			Element elTaxonRelationships = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			makeTaxonRelationships(taxonBase, elTaxonRelationships, success);
			elementList.add(childName.toString());

			childName = "SpecimenCircumscription";
			obligatory = false;
			Element elSpecimenCircumscription = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			makeSpecimenCircumscription(taxonBase, elSpecimenCircumscription, success);
			elementList.add(childName.toString());

			childName = "CharacterCircumscription";
			obligatory = false;
			Element elCharacterCircumscription = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			makeCharacterCircumscription(taxonBase, elCharacterCircumscription, success);
			elementList.add(childName.toString());

			
			childName = "ProviderLink";
			obligatory = false;
			Element elProviderLink = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			makeProviderLink(taxonBase, elProviderLink, success);
			elementList.add(childName.toString());
			
			childName = "ProviderSpecificData";
			obligatory = false;
			Element elProviderSpecificData = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			makeProviderSpecificData(taxonBase, elProviderSpecificData, success);
			elementList.add(childName.toString());

			testAdditionalElements(elTaxonConcept, elementList);
			ImportHelper.setOriginalSource(taxonBase, config.getSourceReference(), strId, idNamespace);
			taxonMap.put(strId, taxonBase);
			
		}
		//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
		logger.info(i + " taxa handled. Saving ...");
		taxonService.saveTaxonAll(taxonMap.objects());
		logger.info("end makeTaxa ...");
		return success.getValue();
	}
	
	
	private boolean hasIsSynonymRelation(Element taxonConcept, Namespace rdfNamespace){
		boolean result = false;
		if (taxonConcept == null || ! "TaxonConcept".equalsIgnoreCase(taxonConcept.getName()) ){
			return false;
		}
		
		String elName = "relationshipCategory";
		Filter filter = new ElementFilter(elName, taxonConcept.getNamespace());
		Iterator<Element> relationshipCategories = taxonConcept.getDescendants(filter);
		while (relationshipCategories.hasNext()){
			Element relationshipCategory = relationshipCategories.next();
			Attribute resource = relationshipCategory.getAttribute("resource", rdfNamespace);
			String isSynonymFor = "http://rs.tdwg.org/ontology/voc/TaxonConcept#IsSynonymFor";
			if (resource != null && isSynonymFor.equalsIgnoreCase(resource.getValue()) ){
				return true;
			}
		}
		return result;
	}
	
	
	/**
	 * @param elTaxonRelationships
	 * @param success
	 */
	private TaxonNameBase<?, ?> makeName(Element elName, NomenclaturalCode code, MapWrapper<? extends TaxonNameBase<?,?>> objectMap, ResultWrapper<Boolean> success){
		TaxonNameBase<?, ?> result = null;
		if (elName != null){
			//scientific
			try {
				String strScientific = elName.getAttributeValue("scientific");
				boolean scientific = Boolean.valueOf(strScientific);
				if (! scientific){
					//TODO
					logger.warn("Non scientific names not yet implemented");
				}
			} catch (Exception e) {
				logger.warn("Value for scientific is not boolean");
			}
			String language = elName.getAttributeValue("scientific");
			//TODO
			//Language
			if (language != null){
				logger.warn("language for name not yet implemented");	
			}
			//Rank rank = null;
			//IdentifiableEntity<?> obj = code.getNewTaxonNameInstance(rank);
			Class<? extends IdentifiableEntity> clazz = (Class<? extends IdentifiableEntity<?>>)NonViralName.class;
			if (code != null){
				clazz = code.getCdmClass();
			}
			result = (TaxonNameBase<?,?>)makeReferenceType (elName, clazz , objectMap, success);
				
			//if ()
			
			logger.warn("makeName not yet implemented");
			success.setValue(false);
		}
		return result;
	}
	
	
	/**
	 * @param elTaxonRelationships
	 * @param success
	 */
	private void makeTaxonRelationships(TaxonBase name, Element elTaxonRelationships, ResultWrapper<Boolean> success){
		if (elTaxonRelationships != null){
			logger.warn("makeTaxonRelationships not yet implemented");
			success.setValue(false);
		}
	}
	
	
	
	
	private ReferenceBase makeAccordingTo(Element elAccordingTo, MapWrapper<ReferenceBase> referenceMap, ResultWrapper<Boolean> success){
		if (elAccordingTo != null){
			logger.warn("makeAccordingTo not yet implemented");
			success.setValue(false);
		}
		return null;
		
		//		//FIXME
//		String secId = "pub_999999";
//		ReferenceBase sec = referenceMap.get(strAccordingTo);
//		
//		if (sec == null){
//			logger.warn("sec could not be found in referenceMap or nomRefMap for secId: " + strAccordingTo);
//		}
	}
	
	
	private void makeSpecimenCircumscription(TaxonBase name, Element elSpecimenCircumscription, ResultWrapper<Boolean> success){
		if (elSpecimenCircumscription != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}
	
	
	private void makeCharacterCircumscription(TaxonBase name, Element elCharacterCircumscription, ResultWrapper<Boolean> success){
		if (elCharacterCircumscription != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}
	
	private void makeProviderLink(TaxonBase name, Element elProviderLink, ResultWrapper<Boolean> success){
		if (elProviderLink != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}
	

	private void makeProviderSpecificData(TaxonBase name, Element elProviderSpecificData, ResultWrapper<Boolean> success){
		if (elProviderSpecificData != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoTaxa();
	}


}
