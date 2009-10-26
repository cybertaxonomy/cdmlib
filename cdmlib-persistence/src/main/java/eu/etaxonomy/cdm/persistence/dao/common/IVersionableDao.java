/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;

public interface IVersionableDao<T extends VersionableEntity> extends ICdmEntityDao<T> {
	
	/**
	 * Returns a list of audit events (in order) which affected the state of an entity t.
	 * The events returned either start at the AuditEvent in context and go forward in time 
	 * (AuditEventSort.FORWARDS) or backwards in time (AuditEventSort.BACKWARDS). If the 
	 * AuditEventContext is set to null, or to AuditEvent.CURRENT_VIEW, then all relevant
	 * AuditEvents are returned.
	 * 
	 * @param t The versionable entity which was affected by the audit events
	 * @param pageSize The maximum number of audit event records returned (can be null for all audit event records)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param sort should the list be sorted going forward in time (AuditEventSort.FORWARDS) or backwards (AuditEventSort.BACKWARDS)
	 * @param propertyPaths paths initialized on the returned audited objects
	 * @return a list of AuditEventRecords, containing the AuditEvent, the state of the entity at that event, and the type of modification
	 */
    public List<AuditEventRecord<T>> getAuditEvents(T t, Integer pageSize, Integer pageNumber, AuditEventSort sort, List<String> propertyPaths);
	
    /**
     * Returns  a count of audit events which affected the state of an entity t.
     * The events either start at the AuditEvent in context and go forward in time 
	 * (AuditEventSort.FORWARDS) or backwards in time (AuditEventSort.BACKWARDS). If the 
	 * AuditEventContext is set to null, or to AuditEvent.CURRENT_VIEW, then all relevant
	 * AuditEvents are considered.
     * 
	 * @param t The versionable entity which was affected by the audit events
     * @param sort should the events considered start now and go forward in time (AuditEventSort.FORWARDS) or backwards (AuditEventSort.BACKWARDS)
     * @return a count of audit events
     */
	public Integer countAuditEvents(T t, AuditEventSort sort);
	
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
