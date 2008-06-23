/**
 * 
 */
package eu.etaxonomy.cdm.io.tcs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Generic;
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
public class TcsTaxonIO {
	private static final Logger logger = Logger.getLogger(TcsTaxonIO.class);

	private static int modCount = 30000;

	public static boolean check(IImportConfigurator tcsiConfig){
		boolean result = true;
		logger.warn("Checking for Taxa not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public static boolean checkRelations(IImportConfigurator tcsiConfig){
		boolean result = true;
		logger.warn("Checking for TaxonRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public static boolean invoke(TcsImportConfigurator tcsConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap){
		
		String xmlElementName;
		String xmlAttributeName;
		Namespace elementNamespace;
		Namespace attributeNamespace;
		String cdmAttrName;
		String value;

		logger.info("start makeTaxa ...");
		
		Element root = tcsConfig.getSourceRoot();
		boolean success =true;
		INameService nameService = cdmApp.getNameService();
		
		String prefix;
		Namespace rdfNamespace = root.getNamespace();
		prefix = "tc";
		Namespace taxonConceptNamespace = root.getNamespace(prefix);
		prefix = "tcom";
		Namespace commonNamespace = root.getNamespace(prefix);
		
		xmlElementName = "TaxonConcept";
		elementNamespace = taxonConceptNamespace;
		List<Element> elTaxonConcepts = root.getChildren(xmlElementName, elementNamespace);

		ITaxonService taxonService = cdmApp.getTaxonService();
		
		int i = 0;
		//for each taxonName
		for (Element elTaxonConcept : elTaxonConcepts){
			if ((i++ % modCount) == 0){ logger.info("Taxa handled: " + (i-1));}
			
			//
			String taxonAbout = elTaxonConcept.getAttributeValue("about", rdfNamespace);
			
			//hasName
			xmlElementName = "hasName";
			elementNamespace = taxonConceptNamespace;
			xmlAttributeName = "resource";
			attributeNamespace = rdfNamespace;
			String strNameResource= XmlHelp.getChildAttributeValue(elTaxonConcept, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
			TaxonNameBase taxonNameBase = taxonNameMap.get(strNameResource);
				
			//accordingTo
			xmlElementName = "accordingTo";
			elementNamespace = taxonConceptNamespace;
			String strAccordingTo = elTaxonConcept.getChildTextTrim(xmlElementName, elementNamespace);
			//FIXME
			Generic sec = Generic.NewInstance();
			sec.setTitleCache(strAccordingTo);
			
			//FIXME or synonym
			TaxonBase taxonBase;
			if (hasIsSynonymRelation(elTaxonConcept, rdfNamespace)){
				taxonBase = Synonym.NewInstance(taxonNameBase, sec);
			}else{
				taxonBase = Taxon.NewInstance(taxonNameBase, sec);
			}
			
			//primary
//			xmlElementName = "primary";
//			elementNamespace = taxonConceptNamespace;
//			cdmAttrName = "isPrimary";
//			Boolean primary = ImportHelper.addXmlBooleanValue(elTaxonConcept, taxon, xmlElementName, elementNamespace, cdmAttrName);
			
			taxonMap.put(taxonAbout, taxonBase);
			
		}
		//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
		logger.info("saving taxa ...");
		taxonService.saveTaxonAll(taxonMap.objects());
		logger.info("end makeTaxa ...");
		return success;
	}
	
	
	private static boolean hasIsSynonymRelation(Element taxonConcept, Namespace rdfNamespace){
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
	

	public static boolean invokeRelations(TcsImportConfigurator tcsConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonBase> taxonMap, MapWrapper<ReferenceBase> referenceMap){

		String xmlElementName;
		String xmlAttributeName;
		Namespace elementNamespace;
		Namespace attributeNamespace;
		String cdmAttrName;
		String value;
		
		logger.info("start makeTaxonRelationships ...");

		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
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
		//for each taxonName
		for (Element elTaxonConcept : elTaxonConcepts){
			if ((i++ % modCount) == 0){ logger.info("Taxa handled: " + (i-1));}
			
			//TaxonConcept about
			xmlElementName = "about";
			elementNamespace = rdfNamespace;
			String taxonAbout = elTaxonConcept.getAttributeValue(xmlElementName, elementNamespace);
			
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
						TaxonBase fromTaxon = taxonMap.get(taxonAbout);
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
								logger.warn("fromTaxon (" + taxonAbout + ") could not be found in taxonMap. Relationship was not added to CDM");
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
	
	
}
