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
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

import eu.etaxonomy.cdm.model.common.EventBase;

@Entity
public class DerivationEvent extends EventBase{
	static Logger logger = Logger.getLogger(DerivationEvent.class);

	private Set<SpecimenOrObservationBase> originals = new HashSet();
	private Set<DerivedUnit> derivatives = new HashSet();
	private DerivationEventType type;
	
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SpecimenOrObservationBase> getOriginals() {
		return originals;
	}
	protected void setOriginals(Set<SpecimenOrObservationBase> originals) {
		this.originals = originals;
	}
	public void addOriginal(SpecimenOrObservationBase original) {
		this.originals.add(original);
	}
	public void removeOriginal(SpecimenOrObservationBase original) {
		this.originals.remove(original);
	}
	
	
	@OneToMany
	@Type(type="PhysicalUnit")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DerivedUnit> getDerivatives() {
		return derivatives;
	}
	protected void setDerivatives(Set<DerivedUnit> derivatives) {
		this.derivatives = derivatives;
	}
	public void addDerivative(DerivedUnit derivative) {
		this.derivatives.add(derivative);
	}
	public void removeDerivative(DerivedUnit derivative) {
		this.derivatives.remove(derivative);
	}

	
	@ManyToOne
	public DerivationEventType getType() {
		return type;
	}
	public void setType(DerivationEventType type) {
		this.type = type;
	}
}
