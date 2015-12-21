/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.ISourceable;


public interface IOriginalSourceDao extends ICdmEntityDao<OriginalSourceBase>{


	/**
	 * Returns a map of identifiable entities of class <code>clazz</code> which have an original source of 
	 * with namespace <code>idNamespace</code> and with an idInSource in <code>idInSourceList</code> <BR>
	 * The key of the map is the idInSource. If there are multiple objects that have the same id an arbitrary one is chosen.
	 * @param clazz
	 * @param idInSourceList
	 * @param idNamespace
	 * @return
	 */
	public Map<String, ISourceable> findOriginalSourcesByIdInSource(Class clazz, Set<String> idInSourceSet, String idNamespace);
	

	/**
	 * Returns a list of identifiable entities according to their class, idInSource and idNamespace
	 * @param clazz
	 * @param idInSource
	 * @param idNamespace
	 * @return
	 */
	public List<IdentifiableEntity> findOriginalSourceByIdInSource(Class clazz, String idInSource, String idNamespace);

	/**
	 * Returns the first OriginalSource with according idInSource and idNamespace
	 * @param idInSource
	 * @param idNamespace
	 * @return
	 */
	public List<OriginalSourceBase> findOriginalSourceByIdInSource(String idInSource, String idNamespace);

	
}
