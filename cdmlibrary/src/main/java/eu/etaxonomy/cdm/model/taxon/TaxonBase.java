/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * {unique name within view/treatment}
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:48
 */
public abstract class TaxonBase extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(TaxonBase.class);

	//The assignement to the Taxon or to the Synonym class is not definitive
	@Description("The assignement to the Taxon or to the Synonym class is not definitive")
	private boolean isDoubtful;
	private TaxonNameBase name;

	public TaxonNameBase getName(){
		return name;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(TaxonNameBase newVal){
		name = newVal;
	}

	public boolean isDoubtful(){
		return isDoubtful;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDoubtful(boolean newVal){
		isDoubtful = newVal;
	}

}