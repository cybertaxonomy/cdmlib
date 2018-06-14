/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.view.IAuditEventDao;

@Service
@Transactional(readOnly = true)
public class AuditEventService implements IAuditEventService {

	IAuditEventDao dao;

	@Autowired
	public void setDao(IAuditEventDao dao) {
		this.dao = dao;
	}

	@Override
    public boolean exists(UUID uuid) {
		return dao.exists(uuid);
	}

	@Override
    public AuditEvent find(Integer id) {
		return dao.findById(id);
	}

	@Override
    public AuditEvent find(UUID uuid) {
		return dao.findByUuid(uuid);
	}

	@Override
    public AuditEvent getNextAuditEvent(AuditEvent auditEvent) {
		return dao.getNextAuditEvent(auditEvent);
	}

	@Override
    public AuditEvent getPreviousAuditEvent(AuditEvent auditEvent) {
		return dao.getPreviousAuditEvent(auditEvent);
	}

	@Override
    public Pager<AuditEvent> list(Integer pageNumber, Integer pageSize,	AuditEventSort sort) {
		long numberOfResults = dao.count();

		List<AuditEvent> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.list(pageNumber, pageSize, sort);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
    public AuditEvent find(DateTime dateTime) {
		return dao.findByDate(dateTime);
	}
}
