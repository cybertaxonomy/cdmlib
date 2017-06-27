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
        String location;
        private IoInfo(String msg, Exception e){
            this.message = msg;
            this.exception = e;
        }
        private IoInfo(String msg, Exception e, String location){
            this.message = msg;
            this.exception = e;
            this.location = location;
        }

        public String getMessage(){
            return message;
        }

        public Exception getException(){
            return exception;
        }
    }



// ************* GETTERS / SETTERS / ADDERS ***********************/

    public List<IoInfo> getErrors() {return errors;}
    public void setErrors(List<IoInfo> ioInfos) {this.errors = ioInfos;}
    public void addError(String message) {
        addError(message, null, null);
    }
    public void addError(String message, Exception e) {
        addError(message, e, null);
    }
    public void addError(String message, int location) {
        addError(message, null, String.valueOf(location));
    }
    public void addError(String message, String location) {
        addError(message, null, location);
    }
    public void addError(String message, Exception e, String location) {
        errors.add(new IoInfo(message, e, makeLocation(e, location)));
    }

    public List<IoInfo> getWarnings() {return warnings;}
    public void setWarnings(List<IoInfo> warnings) {this.warnings = warnings;}
    public void addWarning(String message) {
//       warnings.add(warning.getBytes(StandardCharsets.UTF_8));
        addWarning(message, null);
    }
    public void addWarning(String message, int location) {
        addWarning(message, String.valueOf(location));
    }
    public void addWarning(String message, String location) {
        warnings.add(new IoInfo(message, null, location));
    }

    public List<IoInfo> getExceptions() {return exceptions;}
    public void setExceptions(List<IoInfo> exceptions) {this.exceptions = exceptions;}
    public void addException(Exception e) {
        addException(e, null, null);
        setExceptionState();
    }
    public void addException(Exception e, String message) {
        addException(e, message, null);
        setExceptionState();
    }
    public void addException(Exception e, String message, String location) {
        exceptions.add(new IoInfo(message, e, makeLocation(e, location)));
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
                location = el.getMethodName() + "(" +  el.getClassName() + ":" + el.getLineNumber() + ")";
            }
        }
        return location;
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
//    /**
//     * @param report
//     * @param string
//     * @param warnings2
//     */
//    private void addWarnings(StringBuffer report, String label, List<String> list) {
//        if (!list.isEmpty()){
//            report.append("\n\n" + label + ":\n" + StringUtils.leftPad("", label.length()+1, "="));
//            for (String warning : list){
//                String str = String.valueOf(warning);
//                report.append("\n" + str);
//            }
//        }
//    }

    /**
     * @param report
     * @param label
     * @param list
     */
    private void addErrorReport(StringBuffer report, String label, List<IoInfo> list) {
        if (!list.isEmpty()){
            report.append("\n\n" + label + ":\n" + StringUtils.leftPad("", label.length()+1, "="));
            for (IoInfo ioInfo : list){
                String location = ioInfo.location == null ? "" : (ioInfo.location + ": ");
                String message = ioInfo.message != null ? ioInfo.message : ioInfo.exception != null ? ioInfo.exception.getMessage() : "";

                message = StringUtils.isBlank(message)? "no message" : message;
                Object stacktrace = ioInfo.exception == null? null : ioInfo.exception.getStackTrace();
                String available = (stacktrace != null ? " (stacktrace available)" : "");
                report.append("\n" + location + message + available);
            }
        }
    }
}
