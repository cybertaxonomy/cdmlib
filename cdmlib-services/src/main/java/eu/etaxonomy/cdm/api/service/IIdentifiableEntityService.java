/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

public interface IIdentifiableEntityService<T extends IdentifiableEntity> extends IService<T> {

	/**
	 * (Re-)generate the title caches for all objects of this concrete IdentifiableEntity class
	 */
	public abstract void generateTitleCache();

}
