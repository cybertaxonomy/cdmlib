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
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.media.Rights;

public interface IIdentifiableDao <T extends IdentifiableEntity> extends IAnnotatableDao<T>, ITitledDao<T>{
	
	/**
	 * Return a count of the sources for this identifiable entity
	 * 
	 * @param identifiableEntity The identifiable entity
	 * @return a count of OriginalSource instances
	 */
	public int countSources(T identifiableEntity);
	
	/**
	 * Return a List of the sources for this identifiable entity
	 * 
	 * @param identifiableEntity The identifiable entity
	 * @param pageSize The maximum number of sources returned (can be null for all sources)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of OriginalSource instances
	 */
	public List<OriginalSource> getSources(T identifiableEntity, Integer pageSize, Integer pageNumber);
	
	/**
	 * Return a count of the rights for this identifiable entity
	 * 
	 * @param identifiableEntity The identifiable entity
	 * @return a count of Rights instances
	 */
    public int countRights(T identifiableEntity);
	
	/**
	 * Return a List of the rights for this identifiable entity
	 * 
	 * @param identifiableEntity The identifiable entity
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of Rights instances
	 */
	public List<Rights> getRights(T identifiableEntity, Integer pageSize, Integer pageNumber);
	
	// TODO Migrated from IOriginalSourceDao
	public List<T> findOriginalSourceByIdInSource(String idInSource, String idNamespace);
}
