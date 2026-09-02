/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto.compare;

import java.util.Comparator;

import eu.etaxonomy.cdm.compare.taxon.TaxonNodeSortMode;

/**
 * @author muellera
 * @since 02.09.2026
 */
public class TaxonNodeDtoComparatorFactory {

    public static Comparator<ISortableTaxonNodeDto> getDtoComparator(TaxonNodeSortMode sortMode) {
        switch (sortMode) {
        case NaturalOrder :
            return new TaxonNodeDtoNaturalComparator();
        case RankAndAlphabeticalOrder:
            return new TaxonNodeDtoByRankAndNameComparator();
        case AlphabeticalOrder:
            return new TaxonNodeDtoByNameComparator();
        default:
            throw new IllegalArgumentException("Unsupported sort mode: " + sortMode);
        }
    }

}
