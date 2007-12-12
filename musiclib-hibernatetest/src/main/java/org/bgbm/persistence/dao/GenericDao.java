package org.bgbm.persistence.dao;


import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.bgbm.model.EntityBase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public class GenericDao{
	static Logger logger = Logger.getLogger(GenericDao.class);
	@Autowired
	public SessionFactory factory;

	
	protected Session getSession(){
		return factory.getCurrentSession();
	}
	
	public void save(EntityBase domainObj) throws DataAccessException  {
		getSession().saveOrUpdate(domainObj);
	}

	public void update(EntityBase domainObj) throws DataAccessException {
		getSession().update(domainObj);
	}
	
	public void delete(EntityBase domainObj) throws DataAccessException {
		getSession().delete(domainObj);
	}

	public EntityBase findById(Integer id, Class type) throws DataAccessException {
		EntityBase obj = (EntityBase) getSession().load(type, id);
		return obj;
	}
}
