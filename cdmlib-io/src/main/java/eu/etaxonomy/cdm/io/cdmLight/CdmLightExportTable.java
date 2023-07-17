/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

/**
 * An enumeration with each instance representing a table type in CDM light.
 *
 * @author a.mueller
 * @since 15.03.2017
 */
public enum CdmLightExportTable {
    METADATA("Metadata", metaDataColumns()),
    SCIENTIFIC_NAME("ScientificName", nameColumns()),
    NAME_RELATIONSHIP("NameRelationship",nameRelationColumns()),
    HOMOTYPIC_GROUP("HomotypicGroup", homotypicGroupColumns()),
    NOMENCLATURAL_AUTHOR("PersonOrTeam", nomenclaturalAuthorColumns()),
    NOMENCLATURAL_AUTHOR_TEAM_RELATION("PersonTeamRelation", nomenclaturalAuthorTeamRelColumns()),
    TYPE_DESIGNATION("SpecimenTypeDesignation", typeDesignationColumns()),
    SPECIMEN("Specimen", specimenColumns()),
    TAXON("Taxon", taxonColumns()),
    SYNONYM("Synonym", synonymColumns()),
    REFERENCE("Reference", referenceColumns()),
    SIMPLE_FACT("SimpleFact", simpleFactsColumns()),
    TAXON_INTERACTION_FACT("TaxonInteractionFact", taxonInteractionFactsColumns()),
    SPECIMEN_FACT("SpecimenFact", specimenFactsColumns()),
    GEOGRAPHIC_AREA_FACT("GeographicAreaFact", geographicAreaFactsColumns()),
    COMMON_NAME_FACT("CommonNameFact", commonNameFactsColumns()),
    FACT_SOURCES("FactSources", factSourcesColumns()),
    IDENTIFIER("Identifier", identifierColumns()),
    MEDIA("MediaFact", mediaColumns()),
//    CONDENSED_DISTRIBUTION_FACT("CondensedDistributionFact", compressedDistributionFactColumns()),
    NAME_FACT("NameFact", nameFactColumns()),
    TYPE_SPECIMEN_NAME("NameSpecimenTypeRelation", typeDesignationNameColumns())
    ;

    //Taxon/Synonym
    static final String NAME_FK = "Name_Fk";
    static final String TAXON_ID = "Taxon_ID";
    static final String TAXON_FK = "Taxon_Fk";
    static final String CLASSIFICATION_ID = "Classification_ID";
    static final String CLASSIFICATION_TITLE = "ClassificationName";
    static final String SYNONYM_ID = "Synonym_ID";
    static final String PARENT_FK = "Parent_Fk";
    static final String SEC_REFERENCE_FK = "SecReference_Fk";
    static final String SEC_REFERENCE = "SecReference";
    static final String SEC_SUBNAME_FK = "SecSubName_Fk";
    static final String SEC_SUBNAME = "SecSubName";
    static final String SEC_SUBNAME_AUTHORS = "SecSubNameAuthors";
    static final String SORT_INDEX = "SortIndex";
    static final String INCLUDED = "PlacementIncluded";
    static final String DOUBTFUL = "PlacementDoubtful";
    static final String UNPLACED = "Unplaced";
    static final String EXCLUDED = "Excluded";
    static final String EXCLUDED_EXACT = "ExcludedExact";
    static final String EXCLUDED_GEO = "ExcludedGeo";
    static final String EXCLUDED_TAX = "ExcludedTax";
    static final String EXCLUDED_NOM = "ExcludedNom";
    static final String UNCERTAIN_APPLICATION = "UncertainApplication";
    static final String UNRESOLVED = "PlacementUnresolved";
    static final String PLACEMENT_STATUS = "PlacementStatus";
    static final String PUBLISHED = "Published";
    static final String PLACEMENT_NOTES = "PlacementNotes";
    static final String PLACEMENT_REF_FK = "PlacementReference_Fk";
    static final String PLACEMENT_REFERENCE = "PlacementReference";

    //pro parte / misapplied
    static final String SYN_SEC_REFERENCE_FK = "SynSecReference_Fk";
    static final String SYN_SEC_REFERENCE = "SynSecReference";
    static final String IS_PRO_PARTE = "IsProParteSynonym";
    static final String IS_PARTIAL = "IsPartial";
    static final String IS_MISAPPLIED = "IsMisapplied";

    //Reference
    static final String REFERENCE_ID = "Reference_ID";
    static final String BIBLIO_SHORT_CITATION = "BibliographicShortCitation";
    static final String REF_TITLE = "Title";
    static final String ABBREV_REF_TITLE = "AbbrevTitle";
    static final String DATE_PUBLISHED = "DatePublished";
    static final String EDITION = "Edition";
    static final String EDITOR= "Editor";
    static final String ISBN = "ISBN";
    static final String ISSN = "ISSN";
    static final String ORGANISATION = "Organisation";
    static final String PAGES = "Pages";
    static final String PLACE_PUBLISHED = "PlacePublished";
    static final String PUBLISHER = "Publisher";
    static final String REF_ABSTRACT = "ReferenceAbstract";
    static final String SERIES_PART = "SeriesPart";
    static final String VOLUME = "Volume";
    static final String YEAR = "Year";
    static final String AUTHORSHIP_TITLE = "FullAuthor";


    static final String IN_REFERENCE = "InReference";
    static final String INSTITUTION = "Institution";
   // static final String LSID = "LSID";
    static final String SCHOOL = "School";
    static final String REF_TYPE = "ReferenceType";
    static final String URI = "URI";

    //Name
    static final String NAME_ID = "Name_ID";
//    static final String TROPICOS_ID = "Tropicos_ID";
//    static final String IPNI_ID = "IPNI_ID";
//    static final String WFO_ID = "WorldFloraOnline_ID";
    static final String LSID = "LSID";
    static final String RANK = "Rank";
    static final String RANK_SEQUENCE = "RankSequence";
    static final String FULL_NAME_WITH_AUTHORS = "FullNameWithAuthors";
    static final String FULL_NAME_WITH_REF = "FullNameWithRef";
    static final String FULL_NAME_NO_AUTHORS = "FullNameNoAuthors";
    static final String GENUS_UNINOMIAL = "GenusOrUninomial";
    static final String INFRAGENERIC_RANK = "InfragenericRank";
    static final String INFRAGENERIC_EPITHET = "InfraGenericEpithet";
    static final String SPECIFIC_EPITHET = "SpecificEpithet";
    static final String INFRASPECIFIC_RANK = "InfraspecificRank";
    static final String INFRASPECIFIC_EPITHET = "InfraSpecificEpithet ";
    static final String APPENDED_PHRASE = "AppendedPhrase";
    static final String BAS_EX_AUTHORTEAM_FK = "BasionymExAuthorTeam_Fk";
    static final String BAS_AUTHORTEAM_FK = "BasionymAuthorTeam_Fk";
    static final String COMB_EX_AUTHORTEAM_FK = "PublishingExAuthorTeam_Fk";
    static final String COMB_AUTHORTEAM_FK = "PublishingAuthorTeam_Fk";
    static final String AUTHOR_TEAM_STRING = "AuthorTeamString";
    static final String NAME_USED_IN_SOURCE_FK = "NameUsedInSource_Fk";
   // static final String REFERENCE_FK = "Reference_Fk"
    static final String PUBLICATION_TYPE = "PublicationType";
    static final String ABBREV_TITLE = "AbbreviatedTitle";
    static final String FULL_TITLE = "FullTitle";
    static final String ABBREV_REF_AUTHOR = "AbbreviatedInRefAuthor";
    static final String FULL_REF_AUTHOR = "FullInRefAuthor";
    static final String COLLATION = "Collation";
    static final String VOLUME_ISSUE = "VolumeIssue";
    static final String DETAIL = "Detail";
    static final String YEAR_PUBLISHED = "YearPublished";
    static final String VERBATIM_DATE = "VerbatimDate";
    static final String PROTOLOGUE_URI = "ProtologueURI";
    static final String NOM_STATUS = "NomenclaturalStatus";
    static final String NOM_STATUS_ABBREV = "NomenclaturalStatusAbbreviation";
    static final String HOMOTYPIC_GROUP_FK = "HomotypicGroup_Fk";
    static final String HOMOTYPIC_GROUP_SEQ = "HomotypicGroupSequenceNumber";
    static final String PROTOLOGUE_TYPE_STATEMENT = "ProtologueTypeStatement";
    static final String TYPE_SPECIMEN = "TypeSpecimens";
    static final String TYPE_STATEMENT = "TypeStatements";

    //Name Relationship
    static final String NAME1_FK = "Name1_Fk";
    static final String NAME2_FK = "Name2_Fk";
    static final String NAME_REL_TYPE = "NameRelationshipType";

    //CDM MetaData
    static final String INSTANCE_ID = "EditInstance_ID";
    static final String INSTANCE_NAME = "EditInstanceName";

    static final String DATASET_DESCRIPTION = "DatasetDescription";
    static final String DATASET_CREATOR = "DatasetCreator";
    static final String DATASET_CONTRIBUTOR = "DatasetContributor";
    static final String DATASET_TITLE = "DatasetTitle";
    static final String DATASET_LANGUAGE = "Language";
    static final String DATASET_LANDINGPAGE = "DataSetLandingPage";
    static final String DATASET_DOWNLOAD_LINK = "DatasetDownloadLink";
    static final String DATASET_BASE_URL = "DatasetBaseUrl";
    static final String DATASET_RECOMMENDED_CITATTION = "RecommendedCitation";
    static final String DATASET_LOCATION = "DatasetLocation";
    static final String DATASET_KEYWORDS = "DatasetKeywords";
    static final String DATASET_LICENCE = "Licence";

    //Homotypic Group
    static final String HOMOTYPIC_GROUP_ID = "HomotypicGroup_ID";
    static final String HOMOTYPIC_GROUP_STRING = "HomotypicGroupString";
    static final String HOMOTYPIC_GROUP_WITH_SEC_STRING = "HomotypicGroupStringWithSec";
    static final String HOMOTYPIC_GROUP_WITHOUT_ACCEPTED = "HomotypicGroupStringWithoutAccepted";
    static final String HOMOTYPIC_GROUP_WITHOUT_ACCEPTEDWITHSEC = "HomotypicGroupStringWithoutAcceptedWithSec";
    static final String HOMOTYPIC_GROUP_TYPE_STATEMENT_REFERENCE = "HomotypicGroupTypeStatementReference";

    static final String TYPE_STRING = "TypeSpecimenString";
    static final String TYPE_CACHE = "TypeStatementsString";
    static final String TYPE_STRING_WITH_REF = "TypeSpecimenStringWithRef";
    static final String TYPE_CACHE_WITH_REF = "TypeStatementsStringWithRef";

    //NomenclaturalAuthor
    static final String AUTHOR_ID = "PersonOrTeam_ID";
    static final String ABBREV_AUTHOR = "AbbrevNames";
    static final String AUTHOR_TITLE = "FullNames";
    static final String AUTHOR_GIVEN_NAME = "PersonOtherNames";
    static final String AUTHOR_FAMILY_NAME = "PersonFamiliyNames";
    static final String AUTHOR_PREFIX = "PersonPrefix";
    static final String AUTHOR_SUFFIX = "PersonSuffix";

  //Nomenclatural Author AuthorTeam Relations

    static final String AUTHOR_FK = "Author_Fk";
    static final String AUTHOR_TEAM_FK = "AuthorTeam_Fk";
    static final String AUTHOR_TEAM_SEQ_NUMBER = "SequenceNumber";

    //TypeDesignations
    static final String TYPE_ID="Type_ID";
    static final String SPECIMEN_FK = "Specimen_Fk";
    static final String TYPIFIED_NAME_FK = "TypifiedName_Fk";
    static final String TYPE_VERBATIM_CITATION = "TypeVerbatimCitation";
    static final String TYPE_STATUS = "TypeStatus";
    static final String TYPE_INFORMATION_REF_STRING = "TypeInformationSource";
    static final String TYPE_INFORMATION_REF_FK = "TypeInfoSourcePreferred_Fk";
    static final String TYPE_DESIGNATED_BY_REF_FK = "TypeDesignatedBy_Fk";

    //Type_Name_Rel
    static final String TYPE_FK="Type_Fk";


    //Specimen
    static final String SPECIMEN_ID = "Specimen_ID";
    static final String SPECIMEN_CITATION = "SpecimenCitation";
    static final String FIELDUNIT_CITATION = "FieldUnitCitation";
    static final String LOCALITY= "Locality";
    static final String COUNTRY = "Country";
    static final String AREA_CATEGORY1 = "AreaCategory1";
    static final String AREA_NAME1 = "AreaName1";
    static final String AREA_CATEGORY2 = "AreaCategory2";
    static final String AREA_NAME2 = "AreaName2";
    static final String AREA_CATEGORY3 = "AreaCategory3";
    static final String AREA_NAME3 = "AreaName3";
    static final String FURTHER_AREAS = "FurtherAreas";
    static final String COLLECTOR_STRING = "CollectorString";
    static final String COLLECTOR_NUMBER = "CollectorNumber";
    static final String COLLECTION_DATE = "CollectionDate";
    static final String SPECIMEN_IMAGE_URIS = "SpecimenImageURIs";
    static final String HERBARIUM_ABBREV = "HerbariumAbbrev";
    static final String MEDIA_SPECIMEN_URL = "MediaSpecimenURI";
    static final String PREFERREDSTABLE_ID = "PreferredStableId";
    static final String BARCODE = "Barcode";
    static final String ACCESSION_NUMBER = "AccessionNumber";
    static final String CATALOGUE_NUMBER = "CatalogueNumber ";
    //other specimen attributes

    //SimpleFacts
    static final String FACT_ID = "Fact_ID";
    static final String FACT_TEXT = "FactText";
    static final String LANGUAGE = "Language";
    static final String MEDIA_URI = "MediaURI";
    static final String FACT_CATEGORY = "FactCategory";

    // Specimen Facts
    static final String SPECIMEN_NOTES = "SpecimenNotes";
    static final String SPECIMEN_DESCRIPTION = "SpecimenDescription";

    // TaxonInteraction Facts
    static final String TAXON2_FK = "Taxon2_FK";
    static final String DESCRIPTION = "Description";

    //Geographic Area Facts
    static final String AREA_LABEL = "AreaLabel";
    static final String STATUS_LABEL = "StatusLabel";

    //FactSources
    static final String FACT_FK = "Fact_Fk";
    static final String REFERENCE_FK = "Reference_Fk";
    static final String NAME_IN_SOURCE_FK = "NameInSource_Fk";
    static final String FACT_TYPE = "FactType";

    //Annotations

    //Identifiers
    static final String EXTERNAL_NAME_IDENTIFIER = "ExternalIdentifier";
    static final String IDENTIFIER_TYPE = "IdentifierType";
    static final String FK = "ForeignKey";
    static final String REF_TABLE = "ReferencedTable";

    private final static String[] homotypicGroupColumns(){
        return new String[]{HOMOTYPIC_GROUP_ID, HOMOTYPIC_GROUP_STRING, TYPE_STRING, TYPE_CACHE, HOMOTYPIC_GROUP_WITH_SEC_STRING, HOMOTYPIC_GROUP_WITHOUT_ACCEPTED, HOMOTYPIC_GROUP_WITHOUT_ACCEPTEDWITHSEC, SORT_INDEX, HOMOTYPIC_GROUP_TYPE_STATEMENT_REFERENCE};
    }

//    private static String[] compressedDistributionFactColumns() {
//        return new String[]{FACT_ID, TAXON_FK, FACT_TEXT};
//    }

    private Object usageFactColumns() {
        return new String[]{FACT_ID, TAXON_FK, FACT_TEXT, LANGUAGE, MEDIA_URI, FACT_CATEGORY};
    }

    private final static String[] nameFactColumns() {
        return new String[]{FACT_ID, NAME_FK, FACT_TEXT, LANGUAGE, MEDIA_URI, FACT_CATEGORY};
    }

    private final static String[] taxonInteractionFactsColumns(){
        return new String[]{FACT_ID, TAXON_FK, TAXON2_FK, DESCRIPTION};
    }

    private final static String[] identifierColumns() {
        return new String[]{ FK, REF_TABLE, EXTERNAL_NAME_IDENTIFIER, IDENTIFIER_TYPE};
    }

    private final static String[] mediaColumns() {
        return new String[]{ FACT_ID, TAXON_FK, NAME_FK, MEDIA_URI};
    }

    private final static String[]  factSourcesColumns() {
        return new String[]{FACT_FK, REFERENCE_FK, NAME_IN_SOURCE_FK, FACT_TYPE};
    }

    private final static String[] specimenFactsColumns() {
        return new String[]{FACT_ID, TAXON_FK, SPECIMEN_FK, SPECIMEN_DESCRIPTION, SPECIMEN_NOTES};
    }

    private final static String[] commonNameFactsColumns() {
        return new String[]{FACT_ID, TAXON_FK, FACT_TEXT, LANGUAGE, AREA_LABEL};
    }

    private final static String[] geographicAreaFactsColumns() {
        return new String[]{FACT_ID, TAXON_FK, AREA_LABEL, STATUS_LABEL};
    }

    private final static String[] simpleFactsColumns() {
        return new String[]{FACT_ID, TAXON_FK, FACT_TEXT, LANGUAGE, MEDIA_URI, FACT_CATEGORY};
    }

    private final static String[] nomenclaturalAuthorColumns() {
        return new String[]{AUTHOR_ID, ABBREV_AUTHOR, AUTHOR_TITLE, AUTHOR_GIVEN_NAME, AUTHOR_FAMILY_NAME, AUTHOR_PREFIX, AUTHOR_SUFFIX};
    }

    private final static String[] nomenclaturalAuthorTeamRelColumns() {
        return new String[]{AUTHOR_TEAM_FK, AUTHOR_FK, AUTHOR_TEAM_SEQ_NUMBER};
    }

    private final static String[] metaDataColumns(){
        return new String[]{INSTANCE_ID, INSTANCE_NAME, DATASET_TITLE, DATASET_CONTRIBUTOR, DATASET_CREATOR, DATASET_DESCRIPTION,
                DATASET_DOWNLOAD_LINK, DATASET_BASE_URL, DATASET_KEYWORDS, DATASET_LANDINGPAGE, DATASET_LANGUAGE, DATASET_LICENCE,
                DATASET_LOCATION, DATASET_RECOMMENDED_CITATTION};
    }

    private final static String[] nameRelationColumns(){
        return new String[]{NAME1_FK, NAME2_FK, NAME_REL_TYPE};
    }

    private final static String[] nameColumns(){
        return new String[]{NAME_ID, LSID, RANK, RANK_SEQUENCE,
                FULL_NAME_WITH_AUTHORS, FULL_NAME_NO_AUTHORS, GENUS_UNINOMIAL,
                INFRAGENERIC_RANK, INFRAGENERIC_EPITHET, SPECIFIC_EPITHET,
                INFRASPECIFIC_RANK, INFRASPECIFIC_EPITHET,
                BAS_EX_AUTHORTEAM_FK, BAS_AUTHORTEAM_FK, COMB_EX_AUTHORTEAM_FK, COMB_AUTHORTEAM_FK,
                AUTHOR_TEAM_STRING, REFERENCE_FK, PUBLICATION_TYPE, ABBREV_TITLE, FULL_TITLE,
                ABBREV_REF_AUTHOR, FULL_REF_AUTHOR, COLLATION, VOLUME_ISSUE,
                DETAIL, DATE_PUBLISHED, YEAR_PUBLISHED, VERBATIM_DATE, PROTOLOGUE_URI,
                NOM_STATUS, NOM_STATUS_ABBREV, HOMOTYPIC_GROUP_FK,
                HOMOTYPIC_GROUP_SEQ, PROTOLOGUE_TYPE_STATEMENT, TYPE_SPECIMEN, TYPE_STATEMENT, FULL_NAME_WITH_REF, NAME_USED_IN_SOURCE_FK, APPENDED_PHRASE
        };
    }

    private final static String[] taxonColumns(){
        return new String[]{TAXON_ID, CLASSIFICATION_ID, CLASSIFICATION_TITLE, NAME_FK, PARENT_FK, SEC_REFERENCE_FK, SEC_REFERENCE, SEC_SUBNAME_FK, SEC_SUBNAME, SEC_SUBNAME_AUTHORS, APPENDED_PHRASE, SORT_INDEX,
                INCLUDED, DOUBTFUL, UNPLACED, EXCLUDED, EXCLUDED_EXACT, EXCLUDED_GEO, EXCLUDED_TAX, EXCLUDED_NOM, UNCERTAIN_APPLICATION, UNRESOLVED, PLACEMENT_STATUS,
                PLACEMENT_NOTES, PLACEMENT_REF_FK, PLACEMENT_REFERENCE, PUBLISHED};
    }

    private final static String[] synonymColumns(){
        return new String[]{SYNONYM_ID, TAXON_FK, NAME_FK, SYN_SEC_REFERENCE_FK, SYN_SEC_REFERENCE, SEC_REFERENCE_FK, SEC_REFERENCE,
                IS_PRO_PARTE, IS_PARTIAL, IS_MISAPPLIED, PUBLISHED, SORT_INDEX, APPENDED_PHRASE};
    }

    private final static String[] referenceColumns(){
        return new String[]{REFERENCE_ID, BIBLIO_SHORT_CITATION, REF_TITLE,ABBREV_REF_TITLE, DATE_PUBLISHED, EDITION, EDITOR, ISBN,ISSN, ORGANISATION, PAGES, PLACE_PUBLISHED, PUBLISHER,
                REF_ABSTRACT, SERIES_PART, VOLUME, YEAR, AUTHORSHIP_TITLE, AUTHOR_FK, IN_REFERENCE, INSTITUTION, LSID, SCHOOL, REF_TYPE, URI};
    }

    private final static String[] typeDesignationColumns(){
        return new String[]{TYPE_ID, TYPIFIED_NAME_FK, SPECIMEN_FK, //TYPE_VERBATIM_CITATION,
                TYPE_STATUS, TYPE_DESIGNATED_BY_REF_FK, TYPE_INFORMATION_REF_STRING, TYPE_INFORMATION_REF_FK};
    }

    private final static String[] typeDesignationNameColumns(){
        return new String[]{TYPE_FK, NAME_FK};
    }

    private final static String[] specimenColumns() {
        return new String[]{SPECIMEN_ID, SPECIMEN_CITATION, FIELDUNIT_CITATION, LOCALITY, COUNTRY, AREA_CATEGORY1, AREA_NAME1, AREA_CATEGORY2, AREA_NAME2, AREA_CATEGORY3, AREA_NAME3,
                FURTHER_AREAS, COLLECTOR_STRING, COLLECTOR_NUMBER, COLLECTION_DATE, SPECIMEN_IMAGE_URIS, HERBARIUM_ABBREV, MEDIA_SPECIMEN_URL, PREFERREDSTABLE_ID, BARCODE, CATALOGUE_NUMBER, ACCESSION_NUMBER};
    }

    private String tableName;
    private String[] columnNames;

// ************** CONSTRUCTOR *******************/

    private CdmLightExportTable(String tableName, String[] columnNames){
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

// ****************** GETTER / SETTER *************/

    public String getTableName() {return tableName;}

    public int getSize(){ return columnNames.length;}

    public String[] getColumnNames(){return columnNames;}

    public int getIndex(String columnName) {
        int index= 0;
        for(String column : getColumnNames()){
            if (column.equals(columnName)){
                return index;
            }
            index++;
        }
        return -1;
    }
}