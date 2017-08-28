/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author a.mueller
 * @date 24.03.2017
 *
 */
public abstract class IoResultBase implements Serializable{

    private static final long serialVersionUID = -2077936463767046918L;

    private List<IoInfo> errors = new ArrayList<>();
    private List<IoInfo> warnings = new ArrayList<>();
    private List<IoInfo> exceptions = new ArrayList<>();

    public class IoInfo implements Serializable{
        private static final long serialVersionUID = -8077358746590123757L;
        String message;
        Exception exception;
        String codeLocation;
        String dataLocation;
        private IoInfo(String msg, Exception e){
            this.message = msg;
            this.exception = e;
        }
//        private IoInfo(String msg, Exception e, String location){
//            this.message = msg;
//            this.exception = e;
//            this.codeLocation = location;
//        }
        private IoInfo(String msg, Exception e, String codeLocation, String dataLocation){
            this.message = msg;
            this.exception = e;
            this.codeLocation = codeLocation;
            this.dataLocation = dataLocation;
        }

        public String getMessage(){
            return message;
        }

        public Exception getException(){
            return exception;
        }
        public String getCodeLocation(){
            return codeLocation;
        }
        public String getDataLocation(){
            return dataLocation;
        }
    }



// ************* GETTERS / SETTERS / ADDERS ***********************/

    public List<IoInfo> getErrors() {return errors;}
    public void setErrors(List<IoInfo> ioInfos) {this.errors = ioInfos;}
    public void addError(String message) {
        addError(message, null, getLocationByException());
    }
    public void addError(String message, Exception e) {
        addError(message, e, null, null);
    }
    public void addError(String message, int location) {
        addError(message, null, getLocationByException(), String.valueOf(location));
    }
    public void addError(String message, String codeLocation) {
        addError(message, null, codeLocation, null);
    }
    public void addError(String message, Exception e, String codeLocation) {
        addError(message, e, codeLocation, null);
    }
    public void addError(String message, Exception e, String codeLocation, String dataLocation) {
        errors.add(new IoInfo(message, e, makeLocation(e, codeLocation), dataLocation));
    }


    public List<IoInfo> getWarnings() {return warnings;}
    public void setWarnings(List<IoInfo> warnings) {this.warnings = warnings;}
    public void addWarning(String message) {
        addWarning(message, getLocationByException(), null);
    }
    public void addWarning(String message, int location) {
        addWarning(message, null, String.valueOf(location));
    }
    public void addWarning(String message, String codeLocation) {
        addWarning(message, codeLocation, null);
    }
    public void addWarning(String message, String codeLocation, String dataLocation) {
        warnings.add(new IoInfo(message, null, codeLocation, dataLocation));
    }



    public List<IoInfo> getExceptions() {return exceptions;}
    public void setExceptions(List<IoInfo> exceptions) {this.exceptions = exceptions;}
    public void addException(Exception e) {
        addException(e, null, null, null);
    }
    public void addException(Exception e, String message) {
        addException(e, message, null, null);
    }
    public void addException(Exception e, String message, String codeLocation) {
        addException(e, message, codeLocation, null);
    }
    public void addException(Exception e, String message, String codeLocation, String dataLocation) {
        exceptions.add(new IoInfo(message, e, makeLocation(e, codeLocation), dataLocation));
        setExceptionState();
    }


    /**
     * Computes the location string. If location is not null the location
     * parameter is returned. If location is <code>null</code> the stacktrace
     * is examined and tried to retrieve the location from there
     * @param e
     * @param location
     * @return
     */
    private String makeLocation(Throwable e, String location) {
        if (location == null && e != null){
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0){
                StackTraceElement el = stackTrace[0];
                location = locByStackTraceElement(el);
            }
        }
        return location;
    }
    private String getLocationByException() {
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
        addErrorReport(report, "Warnings", warnings);
        return report;
    }


    /**
     * @param report
     * @param label
     * @param list
     */
    private void addErrorReport(StringBuffer report, String label, List<IoInfo> list) {
        if (!list.isEmpty()){
            report.append("\n\n" + label + ":\n" + StringUtils.leftPad("", label.length()+1, "="));
            for (IoInfo ioInfo : list){
                String codeLocation = ioInfo.codeLocation == null ? "" : ( "[" + ioInfo.codeLocation + "]");
                String dataLocation = ioInfo.dataLocation == null ? "" : (ioInfo.dataLocation + ": ");
                String message = ioInfo.message != null ? ioInfo.message : ioInfo.exception != null ? ioInfo.exception.getMessage() : "";

                message = StringUtils.isBlank(message)? "no message" : message;
                Object stacktrace = ioInfo.exception == null? null : ioInfo.exception.getStackTrace();
                String available = (stacktrace != null ? " (stacktrace available)" : "");
                report.append("\n" + dataLocation + message + available + codeLocation);
            }
        }
    }
}
