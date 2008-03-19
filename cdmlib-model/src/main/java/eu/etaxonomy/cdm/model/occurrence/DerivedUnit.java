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
public abstract class DerivedUnit extends SpecimenOrObservationBase {

	private Collection collection;
	private String catalogNumber;
	private TaxonNameBase storedUnder;
	private DerivationEvent derivedFrom;


	public DerivedUnit() {
		super();
	}
	/**
	 * create new unit derived from an existing field observation
	 * @param fieldObservation existing field observation from where this unit is derived
	 */
	public DerivedUnit(FieldObservation fieldObservation) {
		super();
		DerivationEvent derivedFrom = new DerivationEvent();
		// TODO: should be done in a more controlled way. Probably by making derivation event implement a general relationship interface (for bidirectional add/remove etc)
		fieldObservation.addDerivationEvent(derivedFrom);
		derivedFrom.getOriginals().add(fieldObservation);
		derivedFrom.getDerivatives().add(this);
		this.setDerivedFrom(derivedFrom);
	}
	/**
	 * create new unit derived from an existing gathering event, 
	 * thereby creating a new empty field observation
	 * @param gatheringEvent the gathering event this unit was collected at 
	 */
	public DerivedUnit(GatheringEvent gatheringEvent) {
		this(new FieldObservation());
		FieldObservation field = (FieldObservation) this.getOriginalUnit();
		field.setGatheringEvent(gatheringEvent);
	}
	
	
	
	@ManyToOne
	public DerivationEvent getDerivedFrom() {
		return derivedFrom;
	}
	public void setDerivedFrom(DerivationEvent derivedFrom) {
		this.derivedFrom = derivedFrom;
	}
	@Transient
	public Set<SpecimenOrObservationBase> getOriginals(){
		return this.getDerivedFrom().getOriginals();
	}


	@Override
	@Transient
	public GatheringEvent getGatheringEvent() {
		// FIXME: implement efficient way of getting original gathering event
		// keep link to original gathering event for performance mainly.
		return null;
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
