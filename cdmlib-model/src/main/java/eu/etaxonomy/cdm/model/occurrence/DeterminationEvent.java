/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.EventBase;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:21
 */
@Entity
public class DeterminationEvent extends EventBase {
	static Logger logger = Logger.getLogger(DeterminationEvent.class);

	private SpecimenOrObservationBase identifiedUnit;
	private Taxon taxon;
	private DeterminationModifier modifier;
	private String preferredFlag;

	
	
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
	
	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public DeterminationModifier getModifier() {
		return modifier;
	}

	public void setModifier(DeterminationModifier modifier) {
		this.modifier = modifier;
	}

	@ManyToOne
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
	public Calendar getIdentificationDate(){
		return this.getTimeperiod().getStart();
	}

	/**
	 * 
	 * @param identificationDate    identificationDate
	 */
	public void setIdentificationDate(Calendar identificationDate){
		this.getTimeperiod().setStart(identificationDate);
	}

	@Transient
	public Agent getDeterminer() {
		return this.getActor();
	}
	public void setDeterminer(Agent determiner) {
		this.setActor(determiner);
	}

	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public SpecimenOrObservationBase getIdentifiedUnit() {
		return identifiedUnit;
	}

	public void setIdentifiedUnit(SpecimenOrObservationBase identifiedUnit) {
		this.identifiedUnit = identifiedUnit;
	}
	
	public String getPreferredFlag() {
		return preferredFlag;
	}

	public void setPreferredFlag(String preferredFlag) {
		this.preferredFlag = preferredFlag;
	}


}