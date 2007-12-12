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
public class GenericDao extends DaoBase<EntityBase> {
	static Logger logger = Logger.getLogger(GenericDao.class);

	public GenericDao() {
		super(EntityBase.class);
	}

}
