/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * @author n.hoffmann
 * @since 24.09.2008
 */
public abstract class AnnotatableDaoBaseImpl<T extends AnnotatableEntity>
        extends VersionableDaoBase<T>
        implements IAnnotatableDao<T> {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	public AnnotatableDaoBaseImpl(Class<T> type) {
		super(type);
	}

	@Override
    public long countAnnotations(T annotatableEntity, MarkerType status) {
		checkNotInPriorView("AnnotatableDaoBaseImpl.countAnnotations(T annotatableEntity, MarkerType status)");
		Query<Long> query = null;

		String className = annotatableEntity.getClass().getName();
        if(status == null) {
           //AND annoEnt.class = :class" does not work for some reason
        	query = getSession().createQuery("SELECT COUNT(annotation) FROM " + className + " annoEnt JOIN annoEnt.annotations annotation WHERE annoEnt.id = :id", Long.class );
        } else {
        	query = getSession().createQuery("SELECT COUNT(annotation) FROM " + className + " annoEnt JOIN annoEnt.annotations annotation JOIN annotation.markers marker "
        	        + " WHERE annoEnt.id = :id AND marker.markerType = :status",
        	        Long.class);
        	query.setParameter("status", status);
        }

        query.setParameter("id", annotatableEntity.getId());
		return query.uniqueResult();
	}

	@Override
    public List<Annotation> getAnnotations(T annotatableEntity,	MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("AnnotatableDaoBaseImpl.getAnnotations(T annotatableEntity, MarkerType status, Integer pageSize, Integer pageNumber)");

		Query<Annotation> query = null;
        StringBuffer orderString = new StringBuffer();

        if(orderHints != null && !orderHints.isEmpty()) {
		    orderString.append(" ORDER BY");
		    for(OrderHint orderHint : orderHints) {
		    	orderString.append(" annotation." + orderHint.getPropertyName() + " ");

		    	if(orderHint.getSortOrder() == SortOrder.ASCENDING) {
		    		orderString.append("ASC");
		    	} else {
		    		orderString.append("DESC");
		    	}
		    }
		}

        String className = annotatableEntity.getClass().getName();
        if(status == null) {
            //AND annoEnt.class = :class  does not work for some reason
        	query = getSession().createQuery("SELECT annotation FROM " + className + " annoEnt JOIN annoEnt.annotations annotation WHERE annoEnt.id = :id " + orderString.toString(), Annotation.class);
        } else {
        	query = getSession().createQuery("SELECT annotation FROM " + className + " annoEnt JOIN annoEnt.annotations annotation JOIN annotation.markers marker " +
        	        " WHERE annoEnt.id = :id AND marker.markerType = :status" + orderString.toString(),
        	        Annotation.class);
        	query.setParameter("status",status);
        }

        query.setParameter("id",annotatableEntity.getId());

        addPageSizeAndNumber(query, pageSize, pageNumber);

        List<Annotation> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
    public long countMarkers(T annotatableEntity, Boolean technical) {
		checkNotInPriorView("AnnotatableDaoBaseImpl.countMarkers(T annotatableEntity, Boolean technical");
        Query<Long> query = null;

        String className = annotatableEntity.getClass().getName();
		if(technical == null) {
			query = getSession().createQuery("SELECT COUNT(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker  WHERE annoEnt.id = :id ", Long.class);
		} else {
			query = getSession().createQuery("SELECT COUNT(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type "
			        + " WHERE annoEnt.id = :id AND type.isTechnical = :technical",
			        Long.class);
			query.setParameter("technical", technical);
		}

		query.setParameter("id",annotatableEntity.getId());
		return query.uniqueResult();
	}

    @Override
    public List<Marker> getMarkers(T annotatableEntity, Boolean technical, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
    	checkNotInPriorView("AnnotatableDaoBaseImpl.getMarkers(T annotatableEntity, Boolean technical, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");

    	Query<Marker> query = null;
        StringBuffer orderString = new StringBuffer();

        if(orderHints != null && !orderHints.isEmpty()) {
		    orderString.append(" ORDER BY");
		    for(OrderHint orderHint : orderHints) {
		    	orderString.append(" marker." + orderHint.getPropertyName() + " ");

		    	if(orderHint.getSortOrder() == SortOrder.ASCENDING) {
		    		orderString.append("ASC");
		    	} else {
		    		orderString.append("DESC");
		    	}
		    }
		}

        String className = annotatableEntity.getClass().getName();
		if(technical == null) {
			query = getSession().createQuery("SELECT marker FROM " + className + " annoEnt JOIN annoEnt.markers marker WHERE annoEnt.id = :id" + orderString.toString(), Marker.class);
		} else {
			query = getSession().createQuery("SELECT marker FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type "
			        + " WHERE annoEnt.id = :id AND type.isTechnical = :technical" + orderString.toString(),
			        Marker.class);
			query.setParameter("technical",technical);
		}

		query.setParameter("id",annotatableEntity.getId());

		addPageSizeAndNumber(query, pageSize, pageNumber);
		List<Marker> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
    }

    @Override
    public int countMarkers(Class<? extends T> clazz, Boolean technical) {
		checkNotInPriorView("AnnotatableDaoBaseImpl.countMarkers(Class<? extends T> clazz, Boolean technical, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
		Query<Long> query = null;
		String className = clazz == null ? type.getName() : clazz.getName();
		if(technical == null) {
			query = getSession().createQuery("SELECT count(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type", Long.class);
		} else {
			query = getSession().createQuery("SELECT count(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type "
			        + " WHERE type.technical = :technical", Long.class);
			query.setParameter("technical", technical);
		}

//		if(clazz == null) {
//		  query.setParameter("class", type.getName());
//		} else {
//	      query.setParameter("class", clazz.getName());
//		}

		return query.uniqueResult().intValue();
	}

	@Override
    public List<Object[]> groupMarkers(Class<? extends T> clazz, Boolean technical, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("AnnotatableDaoBaseImpl.groupMarkers(Class<? extends T> clazz, Boolean technical, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
		Query<Object[]> query = null;
		String className = clazz == null ? type.getName() : clazz.getName();
        if(technical == null) {
			query = getSession().createQuery("SELECT type, count(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type GROUP BY type ORDER BY type.titleCache ASC", Object[].class);
		} else {
			query = getSession().createQuery("SELECT type, count(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type "
			        + " WHERE type.technical = :technical GROUP BY type ORDER BY type.titleCache ASC",
			        Object[].class);
			query.setParameter("technical", technical);
		}

//		if(clazz == null) {
//			  query.setParameter("class", type.getName());
//		} else {
//		      query.setParameter("class", clazz.getName());
//		}

        addPageSizeAndNumber(query, pageSize, pageNumber);
		List<Object[]> result = query.list();
		if(propertyPaths != null && !propertyPaths.isEmpty()) {
		  for(Object[] objects : result) {
			defaultBeanInitializer.initialize(objects[0], propertyPaths);
		  }
		}

		return result;
	}
}