/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.common;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.distribution.excelupdate.ExcelDistributionUpdateConfigurator;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImportConfigurator;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
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

	private ArrayList<HashMap<String, String>> recordList = null;

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
		URI source = null;

		byte[] data = null;
		// read and save all rows of the excel worksheet
		if ((state.getConfig() instanceof NormalExplicitImportConfigurator || state.getConfig() instanceof ExcelDistributionUpdateConfigurator) &&
		        (state.getConfig().getStream() != null || state.getConfig().getStream() != null)){
		    data =  state.getConfig().getStream();
		} else{
		    source = state.getConfig().getSource();
		}



		String sheetName = getWorksheetName();


		if (data != null){
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(data);
                recordList = ExcelUtils.parseXLS(stream, sheetName);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else{
    		try {
    			recordList = ExcelUtils.parseXLS(source, sheetName);
    		} catch (FileNotFoundException e) {
    			String message = "File not found: " + source;
    			warnProgress(state, message, e);
    			logger.error(message);
    			state.setUnsuccessfull();
    			return;
    		}
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
    		if (configurator.isDeduplicateReferences()){
    		    getReferenceService().deduplicate(Reference.class, null, null);
    		}
    		if (configurator.isDeduplicateAuthors()){
                getAgentService().deduplicate(TeamOrPersonBase.class, null, null);
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

    /**
     * Returns the value of the record map for the given key.
     * The value is trimmed and empty values are set to <code>null</code>.
     * @param record
     * @param originalKey
     * @return the value
     */
    protected String getValue(Map<String, String> record, String originalKey) {
        String value = record.get(originalKey);
        if (! StringUtils.isBlank(value)) {
            if (logger.isDebugEnabled()) { logger.debug(originalKey + ": " + value); }
            value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
            return value;
        }else{
            return null;
        }
    }
}
