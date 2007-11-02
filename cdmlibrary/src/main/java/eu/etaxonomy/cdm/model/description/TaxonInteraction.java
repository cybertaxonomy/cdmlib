/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.MultilanguageString;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * FIXME
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:36
 */
@Entity
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
	 * @param taxon2
	 */
	public void setTaxon2(TaxonBase taxon2){
		;
	}

	public MultilanguageString getDescription(){
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(MultilanguageString description){
		;
	}

}