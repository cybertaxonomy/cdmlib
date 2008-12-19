/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.media;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.IdentificationKey;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

/**
 * @author a.babadshanjan
 * @created 08.09.2008
 */
public interface IMediaDao extends ICdmEntityDao<Media> {
	
	/**
	 * Return a count of IdentificationKeys, optionally filtered by the parameters passed.
	 * 
	 * @param taxonomicScope a Set of Taxon instances that define the taxonomic scope of the key (can be null)
	 * @param geoScopes a Set of NamedArea instances that define the geospatial scope of the key (can be null)
	 * @return a count of IdentificationKey instances
	 */
	public int countIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes);
	
	/**
	 * Return a List of IdentificationKeys, optionally filtered by the parameters passed.
	 * 
	 * @param taxonomicScope a Set of Taxon instances that define the taxonomic scope of the key (can be null)
	 * @param geoScopes a Set of NamedArea instances that define the geospatial scope of the key (can be null)
	 * @param pageSize The maximum number of keys returned (can be null for all keys)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of IdentificationKey instances
	 */
	public List<IdentificationKey> getIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber);

}
