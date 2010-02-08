/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;




import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * The class representing status (categories) of {@link SpecimenTypeDesignation specimen type designations}
 * for a {@link TaxonNameBase taxon name} or a set of them. Within this set {@link NameRelationshipType#BASIONYM() basionyms}
 * or {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonyms}, in case of reclassifications,
 * will be here referred as "type-bringing" taxon names.
 * <P>
 * The different status indicate whether the {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimens} used as types
 * in a designation are duplicates, replacements, related specimens etc. 
 * <P>
 * A standard (ordered) list of type designation status instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional type designation
 * status if needed. 
 * <P>
 * This class corresponds to: <ul>
 * <li> NomencalturalTypeTypeTerm according to the TDWG ontology
 * <li> NomenclaturalTypeStatusOfUnitsEnum according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecimenTypeDesignationStatus")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class SpecimenTypeDesignationStatus extends TypeDesignationStatusBase<SpecimenTypeDesignationStatus> {
	static Logger logger = Logger.getLogger(SpecimenTypeDesignationStatus.class);

	private static SpecimenTypeDesignationStatus PHOTOTYPE;

	private static SpecimenTypeDesignationStatus ICONOTYPE;

	private static SpecimenTypeDesignationStatus ISOEPITYPE;

	private static SpecimenTypeDesignationStatus PARALECTOTYPE;

	private static SpecimenTypeDesignationStatus SECOND_STEP_NEOTYPE;

	private static SpecimenTypeDesignationStatus SECOND_STEP_LECTOTYPE;

	private static SpecimenTypeDesignationStatus PARANEOTYPE;

	private static SpecimenTypeDesignationStatus ISONEOTYPE;

	private static SpecimenTypeDesignationStatus ISOLECTOTYPE;

	private static SpecimenTypeDesignationStatus PARATYPE;

	private static SpecimenTypeDesignationStatus SYNTYPE;

	private static SpecimenTypeDesignationStatus ISOTYPE;

	private static SpecimenTypeDesignationStatus EPITYPE;

	private static SpecimenTypeDesignationStatus NEOTYPE;

	private static SpecimenTypeDesignationStatus LECTOTYPE;

	private static SpecimenTypeDesignationStatus HOLOTYPE;

	private static SpecimenTypeDesignationStatus UNSPECIFIED;
	
	private static SpecimenTypeDesignationStatus ORIGINAL_MATERIAL;
	
	private static SpecimenTypeDesignationStatus ISOSYNTYPE;
	
	
	
	private static final UUID uuidHolotype = UUID.fromString("a407dbc7-e60c-46ff-be11-eddf4c5a970d");
	private static final UUID uuidLectotype = UUID.fromString("05002d46-083e-4b27-8731-2e7c28a8825c");
	private static final UUID uuidNeotype = UUID.fromString("26e13359-8f77-4e40-a85a-56c01782fce0");
	private static final UUID uuidEpitype = UUID.fromString("989a2715-71d5-4fbe-aa9a-db9168353744");
	private static final UUID uuidIsolectotype = UUID.fromString("7a1a8a53-78f4-4fc0-89f7-782e94992d08");
	private static final UUID uuidIsoneotype = UUID.fromString("7afc2f4f-f70a-4aa5-80a5-87764f746bde");
	private static final UUID uuidIsotype = UUID.fromString("93ef8257-0a08-47bb-9b36-542417ae7560");
	private static final UUID uuidParaneotype = UUID.fromString("0c39e2a5-2fe0-4d4f-819a-f609b5340339");
	private static final UUID uuidParatype = UUID.fromString("eb7df2e5-d9a7-479d-970c-c6f2b0a761d7");
	private static final UUID uuidSecondStepLectotype = UUID.fromString("01d91053-7004-4984-aa0d-9f4de59d6205");
	private static final UUID uuidSecondStepNeotype = UUID.fromString("8d2fed1f-242e-4bcf-bbd7-e85133e479dc");
	private static final UUID uuidSyntype = UUID.fromString("f3b60bdb-4638-4ca9-a0c7-36e77d8459bb");
	private static final UUID uuidParalectotype = UUID.fromString("7244bc51-14d8-41a6-9524-7dc5303bba29");
	private static final UUID uuidIsoepitype = UUID.fromString("95b90696-e103-4bc0-b60b-c594983fb566");
	private static final UUID uuidIconotype = UUID.fromString("643513d0-32f5-46ba-840b-d9b9caf8160f");
	private static final UUID uuidPhototype = UUID.fromString("b7807acc-f559-474e-ad4a-e7a41e085e34");
	private static final UUID uuidUnspecified = UUID.fromString("230fd762-b143-49de-ac2e-744bcc48a63b");
	private static final UUID uuidOriginalMaterial = UUID.fromString("49c96cae-6be6-401e-9b36-1bc12d9dc8f9");
	private static final UUID uuidIsosyntype = UUID.fromString("052a5ff0-8e9a-4355-b24f-5e4bb6071f44");

	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty type designation status instance.
	 * 
	 * @see 	#TypeDesignationStatus(String, String, String)
	 */
	public SpecimenTypeDesignationStatus() {
	}


	/** 
	 * Class constructor: creates an additional type designation status instance
	 * with a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label
	 * and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new type designation status to be created 
	 * @param	label  		 the string identifying the new type designation
	 * 						 status to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new type designation status to be created
	 * @see 				 #TypeDesignationStatus()
	 */
	public SpecimenTypeDesignationStatus(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************
	

	/**
	 * Returns the boolean value indicating whether <i>this</i> type designation
	 * status is itself "lectotype" or a kind of "lectotype" (true) or not
	 * (false). Returns false if <i>this</i> type designation status is null.<BR>
	 * A lectotype is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration} designated as the
	 * nomenclatural type, when no holotype was indicated at the time of
	 * publication of the "type-bringing" {@link TaxonNameBase taxon name}, when the
	 * holotype is found to be assigned to taxon names belonging to more than
	 * one {@link HomotypicalGroup homotypical group}, or as long as it is missing.
	 *
	 * @see  #LECTOTYPE()
	 * @see  #HOLOTYPE()
	 * @see  eu.etaxonomy.cdm.model.common.DefinedTermBase#getKindOf()
	 */
	@Transient
	public boolean isLectotype(){
		if (this.equals(LECTOTYPE()) ||
				this.equals(ISOLECTOTYPE()) ||
				this.equals(SECOND_STEP_LECTOTYPE()) ||
				this.equals(PARALECTOTYPE()) ){
			return true;
		}else{
			return false;
		}
	}

	
	/**
	 * Returns the "holotype" designation status. A holotype of a
	 * set of names is the one {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration}
	 * designated as the nomenclatural type by the {@link NonViralName#getCombinationAuthorTeam() author} of the
	 * "type-bringing" {@link TaxonNameBase taxon name} (or by the author of a later validated
	 * "invalid" taxon name).
	 * 
	 * @see		NameRelationshipType#VALIDATED_BY_NAME()
	 */
	public static final SpecimenTypeDesignationStatus HOLOTYPE(){
		return HOLOTYPE;
	}

	/**
	 * Returns the "lectotype" designation status. A lectotype is a
	 * {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration} designated as the nomenclatural type,
	 * when no holotype was indicated at the time of publication of the
	 * "type-bringing" {@link TaxonNameBase taxon name}, when the
	 * holotype is found to to be assigned to taxon names belonging to more than one
	 * {@link HomotypicalGroup homotypical group}, or as long as it is missing.
	 * 
	 * @see	#HOLOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus LECTOTYPE(){
		return LECTOTYPE;
	}

	/**
	 * Returns the "neotype" designation status. A neotype is a
	 * {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration} selected to serve as nomenclatural type
	 * as long as all of the material on which the "type-bringing" {@link TaxonNameBase taxon name} was based
	 * is missing. 
	 * 
	 * @see	#HOLOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus NEOTYPE(){
		return NEOTYPE;
	}

	/**
	 * Returns the "epitype" designation status. An epitype is a
	 * {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration} selected to serve as an interpretative type
	 * when the holotype, lectotype or previously designated neotype, or all
	 * original material associated with the {@link NomenclaturalStatusType#VALID() validly} published "type-bringing"
	 * {@link TaxonNameBase taxon name}, is demonstrably ambiguous and cannot be critically
	 * identified for purposes of the precise application of the taxon name.
	 * When an epitype is designated, the holotype, lectotype or neotype that
	 * the epitype supports must be explicitly cited.
	 *
	 * @see	#HOLOTYPE()
	 * @see	#LECTOTYPE()
	 * @see	#NEOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus EPITYPE(){
		return EPITYPE;
	}

	/**
	 * Returns the "isotype" designation status. </BR>An isotype is any duplicate of
	 * the holotype; it is always a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#HOLOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus ISOTYPE(){
		return ISOTYPE;
	}

	/**
	 * Returns the "syntype" designation status. </BR>A syntype is any one of two or
	 * more {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimens} cited in the {@link TaxonNameBase#getNomenclaturalReference() protologue} of the
	 * "type-bringing" {@link TaxonNameBase taxon name} when no holotype was designated,
	 * or any one of two or more specimens simultaneously designated as types.
	 * 
	 * @see	#HOLOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus SYNTYPE(){
		return SYNTYPE;
	}
	
	/**
	 * Returns the "isosyntype" designation status. </BR>An isosyntype is any 
	 * {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen} cited in the 
	 * protologue of the type-bringing {@link TaxonNameBase taxon name} which is 
	 * a duplicate of a {@link eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus#SYNTYPE() syntype}.
	 * See also article 9.10 of the ICBN
	 * 
	 * @see	#HOLOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus ISOSYNTYPE(){
		return ISOSYNTYPE;
	}

	/**
	 * Returns the "paratype" designation status. </BR>A paratype is a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}
	 * cited in the {@link TaxonNameBase#getNomenclaturalReference() protologue} of the "type-bringing"
	 * {@link TaxonNameBase taxon name} that is neither the holotype nor an isotype,
	 * nor one of the syntypes if two or more specimens were simultaneously
	 * designated as types.
	 * 
	 * @see	#HOLOTYPE()
	 * @see	#ISOTYPE()
	 * @see	#SYNTYPE()
	 */
	public static final SpecimenTypeDesignationStatus PARATYPE(){
		return PARATYPE;
	}

	/**
	 * Returns the "isolectotype" designation status. </BR>
	 * An isolectotype is any
	 * duplicate of the lectotype; it is always a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#LECTOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus ISOLECTOTYPE(){
		return ISOLECTOTYPE;
	}

	/**
	 * Returns the "isoneotype" designation status. </BR>
	 * An isoneotype is any
	 * duplicate of the neotype; it is always a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#NEOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus ISONEOTYPE(){
		return ISONEOTYPE;
	}
	/**
	 * Returns the "paraneotype" designation status. </BR>
	 * A paraneotype is a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen},
	 * cited when selecting a neotype, other than the neotype itself. Also
	 * called "neoparatype" in zoology.
	 * 
	 * @see	#NEOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus PARANEOTYPE(){
		return PARANEOTYPE;
	}

	/**
	 * Returns the "second step lectotype" designation status. </BR>
	 * A second step lectotype is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration}, designated as lectotype
	 * in order to substitute another already existing lectotype.
	 * 
	 * @see	#LECTOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus SECOND_STEP_LECTOTYPE(){
		return SECOND_STEP_LECTOTYPE;
	}

	/**
	 * Returns the "second step neotype" designation status. </BR>
	 * A second step neotype is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration}, designated as neotype
	 * in order to substitute another already existing neotype.
	 * 
	 * @see	#LECTOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus SECOND_STEP_NEOTYPE(){
		return SECOND_STEP_NEOTYPE;
	}

	/**
	 * Returns the "paralectotype" designation status. </BR>
	 * A paralectotype is a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}, cited when designating a lectotype, other than
	 * the lectotype itself. Also called "lectoparatype" in zoology.
	 * 
	 * @see	#LECTOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus PARALECTOTYPE(){
		return PARALECTOTYPE;
	}

	/**
	 * Returns the "isoepitype" designation status. An isoepitype is any
	 * duplicate of the epitype; it is always a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#EPITYPE()
	 */
	public static final SpecimenTypeDesignationStatus ISOEPITYPE(){
		return ISOEPITYPE;
	}

	/**
	 * Returns the "iconotype" designation status. An iconotype is a holotype or
	 * a lectotype that is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase drawing}
	 * and not a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * "Iconotype" does not have type status according to the ICBN.
	 * 
	 * @see	#HOLOTYPE()
	 * @see	#LECTOTYPE()
	 */
	public static final SpecimenTypeDesignationStatus ICONOTYPE(){
		return ICONOTYPE;
	}

	/**
	 * Returns the "unspecific" type designation status. Used if from literature where it is not 
	 * clear if it refers to another publication not cited or if it is a new designation.
	 * 
	 */
	public static final SpecimenTypeDesignationStatus UNSPECIFIC(){
		return UNSPECIFIED;
	}

	/**
	 * Returns the "original material" type designation status.
	 * Botanical term for material from which a lectotype can be designated.
	 * "Original material" does not have type status according to the ICBN.
	 * 
	 */
	public static final SpecimenTypeDesignationStatus ORIGINAL_MATERIAL(){
		return ORIGINAL_MATERIAL;
	}
	
	
	/**
	 * Returns the "phototype" type designation status.
	 * "Phototype" does not have type status according to the ICBN.
	 * 
	 */
	public static final SpecimenTypeDesignationStatus PHOTOTYPE(){
		return PHOTOTYPE;
	}
	
	@Override
	protected void setDefaultTerms(TermVocabulary<SpecimenTypeDesignationStatus> termVocabulary) {
		SpecimenTypeDesignationStatus.EPITYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidEpitype);
		SpecimenTypeDesignationStatus.HOLOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidHolotype);
		SpecimenTypeDesignationStatus.ICONOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidIconotype);
		SpecimenTypeDesignationStatus.ISOEPITYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidIsoepitype);
		SpecimenTypeDesignationStatus.ISOLECTOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidIsolectotype);
		SpecimenTypeDesignationStatus.ISONEOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidIsoneotype);
		SpecimenTypeDesignationStatus.ISOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidIsotype);
		SpecimenTypeDesignationStatus.LECTOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidLectotype);
		SpecimenTypeDesignationStatus.NEOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidNeotype);
		SpecimenTypeDesignationStatus.PARALECTOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidParalectotype);
		SpecimenTypeDesignationStatus.PARANEOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidParaneotype);
		SpecimenTypeDesignationStatus.PARATYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidParatype);
		SpecimenTypeDesignationStatus.PHOTOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidPhototype);
		SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE= termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidSecondStepLectotype);
		SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidSecondStepNeotype);
		SpecimenTypeDesignationStatus.SYNTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidSyntype);		
		SpecimenTypeDesignationStatus.ISOSYNTYPE = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidIsosyntype);
		SpecimenTypeDesignationStatus.UNSPECIFIED = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidUnspecified);		
		SpecimenTypeDesignationStatus.ORIGINAL_MATERIAL = termVocabulary.findTermByUuid(SpecimenTypeDesignationStatus.uuidOriginalMaterial);
	}

}