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
 * @date 31 Jul 2015
 *
 */
public class ExportResult implements Serializable {

    private static final long serialVersionUID = 6843406252245776806L;

    private ExportResultState state;

    private List<byte[]> data = new ArrayList<>();  //resulting files

    private List<byte[]> errors = new ArrayList<>();
    private List<byte[]> warnings = new ArrayList<>();

    private List<Exception> exceptions = new ArrayList<>();

// **************************** FACTORY ****************************************/

    public static ExportResult NewInstance(){
        return new ExportResult();
    }

    public static ExportResult NewNoDataInstance(){
        ExportResult result = new ExportResult();
        result.state = ExportResultState.SUCCESS_BUT_NO_DATA;
        return result;
    }


// *********************** CONSTRUCTOR *****************************************/

    private ExportResult() {
        state = ExportResultState.SUCCESS;
        data = new ArrayList<>();
    }

    public enum ExportResultState{
        SUCCESS_BUT_NO_DATA,   //Only if NO data at all is exported, if only 1 class is exported use SUCCESS
        SUCCESS,               //All configured data exported, no warning, no errors
        SUCCESS_WITH_WARNING,   //All data exported but with some warnings
        FINISHED_WITH_ERROR,    //Probably all data exported but with errors
        INCOMPLETE_WITH_ERROR,  //Run to the end, but in the middle there might be "larger" amounts of data missing, e.g. some parts did not run to the end
        CANCELED,              //Export canceled by the user
        ABORTED,                //An handled exception occurred that lead to abort the export
        ;
    }

// **************** GETTER /SETTER *********************/

    public boolean isSuccess(){
        return state == ExportResultState.SUCCESS || state == ExportResultState.SUCCESS_WITH_WARNING;
    }

    public ExportResultState getState() {return state;}
    public void setState(ExportResultState state) {this.state = state;}



    public List<byte[]> getExportData() {return data;}
    public void setExportData(List<byte[]> data) {this.data = data;}
    public void addExportData(byte[] exportData) {
        data.add(exportData);
    }

    public List<byte[]> getErrors() {return errors;}
    public void setErrors(List<byte[]> errors) {this.errors = errors;}
    public void addError(String error) {
//        errors.add(error);
    }

    public List<byte[]> getWarnings() {return warnings;}
    public void setWarnings(List<byte[]> warnings) {this.warnings = warnings;}
    public void addWarning(String warning) {
//        warnings.add(StringUtils..warning.to);
    }

    /**
     * @return the exceptions
     */
    public List<Exception> getExceptions() {
        return exceptions;
    }

    /**
     * @param exceptions the exceptions to set
     */
    public void setExceptions(List<Exception> exceptions) {
        this.exceptions = exceptions;
    }

    /**
     *
     */
    public void setAborted() {
        this.state = ExportResultState.ABORTED;
    }

    /**
     * @param e
     */
    public void addException(Exception e) {
        exceptions.add(e);
        this.state = ExportResultState.INCOMPLETE_WITH_ERROR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return state.toString();
    }

    /**
     * @param invoke
     */
    public void merge(ExportResult invoke) {
        // TODO implemented
    }




}
