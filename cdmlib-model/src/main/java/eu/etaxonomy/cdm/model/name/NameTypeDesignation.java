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
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing a typification of a {@link TaxonNameBase taxon name} with
 * a {@link Rank rank} above "species aggregate" by a species taxon name. A type of a
 * genus name or of any subdivision of a genus can only be a species name.
 * A type of a family name or of any subdivision of a family is a genus name 
 * (and resolving it: a species name typifying this genus).
 * Moreover the designation of a particular species name as a type for a
 * suprageneric taxon name might be nomenclaturally rejected or conserved.
 * Depending on the date of publication, the same typification could be rejected
 * according to one reference and later be conserved according to another
 * reference, but a name type designation cannot be simultaneously rejected and
 * conserved. Both names, the typified name and the species name used in
 * the name type designation, must belong to the same {@link HomotypicalGroup homotypical group}.
 * 
 * @see		SpecimenTypeDesignation
 * @author	m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@XmlType(name="NameTypeDesignation")
@Entity
public class NameTypeDesignation extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(NameTypeDesignation.class);
	private boolean isRejectedType;
	private boolean isConservedType;
	private TaxonNameBase typeSpecies;
	private TaxonNameBase typifiedName;
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty name type designation.
	 * 
	 * @see	#NameTypeDesignation(TaxonNameBase, TaxonNameBase, ReferenceBase, String, String, boolean, boolean)
	 */
	protected NameTypeDesignation() {
		super();
	}

	/**
	 * Class constructor: creates a new name type designation instance
	 * (including its {@link reference.ReferenceBase reference source} and eventually
	 * the taxon name string originally used by this reference when establishing
	 * the former designation) and adds it to the corresponding 
	 * {@link TaxonNameBase#getNameTypeDesignations() name type designation set} of the typified name.
	 * The typified name will be added to the {@link HomotypicalGroup homotypical group} to which
	 * the species taxon name used for the typification belongs. 
	 * 
	 * @param typifiedName			the suprageneric taxon name to be typified
	 * @param typeSpecies			the species taxon name typifying the suprageneric taxon name 
	 * @param citation				the reference source for the new designation
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @param originalNameString	the taxon name string used originally in the reference source for the new designation
	 * @param isRejectedType		the boolean flag indicating whether the competent authorities rejected
	 * 								this name type designation
	 * @param isConservedType		the boolean flag indicating whether the competent authorities conserved
	 * 								this name type designation
	 * @see							#NameTypeDesignation()
	 * @see							TaxonNameBase#addNameTypeDesignation(TaxonNameBase, ReferenceBase, String, String, boolean, boolean)
	 */
	protected NameTypeDesignation(TaxonNameBase typifiedName, TaxonNameBase typeSpecies, ReferenceBase citation, String citationMicroReference,
			String originalNameString, boolean isRejectedType, boolean isConservedType) {
		super(citation, citationMicroReference, originalNameString);
		this.setTypeSpecies(typeSpecies);
		this.setTypifiedName(typifiedName);
		typifiedName.setHomotypicalGroup(typeSpecies.getHomotypicalGroup());
		this.isRejectedType = isRejectedType;
		this.isConservedType = isConservedType;
	}
		
	//********* METHODS **************************************/

	/** 
	 * Returns the {@link TaxonNameBase taxon name} that plays the role of the
	 * typified taxon name in this taxon name type designation. The {@link Rank rank}
	 * of a taxon name typified by another taxon name must be higher than
	 * "species aggregate".
	 *  
	 * @see   #getTypeSpecies()
	 */
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getTypifiedName() {
		return typifiedName;
	}
	/**
	 * @see  #getTypifiedName()
	 */
	private void setTypifiedName(TaxonNameBase typifiedName) {
		this.typifiedName = typifiedName;
		if (typifiedName != null){
			typifiedName.getNameTypeDesignations().add(this);
		}
	}


	/** 
	 * Returns the {@link TaxonNameBase taxon name} that plays the role of the
	 * taxon name type in this taxon name type designation. The {@link Rank rank}
	 * of a taxon name type must be "species".
	 *  
	 * @see   #getTypifiedName()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getTypeSpecies(){
		return this.typeSpecies;
	}
	/**
	 * @see  #getTypeSpecies()
	 */
	private void setTypeSpecies(TaxonNameBase typeSpecies){
		this.typeSpecies = typeSpecies;
	}

	/** 
	 * Returns the boolean value "true" if the competent authorities decided to
	 * reject the use of the species taxon name for this taxon name type
	 * designation.
	 *  
	 * @see   #isConservedType()
	 */
	public boolean isRejectedType(){
		return this.isRejectedType;
	}
	/**
	 * @see  #isRejectedType()
	 */
	public void setRejectedType(boolean isRejectedType){
		this.isRejectedType = isRejectedType;
	}

	/** 
	 * Returns the boolean value "true" if the competent authorities decided to
	 * conserve the use of the species taxon name for this taxon name type
	 * designation.
	 *  
	 * @see   #isConservedType()
	 */
	public boolean isConservedType(){
		return this.isConservedType;
	}
	/**
	 * @see  #isConservedType()
	 */
	public void setConservedType(boolean isConservedType){
		this.isConservedType = isConservedType;
	}

}