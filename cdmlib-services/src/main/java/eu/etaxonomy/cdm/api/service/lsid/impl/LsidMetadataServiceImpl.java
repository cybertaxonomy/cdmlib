/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDServiceConfig;

import eu.etaxonomy.cdm.api.service.lsid.LSIDMetadataService;
import eu.etaxonomy.cdm.api.service.lsid.LSIDRegistry;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

@Service("lsidMetadataService")
@Transactional
public class LsidMetadataServiceImpl  implements LSIDMetadataService {

	private LSIDRegistry lsidRegistry;
	
	@Autowired
	public void setLsidRegistry(LSIDRegistry lsidRegistry) {
		this.lsidRegistry = lsidRegistry;
	}	
		
	public IIdentifiableEntity getMetadata(LSID lsid) throws LSIDServerException {
		IIdentifiableDao identfiableDAO = lsidRegistry.lookupDAO(lsid);
		if(identfiableDAO == null) { // we do not have a mapping for lsids with this authority or namespace
			throw new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
		}
		    
		try {
			IIdentifiableEntity identifiable = identfiableDAO.find(lsid);
			if(identifiable == null) { // we have a mapping for lsids with this authority and namespace, but no lsid stored
				throw new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
			}
			
			return identifiable;
		} catch (LSIDException e) {
			throw new LSIDServerException(e, e.getErrorCode(),e.getMessage());
		}
	}

	public void initService(LSIDServiceConfig arg0) throws LSIDServerException {
		
	}

}
