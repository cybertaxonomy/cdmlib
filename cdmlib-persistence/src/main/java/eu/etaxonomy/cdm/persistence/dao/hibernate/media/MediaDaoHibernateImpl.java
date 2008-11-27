/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.media;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;

/**
 * @author a.babadshanjan
 * @created 08.09.2008
 */
@Repository
public class MediaDaoHibernateImpl extends CdmEntityDaoBase<Media> 
	implements IMediaDao {

	public MediaDaoHibernateImpl() {
		super(Media.class);
	}
}
