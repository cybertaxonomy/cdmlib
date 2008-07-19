/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * FIXME
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
public class TaxonInteraction extends DescriptionElementBase {
	private static final Logger logger = Logger.getLogger(TaxonInteraction.class);
	private MultilanguageSet description;
	private Taxon taxon2;

	/**
	 * Factory method
	 * @return
	 */
	public static TaxonInteraction NewInstance(){
		return new TaxonInteraction();
	}
	
	public TaxonInteraction() {
		super(null);
	}
	
	
	@ManyToOne
	public Taxon getTaxon2(){
		return this.taxon2;
	}
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
	public void addDescription(String text, Language language){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	public void removeDescription(Language lang){
		this.description.remove(lang);
	}
}