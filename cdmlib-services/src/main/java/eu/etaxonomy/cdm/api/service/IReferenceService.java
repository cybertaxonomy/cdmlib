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

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


public interface IReferenceService extends IIdentifiableEntityService<ReferenceBase> {
	
	/**
	 * FIXME candidate for harmonization?
	 *  
	 * Finds reference by UUID 
	 */
	public abstract ReferenceBase getReferenceByUuid(UUID uuid);

	/**
	 * FIXME candidate for harmonization?
	 * Finds references by title 
	 */
	public List<ReferenceBase> getReferencesByTitle(String title);
		
	/**
	 * FIXME candidate for harmonization?
	 * Finds references of a certain kind by title 
	 */
	public List<ReferenceBase> getReferencesByTitle(String title, Class<ReferenceBase> clazz);
	
	/**
	 * FIXME candidate for harmonization? 
	 * Gets all references 
	 * @deprecated use {@link #getAllReferences(Integer, Integer) instead
	 */
	@Deprecated
	public abstract List<ReferenceBase> getAllReferences(int limit, int start);
	
	/**
	 * Gets all references ordered by the properties defined by <code>orderHints</code>
	 * @param pageSize the maximum number of entities returned entries per page. Can be null to return all entities.
	 * @param pageNumber a numeric zero based page index 
	 * @param orderHints
	 * @return a Pager instance
	 */
	public Pager<ReferenceBase> getAllReferences(Integer pageSize, Integer pageNumber, List<OrderHint> orderHints);
	
	/**
	 * Gets all references unordered
	 * @param pageSize the maximum number of entities returned entries per page. Can be null to return all entities.
	 * @param pageNumber a numeric zero based page index 
	 * @return a Pager instance
	 */
	public abstract Pager<ReferenceBase> getAllReferences(Integer pageSize, Integer pageNumber);

//	public abstract UUID saveReference(ReferenceBase reference);

	/** 
	 * FIXME candidate for harmonization?
	 * Saves a reference and return its UUID 
	 */
	public abstract UUID saveReference(ReferenceBase reference);

//	public abstract Map<UUID, ReferenceBase> saveReferenceAll(Collection<ReferenceBase> referenceCollection);
	
	/**
	 * FIXME candidate for harmonization? 
	 * Saves a collection of references
	 */
	public abstract Map<UUID, ReferenceBase> saveReferenceAll(Collection<ReferenceBase> referenceCollection);
	
}
