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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase.PostfixTerm;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenCdmExcelImportState;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenRow;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * @author a.mueller
 * @since 12.07.2011
 */
public abstract class ExcelTaxonOrSpecimenImportBase<STATE extends ExcelImportState<CONFIG, ROW>, CONFIG extends ExcelImportConfiguratorBase, ROW extends ExcelRowBase>
            extends ExcelImportBase<STATE, CONFIG, ROW> {
	private static final Logger logger = Logger.getLogger(ExcelTaxonOrSpecimenImportBase.class);


	protected static final String CDM_UUID_COLUMN = "(?i)(CdmUuid)";
	protected static final String IGNORE_COLUMN = "(?i)(Ignore|Not)";


	protected static final String RANK_COLUMN = "(?i)(Rank)";
	protected static final String FULL_NAME_COLUMN = "(?i)(FullName)";
	protected static final String TAXON_UUID_COLUMN = "(?i)(taxonUuid)";
	protected static final String FAMILY_COLUMN = "(?i)(Family)";
	protected static final String GENUS_COLUMN = "(?i)(Genus)";
	protected static final String SPECIFIC_EPITHET_COLUMN = "(?i)(SpecificEpi(thet)?)";
	protected static final String INFRASPECIFIC_EPITHET_COLUMN = "(?i)(InfraSpecificEpi(thet)?)";

	protected static final String LANGUAGE = "(?i)(Language)";

	@Override
	protected void analyzeRecord(HashMap<String, String> record, STATE state) {
		Set<String> keys = record.keySet();

    	ROW row = createDataHolderRow();
    	state.setCurrentRow(row);

    	for (String originalKey: keys) {
    		KeyValue keyValue = makeKeyValue(record, originalKey, state);
    		if (StringUtils.isBlank(keyValue.value)){
    			continue;
    		}
    		if (isBaseColumn(keyValue)){
    			handleBaseColumn(keyValue, row);
    		}else{
    			analyzeSingleValue(keyValue, state);
    		}
    	}
    	return;
	}

	protected abstract ROW createDataHolderRow();

	/**
	 * Analyzes a single record value and fills the row instance accordingly.
	 * @param keyValue
	 * @param state
	 * @return
	 */
	protected abstract void analyzeSingleValue(KeyValue keyValue, STATE state);

	/**
	 *	DataHolder class for all key and value information for a cell.
	 * Value is the value of the cell (as String). Key is the main attribute, further defined by postfix,
	 * and in case of multiple values indexed.
	 * TODO doc for refXXX
	 */
	protected class KeyValue{
		public KeyValue() {}

		//original Key
		public String originalKey;
		//value
		public String value;
		//atomized key
		public String key;
		public String postfix;
		public int index = 0;
		public SourceType refType;
		public int refIndex = 0;
		public boolean hasError = false;
		public boolean isKeyData() {
			return (refType == null);
		}
		public boolean isLanguage(){
			return (refType.isLanguage());
		}
	}

	public enum SourceType{
		Author("RefAuthor"),
		Title("RefTitle"),
		Year("RefYear"),
		RefExtension("RefExt(ension)?"),
		Language("Lang") //strictly not a reference, so some refactoring/renaming is needed
		;

		String keyMatch = null;
		private SourceType(String keyName){
			this.keyMatch = keyName;
		}


		boolean isLanguage(){
			return (this.equals(Language));
		}

		static SourceType byKeyName(String str){
			if (StringUtils.isBlank(str)){
				return null;
			}
			for (SourceType type : SourceType.values()){
				if (str.matches("(?i)(" + type.keyMatch + ")")){
					return type;
				}
			}
			return null;
		}

		static boolean isKeyName(String str){
			return (byKeyName(str) != null);
		}

	}


	/**
	 * @param record
	 * @param originalKey
	 * @param state
	 * @param keyValue
	 * @return
	 */
	protected KeyValue makeKeyValue(HashMap<String, String> record, String originalKey, STATE state) {
		KeyValue keyValue = new KeyValue();
		keyValue.originalKey = originalKey;
		String indexedKey = CdmUtils.removeDuplicateWhitespace(originalKey.trim()).toString();
		String[] split = indexedKey.split("_");
		int current = 0;
		//key
		keyValue.key = split[current++];
		//postfix
		if (split.length > current && ! isRefType(split[current]) && ! isInteger(split[current]) ){
			keyValue.postfix = split[current++];
		}
		//index
		if (split.length > current && isInteger(split[current]) ){
			keyValue.index = Integer.valueOf(split[current++]);
		}else{
			keyValue.index = 0;
		}
		//source
		if (split.length > current && ! isIgnore(keyValue.key)){
			//refType
			if (isRefType(split[current])){
				String refTypeStr = split[current++];
				keyValue.refType = SourceType.byKeyName(refTypeStr);
				if (keyValue.refType == null){
					String message = "Unmatched source key: " + refTypeStr;
					fireWarningEvent(message, state, 10);
					logger.warn(message);
				}
			}else {
				String message = "RefType expected at %d position of key. But %s is no valid reftype";
				message = String.format(message, current, split[current]);
				fireWarningEvent(message, state, 10);
				logger.warn(message);
				keyValue.hasError  = true;
			}
			//ref index
			if (split.length > current){
				 if (isInteger(split[current])){
					 keyValue.refIndex = Integer.valueOf(split[current++]);
				 }else{
					String message = "Ref index expected at position %d of key. But %s is no valid reftype";
					message = String.format(message, current, split[current]);
					fireWarningEvent(message, state, 10);
					logger.warn(message);
					keyValue.hasError = true;
				 }
			}else {
				keyValue.refIndex = 0;
			}

		}
		if (split.length > current  && ! isIgnore(keyValue.key)){
			String message = "Key has unexpected part at position %d of key. %s (and following parts) can not be handled";
			message = String.format(message, current, split[current]);
			fireWarningEvent(message, state, 10);
			logger.warn(message);
			keyValue.hasError = true;
		}

		//TODO shouldn't we use originalKey here??
		String value = record.get(indexedKey);
		if (! StringUtils.isBlank(value)) {
			if (logger.isDebugEnabled()) { logger.debug(keyValue.key + ": " + value); }
			value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
			keyValue.value = value;
		}else{
			keyValue.value = null;
		}
		return keyValue;
	}


	private boolean isIgnore(String key) {
		return key.matches(IGNORE_COLUMN);
	}

	private boolean isRefType(String string) {
		return SourceType.isKeyName(string);
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
		} else if(isIgnore(keyValue.key)) {
			logger.debug("Ignored column" + keyValue.originalKey);
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


	protected boolean analyzeFeatures(STATE state, KeyValue keyValue) {
		String key = keyValue.key;
		Pager<DefinedTermBase> features = getTermService().findByTitle(Feature.class, key, null, null, null, null, null, null);

		if (features.getCount() > 1){
			String message = "More than one feature found matching key " + key;
			fireWarningEvent(message, state, 4);
			return false;
		}else if (features.getCount() == 0){
			return false;
		}else{
			Feature feature = CdmBase.deproxy(features.getRecords().get(0), Feature.class);
			ROW row = state.getCurrentRow();
			if ( keyValue.isKeyData()){
				row.putFeature(feature.getUuid(), keyValue.index, keyValue.value);
			}else if (keyValue.isLanguage()){
				row.putFeatureLanguage(feature.getUuid(), keyValue.index, keyValue.value);
			}else{
				row.putFeatureSource(feature.getUuid(), keyValue.index, keyValue.refType, keyValue.value, keyValue.refIndex);
			}
			return true;
		}
	}


	protected void handleExtensions(IdentifiableEntity<?> identifiable, SpecimenRow row, SpecimenCdmExcelImportState state) {
		List<PostfixTerm> extensions = row.getExtensions();

		for (PostfixTerm exType : extensions){
			ExtensionType extensionType = state.getPostfixExtensionType(exType.postfix);

			Extension extension = Extension.NewInstance();
			extension.setType(extensionType);
			extension.setValue(exType.term);
			identifiable.addExtension(extension);
		}

	}


	protected void fireWarningEvent(String message, STATE state, int severity) {
		fireWarningEvent(message, "Record" + state.getCurrentLine(), severity, 1);
	}
}
