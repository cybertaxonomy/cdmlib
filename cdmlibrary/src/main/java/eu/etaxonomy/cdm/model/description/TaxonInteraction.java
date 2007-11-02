/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.description;


import etaxonomy.cdm.model.common.MultilanguageString;
import etaxonomy.cdm.model.taxon.TaxonBase;
import org.apache.log4j.Logger;

/**
 * FIXME
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:21
 */
public class TaxonInteraction extends FeatureBase {
	static Logger logger = Logger.getLogger(TaxonInteraction.class);

	@Description("")
	private MultilanguageString description;
	private TaxonBase taxon2;

	public TaxonBase getTaxon2(){
		return taxon2;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTaxon2(TaxonBase newVal){
		taxon2 = newVal;
	}

	public MultilanguageString getDescription(){
		return description;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(MultilanguageString newVal){
		description = newVal;
	}

}