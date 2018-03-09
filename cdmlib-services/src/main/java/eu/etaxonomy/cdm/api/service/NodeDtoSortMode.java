package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;

import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

public enum NodeDtoSortMode {

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

    NodeDtoSortMode(Class<? extends Comparator<TaxonNodeDto>> type){
	    this.type = type;
	}

    /**
     * @return
     */
    public Comparator<TaxonNodeDto> newComparator() {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
