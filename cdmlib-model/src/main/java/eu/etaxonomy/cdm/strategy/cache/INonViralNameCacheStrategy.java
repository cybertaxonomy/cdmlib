/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import java.util.List;

import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * A name cache rendering strategy for all TaxonNameBase subclasses.
 * Different TaxonNameBase subclasses could have different strategies.
 * @author a.mueller
 *
 * @param <T> The concrete TaxonName class this strategy applies for
 */
public interface INonViralNameCacheStrategy<T extends NonViralName> extends INameCacheStrategy<T> {
	
	/**
	 * returns the composed author string 
	 * @param object
	 * @return
	 */
	public String getAuthorCache(T object);

}
