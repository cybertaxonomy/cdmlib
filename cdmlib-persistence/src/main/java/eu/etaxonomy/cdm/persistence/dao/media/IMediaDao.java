/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.media;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;

/**
 * @author a.babadshanjan
 * @since 08.09.2008
 */
public interface IMediaDao extends IIdentifiableDao<Media> {

	/**
	 * Return a count of MediaKeys, optionally filtered by the parameters passed.
	 *
	 * @param taxonomicScope a Set of Taxon instances that define the taxonomic scope of the key (can be null)
	 * @param geoScopes a Set of NamedArea instances that define the geospatial scope of the key (can be null)
	 * @return a count of MediaKey instances
	 */
	public long countMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes);

	/**
	 * Return a List of MediaKeys, optionally filtered by the parameters passed. The MediaKey
	 * instances have the following properties initialized:
	 *
	 * MediaKey.title
	 *
	 * @param taxonomicScope a Set of Taxon instances that define the taxonomic scope of the key (can be null)
	 * @param geoScopes a Set of NamedArea instances that define the geospatial scope of the key (can be null)
	 * @param pageSize The maximum number of keys returned (can be null for all keys)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of MediaKey instances
	 */
	public List<MediaKey> getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
	 * Return a count of the rights for this media entity
	 *
	 * @param t The media entity
	 * @return a count of Rights instances
	 */
    @Override
    public long countRights(Media t);

	/**
	 * Return a List of the rights for this media entity
	 *
	 * @param t The media entity
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of Rights instances
	 */
	@Override
    public List<Rights> getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

}
