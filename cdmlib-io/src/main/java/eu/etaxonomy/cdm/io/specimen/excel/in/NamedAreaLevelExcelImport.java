/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;

/**
 * @author p.kelbert
 * @created 29.10.2008
 * @version 1.0
 */
@Component
public class NamedAreaLevelExcelImport  extends ExcelImporterBase<SpecimenCdmExcelImportState>  implements ICdmIO<SpecimenCdmExcelImportState> {
	private static final Logger logger = Logger.getLogger(NamedAreaLevelExcelImport.class);

	private static final String WORKSHEET_NAME = "AreaLevels";

	private static final String UUID_COLUMN = "UUID";
	private static final String LABEL_COLUMN = "Label";
	private static final String ABBREVIATION_COLUMN = "Abbreviation";
	private static final String DESCRIPTION_COLUMN = "Description";
	private static final String POSTFIX_COLUMN = "Postfix";
	private static final String GEOSERVER_LABEL_COLUMN = "GeoserverLabel";
	private static final String GEOSERVER_ATTRIBUTE_COLUMN = "GeoserverAttribute";
	private static final String ORDER_INDEX_COLUMN = "OrderIndex";

	
	public NamedAreaLevelExcelImport() {
		super();
	}
	
	@Override
	protected void analyzeRecord(HashMap<String, String> record, SpecimenCdmExcelImportState state) {
		Set<String> keys = record.keySet();
    	
    	NamedAreaLevellRow row = new NamedAreaLevellRow();
    	state.setNamedAreaLevelRow(row);
    	
    	for (String originalKey: keys) {
    		Integer index = 0;
    		String indexedKey = CdmUtils.removeDuplicateWhitespace(originalKey.trim()).toString();
    		String[] split = indexedKey.split("_");
    		String key = split[0];
    		if (split.length > 1){
    			String indexString = split[split.length - 1];
    			try {
    				index = Integer.valueOf(indexString);
				} catch (NumberFormatException e) {
					String message = "Index must be integer";
					logger.error(message);
					continue;
				}
    		}
    		
    		String value = (String) record.get(indexedKey);
    		if (! StringUtils.isBlank(value)) {
    			if (logger.isDebugEnabled()) { logger.debug(key + ": " + value); }
        		value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
    		}else{
    			continue;
    		}
    		
    		if (key.equalsIgnoreCase(UUID_COLUMN)) {
    			row.setUuid(UUID.fromString(value)); //VALIDATE UUID
 			} else if(key.equalsIgnoreCase(LABEL_COLUMN)) {
				row.setLabel(value);
			} else if(key.equalsIgnoreCase(ABBREVIATION_COLUMN)) {
				row.setAbbreviation(value);
			} else if(key.equalsIgnoreCase(DESCRIPTION_COLUMN)) {
				row.setDescription(value);
			} else if(key.equalsIgnoreCase(POSTFIX_COLUMN)) {
				row.setPostfix(value);
			} else if(key.equalsIgnoreCase(GEOSERVER_LABEL_COLUMN)) {
				row.setGeoserverLabel(value);
			} else if(key.equalsIgnoreCase(GEOSERVER_ATTRIBUTE_COLUMN)) {
				row.setGeoServerAttribute(value);		
			} else if(key.equalsIgnoreCase(ORDER_INDEX_COLUMN)) {
				row.setOrderIndex(value);		
			}else {
				state.setUnsuccessfull();
				logger.error("Unexpected column header " + key);
			}
    	}
    	return;
	}


	@Override
	protected void firstPass(SpecimenCdmExcelImportState state) {
		NamedAreaLevellRow row = state.getNamedAreaLevelRow();
		
		//level
		UUID uuid = row.getUuid();
		String label = row.getAbbreviation();
		String text = row.getDescription();
		String labelAbbrev = row.getAbbreviation();
		
		NamedAreaLevel level = getNamedAreaLevel(state, uuid, label, text, labelAbbrev, null);
		
		//TODO orderIndex
		
		//TODO geoserverLabel
		
		//TODO geoserverAttribute
		
		if (StringUtils.isNotBlank(row.getPostfix())){
			state.putPostfixLevel(row.getPostfix(), level);
		}
		
		//save
		getTermService().save(level);
		return;
	}



	@Override
	protected void secondPass(SpecimenCdmExcelImportState state) {
		//no second path defined yet
		return;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(SpecimenCdmExcelImportState state) {
		logger.warn("Validation not yet implemented for " + this.getClass().getSimpleName());
		return true;
	}

	protected String getWorksheetName() {
		return WORKSHEET_NAME;
	}
	
	@Override
	protected boolean needsNomenclaturalCode() {
		return false;
	}


	@Override
	protected boolean isIgnore(SpecimenCdmExcelImportState state) {
		return !state.getConfig().isDoAreaLevels();
	}


}
