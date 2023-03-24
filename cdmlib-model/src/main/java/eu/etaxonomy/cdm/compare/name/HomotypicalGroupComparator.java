/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.name;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.compare.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.compare.taxon.TaxonComparator;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Comparator to compare 2 homotypic groups.
 *
 * @author a.mueller
 * @date 15.01.2023
 */
public class HomotypicalGroupComparator implements
		Comparator<HomotypicalGroup>, Serializable {

    private static final long serialVersionUID = -676465815899137107L;

    @Override
	public int compare(HomotypicalGroup group1, HomotypicalGroup group2) {
		TaxonBase<?> firstTypified1 = null;
		TaxonBase<?> firstTypified2 = null;
		if (group1.equals(group2)){
		    return 0;
		}
		TaxonComparator taxComparator = new HomotypicGroupTaxonComparator(null);
		Set<TaxonName> typifiedNames1 = group1.getTypifiedNames();
		List<TaxonBase> taxonBasesOfTypifiedNames = new ArrayList<>();
		for (TaxonName typifiedName:typifiedNames1){
			if (!typifiedName.getTaxonBases().isEmpty()){
				taxonBasesOfTypifiedNames.add(typifiedName.getTaxonBases().iterator().next());
			}
		}
		Collections.sort(taxonBasesOfTypifiedNames, taxComparator);
		firstTypified1 = taxonBasesOfTypifiedNames.get(0);

		Set<TaxonName> typifiedNames2 = group2.getTypifiedNames();
		taxonBasesOfTypifiedNames = new ArrayList<>();
		for (TaxonName typifiedName:typifiedNames2){
			if (!typifiedName.getTaxonBases().isEmpty()){
				taxonBasesOfTypifiedNames.add(typifiedName.getTaxonBases().iterator().next());
			}
		}
		Collections.sort(taxonBasesOfTypifiedNames, taxComparator);
		firstTypified2 = taxonBasesOfTypifiedNames.get(0);
		return taxComparator.compare(firstTypified1, firstTypified2);
	}
}