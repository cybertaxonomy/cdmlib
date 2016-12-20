/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.polytomouskey;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author l.morris
 * @date Feb 22, 2013
 *
 */
public class TaxonLinkDto extends AbstractLinkDto {
	
	private TaxonNameBase taxonName = null;
	

	/**
	 * @param taxonNameBase
	 */
	public TaxonLinkDto(TaxonNameBase taxonNameBase) {
		super();
		this.taxonName = taxonNameBase;
	}

	/**
	 * @return the taxonName
	 */
	public TaxonNameBase getTaxonName() {
		return taxonName;
	}

	/**
	 * @param taxonName the taxonName to set
	 */
	public void setTaxonName(TaxonNameBase taxonName) {
		this.taxonName = taxonName;
	}
	
	
}
