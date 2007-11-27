/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;


/**
 * @author a.mueller
 *
 */
public abstract class DaoBase<T extends VersionableEntity> implements IDao<T> {

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
	
	
	public String saveOrUpdate(T transientObject) throws DataAccessException  {
		getSession().saveOrUpdate(transientObject);
		return transientObject.getUuid();
	}

	public String save(T newInstance) throws DataAccessException {
		getSession().save(newInstance);
		return newInstance.getUuid();
	}
	
	public String update(T transientObject) throws DataAccessException {
		getSession().update(transientObject);
		return transientObject.getUuid();
	}
	
	public String delete(T persistentObject) throws DataAccessException {
		getSession().delete(persistentObject);
		return persistentObject.getUuid();
	}

	public T findById(int id) throws DataAccessException {
		return (T) getSession().get(type, id);
	}

	public T findByUuid(String Uuid) throws DataAccessException{
		//FIXME
		return null;
	}
	
	public Boolean exists(String uuid) {
		if (findByUuid(uuid)==null){
			return false;
		}
		return true;
	}

	public List<T> list(int limit, int start) {
		Session s = getSession();
		Criteria crit = s.createCriteria(type); 
		crit.setFirstResult(start);
		crit.setMaxResults(limit);
		List<T> entities = crit.list(); 
		return entities; 
	}

	public List<CdmBase> executeHsql(String hsql){
		Query q = getSession().createQuery(hsql);
		return q.list();
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDao#find(java.lang.String)
	 */
	public abstract List<T> find(String queryString);
	
}
