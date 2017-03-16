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
    SYNONYM("Synonym", new String[]{}),
    REFERENCE("Reference", new String[]{}),
    SIMPLE_FACT("SimpleFact", new String[]{}),
    SPECIMEN_FACT("SpecimenFact", new String[]{}),
    GEOGRAPHIC_AREA_FACT("GeographicAreaFact", new String[]{}),
    COMMON_NAME_FACT("CommonNameFact", new String[]{}),
    ;

    /**
     *
     */
    protected static final String NAME_FK = "Name_Fk";
    protected static final String TAXON_ID = "Taxon_ID";
    protected static final String PARENT_FK = "Parent_FK";
    protected static final String SEC_REFERENCE_FK = "SecReference_Fk";
    protected static final String SEC_REFERENCE = "SecReference";

    final static String[] taxonColumns(){
        return new String[]{TAXON_ID, NAME_FK, PARENT_FK, SEC_REFERENCE_FK, SEC_REFERENCE};
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
