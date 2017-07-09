/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv;

import java.util.Map;

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * State class for {@link CsvImportBase csv imports}. This
 * class can either be used directly or being subclassed
 * according to the needs of a certain import.
 *
 * @author a.mueller
 * @date 08.07.2017
 *
 */
public class CsvImportState<CONFIG extends CsvImportConfiguratorBase>
    extends ImportStateBase<CONFIG, CsvImportBase>{

    private Map<String, String> currentRecord;
    private int row;


    protected CsvImportState(CONFIG config) {
        super(config);
    }


    public Map<String, String> getCurrentRecord() {
        return currentRecord;
    }
    public void setCurrentRecord(Map<String, String> currentRecord) {
        this.currentRecord = currentRecord;
    }

    public void setRow(int row) {
        this.row = row;
    }
    public int getRow() {
        return row;
    }
    public String getLine(){
        return String.valueOf(row);
    }

}
