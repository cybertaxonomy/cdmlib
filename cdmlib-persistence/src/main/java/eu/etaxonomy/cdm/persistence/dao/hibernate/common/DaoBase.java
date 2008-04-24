/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

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
