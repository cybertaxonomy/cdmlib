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
	private DerivationEvent derivedFrom;

	
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
