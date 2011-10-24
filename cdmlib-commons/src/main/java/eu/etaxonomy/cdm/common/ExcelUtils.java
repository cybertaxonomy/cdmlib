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

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
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
    public static ArrayList<HashMap<String, String>> parseXLS(URI uri) throws FileNotFoundException {
    	return parseXLS(uri, null);
    }

    
	/** Reads all rows of an Excel worksheet */
    public static ArrayList<HashMap<String, String>> parseXLS(URI uri, String worksheetName) throws FileNotFoundException {
    	
    	ArrayList<HashMap<String, String>> recordList = new ArrayList<HashMap<String, String>>();

    	try {
    		POIFSFileSystem fs = new POIFSFileSystem(UriUtils.getInputStream(uri));
    		HSSFWorkbook wb = new HSSFWorkbook(fs);
    		HSSFSheet sheet;
    		if (worksheetName == null){
    			sheet = wb.getSheetAt(0);	
    		}else{
    			sheet = wb.getSheet(worksheetName);
    		}
    		
    		if (sheet== null){
    			if (worksheetName != null){
    				logger.debug(worksheetName + " not provided!");
    			}
    		}else{
	    		HSSFRow row;
	    		HSSFCell cell;
	
	    		int rows; // No of rows
	    		rows = sheet.getPhysicalNumberOfRows();
				if(logger.isDebugEnabled()) { logger.debug("Number of rows: " + rows); }
	
	    		int cols = 0; // Number of columns
	    		int tmp = 0;
	
	    		// This trick ensures that we get the data properly even if it doesn't start from first few rows
	    		for(int i = 0; i < 10 || i < rows; i++) {
	    			row = sheet.getRow(i);
	     			if(row != null) {
	    				tmp = sheet.getRow(i).getPhysicalNumberOfCells();
	    				if(tmp > cols){
	    					cols = tmp;
	    				}
	    			}
	    		}
    		
    		
	    		//first row
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
	    		
	    		//value rows
	    		for(int r = 1; r < rows; r++) {
	    			row = sheet.getRow(r);
	    			HashMap<String, String> headers = new HashMap<String, String>();
	    			boolean notEmpty = checkIsEmptyRow(row);
	    			if(notEmpty) {
	    				for(int c = 0; c < cols; c++) {
	    					if (row == null){
	    						System.out.println("XXX");
	    					}
	    					cell = row.getCell(c);
	    					if(cell != null) {
	    						if (c >= columns.size()){
	    							String message = "Cell has no header. There are only %d headers but more not-null cells in approx. row %d. Cell is neglected.";
	    							message = String.format(message, columns.size(),row.getRowNum());
	    							logger.warn(message);
	    						}else{
	    							if(logger.isDebugEnabled()) { logger.debug(String.format("Cell #%d: %s", c, cell.toString())); }
	    							headers.put(columns.get(c), getCellValue(cell));	
	    						}
	    					} else {
	    						if(logger.isDebugEnabled()) { logger.debug("Cell #" + c + " is null"); }
	    					}
	    				}
	    			}
	    			recordList.add(headers);
	    		}
    		}
    	} catch(FileNotFoundException fne) {
    		throw new FileNotFoundException(uri.toString());
    	} catch(Exception ioe) {
    		logger.error("Error reading the Excel file.");
    		ioe.printStackTrace();
    	}
    	return recordList;
    }


	private static String getCellValue(HSSFCell cell) {
		try {
			if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING ){
				return cell.getStringCellValue();
			}else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK){
				return "";
			}else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
				return getNumericCellValue(cell);
			}else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN){
				Boolean cellValue = cell.getBooleanCellValue();
				String value = String.valueOf(cellValue);
				return value;
			}else if (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR){
				return "-error-";
			}else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA){
				String strValue = cell.getStringCellValue();
				if ("".equals(strValue)){
					strValue = getNumericCellValue(cell);
				}
				return strValue;
			}else{
				return cell.toString();
			}
		} catch (Exception e) {
			String message = "Error occurred while reading HSSFCell. Use toString() instead";
			logger.warn(message);
			return cell.toString();
		}
	}


	/**
	 * @param cell
	 * @return
	 */
	private static String getNumericCellValue(HSSFCell cell) {
		Double number = cell.getNumericCellValue();
		if (number.intValue() == number){
			return String.valueOf(number.intValue());
		}else{
			return String.valueOf(number);
		}
	}


	/**
	 * Returns false, if row is null or has no values
	 * @param row
	 * @param notEmpty
	 * @return
	 */
	private static boolean checkIsEmptyRow(HSSFRow row) {
		if (row == null){
			return false;
		}
		boolean notEmpty = false;
		for (int j = 0; j<row.getLastCellNum(); j++){
			if (row.getCell(j) != null){
				notEmpty = true;
			}
		}
		return notEmpty;
	}
	
}
