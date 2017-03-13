package eu.etaxonomy.cdm.model.name;

import java.io.Serializable;
import java.util.Comparator;

public class TaxonNameComparator implements Comparator<TaxonNameBase>, Serializable {
	private static final long serialVersionUID = -1007495803322700031L;

	@Override
    public int compare(TaxonNameBase arg0, TaxonNameBase arg1) {
		if (arg0.equals(arg1)){
			return 0;
		}

		String nameCacheOfArg0 = arg0.getTitleCache();
		String nameCacheOfArg1 = arg1.getTitleCache();

		int result = nameCacheOfArg0.compareToIgnoreCase(nameCacheOfArg1);
		if (result != 0){
			return result;
		}else{
			return arg0.getUuid().compareTo(arg1.getUuid());
		}

	}

}
