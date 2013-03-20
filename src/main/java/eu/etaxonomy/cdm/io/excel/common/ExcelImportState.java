// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class ExcelImportState<CONFIG extends ExcelImportConfiguratorBase, ROW extends ExcelRowBase> extends ImportStateBase<CONFIG, ExcelImporterBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExcelImportState.class);

	private Integer currentLine;
	private ROW currentRow;

	public Integer getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(Integer currentLine) {
		this.currentLine = currentLine;
	}
	
	/**
	 * Increments the current line
	 */
	public void incCurrentLine(){
		this.currentLine++;
	}
	
	public ExcelImportState(CONFIG config) {
		super(config);
	}


	/**
	 * The data holder class in which results for the current record are stored.
	 * @return
	 */
	public ROW getCurrentRow() {
		return currentRow;
	}
	
	public void setCurrentRow(ROW currentRow) {
		this.currentRow = currentRow;
	}


}
