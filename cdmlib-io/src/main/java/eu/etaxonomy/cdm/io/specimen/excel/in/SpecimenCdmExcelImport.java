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

/**
 * @author p.kelbert
 * @created 29.10.2008
 * @version 1.0
 */
@Component
public class SpecimenCdmExcelImport  extends ExcelImporterBase<SpecimenCdmExcelImportState>  implements ICdmIO<SpecimenCdmExcelImportState> {
	private static final Logger logger = Logger.getLogger(SpecimenCdmExcelImport.class);

	private static final String WORKSHEET_NAME = "Specimen";

	private static final String UUID_COLUMN = "UUID";

	private static final String BASIS_OF_RECORD_COLUMN = "BasisOfRecord";

	private static final String COUNTRY_COLUMN = "Country";

	private static final String ISO_COUNTRY_COLUMN = "ISOCountry";

	private static final String LOCALITY_COLUMN = "Locality";

	private static final String FIELD_NOTES_COLUMN = "FieldNotes";

	private static final String FIELD_NUMBER_COLUMN = "FieldNumber";

	private static final String ACCESSION_NUMBER_COLUMN = "AccessionNumber";


	public SpecimenCdmExcelImport() {
		super();
	}
	
	@Override
	protected boolean analyzeRecord(HashMap<String, String> record, SpecimenCdmExcelImportState state) {
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	SpecimenRow row = new SpecimenRow();
    	state.setSpecimenRow(row);
    	
    	for (String originalKey: keys) {
    		String indexedKey = CdmUtils.removeDuplicateWhitespace(originalKey.trim()).toString();
    		String[] split = indexedKey.split("_");
    		String key = split[0];
    		if (split.length > 1){
    			String indexString = split[1];
    			try {
    				Integer.valueOf(indexString);
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
    			
			} else if(key.equalsIgnoreCase(BASIS_OF_RECORD_COLUMN)) {
				row.setBasisOfRecord(value);
				
			} else if(key.equalsIgnoreCase(COUNTRY_COLUMN)) {
				row.setCountry(value);
				
			} else if(key.equalsIgnoreCase(ISO_COUNTRY_COLUMN)) {
				row.setIsoCountry(value);
    			
			} else if(key.equalsIgnoreCase(LOCALITY_COLUMN)) {
				row.setLocality(value);

			} else if(key.equalsIgnoreCase(FIELD_NOTES_COLUMN)) {
				row.setLocality(value);

			} else if(key.equalsIgnoreCase(FIELD_NUMBER_COLUMN)) {
				row.setLocality(value);	

			} else if(key.equalsIgnoreCase(ACCESSION_NUMBER_COLUMN)) {
				row.setLocality(value);		
				
 			} else {
				success = false;
				logger.error("Unexpected column header " + key);
			}
    	}
    	return success;
	}


	@Override
	protected boolean firstPass(SpecimenCdmExcelImportState state) {
		SpecimenRow row = state.getSpecimenRow();
		
		//basis of record
		DerivedUnitType type = DerivedUnitType.valueOf2(row.getBasisOfRecord());
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(type);
		
		
		
		//save
		getOccurrenceService().save(facade.innerDerivedUnit());
		return true;
	}



	private DerivedUnitType getDerivedUnitType(String basisOfRecord) {
		
		return null;
	}

	@Override
	protected boolean secondPass(SpecimenCdmExcelImportState state) {
		//no second path defined yet
		return true;
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
	protected boolean isIgnore(SpecimenCdmExcelImportState state) {
		return !state.getConfig().isDoSpecimen();
	}


}
