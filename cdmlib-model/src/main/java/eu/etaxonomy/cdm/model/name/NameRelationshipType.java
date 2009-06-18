/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * The class representing the categories of {@link NameRelationship taxon name relationships} between
 * two {@link TaxonNameBase taxon names}. These name relationship types are
 * based on the concrete {@link NomenclaturalCode nomenclatural code} governing
 * the taxon names involved in the name relationship or on decisions taken by
 * the competent authorities; they do not depend on the use made of these
 * taxon names in a particular reference or in a particular taxonomic treatment.
 * Most relationships are to be understood as 'is .... of': for instance 
 * <i>Linum radiola</i> L. is a replaced synonym of <i>Radiola linoides</i> Roth or
 * <i>Astragalus rhizanthus</i> Boiss. is a later homonym of
 * <i>Astragalus rhizanthus</i> Royle.  
 * <P>
 * A standard (ordered) list of name relationship type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional name relationship
 * types if needed. 
 * <P>
 * This class corresponds partially to: <ul>
 * <li> TaxonRelationshipTerm and NomenclaturalNoteTypeTerm according to the TDWG ontology
 * <li> RelationshipType and NomenclaturalNoteType according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameRelationshipType")
@Entity
@Audited
public class NameRelationshipType extends RelationshipTermBase<NameRelationshipType> {
	static Logger logger = Logger.getLogger(NameRelationshipType.class);

	private static NameRelationshipType ORTHOGRAPHIC_VARIANT;

	private static NameRelationshipType LATER_HOMONYM;

	private static NameRelationshipType TREATED_AS_LATER_HOMONYM;

	private static NameRelationshipType ALTERNATIVE_NAME;

	private static NameRelationshipType BASIONYM;

	private static NameRelationshipType REPLACED_SYNONYM;

	private static NameRelationshipType CONSERVED_AGAINST;

	private static NameRelationshipType VALIDATED_BY_NAME;

	private static NameRelationshipType LATER_VALIDATED_BY_NAME;

	private static NameRelationshipType BLOCKING_NAME_FOR;

	private static final UUID uuidOrthographicVariant = UUID.fromString("eeaea868-c4c1-497f-b9fe-52c9fc4aca53");
	private static final UUID uuidLaterHomonym = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
	private static final UUID uuidTreatedAsLaterHomonym = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
	private static final UUID uuidAlternativeName = UUID.fromString("049c6358-1094-4765-9fae-c9972a0e7780");
	private static final UUID uuidBasionym = UUID.fromString("25792738-98de-4762-bac1-8c156faded4a");
	private static final UUID uuidReplacedSynonym = UUID.fromString("71c67c38-d162-445b-b0c2-7aba56106696");
	private static final UUID uuidConservedAgainst = UUID.fromString("e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a");
	private static final UUID uuidValidatedByName = UUID.fromString("a176c9ad-b4c2-4c57-addd-90373f8270eb");
	private static final UUID uuidLaterValidatedByName = UUID.fromString("a25ee4c1-863a-4dab-9499-290bf9b89639");
	private static final UUID uuidBlockingNameFor = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty name relationship type instance.
	 * 
	 * @see 	#NameRelationshipType(String, String, String, boolean, boolean)
	 */
	public NameRelationshipType() {
		super();
	}
	
	/** 
	 * Class constructor: creates an additional name relationship type
	 * instance with a description, a label, a label abbreviation and the flags
	 * indicating whether <i>this</i> new name relationship type is symmetric and/or
	 * transitive.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new name relationship type to be created 
	 * @param	label  		 the string identifying the new name relationship
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new name relationship type to be created
	 * @param	symmetric	 the boolean indicating whether the new name
	 * 						 relationship type to be created is symmetric
	 * @param	transitive	 the boolean indicating whether the new name
	 * 						 relationship type to be created is transitive
	 * @see 				 #NameRelationshipType()
	 */
	public NameRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev, symmetric, transitive);
	}


	//********* METHODS **************************************/

	// TODO this method should be moved to consistency proof classes
	/**
	 * Returns the boolean value indicating whether the nomenclatural status
	 * type of the {@link eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom() first taxon name}
	 * involved in a name relationship with <i>this</i> name relationship type should
	 * be "invalid" (true) or not (false). Returns false if <i>this</i> name
	 * relationship status type is null.
	 *
	 * @see  #isLegitimateType()
	 * @see  #isIllegitimateType()
	 * @see  NomenclaturalStatusType#isInvalidType()
	 * @see  eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public boolean isInvalidType(){
		if (this.equals(VALIDATED_BY_NAME()) || 
				this.equals(LATER_VALIDATED_BY_NAME())
			){
			return true;	
		}else{
			return false;
		}
	}

	// TODO this method should be moved to consistency proof classes
	/**
	 * Returns the boolean value indicating whether the nomenclatural status
	 * type of the {@link eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom() first taxon name}
	 * involved in a name relationship with <i>this</i> name relationship type should
	 * be "legitimate" (true) or not (false). Returns false if <i>this</i> name
	 * relationship status type is null.
	 *
	 * @see  #isInvalidType()
	 * @see  #isIllegitimateType()
	 * @see  NomenclaturalStatusType#isLegitimateType()
	 * @see  eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public boolean isLegitimateType(){
		if (this.equals(BASIONYM()) || 
				this.equals(REPLACED_SYNONYM()) || 
				this.equals(ALTERNATIVE_NAME()) ||
				this.equals(CONSERVED_AGAINST())
			){
			return true;	
		}else{
			return false;
		}
	}

	// TODO this method should be moved to consistency proof classes
	/**
	 * Returns the boolean value indicating whether the nomenclatural status
	 * type of the {@link eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom() first taxon name}
	 * involved in a name relationship with <i>this</i> name relationship type should
	 * be "illegitimate" (true) or not (false). Returns false if <i>this</i> name
	 * relationship status type is null.
	 *
	 * @see  #isInvalidType()
	 * @see  #isLegitimateType()
	 * @see  NomenclaturalStatusType#isIllegitimateType()
	 * @see  eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public boolean isIllegitimateType(){
		//TODO: implement isX method. Maybe as persistent class attribute?
		//TODO: RejectedInFavour,
		if (this.equals(LATER_HOMONYM()) || 
				this.equals(TREATED_AS_LATER_HOMONYM())
			){
			return true;	
		}else{
			return false;
		}
	}
	
	@Transient
	public boolean isBasionymRelation(){
		if (BASIONYM() == null){
			throw new IllegalStateException("NameRelationships have not been initialized yet. Please initialize DefinedTerms first");
		}
		return this.equals(BASIONYM());
	}
	
	@Transient
	public boolean isReplacedSynonymRelation(){
		if (REPLACED_SYNONYM() == null){
			throw new IllegalStateException("NameRelationships have not been initialized yet. Please initialize DefinedTerms first");
		}
		return this.equals(REPLACED_SYNONYM());
	}

	
	/**
	 * Returns the "orthographic variant" name relationship type. The first
	 * {@link TaxonNameBase taxon name} involved in such a relationship is an 
	 * orthographic variant of the second taxon name. The two {@link TaxonNameBase taxon names}
	 * involved in such a relationship must have the same {@link NonViralName#getAuthorshipCache() authorship}
	 * and {@link Rank rank}, belong to the same {@link HomotypicalGroup homotypical group} and their name parts
	 * must be almost identical (so one usually does not differentiate them).<BR>
	 * For instance <i>Angelica silvestris</i> L. is an orthographic variant of
	 * <i>Angelica sylvestris</i> L.<BR>
	 * This type is symmetric and transitive.
	 */
	public static final NameRelationshipType ORTHOGRAPHIC_VARIANT(){
		  return ORTHOGRAPHIC_VARIANT;
	}
	/**
	 * Returns the "later homonym" name relationship type. The first
	 * {@link TaxonNameBase taxon name} involved in such a relationship should
	 * have been published after the second taxon name. The two {@link TaxonNameBase taxon names}
	 * involved in such a relationship must belong to different
	 * {@link HomotypicalGroup homotypical groups}, have in general different
	 * {@link NonViralName#getAuthorshipCache() authorship} and their name parts (excluding infraspecific
	 * {@link Rank ranks}) must be (almost) identical, so one could be mistaken for
	 * the other one. The first taxon name is "illegitimate" and the second one
	 * is "legitimate" (this corresponds to "invalid" and "valid" in case of
	 * {@link ZoologicalName zoological names}).<BR>
	 * For instance <i>Astragalus rhizanthus</i> Boiss. is a later homonym of
	 * <i>Astragalus rhizanthus</i> Royle.<BR>
	 * This type is not symmetric but transitive.
	 * 
	 * @see	NomenclaturalStatusType#isIllegitimateType()
	 * @see	NomenclaturalStatusType#isLegitimateType()
	 */
	public static final NameRelationshipType LATER_HOMONYM(){
	  return LATER_HOMONYM; 
	}
	/**
	 * Returns the "treated as later homonym" name relationship type. The first
	 * {@link TaxonNameBase taxon name} involved in such a relationship is
	 * treated as an homonym although it has been published before the second
	 * taxon name. The two taxon names involved must belong to different
	 * {@link HomotypicalGroup homotypical groups} and their name parts (excluding
	 * {@link Rank#isInfraSpecific() infraspecific ranks} and {@link NonViralName#getAuthorshipCache() authorship}) must be
	 * almost identical (so one could be mistaken for the other). The first
	 * taxon name is "illegitimate" and the second one is "legitimate" (this
	 * corresponds to "invalid" and "valid" in case of {@link ZoologicalName zoological names}).<BR>
	 * This type is not symmetric but transitive.
	 * 
	 * @see	#LATER_HOMONYM()
	 * @see	NomenclaturalStatusType#isIllegitimateType()
	 * @see	NomenclaturalStatusType#isLegitimateType()
	 */
	public static final NameRelationshipType TREATED_AS_LATER_HOMONYM(){
	  return TREATED_AS_LATER_HOMONYM; 
	}
	/**
	 * Returns the "alternative name" name relationship type. Both {@link TaxonNameBase taxon names}
	 * involved in such a relationship are family names. The first one is a
	 * classical name long in use, in some cases, even before 1753 and is considered as
	 * {@link NomenclaturalStatusType#VALID() valid} and also {@link NomenclaturalStatusType#isLegitimateType() legitimate}
	 * although it does not follow the rules for family names (see Article 18 of
	 * the ICBN). An alternative name is typified by the type of the name
	 * it is alternative to (so both must belong to the same
	 * {@link HomotypicalGroup homotypical group}).<BR>
	 * For instance <i>Cruciferae</i> Adans is an alternative name to
	 * <i>Brassicaceae</i> Lindl.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final NameRelationshipType ALTERNATIVE_NAME(){
	  return ALTERNATIVE_NAME;
	}
	/**
	 * Returns the "basionym" name relationship type. The first {@link TaxonNameBase taxon name} 
	 * involved in such a relationship is the "basionym" of the second taxon
	 * name. Both taxon names belong to the same {@link HomotypicalGroup homotypical group}).
	 * The basionym is the epithet-bringing taxon name (first taxon name 
	 * ever validly published given to the same {@link Rank#isInfraGeneric() infrageneric}
	 * taxon, the epithet of which is the same as in the second taxon name 
	 * originated through a reclassification).<BR>
	 * According to the ICBN the author of the basionym must be mentioned in the
	 * later taxon name (by placing it in parentheses before the authority of
	 * the new combination). For instance <i>Pinus abies</i> L. is the basionym of
	 * <i>Picea abies</i> (L.) H. Karst.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final NameRelationshipType BASIONYM(){
	  return BASIONYM;
	}
	/**
	 * Returns the "replaced synonym" name relationship type. The first 
	 * {@link TaxonNameBase taxon name} involved in such a relationship is the
	 * "replaced synonym" of the second taxon name. Both taxon names belong to
	 * the same {@link HomotypicalGroup homotypical group}. The replaced synonym is the
	 * first taxon name ever validly published given to the same
	 * {@link Rank#isInfraGeneric() infrageneric} taxon that is either itself a
	 * "later homonym" or the epithet of which could not be used in the new
	 * taxon name originated through a reclassification. A new epithet must be
	 * proposed if the use of the original epithet leads to an already existing
	 * taxon name (for another taxon) or in botany to autonyms (since the ICBN
	 * does not allow such names where epithet and genus name are the same).<BR>
	 * For instance <i>Spartium biflorum</i> Desf. is the replaced synonym of
	 * of <i>Cytisus fontanesii</i> Spach ("novum" taxon name) because at the time
	 * of reclassification a taxon name <i>Cytisus biflorum</i> had been already
	 * published by L'H�r.<BR>
	 * This type is neither symmetric nor transitive.
	 * 
	 * @see #BASIONYM()
	 * @see #LATER_HOMONYM()
	 * @see NomenclaturalStatusType#NOVUM()
	 */
	public static final NameRelationshipType REPLACED_SYNONYM(){
	  return REPLACED_SYNONYM;
	}
	/**
	 * Returns the "conserved against" name relationship type. Both {@link TaxonNameBase taxon names}
	 * involved in such a relationship belong to the same {@link HomotypicalGroup homotypical group}.
	 * Competent authorities decided, regardless of the general
	 * nomenclatural rules, to handle the first one as the "legitimate"
	 * one and the second taxon name as "illegitimate" (this corresponds to
	 * "valid" and "invalid" in case of {@link ZoologicalName zoological names}).<BR>
	 * For instance <i>Cephaloziella</i> (Spruce) Schiffn. is conserved against
	 * <i>Dichiton</i> Mont.<BR>
	 * This type is neither symmetric nor transitive.
	 * 
	 * @see NomenclaturalStatusType#CONSERVED()
	 * @see NomenclaturalStatusType#REJECTED()
	 * @see NomenclaturalStatusType#isLegitimateType()
	 * @see NomenclaturalStatusType#isIllegitimateType()
	 */
	public static final NameRelationshipType CONSERVED_AGAINST(){
	  return CONSERVED_AGAINST;
	}
	/**
	 * Returns the "validated by name" name relationship type. The two
	 * {@link TaxonNameBase taxon names} involved in such a relationship were published
	 * in order to define the same taxonomical group but the first
	 * (earlier) taxon name was invalidly published whereas the second (later)
	 * taxon name is the one which was validly published for the first time.<BR>
	 * This type is neither symmetric nor transitive.
	 * 
	 * @see		NomenclaturalStatusType#isInvalidType()
	 * @see		NomenclaturalStatusType#VALID()
	 */
	public static final NameRelationshipType VALIDATED_BY_NAME(){
	  return VALIDATED_BY_NAME;
	}
	/**
	 * Returns the "later validated by name" name relationship type. The two
	 * {@link TaxonNameBase taxon names} involved in such a relationship were published
	 * in order to define the same taxonomical group but the first
	 * (earlier) taxon name was invalidly published whereas the second (later)
	 * taxon name is the one which was validly published for the first time.<BR>
	 * This type is neither symmetric nor transitive.
	 * 
	 * @see		NomenclaturalStatusType#isInvalidType()
	 * @see		NomenclaturalStatusType#VALID()
	 */
	public static final NameRelationshipType LATER_VALIDATED_BY_NAME(){
	  return LATER_VALIDATED_BY_NAME; 
	}
	/**
	 * Returns the "blocking name" name relationship type. The first 
	 * {@link TaxonNameBase taxon name} involved in such a relationship is the
	 * "blocking name" for the second taxon name. Both taxon names belong to
	 * different {@link HomotypicalGroup homotypical groups}). The blocking taxon name is the
	 * {@link Rank#isInfraGeneric() infrageneric} taxon name, already published at the time of
	 * reclassification, which makes illegitim (because of homonymy) the use of
	 * the epithet in the second taxon name originated through a reclassification.
	 * Therefore a "replaced synonym" name relationship arises.<BR>
	 * For instance  <i>Cytisus biflorum</i> L'H�r. is the blocking name for
	 * <i>Cytisus fontanesii</i> Spach ("novum" taxon name) when reclassifying
	 * <i>Spartium biflorum</i> Desf. from <i>Spartium</i> to <i>Cytisus</i>.<BR>
	 * This type is neither symmetric nor transitive.
	 * 
	 * @see #REPLACED_SYNONYM()
	 * @see #LATER_HOMONYM()
	 * @see NomenclaturalStatusType#NOVUM()
	 */
	public static final NameRelationshipType BLOCKING_NAME_FOR(){
	  return  BLOCKING_NAME_FOR;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<NameRelationshipType> termVocabulary) {
		NameRelationshipType.ALTERNATIVE_NAME = termVocabulary.findTermByUuid(NameRelationshipType.uuidAlternativeName);
		NameRelationshipType.BASIONYM = termVocabulary.findTermByUuid(NameRelationshipType.uuidBasionym);
		NameRelationshipType.BLOCKING_NAME_FOR = termVocabulary.findTermByUuid(NameRelationshipType.uuidBlockingNameFor);
		NameRelationshipType.CONSERVED_AGAINST = termVocabulary.findTermByUuid(NameRelationshipType.uuidConservedAgainst);
		NameRelationshipType.LATER_HOMONYM = termVocabulary.findTermByUuid(NameRelationshipType.uuidLaterHomonym);
		NameRelationshipType.LATER_VALIDATED_BY_NAME = termVocabulary.findTermByUuid(NameRelationshipType.uuidLaterValidatedByName);
		NameRelationshipType.ORTHOGRAPHIC_VARIANT = termVocabulary.findTermByUuid(NameRelationshipType.uuidOrthographicVariant);
		NameRelationshipType.REPLACED_SYNONYM = termVocabulary.findTermByUuid(NameRelationshipType.uuidReplacedSynonym);
		NameRelationshipType.TREATED_AS_LATER_HOMONYM = termVocabulary.findTermByUuid(NameRelationshipType.uuidTreatedAsLaterHomonym);
		NameRelationshipType.VALIDATED_BY_NAME = termVocabulary.findTermByUuid(NameRelationshipType.uuidValidatedByName);		
	}

	@Override
	public NameRelationshipType readCsvLine(Class<NameRelationshipType> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		return super.readCsvLine(termClass, csvLine, terms);
	}
}