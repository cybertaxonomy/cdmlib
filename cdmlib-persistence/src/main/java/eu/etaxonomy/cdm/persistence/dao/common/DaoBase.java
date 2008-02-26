package eu.etaxonomy.cdm.persistence.dao.common;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DaoBase {
	
	@Autowired
	private SessionFactory factory;
	
	protected Session getSession(){
		return factory.getCurrentSession();
	}

}
