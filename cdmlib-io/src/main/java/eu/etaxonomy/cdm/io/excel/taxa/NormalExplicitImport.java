/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.taxa;

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */

@Component
public class NormalExplicitImport extends TaxonExcelImporterBase {
	private static final Logger logger = Logger.getLogger(NormalExplicitImport.class);
	
	@Override
	protected boolean isIgnore(TaxonExcelImportState state) {
		return false;
	}
	
	private int floatString2IntValue(String value) {
		
		int intValue = 0;
		try {
			Float fobj = new Float(Float.parseFloat(value));
			intValue = fobj.intValue();
			if (logger.isDebugEnabled()) { logger.debug("Value formatted: " + intValue); }
		} catch (NumberFormatException ex) {
			logger.error(value + " is not an integer");
		}
		return intValue;
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(TaxonExcelImportState state) {
		logger.warn("DoCheck not yet implemented for NormalExplicitImport");
		return true;
	}

	@Override
    protected boolean analyzeRecord(HashMap<String, String> record, TaxonExcelImportState state) {
		
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	NormalExplicitRow normalExplicitRow = new NormalExplicitRow();
    	state.setTaxonLight(normalExplicitRow);
    	
    	for (String key: keys) {
    		
    		key = CdmUtils.removeDuplicateWhitespace(key.trim()).toString();
    		
    		String value = (String) record.get(key);
    		if (!CdmUtils.isEmpty(value)) {
    			if (logger.isDebugEnabled()) { logger.debug(key + ": " + value); }
        		value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
    		}
    		
    		if (key.equalsIgnoreCase(ID_COLUMN)) {
    			int ivalue = floatString2IntValue(value);
    			normalExplicitRow.setId(ivalue);
    			
			} else if(key.equalsIgnoreCase(PARENT_ID_COLUMN)) {
				int ivalue = floatString2IntValue(value);
				normalExplicitRow.setParentId(ivalue);
				
			} else if(key.equalsIgnoreCase(RANK_COLUMN)) {
				normalExplicitRow.setRank(value);
    			
			} else if(key.equalsIgnoreCase(SCIENTIFIC_NAME_COLUMN)) {
				normalExplicitRow.setScientificName(value);
    			
			} else if(key.equalsIgnoreCase(AUTHOR_COLUMN)) {
				normalExplicitRow.setAuthor(value);
   			
			} else if(key.equalsIgnoreCase(NAME_STATUS_COLUMN)) {
				normalExplicitRow.setNameStatus(value);
    			
			} else if(key.equalsIgnoreCase(VERNACULAR_NAME_COLUMN)) {
				normalExplicitRow.setCommonName(value);
    			
			} else if(key.equalsIgnoreCase(LANGUAGE_COLUMN)) {
				normalExplicitRow.setLanguage(value);
    			
			} else {
				success = false;
				logger.error("Unexpected column header " + key);
			}
    	}
    	return success;
    }
	
	
	/** 
	 *  Stores taxa records in DB
	 */
	@Override
    protected boolean firstPass(TaxonExcelImportState state) {
		boolean success = true;
		Rank rank = null;
		
		String rankStr = state.getTaxonLight().getRank();
		String taxonNameStr = state.getTaxonLight().getScientificName();
		String authorStr = state.getTaxonLight().getAuthor();
		String nameStatus = state.getTaxonLight().getNameStatus();
		Integer id = state.getTaxonLight().getId();
			
		if (CdmUtils.isNotEmpty(taxonNameStr)) {

			// Determine the rank
			try {
				rank = Rank.getRankByName(rankStr);
			} catch (UnknownCdmTypeException ex) {
				success = false;
				logger.error(rankStr + " is not a valid rank.");
			}
			
            // Create the taxon name object depending on the setting of the nomenclatural code 
			// in the configurator (botanical code, zoological code, etc.) 
			NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
			NonViralName taxonNameBase;
			if (nc == NomenclaturalCode.ICVCN){
				logger.warn("ICVCN not yet supported");
				return false;
			}else{
				taxonNameBase =(NonViralName) nc.getNewTaxonNameInstance(rank);
				//NonViralName nonViralName = (NonViralName)taxonNameBase;
				//TODO parse name
				NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
				taxonNameBase = parser.parseFullName(taxonNameStr, nc, rank);
				
				taxonNameBase.setNameCache(taxonNameStr);
				
				// Create the author
				if (CdmUtils.isNotEmpty(authorStr)) {
					//TODO parse authors
					//if (state.getAuthor(authorStr)!= null) {
					taxonNameBase.setAuthorshipCache(authorStr);
//					} else {
//						state.putAuthor(authorStr, null);
//						Person author = Person.NewTitledInstance(authorStr);
//						nonViralName.setCombinationAuthorTeam(author);
//					}
				}
			}
			
			//Create the taxon
			ReferenceBase sec = state.getConfig().getSourceReference();
			TaxonBase taxonBase;
			// Create the nomenclatural status
			if (CdmUtils.isEmpty(nameStatus) || nameStatus.equalsIgnoreCase("valid")){
				taxonBase = Taxon.NewInstance(taxonNameBase, sec);
			}else{
				taxonBase = Synonym.NewInstance(taxonNameBase, sec);
			}
			state.putTaxon(id, taxonBase);
			getTaxonService().save(taxonBase);
		}
		return success;
    }


	/** 
	 *  Stores parent-child, synonym and common name relationships
	 */
	@Override
    protected boolean secondPass(TaxonExcelImportState state) {
		boolean success = true;
		String taxonNameStr = state.getTaxonLight().getScientificName();
		String nameStatus = state.getTaxonLight().getNameStatus();
		String commonNameStr = state.getTaxonLight().getCommonName();
		Integer parentId = state.getTaxonLight().getParentId();
		Integer childId = state.getTaxonLight().getId();
		
		Taxon parentTaxon = (Taxon)state.getTaxonBase(parentId);
		Taxon taxon = (Taxon)state.getTaxonBase(childId);
		if (CdmUtils.isNotEmpty(taxonNameStr)) {
			if (nameStatus != null && nameStatus.equalsIgnoreCase("invalid")){
				//add synonym relationship
				Synonym synonym = (Synonym)state.getTaxonBase(childId);
				parentTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
			}else if (nameStatus == null || nameStatus.equalsIgnoreCase("valid")){
				// Add the parent relationship
				if (state.getTaxonLight().getParentId() != 0) {
					if (parentTaxon != null) {
						//Taxon taxon = (Taxon)state.getTaxonBase(childId);
						
						ReferenceBase citation = state.getConfig().getSourceReference();
						String microCitation = null;
						Taxon childTaxon = taxon;
						success &= makeParent(state, parentTaxon, childTaxon, citation, microCitation);
						getTaxonService().save(parentTaxon);
					} else {
						logger.warn("Taxonomic parent not found for " + taxonNameStr);
						success = false;
					}
				}else{
					//do nothing (parent == 0) no parent exists
				}
			}
		} 
		if (CdmUtils.isNotEmpty(commonNameStr))
		{			// add common name to taxon
			
			Language language = getTermService().getLanguageByIso(state.getTaxonLight().getLanguage());
			if (language == null && CdmUtils.isNotEmpty(state.getTaxonLight().getLanguage())  ){
				String error ="Language is null but shouldn't"; 
				logger.error(error);
				throw new IllegalArgumentException(error);
			}
			CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance(commonNameStr, language);
			try {
				TaxonDescription taxonDescription = getDescription(taxon);
				taxonDescription.addElement(commonTaxonName);
				logger.info("Common name " + commonNameStr + " added to " + taxon.getTitleCache());
			} catch (ClassCastException ex) {
				logger.error(taxonNameStr + " is not a taxon instance.");
			}
		}
		return success;
	}
	
	/**
	 * @param taxon
	 * @return
	 */
	//TODO implementation must be improved when matching of taxa with existing taxa is implemented
	//=> the assumption that the only description is the description added by this import
	//is wrong then
	private TaxonDescription getDescription(Taxon taxon) {
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		if (descriptions.size()>1){
			throw new IllegalStateException("Implementation does not yet support taxa with multiple descriptions");
		}else if (descriptions.size()==1){
			return descriptions.iterator().next();
		}else{
			return TaxonDescription.NewInstance(taxon);
		}
	}

	private boolean makeParent(TaxonExcelImportState state, Taxon parentTaxon, Taxon childTaxon, ReferenceBase citation, String microCitation){
		boolean success = true;
		ReferenceBase sec = parentTaxon.getSec();
		TaxonomicTree tree = state.getTree(sec);
		if (tree == null){
			tree = makeTree(state, sec);
		}
		success &=  (null !=  tree.addParentChild(parentTaxon, childTaxon, citation, microCitation));
		return success;
	}
	

	
}
