/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.io.Serializable;
import java.util.Comparator;

import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author k.luther
 * @date 18.03.2010
 *
 */
public class TaxonComparatorSearch implements Serializable, Comparator<TaxonBase> {

	public int compare(TaxonBase taxon1, TaxonBase taxon2) {
		
		Rank rankTax1 = taxon1.getName().getRank();
		Rank rankTax2 = taxon2.getName().getRank();
		
		if (rankTax1 == null) return 2;
		if (rankTax2 == null) return 1;
		if (rankTax1.isHigher(rankTax2)) return 1;
		else if (rankTax1.equals(rankTax2)) {
			//same rank, order by name
			return taxon1.getName().compareTo(taxon2.getName());
			
		}
		else return 2;
	}

}
