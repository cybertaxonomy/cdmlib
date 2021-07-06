/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * The class representing the categories of {@link NameRelationship taxon name relationships} between
 * two {@link TaxonName taxon names}. These name relationship types are
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
 * @since 08-Nov-2007 13:06:38
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameRelationshipType", propOrder = {
    "nomenclaturalStanding",
    "nomenclaturalStandingInverse"}
)
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.DefinedTermBase")
@Audited
public class NameRelationshipType extends RelationshipTermBase<NameRelationshipType>
        implements INomenclaturalStanding {

    private static final long serialVersionUID = 8504916205254159334L;
	static Logger logger = Logger.getLogger(NameRelationshipType.class);

	private static final UUID uuidOrthographicVariant = UUID.fromString("eeaea868-c4c1-497f-b9fe-52c9fc4aca53");
	private static final UUID uuidMisspelling = UUID.fromString("c6f9afcb-8287-4a2b-a6f6-4da3a073d5de");
	private static final UUID uuidEmendation = UUID.fromString("6e23ad45-3f2a-462b-ad87-d2389cd6e26c");
	private static final UUID uuidLaterHomonym = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
	private static final UUID uuidTreatedAsLaterHomonym = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
	private static final UUID uuidAlternativeName = UUID.fromString("049c6358-1094-4765-9fae-c9972a0e7780");
	private static final UUID uuidBasionym = UUID.fromString("25792738-98de-4762-bac1-8c156faded4a");
	private static final UUID uuidReplacedSynonym = UUID.fromString("71c67c38-d162-445b-b0c2-7aba56106696");
	private static final UUID uuidConservedAgainst = UUID.fromString("e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a");
	private static final UUID uuidValidatedByName = UUID.fromString("a176c9ad-b4c2-4c57-addd-90373f8270eb");
	private static final UUID uuidLaterValidatedByName = UUID.fromString("a25ee4c1-863a-4dab-9499-290bf9b89639");
	private static final UUID uuidBlockingNameFor = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
	private static final UUID uuidLaterIsonym = UUID.fromString("29ab238d-598d-45b9-addd-003cf39ccc3e");
	private static final UUID uuidNonUnspecific = UUID.fromString("78360e2a-159d-4e2f-893e-8666805840fa");


	public static NameRelationshipType NewInstance(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		return new NameRelationshipType(term, label, labelAbbrev, symmetric, transitive);
	}

    /**
     * The {@link NomenclaturalStanding nomenclatural standing} of a name status type for
     * the "from"-name in a name relationship.
     * It is usually needed for correct formatting of a name in a synonymy by e.g. using
     * a dash instead of equal sign in front.
     */
    @XmlAttribute(name ="NomenclaturalStanding")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.name.NomenclaturalStanding")}
    )
    @Audited
	private NomenclaturalStanding nomenclaturalStanding;

    /**
     * The {@link NomenclaturalStanding nomenclatural standing} of a name status type for
     * the "to"-name in a name relationship.
     * It is usually needed for correct formatting of a name in a synonymy by e.g. using
     * a dash instead of equal sign in front.
     */
    @XmlAttribute(name ="NomenclaturalStanding")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.name.NomenclaturalStanding")}
    )
    @Audited
    private NomenclaturalStanding nomenclaturalStandingInverse;

	protected static Map<UUID, NameRelationshipType> termMap = null;

	protected static NameRelationshipType findTermByUuid(UUID uuid){
		if (termMap == null || termMap.isEmpty()){
		    return getTermByClassAndUUID(NameRelationshipType.class, uuid);
		} else {
		    return termMap.get(uuid);
		}
	}

//********************************** Constructor *********************************/

  	//for hibernate use only
  	@Deprecated
  	protected  NameRelationshipType() {
		super(TermType.NameRelationshipType);
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
	private NameRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(TermType.NameRelationshipType, term, label, labelAbbrev, symmetric, transitive);
	}

// ************************ GETTER / SETTER ******************/

    public NomenclaturalStanding getNomenclaturalStanding() {
        return nomenclaturalStanding;
    }
    public void setNomenclaturalStanding(NomenclaturalStanding nomenclaturalStanding) {
        this.nomenclaturalStanding = nomenclaturalStanding;
    }

    public NomenclaturalStanding getNomenclaturalStandingInverse() {
        return nomenclaturalStandingInverse;
    }
    public void setNomenclaturalStandingInverse(NomenclaturalStanding nomenclaturalStandingInverse) {
        this.nomenclaturalStandingInverse = nomenclaturalStandingInverse;
    }

//************************** METHODS ********************************

	@Override
	public void resetTerms(){
		termMap = null;
	}

    @Override
    @Transient
    public boolean isInvalidExplicit() {
        return this.nomenclaturalStanding.isInvalidExplicit();
    }
    @Transient
    public boolean isInvalidExplicitInverse() {
        return this.nomenclaturalStandingInverse.isInvalidExplicit();
    }

    @Override
    @Transient
    public boolean isIllegitimate() {
        return this.nomenclaturalStanding.isLegitimate();
    }
    @Transient
    public boolean isIllegitimateInverse() {
        return this.nomenclaturalStandingInverse.isLegitimate();
    }

    @Override
    @Transient
    public boolean isValidExplicit() {
        return this.nomenclaturalStanding.isValidExplicit();
    }
    @Transient
    public boolean isValidExplicitInverse() {
        return this.nomenclaturalStandingInverse.isValidExplicit();
    }

    @Override
    @Transient
    public boolean isNoStatus() {
        return this.nomenclaturalStanding.isNoStatus();
    }
    @Transient
    public boolean isNoStatusInverse() {
        return this.nomenclaturalStandingInverse.isNoStatus();
    }

    @Override
    @Transient
    public boolean isInvalid() {
        return this.nomenclaturalStanding.isInvalid();
    }
    @Transient
    public boolean isInvalidInverse() {
        return this.nomenclaturalStandingInverse.isInvalid();
    }

    @Override
    @Transient
    public boolean isLegitimate() {
        //later homonym, treated as later homonym, ...
        return this.nomenclaturalStanding.isLegitimate();
    }
    @Transient
    public boolean isLegitimateInverse() {
        return this.nomenclaturalStandingInverse.isLegitimate();
    }

    @Override
    @Transient
    public boolean isValid() {
        return this.nomenclaturalStanding.isValid();
    }
    @Transient
    public boolean isValidInverse() {
        return this.nomenclaturalStandingInverse.isValid();
    }

    @Override
    @Transient
    public boolean isDesignationOnly(){
        return this.nomenclaturalStanding.isDesignationOnly();
    }
    @Transient
    public boolean isDesignationOnlyInverse(){
        return this.nomenclaturalStandingInverse.isDesignationOnly();
    }

	@Transient
	protected boolean isRelationshipType(NameRelationshipType type) {
	    if (type == null){
	        throw new IllegalStateException("NameRelationships have not been initialized yet. Please initialize DefinedTerms first");
	    }
	    return this.equals(type);
	}

	@Transient
	public boolean isBasionymRelation(){
        return isRelationshipType(BASIONYM());
	}

	@Transient
	public boolean isReplacedSynonymRelation(){
        return isRelationshipType(REPLACED_SYNONYM());
	}


	/**
	 * Returns the "orthographic variant" name relationship type. The first
	 * {@link TaxonName taxon name} involved in such a relationship is an
	 * orthographic variant of the second taxon name. The two {@link TaxonName taxon names}
	 * involved in such a relationship must have the same {@link NonViralName#getAuthorshipCache() authorship}
	 * and {@link Rank rank}, belong to the same {@link HomotypicalGroup homotypical group} and their name parts
	 * must be almost identical (so one usually does not differentiate them).<BR>
	 * For instance <i>Angelica silvestris</i> L. is an orthographic variant of
	 * <i>Angelica sylvestris</i> L.
	 * <BR>
	 * This type is symmetric and transitive but usually orthographic
	 * variant relationships should be organized in a star schema with the (only!)
	 * correct variant in the middle and other variants pointing to it.
	 * <BR>
	 * ICNAFP: Art. 61.2. "For the purpose of this Code, orthographical variants are the various spelling,
	 * compounding, and inflectional forms of a name or its final epithet (including typographical errors)
	 * when only one nomenclatural type is involved."<BR>
	 * Art. 61.1. "Only one orthographical variant of any one name is treated as validly published:
	 * the form that appears in the original publication (but see Art. 6.10), except
	 *
	 * @see #MISSPELLING()
	 */
	public static final NameRelationshipType ORTHOGRAPHIC_VARIANT(){
		  return findTermByUuid(uuidOrthographicVariant);
	}

	/**
	 * Returns the "misspelling" name relationship type. The first
	 * {@link TaxonName taxon name} involved in such a relationship is a
	 * misspelling of the second taxon name. The two {@link TaxonName taxon names}
	 * involved in such a relationship must have the same {@link NonViralName#getAuthorshipCache() authorship}
	 * and {@link Rank rank}, belong to the same {@link HomotypicalGroup homotypical group} and their name parts
	 * must be almost identical (so one usually does not differentiate them).<BR>
	 * For instance <i>Anhelica silvestris</i> L. is a misspelling of
	 * <i>Angelica silvestris</i> L.<BR>
	 * A misspelling is always accicentally (not on purpose). Therefore misspellings are overlapping with
	 * {@link #ORTHOGRAPHIC_VARIANT orthographic variants} (in an old version of this documentation they
	 * were called a subset but it seem doubtful that certain typos are orth. vars. according to Art. 61.2 (ICNAFP).
     * and are complementary to {@link #EMENDATION() emendations}.
	 * This type is symmetric and transitive but usually the misspelling relationships should be organized
	 * in a star schema with the correct variant in the middle and the misspellings pointing to it.<BR>
	 * Misspellings are not handled in the ICNAFP.
	 *
	 * @see #ORTHOGRAPHIC_VARIANT()
	 */
	public static final NameRelationshipType MISSPELLING(){
		  return findTermByUuid(uuidMisspelling);
	}
	/**
	 * Returns the "emendation" name relationship type. The first
	 * {@link TaxonName taxon name} involved in such a relationship is a
	 * misspelling of the second taxon name. The two {@link TaxonName taxon names}
	 * involved in such a relationship must have the same {@link NonViralName#getAuthorshipCache() authorship}
	 * and {@link Rank rank}, belong to the same {@link HomotypicalGroup homotypical group} and their name parts
	 * must be almost identical (so one usually does not differentiate them).<BR>
	 * For instance <i>Angelica silvestris</i> L. is a emendation of
	 * <i>Angelica sylvestris</i> L.<BR>
	 * The name corrected by an emendation has originally been used on purpose (not accidentially)
	 * Therefore emendations are a subset of {@link #ORTHOGRAPHIC_VARIANT orthographic variants} and are
	 * complementary to {@link #MISSPELLING missepllings}. An emendation is always an
	 * {@link #ORTHOGRAPHIC_VARIANT orthographic variant}, too.<BR>
	 * This type is symmetric and transitive but usually the misspelling relationships should be organized
	 * in a star schema with the correct variant in the middle and the misspellings pointing to it.
	 *
	 * TODO IN ICNAFP Art. 47.1 emendations are used for alterations of the diagnostic characters, this
	 * is something completely different. We need to check where the above definition comes from (zoology?)
	 */
	public static final NameRelationshipType EMENDATION(){
		  return findTermByUuid(uuidEmendation);
	}
	/**
	 * Returns the "later homonym" name relationship type. The first
	 * {@link TaxonName taxon name} involved in such a relationship should
	 * have been published after the second taxon name. The two {@link TaxonName taxon names}
	 * involved in such a relationship must belong to different
	 * {@link HomotypicalGroup homotypical groups}, have in general different
	 * {@link NonViralName#getAuthorshipCache() authorship} and their name parts (excluding infraspecific
	 * {@link Rank ranks}) must be (almost) identical, so one could be mistaken for
	 * the other one. The first taxon name is "illegitimate" and the second one
	 * is "legitimate" (this corresponds to "invalid" and "valid" in case of
	 * {@link IZoologicalName zoological names}).<BR>
	 * For instance <i>Astragalus rhizanthus</i> Boiss. is a later homonym of
	 * <i>Astragalus rhizanthus</i> Royle.<BR>
	 * This type is not symmetric but transitive.
	 *
	 * @see	NomenclaturalStatusType#isIllegitimate()
	 * @see	NomenclaturalStatusType#isLegitimate()
	 */
	public static final NameRelationshipType LATER_HOMONYM(){
	  return findTermByUuid(uuidLaterHomonym);
	}


	/**
	 * Returns the "treated as later homonym" name relationship type. The first
	 * {@link TaxonName taxon name} involved in such a relationship is
	 * treated as an homonym although it has been published before the second
	 * taxon name. The two taxon names involved must belong to different
	 * {@link HomotypicalGroup homotypical groups} and their name parts (excluding
	 * {@link Rank#isInfraSpecific() infraspecific ranks} and {@link NonViralName#getAuthorshipCache() authorship}) must be
	 * almost identical (so one could be mistaken for the other). The first
	 * taxon name is "illegitimate" and the second one is "legitimate" (this
	 * corresponds to "invalid" and "valid" in case of {@link IZoologicalName zoological names}).<BR>
	 * This type is not symmetric but transitive.
	 *
	 * @see	#LATER_HOMONYM()
	 * @see	NomenclaturalStatusType#isIllegitimate()
	 * @see	NomenclaturalStatusType#isLegitimate()
	 */
	public static final NameRelationshipType TREATED_AS_LATER_HOMONYM(){
	  return findTermByUuid(uuidTreatedAsLaterHomonym);
	}

	/**
	 * Returns the "later isonym" name relationship type where the first
	 * {@link TaxonName taxon name} involved has been published after the second taxon name.<BR>
	 * In contrast to the {@link #LATER_HOMONYM() later homonym} relationship the two
	 * {@link TaxonName taxon names} involved have the type(s) so they belong to the
	 * same {@link HomotypicalGroup homotypical groups}. As later homonyms they have in general
	 * different {@link NonViralName#getAuthorshipCache() authorship} and their name parts
	 * must be (almost) identical, so one could be mistaken for the other one.<BR>
	 * Later isonyms are validly published names but with a wrong citation. So there are rather errors
	 * then independent names.<BR>
	 * Isonyms are handled in Article 6, Note 2 of the ICNAFP (Melbourne Code):
	 * <code>When the same name, based on the same type, has been published independently at different
	 *  times perhaps by different authors, then only the earliest of these �isonyms� has
	 *  nomenclatural status. The name is always to be cited from its original
	 *  place of valid publication, and later isonyms may be disregarded (but see Art. 14.15).</code>
	 * <BR><BR>
	 * See discussion at: <a href=https://dev.e-taxonomy.eu/redmine/issues/2901>#2901</a>
	 *
	 */
	public static final NameRelationshipType LATER_ISONYM(){
		return findTermByUuid(uuidLaterIsonym);
	}

	/**
	 * Returns the "alternative name" name relationship type. Both {@link TaxonName taxon names}
	 * involved in such a relationship are family names. The first one is a
	 * classical name long in use, in some cases, even before 1753 and is considered as
	 * {@link NomenclaturalStatusType#VALID() valid} and also {@link NomenclaturalStatusType#isLegitimate() legitimate}
	 * although it does not follow the rules for family names (see Article 18 of
	 * the ICBN). An alternative name is typified by the type of the name
	 * it is alternative to (so both must belong to the same
	 * {@link HomotypicalGroup homotypical group}).<BR>
	 * For instance <i>Cruciferae</i> Adans is an alternative name to
	 * <i>Brassicaceae</i> Lindl.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final NameRelationshipType ALTERNATIVE_NAME(){
	  return findTermByUuid(uuidAlternativeName);
	}
	/**
	 * Returns the "basionym" name relationship type. The first {@link TaxonName taxon name}
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
	  return findTermByUuid(uuidBasionym);
	}
	/**
	 * Returns the "replaced synonym" name relationship type. The first
	 * {@link TaxonName taxon name} involved in such a relationship is the
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
	  return findTermByUuid(uuidReplacedSynonym);
	}
	/**
	 * Returns the "conserved against" name relationship type. Both {@link TaxonName taxon names}
	 * involved in such a relationship belong to the same {@link HomotypicalGroup homotypical group}.
	 * Competent authorities decided, regardless of the general
	 * nomenclatural rules, to handle the first one as the "legitimate"
	 * one and the second taxon name as "illegitimate" (this corresponds to
	 * "valid" and "invalid" in case of {@link IZoologicalName zoological names}).<BR>
	 * For instance <i>Cephaloziella</i> (Spruce) Schiffn. is conserved against
	 * <i>Dichiton</i> Mont.<BR>
	 * This type is neither symmetric nor transitive.
	 *
	 * @see NomenclaturalStatusType#CONSERVED()
	 * @see NomenclaturalStatusType#REJECTED()
	 * @see NomenclaturalStatusType#isLegitimate()
	 * @see NomenclaturalStatusType#isIllegitimate()
	 */
	public static final NameRelationshipType CONSERVED_AGAINST(){
	  return findTermByUuid(uuidConservedAgainst);
	}
	/**
	 * Returns the "validated by name" name relationship type. The two
	 * {@link TaxonName taxon names} involved in such a relationship were published
	 * in order to define the same taxonomical group but the first
	 * (earlier) taxon name was invalidly published whereas the second (later)
	 * taxon name is the one which was validly published for the first time.<BR>
	 * This type is neither symmetric nor transitive.
	 *
	 * @see		NomenclaturalStatusType#isInvalid()
	 * @see		NomenclaturalStatusType#VALID()
	 */
	public static final NameRelationshipType VALIDATED_BY_NAME(){
	  return findTermByUuid(uuidValidatedByName);
	}
	/**
	 * Returns the "later validated by name" name relationship type. The two
	 * {@link TaxonName taxon names} involved in such a relationship were published
	 * in order to define the same taxonomical group but the first
	 * (earlier) taxon name was invalidly published whereas the second (later)
	 * taxon name is the one which was validly published for the first time.<BR>
	 * This type is neither symmetric nor transitive.
	 *
	 * @see		NomenclaturalStatusType#isInvalid()
	 * @see		NomenclaturalStatusType#VALID()
	 */
	public static final NameRelationshipType LATER_VALIDATED_BY_NAME(){
	  return findTermByUuid(uuidLaterValidatedByName);
	}
	/**
	 * Returns the "blocking name" name relationship type. The first
	 * {@link TaxonName taxon name} involved in such a relationship is the
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
	  return  findTermByUuid(uuidBlockingNameFor);
	}

	/**
     * Returns the unspecific 'non' name relationship type. Name A in this
     * relationship is unspecificly marked as not being name B.
     * This relationship should only be used if in the given
     * context no further information exists to more specifically
     * define what kind of non-relationship is meant.
     * <BR>
     * When cleaning data this relationship type should be replaced
     * by one of the below mentioned more specific relationship types.
     *
     * This type is neither symmetric nor transitive.
     *
     * @see     #LATER_HOMONYM()
     * @see     #TREATED_AS_LATER_HOMONYM()
     * @see     #BLOCKING_NAME_FOR()
     */
	//#5655, #5640
    public static final NameRelationshipType UNSPECIFIC_NON(){
      return findTermByUuid(uuidNonUnspecific);
    }

	@Override
	protected void setDefaultTerms(TermVocabulary<NameRelationshipType> termVocabulary) {
		termMap = new HashMap<UUID, NameRelationshipType>();
		for (NameRelationshipType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

	@Override
	public NameRelationshipType readCsvLine(Class<NameRelationshipType> termClass, List<String> csvLine, TermType termType,
	        Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
		NameRelationshipType result = super.readCsvLine(termClass, csvLine, termType, terms, abbrevAsId);
		String nomenclaturalStanding = csvLine.get(10).trim();  //not in use yet?
        result.setNomenclaturalStanding(NomenclaturalStanding.getByKey(nomenclaturalStanding));
        String nomenclaturalStandingInverse = csvLine.get(11).trim();  //not in use yet?
        result.setNomenclaturalStandingInverse(NomenclaturalStanding.getByKey(nomenclaturalStandingInverse));
        String kindOfString = csvLine.get(12).trim();  //not in use yet?
		if (isNotBlank(kindOfString)){
			UUID uuidKindOf = UUID.fromString(kindOfString);
			DefinedTermBase<?> kindOf = terms.get(uuidKindOf);
			result.setKindOf((NameRelationshipType)kindOf);
		}
		return result;
	}

}