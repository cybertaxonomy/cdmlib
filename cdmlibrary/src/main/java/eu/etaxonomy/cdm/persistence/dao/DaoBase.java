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
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Enumeration;


/**
 * @author a.mueller
 *
 */
@Component
public abstract class DaoBase<T, ID extends Serializable> implements IDao<T, ID> {

	static Logger logger = Logger.getLogger(DaoBase.class);

	@Autowired
	private SessionFactory factory;
	protected Class<T> type;
	
	public DaoBase(Class<T> type){
		this.type = type;
		logger.debug("Creating DAO of type [" + type.getSimpleName() + "]");
	}
	
	protected Session getSession(){
		Session s = factory.getCurrentSession();
		s.beginTransaction();
		return s;
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
		// TODO Auto-generated method stub
		return null;
	}


	
	public Boolean exists(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}


	public List<T> list(Integer limit) {
		// TODO Auto-generated method stub
		return null;
	}

	public abstract List<T> find(String queryString);
}
