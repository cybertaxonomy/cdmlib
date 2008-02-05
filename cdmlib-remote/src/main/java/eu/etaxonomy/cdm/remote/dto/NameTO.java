/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;


/**
 * Data Transfer Object representing a taxonomic Name. The fields are mainly derived from the 
 * domain object {@link TaxonNameBase}. The field <code>typeDesignations</code> however is not 
 * included since these will obtained by a separate call to the web service.
 * 
 * @author a.kohlbecker
 * @author  m.doering
 * @version 1.0 $Id: NameTO.java 1108 2007-12-14 18:03:12Z m.doering $
 * @created 11.12.2007 11:04:42
 */
public class NameTO extends BaseTO {

	private String fullname;
	private List<TaggedText> taggedName = new ArrayList<TaggedText>();

	private Set<LocalisedTermTO> status = new HashSet<LocalisedTermTO>();
	private LocalisedTermTO rank;
	private ReferenceTO nomenclaturalReference;
	
	private NameTO basionym;
	private Set<NameSTO> homotypicalGroup = new HashSet<NameSTO>();
	private Set<NameRelationshipTO> nameRelations = new HashSet<NameRelationshipTO>();
	private Set<NameTO> newCombinations = new HashSet<NameTO>();
	private Set<ReferenceTO> typeDesignations = new HashSet<ReferenceTO>();
	

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public List<TaggedText> getTaggedName() {
		return taggedName;
	}

	public void addNameToken(TaggedText token) {
		this.taggedName.add(token);
	}

	public void setTypeDesignations(Set<ReferenceTO> typeDesignations) {
		this.typeDesignations = typeDesignations;
	}

	public void addNameRelation(NameRelationshipTO nameRelation) {
		this.nameRelations.add(nameRelation);
	}

	public void addStatus(LocalisedTermTO status) {
		this.status.add(status);
	}

	public void setRank(LocalisedTermTO rank) {
		this.rank = rank;
	}

	public void setNomenclaturalReference(ReferenceTO nomenclaturalReference) {
		this.nomenclaturalReference = nomenclaturalReference;
	}

	public Set<ReferenceTO> getTypeDesignations() {
		return typeDesignations;
	}

	public Set<NameRelationshipTO> getNameRelations() {
		return nameRelations;
	}

	public Set<LocalisedTermTO> getStatus() {
		return status;
	}

	public LocalisedTermTO getRank() {
		return rank;
	}

	public ReferenceTO getNomenclaturalReference() {
		return nomenclaturalReference;
	}

	public Set<NameTO> getNewCombinations() {
		return newCombinations;
	}

	public NameTO getBasionym() {
		return basionym;
	}

	public void addNewCombination(NameTO newCombination) {
		this.newCombinations.add(newCombination);
	}

	public void setBasionym(NameTO basionym) {
		this.basionym = basionym;
	}

	public Set<NameSTO> getHomotypicalGroup() {
		return homotypicalGroup;
	}

	public void setHomotypicalGroup(Set<NameSTO> homotypicalGroup) {
		this.homotypicalGroup = homotypicalGroup;
	}

	public void setTaggedName(List<TaggedText> taggedName) {
		this.taggedName = taggedName;
	}

	public void setStatus(Set<LocalisedTermTO> status) {
		this.status = status;
	}

	public void setNameRelations(Set<NameRelationshipTO> nameRelations) {
		this.nameRelations = nameRelations;
	}

	public void setNewCombinations(Set<NameTO> newCombinations) {
		this.newCombinations = newCombinations;
	}

}
