/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.taxon;

import java.util.Comparator;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Factory for {@link TaxonNode} comparators.
 *
 * @author muellera
 * @since 28.09.2026
 */
public class TaxonNodeComparatorFactory {

    public static Comparator<TaxonNode> bySortMode(TaxonNodeSortMode sortMode) {
        switch (sortMode) {
        case NaturalOrder:
            return new TaxonNodeNaturalComparator();
        case RankAndAlphabeticalOrder:
            return new TaxonNodeByRankAndNameComparator();
        case AlphabeticalOrder:
            return new TaxonNodeByNameComparator();
        default:
            throw new IllegalArgumentException("Unexpected value: " + sortMode);
        }
    }
}