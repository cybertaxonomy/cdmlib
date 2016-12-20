/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDServiceConfig;

import eu.etaxonomy.cdm.api.service.lsid.LSIDDataService;
import eu.etaxonomy.cdm.api.service.lsid.LSIDRegistry;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

@Service("lsidDataService")
@Transactional
public class LsidDataServiceImpl implements LSIDDataService {
	private static final Logger logger = Logger.getLogger(LsidDataServiceImpl.class);

	private LSIDRegistry lsidRegistry;
	
	@Autowired
	public void setLsidRegistry(LSIDRegistry lsidRegistry) {
		this.lsidRegistry = lsidRegistry;
	}

	public InputStream getData(LSID lsid) throws LSIDServerException {
		IIdentifiableDao<?> identfiableDAO = lsidRegistry.lookupDAO(lsid);
		if(identfiableDAO == null) { // we do not have a mapping for lsids with this authority or namespace
			throw new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
		}
		    
		try {
			IIdentifiableEntity i = identfiableDAO.find(lsid);
			if(i == null) { // we have a mapping for lsids with this authority and namespace, but no lsid stored
				throw new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
			}
			
			if(i.getData() != null) {
    			return new ByteArrayInputStream(i.getData());
			} else {
				return null;
			}
		} catch (LSIDException e) {
			throw new LSIDServerException(e, e.getErrorCode(),e.getMessage());
		}
	}

	public InputStream getDataByRange(LSID lsid, Integer start, Integer length)
			throws LSIDServerException {
		IIdentifiableDao<?> identfiableDAO = lsidRegistry.lookupDAO(lsid);
		if(identfiableDAO == null) { // we do not have a mapping for lsids with this authority or namespace
			throw new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
		}
		    
		try {
			IIdentifiableEntity i = identfiableDAO.find(lsid);
			if(i == null) { // we have a mapping for lsids with this authority and namespace, but no lsid stored
				throw new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
			}
			if(i.getData() != null) {
    			byte[] subset = new byte[length];
	     		System.arraycopy(i.getData(), start, subset,0, length);
		    	return new ByteArrayInputStream(subset);
			} else {
				return null;
			}
		} catch (LSIDException e) {
			throw new LSIDServerException(e, e.getErrorCode(),e.getMessage());
		}
	}

	public void initService(LSIDServiceConfig arg0) throws LSIDServerException {
		//TODO
		logger.warn("initService(LSIDServiceConfig) not yet implemented");
	}

}
