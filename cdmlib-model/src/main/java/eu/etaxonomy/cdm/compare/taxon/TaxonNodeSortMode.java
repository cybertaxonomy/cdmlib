package eu.etaxonomy.cdm.compare.taxon;

import java.util.Comparator;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;

public enum TaxonNodeSortMode {

	NaturalOrder(TaxonNodeNaturalComparator.class),
	/**
     * sorts by TaxonName titleCaches and rank associated with the taxonNodes
     */
	RankAndAlphabeticalOrder(TaxonNodeByRankAndNameComparator.class),
	/**
	 * sorts by TaxonName titleCaches associated with the taxonNodes
	 */
	AlphabeticalOrder(TaxonNodeByNameComparator.class);

	private Class<? extends Comparator<TaxonNode>> type;

    TaxonNodeSortMode(Class<? extends Comparator<TaxonNode>> type){
	    this.type = type;
	}

    public Comparator<TaxonNode> newComparator() {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
