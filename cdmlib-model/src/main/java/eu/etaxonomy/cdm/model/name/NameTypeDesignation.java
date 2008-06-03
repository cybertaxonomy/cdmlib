/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * Only taxon names which have a rank above "species" are typified by other
 * taxon names of lower rank. A type of a name of a genus or of any
 * subdivision of a genus can only be the name of a species. A type of a name
 * of a family or of any subdivision of a family is the name of a genus
 * (or resolving it: a name of a species typifying this genus).
 * Moreover the mentioned taxon name type may be rejected or conserved.
 * Depending on the date of publication, the same designation could have the
 * rejected status according to one reference and later have the conserved
 * status according to another reference.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@Entity
public class NameTypeDesignation extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(NameTypeDesignation.class);
	private boolean isRejectedType;
	private boolean isConservedType;
	private TaxonNameBase typeSpecies;
	private TaxonNameBase typifiedName;

	protected NameTypeDesignation(TaxonNameBase typifiedName, TaxonNameBase typeSpecies, ReferenceBase citation, String citationMicroReference,
			String originalNameString, boolean isRejectedType, boolean isConservedType) {
		super(citation, citationMicroReference, originalNameString);
		this.setTypeSpecies(typeSpecies);
		this.setTypifiedName(typifiedName);
		// the typified name has to be part of the same homotypical group as the type species
		typifiedName.setHomotypicalGroup(typeSpecies.getHomotypicalGroup());
		this.isRejectedType = isRejectedType;
		this.isConservedType = isConservedType;
	}
	
	
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getTypifiedName() {
		return typifiedName;
	}
	private void setTypifiedName(TaxonNameBase typifiedName) {
		this.typifiedName = typifiedName;
		typifiedName.nameTypeDesignations.add(this);
		}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getTypeSpecies(){
		return this.typeSpecies;
	}
	private void setTypeSpecies(TaxonNameBase typeSpecies){
		this.typeSpecies = typeSpecies;
	}

	public boolean isRejectedType(){
		return this.isRejectedType;
	}
	public void setRejectedType(boolean isRejectedType){
		this.isRejectedType = isRejectedType;
	}

	public boolean isConservedType(){
		return this.isConservedType;
	}
	public void setConservedType(boolean isConservedType){
		this.isConservedType = isConservedType;
	}

}