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
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditCriterion;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.model.view.AuditEventRecordImpl;
import eu.etaxonomy.cdm.model.view.context.AuditEventContext;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dao.common.IVersionableDao;
import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class VersionableDaoBase<T extends VersionableEntity> extends CdmEntityDaoBase<T> implements IVersionableDao<T> {
	private static final Logger logger = Logger.getLogger(VersionableDaoBase.class);

	protected AuditReader getAuditReader() {
		return AuditReaderFactory.get(getSession());
	}

	public VersionableDaoBase(Class<T> type) {
		super(type);
	}

	 protected AuditEvent getAuditEventFromContext() {
		AuditEventContext auditEventContext = AuditEventContextHolder.getContext();

	   	AuditEvent auditEvent = auditEventContext.getAuditEvent();
	   	if(auditEvent != null) {
	   		logger.debug(" AuditEvent found, returning " + auditEvent);
	  	    return auditEvent;
	    } else {
	    	logger.debug(" AuditEvent is NULL, returning AuditEvent.CURRENT_VIEW");
	   		return AuditEvent.CURRENT_VIEW;
	   	}
	}

	protected void checkNotInPriorView(String message) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(!auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			throw new OperationNotSupportedInPriorViewException(message);
		}
	}

	@Override
	public T findByUuid(UUID uuid) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.findByUuid(uuid);
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("uuid").eq(uuid));
			@SuppressWarnings("unchecked")
            T result = (T)query.getSingleResult();
			return result;
		}
	}

    @Override
	protected List<T> findByParam(Class<? extends T> clazz, String param, String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
    	checkNotInPriorView("IdentifiableDaoBase.findByParam(Class<? extends T> clazz, String queryString, MatchMode matchmode, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");
    	return super.findByParam(clazz, param, queryString, matchmode, criterion, pageSize, pageNumber, orderHints, propertyPaths);
    }

	@Override
	public T load(UUID uuid) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.load(uuid);
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("uuid").eq(uuid));
			@SuppressWarnings("unchecked")
            T t = (T)query.getSingleResult();
			defaultBeanInitializer.load(t);
			return t;
		}
	}

	@Override
    public T load(UUID uuid, List<String> propertyPaths) {
	    return this.load(uuid, INCLUDE_UNPUBLISHED, propertyPaths);
	}

	@Override
    protected T load(UUID uuid, boolean includeUnpublished, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.load(uuid, includeUnpublished, propertyPaths);
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("uuid").eq(uuid));
			@SuppressWarnings("unchecked")
            T t = (T)query.getSingleResult();
			defaultBeanInitializer.initialize(t, propertyPaths);
			return t;
		}
	}


	@Override
	public Boolean exists(UUID uuid) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.exists(uuid);
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("uuid").eq(uuid));
			return null != query.getSingleResult();
		}
	}

	@Override
	public int count() {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.count();
		} else {
			return this.count(null);
		}
	}

	@Override
	public int count(Class<? extends T> clazz) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.count(clazz);
		} else {
			AuditQuery query = null;
			if(clazz == null) {
			    query = getAuditReader().createQuery().forEntitiesAtRevision(type, auditEvent.getRevisionNumber());
			} else {
				query = getAuditReader().createQuery().forEntitiesAtRevision(clazz, auditEvent.getRevisionNumber());
			}

			query.addProjection(AuditEntity.id().count());

			return ((Long)query.getSingleResult()).intValue();
		}
	}

	@Override
	public List<T> list(Integer limit, Integer start) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.list(limit, start);
		} else {
			return this.list(null, limit, start);
		}
	}

	@Override
	public <S extends T> List<S> list(Class<S> type, Integer limit, Integer start) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.list(type,limit, start);
		} else {
			return this.list(type, limit, start, null,null);
		}
	}

	@Override
	public <S extends T> List<S> list(Class<S> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.list(clazz, limit, start, orderHints, propertyPaths);
		} else {
			AuditQuery query = null;

			if(clazz == null) {
				query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			} else {
				query = getAuditReader().createQuery().forEntitiesAtRevision(clazz,auditEvent.getRevisionNumber());
			}

			addOrder(query,orderHints);

			if(limit != null) {
		   	  query.setMaxResults(limit);
			  query.setFirstResult(start);
			}

			@SuppressWarnings("unchecked")
            List<S> result = query.getResultList();
			defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;
		}
	}

	protected void addOrder(AuditQuery query, List<OrderHint> orderHints) {
		if(orderHints != null && !orderHints.isEmpty()) {
		   for(OrderHint orderHint : orderHints) {
			   orderHint.add(query);
		   }
		}
	}

	@Override
    public List<AuditEventRecord<T>> getAuditEvents(T t, Integer pageSize, Integer pageNumber, AuditEventSort sort, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();

		AuditQuery query = getAuditReader().createQuery().forRevisionsOfEntity(type, false, true);
		query.add(AuditEntity.id().eq(t.getId()));
		if(sort == null) {
		  sort = AuditEventSort.BACKWARDS;
		}

		if(sort.equals(AuditEventSort.BACKWARDS)) {
            if(!auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		  	  query.add(AuditEntity.revisionNumber().lt(auditEvent.getRevisionNumber()));
		    }
		    query.addOrder(AuditEntity.revisionNumber().desc());
     	} else {
     		if(!auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
  		  	  query.add(AuditEntity.revisionNumber().gt(auditEvent.getRevisionNumber()));
  		    }
  		    query.addOrder(AuditEntity.revisionNumber().asc());
     	}

		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}

        /**
         * At the moment we need to transform the data manually
         */
        @SuppressWarnings("unchecked")
        List<Object[]> objs = query.getResultList();
        List<AuditEventRecord<T>> records = new ArrayList<AuditEventRecord<T>>();

        for(Object[] obj : objs) {
        	records.add(new AuditEventRecordImpl<T>(obj));
        }

        for(AuditEventRecord<T> record : records) {
        	defaultBeanInitializer.initialize(record.getAuditableObject(), propertyPaths);
        }
		return records;
	}

	@Override
    public int countAuditEvents(T t, AuditEventSort sort) {
		AuditEvent auditEvent = getAuditEventFromContext();

		AuditQuery query = getAuditReader().createQuery().forRevisionsOfEntity(type, false, true);
		query.add(AuditEntity.id().eq(t.getId()));

		if(!auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			if(sort == null) {
				sort = AuditEventSort.BACKWARDS;
			}

			if(sort.equals(AuditEventSort.BACKWARDS)) {
				query.add(AuditEntity.revisionNumber().lt(auditEvent.getRevisionNumber()));
			} else {
				query.add(AuditEntity.revisionNumber().gt(auditEvent.getRevisionNumber()));
			}
		}

		query.addProjection(AuditEntity.revisionNumber().count());

		return ((Long)query.getSingleResult()).intValue();
	}

	@Override
    public AuditEventRecord<T> getNextAuditEvent(T t) {
		List<AuditEventRecord<T>> auditEvents = getAuditEvents(t,1,0,AuditEventSort.FORWARDS, null);
		if(auditEvents.isEmpty()) {
			return null;
		} else {
		    return auditEvents.get(0);
		}
	}

	@Override
    public AuditEventRecord<T> getPreviousAuditEvent(T t) {
		List<AuditEventRecord<T>> auditEvents = getAuditEvents(t,1,0,AuditEventSort.BACKWARDS, null);
		if(auditEvents.isEmpty()) {
			return null;
		} else {
		    return auditEvents.get(0);
		}
	}

	@Override
    public int countAuditEvents(Class<? extends T> clazz, AuditEvent from,	AuditEvent to, List<AuditCriterion> criteria) {
		AuditQuery query = null;

		if(clazz == null) {
		   query = getAuditReader().createQuery().forRevisionsOfEntity(type, false, true);
		} else {
		   query = getAuditReader().createQuery().forRevisionsOfEntity(clazz, false, true);
		}

		if(from != null) {
			query.add(AuditEntity.revisionNumber().ge(from.getRevisionNumber()));
		}

		if(to != null && !to.equals(AuditEvent.CURRENT_VIEW)) {
			query.add(AuditEntity.revisionNumber().lt(to.getRevisionNumber()));
		}

		addCriteria(query,criteria);

		query.addProjection(AuditEntity.revisionNumber().count());

		int result = ((Long)query.getSingleResult()).intValue();
		return result;
	}

	protected void addCriteria(AuditQuery query, List<AuditCriterion> criteria) {
		if(criteria != null) {
			for(AuditCriterion c : criteria) {
				query.add(c);
			}
		}
	}

	@Override
    public List<AuditEventRecord<T>> getAuditEvents(Class<? extends T> clazz,AuditEvent from, AuditEvent to, List<AuditCriterion> criteria,	Integer pageSize, Integer pageNumber, AuditEventSort sort,	List<String> propertyPaths) {
        AuditQuery query = null;

		if(clazz == null) {
		   query = getAuditReader().createQuery().forRevisionsOfEntity(type, false, true);
		} else {
		   query = getAuditReader().createQuery().forRevisionsOfEntity(clazz, false, true);
		}

		if(from != null) {
			query.add(AuditEntity.revisionNumber().ge(from.getRevisionNumber()));
		}

		if(to != null && !to.equals(AuditEvent.CURRENT_VIEW)) {
			query.add(AuditEntity.revisionNumber().lt(to.getRevisionNumber()));
		}

		if(sort.equals(AuditEventSort.BACKWARDS)) {
		    query.addOrder(AuditEntity.revisionNumber().desc());
     	} else {
  		    query.addOrder(AuditEntity.revisionNumber().asc());
     	}

		addCriteria(query,criteria);

		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}

		/**
         * At the moment we need to transform the data manually
         */
        @SuppressWarnings("unchecked")
        List<Object[]> objs = query.getResultList();
        List<AuditEventRecord<T>> records = new ArrayList<>();

        for(Object[] obj : objs) {
        	records.add(new AuditEventRecordImpl<>(obj));
        }

        for(AuditEventRecord<T> record : records) {
        	defaultBeanInitializer.initialize(record.getAuditableObject(), propertyPaths);
        }
		return records;
	}

	@Override
	protected long countByParam(Class<? extends T> clazz, String param, String queryString, MatchMode matchmode, List<Criterion> criterion) {
    	checkNotInPriorView("IdentifiableDaoBase.findByParam(Class<? extends T> clazz, String queryString, MatchMode matchmode, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");
    	return super.countByParam(clazz, param, queryString, matchmode, criterion);
	}

	@Override
	public int count(T example, Set<String> includeProperties) {
		this.checkNotInPriorView("count(T example, Set<String> includeProperties)");
		return super.count(example, includeProperties);
	}

	@Override
    public List<T> list(T example, Set<String> includeProperties, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		this.checkNotInPriorView("list(T example, Set<String> includeProperties, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {");
		return super.list(example, includeProperties, limit, start, orderHints, propertyPaths);
	}
}
