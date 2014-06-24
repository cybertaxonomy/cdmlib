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
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author k.luther
 * @date 18.03.2010
 *
 */
public class TaxonComparatorSearch implements Serializable, Comparator<TaxonBase> {
	private static final long serialVersionUID = 2596641007876609704L;

	public int compare(TaxonBase taxon1, TaxonBase taxon2) {
		
		if (taxon1.getUuid().equals(taxon2.getUuid())){
			return 0;
		}
		TaxonNameBase<?,?> name1 = taxon1.getName();
		TaxonNameBase<?,?> name2 = taxon2.getName();
		
		Rank rankTax1 = (name1 == null) ? null : name1.getRank();
		Rank rankTax2 = (name2 == null) ? null : name2.getRank();
		
		//first compare ranks, if ranks are equal (or both null) compare names or taxon title cache if names are null
		if (rankTax1 == null && rankTax2 != null){
			return 1;
		}else if(rankTax2 == null && rankTax1 != null){
			return -1;
		}else if (rankTax1 != null && rankTax1.isHigher(rankTax2)){
			return -1;
		}else if (rankTax1 == null && rankTax2 == null || rankTax1.equals(rankTax2)) {
			if (name1 != null && name2 != null){
				//same rank, order by name
				int result = name1.compareTo(name2);
				if (result == 0){
					return taxon1.getUuid().compareTo(taxon2.getUuid());
				}else{
					return result;
				}
			}else {
				//this is maybe not 100% correct, we need to compare name cases, but it is a very rare case
				return taxon1.getTitleCache().compareTo(taxon2.getTitleCache());
			}
		}else{
			//rankTax2.isHigher(rankTax1)
			return 1;
		}
	}

}
