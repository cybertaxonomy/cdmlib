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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class AnnotationDaoImpl extends AnnotatableDaoBaseImpl<Annotation> implements IAnnotationDao {

	public AnnotationDaoImpl() {
		super(Annotation.class);
	}

	@Override
	public long count(Person commentator, MarkerType markerType) {

//	    checkNotInPriorView("AnnotationDaoImpl.count(Person commentator, MarkerType status)");

	    CriteriaBuilder cb = getCriteriaBuilder();
	    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<Annotation> root = cq.from(Annotation.class);

	    List<Predicate> predicates = new ArrayList<>();

	    if (commentator != null) {
	        predicates.add(cb.equal(root.get("commentator"), commentator));
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

	@Override
    public List<Annotation> list(Person commentator, MarkerType markerType, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

	    checkNotInPriorView("AnnotationDaoImpl.list(Person commentator, MarkerType status,	Integer pageSize, Integer pageNumber)");
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
        Root<Annotation> root = cq.from(Annotation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (commentator != null) {
            predicates.add(cb.equal(root.get("commentator"), commentator));
        }

        // Add marker type Filter
        if (markerType != null) {
            Join<Annotation, ?> markersJoin = root.join("markers");
            predicates.add(cb.equal(markersJoin.get("markerType"), markerType));
        }

        cq.select(root);
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(ordersFrom(cb, root, orderHints));

		List<Annotation> results = addPageSizeAndNumber(
		        getSession().createQuery(cq), pageSize, pageNumber)
		        .getResultList();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
    public long count(User creator, MarkerType markerType) {

	    checkNotInPriorView("AnnotationDaoImpl.count(User creator, MarkerType statu)");
	       CriteriaBuilder cb = getCriteriaBuilder();
	        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	        Root<Annotation> root = cq.from(Annotation.class);

	        List<Predicate> predicates = new ArrayList<>();

	        if (creator != null) {
	            predicates.add(cb.equal(root.get("creator"), creator));
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

	@Override
    public List<Annotation> list(User creator, MarkerType markerType, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {

	    checkNotInPriorView("AnnotationDaoImpl.list(User creator, MarkerType status,	Integer pageSize, Integer pageNumber)");

	    CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
        Root<Annotation> root = cq.from(Annotation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (creator != null) {
            predicates.add(cb.equal(root.get("createdBy"), creator));
        }

        // Add marker type Filter
        if (markerType != null) {
            Join<Annotation, ?> markersJoin = root.join("markers");
            predicates.add(cb.equal(markersJoin.get("markerType"), markerType));
        }

        cq.select(root);
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(ordersFrom(cb, root, orderHints));
//        order(cb, cq, root, orderHints);

        List<Annotation> results = addPageSizeAndNumber(
                getSession().createQuery(cq), pageSize, pageNumber)
                .getResultList();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
	}
}
