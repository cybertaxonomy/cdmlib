/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service.lsid;

import com.ibm.lsid.ExpiringResponse;


import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDService;

import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
/**
 * LSIDAuthorityService interface which was altered from com.ibm.lsid.server.LSIDAuthorityService,
 * replacing the LSIDRequestContext with a simple LSID. My thinking behind this is that provided the 
 * LSIDAuthorityService has no responsibility for security, then there is no need to pass that
 * information to it (in an LSIDRequestContext). This allows better separation of concerns in that methods
 * requiring authentication and authorization can be secured transparently within CATE without the need for 
 * the LSIDAuthorityService to know anything about it. I could be wrong, of course.
 * 
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>)
 * @author ben
 * 
 * @see com.ibm.lsid.server.LSIDAuthorityService
 * @see com.ibm.lsid.server.LSIDRequestContext 
 */
public interface LSIDAuthorityService extends LSIDService {
	/**
	 * Get a WSDL document which describes the this authority
	 * @return ExpiringResponse contains a Source object containing the WSDL document
	 * @throws LSIDServerException
	 * 
	 * @see javax.xml.transform.Source
	 */
	public ExpiringResponse getAuthorityWSDL() throws LSIDServerException;
	
	/**
	 * Get the WSDL document that describes the methods that can be called on the given LSID
	 * @param LSID the LSID to query
	 * @return ExpiringResponse contains a Source object containing the WSDL document
	 * 
	 * @see javax.xml.transform.Source
	 */
	public ExpiringResponse getAvailableServices(LSID lsid) throws LSIDServerException;
	
	/**
	 * Add a known foreign authority to the metadata of an lsid
	 * @param lsid
	 * @param authorityName
	 * @throws LSIDServerException
	 */
	public void notifyForeignAuthority(LSID lsid, LSIDAuthority authorityName) throws LSIDServerException;
	
	
	/**
	 * Remove a foreign authority registration from a specific lsid
	 * @param lsid
	 * @param authorityName
	 * @throws LSIDServerException
	 */
	public void revokeNotificationForeignAuthority(LSID lsid, LSIDAuthority authorityName) throws LSIDServerException;
}
