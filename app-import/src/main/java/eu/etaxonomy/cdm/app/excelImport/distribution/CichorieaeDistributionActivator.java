/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.app.excelImport.distribution;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import eu.etaxonomy.cdm.app.jaxb.CdmExportActivator;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.jaxb.CdmExporter;
import eu.etaxonomy.cdm.io.test.function.TestDatabase;
import eu.etaxonomy.cdm.io.unitsPortal.TestABCD;

/**
 * @author a.babadshanjan
 * @created 31.10.2008
 */
public class CichorieaeDistributionActivator {
	
	private String fileName = new String( System.getProperty("user.home") + File.separator + "Africa plus x.xls");
	private static final String dbName = "cdm_test_jaxb2";
	
	private static final ICdmDataSource destinationDb = TestDatabase.CDM_DB(dbName);
    private static final Logger logger = Logger.getLogger(CichorieaeDistributionActivator.class);

    public static void main(String[] args) {

    	CichorieaeDistributionActivator distributionActivator = new CichorieaeDistributionActivator();
    	CichorieaeDistributionConfigurator distributionConfigurator = 
    		CichorieaeDistributionConfigurator.NewInstance("", destinationDb);
    	distributionActivator.invoke(distributionConfigurator);
    }
	
    public void invoke(IImportConfigurator config) {

    	logger.info("Importing distribution data");

    	ArrayList<Hashtable<String,String>> unitsList = parseXLS(fileName);
    	if (unitsList != null){
    		Hashtable<String,String> unit=null;
    		for (int i=0; i<unitsList.size();i++){
    			unit = unitsList.get(i);
//    			saveData();
    			config.setDbSchemaValidation(DbSchemaValidation.UPDATE);
    		}
    	}
    }

    private static ArrayList<Hashtable<String, String>> parseXLS(String fileName) {
    	ArrayList<Hashtable<String, String>> units = new ArrayList<Hashtable<String,String>>();

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
    		Hashtable<String, String> headers = null;
    		ArrayList<String> columns = new ArrayList<String>();
    		row = sheet.getRow(0);
    		for (int c =0; c<cols; c++){
    			cell = row.getCell(c);
    			columns.add(cell.toString());
    		}
    		for(int r = 1; r < rows; r++) {
    			row = sheet.getRow(r);
    			headers = new Hashtable<String, String>();
    			if(row != null) {
    				for(int c = 0; c < cols; c++) {
    					cell = row.getCell((short)c);
    					if(cell != null) {
    						headers.put(columns.get(c),cell.toString());
    					}
    				}
    			}
    			units.add(headers);
    		}

    	} catch(Exception ioe) {
    		ioe.printStackTrace();
    	}
    	return units;
    }

}
