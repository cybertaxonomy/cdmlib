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

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;

public interface IVersionableService<T extends VersionableEntity> extends IService<T> {
	
	/**
	 * Returns a paged list of audit events (in order) which affected the state of an entity t.
	 * The events returned either start at the AuditEvent in context and go forward in time 
	 * (AuditEventSort.FORWARDS) or backwards in time (AuditEventSort.BACKWARDS). If the 
	 * AuditEventContext is set to null, or to AuditEvent.CURRENT_VIEW, then all relevant
	 * AuditEvents are returned.
	 * 
	 * @param t
	 * @param pageSize
	 * @param pageNumber
	 * @param sort
	 * @return a Pager containing audit event instances, plus metadata
	 */
	public Pager<AuditEventRecord<T>> getAuditEvents(T t, Integer pageSize, Integer pageNumber, AuditEventSort sort);
	
	/**
	 * A convenience method which returns a record of the next (relative to the audit event in context)
	 * audit event to affect the entity t.
	 * 
	 * @param t The versionable entity affected by these audit events
	 * @return a record of the next audit event to affect t, or null if the current event is the last to affect t
	 */
    public AuditEventRecord<T> getNextAuditEvent(T t);
	
	/**
	 * A convenience method which returns a record of the previous (relative to the audit event in context)
	 * audit event to affect the entity t.
	 * 
	 * @param t The versionable entity affected by these audit events
	 * @return a record of the previous audit event to affect t, or null if the current event is the first to affect t
	 */
	public AuditEventRecord<T> getPreviousAuditEvent(T t);

}
