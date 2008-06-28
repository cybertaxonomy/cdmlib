/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache.common;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.strategy.IStrategy;

/**
 * @author AM
 *
 */
public interface IIdentifiableEntityCacheStrategy<T extends IdentifiableEntity> extends IStrategy {


	/**
	 * 
	 * @param object
	 * @return
	 */
	public String getTitleCache(T object);
}
