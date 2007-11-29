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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.Stage;

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
	private Set<SpecimenDescription> descriptions = new HashSet();
	private Set<Determination> determinations = new HashSet();
	private CollectionUnit occurence;
	private Sex sex;
	private Stage lifeStage;


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public CollectionUnit getOccurence(){
		return this.occurence;
	}
	public void setOccurence(CollectionUnit occurence){
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


	@ManyToOne
	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	@ManyToOne
	public Stage getLifeStage() {
		return lifeStage;
	}

	public void setLifeStage(Stage lifeStage) {
		this.lifeStage = lifeStage;
	}
	

	@ManyToMany(mappedBy="observationalUnits")
	public Set<SpecimenDescription> getDescriptions() {
		return descriptions;
	}
	protected void setDescriptions(Set<SpecimenDescription> descriptions) {
		this.descriptions = descriptions;
	}
	public void addDescription(SpecimenDescription description) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.descriptions.add(description);
	}
	public void removeDescription(SpecimenDescription description) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.descriptions.remove(description);
	}

	
	@OneToMany(mappedBy="identifiedUnit")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Determination> getDeterminations() {
		return determinations;
	}
	protected void setDeterminations(Set<Determination> determinations) {
		this.determinations = determinations;
	}
	public void addDetermination(Determination determination) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.determinations.add(determination);
	}
	public void removeDetermination(Determination determination) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.determinations.remove(determination);
	}
	
	
	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}