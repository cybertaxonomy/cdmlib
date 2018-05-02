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
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @author n.hoffmann
 * @since 18.11.2008
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
        InputStream stream;
        try {
            stream = UriUtils.getInputStream(uri);
            return parseXLS(stream, worksheetName);
        } catch(FileNotFoundException fne) {
            throw new FileNotFoundException(uri.toString());
        } catch(Exception ioe) {
            logger.error("Error reading the Excel file." + uri.toString());
            ioe.printStackTrace();
        }
        return null;

    }
        /** Reads all rows of an Excel worksheet */
        public static ArrayList<HashMap<String, String>> parseXLS(InputStream stream, String worksheetName) throws FileNotFoundException {


    	ArrayList<HashMap<String, String>> recordList = new ArrayList<HashMap<String, String>>();

    	try {
//    		POIFSFileSystem fs = new POIFSFileSystem(UriUtils.getInputStream(uri));
//    		HSSFWorkbook wb = new HSSFWorkbook(fs);


    		Workbook wb = WorkbookFactory.create(stream);


    		Sheet sheet;
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
	    		Row row;
	    		Cell cell;

	    		int rows; // Number of rows
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

    	} catch(Exception ioe) {
    		logger.error("Error reading the Excel file.");
    		ioe.printStackTrace();
    	}
    	return recordList;
    }


	public static String getCellValue(Cell cell) {
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
				try {
					String strValue = cell.getStringCellValue();
					if ("".equals(strValue)){
						strValue = getNumericCellValue(cell);
					}
					return strValue;
				} catch (Exception e) {
					String message = "Formula cell (%s) can't be transformed to string";
					message = String.format(message, getExcelCellString(cell));
					throw new RuntimeException(message, e);
				}
			}else{
				return cell.toString();
			}
		} catch (Exception e) {
			String message = "Error occurred while reading Excel cell '%s' . Use toString() instead. Error: %s";
			message = String.format(message,getExcelCellString(cell) ,e.getLocalizedMessage());
			logger.warn(message);
			return cell.toString();
		}
	}

	public static String getExcelCellString(Cell cell){
		String result = "%s%s";
		result = String.format(result, getExcelColString(cell.getColumnIndex()), cell.getRowIndex());
		return result;
	}

	private static String getExcelColString(int colNr){
		int first = colNr / 26;
		int second = colNr % 26;
//		char a = Integer.valueOf(first).shortValue();
		String firstStr = String.valueOf((first > 0 ? (char)(first +64) :""));
		String secondStr = String.valueOf((char)(second + 64));
		return firstStr +  secondStr;
	}


	/**
	 * Returns the numeric cell value. In case the cell is formatted as
	 * a date it returns a date (using the dates toString() method.
	 * @param cell
	 * @return
	 */
	private static String getNumericCellValue(Cell cell) {
		Double number = cell.getNumericCellValue();
//		HSSFCellStyle style = cell.getCellStyle();
//		String dataFormatString = style.getDataFormatString();
//		int index = style.getIndex();
		HSSFDataFormatter formatter = new HSSFDataFormatter();
//		Format defFormat = formatter.getDefaultFormat(cell);
		Format format = formatter.createFormat(cell);
//		String v = formatter.formatCellValue(cell);
		if (format != null && format instanceof DateFormat){
			//TODO use ISO or similar format once TimePeriod knows how to parse this
//			String result = formatter.formatCellValue(cell);
			Date date = cell.getDateCellValue();
			Locale locale = Locale.GERMAN;
			DateFormat df = DateFormat.getDateInstance(2,locale);
			String result = df.format(date); //result of type dd.mm.yyyy
//			String result = date.toString();

			return result;
		}
//		System.out.println(d);

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
	private static boolean checkIsEmptyRow(Row row) {
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
