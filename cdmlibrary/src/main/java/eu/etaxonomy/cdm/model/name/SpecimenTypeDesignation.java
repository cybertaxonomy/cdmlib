/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * {only for typified names which have the "species" rank or below}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:52
 */
@Entity
public class SpecimenTypeDesignation extends TypeDesignationBase {
	static Logger logger = Logger.getLogger(SpecimenTypeDesignation.class);
	private DerivedUnit typeSpecimen;
	private TypeDesignationStatus typeStatus;

	public SpecimenTypeDesignation(TaxonNameBase typifiedName,
			DerivedUnit specimen, TypeDesignationStatus status,
			ReferenceBase citation, String citationMicroReference, String originalNameString) {
		super(typifiedName, citation, citationMicroReference, originalNameString);
		this.typeSpecimen = specimen;
		this.typeStatus = status;
	}
	

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public DerivedUnit getTypeSpecimen(){
		return this.typeSpecimen;
	}
	public void setTypeSpecimen(Specimen typeSpecimen){
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