/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author a.mueller
 *
 */
public interface INameCacheStrategy<T extends TaxonNameBase> extends IStrategy {
	
	//returns the composed name string without author or year
	public String getNameCache(T object);
	
	//returns the composed name string with author and/or year
	public String getTitleCache(T object);
	
}
