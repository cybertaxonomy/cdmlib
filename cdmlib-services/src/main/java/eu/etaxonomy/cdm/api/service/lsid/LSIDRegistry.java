/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid;

import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;


/**
 * This object contains the mappings between lsids (in particular, the authority)
 * and the DAO which provides access to the data on those objects held by this application/
 * 
 * The thinking behind this is that a CATE application can hold data on objects, (for example
 * Scientific Names) which are identified by an LSID belonging to another authority. However, all 
 * Scientific Names are held as org.cateproject.model.ScientificName objects, and are accessed 
 * via an instance of org.cateproject.persistence.dao.ScientificNameDAO. This class resolves the
 * one-to-many mapping between authorities and IdentifiableDAO's
 * 
 * @author ben
 *
 * @see org.cateproject.model.lsid.Identifiable
 * @see org.cateproject.persistence.dao.common.IdentifiableDAO
 */
public interface LSIDRegistry {
	/**
	 * Find the DAO associated with objects with this authority:namespace combination
	 * or null if none exists
	 * 
	 * @param LSID lsid the lsid in question
	 * @return IdentifiableDAO the dao which provides access to objects with this authority:namespace combination or null if none exists
	 */
	public IIdentifiableDao lookupDAO(LSID lsid);
}
