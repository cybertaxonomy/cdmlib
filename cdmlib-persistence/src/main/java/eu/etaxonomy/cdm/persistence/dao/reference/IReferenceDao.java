/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.reference;

import java.util.List;

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;

/**
 * @author a.mueller
 *
 */
public interface IReferenceDao extends IIdentifiableDao<ReferenceBase>, ITitledDao<ReferenceBase> {
	public List<UuidAndTitleCache<ReferenceBase>> getUuidAndTitle();
	/**
	 * @return all references marked with publish-flag
	 */
	public List<ReferenceBase> getAllReferencesForPublishing();
	
	/**
	 * @return all references not used as nomenclatural reference with publish flag
	 */
	public List<ReferenceBase> getAllNotNomenclaturalReferencesForPublishing();
	
	public List<ReferenceBase> getAllNomenclaturalReferences();
	
}
