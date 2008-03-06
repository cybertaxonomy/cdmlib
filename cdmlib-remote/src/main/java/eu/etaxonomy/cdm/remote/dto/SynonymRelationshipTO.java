/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;


/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 14:59:52
 *
 */
public class SynonymRelationshipTO {
	
	private TaxonSTO synoynm;
	private LocalisedTermTO type;
	public TaxonSTO getSynoynm() {
		return synoynm;
	}
	public void setSynoynm(TaxonSTO synoynm) {
		this.synoynm = synoynm;
	}
	public LocalisedTermTO getType() {
		return type;
	}
	public void setType(LocalisedTermTO type) {
		this.type = type;
	}

}
