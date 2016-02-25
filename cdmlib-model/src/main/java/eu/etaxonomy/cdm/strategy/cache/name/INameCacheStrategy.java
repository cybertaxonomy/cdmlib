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
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
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
     * Same as {@link #getTaggedTitle(TaxonNameBase)} but not including authorship.
     * @param taxonName
     * @return
     */
    public List<TaggedText> getTaggedName(T taxonName);


    /**
     * Get {@link TaggedText} for the nomenclatural status part
     * @param taxonName
     * @param includeSeparatorBefore if a separator should be added before
     * @param includePostSeparator if a {@link TagEnum#postSeparator post-separator}
     *  should be added after
     * @return
     */
    public List<TaggedText> getNomStatusTags(T taxonName, boolean includeSeparatorBefore,
            boolean includePostSeparator);

    /**
     * Returns the full title cache as a string. The full title cache contains
     * the name cache, followed by the nomencl. reference, followed by the
     * nomencl. status
     * @param taxonNameBase
     * @return
     */
    public String getFullTitleCache(T taxonNameBase);

	/**
	 * Returns the full title cache tagged by html tags according to tag rules.
	 * @param nonViralName
	 * @param htmlTagRules
	 * @return
	 */
	public String getFullTitleCache(T nonViralName, HTMLTagRules htmlTagRules);


    /**
     * Returns the name cache as a string.
     * @param taxonNameBase
     * @return
     */
    public String getNameCache(T taxonNameBase);


	/**
	 * Returns the title cache tagged by html tags according to tag rules.
	 * @param nonViralName
	 * @param htmlTagRules
	 * @return
	 */
	public String getTitleCache(T nonViralName, HTMLTagRules htmlTagRules);

}
