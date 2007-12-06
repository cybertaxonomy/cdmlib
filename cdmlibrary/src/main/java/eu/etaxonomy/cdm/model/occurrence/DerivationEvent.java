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
public class DerivationEvent<FROM extends SpecimenOrObservationBase, TO extends PhysicalUnit> extends EventBase{
	static Logger logger = Logger.getLogger(DerivationEvent.class);

	private Set<FROM> originals = new HashSet();
	private Set<TO> derivatives = new HashSet();
	private DerivationEventType type;
	
	
	@OneToMany
	@Type(type="SpecimenOrObservationBase")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<FROM> getOriginals() {
		return originals;
	}
	protected void setOriginals(Set<FROM> originals) {
		this.originals = originals;
	}
	public void addOriginal(FROM original) {
		this.originals.add(original);
	}
	public void removeOriginal(FROM original) {
		this.originals.remove(original);
	}
	
	
	@OneToMany
	@Type(type="PhysicalUnit")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TO> getDerivatives() {
		return derivatives;
	}
	protected void setDerivatives(Set<TO> derivatives) {
		this.derivatives = derivatives;
	}
	public void addDerivative(TO derivative) {
		this.derivatives.add(derivative);
	}
	public void removeDerivative(TO derivative) {
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
