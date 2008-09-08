/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.collection;

import java.util.List;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;

/**
 * @author p.kelbert
 *
 */
public interface ICollectionDao extends IIdentifiableDao<Collection>, ITitledDao<Collection> {
	
	public List<Collection> getCollectionByCode(String code);
	
	
	
}
