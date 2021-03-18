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
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IOriginalSourceDao extends ICdmEntityDao<OriginalSourceBase>{

	/**
	 * Returns a map of identifiable entities of class <code>clazz</code> which have an original source of
	 * with namespace <code>idNamespace</code> and with an idInSource in <code>idInSourceList</code> <BR>
	 * The key of the map is the idInSource. If there are multiple objects that have the same id an arbitrary one is chosen.
	 */
	public <S extends ISourceable> Map<String, S> findOriginalSourcesByIdInSource(Class<S> clazz, Set<String> idInSourceSet, String idNamespace);


	/**
	 * Returns a list of identifiable entities according to their class, idInSource and idNamespace
	 */
	public <S extends ISourceable> List<S> findOriginalSourceByIdInSource(Class<S> clazz, String idInSource, String idNamespace);

	/**
	 * Returns the first OriginalSource with according idInSource and idNamespace
	 */
	public List<OriginalSourceBase> findOriginalSourceByIdInSource(String idInSource, String idNamespace);

    public <T extends NamedSourceBase> List<T> listWithNameUsedInSource(Class<T> clazz, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths);

    public <T extends NamedSourceBase> Long countWithNameUsedInSource(Class<T> clazz);

}
