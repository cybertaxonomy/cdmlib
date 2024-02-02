/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.coldp;

import eu.etaxonomy.cdm.io.out.ITaxonTreeExportTable;

/**
 * An enumeration with each instance representing a table type in the Output Model.
 *
 * @author a.mueller
 * @since 2023-07-17
 */
public enum ColDpExportTable implements ITaxonTreeExportTable {

    NAME_RELATION("NameRelation",nameRelationColumns()),
    NAME("Name", nameColumns()),
    NAME_WITH_FULLNAME("Name2", nameWithFullNameColumns()),
    TAXON("Taxon", taxonColumns()),
    SYNONYM("Synonym", synonymColumns()),
    REFERENCE("Reference", referenceColumns()),

    TYPE_MATERIAL("TypeMaterial", typeMaterialColumns()),
    TREATMENT("Treatment", treatmentColumns()),
    SPECIES_INTERACTION("SpeciesInteraction", speciesInteractionColumns()),
    TAXON_CONCEPT_RELATION("TaxonConceptRelation", taxonConceptRelationColumns()),
    SPECIES_ESTIMATE("SpeciesEstimate", speciesEstimateColumns()),
    MEDIA("Media", mediaColumns()),
    DISTRIBUTION("Distribution", distributionColumns()),
    VERNACULAR_NAME("VernacularName", vernacularNameColumns()),
    ;

    //Common
    static final String ID = "ID";
    static final String TAXON_ID = "taxonID";
    static final String TYPE = "type";
    static final String ALTERNATIVE_ID = "alternativeID";
    static final String SOURCE_ID = "sourceID";
    static final String REMARKS = "remarks";
    static final String REFERENCE_ID = "referenceID";
    static final String LINK = "link";

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


    //Synonym
    static final String SYN_TAXON_STATUS = "status";

    //Taxon
    static final String TAX_PARENT_ID = "parentID";
    static final String TAX_SEQ_INDEX = "sequenceIndex";
    static final String TAX_BRANCH_LENGTH = "branchLength";
    static final String TAX_NAMEPHRASE = "namePhrase";
    static final String TAX_NAME_ID = "nameID";
    static final String TAX_SEC_ID = "accordingToID";
    static final String TAX_SCRUTINIZER = "scrutinizer";
    static final String TAX_SCRUTINIZER_ID = "scrutinizerID";
    static final String TAX_SCRUTINIZER_DATE = "scrutinizerDate";
    static final String TAX_PROVISIONAL = "provisional";
    static final String TAX_EXTINCT = "extinct";
    static final String TAX_TEMPORAL_RANGE_START = "temporalRangeStart";
    static final String TAX_TEMPORAL_RANGE_END = "temporalRangeEnd";
    static final String TAX_ENVIRONMENT = "environment";
    static final String TAX_SPECIES = "species";
    static final String TAX_SECTION = "section";
    static final String TAX_SUBGENUS = "subgenus";
    static final String TAX_GENUS = "genus";
    static final String TAX_SUBTRIBE = "subtribe";
    static final String TAX_TRIBE = "tribe";
    static final String TAX_SUBFAMILY = "subfamily";
    static final String TAX_FAMILY = "family";
    static final String TAX_SUPERFAMILY = "superfamily";
    static final String TAX_SUBORDER = "suborder";
    static final String TAX_ORDER = "order";
    static final String TAX_SUBCLASS = "subclass";
    static final String TAX_CLASS = "class";
    static final String TAX_SUBPHYLUM = "subphylum";
    static final String TAX_PHYLUM = "phylum";
    static final String TAX_KINGDOM = "kingdom";

    //Taxon ConceptRelation / Species Interaction
    static final String REL_TAXON_TAXON_ID = "relatedTaxonID";
    static final String REL_TAXON_SCIENTIFIC_NAME = "relatedTaxonScientificName";

    //Species estimate
    static final String SPEC_ESTI_ESTIMATE = "estimate";

    //Media
    static final String MEDIA_URL = "url";
    static final String MEDIA_FORMAT = "format";
    static final String MEDIA_TITLE = "title";
    static final String MEDIA_CREATED = "created";
    static final String MEDIA_CREATOR = "creator";
    static final String MEDIA_LICENSE = "license";

    //Reference
    static final String REF_CITATION = "citation";
    static final String REF_TYPE = "type";
    static final String REF_AUTHOR = "author";
    static final String REF_EDITOR = "editor";
    static final String REF_TITLE = "title";
    static final String REF_CONTAINER_AUTHOR = "containerAuthor";
    static final String REF_CONTAINER_TITLE = "containerTitle";
    static final String REF_ISSUED = "issued";
    static final String REF_ACCESSED = "accessed";
    static final String REF_COLLECTION_TITLE = "collectionTitle";
    static final String REF_COLLECTION_EDITOR = "collectionEditor";
    static final String REF_VOLUME = "volume";
    static final String REF_ISSUE = "issue";
    static final String REF_EDITION = "edition";
    static final String REF_PAGE = "page";
    static final String REF_PUBLISHER = "publisher";
    static final String REF_PUBLISHER_PLACE = "publisherPlace";
    static final String REF_VERSION = "version";
    static final String REF_ISBN = "isbn";
    static final String REF_ISSN = "issn";
    static final String REF_DOI = "doi";

    //Name
    static final String NAME_BASIONYM_ID = "basionymID";
    static final String RANK = "rank";
    static final String NAME_SCIENTIFIC_NAME = "scientificName";
    static final String NAME_FULLNAME = "fullName";

    static final String NAME_AUTHORSHIP = "authorship";
    static final String NAME_COMBINATION_AUTHORSHIP = "combinationAuthorship";
    static final String NAME_COMBINATION_EX_AUTHORSHIP = "combinationExAuthorshipYear";
    static final String NAME_COMBINATION_AUTHORSHIP_YEAR = "combinationAuthorshipYear";
    static final String NAME_BASIONYM_AUTHORSHIP = "basionymAuthorship";
    static final String NAME_BASIONYM_EX_AUTHORSHIP = "basionymExAuthorshipYear";
    static final String NAME_BASIONYM_AUTHORSHIP_YEAR = "basionymAuthorshipYear";
    static final String NAME_UNINOMIAL = "uninomial";
    static final String NAME_GENUS = "genus";
    static final String NAME_INFRAGENERIC_EPITHET = "infragenericEpithet";
    static final String NAME_SPECIFIC_EPITHET = "specificEpithet";
    static final String NAME_INFRASPECIFIC_EPITHET = "infraspecificEpithet ";
    static final String NAME_CULTIVAR_EPITHET = "cultivarEpithet";
    static final String NAME_CODE = "code";
    static final String NAME_STATUS = "status";
    static final String NAME_PUBLISHED_IN_YEAR = "publishedInYear";
    static final String NAME_PUBLISHED_IN_PAGE = "publishedInPage";
    static final String NAME_PUBLISHED_IN_PAGE_LINK = "publishedInPageLink";

    //Name Relationship
    static final String REL_NAME_NAMEID = "nameID";
    static final String REL_NAME_REL_NAMEID = "relatedNameID";

    //Type Material
    static final String TYPE_NAMEID = "nameID";
    static final String TYPE_CITATION = "citation";
    static final String TYPE_STATUS = "status";
    static final String TYPE_INSTITUTION_CODE = "institutionCode";
    static final String TYPE_CATALOG_NUMBER = "catalogNumber";
    static final String TYPE_LOCALITY = "locality";
    static final String TYPE_COUNTRY = "country";
    static final String TYPE_LATITUDE = "latitude";
    static final String TYPE_LONGITUDE = "longitude";
    static final String TYPE_ALTITUDE = "altitude";
    static final String TYPE_HOST = "host";
    static final String TYPE_SEX = "sex";
    static final String TYPE_DATE = "date";
    static final String TYPE_COLLECTOR = "collector";
    static final String TYPE_ASSOC_SEQ = "associatedSequences";

    //Distribution
    static final String DIST_AREA_ID = "areaID";
    static final String DIST_AREA = "area";
    static final String DIST_GAZETTEER = "gazetteer";
    static final String DIST_STATUS = "status";

    //Vernacular Name
    static final String VERN_NAME = "name";
    static final String VERN_TRANSLITERATION = "transliteration";
    static final String VERN_LANGUAGE = "language";
    static final String VERN_COUNTRY = "country";
    static final String VERN_AREA = "area";
    static final String VERN_SEX = "sex";

    //Treatment
    static final String TREAT_DOCUMENT = "document";
    static final String TREAT_FORMAT = "format";


    private final static String[] nameRelationColumns(){
        return new String[]{REL_NAME_NAMEID, REL_NAME_REL_NAMEID, SOURCE_ID, TYPE, REFERENCE_ID, REMARKS};
    }

    private final static String[] nameColumns(){
        return new String[]{ID, ALTERNATIVE_ID, SOURCE_ID, NAME_BASIONYM_ID,
                NAME_SCIENTIFIC_NAME, NAME_AUTHORSHIP, RANK,
                NAME_UNINOMIAL, NAME_GENUS, NAME_INFRAGENERIC_EPITHET, NAME_SPECIFIC_EPITHET,
                NAME_INFRASPECIFIC_EPITHET, NAME_CULTIVAR_EPITHET,
                //TODO notho, originalSpelling
                NAME_COMBINATION_AUTHORSHIP, NAME_COMBINATION_EX_AUTHORSHIP, NAME_COMBINATION_AUTHORSHIP_YEAR,
                NAME_BASIONYM_AUTHORSHIP, NAME_BASIONYM_EX_AUTHORSHIP, NAME_BASIONYM_AUTHORSHIP_YEAR,
                NAME_CODE, NAME_STATUS, REFERENCE_ID,
                NAME_PUBLISHED_IN_YEAR, NAME_PUBLISHED_IN_PAGE, NAME_PUBLISHED_IN_PAGE_LINK,
                LINK, REMARKS
        };
    }

    private final static String[] nameWithFullNameColumns(){
        return new String[]{ID, ALTERNATIVE_ID, SOURCE_ID, NAME_BASIONYM_ID,
                NAME_SCIENTIFIC_NAME,
                NAME_FULLNAME, NAME_AUTHORSHIP, RANK,
                NAME_UNINOMIAL, NAME_GENUS, NAME_INFRAGENERIC_EPITHET, NAME_SPECIFIC_EPITHET,
                NAME_INFRASPECIFIC_EPITHET, NAME_CULTIVAR_EPITHET,
                //TODO notho, originalSpelling
                NAME_COMBINATION_AUTHORSHIP, NAME_COMBINATION_EX_AUTHORSHIP, NAME_COMBINATION_AUTHORSHIP_YEAR,
                NAME_BASIONYM_AUTHORSHIP, NAME_BASIONYM_EX_AUTHORSHIP, NAME_BASIONYM_AUTHORSHIP_YEAR,
                NAME_CODE, NAME_STATUS, REFERENCE_ID,
                NAME_PUBLISHED_IN_YEAR, NAME_PUBLISHED_IN_PAGE, NAME_PUBLISHED_IN_PAGE_LINK,
                LINK, REMARKS
        };
    }

    private final static String[] typeMaterialColumns() {
        return new String[]{ID, SOURCE_ID, TYPE_NAMEID, TYPE_CITATION,
                TYPE_STATUS, TYPE_INSTITUTION_CODE, TYPE_CATALOG_NUMBER,
                REFERENCE_ID, TYPE_LOCALITY, TYPE_COUNTRY,
                TYPE_LATITUDE, TYPE_LONGITUDE, TYPE_ALTITUDE, TYPE_HOST,
                TYPE_SEX, TYPE_DATE, TYPE_COLLECTOR, TYPE_ASSOC_SEQ,
                LINK, REMARKS};
    }

    private final static String[] synonymColumns(){
        return new String[]{ID, SOURCE_ID, TAXON_ID, TAX_NAME_ID,
                TAX_NAMEPHRASE, TAX_SEC_ID, SYN_TAXON_STATUS,
                REFERENCE_ID, LINK, REMARKS};
    }

    private final static String[] taxonColumns(){
        return new String[]{ID, ALTERNATIVE_ID, SOURCE_ID, TAX_PARENT_ID,
                TAX_SEQ_INDEX, TAX_BRANCH_LENGTH, TAX_NAME_ID, TAX_NAMEPHRASE,
                TAX_SEC_ID, TAX_SCRUTINIZER, TAX_SCRUTINIZER_ID, TAX_SCRUTINIZER_DATE,
                TAX_PROVISIONAL, REFERENCE_ID, TAX_EXTINCT,
                TAX_TEMPORAL_RANGE_START, TAX_TEMPORAL_RANGE_END, TAX_ENVIRONMENT,
                TAX_SPECIES, TAX_SECTION, TAX_SUBGENUS, TAX_GENUS, TAX_SUBTRIBE,
                TAX_TRIBE, TAX_SUBFAMILY, TAX_FAMILY, TAX_SUPERFAMILY,
                TAX_SUBORDER, TAX_ORDER, TAX_SUBCLASS, TAX_CLASS,
                TAX_SUBPHYLUM, TAX_PHYLUM, TAX_KINGDOM,
                LINK, REMARKS};
    }

    private final static String[] referenceColumns(){
        return new String[]{ID, ALTERNATIVE_ID, SOURCE_ID, REF_CITATION,
                TYPE, REF_AUTHOR, REF_EDITOR, REF_TITLE, REF_CONTAINER_AUTHOR,
                REF_CONTAINER_TITLE, REF_ISSUED, REF_ACCESSED,
                REF_COLLECTION_TITLE, REF_COLLECTION_EDITOR, REF_VOLUME,
                REF_ISSUE, REF_EDITION, REF_PAGE, REF_PUBLISHER, REF_PUBLISHER_PLACE,
                REF_VERSION, REF_ISBN, REF_ISSN, REF_DOI, LINK, REMARKS};
    }

    private final static String[] treatmentColumns() {
        return new String[]{TAXON_ID, SOURCE_ID,
                TREAT_DOCUMENT, TREAT_FORMAT};
    }

    private final static String[] speciesInteractionColumns() {
        return new String[]{TAXON_ID, REL_TAXON_TAXON_ID, SOURCE_ID,
                REL_TAXON_SCIENTIFIC_NAME, TYPE, REFERENCE_ID,
                REMARKS};
    }


    private final static String[] taxonConceptRelationColumns() {
        return new String[]{TAXON_ID, REL_TAXON_TAXON_ID, SOURCE_ID,
                TYPE, REFERENCE_ID, REMARKS};
    }

    private final static String[] speciesEstimateColumns() {
        return new String[]{TAXON_ID, SOURCE_ID, SPEC_ESTI_ESTIMATE,
                TYPE, REFERENCE_ID, REMARKS};
    }

    private final static String[] mediaColumns() {
        return new String[]{TAXON_ID, SOURCE_ID, MEDIA_URL,
                TYPE, MEDIA_FORMAT, MEDIA_TITLE, MEDIA_CREATED,
                MEDIA_CREATOR, MEDIA_LICENSE, LINK};
    }

    //FIXME
    private final static String[] metaDataColumns(){
        return new String[]{INSTANCE_ID, INSTANCE_NAME, DATASET_TITLE, DATASET_CONTRIBUTOR, DATASET_CREATOR, DATASET_DESCRIPTION,
                DATASET_DOWNLOAD_LINK, DATASET_BASE_URL, DATASET_KEYWORDS, DATASET_LANDINGPAGE, DATASET_LANGUAGE, DATASET_LICENCE,
                DATASET_LOCATION, DATASET_RECOMMENDED_CITATTION};
    }


    private final static String[] distributionColumns() {
        return new String[]{TAXON_ID, SOURCE_ID, DIST_AREA_ID, DIST_AREA,
                DIST_GAZETTEER, DIST_STATUS, REFERENCE_ID, REMARKS};
    }

    private final static String[] vernacularNameColumns() {
        return new String[]{TAXON_ID, SOURCE_ID, VERN_NAME, VERN_TRANSLITERATION,
                VERN_LANGUAGE, VERN_COUNTRY, VERN_AREA, VERN_SEX, REFERENCE_ID,};
    }

    private String tableName;
    private String[] columnNames;

// ************** CONSTRUCTOR *******************/

    private ColDpExportTable(String tableName, String[] columnNames){
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

// ****************** GETTER / SETTER *************/

    @Override
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