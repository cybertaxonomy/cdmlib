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
import eu.etaxonomy.cdm.persistence.dao.common.IGroupDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class GroupDaoImpl extends CdmEntityDaoBase<Group> implements IGroupDao {

	public GroupDaoImpl() {
		super(Group.class);
	}

	@Override
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

	@Override
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

        @SuppressWarnings("unchecked")
        List<String> result = query.list();
        return result;
	}

	@Override
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

		@SuppressWarnings("unchecked")
        List<String> result = query.list();
		return result;
	}

	@Override
    public long countByName(String queryString,	MatchMode matchmode, List<Criterion> criterion) {
		return countByParam(type, "name",queryString,matchmode,criterion);
	}

	@Override
    public List<Group> findByName(String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		return findByParam(type, "name", queryString, matchmode, criterion, pageSize, pageNumber, orderHints, propertyPaths);
	}
}
