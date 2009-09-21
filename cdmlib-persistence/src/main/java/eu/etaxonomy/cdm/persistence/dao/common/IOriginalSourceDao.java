/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;


public interface IOriginalSourceDao extends ICdmEntityDao<OriginalSourceBase>{
	

	public List<IdentifiableEntity> findOriginalSourceByIdInSource(Class clazz, String idInSource, String idNamespace);

	/**
	 * Returns the first OriginalSource with according idInSource and idNamespace
	 * @param idInSource
	 * @param idNamespace
	 * @return
	 */
	public List<OriginalSourceBase> findOriginalSourceByIdInSource(String idInSource, String idNamespace);

	
}
