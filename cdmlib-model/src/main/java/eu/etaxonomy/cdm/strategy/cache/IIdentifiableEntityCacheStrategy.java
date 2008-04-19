/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.strategy.IStrategy;

/**
 * @author AM
 *
 */
public interface IIdentifiableEntityCacheStrategy<T extends IdentifiableEntity> extends IStrategy {


	/**
	 * returns the composed name string with author and/or year
	 * @param object
	 * @return
	 */
	public String getTitleCache(T object);
}
