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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.mueller
 * @date 12.07.2011
 */
public abstract class ExcelTaxonOrSpecimenImportBase<STATE extends ExcelImportState<? extends ExcelImportConfiguratorBase>> extends ExcelImporterBase<STATE> {
	private static final Logger logger = Logger.getLogger(ExcelTaxonOrSpecimenImportBase.class);


	protected static final String UUID_COLUMN = "(?i)(UUID)";

	
	protected static final String RANK_COLUMN = "(?i)(Rank)";
	protected static final String FULL_NAME_COLUMN = "(?i)(FullName)";
	protected static final String FAMILY_COLUMN = "(?i)(Family)";
	protected static final String GENUS_COLUMN = "(?i)(Genus)";
	protected static final String SPECIFIC_EPITHET_COLUMN = "(?i)(SpecificEpi(thet)?)";
	protected static final String INFRASPECIFIC_EPITHET_COLUMN = "(?i)(InfraSpecificEpi(thet)?)";

	
	protected class KeyValue{
		public KeyValue() {}
		
		public String key;
		public String value;
		public String postfix;
		public Integer index;
		public String ref;
		public String refAuthor;
		public String refIndex;
	}
	

	/**
	 * @param record
	 * @param originalKey
	 * @param keyValue
	 * @return
	 */
	protected KeyValue makeKeyValue(HashMap<String, String> record, String originalKey) {
		KeyValue keyValue = new KeyValue();
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

	
	protected boolean isInteger(String value){
		try {
			Integer.valueOf(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
