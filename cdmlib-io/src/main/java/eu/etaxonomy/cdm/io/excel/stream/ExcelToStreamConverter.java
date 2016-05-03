// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.stream;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.dwca.TermUri;
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
	 * @throws InvalidFormatException 
	 */
	public IReader<ExcelRecordStream> getWorksheetStream(STATE state) throws IOException, HttpException, InvalidFormatException{
//		POIFSFileSystem fs = new POIFSFileSystem(UriUtils.getInputStream(source));
//		HSSFWorkbook wb = new HSSFWorkbook(fs);
		Workbook wb = WorkbookFactory.create(UriUtils.getInputStream(source));
		
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
			Sheet ws = wb.getSheetAt(i);
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
			Sheet ws = wb.getSheetAt(i);
			ExcelRecordStream excelRecordStream = new ExcelRecordStream(state, ws, term);
			streamList.add(excelRecordStream); //for relationships
		}else{
			String message = "Taxon worksheet not available for %s";
			logger.warn(String.format(message, "taxon relations"));
			state.setSuccess(false);
		} 
		
		return new ListReader<ExcelRecordStream>(streamList);
	}


	/**
	 * @param wsName
	 * @return
	 */
	private TermUri convertSheetName2TermUri(String wsName) {
		if (StringUtils.isBlank(wsName)){
			throw new IllegalArgumentException("Worksheet name must not be null or empty");
			//FIXME: Hard coded worksheet name should be avoided  
		}else if(wsName.equalsIgnoreCase("Sheet1")){
			return TermUri.DWC_TAXON;
		}else{
			String message = "Worksheet name %s not yet handled by %s";
			throw new IllegalArgumentException(String.format(message, wsName, this.getClass().getSimpleName()));
		}
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
