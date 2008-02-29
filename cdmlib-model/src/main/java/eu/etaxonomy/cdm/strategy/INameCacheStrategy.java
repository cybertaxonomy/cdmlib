/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import java.util.List;

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

	//returns an array of name tokens that together make up the full name
	// a token can be a String (for name parts), Rank, AuthorTeam (for entire authorship string), 
	// Date or ReferenceBase
	// Example: ["Abies","alba",Rank.SUBSPECIES,"alpina",AuthorTeam("Greuther (L.)")]
	public List<Object> getTaggedName(T object);
}
