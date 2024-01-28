/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import eu.etaxonomy.cdm.io.out.ITaxonTreeExportTable;

/**
 * An enumeration with each instance representing a table type in the Output Model.
 *
 * @author a.mueller
 * @since 2023-12-08
 */
public enum WfoExportTable implements ITaxonTreeExportTable {

    CLASSIFICATION("Classification", classificationColumns()),
    REFERENCE("Reference", referenceColumns());

    //Classification
    static final String TAXON_ID = "taxonID";
    static final String NAME_SCIENTIFIC_NAME_ID = "scientificNameID";
    static final String NAME_LOCAL_ID = "localID";
    static final String NAME_SCIENTIFIC_NAME = "scientificName";
    static final String RANK = "taxonRank";
    static final String TAX_PARENT_ID = "parentNameUsageID";
    static final String NAME_AUTHORSHIP = "scientificNameAuthorship";
    static final String TAX_FAMILY = "family";
    static final String TAX_SUBFAMILY = "subfamily";
    static final String TAX_TRIBE = "tribe";
    static final String TAX_SUBTRIBE = "subtribe";
    static final String TAX_GENUS = "genus";
    static final String TAX_SUBGENUS = "subgenus";
    static final String NAME_SPECIFIC_EPITHET = "specificEpithet";
    static final String NAME_INFRASPECIFIC_EPITHET = "infraspecificEpithet ";
    static final String NAME_VERBATIM_RANK = "verbatimTaxonRank ";
    static final String NAME_STATUS = "nomenclaturalStatus";
    static final String NAME_PUBLISHED_IN = "namePublishedIn";
    static final String TAX_STATUS = "nomenclaturalStatus";
    static final String TAX_ACCEPTED_NAME_ID = "acceptedNameUsageID";
    static final String NAME_ORIGINAL_NAME_ID = "originalNameUsageID";
    static final String NAME_ACCORDING_TO_ID = "nameAccordingToID";
    static final String TAXON_REMARKS = "taxonRemarks";
    static final String CREATED = "created";
    static final String MODIFIED = "modified";
    static final String REFERENCES = "references";
    static final String EXCLUDE = "exclude";

    //reference
    static final String IDENTIFIER = "identifier";
    static final String REF_BIBLIO_CITATION = "bibliographicCitation";
    static final String REF_URI = "URI";



    private final static String[] classificationColumns(){
        return new String[]{TAXON_ID, NAME_SCIENTIFIC_NAME_ID, NAME_LOCAL_ID,
                NAME_SCIENTIFIC_NAME, RANK, TAX_PARENT_ID,
                NAME_AUTHORSHIP, TAX_FAMILY, TAX_SUBFAMILY, TAX_TRIBE, TAX_SUBTRIBE, TAX_GENUS,
                TAX_SUBGENUS, NAME_SPECIFIC_EPITHET,
                NAME_INFRASPECIFIC_EPITHET, NAME_VERBATIM_RANK, NAME_STATUS,
                NAME_PUBLISHED_IN, TAX_STATUS, TAX_ACCEPTED_NAME_ID,
                NAME_ORIGINAL_NAME_ID, NAME_ACCORDING_TO_ID, TAXON_REMARKS,
                CREATED, MODIFIED, REFERENCES, EXCLUDE};
    }

    private final static String[] referenceColumns(){
        return new String[]{IDENTIFIER, REF_BIBLIO_CITATION, REF_URI};
    }

    private String tableName;
    private String[] columnNames;

// ************** CONSTRUCTOR *******************/

    private WfoExportTable(String tableName, String[] columnNames){
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