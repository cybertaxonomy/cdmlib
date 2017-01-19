/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid;

import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.transform.Source;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.wsdl.LSIDAuthorityPort;
import com.ibm.lsid.wsdl.LSIDDataPort;
import com.ibm.lsid.wsdl.LSIDMetadataPort;
import com.ibm.lsid.wsdl.LSIDPort;
import com.ibm.lsid.wsdl.LSIDStandardPort;
/**
 * An interface extracted from com.ibm.lsid.wsdl.LSIDWSDLWrapper which allows
 * different implementations of LSIDWSDLWrapper
 * 
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>)
 * @author ben
 *
 * @see com.ibm.lsid.wsdl.LSIDWSDLWrapper
 */
public interface LSIDWSDLWrapper {
	/**
	 * Get the WSDL Definition
	 * @return the WSDL Definition as a javax.xml.transform.Source object
	 * @throws WSDLException
	 */	
	public Source getWSDLSource() throws WSDLException;

	/**
	 * Get the WSDL String
	 * @return String get the String representation of the WSDL
	 */
	public String getWSDL();
	
	/**
	 * Get the WSDL String
	 * @return String the String representation of the WSDL
	 */
	public String toString();
	
	/**
	 * Get the Defintion built by wsdl4j. 
	 * @return javax.wsdl.Definition the WSDL Definition 
	 */
	public Definition getDefinition();
	
	/**
	 * Returns the expiration.
	 * @return Date null value 
	 */
	public Date getExpiration();
	
	/**
	 * Sets the expiration.
	 * @param expiration The expiration to set
	 */
	public void setExpiration(Date expiration);
	
	/**
	 * Get the names of the services.  All metadata ports in a given service must be the same.
	 * the client may assume that all the authorative metadata may be obtained by requesting
	 * one port from each service.
	 * @return Enumeration the service names
	 */
	public Enumeration<String> getServiceNames();
	
	/**
	 * Get the keys of all the metadata ports
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getMetadataPortNames();
	
	/**
	 * Get the keys of all the metadata ports in the given service
	 * @param String the service
	 * @return LSIDMetadataPort the port
	 */
	public Enumeration<String> getMetadataPortNamesForService(String servicename);
	
	/**
	 * Get the keys of all the metadata ports for the given protocol
	 * @param String the protocol
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getMetadataPortNamesForProtocol(String protocol);
	
	/**
	 * Get an arbitrary metadata port if one exists. 
	 * @return LSIDMetadataPort a metadata port if one exits, null otherwise. Uses protocol preference order: HTTP, FTP, ANY
	 */
	public LSIDMetadataPort getMetadataPort();
	
	/**
	 * Get the metadata port with the given key
	 * @param String the key of the port, of the form "serviceName:portName"
	 * @return LSIDMetadataPort, the metadata port
	 */
	public LSIDMetadataPort getMetadataPort(String name);
	
	/**
	 * Get an arbitray metadata port for the given protocol
	 * @param String the protocol
	 * @return LSIDMetadataPort, the metadata port if one exists, null otherwise.
	 */
	public LSIDMetadataPort getMetadataPortForProtocol(String protocol);
	
	/**
	 * Get the keys of all the metadata ports
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getDataPortNames();
	
	/**
	 * Get the keys of all the ports for the given protocol
	 * @param String the protocol
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getDataPortNamesForProtocol(String protocol);
	
	/**
	 * Get an arbitrary data port if one exists. 
	 * @return LSIDDataPort a data port if one exits, null otherwise. Uses protocol preference order: HTTP, FTP, ANY
	 */
	public LSIDDataPort getDataPort();
	
	/**
	 * Get the data port with the given key
	 * @param String the key of the port, of the form "serviceName:portName"
	 * @return LSIDDataPort, the data port
	 */
	public LSIDDataPort getDataPort(String name);
	
	/**
	 * Get an arbitray data port for the given protocol
	 * @param String the protocol
	 * @return LSIDDataPort, the data port if one exists, null otherwise.
	 */
	public LSIDDataPort getDataPortForProtocol(String protocol);
	
	/**
	 * Get the keys of all the authority ports
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getAuthorityPortNames();
	
	/**
	 * Get the keys of all the authority ports for the given protocol
	 * @param String the protocol
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getAuthorityPortNamesForProtocol(String protocol);
	
	/**
	 * Get an arbitrary authority port if one exists. 
	 * @return LSIDAuthorityPort an authority port if one exits, null otherwise. Uses protocol preference order: HTTP, SOAP
	 */
	public LSIDAuthorityPort getAuthorityPort();
	
	/**
	 * Get the authority port with the given key
	 * @param String the key of the port, of the form "serviceName:portName"
	 * @return LSIDAuthorityPort, the authority port
	 */
	public LSIDAuthorityPort getAuthorityPort(String name);
	
	/**
	 * Get an arbitray authority port for the given protocol
	 * @param String the protocol
	 * @return LSIDAuthorityPort, the authority port if one exists, null otherwise.
	 */
	public LSIDAuthorityPort getAuthorityPortForProtocol(String protocol);
	
	/**
	 * Get all the names of extension port names
	 * @return Enumeration an enum of strings, each string can be used as a key for <code>getExtensionPort()</code>
	 */
	public Enumeration<String> getExtensionPortNames();
	
	/**
	 * Get all the names of extension ports for a given LSIDPort implementation
	 * @param Class a specific implementation of LSIDPort for which we would like all the ports
	 * @return Enumeration an enum of strings, each string can be used as a key for <code>getExtensionPort()</code>
	 */
	public Enumeration<String> getExtensionPortNamesByClass(Class portClass);
	
	/**
	 * Get the given extension port
	 * @param String the key of the port, of the form "serviceName:portName"
	 * @return LSIDPort the port
	 */
	public LSIDPort getExtensionPort(String name);
	
	/**
	 * Set the authority location.  
	 * @param LSIDAuthorityPort the the location of the metadata to set
	 */
    void setAuthorityLocation(LSIDAuthorityPort lsidAuthorityPort) throws LSIDException;
	
    /**
	 * Set the location at which data may be retrieved
	 * @param LSIDDataPort the the location of the data to set
	 */
	void setDataLocation(LSIDDataPort lsidDataPort) throws LSIDException;
   
	/**
	 * Set the location at which metadata may be retrieved and queried
	 * @param LSIDMetadataPort the the location of the metadata to set
	 */
	void setMetadataLocation(LSIDMetadataPort lsidMetadataPort) throws LSIDException;
	
	/**
	 * Updates the string representation of the WSDL. This should be called if the WSDL Definition changes
	 */
	public Map<String, LSIDPort> getWSDLExtensionPorts();
	
	/**
	 * get the name of a key for a port
	 */
	public String getPortKey(LSIDPort port);

	/**
	 * Create a port for the given binding, protocol, hostname, port and name.
	 * In the case of some protocols, the hostname might be the entire endpoing, (for SOAP this is the case). 
	 * If the portname is null, then a default name for the given protocol is chosen.
	 */
	public Port createPort(Binding binding, LSIDStandardPort port);
}
