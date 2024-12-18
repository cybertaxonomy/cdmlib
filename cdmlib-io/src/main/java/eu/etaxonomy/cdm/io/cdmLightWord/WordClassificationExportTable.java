/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLightWord;

/**
 * An enumeration with each instance representing a table type in the Output Model.
 *
 * @author a.mueller
 * @date 29.06.2022
 */
public enum WordClassificationExportTable {
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
    protected static final String NAME_FK = "Name_Fk";
    protected static final String TAXON_ID = "Taxon_ID";
    protected static final String TAXON_FK = "Taxon_Fk";
    protected static final String CLASSIFICATION_ID = "Classification_ID";
    protected static final String CLASSIFICATION_TITLE = "ClassificationName";
    protected static final String SYNONYM_ID = "Synonym_ID";
    protected static final String PARENT_FK = "Parent_Fk";
    protected static final String SEC_REFERENCE_FK = "SecReference_Fk";
    protected static final String SEC_REFERENCE = "SecReference";
    protected static final String SORT_INDEX = "SortIndex";
    protected static final String UNPLACED = "Unplaced";
    protected static final String EXCLUDED = "Excluded";
    protected static final String DOUBTFUL = "PlacementDoubtful";
    protected static final String PUBLISHED = "Published";
    protected static final String PLACEMENT_NOTES = "PlacementNotes";

    //pro parte / misapplied
    protected static final String SYN_SEC_REFERENCE_FK = "SynSecReference_Fk";
    protected static final String SYN_SEC_REFERENCE = "SynSecReference";
    protected static final String IS_PRO_PARTE = "IsProParteSynonym";
    protected static final String IS_PARTIAL = "IsPartial";
    protected static final String IS_MISAPPLIED = "IsMisapplied";

    //Reference
    protected static final String REFERENCE_ID = "Reference_ID";
    protected static final String BIBLIO_SHORT_CITATION = "BibliographicShortCitation";
    protected static final String REF_TITLE = "Title";
    protected static final String ABBREV_REF_TITLE = "AbbrevTitle";
    protected static final String DATE_PUBLISHED = "DatePublished";
    protected static final String EDITION = "Edition";
    protected static final String EDITOR= "Editor";
    protected static final String ISBN = "ISBN";
    protected static final String ISSN = "ISSN";
    protected static final String ORGANISATION = "Organisation";
    protected static final String PAGES = "Pages";
    protected static final String PLACE_PUBLISHED = "PlacePublished";
    protected static final String PUBLISHER = "Publisher";
    protected static final String REF_ABSTRACT = "ReferenceAbstract";
    protected static final String SERIES_PART = "SeriesPart";
    protected static final String VOLUME = "Volume";
    protected static final String YEAR = "Year";
    protected static final String AUTHORSHIP_TITLE = "FullAuthor";


    protected static final String IN_REFERENCE = "InReference";
    protected static final String INSTITUTION = "Institution";
   // protected static final String LSID = "LSID";
    protected static final String SCHOOL = "School";
    protected static final String REF_TYPE = "ReferenceType";
    protected static final String URI = "URI";

    //Name
    protected static final String NAME_ID = "Name_ID";
//    protected static final String TROPICOS_ID = "Tropicos_ID";
//    protected static final String IPNI_ID = "IPNI_ID";
//    protected static final String WFO_ID = "WorldFloraOnline_ID";
    protected static final String LSID = "LSID";
    protected static final String RANK = "Rank";
    protected static final String RANK_SEQUENCE = "RankSequence";
    protected static final String FULL_NAME_WITH_AUTHORS = "FullNameWithAuthors";
    protected static final String FULL_NAME_WITH_REF = "FullNameWithRef";
    protected static final String FULL_NAME_NO_AUTHORS = "FullNameNoAuthors";
    protected static final String GENUS_UNINOMIAL = "GenusOrUninomial";
    protected static final String INFRAGENERIC_RANK = "InfragenericRank";
    protected static final String INFRAGENERIC_EPITHET = "InfraGenericEpithet";
    protected static final String SPECIFIC_EPITHET = "SpecificEpithet";
    protected static final String INFRASPECIFIC_RANK = "InfraspecificRank";
    protected static final String INFRASPECIFIC_EPITHET = "InfraSpecificEpithet ";
    protected static final String APPENDED_PHRASE = "AppendedPhrase";
    protected static final String BAS_EX_AUTHORTEAM_FK = "BasionymExAuthorTeam_Fk";
    protected static final String BAS_AUTHORTEAM_FK = "BasionymAuthorTeam_Fk";
    protected static final String COMB_EX_AUTHORTEAM_FK = "PublishingExAuthorTeam_Fk";
    protected static final String COMB_AUTHORTEAM_FK = "PublishingAuthorTeam_Fk";
    protected static final String AUTHOR_TEAM_STRING = "AuthorTeamString";
    protected static final String NAME_USED_IN_SOURCE = "NameUsedInSource_Fk";
   // protected static final String REFERENCE_FK = "Reference_Fk"
    protected static final String PUBLICATION_TYPE = "PublicationType";
    protected static final String ABBREV_TITLE = "AbbreviatedTitle";
    protected static final String FULL_TITLE = "FullTitle";
    protected static final String ABBREV_REF_AUTHOR = "AbbreviatedInRefAuthor";
    protected static final String FULL_REF_AUTHOR = "FullInRefAuthor";
    protected static final String COLLATION = "Collation";
    protected static final String VOLUME_ISSUE = "VolumeIssue";
    protected static final String DETAIL = "Detail";
    protected static final String YEAR_PUBLISHED = "YearPublished";
    protected static final String VERBATIM_DATE = "VerbatimDate";
    protected static final String PROTOLOGUE_URI = "ProtologueURI";
    protected static final String NOM_STATUS = "NomenclaturalStatus";
    protected static final String NOM_STATUS_ABBREV = "NomenclaturalStatusAbbreviation";
    protected static final String HOMOTYPIC_GROUP_FK = "HomotypicGroup_Fk";
    protected static final String HOMOTYPIC_GROUP_SEQ = "HomotypicGroupSequenceNumber";
    protected static final String PROTOLOGUE_TYPE_STATEMENT = "ProtologueTypeStatement";
    protected static final String TYPE_SPECIMEN = "TypeSpecimens";
    protected static final String TYPE_STATEMENT = "TypeStatements";



    //Name Relationship
    protected static final String NAME1_FK = "Name1_Fk";
    protected static final String NAME2_FK = "Name2_Fk";
    protected static final String NAME_REL_TYPE = "NameRelationshipType";

    //CDM MetaData
    protected static final String INSTANCE_ID = "EditInstance_ID";
    protected static final String INSTANCE_NAME = "EditInstanceName";

    protected static final String DATASET_DESCRIPTION = "DatasetDescription";
    protected static final String DATASET_CREATOR = "DatasetCreator";
    protected static final String DATASET_CONTRIBUTOR = "DatasetContributor";
    protected static final String DATASET_TITLE = "DatasetTitle";
    protected static final String DATASET_LANGUAGE = "Language";
    protected static final String DATASET_LANDINGPAGE = "DataSetLandingPage";
    protected static final String DATASET_DOWNLOAD_LINK = "DatasetDownloadLink";
    protected static final String DATASET_BASE_URL = "DatasetBaseUrl";
    protected static final String DATASET_RECOMMENDED_CITATTION = "RecommendedCitation";
    protected static final String DATASET_LOCATION = "DatasetLocation";
    protected static final String DATASET_KEYWORDS = "DatasetKeywords";
    protected static final String DATASET_LICENCE = "Licence";


    //Homotypic Group
    protected static final String HOMOTYPIC_GROUP_ID = "HomotypicGroup_ID";
    protected static final String HOMOTYPIC_GROUP_STRING = "HomotypicGroupString";
    protected static final String HOMOTYPIC_GROUP_WITH_SEC_STRING = "HomotypicGroupStringWithSec";
    protected static final String HOMOTYPIC_GROUP_WITHOUT_ACCEPTED = "HomotypicGroupStringWithoutAccepted";
    protected static final String HOMOTYPIC_GROUP_WITHOUT_ACCEPTEDWITHSEC = "HomotypicGroupStringWithoutAcceptedWithSec";
    protected static final String HOMOTYPIC_GROUP_TYPE_STATEMENT_REFERENCE = "HomotypicGroupTypeStatementReference";

    protected static final String TYPE_STRING = "TypeSpecimenString";
    protected static final String TYPE_CACHE = "TypeStatementsString";
    protected static final String TYPE_STRING_WITH_REF = "TypeSpecimenStringWithRef";
    protected static final String TYPE_CACHE_WITH_REF = "TypeStatementsStringWithRef";

    //NomenclaturalAuthor
    protected static final String AUTHOR_ID = "PersonOrTeam_ID";
    protected static final String ABBREV_AUTHOR = "AbbrevNames";
    protected static final String AUTHOR_TITLE = "FullNames";
    protected static final String AUTHOR_GIVEN_NAME = "PersonOtherNames";
    protected static final String AUTHOR_FAMILY_NAME = "PersonFamiliyNames";
    protected static final String AUTHOR_PREFIX = "PersonPrefix";
    protected static final String AUTHOR_SUFFIX = "PersonSuffix";

  //Nomenclatural Author AuthorTeam Relations

    protected static final String AUTHOR_FK = "Author_Fk";
    protected static final String AUTHOR_TEAM_FK = "AuthorTeam_Fk";
    protected static final String AUTHOR_TEAM_SEQ_NUMBER = "SequenceNumber";

    //TypeDesignations
    protected static final String TYPE_ID="Type_ID";
    protected static final String SPECIMEN_FK = "Specimen_Fk";
    protected static final String TYPIFIED_NAME_FK = "TypifiedName_Fk";
    protected static final String TYPE_VERBATIM_CITATION = "TypeVerbatimCitation";
    protected static final String TYPE_STATUS = "TypeStatus";
    protected static final String TYPE_INFORMATION_REF_STRING = "TypeInformationSource";
    protected static final String TYPE_INFORMATION_REF_FK = "TypeInfoSourcePreferred_Fk";
    protected static final String TYPE_DESIGNATED_BY_REF_FK = "TypeDesignatedBy_Fk";

    //Type_Name_Rel
    protected static final String TYPE_FK="Type_Fk";


    //Specimen
    protected static final String SPECIMEN_ID = "Specimen_ID";
    protected static final String SPECIMEN_CITATION = "SpecimenCitation";
    protected static final String FIELDUNIT_CITATION = "FieldUnitCitation";
    protected static final String LOCALITY= "Locality";
    protected static final String COUNTRY = "Country";
    protected static final String AREA_CATEGORY1 = "AreaCategory1";
    protected static final String AREA_NAME1 = "AreaName1";
    protected static final String AREA_CATEGORY2 = "AreaCategory2";
    protected static final String AREA_NAME2 = "AreaName2";
    protected static final String AREA_CATEGORY3 = "AreaCategory3";
    protected static final String AREA_NAME3 = "AreaName3";
    protected static final String FURTHER_AREAS = "FurtherAreas";
    protected static final String COLLECTOR_STRING = "CollectorString";
    protected static final String COLLECTOR_NUMBER = "CollectorNumber";
    protected static final String COLLECTION_DATE = "CollectionDate";
    protected static final String SPECIMEN_IMAGE_URIS = "SpecimenImageURIs";
    protected static final String HERBARIUM_ABBREV = "HerbariumAbbrev";
    protected static final String MEDIA_SPECIMEN_URL = "MediaSpecimenURI";
    protected static final String PREFERREDSTABLE_ID = "PreferredStableId";
    protected static final String BARCODE = "Barcode";
    protected static final String ACCESSION_NUMBER = "AccessionNumber";
    protected static final String CATALOGUE_NUMBER = "CatalogueNumber ";
    //other specimen attributes

    //SimpleFacts
    protected static final String FACT_ID = "Fact_ID";
    protected static final String FACT_TEXT = "FactText";
    protected static final String LANGUAGE = "Language";
    protected static final String MEDIA_URI = "MediaURI";
    protected static final String FACT_CATEGORY = "FactCategory";

    // Specimen Facts
    protected static final String SPECIMEN_NOTES = "SpecimenNotes";
    protected static final String SPECIMEN_DESCRIPTION = "SpecimenDescription";

    // TaxonInteraction Facts
    protected static final String TAXON2_FK = "Taxon2_FK";
    protected static final String DESCRIPTION = "Description";

    //Geographic Area Facts
    protected static final String AREA_LABEL = "AreaLabel";
    protected static final String STATUS_LABEL = "StatusLabel";

    //FactSources
    protected static final String FACT_FK = "Fact_Fk";
    protected static final String REFERENCE_FK = "Reference_Fk";
    protected static final String NAME_IN_SOURCE_FK = "NameInSource_Fk";
    protected static final String FACT_TYPE = "FactType";

    //Annotations


    //Identifiers
    protected static final String EXTERNAL_NAME_IDENTIFIER = "ExternalIdentifier";
    protected static final String IDENTIFIER_TYPE = "IdentifierType";
    protected static final String FK = "ForeignKey";
    protected static final String REF_TABLE = "ReferencedTable";

    final static String[] homotypicGroupColumns(){
        return new String[]{HOMOTYPIC_GROUP_ID, HOMOTYPIC_GROUP_STRING, TYPE_STRING, TYPE_CACHE, HOMOTYPIC_GROUP_WITH_SEC_STRING, HOMOTYPIC_GROUP_WITHOUT_ACCEPTED, HOMOTYPIC_GROUP_WITHOUT_ACCEPTEDWITHSEC, SORT_INDEX, HOMOTYPIC_GROUP_TYPE_STATEMENT_REFERENCE};

    }

//    private static String[] compressedDistributionFactColumns() {
//        return new String[]{FACT_ID, TAXON_FK, FACT_TEXT};
//    }

    private Object usageFactColumns() {
        return new String[]{FACT_ID, TAXON_FK, FACT_TEXT, LANGUAGE, MEDIA_URI, FACT_CATEGORY};
    }

    final static String[] nameFactColumns() {
        return new String[]{FACT_ID, NAME_FK, FACT_TEXT, LANGUAGE, MEDIA_URI, FACT_CATEGORY};
    }
    final static String[] taxonInteractionFactsColumns(){
        return new String[]{FACT_ID, TAXON_FK, TAXON2_FK, DESCRIPTION};
    }

    final static String[] identifierColumns() {
        return new String[]{ FK, REF_TABLE, EXTERNAL_NAME_IDENTIFIER, IDENTIFIER_TYPE};
    }

    final static String[] mediaColumns() {
        return new String[]{ FACT_ID, TAXON_FK, NAME_FK, MEDIA_URI};
    }

    final static String[]  factSourcesColumns() {
        return new String[]{FACT_FK, REFERENCE_FK, NAME_IN_SOURCE_FK, FACT_TYPE};
    }

    final static String[] specimenFactsColumns() {
        return new String[]{FACT_ID, TAXON_FK, SPECIMEN_FK, SPECIMEN_DESCRIPTION, SPECIMEN_NOTES};
    }

    final static String[] commonNameFactsColumns() {
        return new String[]{FACT_ID, TAXON_FK, FACT_TEXT, LANGUAGE, AREA_LABEL};

    }
    final static String[] geographicAreaFactsColumns() {
        return new String[]{FACT_ID, TAXON_FK, AREA_LABEL, STATUS_LABEL};
    }

    final static String[] simpleFactsColumns() {
        return new String[]{FACT_ID, TAXON_FK, FACT_TEXT, LANGUAGE, MEDIA_URI, FACT_CATEGORY};
    }

    final static String[] nomenclaturalAuthorColumns() {
        return new String[]{AUTHOR_ID, ABBREV_AUTHOR, AUTHOR_TITLE, AUTHOR_GIVEN_NAME, AUTHOR_FAMILY_NAME, AUTHOR_PREFIX, AUTHOR_SUFFIX};
    }

    final static String[] nomenclaturalAuthorTeamRelColumns() {
        return new String[]{AUTHOR_TEAM_FK, AUTHOR_FK, AUTHOR_TEAM_SEQ_NUMBER};
    }

    final static String[] metaDataColumns(){
        return new String[]{INSTANCE_ID, INSTANCE_NAME, DATASET_TITLE, DATASET_CONTRIBUTOR, DATASET_CREATOR, DATASET_DESCRIPTION,
                DATASET_DOWNLOAD_LINK, DATASET_BASE_URL, DATASET_KEYWORDS, DATASET_LANDINGPAGE, DATASET_LANGUAGE, DATASET_LICENCE,
                DATASET_LOCATION, DATASET_RECOMMENDED_CITATTION};
    }

    final static String[] nameRelationColumns(){
        return new String[]{NAME1_FK, NAME2_FK, NAME_REL_TYPE};
    }

    final static String[] nameColumns(){
        return new String[]{NAME_ID, LSID, RANK, RANK_SEQUENCE,
                FULL_NAME_WITH_AUTHORS, FULL_NAME_NO_AUTHORS, GENUS_UNINOMIAL,
                INFRAGENERIC_RANK, INFRAGENERIC_EPITHET, SPECIFIC_EPITHET,
                INFRASPECIFIC_RANK, INFRASPECIFIC_EPITHET,
                BAS_EX_AUTHORTEAM_FK, BAS_AUTHORTEAM_FK, COMB_EX_AUTHORTEAM_FK, COMB_AUTHORTEAM_FK,
                AUTHOR_TEAM_STRING, REFERENCE_FK, PUBLICATION_TYPE, ABBREV_TITLE, FULL_TITLE,
                ABBREV_REF_AUTHOR, FULL_REF_AUTHOR, COLLATION, VOLUME_ISSUE,
                DETAIL, DATE_PUBLISHED, YEAR_PUBLISHED, VERBATIM_DATE, PROTOLOGUE_URI,
                NOM_STATUS, NOM_STATUS_ABBREV, HOMOTYPIC_GROUP_FK,
                HOMOTYPIC_GROUP_SEQ, PROTOLOGUE_TYPE_STATEMENT, TYPE_SPECIMEN, TYPE_STATEMENT, FULL_NAME_WITH_REF, NAME_USED_IN_SOURCE, APPENDED_PHRASE
        };
    }

    final static String[] taxonColumns(){
        return new String[]{TAXON_ID, CLASSIFICATION_ID, CLASSIFICATION_TITLE, NAME_FK, PARENT_FK, SEC_REFERENCE_FK, SEC_REFERENCE, SORT_INDEX, EXCLUDED, PLACEMENT_NOTES, PUBLISHED, UNPLACED, DOUBTFUL, APPENDED_PHRASE};
    }

    final static String[] synonymColumns(){
        return new String[]{SYNONYM_ID, TAXON_FK, NAME_FK, SYN_SEC_REFERENCE_FK, SYN_SEC_REFERENCE, SEC_REFERENCE_FK, SEC_REFERENCE,
                IS_PRO_PARTE, IS_PARTIAL, IS_MISAPPLIED, PUBLISHED, SORT_INDEX, APPENDED_PHRASE};
    }

    final static String[] referenceColumns(){
        return new String[]{REFERENCE_ID, BIBLIO_SHORT_CITATION, REF_TITLE,ABBREV_REF_TITLE, DATE_PUBLISHED, EDITION, EDITOR, ISBN,ISSN, ORGANISATION, PAGES, PLACE_PUBLISHED, PUBLISHER,
                REF_ABSTRACT, SERIES_PART, VOLUME, YEAR, AUTHORSHIP_TITLE, AUTHOR_FK, IN_REFERENCE, INSTITUTION, LSID, SCHOOL, REF_TYPE, URI};
    }

    final static String[] typeDesignationColumns(){
        return new String[]{TYPE_ID, TYPIFIED_NAME_FK, SPECIMEN_FK, //TYPE_VERBATIM_CITATION,
                TYPE_STATUS, TYPE_DESIGNATED_BY_REF_FK, TYPE_INFORMATION_REF_STRING, TYPE_INFORMATION_REF_FK};

    }

    final static String[] typeDesignationNameColumns(){
        return new String[]{TYPE_FK, NAME_FK};
    }


    final static String[] specimenColumns() {
        return new String[]{SPECIMEN_ID, SPECIMEN_CITATION, FIELDUNIT_CITATION, LOCALITY, COUNTRY, AREA_CATEGORY1, AREA_NAME1, AREA_CATEGORY2, AREA_NAME2, AREA_CATEGORY3, AREA_NAME3,
                FURTHER_AREAS, COLLECTOR_STRING, COLLECTOR_NUMBER, COLLECTION_DATE, SPECIMEN_IMAGE_URIS, HERBARIUM_ABBREV, MEDIA_SPECIMEN_URL, PREFERREDSTABLE_ID, BARCODE, CATALOGUE_NUMBER, ACCESSION_NUMBER};
    }

    private String tableName;
    private String[] columnNames;

// ************** CONSTRUCTOR *******************/

    private WordClassificationExportTable(String tableName, String[] columnNames){
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
