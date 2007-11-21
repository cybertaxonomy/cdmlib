/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 *
 */
public abstract class DaoBase<T extends CdmBase, ID extends Serializable> implements IDao<T, ID> {

	static Logger logger = Logger.getLogger(DaoBase.class);

	@Autowired
	private SessionFactory factory;
	protected Class<T> type;
	
	public DaoBase(Class<T> type){
		this.type = type;
		logger.debug("Creating DAO of type [" + type.getSimpleName() + "]");
	}
	
	protected Session getSession(){
		return factory.getCurrentSession();
	}
	
	
	public void saveOrUpdate(T transientObject) throws DataAccessException  {
		getSession().saveOrUpdate(transientObject);
	}

	public Serializable save(T newInstance) throws DataAccessException {
		return getSession().save(newInstance);
	}
	
	public void update(T transientObject) throws DataAccessException {
		getSession().update(transientObject);
	}
	
	public void delete(T persistentObject) throws DataAccessException {
		getSession().delete(persistentObject);
	}

	public T findById(Integer id) throws DataAccessException {
		return (T) getSession().get(type, id);
	}


	
	public Boolean exists(ID id) {
		if (findById(id)==null){
			return false;
		}
		return true;
	}


	public List<T> list(Integer limit) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDao#find(java.lang.String)
	 */
	public abstract List<T> find(String queryString);
}
