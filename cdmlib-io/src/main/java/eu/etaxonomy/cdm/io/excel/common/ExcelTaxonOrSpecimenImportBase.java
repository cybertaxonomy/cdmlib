/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.common;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.mueller
 * @date 12.07.2011
 */
public abstract class ExcelTaxonOrSpecimenImportBase<STATE extends ExcelImportState<? extends ExcelImportConfiguratorBase, ROW>, ROW extends ExcelRowBase> extends ExcelImporterBase<STATE> {
	private static final Logger logger = Logger.getLogger(ExcelTaxonOrSpecimenImportBase.class);


	protected static final String CDM_UUID_COLUMN = "(?i)(CdmUuid)";

	
	protected static final String RANK_COLUMN = "(?i)(Rank)";
	protected static final String FULL_NAME_COLUMN = "(?i)(FullName)";
	protected static final String FAMILY_COLUMN = "(?i)(Family)";
	protected static final String GENUS_COLUMN = "(?i)(Genus)";
	protected static final String SPECIFIC_EPITHET_COLUMN = "(?i)(SpecificEpi(thet)?)";
	protected static final String INFRASPECIFIC_EPITHET_COLUMN = "(?i)(InfraSpecificEpi(thet)?)";

	@Override
	protected boolean analyzeRecord(HashMap<String, String> record, STATE state) {
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	ROW row = createDataHolderRow();
    	state.setCurrentRow(row);
    	
    	for (String originalKey: keys) {
    		KeyValue keyValue = makeKeyValue(record, originalKey);
    		if (StringUtils.isBlank(keyValue.value)){
    			continue;
    		}
    		if (isBaseColumn(keyValue)){
    			success &= handleBaseColumn(keyValue, row);
    		}else{
    			success &= analyzeSingleValue(keyValue, state);
    		}
    	}
    	return success;
	}
	
	protected abstract ROW createDataHolderRow();

	/**
	 * Analyzes a single record value and fills the row instance accordingly.
	 * @param keyValue
	 * @param state 
	 * @return
	 */
	protected abstract boolean analyzeSingleValue(KeyValue keyValue, STATE state);

	/**
	 *	DataHolder class for all key and value information for a cell.
	 * Value is the value of the cell (as String). Key is the main attribute, further defined by postfix,
	 * and in case of multiple values indexed.
	 * TODO doc for refXXX
	 */
	protected class KeyValue{
		public KeyValue() {}
		
		public String value;
		public String key;
		public String postfix;
		public Integer index;
		public String ref;
		public String refAuthor;
		public String refIndex;
		public String originalKey;
	}
	

	/**
	 * @param record
	 * @param originalKey
	 * @param keyValue
	 * @return
	 */
	protected KeyValue makeKeyValue(HashMap<String, String> record, String originalKey) {
		KeyValue keyValue = new KeyValue();
		keyValue.originalKey = originalKey;
		String indexedKey = CdmUtils.removeDuplicateWhitespace(originalKey.trim()).toString();
		String[] split = indexedKey.split("_");
		keyValue.key = split[0];
		if (split.length > 1){
			for (int i = 1 ; i < split.length ; i++ ){
				String indexString = split[i];
				if (isInteger(indexString)){
					keyValue.index = Integer.valueOf(indexString);
				}else{
					keyValue.postfix = split[i];
				}
			}
		}
		
		//TODO shouldn't we use originalKey here??
		String value = (String) record.get(indexedKey);
		if (! StringUtils.isBlank(value)) {
			if (logger.isDebugEnabled()) { logger.debug(keyValue.key + ": " + value); }
			value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
			keyValue.value = value;
		}else{
			keyValue.value = null;
		}
		return keyValue;
	}

	
	private boolean handleBaseColumn(KeyValue keyValue, ExcelRowBase row) {
		String key = keyValue.key;
		String value = keyValue.value;
		if (key.matches(CDM_UUID_COLUMN)) {
			row.setCdmUuid(UUID.fromString(value)); //VALIDATE UUID	
		}
		return true;
	}

	private boolean isBaseColumn(KeyValue keyValue) {
		String key = keyValue.key;
		if (key.matches(CDM_UUID_COLUMN)){
			return true;
		}
		return false;
	}
	
	protected boolean isInteger(String value){
		try {
			Integer.valueOf(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	


	protected void fireWarningEvent(String message, STATE state, int severity) {
		fireWarningEvent(message, "Record" + state.getCurrentLine(), severity, 1);
	}
}
