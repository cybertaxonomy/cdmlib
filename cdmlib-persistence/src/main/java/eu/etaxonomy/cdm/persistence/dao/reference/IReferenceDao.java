/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.reference;

import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ISearchableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;

/**
 * @author a.mueller
 *
 */
public interface IReferenceDao extends IIdentifiableDao<ReferenceBase>, ITitledDao<ReferenceBase>, ISearchableDao<ReferenceBase> {
	public Map<UUID, String> getUuidAndTitleCacheOfReferences();
}
