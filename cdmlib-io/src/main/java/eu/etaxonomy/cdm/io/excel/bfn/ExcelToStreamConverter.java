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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.hibernate.dialect.FirebirdDialect;
import org.springframework.transaction.TransactionStatus;

import bsh.This;

import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.dwca.in.CsvStream;
import eu.etaxonomy.cdm.io.dwca.in.IReader;
import eu.etaxonomy.cdm.io.dwca.in.ListReader;

/**
 * This class transforms excel archive in to a InputStream.
 * 
 * @author a.oppermann
 * @date 16.05.2013
 *
 */
public class ExcelToStreamConverter<STATE extends ExcelStreamImportState> {
	
	private static Logger logger = Logger.getLogger(ExcelToStreamConverter.class);
	
	private URI source;
	
	/**
	 * 
	 * Factory
	 * @param source
	 * @return
	 */
	public static ExcelToStreamConverter<ExcelStreamImportState> NewInstance(URI source) {
		return new ExcelToStreamConverter<ExcelStreamImportState>(source);
	}

	/**
	 * Constructor
	 * @param source
	 */
	public ExcelToStreamConverter(URI source){
		this.source = source;
	}
	
	/**
	 * @param state
	 * @return
	 * @throws HttpException 
	 * @throws IOException 
	 */
	public IReader<ExcelRecordStream> getWorksheetStream(STATE state) throws IOException, HttpException{
		POIFSFileSystem fs = new POIFSFileSystem(UriUtils.getInputStream(source));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		
		Map<TermUri, Integer> map = new HashMap<TermUri, Integer>();
		for (int i = 0 ; i < wb.getNumberOfSheets(); i++){
			String wsName = wb.getSheetName(i);
			TermUri termUri = convertSheetName2TermUri(wsName);
			if (map.get(termUri) != null){
				String message = "Worksheet type exists more then once: %s";
				//TODO fire event
				logger.warn(String.format(message, termUri.toString()));
			}
			map.put(termUri, i);
		}
		
		//core
		List<ExcelRecordStream> streamList = new ArrayList<ExcelRecordStream>();
		TermUri term= TermUri.DWC_TAXON;
		Integer i = map.get(term);
		if (i != null){
			HSSFSheet ws = wb.getSheetAt(i);
			ExcelRecordStream excelRecordStream = new ExcelRecordStream(state, ws, term);
			streamList.add(excelRecordStream); //for taxa and names
		}else{
			String message = "Taxon worksheet not available for %s";
			logger.warn(String.format(message, "taxa"));
			state.setSuccess(false);
		} 
		
		//core relationships
		i = map.get(term);
		if (i != null){
			HSSFSheet ws = wb.getSheetAt(i);
			ExcelRecordStream excelRecordStream = new ExcelRecordStream(state, ws, term);
			streamList.add(excelRecordStream); //for relationships
		}else{
			String message = "Taxon worksheet not available for %s";
			logger.warn(String.format(message, "taxon relations"));
			state.setSuccess(false);
		} 
		
		return new ListReader<ExcelRecordStream>(streamList);
		
//		HSSFSheet sheet;
//		if (worksheetName == null){
//			sheet = wb.getSheetAt(0);	
//		}else{
//			sheet = wb.getSheet(worksheetName);
//		}
		
		
		
//		String sheetName = getWorksheetName();
//		try {
//			ArrayList<HashMap<String, String>> recordList = ExcelUtils.parseXLS(source, sheetName);
//		
//			//handleImport
////			handleRecordList(state, source);
//			logger.debug("End excel data import"); 
//	//		return;
//			IReader<HashMap<String, String>> result = new ListReader<HashMap<String, String>>(recordList);
//			return result;
//		} catch (FileNotFoundException e) {
//			String message = "File not found: " + source;
//			warnProgress(state, message, e);
//			logger.error(message);
//			state.setUnsuccessfull();
//			return new ListReader<HashMap<String,String>>(new ArrayList<HashMap<String,String>>());
//		}
	}
	
	
//	/**
//	 * @param state
//	 * @param source2
//	 */
//	private void 5(STATE state, URI source2) {
//		// TODO Auto-generated method stub
//		
//	}

	/**
	 * @param wsName
	 * @return
	 */
	private TermUri convertSheetName2TermUri(String wsName) {
		if (StringUtils.isBlank(wsName)){
			throw new IllegalArgumentException("Worksheet name must not be null or empty");
		}else if(wsName.equalsIgnoreCase("NormalExplicit.txt")){
			return TermUri.DWC_TAXON;
		}else{
			String message = "Worksheet name %s not yet handled by %s";
			throw new IllegalArgumentException(String.format(message, wsName, this.getClass().getSimpleName()));
		}
	}

	private void initArchive(STATE state){

	}

	/**
	 * @return
	 */
	private String getWorksheetName() {
		return null;
	}

	public void warnProgress(STATE state, String message, Throwable e) {
        if(state.getConfig().getProgressMonitor() != null){
            IProgressMonitor monitor = state.getConfig().getProgressMonitor();
            if (e == null) {
                monitor.warning(message);
            }else{
                monitor.warning(message, e);
            }
        }
    }
	

	
}
