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
    METADATA("Metadata",new String[]{}),
    SCIENTIFIC_NAME("ScientificName", new String[]{}),
    NAME_RELATIONSHIP("NameRelationship",new String[]{}),
    HOMOTYPIC_GROUP("HomotypicGroup",new String[]{}),
    NOMENCLATURAL_AUTHOR("NomenclaturalAuthor", new String[]{}),
    NOMENCLATURAL_AUTHOR_TEAM_RELATION("NomenclaturalAuthorTeamRelation", new String[]{}),
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
    protected static final String RANK = "Rank";
    protected static final String RANK_SEQUENCE = "Name_ID";
    protected static final String FULL_NAME_WITH_AUTHORS = "FullNameWithAuthors";
    protected static final String FULL_NAME_NO_AUTHORS = "FullNameNoAuthors";
    protected static final String GENUS_UNINOMIAL = "GenusOrUninomial";
    protected static final String INFRAGENERIC_RANK = "InfragenericRank";
    protected static final String INFRAGENERIC_EPITHET = "InfraGenericEpithet";
    protected static final String SPECIFIC_EPITHET = "SpecificEpithet";
    protected static final String INFRASPECIFIC_RANK = "InfraspecificRank";
    protected static final String INFRASPECIFIC_EPITHET = "InfraSpecificEpithet ";
    protected static final String BAS_EX_AUTHORTEAM_FK = "BasionymExAuthorTeam_Fk";
    protected static final String BAS_AUTHORTEAM_FK = "BasionymExAuthorTeam_Fk";
    protected static final String COMB_EX_AUTHORTEAM_FK = "PublishingExAuthorTeam_Fk";
    protected static final String COMB_AUTHORTEAM_FK = "PublishingAuthorTeam_Fk";
    protected static final String AUTHOR_TEAM_STRING = "AuthorTeamString";
    protected static final String PUBLICATION_TYPE = "PublicationType";
    protected static final String ABBREV_TITLE = "AbbreviatedTitle";
    protected static final String FULL_TITLE = "FullTitle";
    protected static final String ABBREV_REF_AUTHOR = "AbbreviatedRefAuthor";
//    protected static final String FULL_REF_AUTHOR = "FullRefAuthor";
//
//    protected static final String FULL_REF_AUTHOR = "FullRefAuthor";
//    protected static final String FULL_REF_AUTHOR = "FullRefAuthor";
//    protected static final String FULL_REF_AUTHOR = "FullRefAuthor";
//    protected static final String FULL_REF_AUTHOR = "FullRefAuthor";
//    protected static final String FULL_REF_AUTHOR = "FullRefAuthor";





    final static String[] nameColumns(){
        return new String[]{NAME_ID, TROPICOS_ID, IPNI_ID, RANK, RANK_SEQUENCE,
                };
    }
    final static String[] taxonColumns(){
        return new String[]{TAXON_ID, NAME_FK, PARENT_FK, SEC_REFERENCE_FK, SEC_REFERENCE};
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
