// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.bfn;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.stream.IItemStream;
import eu.etaxonomy.cdm.io.stream.StreamItem;

/**
 * @author a.oppermann
 * @date 16.05.2013
 *
 */
public class ExcelRecordStream_Old  implements IItemStream{
	private static Logger logger = Logger.getLogger(ExcelRecordStream_Old.class);
	
	private StreamItem next;
	
	private ExcelStreamImportState state;

	/**
	 * Constructor. 
	 */
	public ExcelRecordStream_Old(ExcelStreamImportState state) {
		this.state = state;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.IReader#read()
	 */
	@Override
	public StreamItem read() {
		//FIXME:
		return readMe();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.IReader#hasNext()
	 */
	@Override
	public boolean hasNext() {
		if (next != null){
			return true;
		}else{
			next = readMe();
			return (next != null);
		}
	}
	
	private static HSSFWorkbook readFile(String filename) throws IOException {
		return new HSSFWorkbook(new FileInputStream(filename));
	}

	public StreamItem readMe() {
		StreamItem resultItem;
		HSSFWorkbook wb;
		try {
			wb = readFile(state.getConfig().getSource().toString());

		for (int k = 0; k < wb.getNumberOfSheets(); k++) {
			HSSFSheet sheet = wb.getSheetAt(k);
			int rows = sheet.getPhysicalNumberOfRows();
			logger.info("Sheet " + k + " \"" + wb.getSheetName(k) + "\" has " + rows
					+ " row(s).");
			for (int r = 0; r < rows; r++) {
				HSSFRow row = sheet.getRow(r);
				if (row == null) {
					continue;
				}

				int cells = row.getPhysicalNumberOfCells();
				logger.info("\nROW " + row.getRowNum() + " has " + cells
						+ " cell(s).");
				for (int c = 0; c < cells; c++) {
					HSSFCell cell = row.getCell(c);
					String value = null;

					switch (cell.getCellType()) {

						case HSSFCell.CELL_TYPE_FORMULA:
							value = "FORMULA value=" + cell.getCellFormula();
							break;

						case HSSFCell.CELL_TYPE_NUMERIC:
							value = "NUMERIC value=" + cell.getNumericCellValue();
							break;

						case HSSFCell.CELL_TYPE_STRING:
							value = "STRING value=" + cell.getStringCellValue();
							break;

						default:
					}
					logger.info("CELL col=" + cell.getColumnIndex() + " VALUE="
							+ value);
				}
			}
		} 	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getTerm()
	 */
	@Override
	public TermUri getTerm() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getItemLocation()
	 */
	@Override
	public String getItemLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getStreamLocation()
	 */
	@Override
	public String getStreamLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#addObservers(java.util.Set)
	 */
	@Override
	public void addObservers(Set<IIoObserver> observers) {
		// TODO Auto-generated method stub
		
	}

}
