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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing a typification of one or several {@link TaxonNameBase taxon names} by a
 * {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or a figure}. All {@link TaxonNameBase taxon names}
 * which have a {@link Rank rank} "species aggregate" or lower can only be typified
 * by specimens. Moreover each typification by a specimen (or by a figure) has a
 * {@link TypeDesignationStatus status} like "holotype" or "isotype".
 * <P>
 * This class corresponds to: <ul>
 * <li> NomenclaturalType according to the TDWG ontology
 * <li> Typification (partially) according to the TCS
 * <li> NomenclaturalTypeDesignation according to the ABCD schema
 * </ul>
 * 
 * @see		TypeDesignationBase
 * @see		NameTypeDesignation
 * @author	m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@XmlRootElement(name = "SpecimenTypeDesignation")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecimenTypeDesignation", propOrder = {
    "typeSpecimen",
    "typeStatus"
})
@Entity
public class SpecimenTypeDesignation extends TypeDesignationBase implements ITypeDesignation {
	
	private static final Logger logger = Logger.getLogger(SpecimenTypeDesignation.class);
	
	@XmlElement(name = "TypeSpecimen")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private DerivedUnitBase typeSpecimen;
	
	@XmlElement(name = "TypeStatus")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private TypeDesignationStatus typeStatus;
	

	

//	/**
//	 * Creates a new specimen type designation instance
//	 * (including its {@link reference.ReferenceBase reference source} and eventually
//	 * the taxon name string originally used by this reference when establishing
//	 * the former designation) and adds it to the corresponding 
//	 * {@link HomotypicalGroup#getSpecimenTypeDesignations() specimen type designation set} of the
//	 * {@link HomotypicalGroup homotypical group}.
//	 * 
//	 * @param specimen				the derived unit (specimen or figure) used as type
//	 * @param status				the type designation status 
//	 * @param citation				the reference source for the new designation
//	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
//	 * @param originalNameString	the taxon name string used originally in the reference source for the new designation
//	 * @see							#SpecimenTypeDesignation(DerivedUnitBase, TypeDesignationStatus, ReferenceBase, String, String)
//	 * @see							HomotypicalGroup#addSpecimenTypeDesignation(SpecimenTypeDesignation, boolean)
//	 * @see							occurrence.DerivedUnitBase
//	 */
//	protected static SpecimenTypeDesignation NewInstance2(DerivedUnitBase specimen, TypeDesignationStatus status,
//			ReferenceBase citation, String citationMicroReference, String originalNameString){
//		SpecimenTypeDesignation specTypeDesig = new SpecimenTypeDesignation(specimen, status, citation, citationMicroReference, originalNameString);
//		return specTypeDesig;
//	}
	
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty specimen type designation.
	 * 
	 * @see	#SpecimenTypeDesignation(DerivedUnitBase, TypeDesignationStatus,
	 * ReferenceBase, String, String, boolean)
	 */
	protected SpecimenTypeDesignation(){
		
	}
	
	/**
	 * Class constructor: creates a new specimen type designation instance
	 * (including its {@link eu.etaxonomy.cdm.model.reference.ReferenceBase reference source} and 
	 * eventually the taxon name string originally used by this reference when 
	 * establishing the former designation).
	 * 
	 * @param specimen				the derived unit (specimen or figure) used 
	 * 								as type
	 * @param status				the type designation status 
	 * @param citation				the reference source for the new designation
	 * @param citationMicroReference	the string with the details describing 
	 * 								the exact localisation within the reference
	 * @param originalNameString	the taxon name string used originally in the 
	 * 								reference source for the new designation
	 * @param isNotDesignated		the boolean flag indicating whether there is no specimen type at all for 
	 * 								<i>this</i> specimen type designation
	 * @see							#SpecimenTypeDesignation()
	 * @see							TaxonNameBase#addSpecimenTypeDesignation(Specimen, TypeDesignationStatus, ReferenceBase, String, String, boolean, boolean)
	 * @see							TypeDesignationBase#isNotDesignated()
	 * @see							eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase
	 */
	protected SpecimenTypeDesignation(DerivedUnitBase specimen, TypeDesignationStatus status, ReferenceBase citation, String citationMicroReference, 
			String originalNameString, boolean isNotDesignated) {
		super(citation, citationMicroReference, originalNameString, isNotDesignated);
		this.setTypeSpecimen(specimen);
		this.setTypeStatus(status);
	}
	

	
	
	//********* METHODS **************************************/



	/** 
	 * Returns the {@link occurrence.DerivedUnitBase derived unit} (specimen or figure) that is used
	 * in <i>this</i> specimen type designation to typify the {@link TaxonNameBase taxon name}.
	 *  
	 * @see   #getHomotypicalGroup()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public DerivedUnitBase getTypeSpecimen(){
		return this.typeSpecimen;
	}
	/**
	 * @see  #getTypeSpecimen()
	 */
	public void setTypeSpecimen(DerivedUnitBase typeSpecimen){
		this.typeSpecimen = typeSpecimen;
	}

	/** 
	 * Returns the {@link TypeDesignationStatus type designation status} for <i>this</i> specimen type
	 * designation. This status describes which of the possible categories of
	 * types like "holotype", "neotype", "syntype" or "isotype" applies to <i>this</i>
	 * specimen type designation.
	 */
	@ManyToOne
	public TypeDesignationStatus getTypeStatus(){
		return this.typeStatus;
	}
	/**
	 * @see  #getTypeStatus()
	 */
	public void setTypeStatus(TypeDesignationStatus typeStatus){
		this.typeStatus = typeStatus;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.name.ITypeDesignation#isLectoType()
	 */
	/**
	 * Returns the boolean value indicating whether <i>this</i> specimen type
	 * designation has a "lectotype" status (true) or not (false).<BR>
	 * A lectotype is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration} designated as the
	 * nomenclatural type, when no holotype was indicated at the time of
	 * publication of the "type-bringing" {@link TaxonNameBase taxon name}, when the
	 * holotype is found to belong to more than one taxon name,
	 * or as long as it is missing.
	 *
	 * @see  TypeDesignationStatus#isLectotype()
	 * @see  TypeDesignationStatus#HOLOTYPE()
	 */
	@Transient
	public boolean isLectoType() {
		return typeStatus.isLectotype();
	}
	
}