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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */

@Component
public class NormalExplicitImport extends TaxonExcelImporterBase {
	private static final Logger logger = Logger.getLogger(NormalExplicitImport.class);
	
	@Override
	protected boolean isIgnore(ExcelImportState state) {
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
	protected boolean doCheck(ExcelImportState state) {
		logger.warn("DoCheck not yet implemented for NormalExplicitImport");
		return true;
	}

	@Override
    protected boolean analyzeRecord(HashMap<String, String> record) {
		
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	NormalExplicitRow normalExplicitRow = new NormalExplicitRow();
    	setTaxonLight(normalExplicitRow);
    	
    	for (String key: keys) {
    		
    		key = CdmUtils.removeDuplicateWhitespace(key.trim()).toString();
    		
    		String value = (String) record.get(key);
    		if (!value.equals("")) {
    			if (logger.isDebugEnabled()) { logger.debug(key + ": " + value); }
        		value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
    		}
    		
    		if (key.equalsIgnoreCase(ID_COLUMN)) {
    			int ivalue = floatString2IntValue(value);
    			getTaxonLight().setId(ivalue);
    			
			} else if(key.equalsIgnoreCase(PARENT_ID_COLUMN)) {
				int ivalue = floatString2IntValue(value);
				getTaxonLight().setParentId(ivalue);
				
			} else if(key.equalsIgnoreCase(RANK_COLUMN)) {
				getTaxonLight().setRank(value);
    			
			} else if(key.equalsIgnoreCase(SCIENTIFIC_NAME_COLUMN)) {
				getTaxonLight().setScientificName(value);
    			
			} else if(key.equalsIgnoreCase(AUTHOR_COLUMN)) {
				getTaxonLight().setAuthor(value);
   			
			} else if(key.equalsIgnoreCase(NAME_STATUS_COLUMN)) {
				getTaxonLight().setNameStatus(value);
    			
			} else if(key.equalsIgnoreCase(VERNACULAR_NAME_COLUMN)) {
				getTaxonLight().setCommonName(value);
    			
			} else if(key.equalsIgnoreCase(LANGUAGE_COLUMN)) {
				getTaxonLight().setLanguage(value);
    			
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
    protected boolean saveRecord() {
		
		boolean success = true;
		Rank rank = null;
		
		CdmApplicationController appCtr = getApplicationController();
		
		String rankStr = getTaxonLight().getRank();
		String taxonNameStr = getTaxonLight().getScientificName();
		String authorStr = getTaxonLight().getAuthor();
		String nameStatus = getTaxonLight().getNameStatus();
		String commonNameStr = getTaxonLight().getCommonName();
		
		if (!taxonNameStr.equals("")) {

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
			TaxonNameBase<?,?> taxonNameBase = nc.getNewTaxonNameInstance(rank);
			taxonNameBase.setTitleCache(taxonNameStr);
			taxonNameBase.setFullTitleCache(taxonNameStr);
			
			// Create the author
			if (!authorStr.equals("")) {
				if (getAuthors().contains(authorStr)) {
					if (logger.isDebugEnabled()) { logger.debug("Author " + authorStr + " is already loaded"); }
				} else {
					getAuthors().add(authorStr);
					Person author = Person.NewTitledInstance(authorStr);
					try {
						NonViralName nonViralName = (NonViralName)taxonNameBase;
						nonViralName.setCombinationAuthorTeam(author);
					} catch (ClassCastException ex) {
						logger.error(taxonNameBase.getTitleCache() + " is not a  non-viral name." +
								"Author " + authorStr + " ignored");
					}
				}
			}
			
			// Create the nomenclatural status
			try {
				NomenclaturalStatusType statusType = 
					NomenclaturalStatusType.getNomenclaturalStatusTypeByLabel(nameStatus);
				taxonNameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
			} catch (UnknownCdmTypeException ex) {
				logger.warn(nameStatus + " is not a valid nomenclatural status label");
			}
			// Create the taxon
			Taxon taxon = Taxon.NewInstance(taxonNameBase, null);
		
			// Add the parent relationship
			if (getTaxonLight().getParentId() != 0) {
				Taxon parentTaxon = findParentTaxon(getTaxonLight());
				if (parentTaxon != null) {
					parentTaxon.addTaxonomicChild(taxon, null, null);
					UUID parentUuid = appCtr.getTaxonService().saveTaxon(parentTaxon);
					if (logger.isDebugEnabled()) { 
						logger.debug("Child " + getTaxonLight().getScientificName() + " added to parent " 
								+ parentTaxon.getTitleCache() + " (" + parentUuid + ")"); 
						}
				} else {
					logger.warn("Taxonomic parent not found for " + taxonNameStr);
				}
			}

			// Save the taxon
			UUID taxonUuid = appCtr.getTaxonService().saveTaxon(taxon);
//			if (logger.isDebugEnabled()) { logger.debug("taxonUuid = " + taxonUuid); }
			
			// Add the taxon representation to the processed taxa map
			if (getTaxaMap().containsKey(getTaxonLight())) {
				logger.info("Taxon name " + taxonNameStr + " is already loaded");
				return true;
			} else { 
				getTaxaMap().put(getTaxonLight(), taxonUuid);
			}
			//Set the previous taxon
			setPreviousTaxonUuid(taxonUuid);
			
			} else 	{ 
				// add common name to previous taxon
				
				UUID taxonUuid = getPreviousTaxonUuid();
				Language language = appCtr.getTermService().getLanguageByIso(getTaxonLight().getLanguage());
				CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance(commonNameStr, language);
				TaxonBase taxonBase = appCtr.getTaxonService().findByUuid(taxonUuid);
				try {
					TaxonDescription description = TaxonDescription.NewInstance((Taxon)taxonBase);
					description.addElement(commonTaxonName);
					logger.info("Common name " + commonNameStr + " added to " + taxonBase.getTitleCache());
				} catch (ClassCastException ex) {
					logger.error(taxonNameStr + " is not a taxon instance.");
				}
			}
		
		return success;
    }

	
	private Taxon findParentTaxon(NormalExplicitRow normalExplicitRow) {
		
		UUID parentTaxonUuid = null;
		Taxon parentTaxon = null;
		
		for (NormalExplicitRow tLight : getTaxaMap().keySet()) {
//			logger.debug("tLight.getId() = " + tLight.getId());
//			logger.debug("taxonLight.getParentId() = " + taxonLight.getParentId());
			if (tLight.getId() == normalExplicitRow.getParentId()) {
				parentTaxonUuid = getTaxaMap().get(tLight);
				break;
			}
		}
		
		if (parentTaxonUuid == null) {
			logger.warn("Parent taxon of " + normalExplicitRow.getScientificName() + " unknown." +
			" Ignoring parent-child relationship.");
			return null;
		}
		
		TaxonBase parentTaxonBase = getApplicationController().getTaxonService().findByUuid(parentTaxonUuid);
		try {
			parentTaxon = (Taxon)parentTaxonBase;
		} catch (ClassCastException ex) {
			logger.error(normalExplicitRow.getScientificName() + " is not a taxon instance.");
		}
		return parentTaxon;
	}
	
}
