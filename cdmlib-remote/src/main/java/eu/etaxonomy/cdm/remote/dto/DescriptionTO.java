/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.dto;

import java.util.HashSet;
import java.util.Set;

public class DescriptionTO extends BaseTO {

	// general bits from DescriptionBase
	private String label;
	private Set<ReferenceSTO> sources = new HashSet<ReferenceSTO>();
	private Set<DescriptionElementSTO> elements = new HashSet<DescriptionElementSTO>();

	// -- TaxonDescription specific --
	private TaxonSTO taxon;
	//    scopes & geoscopes
	private Set<LocalisedTermSTO> scopes = new HashSet<LocalisedTermSTO>();

	// -- SpecimenDescription specific --
	private Set<SpecimenSTO> specimensOrObersvations = new HashSet<SpecimenSTO>();

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public TaxonSTO getTaxon() {
		return taxon;
	}
	public void setTaxon(TaxonSTO taxon) {
		this.taxon = taxon;
	}

	public Set<LocalisedTermSTO> getScopes() {
		return scopes;
	}
	public void setScopes(Set<LocalisedTermSTO> scopes) {
		this.scopes = scopes;
	}
	public void addScope(LocalisedTermSTO scope){
		this.scopes.add(scope);
	}
	
	public Set<ReferenceSTO> getSources() {
		return sources;
	}
	public void setSources(Set<ReferenceSTO> sources) {
		this.sources = sources;
	}
	public void addSource(ReferenceSTO source){
		this.sources.add(source);
	}
	
	public Set<DescriptionElementSTO> getElements() {
		return elements;
	}
	public void setElements(Set<DescriptionElementSTO> elements) {
		this.elements = elements;
	}
	public void addElement(DescriptionElementSTO element){
		this.elements.add(element);
	}

	public Set<SpecimenSTO> getSpecimensOrObersvations() {
		return specimensOrObersvations;
	}
	public void setSpecimensOrObersvations(Set<SpecimenSTO> specimensOrObersvations) {
		this.specimensOrObersvations = specimensOrObersvations;
	}
	public void addSpecimensOrObersvation(SpecimenSTO specimenOrObersvation){
		this.specimensOrObersvations.add(specimenOrObersvation);
	}
	

}
