/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import javax.persistence.Entity;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

@Entity
public abstract class TypeDesignationBase extends ReferencedEntityBase {
	private TaxonNameBase typifiedName;

	public TypeDesignationBase(TaxonNameBase typifiedName, ReferenceBase citation,
			String citationMicroReference, String originalNameString) {
		super(citation, citationMicroReference, originalNameString);
		this.typifiedName = typifiedName;
	}


	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getTypifiedName() {
		return typifiedName;
	}

	public void setTypifiedName(TaxonNameBase newTypifiedName) {
		// Hibernate bidirectional cascade hack: 
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1054
		if(this.typifiedName == newTypifiedName) return;
		if (typifiedName != null) { 
			typifiedName.typeDesignations.remove(this);
		}
		if (newTypifiedName!= null) { 
			newTypifiedName.typeDesignations.add(this);
		}
		this.typifiedName = newTypifiedName;
	}
	
}
