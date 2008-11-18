/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.common;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
public class ExcelUtils {
	private static final Logger logger = Logger.getLogger(ExcelUtils.class);
	
    /** Reads all rows of an Excel worksheet */
    public static ArrayList<HashMap<String, String>> parseXLS(String fileName) {
    	
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
