/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cmathew
 * @date 5 Aug 2015
 *
 */
public class ImportResult extends IoResultBase implements Serializable {
    private static final long serialVersionUID = -7299667532720042100L;

    private List<byte[]> reports = new ArrayList<>();

    private ImportResultState state;

// **************************** FACTORY ****************************************/

    public static ImportResult NewInstance(){
        return new ImportResult();
    }

    public static ImportResult NewNoDataInstance(){
        ImportResult result = new ImportResult();
        result.state = ImportResultState.SUCCESS_BUT_NO_DATA;
        return result;
    }

// *********************** CONSTRUCTOR *****************************************/

    public ImportResult() {
        state = ImportResultState.SUCCESS;
    }

    public enum ImportResultState{
        SUCCESS_BUT_NO_DATA,   //Only if NO data at all is exported, if only 1 class is exported use SUCCESS
        SUCCESS,               //All configured data exported, no warning, no errors
        SUCCESS_WITH_WARNING,   //All data exported but with some warnings
        FINISHED_WITH_ERROR,    //Probably all data exported but with errors
        INCOMPLETE_WITH_ERROR,  //Run to the end, but in the middle there might be "larger" amounts of data missing, e.g. some parts did not run to the end
        CANCELED,              //Export canceled by the user
        ABORTED,                //An handled exception occurred that lead to abort the export
        ;
    }

    @Override
    protected void setExceptionState() {
        state = ImportResultState.INCOMPLETE_WITH_ERROR;
    }

    @Override
    public void setAborted() {this.state = ImportResultState.ABORTED;}

    public boolean isSuccess(){
        return state == ImportResultState.SUCCESS || state == ImportResultState.SUCCESS_WITH_WARNING;
    }

    /**
     * @param success
     * @param successTaraxacum
     */
    public void merge(ImportResult otherResult) {
        // TODO implement merge
    }

    public List<byte[]> getReports() {
        return reports;
    }
    /**
     * @param reports the reports to set
     */
    public void setReports(List<byte[]> reports) {
        this.reports = reports;
    }

    public void addReport(byte[] report) {
        reports.add(report);
    }
}
