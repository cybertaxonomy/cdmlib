/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.common.ResultBase.ResultInfoBase;

/**
 * @author muellera
 * @since 27.10.2025
 */
public abstract class ResultBase<T extends ResultInfoBase> {

    private List<T> errors = new ArrayList<>();
    private List<T> warnings = new ArrayList<>();
    private List<T> exceptions = new ArrayList<>();

    public static abstract class ResultInfoBase{
        String message;
        Exception exception;
        String codeLocation;

        public ResultInfoBase(String msg, Exception e, String codeLocation){
            this.message = msg;
            this.exception = e;
            this.codeLocation = codeLocation;
        }
        public String getMessage(){return message;}
        public Exception getException(){return exception;}
        public String getCodeLocation(){return codeLocation;}
    }

    // ************* GETTERS / SETTERS / ADDERS ***********************/

    //errors
    public List<T> getErrors() {return errors;}
    public void setErrors(List<T> resultInfos) {this.errors = resultInfos;}
    public void addError(String message) {
        addError(message, null, getLocationByException());
    }
    public void addError(String message, Exception e) {
        addError(message, e, null);
    }
    public void addError(String message, String codeLocation) {
        addError(message, null, codeLocation);
    }
    public void addError(String message, Exception e, String codeLocation) {
        addError(newResultInfo(message, e, codeLocation));
    }
    protected void addError(T error) {
        errors.add(error);
    }

    //warnings
    public List<T> getWarnings() {return warnings;}
    public void setWarnings(List<T> warnings) {this.warnings = warnings;}
    public void addWarning(String message) {
        addWarning(message, getLocationByException());
    }
    public void addWarning(String message, String codeLocation) {
        addWarning(newResultInfo(message, null, codeLocation));
    }
    protected void addWarning(T warning) {
        warnings.add(warning);
    }

    //exceptions
    public List<T> getExceptions() {return exceptions;}
    public void setExceptions(List<T> exceptions) {this.exceptions = exceptions;}
    public void addException(Exception e) {
        addException(e, null, null);
    }
    public void addException(Exception e, String message) {
        addException(e, message, null);
    }
    public void addException(Exception e, String message, String codeLocation) {
        addException(newResultInfo(message, e, makeLocation(e, codeLocation)));
//        setExceptionState();
    }
    protected void addException(T exception) {
        exceptions.add(exception);
    }

    protected abstract T newResultInfo(String message, Exception e, String codeLocation);

    //***************** COMMON **************************+

    /**
     * Computes the location string. If location is not null the location
     * parameter is returned. If location is <code>null</code> the stacktrace
     * is examined and tried to retrieve the location from there
     */
    protected String makeLocation(Throwable e, String location) {
        if (location == null && e != null){
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0){
                StackTraceElement el = stackTrace[0];
                location = locByStackTraceElement(el);
            }
        }
        return location;
    }

    protected String getLocationByException() {
        try {
            throw new RuntimeException();
        } catch (Exception e) {
            StackTraceElement st = e.getStackTrace()[2];
            return locByStackTraceElement(st);
        }
    }

    private String locByStackTraceElement(StackTraceElement st) {
        return st.getMethodName() + "(" + st.getClassName()+ ":" + st.getLineNumber() + ")";
    }
}