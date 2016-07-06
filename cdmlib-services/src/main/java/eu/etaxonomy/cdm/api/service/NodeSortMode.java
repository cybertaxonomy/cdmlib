package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;

import eu.etaxonomy.cdm.model.taxon.TaxonNaturalComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeByNameComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeByRankAndNameComparator;

public enum NodeSortMode {

	NaturalOrder(TaxonNaturalComparator.class),
	/**
     * sorts by TaxonName titleCaches and rank associated with the taxonNodes
     */
	RankAndAlphabeticalOrder(TaxonNodeByRankAndNameComparator.class),
	/**
	 * sorts by TaxonName titleCaches associated with the taxonNodes
	 */
	AlphabeticalOrder(TaxonNodeByNameComparator.class);

	private Class<? extends Comparator<TaxonNode>> type;

    NodeSortMode(Class<? extends Comparator<TaxonNode>> type){
	    this.type = type;
	}

    /**
     * @return
     */
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
