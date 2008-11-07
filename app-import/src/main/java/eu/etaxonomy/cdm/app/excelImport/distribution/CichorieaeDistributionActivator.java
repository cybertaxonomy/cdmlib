/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.app.excelImport.distribution;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;

/**
 * @author a.babadshanjan
 * @created 31.10.2008
 */
public class CichorieaeDistributionActivator {
	
	private String fileName = new String( System.getProperty("user.home") + File.separator + "Africa plus x.xls");
	private static final String dbName = "cdm_test_jaxb2";
	
	private static final ICdmDataSource destinationDb = TestDatabase.CDM_DB(dbName);
    private static final Logger logger = Logger.getLogger(CichorieaeDistributionActivator.class);
    
    /* used */
    private static String EDIT_NAME_COLUMN = "EDIT";
    private static String TDWG_DISTRIBUTION_COLUMN = "TDWG";
    private static String STATUS_COLUMN = "Status";
    private static String LITERATURE_NUMBER_COLUMN = "Lit.";
    private static String LITERATURE_COLUMN = "Literature";
    /* not yet used */
    private static String VERNACULAR_NAME_COLUMN = "Vernacular";
    private static String HABITAT_COLUMN = "Habitat";
    private static String ISO_DISTRIBUTION_COLUMN = "ISO";
    private static String NOTES_COLUMN = "Notes";
    private static String PAGE_NUMBER_COLUMN = "Page";
    private static String INFO_COLUMN = "Info";
    
    private static String SEPARATOR = ",";

    public static void main(String[] args) {

    	CichorieaeDistributionActivator distributionActivator = new CichorieaeDistributionActivator();
    	CichorieaeDistributionConfigurator distributionConfigurator = 
    		CichorieaeDistributionConfigurator.NewInstance("", destinationDb);
    	distributionActivator.invoke(distributionConfigurator);
    }
	
    public void invoke(IImportConfigurator config) {

    	logger.info("Importing distribution data");

    	ArrayList<HashMap<String, String>> recordList = parseXLS(fileName);
    	if (recordList != null) {
    		HashMap<String,String> record = null;
    		for (int i = 0; i < recordList.size(); i++) {
    			record = recordList.get(i);
    			saveData(record);
//    			config.setDbSchemaValidation(DbSchemaValidation.UPDATE);
    		}
    	}
    }
    
    private void saveData(HashMap record) {
    	/*
    	 * Relevant columns:
    	 * Name (EDIT)
    	 * Distribution TDWG
    	 * Status (only entries if not native) 
    	 * Literature number
    	 * Literature
    	*/
    	
    	/*
    	 * Find taxon by name
    	 * TdwgArea.getAreaByTdwgAbbreviation()
    	 */
    	
        String editName = "";
        String distribution = "";
        ArrayList<String> distributionList = new ArrayList<String>();
        String status = "";
        ArrayList<String> statusList = new ArrayList<String>();
        String literatureNumber = "";
        String literature = "";
        
    	Set<String> keys = record.keySet();
    	
    	for (String key: keys) {
    		
    		String value = (String) record.get(key);
    		if (!value.equals("")) {
    			logger.debug("Key = " + key);
    			logger.debug("Value = " + value);
    		}
    		
    		if (key.contains(EDIT_NAME_COLUMN)) {
    			editName = value;
//            	logger.debug("Name = " + editName);
    			
			} else if(key.contains(TDWG_DISTRIBUTION_COLUMN)) {
				distributionList =  buildList(value);
				
			} else if(key.contains(STATUS_COLUMN)) {
				statusList = buildList(value);
				
			} else if(key.contains(LITERATURE_NUMBER_COLUMN)) {
				literatureNumber = value;
//            	logger.debug("Literature number = " + literatureNumber);
				
			} else if(key.contains(LITERATURE_COLUMN)) {
				literature = value;
//            	logger.debug("Literatur = " + literature);
			}
    	}
    	
    	// Store the data of this record in the DB
    	saveRecord(editName, distributionList, statusList, literatureNumber, literature);
    }
    
    
	/** 
	 *  Stores data in the DB
	 */
    private void saveRecord(String taxonName, ArrayList<String> distributionList,
    		ArrayList<String> statusList, String literatureNumber, String literature) {

		CdmApplicationController appCtr = null;
		logger.info("Test modifying shared objects");

		try {
			appCtr = CdmApplicationController.NewInstance(destinationDb, DbSchemaValidation.VALIDATE, true);

		} catch (Exception e) {
			logger.error("Error creating application controller");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
	    	TransactionStatus txStatOne = appCtr.startTransaction();
	    	
//			appCtr.getNameService().;
    	
	    	appCtr.commitTransaction(txStatOne);
	    	appCtr.close();
			logger.info("End test modifying shared objects"); 
				
		} catch (Exception e) {
    		logger.error("Error");
    		e.printStackTrace();
		}
    }
    
    
    private ArrayList<String> buildList(String value) {
    	
    	ArrayList<String> resultList = new ArrayList<String>();
    	StringTokenizer st = new StringTokenizer(value, SEPARATOR);
        while (st.hasMoreTokens()) {
        	String listElement = st.nextToken();
            resultList.add(listElement);
        	logger.debug("Next token = " + listElement);
        }
        return resultList;
    }
    
    
    private static ArrayList<HashMap<String, String>> parseXLS(String fileName) {
    	
    	ArrayList<HashMap<String, String>> recordList = new ArrayList<HashMap<String, String>>();

    	try {
    		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fileName));
    		HSSFWorkbook wb = new HSSFWorkbook(fs);
    		HSSFSheet sheet = wb.getSheetAt(0);
    		HSSFRow row;
    		HSSFCell cell;

    		int rows; // No of rows
    		rows = sheet.getPhysicalNumberOfRows();

    		int cols = 0; // No of columns
    		int tmp = 0;

    		// This trick ensures that we get the data properly even if it doesn't start from first few rows
    		for(int i = 0; i < 10 || i < rows; i++) {
    			row = sheet.getRow(i);
    			if(row != null) {
    				tmp = sheet.getRow(i).getPhysicalNumberOfCells();
    				if(tmp > cols) cols = tmp;
    			}
    		}
    		HashMap<String, String> headers = null;
    		ArrayList<String> columns = new ArrayList<String>();
    		row = sheet.getRow(0);
    		for (int c = 0; c < cols; c++){
    			cell = row.getCell(c);
    			columns.add(cell.toString());
    		}
    		for(int r = 1; r < rows; r++) {
    			row = sheet.getRow(r);
    			headers = new HashMap<String, String>();
    			if(row != null) {
    				for(int c = 0; c < cols; c++) {
    					cell = row.getCell((short)c);
    					if(cell != null) {
    						headers.put(columns.get(c), cell.toString());
    					}
    				}
    			}
    			recordList.add(headers);
    		}

    	} catch(Exception ioe) {
    		ioe.printStackTrace();
    	}
    	return recordList;
    }

}
