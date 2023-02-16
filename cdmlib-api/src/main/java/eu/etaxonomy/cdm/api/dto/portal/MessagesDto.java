/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

/**
 * DTO to report errors, warnings or other types of messages.
 * This may be errors that occur during DTO creation.
 * Or debug messages for debug mode.
 *
 * @author a.mueller
 * @date 07.01.2023
 */
public class MessagesDto {

    public static enum MessageType{
        ERROR,
        WARN;
    }

    public static MessagesDto NewErrorInstance(String message, Exception exception) {
        return new MessagesDto(MessageType.ERROR, message, exception);
    }
    public static MessagesDto NewErrorInstance() {
        return new MessagesDto(MessageType.ERROR, null, null);
    }

    private MessageType type;
    private String message;
    private String exceptionMessage;
    private String stackTrace;

    private MessagesDto(MessageType type, String message, Exception e) {
        this.type = type;
        this.message = message;
        this.setExceptionMessage(e.getMessage());
        this.setStackTrace(e.getStackTrace().toString());
    }

//************ GETTER / SETTER *****************


    public String getMessage() {
        return message;
    }
    public MessageType getType() {
        return type;
    }
    public void setType(MessageType type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getExceptionMessage() {
        return exceptionMessage;
    }
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
    public String getStackTrace() {
        return stackTrace;
    }
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}