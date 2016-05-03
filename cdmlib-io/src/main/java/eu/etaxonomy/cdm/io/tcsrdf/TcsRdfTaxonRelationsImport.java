/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Classification;


/**
 * @author a.mueller
 * @created 29.05.2008
 * @version 1.0
 */
@Component
public class TcsRdfTaxonRelationsImport extends TcsRdfImportBase implements ICdmIO<TcsRdfImportState> {
	private static final Logger logger = Logger.getLogger(TcsRdfTaxonRelationsImport.class);

	private static int modCount = 30000;

	public TcsRdfTaxonRelationsImport(){
		super();
	}
	
	@Override
	public boolean doCheck(TcsRdfImportState state){
		boolean result = true;
		logger.warn("Checking for TaxonRelations not yet implemented");
		logger.warn("Creation of homotypic relations is still problematic");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	@Override
	public void doInvoke(TcsRdfImportState state){ 
	
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		logger.info("start makeTaxonRelationships ...");
		boolean success =true;

		String xmlElementName;
		Namespace elementNamespace;
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
		TcsRdfImportConfigurator config = state.getConfig();
		Model root = config.getSourceRoot();
		String taxonConceptNamespace = config.getTcNamespaceURIString();
		xmlElementName = "TaxonConcept";
		/*elementNamespace = taxonConceptNamespace;
		List<Element> elTaxonConcepts = root.getChildren(xmlElementName, elementNamespace);

		int i = 0;
		//for each taxonConcept
		for (Element elTaxonConcept : elTaxonConcepts){
			try {
				if ((i++ % modCount) == 0){ logger.info("Taxa handled: " + (i-1));}
				
				//TaxonConcept about
				xmlElementName = "about";
				elementNamespace = config.getRdfNamespace();
				String strTaxonAbout = elTaxonConcept.getAttributeValue(xmlElementName, elementNamespace);
				
				TaxonBase aboutTaxon = taxonMap.get(strTaxonAbout);
				
				if (aboutTaxon instanceof Taxon){
					success &= makeHomotypicSynonymRelations((Taxon)aboutTaxon);
				}
				
				xmlElementName = "hasRelationship";
				elementNamespace = taxonConceptNamespace;
				List<Element> elHasRelationships = elTaxonConcept.getChildren(xmlElementName, elementNamespace);
				
				
				for (Element elHasRelationship: elHasRelationships){
					xmlElementName = "relationship";
					elementNamespace = taxonConceptNamespace;
					List<Element> elRelationships = elHasRelationship.getChildren(xmlElementName, elementNamespace);
					
					for (Element elRelationship: elRelationships){
						success &= 	makeRelationship(elRelationship, strTaxonAbout, taxonMap, state, taxonStore);
					}//relationship
				}//hasRelationships
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error. Taxon relationship could not be imported");
				success = false;
			}
		}//elTaxonConcept
		logger.info("Taxa to save: " + taxonStore.size());
		getTaxonService().save(taxonStore);
		
		logger.info("end makeRelTaxa ...");
		*/
		return;

	}
	
	private boolean makeRelationship(
				Element elRelationship, 
				String strTaxonAbout,
				MapWrapper<TaxonBase> taxonMap,
				TcsRdfImportState state,
				Set<TaxonBase> taxonStore){
		boolean success = true;
		String xmlElementName;
		String xmlAttributeName;
		String elementNamespace;
		String attributeNamespace;
		TcsRdfImportConfigurator tcsConfig = state.getConfig();
		//relationship
		xmlElementName = "relationshipCategory";
		elementNamespace = tcsConfig.getTcNamespaceURIString();
		xmlAttributeName = "resource";
		attributeNamespace = tcsConfig.getRdfNamespaceURIString();
	/*	String strRelCategory = XmlHelp.getChildAttributeValue(elRelationship, xmlElementName, elementNamespace, xmlAttributeName, attributeNamespace);
		try {
			RelationshipTermBase relType = TcsRdfTransformer.tcsRelationshipCategory2Relationship(strRelCategory.trim());
			boolean isReverse = TcsRdfTransformer.isReverseRelationshipCategory(strRelCategory);
			//toTaxon
			xmlElementName = "toTaxon";
			elementNamespace = tcsConfig.getTcNamespace();
			xmlAttributeName = "resource";
			attributeNamespace = tcsConfig.getRdfNamespace();
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
					success = false;
				}else{
					Taxon taxonTo = (Taxon)toTaxon;
					Reference citation = null;
					String microReference = null;
					if (relType instanceof SynonymRelationshipType){
						success &= makeSynRelType((SynonymRelationshipType)relType, taxonTo, fromTaxon, citation, microReference);
					}else if (relType instanceof TaxonRelationshipType){
						success &= makeTaxonRelType((TaxonRelationshipType)relType, state, taxonTo, fromTaxon, strTaxonAbout , citation, microReference);
					}else{
						System.out.println("Unknown Relationshiptype" + strRelCategory);
						logger.warn("Unknown Relationshiptype" + strRelCategory);
						success = false;
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
				success = false;
			}
			
		} catch (UnknownCdmTypeException e) {
			//TODO
			logger.warn("tc:relationshipCategory " + strRelCategory + " not yet implemented");
			return false;
		}*/
		return success;
	}
	
	
	private boolean makeSynRelType(SynonymRelationshipType synRelType, Taxon taxonTo, TaxonBase fromTaxon, Reference citation, String microReference){
		boolean success = true;
		if (! (fromTaxon instanceof Synonym )){
			logger.warn("TaxonBase fromTaxon is not of Type 'Synonym'. Relationship is not added.");
			success = false;
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
				success = false;
			}
		}
		return success;
	}
	
	private boolean makeTaxonRelType(TaxonRelationshipType relType, TcsRdfImportState state, Taxon taxonTo, TaxonBase fromTaxon, String strTaxonAbout, Reference citation, String microReference){
		boolean success = true;
		if (! (fromTaxon instanceof Taxon )){
			logger.warn("TaxonBase fromTaxon " + strTaxonAbout + " is not of Type 'Taxon'. Relationship is not added.");
			success = false;
		}else{
			Taxon taxonFrom = (Taxon)fromTaxon;
			if (state.getConfig().isUseClassification() && relType.equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())){
				success &= makeTaxonomicallyIncluded(state, taxonTo, taxonFrom, citation, microReference);
			}else{
				taxonFrom.addTaxonRelation(taxonTo, relType, citation, microReference);
			}
		}
		return success;
	}
	
	private boolean makeTaxonomicallyIncluded(TcsRdfImportState state, Taxon toTaxon, Taxon fromTaxon, Reference citation, String microCitation){
		Reference sec = toTaxon.getSec();
		Classification tree = state.getTree(sec);
		if (tree == null){
			tree = makeTree(state, sec);
		}
		TaxonNode childNode = tree.addParentChild(toTaxon, fromTaxon, citation, microCitation);
		return (childNode != null);
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
	protected boolean isIgnore(TcsRdfImportState state){
		return ! state.getConfig().isDoRelTaxa();
	}
	
}
