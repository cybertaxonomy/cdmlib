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
import java.util.Comparator;

import eu.etaxonomy.cdm.model.name.TaxonName;

public class TaxonNameComparator implements Comparator<TaxonName>, Serializable {

    private static final long serialVersionUID = -1007495803322700031L;

	@Override
    public int compare(TaxonName arg0, TaxonName arg1) {
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