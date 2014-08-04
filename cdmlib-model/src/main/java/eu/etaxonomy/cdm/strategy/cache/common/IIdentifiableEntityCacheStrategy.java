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
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.strategy.IStrategy;

/**
 * Base interface for formatting of {@link IdentifiableEntity identifiable entities}. 
 * @author a.mueller
 */
public interface IIdentifiableEntityCacheStrategy<T extends IIdentifiableEntity> extends IStrategy {


	/**
	 * Returns the computed {@link IdentifiableEntity#getTitleCache() title cache}.
	 * @param identifiableEntity the identifiable entity
	 * @return title cache
	 */
	public String getTitleCache(T identifiableEntity);
}
