// $Id$
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

import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dao.common.IVersionableDao;

@Transactional(readOnly = true)
public abstract class VersionableServiceBase<T extends VersionableEntity, DAO extends IVersionableDao<T>> extends ServiceBase<T,DAO> implements IVersionableService<T> {

	 /**
 	 * FIXME candidate for harmonization
	 * rename pageAuditEvents
	 */
	public Pager<AuditEventRecord<T>> getAuditEvents(T t, Integer pageSize,	Integer pageNumber, AuditEventSort sort, List<String> propertyPaths) {
		Integer numberOfResults = dao.countAuditEvents(t, sort);
			
		List<AuditEventRecord<T>> results = new ArrayList<AuditEventRecord<T>>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getAuditEvents(t, pageSize, pageNumber, sort,propertyPaths);
		}
		
		return new DefaultPagerImpl<AuditEventRecord<T>>(pageNumber, numberOfResults, pageSize, results);
	}

	public AuditEventRecord<T> getNextAuditEvent(T t) {
		return dao.getNextAuditEvent(t);
	}

	public AuditEventRecord<T> getPreviousAuditEvent(T t) {
		return dao.getPreviousAuditEvent(t);
	}

	

}
