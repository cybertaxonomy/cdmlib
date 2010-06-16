// $Id$
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
import java.io.FileNotFoundException;

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
    public static ArrayList<HashMap<String, String>> parseXLS(String fileName) throws FileNotFoundException {
    	
    	ArrayList<HashMap<String, String>> recordList = new ArrayList<HashMap<String, String>>();

    	try {
    		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fileName));
    		HSSFWorkbook wb = new HSSFWorkbook(fs);
    		HSSFSheet sheet = wb.getSheetAt(0);
    		HSSFRow row;
    		HSSFCell cell;

    		int rows; // No of rows
    		rows = sheet.getPhysicalNumberOfRows();
			if(logger.isDebugEnabled()) { logger.debug("Number of rows: " + rows); }

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
				if(cell != null) {
					columns.add(cell.toString());
					if(logger.isDebugEnabled()) { logger.debug("Cell #" + c + ": " + cell.toString()); }
				} else {
					if(logger.isDebugEnabled()) { logger.debug("Cell #" + c + " is null"); }
				}
    		}
    		for(int r = 1; r < rows; r++) {
    			row = sheet.getRow(r);
    			headers = new HashMap<String, String>();
    			boolean notEmpty = false;
    			for (int j = 0; j<row.getRowNum(); j++){
    				if (row.getCell(j) != null){
    					notEmpty = true;
    					break;
    				}
    			}
    			if(row != null && notEmpty) {
    				for(int c = 0; c < cols; c++) {
    					cell = row.getCell((short)c);
    					if(cell != null) {
    						if (c >= columns.size()){
    							logger.warn("Cell has no header. There are only " + columns.size() + " headers but more not-null cells in approx. row " + row.getRowNum() + ". Cell is neglected.");
    						}else{
    							if(logger.isDebugEnabled()) { logger.debug("Cell #" + c + ": " + cell.toString()); }
    							headers.put(columns.get(c), cell.toString());	
    						}
    					} else {
    						if(logger.isDebugEnabled()) { logger.debug("Cell #" + c + " is null"); }
    					}
    				}
    			}
    			recordList.add(headers);
    		}
    	} catch(FileNotFoundException fne) {
    		throw new FileNotFoundException(fileName);
    	} catch(Exception ioe) {
    		logger.error("Error reading the Excel file.");
    		ioe.printStackTrace();
    	}
    	return recordList;
    }
	
}
