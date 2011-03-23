/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class UserDaoImpl extends CdmEntityDaoBase<User> implements IUserDao {

	public UserDaoImpl() {
		super(User.class);
	}

	public User findUserByUsername(String username) {
		Query query = getSession().createQuery("select user from User user where user.username = :username");
		query.setParameter("username", username);
		
		User user = (User)query.uniqueResult(); // username is a @NaturalId
		
		if(user != null) {
			Hibernate.initialize(user.getPerson());
			Hibernate.initialize(user.getGrantedAuthorities());
			for(Group group : user.getGroups()) {
				Hibernate.initialize(group.getGrantedAuthorities());
			}
		}
		
		return user;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IUserDao#countByUsername(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.List)
	 */
	public int countByUsername(String queryString,	MatchMode matchmode, List<Criterion> criterion) {
		return countByParam("username",queryString,matchmode,criterion);
	}
	
	protected int countByParam(String param, String queryString, MatchMode matchmode, List<Criterion> criterion) {
    	//checkNotInPriorView("IdentifiableDaoBase.findByParam(Class<? extends T> clazz, String queryString, MatchMode matchmode, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");
    	Criteria criteria = null;

    	criteria = getSession().createCriteria(type);
    	
    	if (queryString != null) {
    		if(matchmode == null) {
    			criteria.add(Restrictions.ilike(param, queryString));
    		} else if(matchmode == MatchMode.BEGINNING) {
    			criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.START));
    		} else if(matchmode == MatchMode.END) {
    			criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.END));
    		} else if(matchmode == MatchMode.EXACT) {
    			criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.EXACT));
    		} else {
    			criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.ANYWHERE));
    		}
    	}
    	
    	addCriteria(criteria, criterion);
    	
    	criteria.setProjection(Projections.rowCount());    	

    	return (Integer)criteria.uniqueResult();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IUserDao#findByUsername(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.List, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
	 */
	public List<User> findByUsername(String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		return findByParam("username", queryString, matchmode, criterion, pageSize, pageNumber, orderHints, propertyPaths);
	}
	
    protected List<User> findByParam(String param, String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
    	Criteria criteria = null;

    	criteria = getSession().createCriteria(type);
    	
    	if (queryString != null) {
    		if(matchmode == null) {
    			criteria.add(Restrictions.ilike(param, queryString));
    		} else if(matchmode == MatchMode.BEGINNING) {
    			criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.START));
    		} else if(matchmode == MatchMode.END) {
    			criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.END));
    		} else if(matchmode == MatchMode.EXACT) {
    			criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.EXACT));
    		} else {
    			criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.ANYWHERE));
    		}
    	}
    	
    	addCriteria(criteria, criterion);

    	if(pageSize != null) {
    		criteria.setMaxResults(pageSize);
    		if(pageNumber != null) {
    			criteria.setFirstResult(pageNumber * pageSize);
    		} else {
    			criteria.setFirstResult(0);
    		}
    	}

    	addOrder(criteria, orderHints);

    	List<User> result = (List<User>)criteria.list();
    	defaultBeanInitializer.initializeAll(result, propertyPaths);
    	return result;
    }

}
