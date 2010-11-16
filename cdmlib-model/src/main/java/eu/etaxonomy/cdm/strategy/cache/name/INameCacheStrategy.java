/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import java.util.List;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A name cache rendering strategy for all TaxonNameBase subclasses.
 * Different TaxonNameBase subclasses could have different strategies.
 * 
 * @author a.mueller
 *
 * @param <T> The concrete TaxonName class this strategy applies for
 */
public interface INameCacheStrategy<T extends TaxonNameBase> extends IIdentifiableEntityCacheStrategy<T> {


	/**
	 * Returns an array of name tokens that together make up the full name
	 * a token can be a String (for name parts), Rank, AuthorTeam (for entire authorship string), 
	 * Date or Reference
	 * Example: ["Abies","alba",Rank.SUBSPECIES,"alpina",AuthorTeam("Greuther (L.)")]
	 * 
	 * @param taxonNameBase
	 * @return
	 */
	public List<Object> getTaggedName(T taxonNameBase);
	
	public String getFullTitleCache(T taxonNameBase);
}
