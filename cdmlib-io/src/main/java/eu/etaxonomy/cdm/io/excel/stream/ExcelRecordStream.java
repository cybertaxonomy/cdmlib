/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.stream;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.stream.IItemStream;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;

/**
 * @author a.oppermann
 * @since 16.05.2013
 *
 */
public class ExcelRecordStream  implements IItemStream{
	private static Logger logger = Logger.getLogger(ExcelRecordStream.class);

	private ExcelStreamImportState state;

	private Sheet sheet;
//	private ArchiveEntryBase archiveEntry;
	private TermUri term;
	private int line = 0;

	private Map<Integer,String> mapping;

	private StreamItem next;




	/**
	 * Constructor.
	 * @param term
	 */
	public ExcelRecordStream(ExcelStreamImportState state, Sheet sheet, TermUri term) {
		this.state = state;
		this.sheet = sheet;
		this.term = term;
	}

	@Override
	public StreamItem read() {
		//FIXME:
		return readMe();
	}

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
		StreamItem resultItem;
		if (next != null){
			resultItem = next;
			next = null;
			return resultItem;
		}else{

			if (mapping == null){
				mapping = getHeaderMapping(sheet.getRow(line++));
			}
	//		int rows = sheet.getPhysicalNumberOfRows();
			Row row = sheet.getRow(line++);
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

				for (int c :mapping.keySet()){
					Cell cell = row.getCell(c);
					String value = ExcelUtils.getCellValue(cell);
					map.put(mapping.get(c), value);
					logger.info("CELL col=" + cell.getColumnIndex() + " VALUE=" + value);
				}

				resultItem = new StreamItem(term, map, String.valueOf(line));

				return resultItem;
			}
		}
	}

	private Map<Integer,String> getHeaderMapping(Row row) {
		Map<Integer,String> result = new HashMap<Integer, String>();

		int cells = row.getPhysicalNumberOfCells();
		logger.info("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
		for (int c = 0; c < cells; c++) {
			Cell cell = row.getCell(c);
			String value = ExcelUtils.getCellValue(cell);
			String termUri = convert2semanticKey(value);
			if (termUri != null){
				result.put(c, termUri);
			}else{
				//TODO fire Event
				String message = "No mapping defined for column %d '%s'";
				logger.warn(String.format(message, c + 1, value));
			}
		}

		return result;
	}

	/**
	 * @param value
	 * @return
	 */
	private String convert2semanticKey(String key) {
		if (key.equalsIgnoreCase("id")){
			return "id";
		}else if (key.equalsIgnoreCase("ParentId")){
			return TermUri.DWC_PARENT_NAME_USAGE_ID.getUriString();
		}else if (key.equalsIgnoreCase("NameStatus")){
			return TermUri.DWC_TAXONOMIC_STATUS.getUriString();
		}else if (key.equalsIgnoreCase("Rank")){
			return TermUri.DWC_TAXON_RANK.getUriString();
		}else if (key.equalsIgnoreCase("ScientificName")){
			return TermUri.DWC_SCIENTIFIC_NAME.getUriString();
		}else if (key.equalsIgnoreCase("Author")){
			return TermUri.DWC_SCIENTIFIC_NAME_AUTHORS.getUriString();
			//TODO Taxon Remarks
		}else if (key.equalsIgnoreCase("Comments")){
			return TermUri.DWC_TAXON_REMARKS.getUriString();
		}else if (key.equalsIgnoreCase("Language")){
			return TermUri.DC_LANGUAGE.getUriString();
		}else if (key.equalsIgnoreCase("TDWG_1")){
			return TermUri.DWC_COUNTRY_CODE.getUriString();
		}else if (key.equalsIgnoreCase("VernacularName")){
			return TermUri.DWC_VERNACULAR_NAME.getUriString();
		}else if (key.equalsIgnoreCase("ExternalId_sysCode")){
			return TermUri.CDM_SOURCE_IDINSOURCE.getUriString();
		}else if (key.equalsIgnoreCase("External_source")){
			return TermUri.CDM_SOURCE_REFERENCE.getUriString();
		}else if (key.equalsIgnoreCase("IdNamespace")){
			return TermUri.CDM_SOURCE_IDNAMESPACE.getUriString();
		}else{
			//TODO fire Event
			String message = "Key '%s' does not (yet) exist for import";
			logger.warn(String.format(message, key));
			return null;
		}

	//Language	TDWG_1	TDWG_2	VernacularName	SysCode

	}

	@Override
	public TermUri getTerm() {
		return this.term;
	}

	@Override
	public String getItemLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStreamLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addObservers(Set<IIoObserver> observers) {
		// TODO Auto-generated method stub
		logger.warn("addObservers Not yet implemented");
	}

//******************** toString *******************************************

	@Override
	public String toString() {
		if (sheet != null && StringUtils.isNotBlank(sheet.getSheetName())){
			return sheet.getSheetName();
		}else{
			return super.toString();
		}
	}
}
