package eu.etaxonomy.cdm.compare.taxon;

import java.util.Comparator;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;

public enum TaxonNodeSortMode {

	NaturalOrder(new TaxonNodeNaturalComparator()),
	/**
     * sorts by TaxonName titleCaches and rank associated with the taxonNodes
     */
	RankAndAlphabeticalOrder(new TaxonNodeByRankAndNameComparator()),
	/**
	 * sorts by TaxonName titleCaches associated with the taxonNodes
	 */
	AlphabeticalOrder(new TaxonNodeByNameComparator());

    private Comparator<TaxonNode> comparator;

    private TaxonNodeSortMode(Comparator<TaxonNode> comparator){
        this.comparator = comparator;
    }

    public Comparator<TaxonNode> comparator() {
        return comparator;
    }
}
