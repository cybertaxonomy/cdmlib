/**
 * 
 */
package eu.etaxonomy.cdm.io.tcs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.IIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
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
public class TcsTaxonRelationsIO implements IIO<IImportConfigurator>{
	private static final Logger logger = Logger.getLogger(TcsTaxonRelationsIO.class);

	private static int modCount = 30000;

	
	public boolean check(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for TaxonRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	public boolean invoke(IImportConfigurator config, CdmApplicationController cdmApp,MapWrapper<? extends CdmBase>[] storeArray){ 
			
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)storeArray[0];
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)storeArray[1]; 

		String xmlElementName;
		String xmlAttributeName;
		Namespace elementNamespace;
		Namespace attributeNamespace;
		String cdmAttrName;
		String value;
		
		logger.info("start makeTaxonRelationships ...");

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
