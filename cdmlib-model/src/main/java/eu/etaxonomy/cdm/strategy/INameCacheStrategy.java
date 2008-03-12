/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import java.util.List;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * A name cache rendering strategy for all TaxonNameBase subclasses.
 * Different TaxonNameBase subclasses could have different strategies.
 * @author a.mueller
 *
 * @param <T> The concrete TaxonName class this strategy applies for
 */
public interface INameCacheStrategy<T extends TaxonNameBase> extends IStrategy {
	
	/**
	 * returns the composed name string without author or year
	 * @param object
	 * @return
	 */
	public String getNameCache(T object);
	

	/**
	 * returns the composed name string with author and/or year
	 * @param object
	 * @return
	 */
	public String getTitleCache(T object);

	/**
	 * returns an array of name tokens that together make up the full name
	 * a token can be a String (for name parts), Rank, AuthorTeam (for entire authorship string), 
	 * Date or ReferenceBase
	 * Example: ["Abies","alba",Rank.SUBSPECIES,"alpina",AuthorTeam("Greuther (L.)")]
	 * @param object
	 * @return
	 */
	public List<Object> getTaggedName(T object);
}
