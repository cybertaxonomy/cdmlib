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

    private List<IoInfo> ioInfos = new ArrayList<>();
    private List<IoInfo> warnings = new ArrayList<>();
    private List<IoInfo> exceptions = new ArrayList<>();

    public class IoInfo{
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

    public List<IoInfo> getErrors() {return ioInfos;}
    public void setErrors(List<IoInfo> ioInfos) {this.ioInfos = ioInfos;}
    public void addError(String error) {
        ioInfos.add(new IoInfo(error, null));
    }
    public void addError(String error, Exception e) {
        ioInfos.add(new IoInfo(error, e));
    }
    public void addError(String message, int location) {
        ioInfos.add(new IoInfo(message, null, String.valueOf(location)));
    }

    public List<IoInfo> getWarnings() {return warnings;}
    public void setWarnings(List<IoInfo> warnings) {this.warnings = warnings;}
    public void addWarning(String warning) {
//       warnings.add(warning.getBytes(StandardCharsets.UTF_8));
        warnings.add(new IoInfo(warning, null));
    }
    public void addWarning(String message, int location) {
        warnings.add(new IoInfo(message, null, String.valueOf(location)));
    }

    public List<IoInfo> getExceptions() {return exceptions;}
    public void setExceptions(List<IoInfo> exceptions) {this.exceptions = exceptions;}
    public void addException(Exception e) {
        exceptions.add(new IoInfo(null, e));
        setExceptionState();
    }
    public void addException(Exception e, String message) {
        exceptions.add(new IoInfo(message, e));
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
        addErrorReport(report, "Errors", ioInfos);
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
        if (!ioInfos.isEmpty()){
            report.append("\n\n" + label + ":\n" + StringUtils.leftPad("", label.length()+1, "="));
            for (IoInfo ioInfo : list){
                String location = ioInfo.location == null ? "" : (ioInfo.location + ": ");
                String message = ioInfo.message != null ? ioInfo.message : ioInfo.exception != null ? ioInfo.exception.getMessage() : "";
                message = StringUtils.isBlank(message)? "no message" : message;
                Object stacktrace = ioInfo.exception.getStackTrace();
                String available = (stacktrace == null ? " not" : "");
                report.append("\n" + location + message + "(stacktrace" + available + ")");
            }
        }
    }
}
