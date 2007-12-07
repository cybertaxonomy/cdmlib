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
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;

@Entity
public class DerivedUnit extends SpecimenOrObservationBase {

	private Collection collection;
	private String catalogNumber;
	private TaxonNameBase storedUnder;
	private Set<DerivationEvent> derivationEvents = new HashSet();

	
	@Override
	@Transient
	public GatheringEvent getGatheringEvent() {
		// FIXME: implement efficient way of getting original gathering event
		// keep link to original gathering event for performance mainly.
		return null;
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
	
	
	@Transient
	public Set<SpecimenOrObservationBase> getDerivedFrom(){
		Set<SpecimenOrObservationBase> result = new HashSet();
		for (DerivationEvent event : getDerivationEvents()){
			if (event.getDerivatives().contains(this)){
				result.addAll(event.getOriginals());
			}
		}
		return result;
	}
	@Transient
	public Set<SpecimenOrObservationBase> getDerivates(){
		Set<SpecimenOrObservationBase> result = new HashSet();
		for (DerivationEvent event : getDerivationEvents()){
			// check if I am not a derivate
			if (event.getOriginals().contains(this)){
				result.addAll(event.getDerivatives());
			}
		}
		return result;
	}

	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Collection getCollection(){
		return this.collection;
	}
	public void setCollection(Collection collection){
		this.collection = collection;
	}
	

	public String getCatalogNumber() {
		return catalogNumber;
	}

	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getStoredUnder() {
		return storedUnder;
	}
	public void setStoredUnder(TaxonNameBase storedUnder) {
		this.storedUnder = storedUnder;
	}

}
