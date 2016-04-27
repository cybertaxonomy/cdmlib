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
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IMarkerDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class MarkerDaoImpl extends VersionableDaoBase<Marker> implements IMarkerDao {

	public MarkerDaoImpl() {
		super(Marker.class);
	}

	@Override
    public int count(MarkerType markerType) {
		Criteria criteria = getSession().createCriteria(Marker.class);
		criteria.add(Restrictions.eq("markerType", markerType));
		criteria.setProjection(Projections.rowCount());
        return ((Number)criteria.uniqueResult()).intValue();
	}

	@Override
    public List<Marker> list(MarkerType markerType, Integer pageSize,	Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
		Criteria criteria = getSession().createCriteria(Marker.class);
		criteria.add(Restrictions.eq("markerType", markerType));

		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    }
		}

		addOrder(criteria, orderHints);
		List<Marker> results = criteria.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
	public UUID delete(Marker marker) {
		throw new RuntimeException("Delete is not supported for markers. Markers must be removed from the marked object instead.");
	}

	public Integer count(User creator, MarkerType markerType) {
		Criteria criteria = getSession().createCriteria(Marker.class);
		criteria.add(Restrictions.eq("createdBy", creator));
		if(markerType != null) {
		    criteria.add(Restrictions.eq("markerType", markerType));
		}
		criteria.setProjection(Projections.rowCount());
        return ((Number)criteria.uniqueResult()).intValue();
	}

	public List<Marker> list(User creator, MarkerType markerType, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
		Criteria criteria = getSession().createCriteria(Marker.class);
		criteria.add(Restrictions.eq("createdBy", creator));
		if(markerType != null) {
		    criteria.add(Restrictions.eq("markerType", markerType));
		}

		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    }
		}

		addOrder(criteria, orderHints);
		List<Marker> results = criteria.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

}
