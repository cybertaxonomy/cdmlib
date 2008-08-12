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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * The class representing a typification of the {@link TaxonNameBase taxon names}, belonging
 * to an {@link HomotypicalGroup homotypical group}, with a {@link Rank rank} above "species aggregate"
 * by a species taxon name of the same homotypical group.<BR>
 * According to nomenclature a type of a genus name or of any subdivision of a
 * genus can only be a species name. A type of a family name or of any
 * subdivision of a family is a genus name (and resolving it: a species name 
 * typifying this genus).<BR>
 * Moreover the designation of a particular species name as a type for an
 * homotypical group name might be nomenclaturally rejected or conserved.
 * Depending on the date of publication, the same typification could be rejected
 * according to one reference and later be conserved according to another
 * reference, but a name type designation cannot be simultaneously rejected and
 * conserved.
 * 
 * @see		SpecimenTypeDesignation
 * @author	m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@XmlType(name="NameTypeDesignation")
@Entity
public class NameTypeDesignation extends TypeDesignationBase implements ITypeDesignation {
	static Logger logger = Logger.getLogger(NameTypeDesignation.class);
	private boolean isRejectedType;
	private boolean isConservedType;
	private boolean isLectoType;
	private boolean isNotDesignated;
	private TaxonNameBase typeSpecies;
	
//	@XmlElement(name = "HomotypicalGroup")
//	@XmlIDREF
//	@XmlSchemaType(name = "IDREF")
//	private HomotypicalGroup homotypicalGroup;
	
	
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty name type designation.
	 * 
	 * @see	#NameTypeDesignation(TaxonNameBase, ReferenceBase, String, ReferenceBase, String, String, boolean, boolean, boolean)
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
	protected NameTypeDesignation(TaxonNameBase typeSpecies, ReferenceBase citation, String citationMicroReference,
			ReferenceBase lectoTypeReference, String lectoTypeMicroReference, String originalNameString, boolean isRejectedType, boolean isConservedType, boolean isNotDesignated) {
		super(citation, citationMicroReference, lectoTypeReference, lectoTypeMicroReference, originalNameString);
		this.setTypeSpecies(typeSpecies);
		this.isRejectedType = isRejectedType;
		this.isConservedType = isConservedType;
		this.isNotDesignated = isNotDesignated;
	}
		
	//********* METHODS **************************************/


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
	 * @see   #isRejectedType()
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
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.name.ITypeDesignation#isLectoType()
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