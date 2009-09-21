// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;

public interface IIdentifiableEntityService<T extends IdentifiableEntity> extends IAnnotatableService<T> {

	/**
	 * (Re-)generate the title caches for all objects of this concrete IdentifiableEntity class
	 */
	public abstract void generateTitleCache();

	/**
	 * Return a Pager of sources belonging to this object
	 * 
	 * @param t The identifiable entity
	 * @param pageSize The maximum number of sources returned (can be null for all sources)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of OriginalSource entities
	 */
    public Pager<IdentifiableSource> getSources(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
    
	/**
	 * Return a Pager of rights belonging to this object
	 * 
	 * @param t The identifiable entity
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of Rights entities
	 */
    public Pager<Rights> getRights(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
    
    public abstract ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace);
    
	/**
	 * Return a list of all uuids mapped to titleCache in the convenient <code>UuidAndTitleCache</code> object.
	 * Retrieving this list is considered to be significantly faster than initializing the fully fledged buiseness
	 * objects. To be used in cases where you want to present large amount of data and provide details after 
	 * a selection has been made.  
	 * 
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<T>> getUuidAndTitleCache(); 
}
