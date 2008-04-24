/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ICdmGenericDao {

	public UUID saveOrUpdate(CdmBase transientObject) throws DataAccessException;
	
	public UUID save(CdmBase newOrManagedObject) throws DataAccessException;
	
	public UUID update(CdmBase transientObject) throws DataAccessException;
	
	public UUID delete(CdmBase persistentObject) throws DataAccessException;
	
}
