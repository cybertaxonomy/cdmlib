/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.name;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.compare.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * This class orders synonyms of a homotypic group,
 * first by
 * <ul>
 *  <li>Basionym groups (the basionym and all names derived from this basionym)
 *      should be kept together in a subgroup</li>
 *  <li>The order of the subgroups is defined by the ordering of their
 *       basionyms (according to the following ordering)</li>
 *  <li>If a name is illegitimate or not does play a role for ordering</li>
 *  <li>Names with publication year should always come first</li>
 *  <li>Names with no publication year are sorted by rank</li>
 *  <li>Names with no publication year and equal rank are sorted alphabetically</li>
 *  <li>If 2 names have a replaced synonym relationship the replaced synonym comes first,
 *      the replacement name comes later as this reflects the order of publication</li>
 *  </ul>
 *
 * Details on ordering are explained at https://dev.e-taxonomy.eu/redmine/issues/3338<BR>
 *
 * @author k.luther
 * @since 20.03.2017
 *
 */
public class HomotypicalGroupNameComparator implements Comparator<TaxonName>, Serializable{

    private static final long serialVersionUID = 2306033011970230866L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(HomotypicalGroupNameComparator.class);

    private HomotypicGroupTaxonComparator taxonComparator;

    public HomotypicalGroupNameComparator(TaxonName firstNameInGroup, boolean includeRanks) {
        taxonComparator = new HomotypicGroupTaxonComparator(firstNameInGroup, includeRanks);
    }

    @Override
    public int compare(TaxonName taxonName1, TaxonName taxonName2) {
        return taxonComparator.compareNames(taxonName1, taxonName2, null, null);
    }
}