package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DaoBase {
	
	@Autowired
	private SessionFactory factory;
	
	protected Session getSession(){
		Session session = factory.getCurrentSession();
		return session;
	}
	
	public void flush(){
		getSession().flush();
	}
	

}
