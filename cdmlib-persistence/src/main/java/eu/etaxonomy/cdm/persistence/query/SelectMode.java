/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.query;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 06.05.2009
 * @version 1.0
 */
public enum SelectMode {
	ALL,
	TAXA,
	SYNONYMS;
	
	public Class<?> criteria() {
		switch(this) {	
			case TAXA:
				return Taxon.class;			
			case SYNONYMS:
				return Synonym.class;
			default:
				return TaxonBase.class;
		}
	}
}
