/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;

import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDtoByNameComparator;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDtoByRankAndNameComparator;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDtoNaturalComparator;

public enum TaxonNodeDtoSortMode {

	NaturalOrder(new TaxonNodeDtoNaturalComparator()),
	/**
     * sorts by TaxonName titleCaches and rank associated with the taxonNodes
     */
	RankAndAlphabeticalOrder(new TaxonNodeDtoByRankAndNameComparator()),
	/**
	 * sorts by TaxonName titleCaches associated with the taxonNodes
	 */
	AlphabeticalOrder(new TaxonNodeDtoByNameComparator());

	private Comparator<TaxonNodeDto> comparator;

    private TaxonNodeDtoSortMode(Comparator<TaxonNodeDto> comparator){
	    this.comparator = comparator;
	}

    public Comparator<TaxonNodeDto> comparator() {
        return comparator;
    }
}
