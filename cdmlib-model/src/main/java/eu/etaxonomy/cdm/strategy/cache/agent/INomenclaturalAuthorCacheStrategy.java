/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.agent;


import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public interface INomenclaturalAuthorCacheStrategy<T extends TeamOrPersonBase> extends IIdentifiableEntityCacheStrategy<T> {
	
	/**
	 * returns the composed name string without author or year
	 * @param object
	 * @return
	 */
	public String getNomenclaturalTitle(T object);


}
