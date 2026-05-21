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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class AnnotationDaoImpl
        extends VersionableDaoBase<Annotation>
        implements IAnnotationDao {

	public AnnotationDaoImpl() {
		super(Annotation.class);
	}

	@Override
	public long count(Person commentator) {

	    checkNotInPriorView("AnnotationDaoImpl.count(Person commentator, MarkerType status)");

	    CriteriaBuilder cb = getCriteriaBuilder();
	    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<Annotation> root = cq.from(Annotation.class);

	    List<Predicate> predicates = new ArrayList<>();

	    if (commentator != null) {
	        predicates.add(cb.equal(root.get("commentator"), commentator));
	    }

	    cq.select(cb.countDistinct(root.get("id")))
	      .where(predicateAnd(cb, predicates));

	    return getSession().createQuery(cq).getSingleResult();
	}

	@Override
    public List<Annotation> list(Person commentator, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

	    checkNotInPriorView("AnnotationDaoImpl.list(Person commentator, MarkerType status,	Integer pageSize, Integer pageNumber)");

	    CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
        Root<Annotation> root = cq.from(Annotation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (commentator != null) {
            predicates.add(cb.equal(root.get("commentator"), commentator));
//          root.join("commentator", JoinType.LEFT);  //not needed as long as we do not order on any commentator attributes like commentator.titleCache
        }

        cq.select(root)
          .where(predicateAnd(cb, predicates))
          .orderBy(ordersFrom(cb, root, orderHints));

		List<Annotation> results = addPageSizeAndNumber(
		         getSession().createQuery(cq), pageSize, pageNumber)
		        .getResultList();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
    public long count(User creator) {

	    checkNotInPriorView("AnnotationDaoImpl.count(User creator, MarkerType statu)");

	    CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Annotation> root = cq.from(Annotation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (creator != null) {
            predicates.add(cb.equal(root.get("createdBy"), creator));
        }

        cq.select(cb.countDistinct(root.get("id")));

        if (!predicates.isEmpty()) {
            cq.where(predicateAnd(cb, predicates));
        }

        return getSession().createQuery(cq).getSingleResult();
	}

	@Override
    public List<Annotation> list(User creator, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {

	    checkNotInPriorView("AnnotationDaoImpl.list(User creator, MarkerType status,	Integer pageSize, Integer pageNumber)");

	    CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
        Root<Annotation> root = cq.from(Annotation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (creator != null) {
            predicates.add(cb.equal(root.get("createdBy"), creator));
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
}