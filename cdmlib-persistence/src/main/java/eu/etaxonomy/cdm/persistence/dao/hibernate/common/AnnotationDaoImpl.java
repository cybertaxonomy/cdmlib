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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
public class AnnotationDaoImpl extends LanguageStringBaseDaoImpl<Annotation> implements IAnnotationDao {

	public AnnotationDaoImpl() {
		super(Annotation.class);
	}

	@Override
	public long count(Person commentator, MarkerType status) {
		checkNotInPriorView("AnnotationDaoImpl.count(Person commentator, MarkerType status)");
		Criteria criteria = getSession().createCriteria(Annotation.class);

		 if(commentator != null) {
	        criteria.add(Restrictions.eq("commentator",commentator));
	     }

		if(status != null) {
			criteria.createCriteria("markers").add(Restrictions.eq("markerType", status));
		}

		criteria.setProjection(Projections.countDistinct("id"));

		return (Long)criteria.uniqueResult();
	}

	@Override
    public List<Annotation> list(Person commentator, MarkerType status,	Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("AnnotationDaoImpl.list(Person commentator, MarkerType status,	Integer pageSize, Integer pageNumber)");
        Criteria criteria = getSession().createCriteria(Annotation.class);

        if(commentator != null) {
            criteria.add(Restrictions.eq("commentator",commentator));
        }

		if(status != null) {
			criteria.createCriteria("markers").add(Restrictions.eq("markerType", status));
		}

		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    }
		}

		addOrder(criteria, orderHints);
		List<Annotation> results = criteria.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
    public long count(User creator, MarkerType status) {
		checkNotInPriorView("AnnotationDaoImpl.count(User creator, MarkerType statu)");
		Criteria criteria = getSession().createCriteria(Annotation.class);

		 if(creator != null) {
	        criteria.add(Restrictions.eq("createdBy",creator));
	     }

		if(status != null) {
			criteria.createCriteria("markers").add(Restrictions.eq("markerType", status));
		}

		criteria.setProjection(Projections.countDistinct("id"));

		return (Long)criteria.uniqueResult();
	}

	@Override
    public List<Annotation> list(User creator, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
		checkNotInPriorView("AnnotationDaoImpl.list(User creator, MarkerType status,	Integer pageSize, Integer pageNumber)");
        Criteria criteria = getSession().createCriteria(Annotation.class);

        if(creator != null) {
            criteria.add(Restrictions.eq("createdBy",creator));
        }

		if(status != null) {
			criteria.createCriteria("markers").add(Restrictions.eq("markerType", status));
		}

		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    }
		}

		addOrder(criteria, orderHints);
		List<Annotation> results = criteria.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}
}
