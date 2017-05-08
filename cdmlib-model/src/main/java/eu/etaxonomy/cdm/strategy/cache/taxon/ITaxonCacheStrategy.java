/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.taxon;

import java.util.List;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A name cache rendering strategy for all TaxonName subclasses.
 * Different TaxonName subclasses could have different strategies.
 *
 * @author a.mueller
 *
 * @param <T> The concrete TaxonName class this strategy applies for
 */
public interface ITaxonCacheStrategy<T extends TaxonBase> extends IIdentifiableEntityCacheStrategy<T> {

    /**
     * Returns a list of name typified tokens that together make up the name (including authorship etc.).
     * A token (taggedText) is a string and a type which indicates which part of a name the text
     * belongs to. Types may be name (indicating a core part of the name, e.g. a name epithet),
     * author (indicating an authorship part), rank, reference, etc.).
     * <BR>
     * Example: ["Abies"/name,"alba"/name,Rank.SUBSPECIES/rank,"alpina"/name,
     * "Greuther (L.)"/authorship]
     *
     * @param taxonName
     * @return the tagged list, <code>null</code> if taxonName is <code>null</code>
     */
    public List<TaggedText> getTaggedTitle(T taxonBase);


	/**
	 * Returns the title cache tagged by html tags according to tag rules.
	 * @param nonViralName
	 * @param htmlTagRules
	 * @return
	 */
	public String getTitleCache(T taxonBase, HTMLTagRules htmlTagRules);

}
