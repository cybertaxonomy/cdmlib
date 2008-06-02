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

import eu.etaxonomy.cdm.model.common.EventBase;

@Entity
public class DerivationEvent extends EventBase{
	static Logger logger = Logger.getLogger(DerivationEvent.class);

	private Set<SpecimenOrObservationBase> originals = new HashSet<SpecimenOrObservationBase>();
	protected Set<DerivedUnitBase> derivatives = new HashSet<DerivedUnitBase>();
	private DerivationEventType type;
	
	/**
	 * Factory method
	 * @return
	 */
	public static DerivationEvent NewInstance(){
		return new DerivationEvent();
	}
	
	/**
	 * Constructor
	 */
	protected DerivationEvent() {
		super();
	}
	
	@ManyToMany(mappedBy="derivationEvents")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SpecimenOrObservationBase> getOriginals() {
		return originals;
	}
	protected void setOriginals(Set<SpecimenOrObservationBase> originals) {
		this.originals = originals;
	}
	public void addOriginal(SpecimenOrObservationBase original) {
		if (! this.originals.contains(original)){
			this.originals.add(original);
			original.addDerivationEvent(this);
		}
	}
	public void removeOriginal(SpecimenOrObservationBase original) {
		this.originals.remove(original);
	}
	
	
	@OneToMany(mappedBy="derivationEvent")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DerivedUnitBase> getDerivatives() {
		return derivatives;
	}
	protected void setDerivatives(Set<DerivedUnitBase> derivatives) {
		this.derivatives = derivatives;
	}
	public void addDerivative(DerivedUnitBase derivative) {
		if (derivative != null){
			derivative.setDerivedFrom(this);
		}
	}
	public void removeDerivative(DerivedUnitBase derivative) {
		if (derivative != null){
			derivative.setDerivedFrom(null);
		}
	}

	
	@ManyToOne
	public DerivationEventType getType() {
		return type;
	}
	public void setType(DerivationEventType type) {
		this.type = type;
	}
}
