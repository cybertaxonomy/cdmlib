package eu.etaxonomy.cdm.model.name;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;

public class HomotypicalGroupComparator implements
		Comparator<HomotypicalGroup>, Serializable {

	@Override
	public int compare(HomotypicalGroup group1, HomotypicalGroup group2) {
		TaxonBase firstTypified1 = null;
		TaxonBase firstTypified2 = null;
		TaxonComparator taxComparator = new TaxonComparator();
		Set<TaxonNameBase> typifiedNames1 = group1.getTypifiedNames();
		List<TaxonBase> taxonBasesOfTypifiedNames = new ArrayList<TaxonBase>();
		for (TaxonNameBase typifiedName:typifiedNames1){
			if (!typifiedName.getTaxonBases().isEmpty()){
				taxonBasesOfTypifiedNames.add((TaxonBase) typifiedName.getTaxonBases().iterator().next());
			}
		}
		Collections.sort(taxonBasesOfTypifiedNames, taxComparator);
		firstTypified1 = taxonBasesOfTypifiedNames.get(0);
		
		Set<TaxonNameBase> typifiedNames2 = group2.getTypifiedNames();
		taxonBasesOfTypifiedNames = new ArrayList<TaxonBase>();
		for (TaxonNameBase typifiedName:typifiedNames2){
			if (!typifiedName.getTaxonBases().isEmpty()){
				taxonBasesOfTypifiedNames.add((TaxonBase) typifiedName.getTaxonBases().iterator().next());
			}
		}
		Collections.sort(taxonBasesOfTypifiedNames, taxComparator);
		firstTypified2 = taxonBasesOfTypifiedNames.get(0);
		return taxComparator.compare(firstTypified1, firstTypified2);
	}

}
