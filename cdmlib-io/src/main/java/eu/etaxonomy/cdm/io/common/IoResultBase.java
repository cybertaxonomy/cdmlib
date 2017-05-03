/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author a.mueller
 * @date 24.03.2017
 *
 */
public abstract class IoResultBase {

    private List<Error> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<Error> exceptions = new ArrayList<>();

    public class Error{
        String message;
        Exception exception;
        private Error(String msg, Exception e){this.message = msg; this.exception = e;}
    }

// ************* GETTERS / SETTERS / ADDERS ***********************/

    public List<Error> getErrors() {return errors;}
    public void setErrors(List<Error> errors) {this.errors = errors;}
    public void addError(String error) {
        errors.add(new Error(error, null));
    }
    public void addError(String error, Exception e) {
        errors.add(new Error(error, e));
    }

    public List<String> getWarnings() {return warnings;}
    public void setWarnings(List<String> warnings) {this.warnings = warnings;}
    public void addWarning(String warning) {
//       warnings.add(warning.getBytes(StandardCharsets.UTF_8));
        warnings.add(warning);
    }

    public List<Error> getExceptions() {return exceptions;}
    public void setExceptions(List<Error> exceptions) {this.exceptions = exceptions;}
    public void addException(Exception e) {
        exceptions.add(new Error(null, e));
        setExceptionState();
    }
    public void addException(Exception e, String message) {
        exceptions.add(new Error(message, e));
        setExceptionState();
    }

    protected abstract void setExceptionState();

    /**
     * Adds an error and aborts the import.
     * @param string
     */
    public void setAborted(String error) {
        this.addError(error);
        this.setAborted();
    }

    public abstract void setAborted();

    /**
     * @return
     */
    public StringBuffer createReport() {
        StringBuffer report = new StringBuffer("");
        addErrorReport(report, "Errors", errors);
        addErrorReport(report, "Exceptions", exceptions);
        addWarnings(report, "Warnings", warnings);
        return report;
    }
    /**
     * @param report
     * @param string
     * @param warnings2
     */
    private void addWarnings(StringBuffer report, String label, List<String> list) {
        if (!list.isEmpty()){
            report.append("\n\n" + label + ":\n" + StringUtils.leftPad("", label.length()+1, "="));
            for (String warning : list){
                String str = String.valueOf(warning);
                report.append("\n" + str);
            }
        }
    }
    /**
     * @param report
     * @param string
     * @param newRecords2
     */
    private void addErrorReport(StringBuffer report, String label, List<Error> list) {
        if (!errors.isEmpty()){
            report.append("\n\n" + label + ":\n" + StringUtils.leftPad("", label.length()+1, "="));
            for (Error error : list){
                String message = error.message != null ? error.message : error.exception != null ? error.exception.getMessage() : "";
                message = StringUtils.isBlank(message)? "no message" : message;
                Object stacktrace = error.exception.getStackTrace();
                String available = (stacktrace == null ? " not" : "");
                report.append("\n" + message + "(stacktrace" + available + ")");
            }
        }
    }
}
