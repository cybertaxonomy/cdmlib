/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

/**
 * An enumeration with each instance representing a table type in the Output Model.
 *
 * @author a.mueller
 * @date 15.03.2017
 */
public enum OutputModelTable {
    METADATA("Metadata", metaDataColumns()),
    SCIENTIFIC_NAME("ScientificName", nameColumns()),
    NAME_RELATIONSHIP("NameRelationship",nameRelationColumns()),
    HOMOTYPIC_GROUP("HomotypicGroup",homotypicGroupColumns()),
    NOMENCLATURAL_AUTHOR("NomenclaturalAuthor", nomenclaturalAuthorColumns()),
    NOMENCLATURAL_AUTHOR_TEAM_RELATION("NomenclaturalAuthorTeamRelation", nomenclaturalAuthorTeamRelColumns()),
    TYPE_DESIGNATION("TypeDesignation", new String[]{}),
    SPECIMEN("Specimen", new String[]{}),
    TAXON("Taxon", taxonColumns()),
    SYNONYM("Synonym", synonymColumns()),
    REFERENCE("Reference", referenceColumns()),
    SIMPLE_FACT("SimpleFact", new String[]{}),
    SPECIMEN_FACT("SpecimenFact", new String[]{}),
    GEOGRAPHIC_AREA_FACT("GeographicAreaFact", new String[]{}),
    COMMON_NAME_FACT("CommonNameFact", new String[]{}),
    ;

    //Taxon/Synonym
    protected static final String NAME_FK = "Name_FK";
    protected static final String TAXON_ID = "Taxon_ID";
    protected static final String TAXON_FK = "Taxon_FK";
    protected static final String CLASSIFICATION_ID = "Classification_ID";
    protected static final String CLASSIFICATION_TITLE = "Classification_Name";
    protected static final String SYNONYM_ID = "Synonym_ID";
    protected static final String PARENT_FK = "Parent_FK";
    protected static final String SEC_REFERENCE_FK = "SecReference_FK";
    protected static final String SEC_REFERENCE = "SecReference";

    //Reference
    protected static final String REFERENCE_ID = "Reference_ID";
    protected static final String BIBLIO_SHORT_CITATION = "BibliographicShortCitation";
    protected static final String REF_TITLE = "Title";
    protected static final String DATE_PUBLISHED = "DatePublished";
    //TBC

    //Name
    protected static final String NAME_ID = "Name_ID";
    protected static final String TROPICOS_ID = "Tropicos_ID";
    protected static final String IPNI_ID = "IPNI_ID";
    protected static final String LSID = "LSID";
    protected static final String RANK = "Rank";
    protected static final String RANK_SEQUENCE = "Rank_Sequence";
    protected static final String FULL_NAME_WITH_AUTHORS = "FullNameWithAuthors";
    protected static final String FULL_NAME_NO_AUTHORS = "FullNameNoAuthors";
    protected static final String GENUS_UNINOMIAL = "GenusOrUninomial";
    protected static final String INFRAGENERIC_RANK = "InfragenericRank";
    protected static final String INFRAGENERIC_EPITHET = "InfraGenericEpithet";
    protected static final String SPECIFIC_EPITHET = "SpecificEpithet";
    protected static final String INFRASPECIFIC_RANK = "InfraspecificRank";
    protected static final String INFRASPECIFIC_EPITHET = "InfraSpecificEpithet ";
    protected static final String BAS_EX_AUTHORTEAM_FK = "BasionymExAuthorTeam_Fk";
    protected static final String BAS_AUTHORTEAM_FK = "BasionymAuthorTeam_Fk";
    protected static final String COMB_EX_AUTHORTEAM_FK = "PublishingExAuthorTeam_Fk";
    protected static final String COMB_AUTHORTEAM_FK = "PublishingAuthorTeam_Fk";
    protected static final String AUTHOR_TEAM_STRING = "AuthorTeamString";
    protected static final String PUBLICATION_TYPE = "PublicationType";
    protected static final String ABBREV_TITLE = "AbbreviatedTitle";
    protected static final String FULL_TITLE = "FullTitle";
    protected static final String ABBREV_REF_AUTHOR = "AbbreviatedRefAuthor";
    protected static final String FULL_REF_AUTHOR = "FullRefAuthor";
    protected static final String COLLATION = "Collation";
    protected static final String VOLUME_ISSUE = "Volume_Issue";
    protected static final String DETAIL = "Detail";
    protected static final String YEAR_PUBLISHED = "YearPublished";
    protected static final String TITLE_PAGE_YEAR = "TitlePageYear";
    protected static final String PROTOLOGUE_URI = "ProtologueURI";
    protected static final String NOM_STATUS = "NomenclaturalStatus";
    protected static final String NOM_STATUS_ABBREV = "NomenclaturalStatusAbbreviation";
    protected static final String HOMOTYPIC_GROUP_FK = "HomotypicGroup_Fk";
    protected static final String HOMOTYPIC_GROUP_SEQ = "HomotypicGroupSequenceNumber";


    //Name Relationship
    protected static final String NAME1_FK = "Name1_FK";
    protected static final String NAME2_FK = "Name2_FK";
    protected static final String NAME_REL_TYPE = "NameRelationship_Type";

    //CDM MetaData
    protected static final String INSTANCE_ID = "EditInstance_ID";
    protected static final String INSTANCE_NAME = "EditInstanceName";

    //Homotypic Group
    protected static final String HOMOTYPIC_GROUP_ID = "HomotypicGroup_ID";
    protected static final String HOMOTYPIC_GROUP_STRING = "HomotypicGroupString";
    protected static final String TYPE_STRING = "TypeString";

    //NomenclaturalAuthor
    protected static final String AUTHOR_ID = "Author_ID";
    protected static final String ABBREV_AUTHOR = "AbbrevAuthor";
    protected static final String AUTHOR_TITLE = "AuthorTitle";
    protected static final String AUTHOR_FIRST_NAME = "AuthorFirstName";
    protected static final String AUTHOR_LASTNAME = "AuthorLastName";
    protected static final String AUTHOR_PREFIX = "AuthorPrefix";
    protected static final String AUTHOR_SUFFIX = "AuthorSurfix";

  //Nomenclatural Author AuthorTeam Relations

    protected static final String AUTHOR_FK = "Author_Fk";
    protected static final String AUTHOR_TEAM_FK = "AuthorTeam_Fk";
    protected static final String AUTHOR_TEAM_SEQ_NUMBER = "SequenceNumber";



    final static String[] homotypicGroupColumns(){
        return new String[]{HOMOTYPIC_GROUP_ID, HOMOTYPIC_GROUP_STRING, TYPE_STRING};
    }

    final static String[] nomenclaturalAuthorColumns() {
        return new String[]{AUTHOR_ID, ABBREV_AUTHOR, AUTHOR_TITLE, AUTHOR_FIRST_NAME, AUTHOR_LASTNAME, AUTHOR_PREFIX, AUTHOR_SUFFIX};
    }

    final static String[] nomenclaturalAuthorTeamRelColumns() {
        return new String[]{AUTHOR_TEAM_FK, AUTHOR_FK, AUTHOR_TEAM_SEQ_NUMBER};
    }

    final static String[] metaDataColumns(){
        return new String[]{INSTANCE_ID, INSTANCE_NAME};
    }
    final static String[] nameRelationColumns(){
        return new String[]{NAME1_FK, NAME2_FK, NAME_REL_TYPE};
    }
    final static String[] nameColumns(){
        return new String[]{NAME_ID, LSID, TROPICOS_ID, IPNI_ID, RANK, RANK_SEQUENCE,
                FULL_NAME_WITH_AUTHORS, FULL_NAME_NO_AUTHORS, GENUS_UNINOMIAL,
                INFRAGENERIC_RANK, INFRAGENERIC_EPITHET, SPECIFIC_EPITHET,
                INFRASPECIFIC_RANK, INFRASPECIFIC_EPITHET,
                BAS_EX_AUTHORTEAM_FK, BAS_AUTHORTEAM_FK, COMB_EX_AUTHORTEAM_FK, COMB_AUTHORTEAM_FK,
                AUTHOR_TEAM_STRING, PUBLICATION_TYPE, ABBREV_TITLE, FULL_TITLE,
                ABBREV_REF_AUTHOR, FULL_REF_AUTHOR, COLLATION, VOLUME_ISSUE,
                DETAIL, DATE_PUBLISHED, YEAR_PUBLISHED, TITLE_PAGE_YEAR, PROTOLOGUE_URI,
                NOM_STATUS, NOM_STATUS_ABBREV, HOMOTYPIC_GROUP_FK,
                HOMOTYPIC_GROUP_SEQ
        };
    }
    final static String[] taxonColumns(){
        return new String[]{TAXON_ID, CLASSIFICATION_ID, CLASSIFICATION_TITLE, NAME_FK, PARENT_FK, SEC_REFERENCE_FK, SEC_REFERENCE};
    }
    final static String[] synonymColumns(){
        return new String[]{SYNONYM_ID, TAXON_FK, NAME_FK, SEC_REFERENCE_FK, SEC_REFERENCE};
    }
    final static String[] referenceColumns(){
        return new String[]{REFERENCE_ID, BIBLIO_SHORT_CITATION, REF_TITLE, DATE_PUBLISHED};
    }

    private String tableName;
    private String[] columnNames;

// ************** CONSTRUCTOR *******************/

    private OutputModelTable(String tableName, String[] columnNames){
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

// ****************** GETTER / SETTER *************/

    public String getTableName() {return tableName;}

    public int getSize(){ return columnNames.length;}

    public String[] getColumnNames(){return columnNames;}

    /**
     * @param taxonId
     * @return
     */
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
