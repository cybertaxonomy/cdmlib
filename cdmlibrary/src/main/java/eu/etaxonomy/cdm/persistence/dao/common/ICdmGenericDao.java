package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.init.ICdmBaseSaver;

public interface ICdmGenericDao extends ICdmBaseSaver {

	public UUID saveOrUpdate(CdmBase transientObject) throws DataAccessException;
	
	public UUID save(CdmBase newOrManagedObject) throws DataAccessException;
	
	public UUID update(CdmBase transientObject) throws DataAccessException;
	
	public UUID delete(CdmBase persistentObject) throws DataAccessException;
	
}
