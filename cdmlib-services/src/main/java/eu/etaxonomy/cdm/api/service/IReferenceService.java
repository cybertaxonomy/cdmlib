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

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


public interface IReferenceService extends IIdentifiableEntityService<ReferenceBase> {
	
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
	public List<UuidAndTitleCache<ReferenceBase>> getUuidAndTitle();
	
	public List<ReferenceBase> getAllReferencesForPublishing();
	public List<ReferenceBase> getAllNomenclaturalReferences();
}
