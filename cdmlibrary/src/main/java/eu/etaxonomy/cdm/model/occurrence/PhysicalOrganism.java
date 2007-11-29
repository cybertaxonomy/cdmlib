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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class PhysicalOrganism extends CollectionUnit {

	private DerivationEvent derivedFrom;
	private Set<DerivationEvent> derivationEvents = new HashSet();

	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public DerivationEvent getDerivedFrom() {
		return this.derivedFrom;
	}
	public void setDerivedFrom(DerivationEvent derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

	
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
	
}
