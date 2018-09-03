/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.common;

import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public class ExcelImportState<CONFIG extends ExcelImportConfiguratorBase, ROW extends ExcelRowBase>
        extends ImportStateBase<CONFIG, ExcelImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExcelImportState.class);

	private Integer currentLine;
	private ROW currentRow;
    private Map<String, String> originalRecord;

    private Reference sourceReference;

    public ExcelImportState(CONFIG config) {
        super(config);
    }

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

	public Map<String,String> getOriginalRecord(){
	    return this.originalRecord;
	}

    public void setOriginalRecord(Map<String,String> originalRecord){
        this.originalRecord = originalRecord;
    }


    public Reference getSourceReference() {
        return this.sourceReference;
    }
    public void setSourceReference(Reference sourceReference) {
        this.sourceReference = sourceReference;
    }

}
