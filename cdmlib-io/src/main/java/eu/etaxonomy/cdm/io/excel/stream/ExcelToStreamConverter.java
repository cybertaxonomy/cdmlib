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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.stream.IReader;
import eu.etaxonomy.cdm.io.stream.ListReader;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;

/**
 * This class transforms excel archive in to a InputStream.
 *
 * @author a.oppermann
 * @since 16.05.2013
 */
public class ExcelToStreamConverter<STATE extends ExcelStreamImportState> {

    private static final Logger logger = LogManager.getLogger();

	private URI source;

	/**
	 * Factory
	 */
	public static ExcelToStreamConverter<ExcelStreamImportState> NewInstance(URI source) {
		return new ExcelToStreamConverter<ExcelStreamImportState>(source);
	}

	/**
	 * Constructor
	 */
	public ExcelToStreamConverter(URI source){
		this.source = source;
	}

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
		List<ExcelRecordStream> streamList = new ArrayList<>();
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

		return new ListReader<>(streamList);
	}

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