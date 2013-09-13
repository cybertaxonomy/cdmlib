package eu.etaxonomy.cdm.model.name;

import java.io.Serializable;
import java.util.Comparator;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class TaxonNameComparator implements Comparator<TaxonNameBase>, Serializable {

	public int compare(TaxonNameBase arg0, TaxonNameBase arg1) {
		String nameCacheOfArg0 = arg0.getTitleCache();
		String nameCacheOfArg1 = arg1.getTitleCache();
			
		return nameCacheOfArg0.compareToIgnoreCase(nameCacheOfArg1);
	}

}