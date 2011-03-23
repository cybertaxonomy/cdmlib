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
import eu.etaxonomy.cdm.persistence.dao.common.IGroupDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class GroupDaoImpl extends CdmEntityDaoBase<Group> implements IGroupDao {

	public GroupDaoImpl() {
		super(Group.class);
	}

	public Group findGroupByName(String groupName) {
		Query query = getSession().createQuery("select g from Group g where g.name = :name");
		query.setParameter("name",groupName);
		
		Group group = (Group)query.uniqueResult();
		if(group != null) {
		  Hibernate.initialize(group.getGrantedAuthorities());
		  Hibernate.initialize(group.getMembers());
		}
		
		return group;
	}

	public List<String> listNames(Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select g.name from Group g");
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<String>)query.list();
	}

	public List<String> listMembers(Group group, Integer pageSize,	Integer pageNumber) {
		Query query = getSession().createQuery("select m.username from Group g join g.members m where g = :group");
		query.setParameter("group", group);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<String>)query.list();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IGroupDao#countByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.List)
	 */
	public int countByName(String queryString,	MatchMode matchmode, List<Criterion> criterion) {
		return countByParam("name",queryString,matchmode,criterion);
	}
	
	protected int countByParam(String param, String queryString, MatchMode matchmode, List<Criterion> criterion) {
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
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IGroupDao#findByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.List, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
	 */
	public List<Group> findByName(String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		return findByParam("name", queryString, matchmode, criterion, pageSize, pageNumber, orderHints, propertyPaths);
	}
	
    protected List<Group> findByParam(String param, String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
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

    	List<Group> result = (List<Group>)criteria.list();
    	defaultBeanInitializer.initializeAll(result, propertyPaths);
    	return result;
    }

}
