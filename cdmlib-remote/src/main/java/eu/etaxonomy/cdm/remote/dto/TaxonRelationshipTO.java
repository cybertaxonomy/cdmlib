/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 15:00:00
 *
 */
public class TaxonRelationshipTO{
	
	private LocalisedTermSTO type;
	private TaxonSTO taxon;
	
	public LocalisedTermSTO getType() {
		return type;
	}
	public void setType(LocalisedTermSTO type) {
		this.type = type;
	}
	public TaxonSTO getTaxon() {
		return taxon;
	}
	public void setTaxon(TaxonSTO taxon) {
		this.taxon = taxon;
	}

}
