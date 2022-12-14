/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.distribution;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.config.IdentifiableServiceConfiguratorFactory;
import eu.etaxonomy.cdm.api.service.config.IdentifiableServiceConfiguratorImpl;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.babadshanjan
 * @since 10.11.2008
 */
@Component
public class DistributionImport
        extends CdmImportBase<ExcelImportConfiguratorBase, ExcelImportState<ExcelImportConfiguratorBase, ExcelRowBase>> {

    private static final long serialVersionUID = 7765309119416657235L;
    private static final Logger logger = LogManager.getLogger();

    /* used */
    private static final String EDIT_NAME_COLUMN = "EDIT";
    private static final String TDWG_DISTRIBUTION_COLUMN = "TDWG";
    private static final String STATUS_COLUMN = "Status";
    /* not used */
//    private static final String LITERATURE_NUMBER_COLUMN = "Lit.";
//    private static final String LITERATURE_COLUMN = "Literature";
//    private static final String VERNACULAR_NAME_COLUMN = "Vernacular";
//    private static final String HABITAT_COLUMN = "Habitat";
//    private static final String CONTROL_COLUMN = "Control";
//    private static final String TRANSLATED_COLUMN = "Translated";
//    private static final String ISO_DISTRIBUTION_COLUMN = "ISO";
//    private static final String NOTES_COLUMN = "Notes";
//    private static final String PAGE_NUMBER_COLUMN = "Page";
//    private static final String INFO_COLUMN = "Info";


	// Stores already processed descriptions
	Map<Taxon, TaxonDescription> myDescriptions = new HashMap<Taxon, TaxonDescription>();

	@Override
	protected void doInvoke(ExcelImportState<ExcelImportConfiguratorBase, ExcelRowBase> state) {

		if (logger.isDebugEnabled()) { logger.debug("Importing distribution data"); }

		// read and save all rows of the excel worksheet
		List<Map<String, String>> recordList;
		URI source = state.getConfig().getSource();
		try{
    		recordList = ExcelUtils.parseXLS(source);
		} catch (FileNotFoundException e) {
			String message = "File not found: " + source;
			warnProgress(state, message, e);
			logger.error(message);
			state.setUnsuccessfull();
			return;
		}
    	if (recordList != null) {
    		Map<String,String> record = null;
    		TransactionStatus txStatus = startTransaction();

    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			analyzeRecord(record);
    		}
    		commitTransaction(txStatus);
    	}

		try {
			if (logger.isDebugEnabled()) { logger.debug("End distribution data import"); }

		} catch (Exception e) {
    		logger.error("Error closing the application context");
    		e.printStackTrace();
		}

    	return;
	}

	/**
	 *  Reads the data of one Excel sheet row
	 */
    private void analyzeRecord(Map<String,String> record) {
    	/*
    	 * Relevant columns:
    	 * Name (EDIT)
    	 * Distribution TDWG
    	 * Status (only entries if not native)
    	 * Literature number
    	 * Literature
    	*/

        String editName = "";
        List<String> distributionList = new ArrayList<String>();
        String status = "";
        String literatureNumber = "";
        String literature = "";

    	Set<String> keys = record.keySet();

    	for (String key: keys) {

    		String value = record.get(key);
    		if (!value.equals("")) {
    			if (logger.isDebugEnabled()) { logger.debug(key + ": '" + value + "'"); }
    		}

    		if (key.contains(EDIT_NAME_COLUMN)) {
    			editName = (String) CdmUtils.removeDuplicateWhitespace(value.trim());

			} else if(key.contains(TDWG_DISTRIBUTION_COLUMN)) {
				distributionList =  CdmUtils.buildList(value);

			} else if(key.contains(STATUS_COLUMN)) {
				status = (String) CdmUtils.removeDuplicateWhitespace(value.trim());

//			} else if(key.contains(LITERATURE_NUMBER_COLUMN)) {
//				literatureNumber = (String) CdmUtils.removeDuplicateWhitespace(value.trim());
//
//			} else if(key.contains(LITERATURE_COLUMN)) {
//				literature = (String) CdmUtils.removeDuplicateWhitespace(value.trim());
//
			} else {
				//logger.warn("Column " + key + " ignored");
			}
    	}

    	// Store the data of this record in the DB
    	if (!editName.equals("")) {
    		saveRecord(editName, distributionList, status, literatureNumber, literature);
    	}
    }

	/**
	 *  Stores the data of one Excel sheet row in the database
	 */
    private void saveRecord(String taxonName, List<String> distributionList,
    		String status, String literatureNumber, String literature) {

    	IdentifiableServiceConfiguratorImpl<TaxonName> config = IdentifiableServiceConfiguratorFactory.getConfigurator(TaxonName.class);
    	config.setTitleSearchString(taxonName);
    	config.setMatchMode(MatchMode.BEGINNING);

		try {
    		// get the matching names from the DB
    		//List<TaxonName> taxonNames = getNameService().findByTitle(config);
    		List<TaxonName> taxonNames = getNameService().findByName(null, taxonName, null, null, null, null,null,null).getRecords();
    		if (taxonNames.isEmpty()) {
    			logger.error("Taxon name '" + taxonName + "' not found in DB");
    		} else {
    			if (logger.isDebugEnabled()) { logger.debug("Taxon found"); }
    		}

    		// get the taxa for the matching names
    		for(TaxonName dbTaxonName: taxonNames) {

    			Set<Taxon> taxa = dbTaxonName.getTaxa();
    			if (taxa.isEmpty()) {
    				logger.warn("No taxon found for name '" + taxonName + "'");
    			} else if (taxa.size() > 1) {
    				logger.warn("More than one taxa found for name '" + taxonName + "'");
    			}

    			for(Taxon taxon: taxa) {

    				TaxonDescription myDescription = null;

    				// If we have created a description for this taxon earlier, take this one.
    				// Otherwise, create a new description.
    				// We don't update any existing descriptions in the database at this point.
    				if (myDescriptions.containsKey(taxon)) {
    					myDescription = myDescriptions.get(taxon);
    				} else {
    					myDescription = TaxonDescription.NewInstance(taxon);
    					taxon.addDescription(myDescription);
    					myDescriptions.put(taxon, myDescription);
    				}

    				// Status
    				PresenceAbsenceTerm presenceAbsenceStatus = PresenceAbsenceTerm.NewInstance();
    				if (status.equals("")) {
    					presenceAbsenceStatus = PresenceAbsenceTerm.NATIVE();
    				} else {
    					presenceAbsenceStatus = PresenceAbsenceTerm.getPresenceAbsenceTermByAbbreviation(status);
    				}
    				// TODO: Handle absence case.
    				// This case has not yet occurred in the excel input file, though.

    				/* Set to true if taxon needs to be saved if at least one new distribution exists */
    				boolean save = false;

    				// TDWG areas
    				for (String distribution: distributionList) {

                        /* Set to true if this distribution is a new one*/
        				boolean ignore = false;

    					if(!distribution.equals("")) {
    						NamedArea namedArea = TdwgAreaProvider.getAreaByTdwgAbbreviation(distribution);
        					TaxonDescription taxonDescription = myDescriptions.get(taxon);
        					if (namedArea != null) {
    		    				// Check against existing distributions and ignore the ones that occur multiple times
            					Set<DescriptionElementBase> myDescriptionElements = taxonDescription.getElements();
    	    					for(DescriptionElementBase descriptionElement : myDescriptionElements) {
    	    						if (descriptionElement instanceof Distribution) {
    	    							if (namedArea == ((Distribution)descriptionElement).getArea()) {
    	    								ignore = true;
    	    								if (logger.isDebugEnabled()) {
    	    									logger.debug("Distribution ignored: " + distribution);
    	    								}
    	    		    					break;
     	    							}
    	    						}
    	    					}
    	    					// Create new distribution if not yet exist
    	    					if (ignore == false) {
    	    						save = true;
    	    						Distribution newDistribution = Distribution.NewInstance(namedArea, presenceAbsenceStatus);
    	    						myDescription.addElement(newDistribution);
    	    						if (logger.isDebugEnabled()) {
    	    							logger.debug("Distribution created: " + newDistribution.toString());
    	    						}
    	    					}
    						}
    					}
    				}
    				if (save == true) {
    					getTaxonService().save(taxon);
    					if (logger.isDebugEnabled()) { logger.debug("Taxon saved"); }
    				}
    			}
    		}
    	} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
    	}
    }

	@Override
	protected boolean doCheck(ExcelImportState<ExcelImportConfiguratorBase, ExcelRowBase> state) {
		boolean result = true;
		logger.warn("No check implemented for distribution data import");
		return result;
	}

	@Override
	protected boolean isIgnore(ExcelImportState<ExcelImportConfiguratorBase, ExcelRowBase> state) {
		return false;
	}
}