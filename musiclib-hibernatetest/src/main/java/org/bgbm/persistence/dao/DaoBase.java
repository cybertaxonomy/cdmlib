/**
 * 
 */
package org.bgbm.persistence.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.bgbm.model.EntityBase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author a.mueller
 *
 */
public abstract class DaoBase<T extends EntityBase> implements IDao<T> {
	static Logger logger = Logger.getLogger(DaoBase.class);

	@Autowired
	public SessionFactory factory;
	protected Class<T> type;
	
	public DaoBase(Class<T> type){
		this.type = type;
		logger.debug("Creating DAO of type [" + type.getSimpleName() + "]");
	}
	
	protected Session getSession(){
		return factory.getCurrentSession();
	}
	
	public void save(T domainObj) throws DataAccessException  {
		getSession().saveOrUpdate(domainObj);
	}

	public void update(T domainObj) throws DataAccessException {
		getSession().update(domainObj);
	}
	
	public void delete(T domainObj) throws DataAccessException {
		getSession().delete(domainObj);
	}

	public T findById(Integer id) throws DataAccessException {
		T obj = (T) getSession().load(type, id);
		return obj;
	}

}
