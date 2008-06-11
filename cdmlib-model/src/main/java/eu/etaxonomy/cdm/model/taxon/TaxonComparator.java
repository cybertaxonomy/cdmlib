/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.Comparator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;

/**
 * @author a.mueller
 * @created 11.06.2008
 * @version 1.0
 */
public class TaxonComparator implements Comparator<TaxonBase> {
	private static final Logger logger = Logger.getLogger(TaxonComparator.class);

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(TaxonBase taxonBase1, TaxonBase taxonBase2) {
		String date1 = getDate(taxonBase1);;
		String date2 = getDate(taxonBase2);
		if (date1 == null && date2 == null){
			return 0;
		}else if (date1 == null){
			return 1;
		}else if (date2 == null){
			return -1;
		}
		return date1.compareTo(date2);
	}
	
	
	
	private String getDate(TaxonBase taxonBase){
		String result = null;
		if (taxonBase == null){
			result = null;
		}else{
			TaxonNameBase name = taxonBase.getName();
			if (name == null){
				result = null;
			}else{
				if (name instanceof ZoologicalName){
					
					result = String.valueOf(((ZoologicalName)name).getPublicationYear());
				}else{
					INomenclaturalReference ref = name.getNomenclaturalReference();
					if (ref == null){
						result = null;
					}else{
						result = ref.getYear();
					}
				}
			}
		}
		if (result != null){
			result = result.trim();
		}
		if ("".equals(result)){
			result = null;
		}
		return result;
	}
}
