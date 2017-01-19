/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
 
package eu.etaxonomy.cdm.api.service.lsid.impl;

import java.util.HashMap;
import java.util.Map;

import com.ibm.lsid.LSIDCredentials;
import com.ibm.lsid.wsdl.LSIDAuthorityPort;
import com.ibm.lsid.wsdl.LSIDDataPort;
import com.ibm.lsid.wsdl.LSIDMetadataPort;

/**
 * Implements the public interface LSIDDataPort so that instances of this
 * class may be used to retrieve data.
 */
public class LsidStandardPortImpl implements LSIDDataPort, LSIDMetadataPort, LSIDAuthorityPort {

	private String name = null;
	private String serviceName = null;
	private String location;
	private String protocol;
	private String path;
	private LSIDCredentials lsidCredentials = null;
	private Map<String,String> headers = new HashMap<String,String>();
	/**
	 * Method addProtocolHeader.
	 * 
	 * @param name
	 * @param value
	 */
	public void addProtocolHeader(String name, String value) {
		headers.put(name, value);
	}

	/**
	 * Method getProtocolHeaders.
	 * @return Map
	 */
	public Map<String,String> getProtocolHeaders() {
		return headers;
	}

	/**
	 * Returns the lsidCredentials.
	 * @return LSIDCredentials
	 */
	public LSIDCredentials getLsidCredentials() {
		if (lsidCredentials == null) {
			LSIDCredentials portCreds = new LSIDCredentials(this);
			if (portCreds.keys().hasMoreElements())
				lsidCredentials = portCreds;
		}
		return lsidCredentials;
	}

	/**
	 * Sets the lsidCredentials.
	 * @param lsidCredentials The lsidCredentials to set
	 */
	public void setLsidCredentials(LSIDCredentials lsidCredentials) {
		this.lsidCredentials = lsidCredentials;
	}

	/**
	 * Return the name of the port
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see com.ibm.lsid.client.LSIDDataPort#getServiceName()
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Return the host name of the data, includes the protocol for http (http://hostname), 
	 * return entire URI for soap
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Return the path of the data
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Return the protocol that must be used to retrieve the data
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * get the key to store this port by
	 */
	public String getKey() {
		return serviceName + ":" + name;
	}

	public Map getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String,String> headers) {
		this.headers = headers;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
