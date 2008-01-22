package eu.etaxonomy.cdm.model.common.init;

import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;


public interface ICdmBaseSaver {
	
	public UUID saveOrUpdate(CdmBase transientObject) throws DataAccessException;
}
