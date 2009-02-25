package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.view.IAuditEventDao;

@Service
public class AuditEventService implements IAuditEventService {
	
	IAuditEventDao dao;
	
	@Autowired
	public void setDao(IAuditEventDao dao) {
		this.dao = dao;
	}

	public boolean exists(UUID uuid) {
		return dao.exists(uuid);
	}

	public AuditEvent findById(Integer id) {
		return dao.findById(id);
	}

	public AuditEvent findByUuid(UUID uuid) {
		return dao.findByUuid(uuid);
	}

	public AuditEvent getNextAuditEvent(AuditEvent auditEvent) {
		return dao.getNextAuditEvent(auditEvent);
	}

	public AuditEvent getPreviousAuditEvent(AuditEvent auditEvent) {
		return dao.getPreviousAuditEvent(auditEvent);
	}

	public Pager<AuditEvent> list(Integer pageNumber, Integer pageSize,	AuditEventSort sort) {
		 Integer numberOfResults = dao.count();
			
		List<AuditEvent> results = new ArrayList<AuditEvent>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.list(pageNumber, pageSize, sort); 
		}
			
		return new DefaultPagerImpl<AuditEvent>(pageNumber, numberOfResults, pageSize, results);
	}
}
