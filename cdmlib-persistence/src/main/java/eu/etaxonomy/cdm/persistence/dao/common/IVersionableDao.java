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

import org.hibernate.envers.query.criteria.AuditCriterion;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.view.AuditEvent;
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
	public long countAuditEvents(T t, AuditEventSort sort);

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

	/**
	 * Returns a count of the total number of audit events affecting objects of class T, optionally restricted to objects of
	 * class clazz, the AuditEvents from and to, inclusive, optionally filtered by other criteria
	 *
	 * @param clazz Restrict the results returned to objects of this class
	 * @param from The audit event to start from (or pass null to start from the beginning of the recordset)
	 * @param to The audit event to continue until (or pass null to return audit events up to the time of the query)
	 * @param criteria Extra criteria to filter by
	 * @return the count of audit events
	 */
	public long countAuditEvents(Class<? extends T> clazz,AuditEvent from,AuditEvent to,List<AuditCriterion> criteria);

	/**
	 * Returns a list of all audit events occurring to objects of type T, optionally restricted to objects of type clazz
	 * between the AuditEvents from and to, inclusive, optionally filtered by other criteria
	 *
	 * @param clazz Restrict the results returned to objects of this class
	 * @param from The audit event to start from (inclusive, or pass null to start from the beginning of the recordset)
	 * @param to The audit event to continue until (exclusive, or pass null to return audit events up to the time of the query)
	 * @param criteria Extra criteria to filter by
	 * @param pageSize The maximum number of objects returned (can be null for all matching objects)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based,
	 *                   can be null, equivalent of starting at the beginning of the recordset)
	 * @param sort Sort the events either forwards or backwards in time
	 * @param propertyPaths properties to be initialized
	 * @return
	 */
	public List<AuditEventRecord<T>> getAuditEvents(Class<? extends T> clazz, AuditEvent from,AuditEvent to,List<AuditCriterion> criteria, Integer pageSize, Integer pageNumber,AuditEventSort sort,List<String> propertyPaths);
}
