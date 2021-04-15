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
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * This class orders names of a homotypic group according to the rules defined for
 * {@link HomotypicGroupTaxonComparator the similar taxon comparator} but not
 * including information attached to {@link TaxonBase taxa} only.
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