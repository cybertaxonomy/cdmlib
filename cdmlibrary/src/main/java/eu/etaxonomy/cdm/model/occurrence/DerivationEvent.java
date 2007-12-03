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

import eu.etaxonomy.cdm.model.common.EventBase;

@Entity
public class DerivationEvent extends EventBase{
	static Logger logger = Logger.getLogger(DerivationEvent.class);

	private Set<SpecimenOrObservation> originals = new HashSet();
	private Set<PhysicalOrganism> derivatives = new HashSet();
	private DerivationEventType type;
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SpecimenOrObservation> getOriginals() {
		return originals;
	}
	protected void setOriginals(Set<SpecimenOrObservation> originals) {
		this.originals = originals;
	}
	public void addOriginal(SpecimenOrObservation original) {
		this.originals.add(original);
	}
	public void removeOriginal(SpecimenOrObservation original) {
		this.originals.remove(original);
	}
	
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<PhysicalOrganism> getDerivatives() {
		return derivatives;
	}
	protected void setDerivatives(Set<PhysicalOrganism> derivatives) {
		this.derivatives = derivatives;
	}
	public void addDerivative(PhysicalOrganism derivative) {
		this.derivatives.add(derivative);
	}
	public void removeDerivative(PhysicalOrganism derivative) {
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
