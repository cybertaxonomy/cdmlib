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
import eu.etaxonomy.cdm.strategy.TaggedText;
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
	 * Returns an array of name tokens that together make up the full name.
	 * A token can be a String (for name parts), Rank, AuthorTeam (for entire authorship string), 
	 * Date or Reference
	 * Example: ["Abies","alba",Rank.SUBSPECIES,"alpina",AuthorTeam("Greuther (L.)")]
	 * 
	 * @param taxonNameBase
	 * @return
	 */
	public List<Object> getTaggedNameDeprecated(T taxonNameBase);
	
	/**
	 * Returns a list of name typified tokens that together make up the name (including authorship etc.).
	 * A token (taggedText) is a string and a type which indicates which part of a name the text
	 * belongs to. Types may be name (indicating a core part of the name, e.g. a name epithet),
	 * author (indicating an authorship part), rank, reference, etc.).
	 * <BR>
	 * Example: ["Abies"/name,"alba"/name,Rank.SUBSPECIES/rank,"alpina"/name,
	 * "Greuther (L.)"/authorship]
	 * 
	 * @param taxonNameBase
	 * @return the tagged list, <code>null</code> if taxonName is <code>null</code>
	 */
	public List<TaggedText> getTaggedTitle(T taxonName);
	
	/**
	 * Same as {@link #getTaggedTitle(TaxonNameBase)} but also includes the reference and
	 * the nomenclatural status in the result. 
	 * @param taxonName
	 * @return
	 */
	public List<TaggedText> getTaggedFullTitle(T taxonName);
	
	
	/**
	 * Returns the full title cache as a string.
	 * @param taxonNameBase
	 * @return
	 */
	public String getFullTitleCache(T taxonNameBase);
}
