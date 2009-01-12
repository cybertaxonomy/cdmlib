/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.common;

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
	private String taxonName = "";
	
	private CdmApplicationController appCtr = null;
	private ExcelImportConfiguratorBase configurator = null;

//	@Override
//	protected boolean doInvoke(IImportConfigurator config,
//			Map<String, MapWrapper<? extends CdmBase>> stores) {
//		
//    	logger.debug("Importing excel data");
//		URI uri = null;
//		boolean success = true;
//		ExcelImportConfiguratorBase excelImpConfig = (ExcelImportConfiguratorBase)config;
//    	String dbName = excelImpConfig.getDestination().getDatabase();
//    	
//    	String urlFileName = (String)config.getSource();
//		logger.debug("urlFileName: " + urlFileName);
//		uri = CdmUtils.string2Uri(urlFileName);
//		if (uri == null) {
//			return false;
//		}
//    	
//    	return success;
//	}

	
	/** Reads data from an Excel file and stores them into a CDM DB.
     * 
     * @param config
     * @param stores (not used)
     */
	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores) {
		
    	logger.debug("Importing excel data");
    	appCtr = config.getCdmAppController();
    	
    	configurator = (ExcelImportConfiguratorBase) config;
    	config.setNomenclaturalCode(NomenclaturalCode.ICBN());
    	
		// read and save all rows of the excel worksheet
    	recordList = ExcelUtils.parseXLS((String)config.getSource());
    	//ArrayList<HashMap<String, String>> recordList = ExcelUtils.parseXLS((String)config.getSource());
    	
    	if (recordList != null) {
    		HashMap<String,String> record = null;
    		TransactionStatus txStatus = appCtr.startTransaction();

    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			analyzeRecord(record);
    			saveRecord();
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
    	
    	return true;
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

	public String getTaxonName() {
		
		return this.taxonName;
	}
	
	public void setTaxonName(String taxonNameBase) {
	
		this.taxonName = taxonNameBase;
	}
}
