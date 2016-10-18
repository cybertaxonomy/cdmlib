// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.lang.model.element.Modifier;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;



/**
 * The term type is used to define the type of a {@link TermBase term}, may it be a vocabulary or a defined term.<BR>
 * It is used to define in which context a term may be used. From v3.3 on this replaces the semantic part of the subclasses
 * of the class {@link DefinedTermBase}. E.g. before v3.3 a term defining a sex and a term defining a stage had to different
 * classes Sex and Stage. With v3.3 they both became {@link DefinedTerm}s but with different types.<BR>
 * The type of a term and the type of its vocabulary should be the same. Before v3.3 it was not always
 * possible to define what the context of an (empty) vocabulary is.
 * In future versions this enum may also be expressed as a term, not an enum. For now the general policy is to
 * transform former classes to general classes with enums as their types.
 *
 * @author a.mueller
 * @created 11.06.2013
 */
@XmlEnum
public enum TermType implements IEnumTerm<TermType>, Serializable{

    //0
    /**
     * Unknown term type is the type to be used if no information is available about the type.
     * In the current model this type should never be used. However, it is a placeholder in case
     * we find an appropriate usage in future.
     */
    @XmlEnumValue("Unknown")
    Unknown(UUID.fromString("b2836c89-3b1d-4758-ba6d-568ef8d6fbc4"), "Unknown Term Type","UNK", null),

    //1
    /**
     * Term type to represent languages.
     */
    @XmlEnumValue("Language")
    Language(UUID.fromString("5591dc6c-ad1f-4abd-b6c2-4852ea8e46df"), "Language", "LA", null),

    //2
    /**
     * Term type for areas.
     */
    @XmlEnumValue("NamedArea")
    NamedArea(UUID.fromString("8c9a0bc9-da91-478d-bc8b-44b11565e160"), "Named Area", "NA", null),

    //3
    /**
     * Term type for taxonomic ranks.
     */
    @XmlEnumValue("Rank")
    Rank(UUID.fromString("8d26b6a9-8a89-45d5-8358-49c3e4f30ade"), "Rank", "RK", null),

    //4
    /**
     * Term type for descriptive features.
     * @see DescriptionElementBase
     */
    @XmlEnumValue("Feature")
    Feature(UUID.fromString("b866a1d6-f962-4c23-bb8e-a3b66d33aedc"), "Feature", "FE", null),

    //5
    /**
     * Term type for annotation types.
     * @see Annotation
     */
    @XmlEnumValue("AnnotationType")
    AnnotationType(UUID.fromString("c3aabb64-6174-4152-95b1-7cec57e485cf"), "Annotation Type", "ANT", null),

    //6
    /**
     * Term type for marker types.
     * @see Marker
     */
    @XmlEnumValue("MarkerType")
    MarkerType(UUID.fromString("d28a1bf8-95ed-483a-8f02-3515b14998e0"), "Marker Type", "MKT", null),

    //7
    /**
     * Term type for extension types.
     * @see Extension
     */
    @XmlEnumValue("ExtensionType")
    ExtensionType(UUID.fromString("12f5c03b-528a-4909-b81b-e525feabc97c"), "Extension Type", "EXT", null),

    //8
    /**
     * Term type for derivation event types.
     * @see DerivationEvent
     */
    @XmlEnumValue("DerivationEventType")
    DerivationEventType(UUID.fromString("ba8e4b10-c792-42e7-a3f5-874708f10094"), "Derivation Event Type", "DET", null),

    //9
    /**
     * Term type for presence or absence status
     * @see Distribution
     */
    @XmlEnumValue("PresenceAbsenceTerm")
    PresenceAbsenceTerm(UUID.fromString("f6b80f88-c8c5-456b-bbd6-d63ecf35606e"), "Presence or Absence Term", "PAT", null),

    //10
    /**
     * Term type for the taxonomic nomenclatural status of a {@link TaxonNameBase taxon name}.
     * @see NomenclaturalStatus
     */
    @XmlEnumValue("NomenclaturalStatusType")
    NomenclaturalStatusType(UUID.fromString("c1acb71a-1d11-4305-8818-c2268d341742"), "Nomenclatural Status Type", "NST", null),

    //11
    /**
     * Term type for the type of a name relationship between {@link TaxonNameBase taxon names}
     * @see NameRelationship
     */
    @XmlEnumValue("NameRelationshipType")
    NameRelationshipType(UUID.fromString("acd8189a-23b9-4a53-8f48-1d2aa270a6ba"), "Name Relationship Type", "NRT"),

    //12
    /**
     * Term type for the type of a hybrid relationship between {@link TaxonNameBase taxon names}
     * @see HybridRelationship
     */
    @XmlEnumValue("HybridRelationshipType")
    HybridRelationshipType(UUID.fromString("aade9e61-eaa1-40fe-9eb1-40f9e8ae1114"), "Hybrid Relationship Type", "HRT"),

    //13
    /**
     * Term type for the type of a synonym relationship between 2 {@link TaxonBase taxa}
     * @see Synonym#getType()
     * @see TaxonBase
     */
    @XmlEnumValue("SynonymType")
    SynonymType(UUID.fromString("1eb4fee0-7716-4531-a9ed-a95327f1f4bb"), "Synonym Type", "SRT"),

    //14
    /**
     * Term type for the type of a taxonomic concept relationship between
     * 2 {@link Taxon accepted taxa}  with different secundum referece.
     * @see TaxonRelationship
     * @see Taxon
     */
    @XmlEnumValue("TaxonRelationshipType")
    TaxonRelationshipType(UUID.fromString("2d4b281c-142e-42c5-8eb5-1747592b54d8"), "Taxon Relationship Type", "TRT"),

    //15
    /**
     * Term type for the type of a typification of a taxonomic name
     * with a rank higher then species.
     * @see eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus
     * @see NameTypeDesignation
     */
    @XmlEnumValue("NameTypeDesignationStatus")
    NameTypeDesignationStatus(UUID.fromString("d3860be6-8a08-4fff-984a-6ee8b42937c9"), "Name Type Designation Status", "NTD"),

    //16
    /**
     * Term type for the type of a typification of a taxonomic name
     * with a rank equal or lower then species.
     * @see {@link eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus}
     * @see SpecimenTypeDesignation
     */
    @XmlEnumValue("SpecimenTypeDesignationStatus")
    SpecimenTypeDesignationStatus(UUID.fromString("4014d7d4-f2dd-4328-8015-357a1a77c1ed"), "Specimen Type Designation Status", "STD"),

    //17
    /**
     * Term type for an institution type.
     * @see Institution
     */
    @XmlEnumValue("InstitutionType")
    InstitutionType(UUID.fromString("09d78265-18b5-4352-b154-d2f39e84d3f3"), "Institution Type", "IT", null),

    //18
    /**
     * Term type for a named area type.
     * @see NamedArea
     * @see eu.etaxonomy.cdm.model.location.NamedAreaType
     */
    @XmlEnumValue("NamedAreaType")
    NamedAreaType(UUID.fromString("6a9aba35-6272-4373-8386-000cf95b729e"), "Named Area Type", "NAT", null),

    //19
    /**
     * Term type for a named area level.
     * @see NamedArea
     * @see eu.etaxonomy.cdm.model.location.NamedAreaLevel
     */
    @XmlEnumValue("NamedAreaLevel")
    NamedAreaLevel(UUID.fromString("62c16c74-dc79-4970-9031-bb1504be46f5"), "Named Area Level", "NAL", null),

    //20
    /**
     * Term type for rights.
     * @see Rights
     */
    @XmlEnumValue("RightsType")
    RightsType(UUID.fromString("80e06b04-8d0d-4bd5-bcd6-d35f73c24d55"), "Rights Type", "RT", null),

    //21
    /**
     * Term type for a measurement unit.
     * @see MeasurementUnit
     */
    @XmlEnumValue("MeasurementUnit")
    MeasurementUnit(UUID.fromString("f9e6c44f-f0d6-428b-9bc0-bb00a6514883"), "Measurement Unit", "MU", null),

    //22
    /**
     * Term type for a statistical measure.
     * @see eu.etaxonomy.cdm.model.description.StatisticalMeasure
     * @see StatisticalMeasurementValue
     */
    @XmlEnumValue("StatisticalMeasure")
    StatisticalMeasure(UUID.fromString("a22d19cd-a342-4af2-b156-d688a7aa8a6b"), "Statistical Measure", "SM", null),

    //23
    @XmlEnumValue("MaterialOrMethod")
    MaterialOrMethod(UUID.fromString("285a1406-a606-46b9-b0a4-717748296e50"), "Material or Method", "MOM"),

    //23a
    @XmlEnumValue("Material")
    Material(UUID.fromString("58499bc1-9808-4e03-861c-4325ca889515"), "Material", "MAT", MaterialOrMethod),

    //23b
    @XmlEnumValue("Method")
    Method(UUID.fromString("527fc141-7fdf-43c1-9d46-20be13c2590c"), "Method", "MET", MaterialOrMethod),


//	//23c TODO required??
//	@XmlEnumValue("PreservationMethod")   --> Method
//	PreservationMethod(UUID.fromString("9cadbee4-22b5-40f5-bb37-e4f5340d246e"), "Preservation method", "PRM", MaterialOrMethod),
//
//	//23d TODO required??  --> Material
//	@XmlEnumValue("PreservationMedium")
//	PreservationMedium(UUID.fromString("c6269a06-6954-4838-a7a7-7f94ba372f3d"), "Preservation medium", "PME", MaterialOrMethod),


    //24
    /**
     * This type represents possible modulations for the validity of
     * information pieces ({@link DescriptionElementBase} description elements).
     * It can cover probability ("perhaps"), frequency ("often") intensity ("very"),
     * timing ("spring") and other domains. Its instances can be grouped to build
     * different controlled {@link TermVocabulary term vocabularies}.
     * <P>
     * This class corresponds to GeneralModifierNLDType according to
     * the SDD schema.
     * This class is a generalization of {@link #Scope} and {@link #DeterminationModifier}
     *
     * @see DescriptionElementBase#getModifiers()
     * @see StateData#getModifiers()
     */
    @XmlEnumValue("Modifier")
    Modifier(UUID.fromString("97c4db67-ccf5-40bf-9fb8-83fb7446a364"), "Modifier", "MO", null),

    //25
    /**
     * The type representing restrictions for the validity of
     * {@link TaxonDescription taxon descriptions} and others.
     * This could include not only Stage (life stage) and Sex
     * but also for instance particular organism parts or seasons.<BR>
     * Scope is a specialization of {@link #Modifier} and a generalization of
     * {@link #Sex} and {@link #Stage}
     *
     * @see Modifier
     * @see DescriptionElementBase
     * @see TaxonDescription
     * @see TaxonDescription#getScopes()
     */
    @XmlEnumValue("Scope")
    Scope(UUID.fromString("8862b66e-9059-4ea4-885e-47a373357075"), "Scope", "SCO", Modifier),

    //26
    /** The stage type represents the restriction (scope) concerning the life stage for
     * the applicability of {@link TaxonDescription taxon descriptions}. The life stage of a
     * {@link SpecimenOrObservationBase specimen or observation}
     * does not belong to a {@link SpecimenDescription specimen description} but is an attribute of
     * the specimen itself.<BR>
     *
     * Stage is a specialization of {@link #Scope}.
     */
    @XmlEnumValue("Stage")
    Stage(UUID.fromString("cf411ef0-8eee-4461-99e9-c03f4f0a1656"), "Stage", "STG", Scope),

    //26a
    /** The kind-of-unit type represents the restriction (scope) concerning the part(s) of an
     * organism or the class of materials represented by a specimen or observation or described
     * by a {@link DescriptionBase taxon or specimen description}.<BR>
     * In a {@link DescriptionBase description} it can be used as a {@link #Modifier} or
     * a {@link #Scope} and therefore it is a specialization of {@link #Scope}.<BR>
     * The kind-of-unit of a {@link SpecimenOrObservationBase specimen or observation}
     * should be added as an {@link SpecimenOrObservationBase#getKindOfUnit() kindOfUnit} attribute
     * if the specimen is only a part of an organism and this part is described by the part-of-unit.
     * If only a part of a specimen is described in a {@link SpecimenDescription specimen description}
     * the kind-of-unit should be used as a {@link DescriptionBase description} scope
     * (not yet implemented for specimen descriptions) or as a modifier in a
     * {@link DescriptionElementBase description item}.<BR><BR>
     *
     * Value examples are: fruits, seeds, tissue, gDNA, leaves, ... <BR>
     *
     * KindOfUnit is a Specialization of {@link #Scope}.
     *
     * @see SpecimenOrObservationBase#getKindOfUnit()
     * @see TaxonDescription#getScopes()
     * @see DescriptionElementBase#getModifiers()
     * @see StateData#getModifiers()
     */
    @XmlEnumValue("KindOfUnit")
    KindOfUnit(UUID.fromString("e3edf086-ff7b-4e58-ab1c-d509352fe5f4"), "Kind of Unit", "KOU", Scope),


    //27
    /**
     * The type represents the restriction concerning the sex for
     * the applicability of {@link TaxonDescription taxon descriptions} or others. The sex of a
     * {@link SpecimenOrObservationBase specimen or observation}
     * does not belong to a {@link SpecimenDescription specimen description} but is an attribute of
     * the specimen itself.<BR>
     *
     * Sex is a specialization of {@link #Scope}.
     */
    @XmlEnumValue("Sex")
    Sex(UUID.fromString("4046f91f-063b-4b84-b34a-6245c2abc06f"), "Sex", "SEX", Scope),

    //28
    /**
     * Term type for a reference system of a geographic information.
     * @see Point
     * @see Point#getReferenceSystem()
     */
    @XmlEnumValue("ReferenceSystem")
    ReferenceSystem(UUID.fromString("b8cfa986-ef90-465e-9609-1dadae2a0f5b"), "Reference System", "RS", null),

    //29
    /**
     * Term type for a term representing a state for {@link CategoricalData categorical data}
     * @see CategoricalData#getStateData()
     * @see StateData#getState()
     */
    @XmlEnumValue("State")
    State(UUID.fromString("5e5b8b60-7300-440a-8706-72fbf31a594f"), "State", "STA", null),

    //30
    /**
     * Term type representing a natural language term.
     */
    @XmlEnumValue("NaturalLanguageTerm")
    NaturalLanguageTerm(UUID.fromString("9a42ac4e-c175-4633-8b31-74ba8203566a"), "Natural Language Term", "NLT", null),


    //31
    /**
     * Term type for a text format.
     * @see TextData
     */
    @XmlEnumValue("TextFormat")
    TextFormat(UUID.fromString("d26cfdb4-baeb-43d0-a51b-a3428d838790"), "Text Format", "TF", null),

    //32
    /**
     * TODO
     *
     * Determination modifier is a specialization of {@link #Modifier}.
     */
    @XmlEnumValue("DeterminationModifier")
    DeterminationModifier(UUID.fromString("ce910516-bc5d-4ac5-be4d-f3c14c27dd85"), "Determination Modifier", "DMO", Modifier),

    //33
    /**
     * A marker is a region on a DNA which is addressed in an {@link Amplification amplification process}.
     * It is very similar to a locus, a term which is often used as a synonym. However, a locus is correctly
     * defining one concrete place on a given DNA and therefore is more specific. As this specific information
     * is usually not available the marker information is provided instead.
     * Sometimes marker information includes multiple markers. To handle this it was decided that for such
     * combined markers we need to create a new "parent" marker which includes the single markers as "partOf" marker.
     * However, this works only if you have a distinct marker hierarchy, so futuere
     * @see Amplification
     * @see Amplification#getMarker()
     * @see Sequencing
     * @see Sequencing#getMarker()
     */
    @XmlEnumValue("DnaMarker")
    DnaMarker(UUID.fromString("7fdddb4f-b0ec-4ce0-bc28-dc94e30e8252"), "DNA Marker", "DMA", null),

  //34
    /**
     * An Identifier Type which can be used as a type in {@link Identifier#type} . It may often be a global unique identifier such as DOI, LSID, Barcode ....
     * But also local identifier types such Sample Designation, Catalog Number,
     * Accession Number, ... are allowed.
     * {@link Identifier#type}
     */
    @XmlEnumValue("IdentifierType")
    IdentifierType(UUID.fromString("d24eda99-0716-467f-aa1e-4c0f2b6c3922"), "Identifier Type", "IDE", null),

    //35
    /**
     * DNA Quality levels {link DnaQuality#type} . It may often be a global unique identifier such as DOI, LSID, Barcode ....
     * But also local identifier types such Sample Designation, Catalog Number,
     * Accession Number, ... are allowed.
     * {@link DnaQu}
     */
    @XmlEnumValue("DnaQualityType")
    DnaQualityType(UUID.fromString("18049372-56e6-4d45-85fc-6a403fecb881"), "DNA Quality Type", "DQT", null),

    //36
    /**
     * {@link TaxonNodeAgentRelation#getType() TaxonNode Agent Relation Type}.
     * The role an agent plays for a taxon node (classification subtree).
     */
    @XmlEnumValue("DnaQualityType")
    TaxonNodeAgentRelationType(UUID.fromString("8e28881a-0744-41ff-a5ed-31246125c424"), "TaxonNode Agent Relation Type", "TART", null),

    ;


// **************** END ENUM **********************/

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermType.class);

    private TermType(UUID uuid, String defaultString, String key){
        this(uuid, defaultString, key, null);
    }

    private TermType(UUID uuid, String defaultString, String key, TermType parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
    }


// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<TermType> delegateVoc;
    private IEnumTerm<TermType> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(TermType.class);
    }

    @Override
    public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getMessage(){return delegateVocTerm.getMessage();}

    @Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}

    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

    @Override
    public TermType getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<TermType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(TermType ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

    @Override
    public Set<TermType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static TermType getByKey(String key){return delegateVoc.getByKey(key);}
    public static TermType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

    /**
     * Returns a defined term base object corresponding to this term type object
     *
     * @return a defined term base object
     */
    public DefinedTermBase getEmptyDefinedTermBase() {
        switch(this) {
            case DeterminationModifier:
            case Modifier:
            case Scope:
            case Sex:
            case Stage:
            case KindOfUnit:
            case IdentifierType:
            case DnaMarker:
                return DefinedTerm.NewInstance(this, null, "Untitled", null);
            case Feature:
                return eu.etaxonomy.cdm.model.description.Feature.NewInstance(null, "Untitled", null);
            case State:
                return eu.etaxonomy.cdm.model.description.State.NewInstance(null, "Untitled", null);
            case MarkerType:
                return eu.etaxonomy.cdm.model.common.MarkerType.NewInstance(null, "Untitled", null);
            case ExtensionType:
                return eu.etaxonomy.cdm.model.common.ExtensionType.NewInstance(null, "Untitled", null);
            case NamedArea:
                return eu.etaxonomy.cdm.model.location.NamedArea.NewInstance(null, "Untitled", null);
            case NamedAreaLevel:
                return eu.etaxonomy.cdm.model.location.NamedAreaLevel.NewInstance(null, "Untitled", null);
            case NamedAreaType:
                return eu.etaxonomy.cdm.model.location.NamedAreaType.NewInstance(null, "Untitled", null);
            case PresenceAbsenceTerm:
            	return eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm.NewPresenceInstance(null, "Untitled", null);
            default:
                return null;
        }
    }

}
