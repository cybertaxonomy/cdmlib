/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service.lsid;

import java.io.InputStream;


import com.ibm.lsid.LSIDException;

import eu.etaxonomy.cdm.model.common.LSID;
/**
 * A factory class which provides instances of LSIDWSDLWrapper
 * 
 * @author ben
 *
 */
public interface LSIDWSDLWrapperFactory {
	/**
	 * Get the authority wsdl for a given lsid
	 * @param LSID lsid
	 * @return LSIDWSDLWrapper the authority WSDL
	 */
	LSIDWSDLWrapper getLSIDWSDLWrapper(LSID lsid);
	/**
	 * Get the authority wsdl
	 * @param String wsdl the uri of the wsdl file
	 * @return LSIDWSDLWrapper the authority WSDL
	 */
	LSIDWSDLWrapper getLSIDWSDLWrapper(String wsdl) throws LSIDException;
	/**
	 * Get the authority wsdl
	 * @param InputStream wsdl The resource as an input stream 
	 * @return LSIDWSDLWrapper the authority WSDL
	 */
	LSIDWSDLWrapper getLSIDWSDLWrapper(InputStream wsdl) throws LSIDException;
}
