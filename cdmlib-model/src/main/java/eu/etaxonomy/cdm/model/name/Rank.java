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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * The class representing the taxonomical ranks (like "Family", "Genus" or
 * "Species") used for {@link TaxonNameBase taxon names} across all {@link NomenclaturalCode nomenclatural codes}
 * for bacteria (ICNB), viruses (ICVCN), plants and fungi (ICBN),
 * cultivars (ICNCP) and animals (ICZN).
 * <P>
 * A standard (ordered) list of taxonomical rank instances will be automatically
 * created as the project starts. But this class allows to extend this standard
 * list by creating new instances of additional taxonomical ranks if needed.
 * <P>
 * This class corresponds to: <ul>
 * <li> TaxonRankTerm according to the TDWG ontology
 * <li> TaxonomicRankEnum according to the TCS
 * <li> Rank according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rank")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class Rank extends OrderedTermBase<Rank> {
    private static final long serialVersionUID = -8648081681348758485L;
    private static final Logger logger = Logger.getLogger(Rank.class);

    private static final UUID uuidEmpire = UUID.fromString("ac470211-1586-4b24-95ca-1038050b618d");
    private static final UUID uuidDomain = UUID.fromString("ffca6ec8-8b88-417b-a6a0-f7c992aac19b");
    private static final UUID uuidSuperkingdom = UUID.fromString("64223610-7625-4cfd-83ad-b797bf7f0edd");
    private static final UUID uuidKingdom = UUID.fromString("fbe7109d-66b3-498c-a697-c6c49c686162");
    private static final UUID uuidSubkingdom = UUID.fromString("a71bd9d8-f3ab-4083-afb5-d89315d71655");
    private static final UUID uuidInfrakingdom = UUID.fromString("1e37930c-86cf-44f6-90fd-7822928df260");
    private static final UUID uuidSuperphylum = UUID.fromString("0d0cecb1-e254-4607-b210-6801e7ecbb04");
    private static final UUID uuidPhylum = UUID.fromString("773430d2-76b4-438c-b817-97a543a33287");
    private static final UUID uuidSubphylum = UUID.fromString("23a9b6ff-9408-49c9-bd9e-7a2ca5ab4725");
    private static final UUID uuidInfraphylum = UUID.fromString("1701de3a-7693-42a5-a2d3-42697f944190");
    private static final UUID uuidSuperdivision = UUID.fromString("a735a48f-4fc8-49a7-ae0c-6a984f658131");
    private static final UUID uuidDivision = UUID.fromString("7e56f5cc-123a-4fd1-8cbb-6fd80358b581");
    private static final UUID uuidSubdivision = UUID.fromString("931c840f-7a6b-4d76-ad38-bfdd77d7b2e8");
    private static final UUID uuidInfradivision = UUID.fromString("c0ede273-be52-4dee-b411-66ee08d30c94");
    private static final UUID uuidSuperclass = UUID.fromString("e65b4e1a-21ec-428d-9b9f-e87721ab967c");
    private static final UUID uuidClass = UUID.fromString("f23d14c4-1d34-4ee6-8b4e-eee2eb9a3daf");
    private static final UUID uuidSubclass = UUID.fromString("8cb26733-e2f5-46cb-ab5c-f99254f877aa");
    private static final UUID uuidInfraclass = UUID.fromString("ad23cfda-879a-4021-8629-c54d27caf717");
    private static final UUID uuidSuperorder = UUID.fromString("c8c67a22-301a-4219-b882-4a49121232ff");
    private static final UUID uuidOrder = UUID.fromString("b0785a65-c1c1-4eb4-88c7-dbd3df5aaad1");
    private static final UUID uuidSuborder = UUID.fromString("768ad378-fa85-42ab-b668-763225832f57");
    private static final UUID uuidInfraorder = UUID.fromString("84099182-a6f5-47d7-8586-33c9e9955a10");
    private static final UUID uuidSectionZoology = UUID.fromString("691d371e-10d7-43f0-93db-3d7fa1a62c54");
    private static final UUID uuidSubsectionZoology = UUID.fromString("0ed32d28-adc4-4303-a9ca-68e2acd67e33");
    private static final UUID uuidSuperfamily = UUID.fromString("2cfa510a-dcea-4a03-b66a-b1528f9b0796");
    private static final UUID uuidFamily = UUID.fromString("af5f2481-3192-403f-ae65-7c957a0f02b6");
    private static final UUID uuidSubfamily = UUID.fromString("862526ee-7592-4760-a23a-4ff3641541c5");
    private static final UUID uuidInfrafamily = UUID.fromString("c3f2e3bb-6eef-4a26-9fb7-b14f4c8c5e4f");
    private static final UUID uuidSupertribe = UUID.fromString("11e94828-8c61-499b-87d6-1de35ce2c51c");
    private static final UUID uuidTribe = UUID.fromString("4aa6890b-0363-4899-8d7c-ee0cb78e6166");
    private static final UUID uuidSubtribe = UUID.fromString("ae41ecc5-5165-4126-9d24-79939ae5d822");
    private static final UUID uuidInfratribe = UUID.fromString("1ec02e8f-f2b7-4c65-af9f-b436b34c79a3");
    private static final UUID uuidSupragenericTaxon = UUID.fromString("1fdc0b93-c354-441a-8406-091e0303ff5c");
    public static final UUID uuidGenus = UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a");
    private static final UUID uuidSubgenus = UUID.fromString("78786e16-2a70-48af-a608-494023b91904");
    private static final UUID uuidInfragenus = UUID.fromString("a9972969-82cd-4d54-b693-a096422f13fa");
    private static final UUID uuidSectionBotany = UUID.fromString("3edff68f-8527-49b5-bf91-7e4398bb975c");
    private static final UUID uuidSubsectionBotany = UUID.fromString("d20f5b61-d463-4448-8f8a-c1ff1f262f59");
    private static final UUID uuidSeries = UUID.fromString("d7381ecf-48f8-429b-9c54-f461656978cd");
    private static final UUID uuidSubseries = UUID.fromString("80c9a263-f4db-4a13-b6c2-b7fec1aa1200");
    private static final UUID uuidSpeciesAggregate = UUID.fromString("1ecae058-4217-4f75-9c27-6d8ba099ac7a");
    private static final UUID uuidSpeciesGroup = UUID.fromString("d1988a11-292b-46fa-8fb7-bc64ea6d8fc6");
    public static final UUID uuidInfragenericTaxon = UUID.fromString("41bcc6ac-37d3-4fd4-bb80-3cc5b04298b9");
    public static final UUID uuidSpecies = UUID.fromString("b301f787-f319-4ccc-a10f-b4ed3b99a86d");
    private static final UUID uuidSubspecificAggregate = UUID.fromString("72c248b9-027d-4402-b375-dd4f0850c9ad");
    private static final UUID uuidSubspecies = UUID.fromString("462a7819-8b00-4190-8313-88b5be81fad5");
    private static final UUID uuidInfraspecies = UUID.fromString("f28ebc9e-bd50-4194-9af1-42f5cb971a2c");
    private static final UUID uuidNatio = UUID.fromString("965f2f38-7f97-4270-ab5a-1999bf050a22");
    private static final UUID uuidVariety = UUID.fromString("d5feb6a5-af5c-45ef-9878-bb4f36aaf490");
    private static final UUID uuidBioVariety = UUID.fromString("a3a364cb-1a92-43fc-a717-3c44980a0991");
    private static final UUID uuidPathoVariety = UUID.fromString("2f4f4303-a099-47e3-9048-d749d735423b");
    private static final UUID uuidSubvariety = UUID.fromString("9a83862a-7aee-480c-a98d-4bceaf8712ca");
    private static final UUID uuidSubsubvariety = UUID.fromString("bff22f84-553a-4429-a4e7-c4b3796c3a18");

    private static final UUID uuidProles = UUID.fromString("8810d1ba-6a34-4ae3-a355-919ccd1cd1a5");
    private static final UUID uuidRace = UUID.fromString("196dee39-cfd8-4460-8bf0-88b83da27f62");
    private static final UUID uuidSublusus = UUID.fromString("1fafa596-a8e7-4e62-a378-3cc8cb3627ca");

    private static final UUID uuidConvar = UUID.fromString("2cc740c9-cebb-43c8-9b06-1bef79e6a56a");
    private static final UUID uuidForm = UUID.fromString("0461281e-458a-47b9-8d41-19a3d39356d5");
    private static final UUID uuidSpecialForm = UUID.fromString("bed20aee-2f5a-4635-9c02-eff06246d067");
    private static final UUID uuidSubform = UUID.fromString("47cfc5b0-0fb7-4ceb-b61d-e1dd8de8b569");
    private static final UUID uuidSubsubform = UUID.fromString("1c8ac389-4349-4ae0-87be-7239f6635068");
    public static final UUID uuidInfraspecificTaxon = UUID.fromString("eb75c27d-e154-4570-9d96-227b2df60474");
    private static final UUID uuidCandidate = UUID.fromString("ead9a1f5-dfd4-4de2-9121-70a47accb10b");
    private static final UUID uuidDenominationClass = UUID.fromString("49bdf74a-2170-40ed-8be2-887a0db517bf");
    private static final UUID uuidGrex = UUID.fromString("08dcb4ff-ac58-48a3-93af-efb3d836ac84");
    private static final UUID uuidGraftChimaera = UUID.fromString("6b4063bc-f934-4796-9bf3-0ef3aea5c1cb");
    private static final UUID uuidCultivarGroup = UUID.fromString("d763e7d3-e7de-4bb1-9d75-225ca6948659");
    private static final UUID uuidCultivar = UUID.fromString("5e98415b-dc6e-440b-95d6-ea33dbb39ad0");
    private static final UUID uuidUnknownRank = UUID.fromString("5c4d6755-2cf6-44ca-9220-cccf8881700b");

    private static Map<String, UUID> idInVocMap = null;
    private static Map<String, UUID> labelMap = null;

    protected static Map<UUID, Rank> termMap = null;

//*********************** Factory methods ********************************************/

//    /**
//     * Creates a new empty rank.
//     *
//     * @see #NewInstance(String, String, String)
//     */
//    private static Rank NewInstance(){
//        return new Rank();
//    }

    /**
     * Creates an additional rank with a description (in the {@link Language#DEFAULT() default language}),
     * a label and a label abbreviation.
     *
     * @param	term  		 the string (in the default language) describing the
     * 						 new rank to be created
     * @param	label  		 the string identifying the new rank to be created
     * @param	labelAbbrev  the string identifying (in abbreviated form) the
     * 						 new rank to be created
     * @see 				 #NewInstance()
     */
    public static Rank NewInstance(RankClass rankClass, String term, String label, String labelAbbrev){
        return new Rank(rankClass, term, label, labelAbbrev);
    }

    /**
     * The {@link RankClass rank class} of a rank. It is usually needed for correct formatting of a
     * rank by using e.g. isSupraGeneric(). Prior to v3.3 this was computed by comparison of ranks.
     */
    @XmlAttribute(name ="RankClass")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.name.RankClass")}
    )
    @Audited
    private RankClass rankClass;


//********************************** Constructor *********************************/

      //for hibernate use only
      @Deprecated
      protected Rank() {
        super(TermType.Rank);
    }

    /**
     * Class constructor: creates an additional rank instance with a description
     * (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label and a label abbreviation.
     *
     * @param	term  		 the string (in the default language) describing the
     * 						 new rank to be created
     * @param	label  		 the string identifying the new rank to be created
     * @param	labelAbbrev  the string identifying (in abbreviated form) the
     * 						 new rank to be created
     * @see 	#Rank()
     */
    protected Rank(RankClass rankClass, String term, String label, String labelAbbrev) {
        super(TermType.Rank, term, label, labelAbbrev);
        this.rankClass = rankClass;
    }


//********* METHODS **************************************/

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
     */
    @Override
    public void resetTerms(){
        termMap = null;
    }



    protected static Rank getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
           return getTermByClassAndUUID(Rank.class, uuid);
        } else {
            return termMap.get(uuid);
        }
    }

    public static final Rank EMPIRE(){
      return getTermByUuid(uuidEmpire);
    }
    public static final Rank DOMAIN(){
          return getTermByUuid(uuidDomain);
    }
    public static final Rank SUPERKINGDOM(){
        return getTermByUuid(uuidSuperkingdom);
    }
    public static final Rank KINGDOM(){
        return getTermByUuid(uuidKingdom);
    }
    public static final Rank SUBKINGDOM(){
        return getTermByUuid(uuidSubkingdom);
    }
    public static final Rank INFRAKINGDOM(){
        return getTermByUuid(uuidInfrakingdom);
    }
    public static final Rank SUPERPHYLUM(){
        return getTermByUuid(uuidSuperphylum);
    }
    public static final Rank PHYLUM(){
        return getTermByUuid(uuidPhylum);
    }
    public static final Rank SUBPHYLUM(){
        return getTermByUuid(uuidSubphylum);
    }
    public static final Rank INFRAPHYLUM(){
        return getTermByUuid(uuidInfraphylum);
    }
    public static final Rank SUPERDIVISION(){
        return getTermByUuid(uuidSuperdivision);
    }
    public static final Rank DIVISION(){
        return getTermByUuid(uuidDivision);
    }
    public static final Rank SUBDIVISION(){
        return getTermByUuid(uuidSubdivision);
    }
    public static final Rank INFRADIVISION(){
        return getTermByUuid(uuidInfradivision);
    }
    public static final Rank SUPERCLASS(){
        return getTermByUuid(uuidSuperclass);
    }
    public static final Rank CLASS(){
        return getTermByUuid(uuidClass);
    }
    public static final Rank SUBCLASS(){
        return getTermByUuid(uuidSubclass);
    }
    public static final Rank INFRACLASS(){
        return getTermByUuid(uuidInfraclass);
    }
    public static final Rank SUPERORDER(){
        return getTermByUuid(uuidSuperorder);
    }
    public static final Rank ORDER(){
        return getTermByUuid(uuidOrder);
    }
    public static final Rank SUBORDER(){
        return getTermByUuid(uuidSuborder);
    }
    public static final Rank INFRAORDER(){
        return getTermByUuid(uuidInfraorder);
    }
    public static final Rank SUPERFAMILY(){
        return getTermByUuid(uuidSuperfamily);
    }
    public static final Rank FAMILY(){
        return getTermByUuid(uuidFamily);
    }
    public static final Rank SUBFAMILY(){
        return getTermByUuid(uuidSubfamily);
    }
    public static final Rank INFRAFAMILY(){
        return getTermByUuid(uuidInfrafamily);
    }
    public static final Rank SUPERTRIBE(){
        return getTermByUuid(uuidSupertribe);
    }
    public static final Rank TRIBE(){
        return getTermByUuid(uuidTribe);
    }
    public static final Rank SUBTRIBE(){
        return getTermByUuid(uuidSubtribe);
    }
    public static final Rank INFRATRIBE(){
        return getTermByUuid(uuidInfratribe);
    }
    public static final Rank SUPRAGENERICTAXON(){
        return getTermByUuid(uuidSupragenericTaxon);
    }
    public static final Rank GENUS(){
        return getTermByUuid(uuidGenus);
    }
    public static final Rank SUBGENUS(){
        return getTermByUuid(uuidSubgenus);
    }
    public static final Rank INFRAGENUS(){
        return getTermByUuid(uuidInfragenus);
    }
    public static final Rank SECTION_BOTANY(){
        return getTermByUuid(uuidSectionBotany);
    }
    public static final Rank SUBSECTION_BOTANY(){
        return getTermByUuid(uuidSubsectionBotany);
    }
    public static final Rank SECTION_ZOOLOGY(){
        return getTermByUuid(uuidSectionZoology);
    }
    public static final Rank SUBSECTION_ZOOLOGY(){
        return getTermByUuid(uuidSubsectionZoology);
    }
    public static final Rank SERIES(){
        return getTermByUuid(uuidSeries);
    }
    public static final Rank SUBSERIES(){
        return getTermByUuid(uuidSubseries);
    }
    public static final Rank SPECIESAGGREGATE(){
        return getTermByUuid(uuidSpeciesAggregate);
    }
    public static final Rank SPECIESGROUP(){
        return getTermByUuid(uuidSpeciesGroup);
    }
    /**
     * 'Unranked infrageneric'. An infrageneric rank which is on purpose not further defined.
     * This sometimes holds for names from the 19th century.
     */
    public static final Rank INFRAGENERICTAXON(){
        return getTermByUuid(uuidInfragenericTaxon);
    }
    public static final Rank SPECIES(){
        return getTermByUuid(uuidSpecies);
    }
    public static final Rank SUBSPECIFICAGGREGATE(){
        return getTermByUuid(uuidSubspecificAggregate);
    }
    public static final Rank SUBSPECIES(){
        return getTermByUuid(uuidSubspecies);
    }
    public static final Rank INFRASPECIES(){
        return getTermByUuid(uuidInfraspecies);
    }
    public static final Rank VARIETY(){
        return getTermByUuid(uuidVariety);
    }
    public static final Rank BIOVARIETY(){
        return getTermByUuid(uuidBioVariety);
    }
    public static final Rank PATHOVARIETY(){
        return getTermByUuid(uuidPathoVariety);
    }
    public static final Rank SUBVARIETY(){
        return getTermByUuid(uuidSubvariety);
    }
    public static final Rank SUBSUBVARIETY(){
        return getTermByUuid(uuidSubsubvariety );
    }
    public static final Rank PROLES(){
        return getTermByUuid(uuidProles);
    }
    public static final Rank RACE(){
        return getTermByUuid(uuidRace);
    }
    public static final Rank SUBLUSUS(){
        return getTermByUuid(uuidSublusus);
    }

    public static final Rank CONVAR(){
        return getTermByUuid(uuidConvar);
    }
    public static final Rank FORM(){
        return getTermByUuid(uuidForm);
    }
    public static final Rank SPECIALFORM(){
        return getTermByUuid(uuidSpecialForm);
    }
    public static final Rank SUBFORM(){
        return getTermByUuid(uuidSubform);
    }
    public static final Rank SUBSUBFORM(){
        return getTermByUuid(uuidSubsubform);
    }
    /**
     * 'Unranked infraspecific'. An infraspecific rank which is on purpose not further defined.
     * This sometimes holds for names from the 19th century.
     */
    public static final Rank INFRASPECIFICTAXON(){
        return getTermByUuid(uuidInfraspecificTaxon);
    }
    public static final Rank CANDIDATE(){
        return getTermByUuid(uuidCandidate);
    }
    public static final Rank DENOMINATIONCLASS(){
        return getTermByUuid(uuidDenominationClass);
    }
    public static final Rank GREX(){
        return getTermByUuid(uuidGrex);
    }
    public static final Rank GRAFTCHIMAERA(){
        return getTermByUuid(uuidGraftChimaera);
    }
    public static final Rank CULTIVARGROUP(){
        return getTermByUuid(uuidCultivarGroup);
    }
    public static final Rank CULTIVAR(){
        return getTermByUuid(uuidCultivar);
    }
    public static final Rank UNKNOWN_RANK(){
        return getTermByUuid(uuidUnknownRank);
    }
    public static final Rank NATIO(){
        return getTermByUuid(uuidNatio);
    }
    /**
     * @see #INFRASPECIFICTAXON()
     */
    public static final Rank UNRANKED_INFRASPECIFIC(){
        return getTermByUuid(uuidInfraspecificTaxon);
    }
    /**
     * @see #INFRAGENERICTAXON()
     */
    public static final Rank UNRANKED_INFRAGENERIC(){
        return getTermByUuid(uuidInfragenericTaxon);
    }

// ************************ GETTER / SETTER **********************************/

    public RankClass getRankClass() {
        return rankClass;
    }

    public void setRankClass(RankClass rankClass) {
        this.rankClass = rankClass;
    }

// ******************************** METHODS ***************************************/

    /**
     * Returns the boolean value indicating whether <i>this</i> rank is higher than
     * the genus rank (true) or not (false). Returns false if <i>this</i> rank is null.
     *
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isSupraGeneric(){
        return this.rankClass.equals(RankClass.Suprageneric); // (this.isHigher(Rank.GENUS()));
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> rank is the genus rank
     * (true) or not (false). Returns false if <i>this</i> rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isGenus(){
        return this.rankClass.equals(RankClass.Genus); // (this.equals(Rank.GENUS()));
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> rank is higher than the
     * species rank and lower than the genus rank (true) or not (false). Species groups or
     * aggregates are also handled as infrageneric ranks.
     * Returns false if <i>this</i> rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isSpeciesAggregate()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isInfraGeneric(){
        return this.rankClass.equals(RankClass.Infrageneric) || this.rankClass.equals(RankClass.SpeciesGroup) ; //(this.isLower(Rank.GENUS()) && this.isHigher(Rank.SPECIES()));
    }

    /**
     * Returns true if this rank indicates a rank that aggregates species
     * like species aggregates or species groups, false otherwise.
     * @return
     */
    @Transient
    public boolean isSpeciesAggregate(){
        return this.rankClass.equals(RankClass.SpeciesGroup); //(this.equals(Rank.SPECIESAGGREGATE()) || (this.isLower(Rank.SPECIESAGGREGATE()) && this.isHigher(Rank.SPECIES())));
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> rank is the species
     * rank (true) or not (false). Returns false if <i>this</i> rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isSpecies(){
        return this.rankClass.equals(RankClass.Species); //(this.equals(Rank.SPECIES()));
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> rank is lower than the
     * species rank (true) or not (false). Returns false if <i>this</i> rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     */
    @Transient
    public boolean isInfraSpecific(){
        return this.rankClass.equals(RankClass.Infraspecific); // (this.isLower(Rank.SPECIES()));
    }


    /**
     * Returns the rank identified through a label or the identifier within the vocabulary
     * Preliminary implementation for BotanicalNameParser.
     *
     * @param	strRank	the string identifying the rank
     * @return  		the rank
     */
    public static Rank getRankByNameOrIdInVoc(String strRank) throws UnknownCdmTypeException{
        return getRankByNameOrIdInVoc(strRank, false);
    }

    /**
     * Returns the rank identified through a label or the identifier within the vocabulary
     * for a given nomenclatural code.
     * Preliminary implementation for BotanicalNameParser.
     *
     * @param	strRank	the string identifying the rank
     * @param   nc      the nomenclatural code
     * @return  		the rank
     */
    public static Rank getRankByNameOrIdInVoc(String strRank, NomenclaturalCode nc) throws UnknownCdmTypeException{
        return getRankByNameOrIdInVoc(strRank, nc, false);
    }

    // TODO
    // Preliminary implementation for BotanicalNameParser.
    // not yet complete
    /**
     * Returns the rank identified through a label or the identifier within the vocabulary.
     * Preliminary implementation for BotanicalNameParser.
     *
     * @param	strRank	the string identifying the rank
     * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the abbrev is
     * 			unknown or not yet implemented
     * @return  		the rank
     */
    public static Rank getRankByNameOrIdInVoc(String strRank, boolean useUnknown) throws UnknownCdmTypeException{
        try {
            return getRankByIdInVoc(strRank);
        } catch (UnknownCdmTypeException e) {
            return getRankByName(strRank, useUnknown);
        }
    }

    // TODO
    // Preliminary implementation for BotanicalNameParser.
    // not yet complete
    /**
     * Returns the rank identified through a label or the identifier within the vocabulary.
     * Preliminary implementation for BotanicalNameParser.
     *
     * @param	strRank	the string identifying the rank
     * @param   nc      the nomenclatural code
     * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the abbrev is
     * 			unknown or not yet implemented
     * @return  		the rank
     */
    public static Rank getRankByNameOrIdInVoc(String strRank, NomenclaturalCode nc, boolean useUnknown)
            throws UnknownCdmTypeException{
        try {
            return getRankByIdInVoc(strRank, nc);
        } catch (UnknownCdmTypeException e) {
            return getRankByName(strRank, nc, useUnknown);
        }
    }

    /**
     * Returns the rank identified through the vocabulary identifier.
     * Preliminary implementation for BotanicalNameParser.<BR>
     * Note: For abbrev = "[unranked]" the result is undefined.
     * It maybe the infrageneric unranked or the infraspecific unranked.
     * You need to define by context which one is correct.
     *
     * @param	abbrev	the string for the name abbreviation
     * @return  		the rank
     */
    public static Rank getRankByIdInVoc(String abbrev) throws UnknownCdmTypeException{
        return getRankByIdInVoc(abbrev, false);
    }

    /**
     * Returns the rank identified through an abbreviated name for a given nomenclatural code.
     * See also {@link #getRankByIdInVoc(String, boolean)}
     *
     * @param	abbrev	the string for the name abbreviation
     * @param	nc	    the nomenclatural code
     * @return  		the rank
     */
    public static Rank getRankByIdInVoc(String abbrev, NomenclaturalCode nc) throws UnknownCdmTypeException{
        return getRankByIdInVoc(abbrev, nc, false);
    }

    // TODO
    // Preliminary implementation for BotanicalNameParser.
    // not yet complete
    /**
     * Returns the rank identified through an abbreviated representation.
     * At the moment it uses the English abbreviations (being Latin because
     * we do not have Latin representations yet.
     * TODO
     * If no according abbreviation is available it throws either an UnknownCdmTypeException
     * or an #Rank.UNKNOWN() object depending on the useUnknown flag.
     *
     * @param	idInVoc		the string for the name abbreviation
     * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the abbrev is
     * 			unknown or not yet existent
     * @return  the rank
     */
    public static Rank getRankByIdInVoc(String idInVoc, boolean useUnknown) throws UnknownCdmTypeException{
        Rank result = null;
        if (idInVoc == null){
            throw new NullPointerException("idInVoc is NULL in getRankByIdInVoc");
        }
        if (StringUtils.isBlank(idInVoc)){
            //handle empty idInVoc as unknown
            idInVoc = "oijas34\u0155";
        }
        if (idInVocMap == null){
            return null;
        }
        idInVoc = normalizeSectionAndSubsection(idInVoc);
        idInVoc = normalizeSpecialForm(idInVoc);
        idInVoc = normalizeSpecialForm(idInVoc);
        UUID uuid = idInVocMap.get(idInVoc);
        if (uuid != null ){
            result = getTermByUuid(uuid);
        }
        if (result != null){
            return result;
        }else {
            if (idInVoc == null){
                idInVoc = "(null)";
            }
            if (useUnknown){
                logger.info("Unknown rank name: " + idInVoc + ". Rank 'UNKNOWN_RANK' created instead");
                return Rank.UNKNOWN_RANK();
            }else{
                throw new UnknownCdmTypeException("Unknown rank abbreviation: " + idInVoc);
            }
        }
    }

    private static String normalizeSectionAndSubsection(String idInVoc) {
        if (idInVoc.equals("sect.")){
            return "sect.(bot.)";
        }else if (idInVoc.equals("subsect.")){
            return "subsect.(bot.)";
        }
        return idInVoc;
    }

    private static String normalizeSpecialForm(String idInVoc) {
        if (idInVoc.equals("f.sp.") || idInVoc.equals("f. sp.")){
            return "f.spec.";
        }
        return idInVoc;
    }

    private static String normalizeSsp(String idInVoc) {
        if (idInVoc.equals("ssp.") && !idInVocMap.containsKey("ssp.") && idInVocMap.containsKey("subsp.")){
            return "subsp.";
        }
        return idInVoc;
    }

    // TODO
    // Preliminary implementation to cover Botany and Zoology.
    /**
     * Returns the rank identified through an abbreviated name for a given nomenclatural code.
     * Preliminary implementation for ICBN and ICZN.
     * See also {@link #getRankByIdInVoc(String, boolean)}

     *
     * @param	abbrev		the string for the name abbreviation
     * @param	nc	        the nomenclatural code
     * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the abbrev is
     * 			unknown or not yet implemented
     * @return  the rank
     */
    public static Rank getRankByIdInVoc(String abbrev, NomenclaturalCode nc, boolean useUnknown)
            throws UnknownCdmTypeException{

        if (nc != null && nc.equals(NomenclaturalCode.ICZN)) {
            if (abbrev != null){
                if (abbrev.equalsIgnoreCase("sect.")) {
                    return Rank.SECTION_ZOOLOGY();
                } else if (abbrev.equalsIgnoreCase("subsect.")) {
                    return Rank.SUBSECTION_ZOOLOGY();
                }
            }
        }else if (nc != null && nc.equals(NomenclaturalCode.ICNAFP)) {
            if (abbrev != null){
                if (abbrev.equalsIgnoreCase("sect.")) {
                    return Rank.SECTION_BOTANY();
                } else if (abbrev.equalsIgnoreCase("subsect.")) {
                    return Rank.SUBSECTION_BOTANY();
                }
            }
        }
        return getRankByIdInVoc(abbrev, useUnknown);
    }

    // TODO
    // Preliminary implementation for BotanicalNameParser.
    // not yet complete
    /**
     * Returns the rank identified through a name.
     * Preliminary implementation for BotanicalNameParser.
     *
     * @param	rankName	the string for the name of the rank
     * @return  			the rank
     */
    public static Rank getRankByName(String rankName) throws UnknownCdmTypeException{
        return getRankByName(rankName, false);
    }


    // TODO
    // Preliminary implementation for ICBN and ICZN.
    // not yet complete
    /**
     * Returns the rank identified through a name for a given nomenclatural code.
     * Preliminary implementation for ICBN and ICZN.
     *
     * @param	rankName	the string for the name of the rank
     * @param	nc	        the nomenclatural code
     * @return  			the rank
     */
    public static Rank getRankByName(String rankName, NomenclaturalCode nc) throws UnknownCdmTypeException{
        return getRankByName(rankName, nc, false);
    }

    /**
     * Returns the rank identified through a name.
     * Preliminary implementation for BotanicalNameParser.
     * TODO At the moment we do not have Latin representations yet.
     *
     * @param	rankName	the string for the name of the rank
     * @param 	useUnknown 	if true the rank UNKNOWN_RANK is returned if the rank name is
     * 			unknown or not yet implemented
     * @return  			the rank
     */
    public static Rank getRankByName(String rankName, boolean useUnknown)
            throws UnknownCdmTypeException{
        if (rankName.equalsIgnoreCase("Regnum")){ return Rank.KINGDOM();
        }else if (rankName.equalsIgnoreCase("Subregnum")){ return Rank.SUBKINGDOM();
        }else if (rankName.equalsIgnoreCase("Phylum")){ return Rank.PHYLUM();
        }else if (rankName.equalsIgnoreCase("Subphylum")){ return Rank.SUBPHYLUM();
        }else if (rankName.equalsIgnoreCase("Divisio")){ return Rank.DIVISION();
        }else if (rankName.equalsIgnoreCase("Subdivisio")){ return Rank.SUBDIVISION();
        }else if (rankName.equalsIgnoreCase("Classis")){ return Rank.CLASS();
        }else if (rankName.equalsIgnoreCase("Subclassis")){ return Rank.SUBCLASS();
        }else if (rankName.equalsIgnoreCase("Superordo")){ return Rank.SUPERORDER();
        }else if (rankName.equalsIgnoreCase("Ordo")){ return Rank.ORDER();
        }else if (rankName.equalsIgnoreCase("Subordo")){ return Rank.SUBORDER();
        }else if (rankName.equalsIgnoreCase("Familia")){ return Rank.FAMILY();
        }else if (rankName.equalsIgnoreCase("Subfamilia")){ return Rank.SUBFAMILY();
        }else if (rankName.equalsIgnoreCase("Tribus")){ return Rank.TRIBE();
        }else if (rankName.equalsIgnoreCase("Subtribus")){ return Rank.SUBTRIBE();
        }else if (rankName.equalsIgnoreCase("Genus")){ return Rank.GENUS();
        }else if (rankName.equalsIgnoreCase("Subgenus")){ return Rank.SUBGENUS();
        }else if (rankName.equalsIgnoreCase("Sectio")){ return Rank.SECTION_BOTANY();
        }else if (rankName.equalsIgnoreCase("Subsectio")){ return Rank.SUBSECTION_BOTANY();
        }else if (rankName.equalsIgnoreCase("Series")){ return Rank.SERIES();
        }else if (rankName.equalsIgnoreCase("Subseries")){ return Rank.SUBSERIES();
        }else if (rankName.equalsIgnoreCase("Aggregate")){ return Rank.SPECIESAGGREGATE();
        }else if (rankName.equalsIgnoreCase("Speciesgroup")){ return Rank.SPECIESGROUP();
        }else if (rankName.equalsIgnoreCase("Species")){ return Rank.SPECIES();
        }else if (rankName.equalsIgnoreCase("Subspecies")){ return Rank.SUBSPECIES();
        }else if (rankName.equalsIgnoreCase("Convarietas")){ return Rank.CONVAR();
        }else if (rankName.equalsIgnoreCase("Varietas")){ return Rank.VARIETY();
        }else if (rankName.equalsIgnoreCase("Subvarietas")){ return Rank.SUBVARIETY();
        }else if (rankName.equalsIgnoreCase("Forma")){ return Rank.FORM();
        }else if (rankName.equalsIgnoreCase("Subforma")){ return Rank.SUBFORM();
        }else if (rankName.equalsIgnoreCase("Forma spec.")){ return Rank.SPECIALFORM();
        }else if (rankName.equalsIgnoreCase("tax.infragen.")){ return Rank.INFRAGENERICTAXON();
        }else if (rankName.equalsIgnoreCase("tax.infrasp.")){ return Rank.INFRASPECIFICTAXON();
        // old ranks
        }else if (rankName.equalsIgnoreCase("proles")){ return Rank.PROLES();
        }else if (rankName.equalsIgnoreCase("race")){ return Rank.RACE();
        }else if (rankName.equalsIgnoreCase("sublusus")){ return Rank.SUBLUSUS();

        }else if (rankName.equalsIgnoreCase("taxon")){ return Rank.INFRASPECIFICTAXON(); //to create the name put 'taxon' and the infraspeciesepi to the field unnamed namephrase

        }else{
            if (rankName == null){
                rankName = "(null)";  //see NPE above
            }
            if (useUnknown){
                if (logger.isInfoEnabled()){logger.info("Unknown rank name: " + rankName+". Rank 'UNKNOWN_RANK' created instead");}
                return Rank.UNKNOWN_RANK();
            }else{
                throw new UnknownCdmTypeException("Unknown rank name: " + rankName);
            }
        }
    }

    /**
     * Defines the rank according to the English name.
     * @param rankName English rank name.
     * @param nc Defines the handling of the section and subsection ranks. These are in different orders depending on the
     * nomenclatural code.
     * @param useUnknown if true, the "Unknown" rank is returned as a placeholder.
     * @return
     * @throws UnknownCdmTypeException never thrown if useUnknown is true
     */
    public static Rank getRankByEnglishName(String rankName, NomenclaturalCode nc, boolean useUnknown) throws UnknownCdmTypeException{
        Rank result = null;
        if (rankName == null){
            throw new NullPointerException("Abbrev is NULL in getRankByAbbreviation");
        }
        if (labelMap == null){
            return null;
        }
        //handle section and subsection (not unique representations)
        if (rankName.equalsIgnoreCase("Section")){
            if (nc != null && nc.equals(NomenclaturalCode.ICZN)){	return Rank.SECTION_ZOOLOGY();
            }else if (nc != null && nc.equals(NomenclaturalCode.ICNAFP)){return Rank.SECTION_BOTANY();
            }else{
                String errorWarning = "Section is only defined for ICZN and ICNAFP at the moment but here needed for " + ((nc == null)? "(null)": nc.toString());
                logger.warn(errorWarning);
                throw new UnknownCdmTypeException (errorWarning);
            }
        }else if (rankName.equalsIgnoreCase("Subsection")){
            if (nc != null && nc.equals(NomenclaturalCode.ICZN)){ return Rank.SUBSECTION_ZOOLOGY();
            }else if (nc != null && nc.equals(NomenclaturalCode.ICNAFP)){ return Rank.SUBSECTION_BOTANY();
            }else{
                String errorWarning = "Subsection is only defined for ICZN and ICBN at the moment but here needed for " + ((nc == null)? "(null)": nc.toString());
                logger.warn(errorWarning);
                throw new UnknownCdmTypeException (errorWarning);
            }
        }

        rankName = rankName.toLowerCase();

        UUID uuid = labelMap.get(rankName);
        if (uuid != null ){
            result = getTermByUuid(uuid);
        }
        if (result != null){
            return result;
        }else {
            if (rankName == null){
                rankName = "(null)";
            }
            if (useUnknown){
                if (logger.isInfoEnabled()){logger.info("Unknown rank name: " + rankName + ". Rank 'UNKNOWN_RANK' created instead");}
                return Rank.UNKNOWN_RANK();
            }else{
                throw new UnknownCdmTypeException("Unknown rank: " + rankName);
            }
        }
    }


    public static Rank getRankByName(String rankName, NomenclaturalCode nc, boolean useUnknown)
        throws UnknownCdmTypeException {

        if (nc.equals(NomenclaturalCode.ICZN)) {
            if (rankName.equalsIgnoreCase("Sectio")) { return Rank.SECTION_ZOOLOGY();
            }else if (rankName.equalsIgnoreCase("Subsectio")) { return Rank.SUBSECTION_ZOOLOGY();
            }
        }
        return getRankByName(rankName, useUnknown);
    }

    /**
     * Returns the abbreviated rank name for <i>this</i> rank according to the English representation
     * abbreviated label.
     * TODO Needs to be changed to Latin as soon as Latin representations are available.
     *
     * @return	the abbreviation string for <i>this</i> rank
     */
    public String getAbbreviation(){
        Language language = Language.getLanguageFromUuid(Language.uuidEnglish);
        String result = this.getRepresentation(language).getAbbreviatedLabel();
        if (result== null) {
             logger.warn("Abbreviation for this Rank " + this.toString() +  " not yet implemented");
            return "no abbreviation available.";
        }else{
            return result;
        }
    }
    @Transient
    public String getInfraGenericMarker() throws UnknownCdmTypeException{
        String result = null;
        if (! this.isInfraGeneric()){
            throw new IllegalStateException("An infrageneric marker is only available for a infrageneric rank but was asked for rank: " + this.toString());
        }else{
            result = this.getAbbreviation();
        }
        if (result == null){
            throw new UnknownCdmTypeException("Abbreviation for rank unknown: " + this.toString());
        }
        return result;
    }



    @Override
    public Rank readCsvLine(Class<Rank> termClass, List<String> csvLine, Map<UUID, DefinedTermBase> terms, boolean abbrevAsId) {
        Rank rank = super.readCsvLine(termClass, csvLine, terms, abbrevAsId);
        RankClass rankClass = RankClass.getByKey(csvLine.get(5));
        assert rankClass != null: "XXXXXXXXXXXXXXXXXXXXX  Rank class must not be null: " + csvLine ;
        rank.setRankClass(rankClass);
        return rank;
    }

    @Override
    protected void setDefaultTerms(TermVocabulary<Rank> termVocabulary) {
        termMap = new HashMap<UUID, Rank>();
        for (Rank term : termVocabulary.getTerms()){
            termMap.put(term.getUuid(), term);
            addRank(term);
        }
    }

    // FIXME:Remoting This is a ugly hack and need to be removed
    // once the static initialisation is refactored
    public static void initDefaultTerms() {
        TermVocabulary<Rank> vocabulary = getTermByUuid(uuidKingdom).getVocabulary();
        if(idInVocMap != null) {
            idInVocMap.clear();
        }
        (new Rank()).setDefaultTerms(vocabulary);
    }

    /**
     * @param term
     */
    private void addRank(Rank rank) {
        if (rank == null){
            logger.warn("rank is NULL");
            return;
        }
        if (rank.getUuid().equals(uuidSectionZoology) || rank.getUuid().equals(uuidSubsectionZoology )){
            //sect./subsect. is used for botanical sections, see also #getRankByAbbreviation(String, NomenclaturalCode, boolean)
            return;
        }
        Language lang = Language.DEFAULT();  //TODO should be Latin but at the moment we have only English representations
        Representation representation = rank.getRepresentation(lang);
        String abbrevLabel = representation.getAbbreviatedLabel();
        String label = representation.getLabel();

        //initialize maps
        if (idInVocMap == null){
            idInVocMap = new HashMap<String, UUID>();
        }
        if (labelMap == null){
            labelMap = new HashMap<String, UUID>();
        }
        labelMap.put(label.toLowerCase(), rank.getUuid());
        //add to map
        if (StringUtils.isBlank(abbrevLabel)){
            if (logger.isDebugEnabled()){logger.info("Abbreviated label for rank is NULL or empty.Can't add rank to abbrevLabel map: " + CdmUtils.Nz(rank.getLabel()));}
        }else{
            idInVocMap.put(abbrevLabel, rank.getUuid());
        }
    }


    /**
     * It is necessary to skip the vocabulary check, otherwise we would have
     * problems in some CacheStrategies, due to uninitialized Vocabularies.
     *
     * @see eu.etaxonomy.cdm.model.common.OrderedTermBase#compareTo(eu.etaxonomy.cdm.model.common.OrderedTermBase)
     */
    @Override
    public int compareTo(Rank orderedTerm) {
        return performCompareTo(orderedTerm, true);
    }

}