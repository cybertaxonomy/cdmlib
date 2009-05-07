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
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.IdentificationKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;

public interface IMediaService extends IAnnotatableService<Media> {

	// FIXME candidate for harmonization?
	public abstract Map<UUID, Media> saveMediaAll(Collection<Media> mediaCollection);
	
	// FIXME candidate for harmonization?
	public abstract List<Media> getAllMedia(int limit, int start);

	public abstract List<MediaRepresentation> getAllMediaRepresentations(int limit, int start);

	public abstract List<MediaRepresentationPart> getAllMediaRepresentationParts(int limit, int start);
	
	/**
	 * Return a List of IdentificationKeys, optionally filtered by the parameters passed.
	 * 
	 * @param taxonomicScope a Set of Taxon instances that define the taxonomic scope of the key (can be null)
	 * @param geoScopes a Set of NamedArea instances that define the geospatial scope of the key (can be null)
	 * @param pageSize The maximum number of keys returned (can be null for all keys)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager containing IdentificationKey instances
	 */
	public Pager<IdentificationKey> getIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
	 * Return a Pager of rights belonging to this object
	 * 
	 * @param t The media object
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of Rights entities
	 */
    public Pager<Rights> getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
}
