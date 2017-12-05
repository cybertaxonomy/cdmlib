/**
* Copyright (C) 2007 EDIT
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

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.IAnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * @author n.hoffmann
 * @created 24.09.2008
 */
public abstract class AnnotatableDaoImpl<T extends AnnotatableEntity>
        extends VersionableDaoBase<T>
        implements IAnnotatableDao<T> {

    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AnnotatableDaoImpl.class);

	/**
	 * @param type
	 */
	public AnnotatableDaoImpl(Class<T> type) {
		super(type);
	}

	@Override
    public int countAnnotations(T annotatableEntity, MarkerType status) {
		checkNotInPriorView("AnnotatableDaoImpl.countAnnotations(T annotatableEntity, MarkerType status)");
		Query query = null;

		String className = annotatableEntity.getClass().getName();
        if(status == null) {
           //AND annoEnt.class = :class" does not work for some reason
        	query = getSession().createQuery("SELECT COUNT(annotation) FROM " + className + " annoEnt JOIN annoEnt.annotations annotation WHERE annoEnt.id = :id" );
        } else {
        	query = getSession().createQuery("SELECT COUNT(annotation) FROM " + className + " annoEnt JOIN annoEnt.annotations annotation JOIN annotation.markers marker "
        	        + " WHERE annoEnt.id = :id AND marker.markerType = :status");
        	query.setParameter("status", status);
        }

        query.setParameter("id", annotatableEntity.getId());


		return ((Long)query.uniqueResult()).intValue();
	}

	@Override
    public List<Annotation> getAnnotations(T annotatableEntity,	MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("AnnotatableDaoImpl.getAnnotations(T annotatableEntity, MarkerType status, Integer pageSize, Integer pageNumber)");
        Query query = null;

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
        	query = getSession().createQuery("SELECT annotation FROM " + className + " annoEnt JOIN annoEnt.annotations annotation WHERE annoEnt.id = :id " + orderString.toString());
        } else {
        	query = getSession().createQuery("SELECT annotation FROM " + className + " annoEnt JOIN annoEnt.annotations annotation JOIN annotation.markers marker " +
        	        " WHERE annoEnt.id = :id AND marker.markerType = :status" + orderString.toString());
        	query.setParameter("status",status);
        }

        query.setParameter("id",annotatableEntity.getId());


		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}

		List<Annotation> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
    public int countMarkers(T annotatableEntity, Boolean technical) {
		checkNotInPriorView("AnnotatableDaoImpl.countMarkers(T annotatableEntity, Boolean technical");
        Query query = null;

        String className = annotatableEntity.getClass().getName();
		if(technical == null) {
			query = getSession().createQuery("SELECT COUNT(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker  WHERE annoEnt.id = :id ");
		} else {
			query = getSession().createQuery("SELECT COUNT(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type "
			        + " WHERE annoEnt.id = :id AND type.isTechnical = :technical");
			query.setParameter("technical", technical);
		}

		query.setParameter("id",annotatableEntity.getId());

		return ((Long)query.uniqueResult()).intValue();
	}

    @Override
    public List<Marker> getMarkers(T annotatableEntity, Boolean technical, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
    	checkNotInPriorView("AnnotatableDaoImpl.getMarkers(T annotatableEntity, Boolean technical, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");
        Query query = null;

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
			query = getSession().createQuery("SELECT marker FROM " + className + " annoEnt JOIN annoEnt.markers marker WHERE annoEnt.id = :id" + orderString.toString());
		} else {
			query = getSession().createQuery("SELECT marker FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type "
			        + " WHERE annoEnt.id = :id AND type.isTechnical = :technical" + orderString.toString());
			query.setParameter("technical",technical);
		}

		query.setParameter("id",annotatableEntity.getId());

		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}

		List<Marker> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
    }

    @Override
    public int countMarkers(Class<? extends T> clazz, Boolean technical) {
		checkNotInPriorView("AnnotatableDaoImpl.countMarkers(Class<? extends T> clazz, Boolean technical, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
		Query query = null;
		String className = clazz == null ? type.getName() : clazz.getName();
		if(technical == null) {
			query = getSession().createQuery("SELECT count(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type");
		} else {
			query = getSession().createQuery("SELECT count(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type "
			        + " WHERE type.technical = :technical");
			query.setParameter("technical", technical);
		}

//		if(clazz == null) {
//		  query.setParameter("class", type.getName());
//		} else {
//	      query.setParameter("class", clazz.getName());
//		}

		return ((Long)query.uniqueResult()).intValue();
	}

	@Override
    public List<Object[]> groupMarkers(Class<? extends T> clazz, Boolean technical, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("AnnotatableDaoImpl.groupMarkers(Class<? extends T> clazz, Boolean technical, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
		Query query = null;
		String className = clazz == null ? type.getName() : clazz.getName();
        if(technical == null) {
			query = getSession().createQuery("SELECT type, count(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type GROUP BY type ORDER BY type.titleCache ASC");
		} else {
			query = getSession().createQuery("SELECT type, count(marker) FROM " + className + " annoEnt JOIN annoEnt.markers marker JOIN marker.markerType type "
			        + " WHERE type.technical = :technical GROUP BY type ORDER BY type.titleCache ASC");
			query.setParameter("technical", technical);
		}

//		if(clazz == null) {
//			  query.setParameter("class", type.getName());
//		} else {
//		      query.setParameter("class", clazz.getName());
//		}

		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}

		List<Object[]> result = query.list();

		if(propertyPaths != null && !propertyPaths.isEmpty()) {
		  for(Object[] objects : result) {
			defaultBeanInitializer.initialize(objects[0], propertyPaths);
		  }
		}

		return result;
	}

    @Override
    public List<UuidAndTitleCache<T>> getUuidAndTitleCache(Integer limit, String pattern){
        return getUuidAndTitleCache(type, limit, pattern);
    }


    @Override
    public <S extends T> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz, Integer limit, String pattern){
        Session session = getSession();
        Query query = null;
        if (pattern != null){
            query = session.createQuery("select uuid, id, titleCache from " + clazz.getSimpleName() +" where titleCache like :pattern");
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery("select uuid, id, titleCache from " + clazz.getSimpleName() );
        }
        if (limit != null){
           query.setMaxResults(limit);
        }
        return getUuidAndTitleCache(query);
    }


    @Override
    public List<UuidAndTitleCache<T>> getUuidAndTitleCache(){
        return getUuidAndTitleCache(type, null, null);
    }

    protected <E extends IAnnotatableEntity> List<UuidAndTitleCache<E>> getUuidAndAbbrevTitleCache(Query query){
        List<UuidAndTitleCache<E>> list = new ArrayList<UuidAndTitleCache<E>>();

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<E>((UUID) object[0],(Integer) object[1], (String) object[3], (String) object[2]));
        }
        return list;
    }

    protected <E extends IAnnotatableEntity> List<UuidAndTitleCache<E>> getUuidAndTitleCache(Query query){
        List<UuidAndTitleCache<E>> list = new ArrayList<UuidAndTitleCache<E>>();

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<E>((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }

}
