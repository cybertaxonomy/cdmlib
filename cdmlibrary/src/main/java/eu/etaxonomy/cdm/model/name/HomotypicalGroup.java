/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.api.service.NameServiceImpl;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

@Entity
public class HomotypicalGroup extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(HomotypicalGroup.class);

	protected Set<TaxonNameBase> typifiedNames = new HashSet();
	protected Set<SpecimenTypeDesignation> typeDesignations = new HashSet();

	public HomotypicalGroup() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	@OneToMany
	public Set<TaxonNameBase> getTypifiedNames() {
		return typifiedNames;
	}
	protected void setTypifiedNames(Set<TaxonNameBase> typifiedNames) {
		this.typifiedNames = typifiedNames;
	}
	public void addTypifiedName(TaxonNameBase typifiedName) {
		typifiedName.setHomotypicalGroup(this);
	}
	public void removeTypifiedName(TaxonNameBase typifiedName) {
		typifiedName.setHomotypicalGroup(null);
	}

	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SpecimenTypeDesignation> getTypeDesignations() {
		return typeDesignations;
	}
	protected void setTypeDesignations(Set<SpecimenTypeDesignation> typeDesignations) {
		this.typeDesignations = typeDesignations;
	}	
	public void addTypeDesignation(SpecimenTypeDesignation typeDesignation) {
		typeDesignation.setHomotypicalGroup(this);
	}	
	public void removeTypeDesignation(SpecimenTypeDesignation typeDesignation) {
		typeDesignation.setHomotypicalGroup(null);
	}	
	public void addTypeDesignation(Specimen typeSpecimen, TypeDesignationStatus status, ReferenceBase citation, String citationMicroReference, String originalNameString) {
		SpecimenTypeDesignation td = new SpecimenTypeDesignation(this, typeSpecimen, status, citation, citationMicroReference, originalNameString);
		td.setHomotypicalGroup(this);
	}
	
}
