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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.support.DefaultTransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.distribution.excelupdate.ExcelDistributionUpdateConfigurator;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImportConfigurator;
import eu.etaxonomy.cdm.io.excel.taxa.TaxonListImportConfigurator;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.babadshanjan
 * @since 17.12.2008
 */
public abstract class ExcelImportBase<STATE extends ExcelImportState<CONFIG, ROW>, CONFIG extends ExcelImportConfiguratorBase, ROW extends ExcelRowBase>
        extends CdmImportBase<CONFIG, STATE> {

    private static final long serialVersionUID = 2759164811664484732L;
    private static final Logger logger = LogManager.getLogger();

	protected static final String SCIENTIFIC_NAME_COLUMN = "ScientificName";

	private List<Map<String, String>> recordList = null;

	private ExcelImportConfiguratorBase configurator = null;


	/** Reads data from an Excel file and stores them into a CDM DB.
     *
     * @param config
     * @param stores (not used)
     */
	@Override
	protected void doInvoke(STATE state){

		logger.debug("Importing excel data");

		//cleanup state from prior session
		state.setSourceReference(null);

    	configurator = state.getConfig();

		NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
		if (nc == null && requiresNomenclaturalCode()) {
			logger.error("Nomenclatural code could not be determined. Skip invoke.");
			state.setUnsuccessfull();
			return;
		}
		URI source = null;

		byte[] data = null;
		// read and save all rows of the excel worksheet
		if ((state.getConfig() instanceof NormalExplicitImportConfigurator
		        || state.getConfig() instanceof ExcelDistributionUpdateConfigurator
		        || state.getConfig() instanceof TaxonListImportConfigurator) && (state.getConfig().getStream() != null)
		    ){
		    data =  state.getConfig().getStream();
		} else{
		    source = state.getConfig().getSource();
		}

		String sheetName = getWorksheetName(state.getConfig());

		if (data != null){
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(data);
                recordList = ExcelUtils.parseXLS(stream, sheetName);
            } catch (Exception e) {
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

	protected boolean requiresNomenclaturalCode() {
		return true;
	}

	private void handleRecordList(STATE state, URI source) {
		Integer startingLine = 2;
		if (recordList != null) {
    		Map<String,String> record = null;

    		state.setTransactionStatus(startTransaction());

    		//first pass
    		state.setCurrentLine(startingLine);
    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			analyzeRecord(record, state);
    			state.setOriginalRecord(record);
    			try {
					firstPass(state);
					//for debugging only
//					if (i % 1000 == 0){
//					    try {
//                            System.out.println(i);
//					        getSession().flush();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//					}
					DefaultTransactionStatus defStatus = (DefaultTransactionStatus) state.getTransactionStatus();
			        if (defStatus.isRollbackOnly()){
			            logger.warn("Rollback only in line: " + i);
			        }
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
    		commitTransaction(state.getTransactionStatus());
    	}else{
    		logger.warn("No records found in " + source);
    	}
		return;
	}

	/**
	 * To define a worksheet name other then the one defined in the configurator
	 * override this method with a non <code>null</code> return value.
	 * If <code>null</code> is returned the first worksheet is taken.

	 * @return worksheet name. <code>null</null> if no worksheet is defined.
	 */
	protected String getWorksheetName(CONFIG config) {
		return config.getWorksheetName();
	}

	@Override
	protected boolean doCheck(STATE state) {
		boolean result = true;
		logger.warn("No check implemented for Excel import");
		return result;
	}

	protected abstract void analyzeRecord(Map<String,String> record, STATE state);

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
    protected static String getValue(Map<String, String> record, String originalKey) {
        String value = record.get(originalKey);
        if (! StringUtils.isBlank(value)) {
            if (logger.isDebugEnabled()) { logger.debug(originalKey + ": " + value); }
            value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
            return value;
        }else{
            return null;
        }
    }

    protected String getValue(STATE state, String key){
        key = state.getConfig().replaceColumnLabel(key);
        return getValue(state.getOriginalRecord(), key);
    }

    /**
     * Returns the taxon for the given CDM uuid. If no taxon exists for the given id
     * no record is returned. If a name cache, name title cache (full name) or
     * taxon title cache column is given the name is checked against the given columns.
     * If they don't manage it is logged as a warning in import result.
     * <BR>If clazz is given, only objects of the given class are loaded.
     *
     *
     * @param state
     * @param colTaxonUuid taxon uuid column
     * @param colNameCache name cache column (if exists)
     * @param colNameTitleCache name title cache column (if exists)
     * @param colTaxonTitleCache taxon title cache column (if exists)
     * @param clazz the clazz null
     * @param line the row, for debug information
     * @return the taxon to load
     */
    protected <T extends TaxonBase<?>> T getTaxonByCdmId(STATE state, String colTaxonUuid,
            String colNameCache, String colNameTitleCache, String colTaxonTitleCache,
            Class<T> clazz, String line) {

        Map<String, String> record = getRecord(state);
        String strUuidTaxon = record.get(colTaxonUuid);
        if (strUuidTaxon != null){
            UUID uuidTaxon;
            try {
                uuidTaxon = UUID.fromString(strUuidTaxon);
            } catch (Exception e) {
                state.getResult().addError("Taxon uuid has incorrect format. Taxon could not be loaded. Data not imported.", null, line);
                return null;
            }
            TaxonBase<?> result = getTaxonService().find(uuidTaxon);
            //TODO load only objects of correct class
            if (result != null && clazz != null && !result.isInstanceOf(clazz)){
                result = null;
            }


            if (result == null){
                state.getResult().addError("Taxon for uuid  "+strUuidTaxon+" could not be found in database. "
                        + "Taxon could not be loaded. Data not imported.", null, line);
            }else{
                verifyName(state, colNameCache, colNameTitleCache, colTaxonTitleCache, line, record, result);
            }
            result = CdmBase.deproxy(result, clazz);


            return CdmBase.deproxy(result, clazz);
        }else{
            String message = "No taxon identifier column found";
            state.getResult().addWarning(message, null, line);
            return null;
        }
    }

    protected Map<String, String> getRecord(STATE state) {
        Map<String, String> record = state.getOriginalRecord();
        return record;
    }

    /**
     * @see #getTaxonByCdmId(ExcelImportState, String, String, String, String, Class, String)
     */
    protected void verifyName(STATE state, String colNameCache, String colNameTitleCache, String colTaxonTitleCache,
            String line, Map<String, String> record, TaxonBase<?> result) {
        //nameCache
        String strExpectedNameCache = record.get(colNameCache);
        String nameCache = result.getName() == null ? null : result.getName().getNameCache();
        if (isNotBlank(strExpectedNameCache) && (!strExpectedNameCache.trim().equals(nameCache))){
            String message = "Name cache (%s) does not match expected name (%s)";
            message = String.format(message, nameCache==null? "null":nameCache, strExpectedNameCache);
            state.getResult().addWarning(message, null, line);
        }
        //name title
        String strExpectedNameTitleCache = record.get(colNameTitleCache);
        String nameTitleCache = result.getName() == null ? null : result.getName().getTitleCache();
        if (isNotBlank(strExpectedNameTitleCache) && (!strExpectedNameTitleCache.trim().equals(nameTitleCache))){
            String message = "Name title cache (%s) does not match expected name (%s)";
            message = String.format(message, nameTitleCache==null? "null":nameTitleCache, strExpectedNameTitleCache);
            state.getResult().addWarning(message, null, line);
        }
        //taxon title cache
        String strExpectedTaxonTitleCache = record.get(colTaxonTitleCache);
        String taxonTitleCache = result.getTitleCache();
        if (isNotBlank(strExpectedTaxonTitleCache) && (!strExpectedTaxonTitleCache.trim().equals(taxonTitleCache))){
            String message = "Name cache (%s) does not match expected name (%s)";
            message = String.format(message, taxonTitleCache==null? "null":taxonTitleCache, strExpectedTaxonTitleCache);
            state.getResult().addWarning(message, null, line);
        }
    }

    /**
     * Non transaction save method to retrieve the source reference
     * if either existent or not in the database (uses check for uuid).
     *
     * @param state
     * @return the source reference
     */
    protected Reference getSourceReference(STATE state) {

        Reference sourceRef = state.getSourceReference();
        if (sourceRef != null){
            return sourceRef;
        }
        UUID uuid = state.getConfig().getSourceRefUuid();
        if (uuid == null){
            sourceRef = state.getConfig().getSourceReference();
            if (sourceRef != null){
                uuid = sourceRef.getUuid();
            }
        }
        if (uuid != null){
            Reference existingRef = getReferenceService().find(uuid);
            if (existingRef != null){
                sourceRef = existingRef;
            }
//            else if (sourceRef != null){
//                getReferenceService().save(sourceRef);
//            }
        }
        if (sourceRef == null){
            sourceRef = ReferenceFactory.newGeneric();
            String title = state.getConfig().getSourceNameString();
            sourceRef.setTitle(title);
            state.getConfig().setSourceReference(sourceRef);
        }
        state.setSourceReference(sourceRef);

        return sourceRef;
    }
}