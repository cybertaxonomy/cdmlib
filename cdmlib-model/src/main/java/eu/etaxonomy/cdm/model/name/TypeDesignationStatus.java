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

import eu.etaxonomy.cdm.model.common.OrderedTermBase;
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
@XmlType(name = "TypeDesignationStatus")
@Entity
//@Audited
public class TypeDesignationStatus extends OrderedTermBase<TypeDesignationStatus> {
	static Logger logger = Logger.getLogger(TypeDesignationStatus.class);

	private static TypeDesignationStatus PHOTOTYPE;

	private static TypeDesignationStatus ICONOTYPE;

	private static TypeDesignationStatus ISOEPITYPE;

	private static TypeDesignationStatus PARALECTOTYPE;

	private static TypeDesignationStatus SECOND_STEP_NEOTYPE;

	private static TypeDesignationStatus SECOND_STEP_LECTOTYPE;

	private static TypeDesignationStatus PARANEOTYPE;

	private static TypeDesignationStatus ISONEOTYPE;

	private static TypeDesignationStatus ISOLECTOTYPE;

	private static TypeDesignationStatus PARATYPE;

	private static TypeDesignationStatus SYNTYPE;

	private static TypeDesignationStatus ISOTYPE;

	private static TypeDesignationStatus EPITYPE;

	private static TypeDesignationStatus NEOTYPE;

	private static TypeDesignationStatus LECTOTYPE;

	private static TypeDesignationStatus HOLOTYPE;

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

	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty type designation status instance.
	 * 
	 * @see 	#TypeDesignationStatus(String, String, String)
	 */
	public TypeDesignationStatus() {
		super();
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
	public TypeDesignationStatus(String term, String label, String labelAbbrev) {
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
	public static final TypeDesignationStatus HOLOTYPE(){
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
	public static final TypeDesignationStatus LECTOTYPE(){
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
	public static final TypeDesignationStatus NEOTYPE(){
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
	public static final TypeDesignationStatus EPITYPE(){
		return EPITYPE;
	}

	/**
	 * Returns the "isotype" designation status. An isotype is any duplicate of
	 * the holotype; it is always a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#HOLOTYPE()
	 */
	public static final TypeDesignationStatus ISOTYPE(){
		return ISOTYPE;
	}

	/**
	 * Returns the "syntype" designation status. A syntype is any one of two or
	 * more {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimens} cited in the {@link TaxonNameBase#getNomenclaturalReference() protologue} of the
	 * "type-bringing" {@link TaxonNameBase taxon name} when no holotype was designated,
	 * or any one of two or more specimens simultaneously designated as types.
	 * 
	 * @see	#HOLOTYPE()
	 */
	public static final TypeDesignationStatus SYNTYPE(){
		return SYNTYPE;
	}

	/**
	 * Returns the "paratype" designation status. A paratype is a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}
	 * cited in the {@link TaxonNameBase#getNomenclaturalReference() protologue} of the "type-bringing"
	 * {@link TaxonNameBase taxon name} that is neither the holotype nor an isotype,
	 * nor one of the syntypes if two or more specimens were simultaneously
	 * designated as types.
	 * 
	 * @see	#HOLOTYPE()
	 * @see	#ISOTYPE()
	 * @see	#SYNTYPE()
	 */
	public static final TypeDesignationStatus PARATYPE(){
		return PARATYPE;
	}

	/**
	 * Returns the "isolectotype" designation status. An isolectotype is any
	 * duplicate of the lectotype; it is always a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#LECTOTYPE()
	 */
	public static final TypeDesignationStatus ISOLECTOTYPE(){
		return ISOLECTOTYPE;
	}

	/**
	 * Returns the "isoneotype" designation status. An isoneotype is any
	 * duplicate of the neotype; it is always a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#NEOTYPE()
	 */
	public static final TypeDesignationStatus ISONEOTYPE(){
		return ISONEOTYPE;
	}
	/**
	 * Returns the "paraneotype" designation status. A paraneotype is a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen},
	 * cited when selecting a neotype, other than the neotype itself. Also
	 * called "neoparatype" in zoology.
	 * 
	 * @see	#NEOTYPE()
	 */
	public static final TypeDesignationStatus PARANEOTYPE(){
		return PARANEOTYPE;
	}

	/**
	 * Returns the "second step lectotype" designation status. A second step
	 * lectotype is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration}, designated as lectotype
	 * in order to substitute another already existing lectotype.
	 * 
	 * @see	#LECTOTYPE()
	 */
	public static final TypeDesignationStatus SECOND_STEP_LECTOTYPE(){
		return SECOND_STEP_LECTOTYPE;
	}

	/**
	 * Returns the "second step neotype" designation status. A second step
	 * neotype is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimen or illustration}, designated as neotype
	 * in order to substitute another already existing neotype.
	 * 
	 * @see	#LECTOTYPE()
	 */
	public static final TypeDesignationStatus SECOND_STEP_NEOTYPE(){
		return SECOND_STEP_NEOTYPE;
	}

	/**
	 * Returns the "paralectotype" designation status. A paralectotype is a
	 * {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}, cited when designating a lectotype, other than
	 * the lectotype itself. Also called "lectoparatype" in zoology.
	 * 
	 * @see	#LECTOTYPE()
	 */
	public static final TypeDesignationStatus PARALECTOTYPE(){
		return PARALECTOTYPE;
	}

	/**
	 * Returns the "isoepitype" designation status. An isoepitype is any
	 * duplicate of the epitype; it is always a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#EPITYPE()
	 */
	public static final TypeDesignationStatus ISOEPITYPE(){
		return ISOEPITYPE;
	}

	/**
	 * Returns the "iconotype" designation status. An iconotype is a holotype or
	 * a lectotype that is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase drawing}
	 * and not a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#HOLOTYPE()
	 * @see	#LECTOTYPE()
	 */
	public static final TypeDesignationStatus ICONOTYPE(){
		return ICONOTYPE;
	}

	/**
	 * Returns the "iconotype" designation status. An iconotype is a holotype or
	 * a lectotype that is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase photograph}
	 * and not a {@link eu.etaxonomy.cdm.model.occurrence.Specimen specimen}.
	 * 
	 * @see	#HOLOTYPE()
	 * @see	#LECTOTYPE()
	 */
	public static final TypeDesignationStatus PHOTOTYPE(){
		return PHOTOTYPE;
	}
	
	@Override
	protected void setDefaultTerms(TermVocabulary<TypeDesignationStatus> termVocabulary) {
		TypeDesignationStatus.EPITYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidEpitype);
		TypeDesignationStatus.HOLOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidHolotype);
		TypeDesignationStatus.ICONOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidIconotype);
		TypeDesignationStatus.ISOEPITYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidIsoepitype);
		TypeDesignationStatus.ISOLECTOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidIsolectotype);
		TypeDesignationStatus.ISONEOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidIsoneotype);
		TypeDesignationStatus.ISOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidIsotype);
		TypeDesignationStatus.LECTOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidLectotype);
		TypeDesignationStatus.NEOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidNeotype);
		TypeDesignationStatus.PARALECTOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidParalectotype);
		TypeDesignationStatus.PARANEOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidParaneotype);
		TypeDesignationStatus.PARATYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidParatype);
		TypeDesignationStatus.PHOTOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidPhototype);
		TypeDesignationStatus.SECOND_STEP_LECTOTYPE= termVocabulary.findTermByUuid(TypeDesignationStatus.uuidSecondStepLectotype);
		TypeDesignationStatus.SECOND_STEP_NEOTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidSecondStepNeotype);
		TypeDesignationStatus.SYNTYPE = termVocabulary.findTermByUuid(TypeDesignationStatus.uuidSyntype);		
	}

}