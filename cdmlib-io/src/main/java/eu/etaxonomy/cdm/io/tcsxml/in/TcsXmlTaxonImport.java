/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml.in;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
@Component
public class TcsXmlTaxonImport  extends TcsXmlImportBase implements ICdmIO<TcsXmlImportState> {
	private static final Logger logger = Logger.getLogger(TcsXmlTaxonImport.class);

	private static int modCount = 30000;

	public TcsXmlTaxonImport(){
		super();
	}


	@Override
	public boolean doCheck(TcsXmlImportState state){
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
			List<Element> elTaxonRelationshipList = elTaxonRelationships == null ? new ArrayList<Element>() : elTaxonRelationships.getChildren(tcsElementName, tcsNamespace);
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
	public void doInvoke(TcsXmlImportState state){

		logger.info("start make TaxonConcepts ...");
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<TaxonName> taxonNameMap = (MapWrapper<TaxonName>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		Map<String, CommonTaxonName> commonNameMap = new HashMap<String, CommonTaxonName>();

		ITaxonService taxonService = getTaxonService();

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonConcept";

		TcsXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace tcsNamespace = config.getTcsXmlNamespace();

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
			List<String> elementList = new ArrayList<>();

			//create TaxonName element
			String strId = elTaxonConcept.getAttributeValue("id");
			//TODO
			String strConceptType = elTaxonConcept.getAttributeValue("type"); //original, revision, incomplete, aggregate, nominal
			String strPrimary = elTaxonConcept.getAttributeValue("primary"); //If primary='true' the concept is the first level response to a query. If 'false' the concept may be a secondary concept linked directly or indirectly to the definition of a primary concept.
			String strForm = elTaxonConcept.getAttributeValue("form");  //anamorph, teleomorph, hybrid

			childName = "Name";
			obligatory = true;
			Element elName = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
			if (isVernacular(success, elName)){
				handleVernacularName(success, strId, elName, commonNameMap);
			}else{
				TaxonName taxonName = makeScientificName(elName, null, taxonNameMap, success);
				elementList.add(childName.toString());

				//TODO how to handle
				childName = "Rank";
				obligatory = false;
				Element elRank = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
				Rank rank = TcsXmlTaxonNameImport.makeRank(elRank);
				if (rank != null){
					logger.warn("Rank in TaxonIO not yet implemented");
				}
				elementList.add(childName.toString());

				childName = "AccordingTo";
				obligatory = false;
				Element elAccordingTo = XmlHelp.getSingleChildElement(success, elTaxonConcept, childName, tcsNamespace, obligatory);
				Reference sec = makeAccordingTo(elAccordingTo, referenceMap, success);
				elementList.add(childName.toString());
				// TODO may sec be null?
				if (sec == null){
					sec = unknownSec();
				}

				TaxonBase<?> taxonBase;
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
				//delete the version information

				taxonMap.put(removeVersionOfRef(strId), taxonBase);
			}

		}
		state.setCommonNameMap(commonNameMap);

		//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
		logger.info(i + " taxa handled. Saving ...");
		taxonService.save(taxonMap.objects());
		logger.info("end makeTaxa ...");
		if (!success.getValue()){
			state.setUnsuccessfull();
		}
		return;
	}

	private void handleVernacularName(ResultWrapper<Boolean> success, String taxonId, Element elName, Map<String, CommonTaxonName> commonNameMap) {
		String name = elName.getTextNormalize();
		//TODO ref

		Language language = null;
		String strLanguage = elName.getAttributeValue("language");
		//TODO
		//Language
		if (strLanguage != null){
			language = Language.getLanguageByLabel(strLanguage);
			if (language == null){
				language = Language.getLanguageByDescription(strLanguage);
			}
			if (language == null){
				logger.warn("language ("+strLanguage+") not found for name " + name);
			}
		}
		CommonTaxonName commonName = CommonTaxonName.NewInstance(name, language);
		commonNameMap.put(taxonId, commonName);

		//TODO check other elements
	}


	private boolean isVernacular(ResultWrapper<Boolean> success, Element elName) {
		try{
			String strScientific = elName.getAttributeValue("scientific");
			boolean scientific = Boolean.valueOf(strScientific);
			return ! scientific;
		} catch (Exception e) {
			logger.warn("Value for scientific is not boolean");
			success.setValue(false);
			return false;
		}
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
	private TaxonName makeScientificName(Element elName, NomenclaturalCode code, MapWrapper<? extends TaxonName> objectMap, ResultWrapper<Boolean> success){
		TaxonName result = null;
		if (elName != null){
			String language = elName.getAttributeValue("language");
			//Language
			if (language != null){
				logger.warn("language for name not yet implemented. Language for scientific name should always be Latin");
			}
			Class<? extends IdentifiableEntity> clazz = TaxonName.class;
			result = (TaxonName)makeReferenceType (elName, clazz , objectMap, success);
			if(result == null){
				logger.warn("Name not found");
				success.setValue(false);
			}else{
			    if (result.getNameType() == null){
			        if (code == null){
			            code = NomenclaturalCode.NonViral;
			        }
			        result.setNameType(code);
			    }
			}
		}else{
			logger.warn("Name element is null");
		}
		return result;
	}


	/**
	 * @param elTaxonRelationships
	 * @param success
	 */
	private void makeTaxonRelationships(TaxonBase<?> name, Element elTaxonRelationships, ResultWrapper<Boolean> success){
		//TaxonRelationships are handled in TcsXmlTaxonRelationsImport
		return;
	}



	private void makeSpecimenCircumscription(TaxonBase<?> name, Element elSpecimenCircumscription, ResultWrapper<Boolean> success){
		if (elSpecimenCircumscription != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}


	private void makeCharacterCircumscription(TaxonBase<?> name, Element elCharacterCircumscription, ResultWrapper<Boolean> success){
		if (elCharacterCircumscription != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}

	private void makeProviderLink(TaxonBase<?> name, Element elProviderLink, ResultWrapper<Boolean> success){
		if (elProviderLink != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}


	private void makeProviderSpecificData(TaxonBase<?> name, Element elProviderSpecificData, ResultWrapper<Boolean> success){
		if (elProviderSpecificData != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
    protected boolean isIgnore(TcsXmlImportState state){
		return ! state.getConfig().isDoTaxa();
	}


}
