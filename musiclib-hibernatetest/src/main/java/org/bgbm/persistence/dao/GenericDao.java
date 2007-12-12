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


@Repository
public class GenericDao {
	static Logger logger = Logger.getLogger(GenericDao.class);

	@Autowired
	private SessionFactory factory;
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

}
