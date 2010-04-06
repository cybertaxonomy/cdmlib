/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class DaoBase {
	
	@Autowired
	private SessionFactory factory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.factory = sessionFactory;
	}
	
	protected Session getSession(){
		Session session = factory.getCurrentSession();
		return session;
	}
	
	public void flush(){
		getSession().flush();
	}
	
	private class OrderHintComparator implements Comparator<OrderHint> {

		public int compare(OrderHint o1, OrderHint o2) {
			return o1.getPropertyName().compareTo(o2.getPropertyName());
		}
		
	}
	
	protected void addOrder(Criteria criteria, List<OrderHint> orderHints) {
		
		if(orderHints != null){
			Collections.sort(orderHints, new OrderHintComparator());
			
			Map<String,Criteria> criteriaMap = new HashMap<String,Criteria>();
			for(OrderHint orderHint : orderHints){
				orderHint.add(criteria,criteriaMap);
			}
		}
	}

}
