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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * The class representing a typification of a {@link TaxonName taxon name} with a {@link Rank rank}
 * above "species aggregate" by another taxon name.<BR>
 * According to nomenclature a type of a genus name or of any subdivision of a
 * genus can only be a species name. A type of a family name or of any
 * subdivision of a family is a genus name.<BR>
 * Moreover the designation of a particular taxon name as a type might be
 * nomenclaturally rejected or conserved. Depending on the date of publication,
 * the same typification could be rejected according to one reference and later
 * be conserved according to another reference, but a name type designation
 * cannot be simultaneously rejected and conserved.<BR>
 * Name type designations are treated as {@link TypeDesignationBase type designations}
 * and not as {@link NameRelationship name relationships}.
 *
 * @see		TypeDesignationBase
 * @see		SpecimenTypeDesignation
 * @author	m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@XmlRootElement(name = "NameTypeDesignation")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameTypeDesignation", propOrder = {
    "rejectedType",
    "conservedType",
    "typeName"
})
@Entity
@Audited
public class NameTypeDesignation extends TypeDesignationBase<NameTypeDesignationStatus> implements ITypeDesignation, Cloneable {
	private static final long serialVersionUID = 8478663508862210879L;
	final static Logger logger = Logger.getLogger(NameTypeDesignation.class);

	@XmlElement(name = "IsRejectedType")
	private boolean rejectedType;

	@XmlElement(name = "IsConservedType")
	private boolean conservedType;

	@XmlElement(name = "TypeName")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private TaxonName typeName;


	public static NameTypeDesignation NewInstance() {
		return new NameTypeDesignation();
	}


	// ************* CONSTRUCTORS *************/
	/**
	 * Class constructor: creates a new empty name type designation.
	 *
	 * @see	#NameTypeDesignation(TaxonName, Reference, String, String, boolean, boolean, boolean)
	 */
	protected NameTypeDesignation() {
	}


	/**
	 * Class constructor: creates a new name type designation instance
	 * (including its {@link eu.etaxonomy.cdm.model.reference.Reference reference source} and eventually
	 * the taxon name string originally used by this reference when establishing
	 * the former designation).
	 *
	 * @param typeName				the taxon name used as a type
	 * @param citation				the reference source for the new designation
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @param originalNameString	the taxon name string used originally in the reference source for the new designation
	 * @param isRejectedType		the boolean flag indicating whether the competent authorities rejected
	 * 								<i>this</i> name type designation
	 * @param isConservedType		the boolean flag indicating whether the competent authorities conserved
	 * 								<i>this</i> name type designation
	 * @param isNotDesignated		the boolean flag indicating whether there is no name type at all for
	 * 								<i>this</i> name type designation
	 * @see							#NameTypeDesignation()
	 * @see							TypeDesignationBase#isNotDesignated()
	 * @see							TaxonName#addNameTypeDesignation(TaxonName, Reference, String, String, boolean, boolean, boolean, boolean, boolean)
	 */
	protected NameTypeDesignation(TaxonName typeName, NameTypeDesignationStatus status,
			Reference citation, String citationMicroReference, String originalNameString) {
		super(citation, citationMicroReference, originalNameString);
		this.setTypeName(typeName);
		this.setTypeStatus(status);
	}

	/**
	 * Class constructor: creates a new name type designation instance
	 * (including its {@link eu.etaxonomy.cdm.model.reference.Reference reference source} and eventually
	 * the taxon name string originally used by this reference when establishing
	 * the former designation).
	 *
	 * @param typeName				the taxon name used as a type
	 * @param citation				the reference source for the new designation
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @param originalNameString	the taxon name string used originally in the reference source for the new designation
	 * @param isRejectedType		the boolean flag indicating whether the competent authorities rejected
	 * 								<i>this</i> name type designation
	 * @param isConservedType		the boolean flag indicating whether the competent authorities conserved
	 * 								<i>this</i> name type designation
	 * @param isNotDesignated		the boolean flag indicating whether there is no name type at all for
	 * 								<i>this</i> name type designation
	 * @see							#NameTypeDesignation()
	 * @see							TypeDesignationBase#isNotDesignated()
	 * @see							TaxonName#addNameTypeDesignation(TaxonName, Reference, String, String, boolean, boolean, boolean, boolean, boolean)
	 */
	protected NameTypeDesignation(	TaxonName typeName,
									Reference citation,
									String citationMicroReference,
									String originalNameString,
									NameTypeDesignationStatus status,
									boolean rejectedType,
									boolean conservedType,
									boolean isNotDesignated
								) {
		this(typeName, status, citation, citationMicroReference, originalNameString);
		this.setNotDesignated(isNotDesignated);
		this.rejectedType = rejectedType;
		this.conservedType = conservedType;
	}

	//********* METHODS **************************************/


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.name.TypeDesignationBase#removeType()
	 */
	@Override
	public void removeType() {
		this.typeName = null;
	}

	/**
	 * Returns the {@link TaxonName taxon name} that plays the role of the
	 * taxon name type in <i>this</i> taxon name type designation. The {@link Rank rank}
	 * of the taxon name type must be "species".
	 */
	public TaxonName getTypeName(){
		return this.typeName;
	}
	/**
	 * @see  #getTypeName()
	 */
	public void setTypeName(TaxonName typeName){
		this.typeName = typeName;
	}

	/**
	 * Returns the boolean value "true" if the competent authorities decided to
	 * reject the use of the species taxon name as the type for <i>this</i> taxon
	 * name type designation.
	 *
	 * @see   #isConservedType()
	 */
	public boolean isRejectedType(){
		return this.rejectedType;
	}
	/**
	 * @see  #isRejectedType()
	 */
	public void setRejectedType(boolean rejectedType){
		this.rejectedType = rejectedType;
	}

	/**
	 * Returns the boolean value "true" if the competent authorities decided to
	 * conserve the use of the species taxon name as the type for <i>this</i> taxon
	 * name type designation.
	 *
	 * @see   #isRejectedType()
	 */
	public boolean isConservedType(){
		return this.conservedType;
	}
	/**
	 * @see  #isConservedType()
	 */
	public void setConservedType(boolean conservedType){
		this.conservedType = conservedType;
	}

	@Override
    @Transient
	public boolean isLectoType() {
		if (getTypeStatus() == null) {
			return false;
		}
		return getTypeStatus().isLectotype();
	}

	/**
	 * Returns the boolean value "true" if the use of the species {@link TaxonName taxon name}
	 * as the type for <i>this</i> taxon name type designation was posterior to the
	 * publication of the typified taxon name. In this case the taxon name type
	 * designation should have a {@link eu.etaxonomy.cdm.model.reference.Reference reference} that is different to the
	 * {@link TaxonName#getNomenclaturalReference() nomenclatural reference} of the typified taxon name.
	 *
	 * @see   ReferencedEntityBase#getCitation()
	 */
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.model.name.ITypeDesignation#isLectoType()
//	 */
//	public boolean isLectoType() {
//		return lectoType;
//	}
//
//	/**
//	 * @see   #isLectoType()
//	 */
//	public void setLectoType(boolean lectoType) {
//		this.lectoType = lectoType;
//	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> name type. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> name type by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.name.TypeDesignationBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		NameTypeDesignation result;
		try {
			result = (NameTypeDesignation)super.clone();
			//no changes to: rejectedType, conservedType, typeName
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}
