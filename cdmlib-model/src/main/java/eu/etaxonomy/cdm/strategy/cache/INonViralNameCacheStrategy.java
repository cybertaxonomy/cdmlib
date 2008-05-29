/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import eu.etaxonomy.cdm.model.name.NonViralName;

/**
 * A name cache rendering strategy for all TaxonNameBase subclasses.
 * Different TaxonNameBase subclasses could have different strategies.
 * @author a.mueller
 *
 * @param <T> The concrete TaxonName class this strategy applies for
 */
public interface INonViralNameCacheStrategy<T extends NonViralName> extends INameCacheStrategy<T> {
	
	
	/**
	 * returns the composed name string without author or year
	 * @param object
	 * @return
	 */
	public String getNameCache(T taxonNameBase);
	
	/**
	 * returns the composed author string 
	 * @param object
	 * @return
	 */
	public String getAuthorshipCache(T nonViralName);

}
