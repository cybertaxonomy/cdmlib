/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.dto;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;


public class SpecimenTypeDesignationSTO extends TypeDesignationSTO {
	private SpecimenSTO typeSpecimen;
	private IdentifiedString status;
	
	private ReferenceSTO reference;

	/**
	 * @return
	 */
	public SpecimenSTO getTypeSpecimen() {
		return typeSpecimen;
	}

	/**
	 * @param typeSpecimen
	 */
	public void setTypeSpecimen(SpecimenSTO typeSpecimen) {
		this.typeSpecimen = typeSpecimen;
	}

	/**
	 * @return
	 */
	public IdentifiedString getStatus() {
		return status;
	}

	/**
	 * @param status
	 */
	public void setStatus(IdentifiedString status) {
		this.status = status;
	}

	public void setReference(ReferenceSTO reference) {
		this.reference = reference;
	}

	public ReferenceSTO getReference() {
		return reference;
	}

}
