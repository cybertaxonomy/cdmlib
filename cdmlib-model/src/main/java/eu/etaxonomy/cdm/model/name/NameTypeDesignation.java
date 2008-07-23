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
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

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
	private boolean isLectoType;
	private ReferenceBase lectoTypeReference;
	private String lectoTypeMicroReference;
	private boolean isNotDesignated;
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
	 * 								<i>this</i> name type designation
	 * @param isConservedType		the boolean flag indicating whether the competent authorities conserved
	 * 								<i>this</i> name type designation
	 * @param isNotDesignated		see at {@link #isNotDesignated()}
	 * @see							#NameTypeDesignation()
	 * @see							TaxonNameBase#addNameTypeDesignation(TaxonNameBase, ReferenceBase, String, String, boolean, boolean)
	 */
	protected NameTypeDesignation(TaxonNameBase typifiedName, TaxonNameBase typeSpecies, ReferenceBase citation, String citationMicroReference,
			String originalNameString, boolean isRejectedType, boolean isConservedType, boolean isNotDesignated) {
		super(citation, citationMicroReference, originalNameString);
		this.setTypeSpecies(typeSpecies);
		typifiedName.addNameTypeDesignation(this);
		//TODO check if this should be so
		// seems like it shouldn't be so
		//typifiedName.setHomotypicalGroup(typeSpecies.getHomotypicalGroup());
		this.isRejectedType = isRejectedType;
		this.isConservedType = isConservedType;
		this.isNotDesignated = isNotDesignated;
	}
		
	//********* METHODS **************************************/

	/** 
	 * Returns the {@link TaxonNameBase taxon name} that plays the role of the
	 * typified taxon name in <i>this</i> taxon name type designation. The {@link Rank rank}
	 * of a taxon name typified by another taxon name must be higher than
	 * "species aggregate".
	 *  
	 * @see   #getTypeSpecies()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getTypifiedName() {
		return typifiedName;
	}
	/**
	 * @see  #getTypifiedName()
	 */
	@Deprecated // to be used by hibernate only
	protected void setTypifiedName(TaxonNameBase typifiedName) {
		this.typifiedName = typifiedName;
	}


	/** 
	 * Returns the {@link TaxonNameBase taxon name} that plays the role of the
	 * taxon name type in <i>this</i> taxon name type designation. The {@link Rank rank}
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
	 * reject the use of the species taxon name for <i>this</i> taxon name type
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
	 * conserve the use of the species taxon name for <i>this</i> taxon name type
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

	/** 
	 * Returns the boolean value "true" if the use of the species {@link TaxonNameBase taxon name} for
	 * <i>this</i> taxon name type designation was posterior to the publication of the
	 * typified taxon name. In this case the taxon name type designation should
	 * have a {@link reference.ReferenceBase reference} that is different to the
	 * nomenclatural reference of the typified taxon name.
	 *  
	 * @see   #getLectoTypeReference()
	 */
	public boolean isLectoType() {
		return isLectoType;
	}

	/**
	 * @see   #isLectoType()
	 */
	public void setLectoType(boolean isLectoType) {
		this.isLectoType = isLectoType;
	}

	/** 
	 * Returns the {@link reference.ReferenceBase reference} used in case <i>this</i> 
	 * taxon name type designation is a lectotype. This reference is different
	 * to the nomenclatural reference of the typified taxon name.
	 *  
	 * @see   #isLectoType()
	 */
	public ReferenceBase getLectoTypeReference() {
		return lectoTypeReference;
	}

	/**
	 * @see   #getLectoTypeReference()
	 */
	public void setLectoTypeReference(ReferenceBase lectoTypeReference) {
		this.lectoTypeReference = lectoTypeReference;
	}

	/** 
	 * Returns the details string of the reference corresponding to <i>this</i> taxon 
	 * type designation if it is a lectotype. The details describe the exact
	 * localisation within the publication used for the lectotype assignation.
	 * These are mostly (implicitly) pages but can also be figures or tables or
	 * any other element of a publication. A lectotype micro reference (details)
	 * requires the existence of a lectotype reference.
	 * 
	 * @see   #getLectoTypeReference()
	 */
	public String getLectoTypeMicroReference() {
		return lectoTypeMicroReference;
	}

	/**
	 * @see   #getLectoTypeMicroReference()
	 */
	public void setLectoTypeMicroReference(String lectoTypeMicroReference) {
		this.lectoTypeMicroReference = lectoTypeMicroReference;
	}

	/**
	 * Returns the boolean value "true" if a name type does not exist.
	 * Two cases must be differentiated: <BR><ul> 
	 * <li> a) it is unknown whether a name type exists and <BR>
	 * <li> b) it is known that no name type exists <BR>
	 *  </ul>
	 * If b) is true there should be a NameTypeDesignation with the flag
	 * isNotDesignated set. The typeSpecies should then be "null".
	 */
	public boolean isNotDesignated() {
		return isNotDesignated;
	}

	/**
	 * @see   #isNotDesignated()
	 */
	public void setNotDesignated(boolean isNotDesignated) {
		this.isNotDesignated = isNotDesignated;
	}
	
	
	
	

}