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

import java.util.List;
import java.util.UUID;

import org.hibernate.envers.query.criteria.AuditCriterion;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.view.AuditEvent;
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
	 * @param propertyPaths paths initialized on the returned objects - only applied to the objects returned from the first grouping
	 * @return a Pager containing audit event instances, plus metadata
	 */
	public Pager<AuditEventRecord<T>> pageAuditEvents(T t, Integer pageSize, Integer pageNumber, AuditEventSort sort, List<String> propertyPaths);

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
	public Pager<AuditEventRecord<T>> pageAuditEvents(Class<? extends T> clazz,AuditEvent from,AuditEvent to, List<AuditCriterion> criteria, Integer pageSize, Integer pageValue, AuditEventSort sort,List<String> propertyPaths);


    /**
	 * checks whether the object is deletable concerning the configurator or not
	 */

	public DeleteResult isDeletable(UUID object, DeleteConfiguratorBase config);

}
