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

import org.apache.log4j.Logger;
import org.hibernate.Query;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * @author n.hoffmann
 * @created 24.09.2008
 * @version 1.0
 */
public abstract class AnnotatableDaoImpl<T extends AnnotatableEntity> extends VersionableDaoBase<T> implements IAnnotatableDao<T> {
	private static Logger logger = Logger.getLogger(AnnotatableDaoImpl.class);
	
	/**
	 * @param type
	 */
	public AnnotatableDaoImpl(Class<T> type) {
		super(type);
	}
	
	public int countAnnotations(T annotatableEntity, MarkerType status) {
		checkNotInPriorView("AnnotatableDaoImpl.countAnnotations(T annotatableEntity, MarkerType status)");
		Query query = null;
		
		if(status == null) {
			query = getSession().createQuery("select count(annotation) from Annotation annotation where annotation.annotatedObj.id = :id and annotation.annotatedObj.class = :class");
		} else {
			query = getSession().createQuery("select count(annotation) from Annotation annotation join annotation.markers marker where annotation.annotatedObj.id = :id and annotation.annotatedObj.class = :class and marker.markerType = :status");
			query.setParameter("status",status);
		}
		
		query.setParameter("id",annotatableEntity.getId());
		query.setParameter("class", annotatableEntity.getClass().getName());
		
		return ((Long)query.uniqueResult()).intValue();
	}
	
	public List<Annotation> getAnnotations(T annotatableEntity,	MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("AnnotatableDaoImpl.getAnnotations(T annotatableEntity, MarkerType status, Integer pageSize, Integer pageNumber)");
        Query query = null;
        
        StringBuffer orderString = new StringBuffer();
        
        if(orderHints != null && !orderHints.isEmpty()) {
		    orderString.append(" order by");
		    for(OrderHint orderHint : orderHints) {
		    	orderString.append(" annotation." + orderHint.getPropertyName() + " ");
		    	
		    	if(orderHint.getSortOrder() == SortOrder.ASCENDING) {
		    		orderString.append("asc");
		    	} else {
		    		orderString.append("desc");
		    	}
		    }
		}
        
		
		if(status == null) {
			query = getSession().createQuery("select annotation from Annotation annotation where annotation.annotatedObj.id = :id and annotation.annotatedObj.class = :class" + orderString.toString());
		} else {
			query = getSession().createQuery("select annotation from Annotation annotation join annotation.markers marker where annotation.annotatedObj.id = :id and annotation.annotatedObj.class = :class and marker.markerType = :status" + orderString.toString());
			query.setParameter("status",status);
		}
		
		query.setParameter("id",annotatableEntity.getId());
		query.setParameter("class", annotatableEntity.getClass().getName());
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		List<Annotation> results = (List<Annotation>)query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}
	
}
