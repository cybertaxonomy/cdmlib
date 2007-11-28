/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.description.Description;

/**
 * part of a specimen or observation that is being described or determined.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:41
 */
@Entity
public class ObservationalUnit extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(ObservationalUnit.class);
	//Description defining the Observational unit in the context of the original Occurrence
	private MultilanguageSet definition;
	private Set<Description> descriptions = new HashSet();
	private Set<Determination> determinations = new HashSet();
	private Occurrence occurence;


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Occurrence getOccurence(){
		return this.occurence;
	}
	public void setOccurence(Occurrence occurence){
		this.occurence = occurence;
	}


	public MultilanguageSet getDefinition(){
		return this.definition;
	}
	private void setDefinition(MultilanguageSet definition){
		this.definition = definition;
	}
	public void addDefinition(LanguageString definition){
		this.definition.add(definition);
	}
	public void addDefinition(String text, Language lang){
		this.definition.add(text, lang);
	}
	public void removeDefinition(Language lang){
		this.definition.remove(lang);
	}
	
	@Override
	public String generateTitle(){
		return "";
	}

}