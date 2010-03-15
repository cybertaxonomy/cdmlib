package eu.etaxonomy.cdm.model.taxon;

import java.io.Serializable;
import java.util.Comparator;

import eu.etaxonomy.cdm.model.name.Rank;

public class TaxonComparatorSearch implements Serializable, Comparator<TaxonBase> {

	public int compare(TaxonBase taxon1, TaxonBase taxon2) {
		
		Rank rankTax1 = taxon1.getName().getRank();
		Rank rankTax2 = taxon2.getName().getRank();
		
		if (rankTax1.isHigher(rankTax2)) return 1;
		else if (rankTax1.equals(rankTax2)) return 0;
		else return 2;
	}

}
