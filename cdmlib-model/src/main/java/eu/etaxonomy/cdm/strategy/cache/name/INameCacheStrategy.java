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

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
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
public interface INameCacheStrategy extends IIdentifiableEntityCacheStrategy<TaxonName> {

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
    public List<TaggedText> getTaggedTitle(TaxonName taxonName);

    /**
     * Same as {@link #getTaggedTitle(TaxonName)} but also includes the reference and
     * the nomenclatural status in the result.
     * @param taxonName
     * @return
     */
    public List<TaggedText> getTaggedFullTitle(TaxonName taxonName);


    /**
     * Get {@link TaggedText} for the nomenclatural status part
     * @param taxonName
     * @param includeSeparatorBefore if a separator should be added before
     * @param includePostSeparator if a {@link TagEnum#postSeparator post-separator}
     *  should be added after
     * @return
     */
    public List<TaggedText> getNomStatusTags(TaxonName taxonName, boolean includeSeparatorBefore,
            boolean includePostSeparator);

    /**
     * Returns the full title cache as a string. The full title cache contains
     * the name cache, followed by the nomencl. reference, followed by the
     * nomencl. status
     * @param taxonName
     * @return
     */
    public String getFullTitleCache(TaxonName taxonName);

	/**
	 * Returns the full title cache tagged by html tags according to tag rules.
	 * @param nonViralName
	 * @param htmlTagRules
	 * @return
	 */
	public String getFullTitleCache(TaxonName nonViralName, HTMLTagRules htmlTagRules);


	/**
	 * Returns the title cache tagged by html tags according to tag rules.
	 * @param nonViralName
	 * @param htmlTagRules
	 * @return
	 */
	public String getTitleCache(TaxonName nonViralName, HTMLTagRules htmlTagRules);

	/**
     * Returns the composed scientific taxon name string without authors or year.
     * For viral names this returns <code>null</code>. This may be discussed
     * as for some functionality the nameCache is crucial and therefore
     * we maybe prefer to return the titleCache instead.
     */
    public String getNameCache(TaxonName taxonName);

    /**
     * Returns the composed scientific taxon name string without authors or year.
     * Like {@link #getNameCache(TaxonName)} but using the given tag rules.
     */
    public String getNameCache(TaxonName nonViralName, HTMLTagRules htmlTagRules);

    /**
     * Same as {@link #getTaggedTitle(TaxonName)} but not including authorship.
     * @param taxonName
     * @return
     */
    public List<TaggedText> getTaggedName(TaxonName taxonName);

    /**
     * Returns the composed author string. Returns null for viral names.
     * @param object
     * @return
     */
    public String getAuthorshipCache(TaxonName nonViralName);


    /**
     * If a name has more authors then this position
     * add "& al." instead at the given author position.
     */
    public Integer getEtAlPosition();
    /**
     * @see #getEtAlPosition()
     * @param etAlPosition
     */
    public void setEtAlPosition(Integer etAlPosition);

}
