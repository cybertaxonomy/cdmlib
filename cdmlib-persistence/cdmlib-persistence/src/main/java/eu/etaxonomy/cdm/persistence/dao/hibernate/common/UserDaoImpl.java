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

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
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
		return countByParam(type, "username",queryString,matchmode,criterion);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IUserDao#findByUsername(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.List, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
	 */
	public List<User> findByUsername(String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		return findByParam(type, "username", queryString, matchmode, criterion, pageSize, pageNumber, orderHints, propertyPaths);
	}
	
}
