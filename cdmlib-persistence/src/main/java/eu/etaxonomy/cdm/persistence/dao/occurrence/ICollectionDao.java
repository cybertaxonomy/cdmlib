/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.occurrence;

import java.util.List;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public interface ICollectionDao extends IIdentifiableDao<Collection> {
	
	/**
	 * Returns a list of Collection instances matching the code supplied
	 * 
	 * @param code The code 
	 * @return a List of Collection instances
	 */
	public List<Collection> getCollectionByCode(String code);
}
