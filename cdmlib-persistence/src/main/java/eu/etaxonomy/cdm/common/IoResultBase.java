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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.IoResultBase.IoInfo;
import eu.etaxonomy.cdm.strategy.parser.ParserResult;

/**
 * @see ParserResult
 * @author a.mueller
 * @since 24.03.2017
 */
public abstract class IoResultBase
            extends ResultBase<IoInfo>
            implements Serializable{

    private static final long serialVersionUID = -2077936463767046918L;

    public class IoInfo extends ResultBase.ResultInfoBase implements Serializable{
        private static final long serialVersionUID = -8077358746590123757L;
        String dataLocation;
        private IoInfo(String msg, Exception e){
            super(msg, e, null);
        }
        private IoInfo(String msg, Exception e, String codeLocation, String dataLocation){
            super(msg, e, codeLocation);
            this.dataLocation = dataLocation;
        }

        public String getDataLocation(){
            return dataLocation;
        }
        public IoInfo setDataLocation(String dataLocation){
            this.dataLocation = dataLocation;
            return this;
        }
    }

// ************* GETTERS / SETTERS / ADDERS ***********************/

    //errors
    public void addError(String message, int location) {
        addError(message, null, getLocationByException(), String.valueOf(location));
    }
    public void addError(String message, Exception e, String codeLocation, String dataLocation){
        addError(newResultInfo(message, e, makeLocation(e, codeLocation), dataLocation));
    }

    //warnings
    public void addWarning(String message, int location) {
        addWarning(message, null, String.valueOf(location));
    }
    public void addWarning(String message, String codeLocation, String dataLocation) {
        addWarning(newResultInfo(message, null, codeLocation, dataLocation));
    }

    //exceptions
    @Override
    public void addException(Exception e, String message, String codeLocation) {
        addException(e, message, codeLocation, null);
    }
    public void addException(Exception e, String message, String codeLocation, String dataLocation) {
        addException(newResultInfo(message, e, makeLocation(e, codeLocation), dataLocation));
        setExceptionState();
    }

    //new ResultInfo
    @Override
    protected IoInfo newResultInfo(String message, Exception e, String codeLocation) {
        return new IoInfo(message, e, codeLocation, null);
    }

    private IoInfo newResultInfo(String message, Exception e, String codeLocation, String dataLocation) {
        return newResultInfo(message, e, codeLocation).setDataLocation(dataLocation);
    }

    //******************* MERGE **************************/

    public void getParserResultMessages(ParserResult<?> parserResult) {
        parserResult.getWarnings().forEach(w->addWarning(parserResultToIoResult(w)));
        parserResult.getErrors().forEach(w->addError(parserResultToIoResult(w)));
        parserResult.getExceptions().forEach(w->addException(parserResultToIoResult(w)));
    }

    private IoInfo parserResultToIoResult(ParserResult<?>.ParserInfo parserInfo) {
        return new IoInfo(parserInfo.getMessage(), parserInfo.getException(), parserInfo.getCodeLocation(), null);
    }
    //****************************************************/

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

    public StringBuffer createReport() {
        StringBuffer report = new StringBuffer("");
        addShortDescription(report);
        addErrorReport(report, "Errors", getErrors());
        addErrorReport(report, "Exceptions", getExceptions());
        addErrorReport(report, "Warnings", getWarnings());
        return report;
    }

    protected void addShortDescription(StringBuffer report) {
        //do nothing
    }

    private void addErrorReport(StringBuffer report, String label, List<IoInfo> list) {
        if (!list.isEmpty()){
            report.append("\n\n" + label + ":\n" + StringUtils.leftPad("", label.length()+1, "="));
            for (IoInfo ioInfo : list){
                String codeLocation = ioInfo.getCodeLocation() == null ? "" : ( "[" + ioInfo.getCodeLocation() + "]");
                String dataLocation = ioInfo.dataLocation == null ? "" : (ioInfo.dataLocation + ": ");
                String message = ioInfo.getMessage() != null ? (ioInfo.getMessage()) :
                        ioInfo.getException() != null ?
                        (ioInfo.getException().getMessage()) : "";

                message = StringUtils.isBlank(message)? "no message" : message;
                Object stacktrace = ioInfo.getException() == null? null : ioInfo.getException().getStackTrace();
                String available = (stacktrace != null ? "(stacktrace available)" : "");
                report.append("\n" + CdmUtils.concat(" ", dataLocation, message, available, codeLocation));
            }
        }
    }
}