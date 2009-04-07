package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.model.view.AuditEventRecordImpl;
import eu.etaxonomy.cdm.model.view.context.AuditEventContext;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dao.common.IVersionableDao;
import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;

public abstract class VersionableDaoBase<T extends VersionableEntity> extends CdmEntityDaoBase<T> implements IVersionableDao<T> {
	
	private static Log log = LogFactory.getLog(VersionableDaoBase.class);
	
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
	  	    return auditEvent;
	    } else {
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
			// TODO initialize bits
			return (T)query.getSingleResult();			
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
			return null != (T)query.getSingleResult();			
		}
	}
	
	@Override
	public int count() {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.count();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.addProjection(AuditEntity.id().count("id"));
			return ((Long)query.getSingleResult()).intValue();
		}
	}
	
	@Override
	public <TYPE extends T> int count(Class<TYPE> type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.count(type);
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.addProjection(AuditEntity.id().count("id"));
			return ((Long)query.getSingleResult()).intValue();
		}
	}
	
	@Override
	public List<T> list(Integer limit, Integer start) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.list(limit, start);
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			if(limit != null) {
		   	  query.setMaxResults(limit);
			  query.setFirstResult(start);
			}
			return (List<T>)query.getResultList();		
		}
	}
	
	@Override
	public <TYPE extends T> List<TYPE> list(Class<TYPE> type, Integer limit, Integer start) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			return super.list(type,limit, start);
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			if(limit != null) {
		   	  query.setMaxResults(limit);
			  query.setFirstResult(start);
			}
			return (List<TYPE>)query.getResultList();
		}
	}
	
	public List<AuditEventRecord<T>> getAuditEvents(T t, Integer pageSize, Integer pageNumber, AuditEventSort sort) {
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
        List<Object[]> objs = (List<Object[]>)query.getResultList();
        List<AuditEventRecord<T>> records = new ArrayList<AuditEventRecord<T>>();
        
        for(Object[] obj : objs) {
        	records.add(new AuditEventRecordImpl<T>(obj));
        }
        
		return records;
	}
	
	public Integer countAuditEvents(T t, AuditEventSort sort) {
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
	
	public AuditEventRecord<T> getNextAuditEvent(T t) {
		List<AuditEventRecord<T>> auditEvents = getAuditEvents(t,1,0,AuditEventSort.FORWARDS);
		if(auditEvents.isEmpty()) {
			return null;
		} else {
		    return auditEvents.get(0);
		}
	}
	
	public AuditEventRecord<T> getPreviousAuditEvent(T t) {
		List<AuditEventRecord<T>> auditEvents = getAuditEvents(t,1,0,AuditEventSort.BACKWARDS);
		if(auditEvents.isEmpty()) {
			return null;
		} else {
		    return auditEvents.get(0);
		}
	}
}
