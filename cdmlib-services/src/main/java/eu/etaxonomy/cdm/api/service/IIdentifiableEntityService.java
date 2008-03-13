package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public interface IIdentifiableEntityService<T extends IdentifiableEntity> extends IService<T> {

	/**
	 * (Re-)generate the title caches for all objects of this concrete IdentifiableEntity class
	 */
	public abstract void generateTitleCache();

}
