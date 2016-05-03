/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.media;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationDao;

/**
 * @author a.babadshanjan
 * @created 08.09.2008
 */
@Repository
public class MediaRepresentationDaoImpl extends CdmEntityDaoBase<MediaRepresentation>
implements IMediaRepresentationDao {

	public MediaRepresentationDaoImpl() {
		super(MediaRepresentation.class);
	}
}
