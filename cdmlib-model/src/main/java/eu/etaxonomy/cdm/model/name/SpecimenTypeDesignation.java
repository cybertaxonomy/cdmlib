/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * Taxon names which have the a rank "species" or below can only be typified
 * by specimens. Above the species rank the taxon names are generally typified
 * by taxon names with lower rank (species for genus and genus for family) but
 * can also be typified directly by specimens.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:52
 */
@Entity
public class SpecimenTypeDesignation extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(SpecimenTypeDesignation.class);
	private HomotypicalGroup homotypicalGroup;
	private DerivedUnitBase typeSpecimen;
	private TypeDesignationStatus typeStatus;

	public SpecimenTypeDesignation(HomotypicalGroup homotypicalGroup,
			DerivedUnitBase specimen, TypeDesignationStatus status,
			ReferenceBase citation, String citationMicroReference, String originalNameString) {
		super(citation, citationMicroReference, originalNameString);
		this.setHomotypicalGroup(homotypicalGroup);
		this.setTypeSpecimen(specimen);
		this.setTypeStatus(status);
	}
	

	@ManyToOne
	public HomotypicalGroup getHomotypicalGroup() {
		return homotypicalGroup;
	}
	public void setHomotypicalGroup(HomotypicalGroup newHomotypicalGroup) {
		if(this.homotypicalGroup == newHomotypicalGroup) return;
		if (homotypicalGroup != null) { 
			homotypicalGroup.typeDesignations.remove(this);
		}
		if (newHomotypicalGroup!= null) { 
			newHomotypicalGroup.typeDesignations.add(this);
		}
		this.homotypicalGroup = newHomotypicalGroup;		
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public DerivedUnitBase getTypeSpecimen(){
		return this.typeSpecimen;
	}
	public void setTypeSpecimen(DerivedUnitBase typeSpecimen){
		this.typeSpecimen = typeSpecimen;
	}

	@ManyToOne
	public TypeDesignationStatus getTypeStatus(){
		return this.typeStatus;
	}
	public void setTypeStatus(TypeDesignationStatus typeStatus){
		this.typeStatus = typeStatus;
	}

}