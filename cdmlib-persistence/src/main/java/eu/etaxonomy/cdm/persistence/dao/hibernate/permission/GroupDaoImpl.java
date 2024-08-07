/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.permission;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.permission.IGroupDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class GroupDaoImpl extends CdmEntityDaoBase<Group> implements IGroupDao {

	public GroupDaoImpl() {
		super(Group.class);
	}

	@Override
	public Group findGroupByName(String groupName) {
		Query<Group> query = getSession().createQuery("select g from Group g where g.name = :name", Group.class);
		query.setParameter("name",groupName);

		Group group = query.uniqueResult();
		if(group != null) {
		  Hibernate.initialize(group.getGrantedAuthorities());
		  Hibernate.initialize(group.getMembers());
		}

		return group;
	}

	@Override
    public List<String> listNames(Integer pageSize, Integer pageNumber) {
		Query<String> query = getSession().createQuery("SELECT g.name FROM Group g", String.class);
		this.addPageSizeAndNumber(query, pageSize, pageNumber);

        List<String> result = query.list();
        return result;
	}

	@Override
    public List<String> listMembers(Group group, Integer pageSize,	Integer pageNumber) {
		Query<String> query = getSession().createQuery("SELECT m.username FROM Group g JOIN g.members m WHERE g = :group", String.class);
		query.setParameter("group", group);
		this.addPageSizeAndNumber(query, pageSize, pageNumber);

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
