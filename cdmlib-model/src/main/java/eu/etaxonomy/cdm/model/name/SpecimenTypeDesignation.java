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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * The class representing a typification of one or several {@link TaxonName taxon names} by a
 * {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnit specimen or a figure}. All {@link TaxonName taxon names}
 * which have a {@link Rank rank} "species aggregate" or lower can only be typified
 * by specimens. Moreover each typification by a specimen (or by a figure) has a
 * {@link SpecimenTypeDesignationStatus status} like "holotype" or "isotype".
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
 * @since 08-Nov-2007 13:06:38
 */
@XmlRootElement(name = "SpecimenTypeDesignation")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecimenTypeDesignation", propOrder = {
    "typeSpecimen"
})
@Entity
@Audited
public class SpecimenTypeDesignation
        extends TypeDesignationBase<SpecimenTypeDesignationStatus> {

	private static final long serialVersionUID = 6481627446997275007L;
	private static final Logger logger = LogManager.getLogger();

	@XmlElement(name = "TypeSpecimen")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="typeSpecimen_id")
	private DerivedUnit typeSpecimen;

	//************* FACTORY *************************/

    public static SpecimenTypeDesignation NewInstance() {
        return new SpecimenTypeDesignation();
    }

//	/**
//	 * Creates a new specimen type designation instance
//	 * (including its {@link reference.Reference reference source} and eventually
//	 * the taxon name string originally used by this reference when establishing
//	 * the former designation) and adds it to the corresponding
//	 * {@link HomotypicalGroup#getSpecimenTypeDesignations() specimen type designation set} of the
//	 * {@link HomotypicalGroup homotypical group}.
//	 *
//	 * @param specimen				the derived unit (specimen or figure) used as type
//	 * @param status				the type designation status
//	 * @param citation				the reference source for the new designation
//	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
//	 * @param originalInfo       	any information from the original source, might be the name as written in the source (#10097)
//	 * @see							#SpecimenTypeDesignation(DerivedUnit, TypeDesignationStatus, Reference, String, String)
//	 * @see							HomotypicalGroup#addSpecimenTypeDesignation(SpecimenTypeDesignation, boolean)
//	 * @see							occurrence.DerivedUnit
//	 */
//	protected static SpecimenTypeDesignation NewInstance2(DerivedUnit specimen, TypeDesignationStatus status,
//			Reference citation, String citationMicroReference, String originalInfo){
//		SpecimenTypeDesignation specTypeDesig = new SpecimenTypeDesignation(specimen, status, citation, citationMicroReference, originalInfo);
//		return specTypeDesig;
//	}

// ************* CONSTRUCTORS *************/
	/**
	 * Class constructor: creates a new empty specimen type designation.
	 *
	 * @see	#SpecimenTypeDesignation(DerivedUnit, SpecimenTypeDesignationStatus,
	 * Reference, String, String, boolean)
	 */
	protected SpecimenTypeDesignation(){
	}


	/**
	 * Class constructor: creates a new specimen type designation instance
	 * (including its {@link eu.etaxonomy.cdm.model.reference.Reference reference source} and
	 * eventually the taxon name string originally used by this reference when
	 * establishing the former designation).
	 *
	 * @param specimen				the derived unit (specimen or figure) used
	 * 								as type
	 * @param status				the type designation status
	 * @param citation				the reference source for the new designation
	 * @param citationMicroReference	the string with the details describing
	 * 								the exact localisation within the reference
	 * @param originalInfo         	any information from the original source, might be the name as written in the source (#10097)
	 * 								reference source for the new designation
	 * @param isNotDesignated		the boolean flag indicating whether there is no specimen type at all for
	 * 								<i>this</i> specimen type designation
	 * @see							#SpecimenTypeDesignation()
	 * @see							TaxonName#addSpecimenTypeDesignation(Specimen, SpecimenTypeDesignationStatus, Reference, String, String, boolean, boolean)
	 * @see							TypeDesignationBase#isNotDesignated()
	 * @see							eu.etaxonomy.cdm.model.occurrence.DerivedUnit
	 */
	protected SpecimenTypeDesignation(DerivedUnit specimen, SpecimenTypeDesignationStatus status, Reference citation, String citationMicroReference,
			String originalInfo, boolean isNotDesignated) {
		super(citation, citationMicroReference, originalInfo, isNotDesignated);
		this.setTypeSpecimen(specimen);
		this.setTypeStatus(status);
	}

//********* METHODS **************************************/

	@Override
	public void removeType() {
		this.setTypeSpecimen(null);
	}

	/**
	 * Returns the {@link DerivedUnit.DerivedUnit derived unit} (specimen or figure) that is used
	 * in <i>this</i> specimen type designation to typify the {@link TaxonName taxon name}.
	 *
	 * @see   #getHomotypicalGroup()
	 */
	public DerivedUnit getTypeSpecimen(){
		return this.typeSpecimen;
	}

	/**
	 * @see  #getTypeSpecimen()
	 */
	public void setTypeSpecimen(DerivedUnit typeSpecimen){
		if (this.typeSpecimen == typeSpecimen){
			return;
		}
		if (this.typeSpecimen != null){
			this.typeSpecimen.removeSpecimenTypeDesignation(this);
		}
		if (typeSpecimen != null && ! typeSpecimen.getSpecimenTypeDesignations().contains(this)){
			typeSpecimen.addSpecimenTypeDesignation(this);
		}
		this.typeSpecimen = typeSpecimen;
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> type specimen. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> type specimen by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.name.TypeDesignationBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SpecimenTypeDesignation clone() {

		SpecimenTypeDesignation result;
		try {
			result = (SpecimenTypeDesignation)super.clone();
			//no changes to: typeSpecimen
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}