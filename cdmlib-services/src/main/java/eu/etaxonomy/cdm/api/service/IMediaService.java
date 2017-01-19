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
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.MediaDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IMediaService extends IIdentifiableEntityService<Media> {

	/**
	 * Return a List of MediaKeys, optionally filtered by the parameters passed.
	 *
	 * @param taxonomicScope a Set of Taxon instances that define the taxonomic scope of the key (can be null)
	 * @param geoScopes a Set of NamedArea instances that define the geospatial scope of the key (can be null)
	 * @param pageSize The maximum number of keys returned (can be null for all keys)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager containing MediaKey instances
	 */
	public Pager<MediaKey> getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
	 * Return a Pager of rights belonging to this object
	 *
	 * @param t The media object
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of Rights entities
	 */
    @Override
    public Pager<Rights> getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
	 * Returns a Paged List of Media instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 *
	 * @param clazz filter the results by class (or pass null to return all Media instances)
	 * @param queryString
	 * @param pageSize The maximum number of media returned (can be null for all matching media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager Media instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	@Override
    public Pager<Media> search(Class<? extends Media> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


	public DeleteResult delete(UUID mediaUuid, MediaDeletionConfigurator config);
}
