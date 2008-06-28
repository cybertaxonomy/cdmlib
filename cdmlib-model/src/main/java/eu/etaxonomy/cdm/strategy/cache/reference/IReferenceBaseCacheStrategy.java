/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache.reference;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A reference cache rendering strategy for all subclasses of ReferenceBase.
 * @author a.mueller
 *
 * @param <T> The concrete ReferenceBase class this strategy applies for
 */
public interface IReferenceBaseCacheStrategy<T extends ReferenceBase> extends IIdentifiableEntityCacheStrategy<T> {
	


}
