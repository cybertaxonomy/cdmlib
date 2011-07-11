// $Id$
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
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * @author a.kohlbecker
 * @date 06.04.2009
 *
 */
public class HttpStatusMessage {
	
	public final static HttpStatusMessage UUID_MISSING = new HttpStatusMessage(HttpServletResponse.SC_BAD_REQUEST, "missing uuid parameter");
	public final static HttpStatusMessage UUID_INVALID = new HttpStatusMessage(HttpServletResponse.SC_BAD_REQUEST, "invalid uuid");
	public final static HttpStatusMessage UUID_NOT_FOUND = new HttpStatusMessage(HttpServletResponse.SC_NOT_FOUND, "uuid not found");
	public final static HttpStatusMessage UUID_REFERENCES_WRONG_TYPE = new HttpStatusMessage(HttpServletResponse.SC_NOT_FOUND, "uuid references wrong type");

	public final static HttpStatusMessage PROPERTY_NOT_FOUND = new HttpStatusMessage(HttpServletResponse.SC_NOT_FOUND, "property not found");
	
	public final static HttpStatusMessage INTERNAL_ERROR = new HttpStatusMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
	
	
	private int statusCode;
	
	private String message;
	
	private  String statusMessage;
	
	private static Hashtable<String , HttpStatusMessage> cache;
	
	private HttpStatusMessage(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
		this.statusMessage = StringUtils.leftPad(Integer.toString(statusCode), 3, "0") + message;
		if(cache == null) {
			 cache = new Hashtable<String , HttpStatusMessage>();
		}
		HttpStatusMessage.cache.put(statusMessage, this);
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
	
	public String toString() {
		return statusMessage;
	}
	
	public static HttpStatusMessage fromString(String statusMessage) {
		return cache.get(statusMessage);
	}
	
	public void send(HttpServletResponse response) throws IOException{
		response.sendError(getStatusCode(), getMessage());
	}

}
