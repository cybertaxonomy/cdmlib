/*******************************************************************************
 * Copyright (c) 2002,2003 IBM Corporation 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/cpl.php
 * 
  ******************************************************************************/
package eu.etaxonomy.cdm.api.service.lsid.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.client.LSIDResolver;
import com.ibm.lsid.wsdl.DefaultLSIDPort;
import com.ibm.lsid.wsdl.LSIDAuthorityPort;
import com.ibm.lsid.wsdl.LSIDDataPort;
import com.ibm.lsid.wsdl.LSIDMetadataPort;
import com.ibm.lsid.wsdl.LSIDPort;
import com.ibm.lsid.wsdl.LSIDPortFactory;
import com.ibm.lsid.wsdl.LSIDStandardPort;
import com.ibm.lsid.wsdl.WSDLConstants;
import com.ibm.wsdl.DefinitionImpl;
import com.ibm.wsdl.extensions.PopulatedExtensionRegistry;
import com.ibm.wsdl.extensions.file.FileBinding;
import com.ibm.wsdl.extensions.file.FileBindingSerializer;
import com.ibm.wsdl.extensions.file.FileLocation;
import com.ibm.wsdl.extensions.file.FileLocationImpl;
import com.ibm.wsdl.extensions.file.FileLocationSerializer;
import com.ibm.wsdl.extensions.file.FileOperation;
import com.ibm.wsdl.extensions.file.FileOperationSerializer;
import com.ibm.wsdl.extensions.file.FileOutput;
import com.ibm.wsdl.extensions.file.FileOutputSerializer;
import com.ibm.wsdl.extensions.ftp.FTPBinding;
import com.ibm.wsdl.extensions.ftp.FTPBindingSerializer;
import com.ibm.wsdl.extensions.ftp.FTPLocation;
import com.ibm.wsdl.extensions.ftp.FTPLocationImpl;
import com.ibm.wsdl.extensions.ftp.FTPLocationSerializer;
import com.ibm.wsdl.extensions.ftp.FTPOperation;
import com.ibm.wsdl.extensions.ftp.FTPOperationSerializer;
import com.ibm.wsdl.extensions.ftp.FTPOutput;
import com.ibm.wsdl.extensions.ftp.FTPOutputSerializer;
import com.ibm.wsdl.extensions.http.HTTPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.factory.WSDLFactoryImpl;
import com.ibm.wsdl.xml.WSDLWriterImpl;

import eu.etaxonomy.cdm.api.service.lsid.LSIDWSDLWrapper;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDWSDLLocator;

/**
 *
 * This class provides a simple API into the WSDL that an LSID resolves.  Most of its functionality is 
 * available internally only to LSIDResolver. This class is meant to be used publicly as a way to generate
 * a WSDL document.  Also, a few public methods exists for accessing the WSDL document in different formats. 
 * 
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>)
 */
public class LsidWsdlWrapperImpl implements LSIDWSDLWrapper,WSDLConstants {

	// WSDL representations
	private String wsdl = null;
	private Definition definition;

	// the metadata ports keyed by servicename:portname
	private Hashtable<String,LSIDMetadataPort> lsidMetadataPorts = new Hashtable<String,LSIDMetadataPort>();

	// the data ports keyed by servicename:portname
	private Hashtable<String,LSIDDataPort> lsidDataPorts = new Hashtable<String,LSIDDataPort>();

	// the authority ports keyed by servicename:portname
	private Hashtable<String,LSIDAuthorityPort> lsidAuthorityPorts = new Hashtable<String,LSIDAuthorityPort>();

	// the extension ports keyed by servicename:portname
	private Hashtable<String,LSIDPort> wsdlExtensionPorts = new Hashtable<String,LSIDPort>();

	// the expiration date/time of the WSDL
	private Date expiration;

	// maintain a counter of the number of ports and bindings that are added without give names so that we
	// can provide unique name for them.
	private int currPortNum = 1;
	private int currBindingNum = 1;

	/**
	 * Get the WSDL String
	 * @return String get the String representation of the WSDL
	 */
	public String getWSDL() {
		try {
			if (wsdl == null) {
				updateStringRepresentation();
			}
		} catch (LSIDException e) {
			e.printStackTrace();
			return null;
		}
		return wsdl;
	}

	/**
	 * Get the WSDL String
	 * @return String the String representation of the WSDL
	 */
	public String toString() {
		return getWSDL();
	}

	/**
	 * Get the Defintion built by wsdl4j. 
	 * @return javax.wsdl.Definition the WSDL Definition 
	 */
	public Definition getDefinition() {
		// the caller will most likely update the def, so we have invalidate the string rep
		wsdl = null;
		return definition;
	}

	/**
	 * Returns the expiration.
	 * @return Date null value 
	 */
	public Date getExpiration() {
		return expiration;
	}

	/**
	 * Sets the expiration.
	 * @param expiration The expiration to set
	 */
	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
	
	/**
	 * Get the names of the services.  All metadata ports in a given service must be the same.
	 * the client may assume that all the authorative metadata may be obtained by requesting
	 * one port from each service.
	 * @return Enumeration the service names
	 */
	public Enumeration<String> getServiceNames() {
		Map map = definition.getServices();
		Vector<String> names = new Vector<String>();
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			QName qname = (QName)it.next();
			names.add(qname.getLocalPart());	
		}
		return names.elements();	
	}

	/**
	 * Get the keys of all the metadata ports
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getMetadataPortNames() {
		return lsidMetadataPorts.keys();
	}
	
	/**
	 * Get the keys of all the metadata ports in the given service
	 * @param String the service
	 * @return LSIDMetadataPort the port
	 */
	public Enumeration<String> getMetadataPortNamesForService(String servicename) {
		Service service = definition.getService(new QName(definition.getTargetNamespace(),servicename));
		Map ports = service.getPorts();
		Vector<String> keys = new Vector<String>();
		Iterator it = ports.keySet().iterator();
		while (it.hasNext()) {
			String portname = (String)it.next();
			Port port = (Port)ports.get(portname);
			if (port.getBinding().getPortType().getQName().getLocalPart().equals(METADATA_PORT_TYPE))
				keys.add(servicename + ":" + portname);	
		}
		return keys.elements();
	}

	/**
	 * Get the keys of all the metadata ports for the given protocol
	 * @param String the protocol
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getMetadataPortNamesForProtocol(String protocol) {
		Vector<String> result = new Vector<String>();
		Enumeration<String> portNames = lsidMetadataPorts.keys();
		while (portNames.hasMoreElements()) {
			String portName = portNames.nextElement();
			LSIDMetadataPort lmdp = lsidMetadataPorts.get(portName);
			String prot = lmdp.getProtocol();
			if (prot == null)
				continue;
			if (prot.equals(protocol))
				result.add(portName);
		}
		return result.elements();
	}

	/**
	 * Get an arbitrary metadata port if one exists. 
	 * @return LSIDMetadataPort a metadata port if one exits, null otherwise. Uses protocol preference order: HTTP, FTP, ANY
	 */
	public LSIDMetadataPort getMetadataPort() {
		LSIDMetadataPort port = getMetadataPortForProtocol(HTTP);
		if (port != null)
			return port;
		port = getMetadataPortForProtocol(FTP);
		if (port != null)
			return port;
		if (!lsidMetadataPorts.keys().hasMoreElements())
			return null;
		return lsidMetadataPorts.get(lsidMetadataPorts.keys().nextElement());
	}

	/**
	 * Get the metadata port with the given key
	 * @param String the key of the port, of the form "serviceName:portName"
	 * @return LSIDMetadataPort, the metadata port
	 */
	public LSIDMetadataPort getMetadataPort(String name) {
		return lsidMetadataPorts.get(name);
	}

	/**
	 * Get an arbitray metadata port for the given protocol
	 * @param String the protocol
	 * @return LSIDMetadataPort, the metadata port if one exists, null otherwise.
	 */
	public LSIDMetadataPort getMetadataPortForProtocol(String protocol) {
		Enumeration portNames = lsidMetadataPorts.keys();
		while (portNames.hasMoreElements()) {
			LSIDMetadataPort lmdp = lsidMetadataPorts.get(portNames.nextElement());
			String prot = lmdp.getProtocol();
			if (prot == null)
				continue;
			if (prot.equals(protocol))
				return lmdp;
		}
		return null;
	}

	/**
	 * Get the keys of all the metadata ports
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getDataPortNames() {
		return lsidDataPorts.keys();
	}

	/**
	 * Get the keys of all the ports for the given protocol
	 * @param String the protocol
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getDataPortNamesForProtocol(String protocol) {
		Vector<String> result = new Vector<String>();
		Enumeration portNames = lsidDataPorts.keys();
		while (portNames.hasMoreElements()) {
			String portName = (String) portNames.nextElement();
			LSIDDataPort ldp = lsidDataPorts.get(portName);
			if (ldp.getProtocol().equals(protocol))
				result.add(portName);
		}
		return result.elements();
	}

	/**
	 * Get an arbitrary data port if one exists. 
	 * @return LSIDDataPort a data port if one exits, null otherwise. Uses protocol preference order: HTTP, FTP, ANY
	 */
	public LSIDDataPort getDataPort() {
		LSIDDataPort port = getDataPortForProtocol(HTTP);
		if (port != null)
			return port;
		port = getDataPortForProtocol(FTP);
		if (port != null)
			return port;
		if (!lsidDataPorts.keys().hasMoreElements())
			return null;
		return lsidDataPorts.get(lsidDataPorts.keys().nextElement());
	}

	/**
	 * Get the data port with the given key
	 * @param String the key of the port, of the form "serviceName:portName"
	 * @return LSIDDataPort, the data port
	 */
	public LSIDDataPort getDataPort(String name) {
		return (LSIDDataPort) lsidDataPorts.get(name);
	}

	/**
	 * Get an arbitray data port for the given protocol
	 * @param String the protocol
	 * @return LSIDDataPort, the data port if one exists, null otherwise.
	 */
	public LSIDDataPort getDataPortForProtocol(String protocol) {
		Enumeration portNames = lsidDataPorts.keys();
		while (portNames.hasMoreElements()) {
			LSIDDataPort ldp =  lsidDataPorts.get(portNames.nextElement());
			if (ldp.getProtocol().equals(protocol))
				return ldp;
		}
		return null;
	}

	/**
	 * Get the keys of all the authority ports
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getAuthorityPortNames() {
		return lsidAuthorityPorts.keys();
	}

	/**
	 * Get the keys of all the authority ports for the given protocol
	 * @param String the protocol
	 * @return Enumeration an Enumeration of Strings of the form "serviceName:portName"
	 */
	public Enumeration<String> getAuthorityPortNamesForProtocol(String protocol) {
		Vector<String> result = new Vector<String>();
		Enumeration portNames = lsidAuthorityPorts.keys();
		while (portNames.hasMoreElements()) {
			String portName = (String) portNames.nextElement();
			LSIDAuthorityPort lap = lsidAuthorityPorts.get(portName);
			String prot = lap.getProtocol();
			if (prot == null)
				continue;
			if (prot.equals(protocol))
				result.add(portName);
		}
		return result.elements();
	}

	/**
	 * Get an arbitrary authority port if one exists. 
	 * @return LSIDAuthorityPort an authority port if one exits, null otherwise. Uses protocol preference order: HTTP, SOAP
	 */
	public LSIDAuthorityPort getAuthorityPort() {
		LSIDAuthorityPort port = getAuthorityPortForProtocol(HTTP);
		if (port != null)
			return port;
		port = getAuthorityPortForProtocol(SOAP);
		if (port != null)
			return port;
		if (!lsidAuthorityPorts.keys().hasMoreElements())
			return null;
		return lsidAuthorityPorts.get(lsidAuthorityPorts.keys().nextElement());
	}

	/**
	 * Get the authority port with the given key
	 * @param String the key of the port, of the form "serviceName:portName"
	 * @return LSIDAuthorityPort, the authority port
	 */
	public LSIDAuthorityPort getAuthorityPort(String name) {
		return (LSIDAuthorityPort) lsidAuthorityPorts.get(name);
	}

	/**
	 * Get an arbitray authority port for the given protocol
	 * @param String the protocol
	 * @return LSIDAuthorityPort, the authority port if one exists, null otherwise.
	 */
	public LSIDAuthorityPort getAuthorityPortForProtocol(String protocol) {
		Enumeration<String> portNames = lsidAuthorityPorts.keys();
		while (portNames.hasMoreElements()) {
			LSIDAuthorityPort lap = lsidAuthorityPorts.get(portNames.nextElement());
			String prot = lap.getProtocol();
			if (prot == null)
				continue;
			if (prot.equals(protocol))
				return lap;
		}
		return null;
	}

	/**
	 * Get all the names of extension port names
	 * @return Enumeration an enum of strings, each string can be used as a key for <code>getExtensionPort()</code>
	 */
	public Enumeration<String> getExtensionPortNames() {
		return wsdlExtensionPorts.keys();
	}

	/**
	 * Get all the names of extension ports for a given LSIDPort implementation
	 * @param Class a specific implementation of LSIDPort for which we would like all the ports
	 * @return Enumeration an enum of strings, each string can be used as a key for <code>getExtensionPort()</code>
	 */
	public Enumeration<String> getExtensionPortNamesByClass(Class portClass) {
		Enumeration<String> keys = wsdlExtensionPorts.keys();
		Vector<String> ret = new Vector<String>();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			LSIDPort port = wsdlExtensionPorts.get(key);
			if (port.getClass().equals(portClass))
				ret.add(key);
		}
		return ret.elements();
	}

	/**
	 * Get the given extension port
	 * @param String the key of the port, of the form "serviceName:portName"
	 * @return LSIDPort the port
	 */
	public LSIDPort getExtensionPort(String name) {
		return wsdlExtensionPorts.get(name);
	}

	/**
	 * Updates the string representation of the WSDL. This should be called if the WSDL Definition changes
	 */
	private void updateStringRepresentation() throws LSIDException {
		StringWriter strWriter = new StringWriter();
		WSDLWriter writer = new WSDLWriterImpl();
		try {
			writer.writeWSDL(definition, strWriter);
		} catch (WSDLException e) {
			throw new LSIDException(e, "Error writing WSDL def to string");
		}
		wsdl = strWriter.getBuffer().toString();
	}

	
	/**
	 * Create a port for the given binding, protocol, hostname, port and name.
	 * In the case of some protocols, the hostname might be the entire endpoing, (for SOAP this is the case). 
	 * If the portname is null, then a default name for the given protocol is chosen.
	 */
	public Port createPort(Binding binding, LSIDStandardPort port) {
		Port newPort = definition.createPort();
		newPort.setBinding(binding);
		String portName = port.getName();
		String protocol = port.getProtocol();
		if (portName == null)
			portName = newPortName(protocol);
		newPort.setName(portName);
		if (protocol.equals(HTTP)) {
			HTTPAddress addr = new HTTPAddressImpl();
			addr.setLocationURI(port.getLocation());
			newPort.addExtensibilityElement(addr);
		} else if (protocol.equals(FTP)) {
			FTPLocation loc = new FTPLocationImpl(port.getLocation(), port.getPath());
			newPort.addExtensibilityElement(loc);
		} else if (protocol.equals(FILE)) {
			FileLocation loc = new FileLocationImpl(port.getLocation());
			newPort.addExtensibilityElement(loc);
		} else if (protocol.equals(SOAP)) {
			SOAPAddress addr = new SOAPAddressImpl();
			addr.setLocationURI(port.getLocation());
			newPort.addExtensibilityElement(addr);
		}
		return newPort;
	}

	/**
	 * Generate a unique port name using the counter and protocol
	 */
	private synchronized String newPortName(String protocol) {
		return protocol + "Port" + String.valueOf(currPortNum++);
	}

	/**
	 * create the string for the target namespace of a WSDL doc with the given LSID
	 */
	public static String getTargetNamespace(LSID lsid) {
		if (lsid == null)
			return OMG_LSID_PORT_TYPES_WSDL_NS_URI;
		return "http://" + lsid.getAuthority() + "/" + "availableServices?" + lsid.toString();
	}

	/**
	 * get the name of a key for a port
	 */
	public String getPortKey(LSIDPort port) {
		return (port.getServiceName() != null ? port.getServiceName() : SERVICE_NAME) + ":" + port.getName();
	}	

	public Map<String,LSIDPort> getWSDLExtensionPorts() {
		return wsdlExtensionPorts;
	}

	public Source getWSDLSource() throws WSDLException {
		WSDLFactory wsdlFactory = WSDLFactory.newInstance();
	    WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
	    Document document = wsdlWriter.getDocument(this.getDefinition());
		return new DOMSource(document);
	}

	public void setAuthorityLocation(LSIDAuthorityPort authorityPort) throws LSIDException {
		String serviceName = authorityPort.getServiceName();
		if (serviceName == null)
			serviceName = SERVICE_NAME;
		Map services = definition.getServices();
		QName key = new QName(definition.getTargetNamespace(), serviceName);
		Service service = (Service) services.get(key);
		if (service == null) {
			service = definition.createService();
			service.setQName(key);
			definition.addService(service);
		}

		// remove data port if it already exists.
		String portName = authorityPort.getName();
		if (portName != null) {
			Port port = service.getPort(portName);
			if (port != null) {
				definition.removeBinding(port.getBinding().getQName());
				service.getPorts().remove(portName);
			}
			lsidAuthorityPorts.remove(getPortKey(authorityPort));
		}

		String protocol = authorityPort.getProtocol();

		// we have to create a new port and possibly a binding
		// make sure we have the namespaces set in the defintion
		configureAuthorityServiceDef(protocol);

		PortType authorityPortType = definition.getPortType(new QName(OMG_LSID_PORT_TYPES_WSDL_NS_URI, AUTHORITY_PORT_TYPE));

		Binding binding = null;
		if (protocol.equals(SOAP))
			binding = definition.getBinding(AUTHORITY_SOAP_BINDING);
		else if (protocol.equals(HTTP))
			binding = definition.getBinding(AUTHORITY_HTTP_BINDING);
		if (binding == null)
			throw new LSIDException("Unsuported protocol for authority port: " + protocol);
		Port authport = createPort(binding, authorityPort);

		service.addPort(authport);
		lsidAuthorityPorts.put(getPortKey(authorityPort), authorityPort);
		// indicate that the WSDL has changed, so the string rep is no longer valid...
		wsdl = null;
	}

	public void setDataLocation(LSIDDataPort dataPort) throws LSIDException {
		String serviceName = dataPort.getServiceName();
		if (serviceName == null)
			serviceName = SERVICE_NAME;
		Map services = definition.getServices();
		QName key = new QName(definition.getTargetNamespace(), serviceName);
		Service service = (Service) services.get(key);
		if (service == null) {
			service = definition.createService();
			service.setQName(key);
			definition.addService(service);
		}

		// remove data port if it already exists.
		String portName = dataPort.getName();
		if (portName != null) {
			Port port = service.getPort(portName);
			if (port != null) {
				definition.removeBinding(port.getBinding().getQName());
				service.getPorts().remove(portName);
			}
			lsidDataPorts.remove(getPortKey(dataPort));
		}

		String protocol = dataPort.getProtocol();

		// we have to create a new port and possibly a binding
		// make sure we have the namespaces set in the defintion
		configureDataServiceDef(dataPort.getProtocol());

		PortType dataPortType = definition.getPortType(new QName(OMG_LSID_PORT_TYPES_WSDL_NS_URI, DATA_PORT_TYPE));

		Binding binding = null;
		if (protocol.equals(SOAP))
			binding = definition.getBinding(DATA_SOAP_BINDING);
		else if (protocol.equals(HTTP)) {
			if (dataPort.getPath().equals(LSIDDataPort.PATH_TYPE_URL_ENCODED))
				binding = definition.getBinding(DATA_HTTP_BINDING);
			else
				binding = definition.getBinding(DATA_HTTP_BINDING_DIRECT);
		} else if (protocol.equals(FTP))
			binding = definition.getBinding(DATA_FTP_BINDING);
		else if (protocol.equals(FILE))
			binding = definition.getBinding(DATA_FILE_BINDING);
		Port port = createPort(binding, dataPort);
		service.addPort(port);
		lsidDataPorts.put(getPortKey(dataPort), dataPort);

		// indicate that the WSDL has changed, so the string rep is no longer valid...
		wsdl = null;
	}

	public void setMetadataLocation(LSIDMetadataPort metadataPort) throws LSIDException {
		String serviceName = metadataPort.getServiceName();
		if (serviceName == null)
			serviceName = SERVICE_NAME;
		Map services = definition.getServices();
		QName key = new QName(definition.getTargetNamespace(), serviceName);
		Service service = (Service) services.get(key);
		if (service == null) {
			service = definition.createService();
			service.setQName(key);
			definition.addService(service);
		}

		// remove data port if it already exists.
		String portName = metadataPort.getName();
		if (portName != null) {
			Port port = service.getPort(portName);
			if (port != null) {
				definition.removeBinding(port.getBinding().getQName());
				service.getPorts().remove(portName);
			}
			lsidMetadataPorts.remove(getPortKey(metadataPort));
		}

		String protocol = metadataPort.getProtocol();

		// we have to create a new port and possibly a binding
		// make sure we have the namespaces set in the defintion
		configureDataServiceDef(metadataPort.getProtocol());

		PortType metadataPortType = definition.getPortType(new QName(OMG_LSID_PORT_TYPES_WSDL_NS_URI, METADATA_PORT_TYPE));

		Binding binding = null;
		if (protocol.equals(SOAP))
			binding = definition.getBinding(METADATA_SOAP_BINDING);
		else if (protocol.equals(HTTP)) {
			if (metadataPort.getPath().equals(LSIDMetadataPort.PATH_TYPE_URL_ENCODED))
				binding = definition.getBinding(METADATA_HTTP_BINDING);
			else
				binding = definition.getBinding(METADATA_HTTP_BINDING_DIRECT);
		} else if (protocol.equals(FTP))
			binding = definition.getBinding(METADATA_FTP_BINDING);
		else if (protocol.equals(FILE))
			binding = definition.getBinding(METADATA_FILE_BINDING);
		Port port = createPort(binding, metadataPort);
		service.addPort(port);
		lsidMetadataPorts.put(getPortKey(metadataPort), metadataPort);

		// indicate that the WSDL has changed, so the string rep is no longer valid...
		wsdl = null;
		
	}
	
	public void configureAuthorityServiceDef(String protocol) throws LSIDException {
		if (protocol.equals(HTTP)) {
			if (!definition.getNamespaces().containsValue(OMG_AUTHORITY_HTTP_BINDINGS_WSDL_NS_URI)) {
				importNamespace(AHB, OMG_AUTHORITY_HTTP_BINDINGS_WSDL_NS_URI, AUTHORITY_HTTP_BINDINGS_LOCATION);
			}
			if (!definition.getNamespaces().containsValue(HTTP_NS_URI))
				definition.addNamespace(HTTP, HTTP_NS_URI);

		} else if (protocol.equals(SOAP)) {
			if (!definition.getNamespaces().containsValue(OMG_AUTHORITY_SOAP_BINDINGS_WSDL_NS_URI)) {
				importNamespace(ASB, OMG_AUTHORITY_SOAP_BINDINGS_WSDL_NS_URI, AUTHORITY_SOAP_BINDINGS_LOCATION);
			}
			if (!definition.getNamespaces().containsValue(SOAP_NS_URI))
				definition.addNamespace(SOAP, SOAP_NS_URI);
		}
		
	}

	public void configureDataServiceDef(String protocol) throws LSIDException {
		if (protocol.equals(HTTP)) {
			if (!definition.getNamespaces().containsValue(OMG_DATA_HTTP_BINDINGS_WSDL_NS_URI)) {
				importNamespace(DHB, OMG_DATA_HTTP_BINDINGS_WSDL_NS_URI, DATA_HTTP_BINDINGS_LOCATION);
			}
			if (!definition.getNamespaces().containsValue(HTTP_NS_URI))
				definition.addNamespace(HTTP, HTTP_NS_URI);

		} else if (protocol.equals(FTP)) { // ftp
			if (!definition.getNamespaces().containsValue(OMG_DATA_FTP_BINDINGS_WSDL_NS_URI)) {
				importNamespace(DFB, OMG_DATA_FTP_BINDINGS_WSDL_NS_URI, DATA_FTP_BINDINGS_LOCATION);
			}
			if (!definition.getNamespaces().containsValue(FTP_NS_URI)) {
				definition.addNamespace(FTP, FTP_NS_URI);
				ExtensionRegistry reg = definition.getExtensionRegistry();
				LsidWsdlWrapperImpl.registerFTP(reg);
			}

		} else if (protocol.equals(FILE)) { // ftp
			if (!definition.getNamespaces().containsValue(OMG_DATA_FILE_BINDINGS_WSDL_NS_URI)) {
				importNamespace(DFB, OMG_DATA_FILE_BINDINGS_WSDL_NS_URI, DATA_FILE_BINDINGS_LOCATION);
			}
			if (!definition.getNamespaces().containsValue(FILE_NS_URI)) {
				definition.addNamespace(FILE, FILE_NS_URI);
				ExtensionRegistry reg = definition.getExtensionRegistry();
				LsidWsdlWrapperImpl.registerFile(reg);
			}
		} else if (protocol.equals(SOAP)) {
			if (!definition.getNamespaces().containsValue(OMG_DATA_SOAP_BINDINGS_WSDL_NS_URI)) {
				importNamespace(DSB, OMG_DATA_SOAP_BINDINGS_WSDL_NS_URI, DATA_SOAP_BINDINGS_LOCATION);
			}
			if (!definition.getNamespaces().containsValue(SOAP_NS_URI))
				definition.addNamespace(SOAP, SOAP_NS_URI);
		}
		
	}
	
	private String baseURI;
	
	/* (non-Javadoc)
	 * @see org.cateproject.controller.lsid.LSIDWSDLWrapperFactory#getLSIDWSDLWrapper(com.ibm.lsid.LSID)
	 */
	public LsidWsdlWrapperImpl(LSID lsid, String baseURI) {
		this.baseURI = baseURI;
		definition = new DefinitionImpl();
		definition.setExtensionRegistry(new PopulatedExtensionRegistry());
	    String tns = LsidWsdlWrapperImpl.getTargetNamespace(lsid);
	    definition.setTargetNamespace(tns);
	    definition.addNamespace(TNS, tns);
	}
	
	/* (non-Javadoc)
	 * @see org.cateproject.controller.lsid.LSIDWSDLWrapperFactory#getLSIDWSDLWrapper(java.lang.String)
	 */
	public LsidWsdlWrapperImpl(String wsdl, String baseURI) throws LSIDException {
		this.wsdl = wsdl;
		this.baseURI = baseURI;
		try {
			WSDLReader wsdlReader = WSDLFactoryImpl.newInstance().newWSDLReader();
			// load the ftp extension into the reader's registry
			wsdlReader.setExtensionRegistry(new PopulatedExtensionRegistry());
			ExtensionRegistry reg = wsdlReader.getExtensionRegistry();
			LsidWsdlWrapperImpl.registerFTP(reg);
			LsidWsdlWrapperImpl.registerFile(reg);
			// parse the WSDL
			String resource = baseURI + wsdl;
			Reader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(resource));
			WSDLLocator locator = new LSIDWSDLLocator(baseURI,reader,Thread.currentThread().getContextClassLoader());
			definition = wsdlReader.readWSDL(locator);
		} catch (WSDLException e) {
			throw new LSIDException(e, "Error reading wsdl file into wsdl4j");
		}
		extractPorts();
	}
	
	/* (non-Javadoc)
	 * @see org.cateproject.controller.lsid.LSIDWSDLWrapperFactory#getLSIDWSDLWrapper(java.io.InputStream)
	 */
	public LsidWsdlWrapperImpl(InputStream wsdl, String baseURI) throws LSIDException {
		this.baseURI = baseURI;
		try {
			WSDLReader wsdlReader = WSDLFactoryImpl.newInstance().newWSDLReader();
			// load the ftp extension into the reader's registry
			wsdlReader.setExtensionRegistry(new PopulatedExtensionRegistry());
			ExtensionRegistry reg = wsdlReader.getExtensionRegistry();
			LsidWsdlWrapperImpl.registerFTP(reg);
			LsidWsdlWrapperImpl.registerFile(reg);
			// parse the WSDL
			Reader reader = new InputStreamReader(wsdl);
			WSDLLocator locator = new LSIDWSDLLocator(baseURI,reader,Thread.currentThread().getContextClassLoader());
			definition = wsdlReader.readWSDL(locator);
		} catch (WSDLException e) {
			throw new LSIDException(e, "Error reading wsdl file into wsdl4j");
		}
		extractPorts();
	}

	
	
	/**
	 * import the given namespace into the def.
	 */
	private void importNamespace(String prefix, String ns, String location) throws LSIDException {
		Definition importDef = null;
		try {
			WSDLReader wsdlReader = WSDLFactoryImpl.newInstance().newWSDLReader();;
			String resource = baseURI + location;
			Reader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(resource));
			WSDLLocator locator = new LSIDWSDLLocator(baseURI,reader,Thread.currentThread().getContextClassLoader());
			importDef = wsdlReader.readWSDL(locator);
		} catch (WSDLException e) {
			throw new LSIDException(e, "Error importing namespace: " + ns);
		} 
		definition.addNamespace(prefix, ns);
		Import imp = definition.createImport();
		imp.setLocationURI(location);
		imp.setNamespaceURI(ns);
		imp.setDefinition(importDef);
		definition.addImport(imp);
	}
	
	/**
	 * add the FILE extension to the given registry
	 */
	private static void registerFile(ExtensionRegistry reg) {
		reg.registerDeserializer(Port.class, FileLocation.DEFAULT_ELEMENT_TYPE, new FileLocationSerializer());
		reg.registerDeserializer(BindingOutput.class, FileOutput.DEFAULT_ELEMENT_TYPE, new FileOutputSerializer());
		reg.registerDeserializer(BindingOperation.class, FileOperation.DEFAULT_ELEMENT_TYPE, new FileOperationSerializer());
		reg.registerDeserializer(Binding.class, FileBinding.DEFAULT_ELEMENT_TYPE, new FileBindingSerializer());
		reg.registerSerializer(Port.class, FileLocation.DEFAULT_ELEMENT_TYPE, new FileLocationSerializer());
		reg.registerSerializer(BindingOutput.class, FileOutput.DEFAULT_ELEMENT_TYPE, new FileOutputSerializer());
		reg.registerSerializer(BindingOperation.class, FileOperation.DEFAULT_ELEMENT_TYPE, new FileOperationSerializer());
		reg.registerSerializer(Binding.class, FileBinding.DEFAULT_ELEMENT_TYPE, new FileBindingSerializer());
	}
	
	/**
	 * add the FTP extension to the given registry
	 */
	private static void registerFTP(ExtensionRegistry reg) {
		reg.registerDeserializer(Port.class, FTPLocation.DEFAULT_ELEMENT_TYPE, new FTPLocationSerializer());
		reg.registerDeserializer(BindingOutput.class, FTPOutput.DEFAULT_ELEMENT_TYPE, new FTPOutputSerializer());
		reg.registerDeserializer(BindingOperation.class, FTPOperation.DEFAULT_ELEMENT_TYPE, new FTPOperationSerializer());
		reg.registerDeserializer(Binding.class, FTPBinding.DEFAULT_ELEMENT_TYPE, new FTPBindingSerializer());
		reg.registerSerializer(Port.class, FTPLocation.DEFAULT_ELEMENT_TYPE, new FTPLocationSerializer());
		reg.registerSerializer(BindingOutput.class, FTPOutput.DEFAULT_ELEMENT_TYPE, new FTPOutputSerializer());
		reg.registerSerializer(BindingOperation.class, FTPOperation.DEFAULT_ELEMENT_TYPE, new FTPOperationSerializer());
		reg.registerSerializer(Binding.class, FTPBinding.DEFAULT_ELEMENT_TYPE, new FTPBindingSerializer());
	}
	
	/**
	 * Extract the port info for data and meta data
	 */
	private void extractPorts() throws LSIDException {
		Map services = definition.getServices();
		Object[] serviceKeys = services.keySet().toArray();
		for (int j = 0; j < serviceKeys.length; j++) {
			Service service = (Service) services.get(serviceKeys[j]);
			Map ports = service.getPorts();
			Object[] portKeys = ports.keySet().toArray();
			// go through the ports, meta data and data
			for (int i = 0; i < portKeys.length; i++) {
				Port port = (Port) ports.get(portKeys[i]);
				Binding binding = port.getBinding();
				PortType portType = binding.getPortType();
				QName qname = portType.getQName();
				if (qname.getLocalPart().equals(METADATA_PORT_TYPE) && qname.getNamespaceURI().equals(OMG_LSID_PORT_TYPES_WSDL_NS_URI)) {
					if (!binding.getQName().equals(METADATA_SOAP_BINDING) && !binding.getQName().equals(METADATA_HTTP_BINDING) && !binding.getQName().equals(METADATA_FTP_BINDING) && !binding.getQName().equals(METADATA_FILE_BINDING))
						throw new LSIDException(LSIDException.UNKNOWN_METHOD, "Unrecognized metadata binding: " + binding.getQName());
					LsidStandardPortImpl portImpl = extractPort(service, port, qname);
					lsidMetadataPorts.put(portImpl.getKey(), portImpl);
				} else if (qname.getLocalPart().equals(DATA_PORT_TYPE) && qname.getNamespaceURI().equals(OMG_LSID_PORT_TYPES_WSDL_NS_URI)) {
					if (!binding.getQName().equals(DATA_SOAP_BINDING) && !binding.getQName().equals(DATA_HTTP_BINDING) && !binding.getQName().equals(DATA_FTP_BINDING) && !binding.getQName().equals(DATA_FILE_BINDING))
						throw new LSIDException(LSIDException.UNKNOWN_METHOD, "Unrecognized data binding: " + binding.getQName());
					LsidStandardPortImpl portImpl = extractPort(service, port, qname);
					lsidDataPorts.put(portImpl.getKey(), portImpl);
				} else if (qname.getLocalPart().equals(AUTHORITY_PORT_TYPE) && qname.getNamespaceURI().equals(OMG_LSID_PORT_TYPES_WSDL_NS_URI)) {
					if (!binding.getQName().equals(AUTHORITY_SOAP_BINDING) && !binding.getQName().equals(AUTHORITY_HTTP_BINDING))
						throw new LSIDException(LSIDException.UNKNOWN_METHOD, "Unrecognized authority binding: " + binding.getQName());
					LsidStandardPortImpl portImpl = extractPort(service, port, qname);
					lsidAuthorityPorts.put(portImpl.getKey(), portImpl);
				} else {
					LSIDPortFactory lpf = LSIDResolver.getConfig().getLSIDPortFactory(portType);
					LSIDPort newPort = null;
					if (lpf == null) {
						newPort = new DefaultLSIDPort(service.getQName().getLocalPart(), port.getName(), port);
					} else {
						newPort = lpf.createPort(service.getQName().getLocalPart(), port); // might have to use whole qname, not sure for now
					}
					wsdlExtensionPorts.put(getPortKey(newPort), newPort);
				}
			}
		}
	}

	private static LsidStandardPortImpl extractPort(Service service, Port port, QName portTypeName) throws LSIDException {
		String portName = port.getName();
		LsidStandardPortImpl portImpl = new LsidStandardPortImpl();
		portImpl.setName(portName);
		portImpl.setServiceName(service.getQName().getLocalPart()); // might have to use whole QName, not sure for now
		Iterator portElts = port.getExtensibilityElements().listIterator();
		while (portElts.hasNext()) { // expecting only 1
			Object portElt = portElts.next();
			if (portElt instanceof SOAPAddress) {
				SOAPAddress soapaddr = (SOAPAddress) portElt;
				portImpl.setLocation(soapaddr.getLocationURI());
				portImpl.setProtocol(SOAP);

			} else if (portElt instanceof HTTPAddress) {
				HTTPAddress httpaddr = (HTTPAddress) portElt;
				portImpl.setLocation(httpaddr.getLocationURI());
				portImpl.setProtocol(HTTP);
				QName bindingName = port.getBinding().getQName();
				if (bindingName.equals(DATA_HTTP_BINDING_DIRECT))
					portImpl.setPath(LSIDStandardPort.PATH_TYPE_DIRECT);
				else
					portImpl.setPath(LSIDStandardPort.PATH_TYPE_URL_ENCODED);
			} else if (portElt instanceof FTPLocation) {
				FTPLocation ftploc = (FTPLocation) portElt;
				portImpl.setLocation(ftploc.getServer());
				portImpl.setPath(ftploc.getFilePath());
				portImpl.setProtocol(FTP);
			} else if (portElt instanceof FileLocation) {
				FileLocation fileloc = (FileLocation) portElt;
				portImpl.setLocation(fileloc.getFilename());
				portImpl.setProtocol(FILE);
			} else {
				throw new LSIDException("Unknown Port impl");
			}
		}
		return portImpl;
	}
}
