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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;

/**
 * @author p.kelbert
 * @created 29.10.2008
 * @version 1.0
 */
@Component
public class NameAreaLevelExcelImport  extends ExcelImporterBase<SpecimenCdmExcelImportState>  implements ICdmIO<SpecimenCdmExcelImportState> {
	private static final Logger logger = Logger.getLogger(NameAreaLevelExcelImport.class);

	private static final String WORKSHEET_NAME = "AreaLevel";

	public NameAreaLevelExcelImport() {
		super();
	}
	
	@Override
	protected boolean analyzeRecord(HashMap<String, String> record, SpecimenCdmExcelImportState state) {
		return false;
	}


	@Override
	protected boolean firstPass(SpecimenCdmExcelImportState state) {
		// TODO Auto-generated method stub
		return false;
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
