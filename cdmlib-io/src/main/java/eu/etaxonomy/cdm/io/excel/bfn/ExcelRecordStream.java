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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.stream.IItemStream;
import eu.etaxonomy.cdm.io.stream.StreamItem;

/**
 * @author a.oppermann
 * @date 16.05.2013
 *
 */
public class ExcelRecordStream  implements IItemStream{
	private static Logger logger = Logger.getLogger(ExcelRecordStream.class);
	
	private ExcelStreamImportState state;
	
	private HSSFSheet sheet;
//	private ArchiveEntryBase archiveEntry;
	private TermUri term;
	private int line = 0;
	
	private Map<Integer,TermUri> mapping;
	
	private StreamItem next;
	
	
	

	/**
	 * Constructor. 
	 * @param term 
	 */
	public ExcelRecordStream(ExcelStreamImportState state, HSSFSheet sheet, TermUri term) {
		this.state = state;
		this.sheet = sheet;
		this.term = term;
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


	private StreamItem readMe() {
		
		
		if (mapping == null){
			mapping = getHeaderMapping(sheet.getRow(0));
		}
//		int rows = sheet.getPhysicalNumberOfRows();
		HSSFRow row = sheet.getRow(line++);
		int i = 0;
		while (row == null && i++ < 10){
			row = sheet.getRow(line++);
			continue;
		}
		if (row == null){
			return null;
		}else{
			int cells = row.getPhysicalNumberOfCells();
			logger.info("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
			Map<String, String> map = new HashMap<String, String>();
			for (int c = 0; c < cells; c++) {
				HSSFCell cell = row.getCell(c);
				String value = ExcelUtils.getCellValue(cell);
				TermUri key = mapping.get(c);
				map.put(key.getUriString(), value);
				logger.info("CELL col=" + cell.getColumnIndex() + " VALUE=" + value);
			}
			StreamItem resultItem = new StreamItem(term, map, String.valueOf(line));
			
			return resultItem;
		}
	}
	
	/**
	 * @param row2
	 * @return
	 */
	private Map<Integer,TermUri> getHeaderMapping(HSSFRow row) {
		Map<Integer,TermUri> result = new HashMap<Integer, TermUri>();
		
		int cells = row.getPhysicalNumberOfCells();
		logger.info("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
		for (int c = 0; c < cells; c++) {
			HSSFCell cell = row.getCell(c);
			String value = ExcelUtils.getCellValue(cell);
			TermUri termUri = convert2TermUri(value);
			result.put(c, termUri);
		}
		
		return result;
	}

	/**
	 * @param value
	 * @return
	 */
	private TermUri convert2TermUri(String key) {
		if (key.equalsIgnoreCase("id")){
			//FIXME in work
//			return TermUri.Dwc_I
		}
		
		
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getTerm()
	 */
	@Override
	public TermUri getTerm() {
		return this.term;
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
		logger.warn("addObservers Not yet implemented");
	}
	
//******************** toString *******************************************

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (sheet != null && StringUtils.isNotBlank(sheet.getSheetName())){
			return sheet.getSheetName();
		}else{
			return super.toString();
		}
	}
	
	
	

}
