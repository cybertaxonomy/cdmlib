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
public enum OutputModelTables {
    METADATA("Metadata"),
    SCIENTIFIC_NAME("ScientificName"),
    NAME_RELATIONSHIP("NameRelationship"),
    HOMOTYPIC_GROUP("HomotypicGroup"),
    NOMENCLATURAL_AUTHOR("NomenclaturalAuthor"),
    NOMENCLATURAL_AUTHOR_TEAM_RELATION("NomenclaturalAuthorTeamRelation"),
    TYPE_DESIGNATION("TypeDesignation"),
    SPECIMEN("Specimen"),
    TAXON("Taxon"),
    SYNONYM("Synonym"),
    REFERENCE("Reference"),
    SIMPLE_FACT("SimpleFact"),
    SPECIMEN_FACT("SpecimenFact"),
    GEOGRAPHIC_AREA_FACT("GeographicAreaFact"),
    COMMON_NAME_FACT("CommonNameFact"),
    ;

    private String tableName;

// ************** CONSTRUCTOR *******************/

    private OutputModelTables(String tableName){
        this.tableName = tableName;
    }

// ****************** GETTER / SETTER *************/

    public String getTableName() {return tableName;}

}
