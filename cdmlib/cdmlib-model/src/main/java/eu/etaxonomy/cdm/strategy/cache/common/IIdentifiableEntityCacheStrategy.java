/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.common;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.strategy.IStrategy;

/**
 * @author AM
 *
 */
public interface IIdentifiableEntityCacheStrategy<T extends IIdentifiableEntity> extends IStrategy {


	/**
	 * 
	 * @param object
	 * @return
	 */
	public String getTitleCache(T object);
}
