package eu.etaxonomy.cdm.persistence.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Enumeration;



public class EnumerationDaoHibernateImpl extends DaoBase<Enumeration, Integer> implements IEnumerationDAO {
	private static final Logger logger = Logger.getLogger(EnumerationDaoHibernateImpl.class);

	public EnumerationDaoHibernateImpl() {
		super(Enumeration.class);
	}

	@Override
	public List<Enumeration> find(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

}
