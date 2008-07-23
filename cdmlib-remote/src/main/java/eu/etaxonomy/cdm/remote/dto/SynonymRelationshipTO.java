/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;


/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 14:59:52
 *
 */
public class SynonymRelationshipTO {
	
	private ArrayList<LocalisedTermSTO> type = new ArrayList<LocalisedTermSTO>();
	private TaxonSTO synoynm;
	
	public TaxonSTO getSynoynm() {
		return synoynm;
	}
	public void setSynoynm(TaxonSTO synoynm) {
		this.synoynm = synoynm;
	}
	public ArrayList<LocalisedTermSTO> getTypes() {
		return type;
	}
	public void setType(ArrayList<LocalisedTermSTO> types) {
		this.type = types;
	}
	public void addType(LocalisedTermSTO sto) {
		type.add(sto);
	}

}
