/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.taxon;

public enum TaxonNodeSortMode {

	/**
	 * Sorts by order defined in the parent's children list.
	 */
	NaturalOrder(),
	/**
     * Sorts by TaxonName titleCaches and rank associated with the taxonNodes
     */
	RankAndAlphabeticalOrder(),
	/**
	 * Sorts by TaxonName titleCaches associated with the taxonNodes
	 */
	AlphabeticalOrder();

    private TaxonNodeSortMode(){}
}
