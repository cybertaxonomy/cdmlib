/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.view;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.view.IAuditEventDao;

@Repository
public class AuditEventDao extends DaoBase implements IAuditEventDao {

	protected AuditReader getAuditReader() {
		return AuditReaderFactory.get(getSession());
	}

	@Override
    public int count() {
		Query query = getSession().createQuery("select count(auditEvent) from AuditEvent auditEvent");

		return ((Long)query.uniqueResult()).intValue();
	}

	@Override
    public boolean exists(UUID uuid) {
		Query query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.uuid = :uuid");
		query.setParameter("uuid", uuid);
		return null != (AuditEvent)query.uniqueResult();
	}

	@Override
    public AuditEvent findById(Integer id) {
		Query query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.id = :id");
		query.setParameter("id", id);
		return (AuditEvent)query.uniqueResult();
	}

	@Override
    public AuditEvent findByUuid(UUID uuid) {
		Query query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.uuid = :uuid");
		query.setParameter("uuid", uuid);
		return (AuditEvent)query.uniqueResult();
	}

	@Override
    public AuditEvent getNextAuditEvent(AuditEvent auditEvent) {
		Query query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.revisionNumber = :revisionNumber + 1");
		query.setParameter("revisionNumber", auditEvent.getRevisionNumber());
		return (AuditEvent) query.uniqueResult();
	}

	@Override
    public AuditEvent getPreviousAuditEvent(AuditEvent auditEvent) {
		Query query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.revisionNumber = :revisionNumber - 1");
		query.setParameter("revisionNumber", auditEvent.getRevisionNumber());
		return (AuditEvent) query.uniqueResult();
	}

	@Override
    public List<AuditEvent> list(Integer pageNumber, Integer pageSize, AuditEventSort sort) {
		if(sort == null) {
			sort = AuditEventSort.BACKWARDS;
		}

		Query query = null;

		if(sort.equals(AuditEventSort.FORWARDS)) {
			query = getSession().createQuery("select auditEvent from AuditEvent auditEvent order by auditEvent.timestamp asc");
		} else {
			query = getSession().createQuery("select auditEvent from AuditEvent auditEvent order by auditEvent.timestamp desc");
		}

		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}

		return query.list();
	}

	public AuditEvent findByDate(ZonedDateTime dateTime) {
		Number id = getAuditReader().getRevisionNumberForDate(java.util.Date.from ( dateTime.toInstant() ));
		AuditEvent auditEvent  =getSession().load(AuditEvent.class, id);
		Hibernate.initialize(auditEvent);
		return auditEvent;
	}
}
