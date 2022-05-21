/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.view;

import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
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
    public long count() {
		Query<Long> query = getSession().createQuery("select count(auditEvent) from AuditEvent auditEvent", Long.class);
		return query.uniqueResult();
	}

	@Override
    public boolean exists(UUID uuid) {
		Query<AuditEvent> query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.uuid = :uuid", AuditEvent.class);
		query.setParameter("uuid", uuid);
		return null != query.uniqueResult();
	}

	@Override
    public AuditEvent findById(Integer id) {
		Query<AuditEvent> query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.id = :id", AuditEvent.class);
		query.setParameter("id", id);
		return query.uniqueResult();
	}

	@Override
    public AuditEvent findByUuid(UUID uuid) {
		Query<AuditEvent> query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.uuid = :uuid", AuditEvent.class);
		query.setParameter("uuid", uuid);
		return query.uniqueResult();
	}

	@Override
    public AuditEvent getNextAuditEvent(AuditEvent auditEvent) {
		Query<AuditEvent> query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.revisionNumber = :revisionNumber + 1", AuditEvent.class);
		query.setParameter("revisionNumber", auditEvent.getRevisionNumber());
		return query.uniqueResult();
	}

	@Override
    public AuditEvent getPreviousAuditEvent(AuditEvent auditEvent) {
		Query<AuditEvent> query = getSession().createQuery("select auditEvent from AuditEvent auditEvent where auditEvent.revisionNumber = :revisionNumber - 1", AuditEvent.class);
		query.setParameter("revisionNumber", auditEvent.getRevisionNumber());
		return query.uniqueResult();
	}

	@Override
    public List<AuditEvent> list(Integer pageNumber, Integer pageSize, AuditEventSort sort) {
		if(sort == null) {
			sort = AuditEventSort.BACKWARDS;
		}

		Query<AuditEvent> query = null;

		if(sort.equals(AuditEventSort.FORWARDS)) {
			query = getSession().createQuery("select auditEvent from AuditEvent auditEvent order by auditEvent.timestamp asc", AuditEvent.class);
		} else {
			query = getSession().createQuery("select auditEvent from AuditEvent auditEvent order by auditEvent.timestamp desc", AuditEvent.class);
		}

		this.addPageSizeAndNumber(query, pageSize, pageNumber);
		return query.list();
	}

	@Override
    public AuditEvent findByDate(DateTime dateTime) {
		Number id = getAuditReader().getRevisionNumberForDate(dateTime.toDate());
		AuditEvent auditEvent  =getSession().load(AuditEvent.class, id);
		Hibernate.initialize(auditEvent);
		return auditEvent;
	}
}
