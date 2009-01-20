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
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.model.common.EventBase;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:21
 */
@Entity
//@Audited
public class DeterminationEvent extends EventBase {
	private static final Logger logger = Logger.getLogger(DeterminationEvent.class);

	private SpecimenOrObservationBase identifiedUnit;
	private Taxon taxon;
	private DeterminationModifier modifier;
	private boolean preferredFlag;
	private Set<ReferenceBase> setOfReferences = getNewReferencesSet();

	
	
	/**
	 * Factory method
	 * @return
	 */
	public static DeterminationEvent NewInstance(){
		return new DeterminationEvent();
	}
	
	/**
	 * Constructor
	 */
	protected DeterminationEvent() {
		super();
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE })
	public DeterminationModifier getModifier() {
		return modifier;
	}

	public void setModifier(DeterminationModifier modifier) {
		this.modifier = modifier;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE })
	public Taxon getTaxon(){
		return this.taxon;
	}

	/**
	 * 
	 * @param taxon    taxon
	 */
	public void setTaxon(Taxon taxon){
		this.taxon = taxon;
	}


	@Transient
	public Partial getIdentificationDate(){
		return this.getTimeperiod().getStart();
	}

	/**
	 * 
	 * @param identificationDate    identificationDate
	 */
	public void setIdentificationDate(Partial identificationDate){
		this.getTimeperiod().setStart(identificationDate);
	}

	@Transient
	public Agent getDeterminer() {
		return this.getActor();
	}
	public void setDeterminer(Agent determiner) {
		this.setActor(determiner);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE })
	public SpecimenOrObservationBase getIdentifiedUnit() {
		return identifiedUnit;
	}

	public void setIdentifiedUnit(SpecimenOrObservationBase identifiedUnit) {
		this.identifiedUnit = identifiedUnit;
	}
	
	public boolean getPreferredFlag() {
		return preferredFlag;
	}

	public void setPreferredFlag(boolean preferredFlag) {
		this.preferredFlag = preferredFlag;
	}
	
	@Transient
	private static Set<ReferenceBase> getNewReferencesSet(){
		return new HashSet<ReferenceBase>();
	}
	
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE })
	public Set<ReferenceBase> getReferences() {
		return setOfReferences;
	}

	public void setReferences(Set<ReferenceBase> references) {
		this.setOfReferences = references;
	}
	
	public void addReference(ReferenceBase reference) {
		this.setOfReferences.add(reference);
	}
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> determination event. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> determination event
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link EventBase EventBase}.
	 * 
	 * @see EventBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DeterminationEvent clone(){
		try{
			DeterminationEvent result = (DeterminationEvent)super.clone();
			//type
			result.setIdentifiedUnit(this.getIdentifiedUnit());
			//modifier
			result.setModifier(this.getModifier());
			//taxon
			result.setTaxon(this.getTaxon()); //TODO
			//no changes to: preferredFlag
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
	
	

}