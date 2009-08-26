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
import java.util.Map;
import java.util.UUID;

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
	
	/**
	 * Returns a Paged List of ReferenceBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param clazz filter the results by class (or pass null to return all ReferenceBase instances)
	 * @param queryString
	 * @param pageSize The maximum number of references returned (can be null for all matching references)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager ReferenceBase instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public Pager<ReferenceBase> search(Class<? extends ReferenceBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns a map that holds uuid, titleCache pairs of all references in the current database
	 * 
	 * @return 
	 * 			a <code>Map</code> containing uuid and titleCache of references
	 */
	public Map<UUID, String> getUuidAndTitleCacheOfReferences();
	
}
