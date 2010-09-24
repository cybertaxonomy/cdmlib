/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.common;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.babadshanjan
 * @created 17.12.2008
 * @version 1.0
 */
public abstract class ExcelImporterBase<STATE extends ExcelImportState> extends CdmImportBase<ExcelImportConfiguratorBase, STATE> {
	private static final Logger logger = Logger.getLogger(ExcelImporterBase.class);

	protected static final String SCIENTIFIC_NAME_COLUMN = "ScientificName";
	
	ArrayList<HashMap<String, String>> recordList = null;
	
	private CdmApplicationController appCtr = null;
	private ExcelImportConfiguratorBase configurator = null;

	
	/** Reads data from an Excel file and stores them into a CDM DB.
     * 
     * @param config
     * @param stores (not used)
     */
	@Override
	protected boolean doInvoke(STATE state){
		
		boolean success = false;
		
    	logger.debug("Importing excel data");
    	
    	configurator = state.getConfig();
    	
		NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
		if (nc == null) {
			logger.error("Nomenclatural code could not be determined.");
			return false;
		}
		// read and save all rows of the excel worksheet
		URI source = state.getConfig().getSource();
		try {
			recordList = ExcelUtils.parseXLS(source);
		} catch (FileNotFoundException e) {
			String message = "File not found: " + source;
			warnProgress(state, message, e);
			logger.error(message);
			return false;
		}
    	
    	if (recordList != null) {
    		HashMap<String,String> record = null;
    		
    		TransactionStatus txStatus = startTransaction();

    		//first pass
    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			success = analyzeRecord(record, state);
    			success = firstPass(state);
    		}
    		//second pass
    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			success = analyzeRecord(record, state);
    			success = secondPass(state);
        	}
    		
    		commitTransaction(txStatus);
    	}else{
    		logger.warn("No records found in " + source);
    	}
    	
		try {
	    	logger.debug("End excel data import"); 
				
		} catch (Exception e) {
    		logger.error("Error closing the application context");
    		e.printStackTrace();
		}
    	
    	return success;
	}

	@Override
	protected boolean doCheck(STATE state) {
		boolean result = true;
		logger.warn("No check implemented for Excel import");
		return result;
	}
	
	/** 
	 * 
	 * 
	 * @param record
	 * @return
	 */
	protected abstract boolean analyzeRecord(HashMap<String,String> record, STATE state);
	
	protected abstract boolean firstPass(STATE state);
	protected abstract boolean secondPass(STATE state);
	
	
	public ExcelImportConfiguratorBase getConfigurator() {
		return configurator;
	}
	
	
	public CdmApplicationController getApplicationController() {
		return appCtr;
	}
	
	
	protected int floatString2IntValue(String value) {
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
	
	protected String floatString2IntStringValue(String value) {
		int i = floatString2IntValue(value);
		return String.valueOf(i);
	}


}
