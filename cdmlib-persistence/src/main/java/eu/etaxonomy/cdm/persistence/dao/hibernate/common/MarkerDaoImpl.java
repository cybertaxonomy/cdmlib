/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.common.IMarkerDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class MarkerDaoImpl extends VersionableDaoBase<Marker> implements IMarkerDao {

	public MarkerDaoImpl() {
		super(Marker.class);
	}

	@Override
    public long count(MarkerType markerType) {
		return count(null, markerType);
	}

	@Override
    public List<Marker> list(MarkerType markerType, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

	    return list(null, markerType, pageSize, pageNumber, orderHints, propertyPaths);
	}

	@Override
	public UUID delete(Marker marker) {
		throw new RuntimeException("Delete is not supported for markers. Markers must be removed from the marked object instead.");
	}

	public long count(User creator, MarkerType markerType) {

	    CriteriaBuilder cb = getCriteriaBuilder();
	    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<Marker> root = cq.from(Marker.class);

	    List<Predicate> predicates = new ArrayList<>();

        if (creator != null) {
            predicates.add(cb.equal(root.get("createdBy"), creator));
        }

        // Add marker type Filter
        if (markerType != null) {
            Join<Annotation, ?> markersJoin = root.join("markers");
            predicates.add(cb.equal(markersJoin.get("markerType"), markerType));
        }

        cq.select(cb.countDistinct(root.get("id")));

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return getSession().createQuery(cq).getSingleResult();
	}

	public List<Marker> list(User creator, MarkerType markerType, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Marker> cq = cb.createQuery(Marker.class);
        Root<Marker> root = cq.from(Marker.class);

        List<Predicate> predicates = new ArrayList<>();

        if (creator != null) {
            predicates.add(cb.equal(root.get("createdBy"), creator));
        }

        // Add marker type Filter
        if (markerType != null) {
            Join<Annotation, ?> markersJoin = root.join("markers");
            predicates.add(cb.equal(markersJoin.get("markerType"), markerType));
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        cq.select(root);
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(ordersFrom(cb, root, orderHints));

        List<Marker> results = addPageSizeAndNumber(
                 getSession().createQuery(cq), pageSize, pageNumber)
                .getResultList();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
	}
}