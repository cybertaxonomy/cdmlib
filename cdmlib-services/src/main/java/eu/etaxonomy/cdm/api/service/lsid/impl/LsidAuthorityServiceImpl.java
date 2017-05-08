/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid.impl;

import java.util.Date;

import javax.wsdl.WSDLException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lsid.ExpiringResponse;
import com.ibm.lsid.LSIDException;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDServiceConfig;
import com.ibm.lsid.wsdl.HTTPLocation;
import com.ibm.lsid.wsdl.LSIDDataPort;
import com.ibm.lsid.wsdl.LSIDMetadataPort;

import eu.etaxonomy.cdm.api.service.lsid.LSIDAuthorityService;
import eu.etaxonomy.cdm.api.service.lsid.LSIDRegistry;
import eu.etaxonomy.cdm.api.service.lsid.LSIDWSDLWrapper;
import eu.etaxonomy.cdm.api.service.lsid.LSIDWSDLWrapperFactory;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
/**
 *
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>)
 * @author ben
 *
 */
@Service("lsidAuthorityService")
@Transactional
public class LsidAuthorityServiceImpl implements LSIDAuthorityService {
	@SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(LsidAuthorityServiceImpl.class);
	private String lsidDomain;
	private Integer lsidPort;
	private LSIDRegistry lsidRegistry;
	private LSIDWSDLWrapperFactory lsidWSDLWrapperFactory;

	public void setLsidDomain(String lsidDomain) {
		this.lsidDomain = lsidDomain;
	}

	public void setLsidPort(Integer lsidPort) {
		this.lsidPort = lsidPort;
	}

	@Autowired
	public void setLsidRegistry(LSIDRegistry lsidRegistry) {
		this.lsidRegistry = lsidRegistry;
	}

	@Autowired
	public void setLsidWSDLWrapperFactory(LSIDWSDLWrapperFactory lsidWSDLWrapperFactory) {
		this.lsidWSDLWrapperFactory = lsidWSDLWrapperFactory;
	}

	protected LSIDDataPort[] getDataLocations(LSID lsid) throws LSIDServerException {
	    IIdentifiableDao identfiableDAO = lsidRegistry.lookupDAO(lsid);
		if(identfiableDAO == null) { // we do not have a mapping for lsids with this authority or namespace
			throw new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
		}

		try {
			if(identfiableDAO.find(lsid) == null) { // we have a mapping for lsids with this authority and namespace, but no lsid stored
				throw new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
			}
		} catch (LSIDException e) {
			throw new LSIDServerException(e, e.getErrorCode(),e.getMessage());
		}
		return new LSIDDataPort[] {
				//new SOAPLocation("CATEDataSOAPPort", "http://"+ lsidDomain + ":" + lsidPort + "/authority/data/soap/"),
				new HTTPLocation("CATEDataHTTPPort", lsidDomain, lsidPort, "/authority/data.do") };
	}

	protected LSIDMetadataPort[] getMetadataLocations(LSID lsid) {
		return new LSIDMetadataPort[] {
				new HTTPLocation("CATEMetadataHTTPPort", lsidDomain, lsidPort, "/authority/metadata.do")
		};
	}

	@Override
    public void initService(LSIDServiceConfig arg0) throws LSIDServerException {

	}

	/**
	 * Get the expiration date/time of the available operations.  By default, returns null, indicating no expiration.
	 * Implementing classes should override this method if they want to include expiration information.
	 * @return Date the date/time at which the available operations will expire.
	 */
	protected Date getExpiration() {
		return null;
	}

	@Override
    public ExpiringResponse getAvailableServices(LSID lsid) throws LSIDServerException {
		try {
			LSIDWSDLWrapper wsdl = lsidWSDLWrapperFactory.getLSIDWSDLWrapper(lsid);

			for (LSIDDataPort lsidDataPort :  getDataLocations(lsid)) {
				wsdl.setDataLocation(lsidDataPort);
			}

		    for (LSIDMetadataPort lsidMetadataPort : getMetadataLocations(lsid)) {
		    	wsdl.setMetadataLocation(lsidMetadataPort);
			}


			return new ExpiringResponse(wsdl.getWSDLSource(), getExpiration());
		} catch (LSIDException e) {
			throw new LSIDServerException(e, e.getErrorCode(), "LSIDAuthorityServiceImpl Error in getAvailableOperations");
		} catch (WSDLException e) {
			throw new LSIDServerException(e, LSIDServerException.INTERNAL_PROCESSING_ERROR, "LSIDAuthorityServiceImpl Error in getAvailableOperations");
		}
	}

	@Override
    public void notifyForeignAuthority(LSID lsid, LSIDAuthority arg1) throws LSIDServerException {
		throw new LSIDServerException(LSIDServerException.METHOD_NOT_IMPLEMENTED, "FAN service not available");
	}

	@Override
    public void revokeNotificationForeignAuthority(LSID lsid, LSIDAuthority arg1) throws LSIDServerException {
		throw new LSIDServerException(LSIDServerException.METHOD_NOT_IMPLEMENTED, "FAN service not available");
	}

	@Override
    public ExpiringResponse getAuthorityWSDL() throws LSIDServerException {
		LSID lsid = null;
		LSIDWSDLWrapper wsdl = lsidWSDLWrapperFactory.getLSIDWSDLWrapper(lsid);

		try {
			wsdl.setAuthorityLocation(new HTTPLocation("AuthorityServiceHTTP", "HTTPPort", lsidDomain, lsidPort, null));
			//lsidWSDLWrapperFactory.setAuthorityLocation(new SOAPLocation("AuthorityServiceSOAP", "SOAPPort", "http://" + lsidDomain	+ ":" + lsidPort + "/authority/soap/"), wsdl);
			return new ExpiringResponse(wsdl.getWSDLSource(), getExpiration());
		} catch (LSIDException e) {
			throw new LSIDServerException(e, e.getErrorCode(), "LSIDAuthorityServiceImpl Error in getAuthorityWSDL");
		} catch (WSDLException e) {
			throw new LSIDServerException(e, LSIDServerException.INTERNAL_PROCESSING_ERROR, "LSIDAuthorityServiceImpl Error in getAuthorityWSDL");
		}


	}
}
