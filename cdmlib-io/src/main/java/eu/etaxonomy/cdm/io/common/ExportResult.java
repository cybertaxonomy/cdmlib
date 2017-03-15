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

    private ExportResultState success;

    private List<byte[]> data = new ArrayList<>();  //resulting files

    private List<byte[]> errors = new ArrayList<>();
    private List<byte[]> warnings = new ArrayList<>();

    private List<Exception> exceptions = new ArrayList<>();

    public ExportResult() {
        success = ExportResultState.SUCCESS;
        data = new ArrayList<>();
    }

    public enum ExportResultState{
        SUCCESS,
        SUCCESS_BUT_NO_DATA,
        SUCCESS_WITH_WARNING,
        FINISHED_WITH_ERROR,
        ABORTED,
        CANCELED,
        ERROR
        ;
    }

// **************** GETTER /SETTER *********************/

    public ExportResultState isSuccess() {return success;}
    public void setSuccess(ExportResultState success) {this.success = success;}

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
     * @deprecated use {@link #setSuccess(ExportResultState)} instead
     * @param doCheck
     */
    @Deprecated
    public void setSuccess(boolean success) {
       if (success){
           this.setSuccess(ExportResultState.SUCCESS);
       }else{
           this.setSuccess(ExportResultState.ERROR);
       }

    }

}
