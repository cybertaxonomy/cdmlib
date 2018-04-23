/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.polytomouskey;

import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * @author l.morris
 * @since Feb 22, 2013
 *
 */
public class TaxonLinkDto extends AbstractLinkDto {

	private TaxonName taxonName = null;


	/**
	 * @param taxonName
	 */
	public TaxonLinkDto(TaxonName taxonName) {
		super();
		this.taxonName = taxonName;
	}

	/**
	 * @return the taxonName
	 */
	public TaxonName getTaxonName() {
		return taxonName;
	}

	/**
	 * @param taxonName the taxonName to set
	 */
	public void setTaxonName(TaxonName taxonName) {
		this.taxonName = taxonName;
	}


}
