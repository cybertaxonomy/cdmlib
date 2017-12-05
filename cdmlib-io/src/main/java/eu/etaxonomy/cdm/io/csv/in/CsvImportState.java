/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv.in;

import java.util.Map;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.utils.ImportDeduplicationHelper;
import eu.etaxonomy.cdm.model.reference.Reference;

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

    private ImportDeduplicationHelper<CsvImportState> dedupHelper;
    private Reference sourceReference;

    protected CsvImportState(CONFIG config) {
        super(config);
    }

    public void resetSession(){
        getDedupHelper().restartSession(getCurrentIO(), this.getResult());
        this.sourceReference = null;
    }


    /**
     * Map representation of the current line.
     * @return The String map representing the current line in the csv.
     */
    public Map<String, String> getCurrentRecord() {
        return currentRecord;
    }
    public void setCurrentRecord(Map<String, String> currentRecord) {
        this.currentRecord = currentRecord;
    }

    /**
     * Integer representation of the line number in the csv.
     * {@link #getLine()}
     * @return the line number
     */
    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * String representation of the line number in the csv
     * {@link #getRow()}
     * @return the line string
     */
    public String getLine(){
        return String.valueOf(row);
    }


    public ImportDeduplicationHelper<CsvImportState> getDedupHelper() {
        if (this.dedupHelper == null){
            this.dedupHelper = ImportDeduplicationHelper.NewInstance(getCurrentIO(), this);
        }
        return dedupHelper;
    }


    public void setDedupHelper(ImportDeduplicationHelper<CsvImportState> dedupHelper) {
        this.dedupHelper = dedupHelper;
    }


    public Reference getSourceReference() {
        return sourceReference;
    }
    public void setSourceReference(Reference sourceReference) {
        this.sourceReference = sourceReference;
    }

}
