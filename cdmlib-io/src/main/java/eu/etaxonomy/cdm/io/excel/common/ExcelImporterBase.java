/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.common;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.babadshanjan
 * @created 17.12.2008
 * @version 1.0
 */
public abstract class ExcelImporterBase extends CdmIoBase<IImportConfigurator> {
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
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores) {
		
		boolean success = false;
		
    	logger.debug("Importing excel data");
    	
    	configurator = (ExcelImportConfiguratorBase) config;
    	
		NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
		if (nc == null) {
			logger.error("Nomenclatural code could not be determined.");
			return false;
		}
		// read and save all rows of the excel worksheet
    	try {
			recordList = ExcelUtils.parseXLS((String)config.getSource());
		} catch (FileNotFoundException e1) {
			logger.error("File not found: " + (String)config.getSource());
			return false;
		}
    	
    	if (recordList != null) {
    		HashMap<String,String> record = null;
    		TransactionStatus txStatus = appCtr.startTransaction();

    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			success = analyzeRecord(record);
    			success = saveRecord();
    		}
    		appCtr.commitTransaction(txStatus);
    	}
    	
		try {
	    	appCtr.close();
			logger.debug("End excel data import"); 
				
		} catch (Exception e) {
    		logger.error("Error closing the application context");
    		e.printStackTrace();
		}
    	
    	return success;
	}

	@Override
	protected boolean doCheck(IImportConfigurator config) {
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
	protected abstract boolean analyzeRecord(HashMap<String,String> record);
	
	protected abstract boolean saveRecord();
	
	
	public ExcelImportConfiguratorBase getConfigurator() {
		
		return configurator;
	}
	
	
	public CdmApplicationController getApplicationController() {
		
		return appCtr;
	}

}
