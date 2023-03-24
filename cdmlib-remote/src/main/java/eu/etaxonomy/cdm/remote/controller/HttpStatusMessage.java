/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.kohlbecker
 * @since 06.04.2009
 */
public class HttpStatusMessage {

    private static final Logger logger = LogManager.getLogger();

    public final static HttpStatusMessage UUID_MISSING = new HttpStatusMessage(HttpServletResponse.SC_BAD_REQUEST, "missing uuid parameter");
    public final static HttpStatusMessage UUID_INVALID = new HttpStatusMessage(HttpServletResponse.SC_BAD_REQUEST, "invalid uuid");
    public final static HttpStatusMessage UUID_NOT_FOUND = new HttpStatusMessage(HttpServletResponse.SC_NOT_FOUND, "uuid not found");
    public final static HttpStatusMessage UUID_REFERENCES_WRONG_TYPE = new HttpStatusMessage(HttpServletResponse.SC_NOT_FOUND, "uuid references wrong type");

    public final static HttpStatusMessage PROPERTY_NOT_FOUND = new HttpStatusMessage(HttpServletResponse.SC_NOT_FOUND, "property not found");

    public final static HttpStatusMessage INTERNAL_ERROR = new HttpStatusMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");

    public final static HttpStatusMessage ACCESS_DENIED = new HttpStatusMessage(HttpServletResponse.SC_FORBIDDEN, "access denied");
    public final static HttpStatusMessage SUBTREE_FILTER_INVALID = new HttpStatusMessage(HttpServletResponse.SC_NOT_FOUND, "invalid uuid for subtree filter");

    private int statusCode;

    private final String message;

    private HttpStatusMessage(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    /**
     * create a new HttpStatusMessage
     */
    public static HttpStatusMessage create(String statusMessage, int statusCode) {
        return new HttpStatusMessage(statusCode, statusMessage);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpStatusMessage setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return StringUtils.leftPad(Integer.toString(statusCode), 3, "0") + message;
    }

    public void send(HttpServletResponse response) throws IOException{
        send(response, null);
    }

    public void send(HttpServletResponse response, String message) throws IOException{
        message = getMessage() + ((message == null)? "": ". " + message);
        logger.info("HTTP " + getStatusCode() + " : " +  message);
        response.sendError(getStatusCode(), message);
    }
}