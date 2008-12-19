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
	
	public int countIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes);
	
	public List<IdentificationKey> getIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber);

}
