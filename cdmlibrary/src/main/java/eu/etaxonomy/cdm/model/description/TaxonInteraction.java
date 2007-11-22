/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.Taxon;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * FIXME
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
public class TaxonInteraction extends FeatureBase {
	static Logger logger = Logger.getLogger(TaxonInteraction.class);
	private MultilanguageSet description;
	private Taxon taxon2;

	public Taxon getTaxon2(){
		return this.taxon2;
	}

	/**
	 * 
	 * @param taxon2    taxon2
	 */
	public void setTaxon2(Taxon taxon2){
		this.taxon2 = taxon2;
	}

	public MultilanguageSet getDescription(){
		return this.description;
	}
	private void setDescription(MultilanguageSet description){
		this.description = description;
	}
	public void addDescription(LanguageString description){
		this.description.add(description);
	}
	public void addDescription(String text, Language lang){
		this.description.add(text, lang);
	}
	public void removeDescription(Language lang){
		this.description.remove(lang);
	}
}