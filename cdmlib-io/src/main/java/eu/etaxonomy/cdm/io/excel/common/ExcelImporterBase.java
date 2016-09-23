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
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.babadshanjan
 * @created 17.12.2008
 */
public abstract class ExcelImporterBase<STATE extends ExcelImportState<? extends ExcelImportConfiguratorBase, ? extends ExcelRowBase>>
        extends CdmImportBase<ExcelImportConfiguratorBase, STATE> {
    private static final long serialVersionUID = 2759164811664484732L;

    private static final Logger logger = Logger.getLogger(ExcelImporterBase.class);

	protected static final String SCIENTIFIC_NAME_COLUMN = "ScientificName";


	ArrayList<HashMap<String, String>> recordList = null;

	private final CdmApplicationController appCtr = null;
	private ExcelImportConfiguratorBase configurator = null;


	/** Reads data from an Excel file and stores them into a CDM DB.
     *
     * @param config
     * @param stores (not used)
     */
	@Override
	protected void doInvoke(STATE state){

		logger.debug("Importing excel data");

    	configurator = state.getConfig();

		NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
		if (nc == null && needsNomenclaturalCode()) {
			logger.error("Nomenclatural code could not be determined. Skip invoke.");
			state.setUnsuccessfull();
			return;
		}
		// read and save all rows of the excel worksheet
		URI source = state.getConfig().getSource();
		String sheetName = getWorksheetName();
		try {
			recordList = ExcelUtils.parseXLS(source, sheetName);
		} catch (FileNotFoundException e) {
			String message = "File not found: " + source;
			warnProgress(state, message, e);
			logger.error(message);
			state.setUnsuccessfull();
			return;
		}

    	handleRecordList(state, source);
    	logger.debug("End excel data import");
    	return;
	}

	protected boolean needsNomenclaturalCode() {
		return true;
	}

	/**
	 * @param state
	 * @param success
	 * @param source
	 * @return
	 */
	private void handleRecordList(STATE state, URI source) {
		Integer startingLine = 2;
		if (recordList != null) {
    		HashMap<String,String> record = null;

    		TransactionStatus txStatus = startTransaction();

    		//first pass
    		state.setCurrentLine(startingLine);
    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			analyzeRecord(record, state);
    			state.setOriginalRecord(record);
    			try {
					firstPass(state);
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					state.incCurrentLine();
				}
    		}
    		//second pass
    		state.setCurrentLine(startingLine);
    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			analyzeRecord(record, state);
    			state.setOriginalRecord(record);
                secondPass(state);
    			state.incCurrentLine();
    	   	}

    		commitTransaction(txStatus);
    	}else{
    		logger.warn("No records found in " + source);
    	}
		return;
	}

	/**
	 * To define a worksheet name override this method. Otherwise the first worksheet is taken.
	 * @return worksheet name. <code>null</null> if not worksheet is defined.
	 */
	protected String getWorksheetName() {
		return null;
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
	protected abstract void analyzeRecord(HashMap<String,String> record, STATE state);

	protected abstract void firstPass(STATE state);
	protected abstract void secondPass(STATE state);


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


	/**
	 * @param start
	 * @param end
	 * @return
	 */
	protected TimePeriod getTimePeriod(String start, String end) {
		String strPeriod = CdmUtils.concat(" - ", start, end);
		TimePeriod result = TimePeriodParser.parseString(strPeriod);
		return result;
	}


}
