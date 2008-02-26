/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IMediaDocumented;
import eu.etaxonomy.cdm.model.common.IdentifyableMediaEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.Stage;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * type figures are observations with at least a figure object in media
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:41
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class SpecimenOrObservationBase extends IdentifyableMediaEntity{
	static Logger logger = Logger.getLogger(SpecimenOrObservationBase.class);
	private Set<SpecimenDescription> descriptions = new HashSet();
	private Set<DeterminationEvent> determinations = new HashSet();
	private Sex sex;
	private Stage lifeStage;
	private Integer individualCount;
	// the verbatim description of this occurrence. Free text usable when no atomised data is available.
	// in conjunction with titleCache which serves as the "citation" string for this object
	private MultilanguageSet description;
	// events that created derivedUnits from this unit
	private Set<DerivationEvent> derivationEvents = new HashSet();

	
	@ManyToMany
	@Cascade( { CascadeType.SAVE_UPDATE })
	public Set<DerivationEvent> getDerivationEvents() {
		return derivationEvents;
	}
	protected void setDerivationEvents(Set<DerivationEvent> derivationEvents) {
		this.derivationEvents = derivationEvents;
	}
	public void addDerivationEvent(DerivationEvent event) {
		this.derivationEvents.add(event);
	}
	public void removeDerivationEvent(DerivationEvent event) {
		this.derivationEvents.remove(event);
	}
	

	@OneToMany(mappedBy="identifiedUnit")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DeterminationEvent> getDeterminations() {
		return determinations;
	}
	protected void setDeterminations(Set<DeterminationEvent> determinations) {
		this.determinations = determinations;
	}
	public void addDetermination(DeterminationEvent determination) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.determinations.add(determination);
	}
	public void removeDetermination(DeterminationEvent determination) {
		// FIXME bidirectional integrity. Use protected Determination setter
		this.determinations.remove(determination);
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
	
	
	public String generateTitle(){
		return "";
	}


	public Integer getIndividualCount() {
		return individualCount;
	}

	public void setIndividualCount(Integer individualCount) {
		this.individualCount = individualCount;
	}


	public MultilanguageSet getDefinition(){
		return this.description;
	}
	private void setDefinition(MultilanguageSet description){
		this.description = description;
	}
	public void addDefinition(LanguageString description){
		this.description.add(description);
	}
	public void addDefinition(String text, Language lang){
		this.description.add(text, lang);
	}
	public void removeDefinition(Language lang){
		this.description.remove(lang);
	}
	
	
	/**
	 * for derived units get the single next higher parental/original unit.
	 * If multiple original units exist throw error
	 * @return
	 */
	@Transient
	public GatheringEvent getOriginalUnit(){
		return null;
	}

	@Transient
	public abstract GatheringEvent getGatheringEvent();
	
}