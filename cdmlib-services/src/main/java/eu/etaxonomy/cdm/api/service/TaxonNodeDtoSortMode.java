package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;

import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDtoByNameComparator;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDtoByRankAndNameComparator;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDtoNaturalComparator;

public enum TaxonNodeDtoSortMode {

	NaturalOrder(TaxonNodeDtoNaturalComparator.class),
	/**
     * sorts by TaxonName titleCaches and rank associated with the taxonNodes
     */
	RankAndAlphabeticalOrder(TaxonNodeDtoByRankAndNameComparator.class),
	/**
	 * sorts by TaxonName titleCaches associated with the taxonNodes
	 */
	AlphabeticalOrder(TaxonNodeDtoByNameComparator.class);

	private Class<? extends Comparator<TaxonNodeDto>> type;

    private TaxonNodeDtoSortMode(Class<? extends Comparator<TaxonNodeDto>> type){
	    this.type = type;
	}

    public Comparator<TaxonNodeDto> newComparator() {
        try {
            return type.newInstance();
        } catch (InstantiationException |IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
