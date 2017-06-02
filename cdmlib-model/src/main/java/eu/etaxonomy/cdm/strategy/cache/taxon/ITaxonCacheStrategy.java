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

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
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
public interface ITaxonCacheStrategy<T extends TaxonBase>
        extends IIdentifiableEntityCacheStrategy<T> {

    /**
     * Returns a list of typified tokens that together make up the title of
     * a taxon or synonym including the name part and the secundum part
     *
     * @param taxonBase accepted taxon or synonym
     * @return the tagged list, <code>null</code> if taxonBase is <code>null</code>
     */
    public List<TaggedText> getTaggedTitle(T taxonBase);


	/**
	 * Returns the title cache tagged by html tags according to tag rules.
	 * @param nonViralName
	 * @param htmlTagRules
	 * @return
	 */
	public String getTitleCache(T taxonBase, HTMLTagRules htmlTagRules);


    /**
     * Returns a list of typified tokens that together make up the title of
     * a taxon if used as a misapplication
     *
     * @param taxon the misapplied taxon
     * @return the tagged list, <code>null</code> if taxon is <code>null</code>
     */
	public List<TaggedText> getMisappliedTaggedTitle(Taxon taxon);

	/**
	 * Computes the title of an {@link Taxon accepted taxon} if used as
	 * misapplication
	 * @param taxon the misapplied taxon
	 * @return the misapplied title
	 */
	public String getMisappliedTitle(Taxon taxon);

}
