package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ICdmGenericDao {

	public String saveOrUpdate(CdmBase transientObject) throws DataAccessException;
	
	public String save(CdmBase newOrManagedObject) throws DataAccessException;
	
	public String update(CdmBase transientObject) throws DataAccessException;
	
	public String delete(CdmBase persistentObject) throws DataAccessException;
	
	public List<Object> executeHsql(String hsql) throws DataAccessException;

}
