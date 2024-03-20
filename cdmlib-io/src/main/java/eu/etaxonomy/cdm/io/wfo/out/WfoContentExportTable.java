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
 * An enumeration with each instance representing a table type
 * in the WFO Content export format.
 *
 * @author a.mueller
 * @since 2024-01-30
 */
public enum WfoContentExportTable implements ITaxonTreeExportTable {

    CLASSIFICATION("Classification", classificationColumns()),
    REFERENCE("Reference", referenceColumns()),
    DESCRIPTION("Description", descriptionColumns()),
    MEASUREMENT_OR_FACT("Measurement_Or_Fact ", measurementOrFactColumns()),
    VERNACULAR_NAME("Vernacular_Name", vernacularColumns()),
    IMAGES("Images", imageColumns()),
    DISTRIBUTION("Distribution", distributionColumns()),
    ;

    //Classification
    static final String TAXON_ID = "taxonID";
    static final String NAME_SCIENTIFIC_NAME_ID = "scientificNameID";
    static final String NAME_SCIENTIFIC_NAME = "scientificName";
    static final String RANK = "taxonRank";
    static final String TAX_PARENT_ID = "parentNameUsageID";
    static final String NAME_AUTHORSHIP = "scientificNameAuthorship";
    static final String TAX_FAMILY = "family";
    static final String TAX_GENUS = "genus";
    static final String NAME_SPECIFIC_EPITHET = "specificEpithet";
    static final String NAME_INFRASPECIFIC_EPITHET = "infraspecificEpithet ";
    static final String NAME_PUBLISHED_IN = "namePublishedIn";

    //reference
    static final String REF_BIBLIO_CITATION = "bibliographicCitation";
    static final String REF_URI = "URI";

    //Common
    static final String IDENTIFIER = "identifier";
    static final String SOURCE = "source";
    static final String RIGHTS = "rights";
    static final String LICENSE = "license";
    static final String LANGUAGE = "language";
    static final String CREATOR = "creator";
    static final String CREATED = "created";
    static final String CONTRIBUTOR = "contributor";
    static final String AUDIENCE = "audience";
    static final String RIGHTS_HOLDER = "rightsHolder";

    //descriptions
    static final String DESC_TYPE = "type";
    static final String DESC_DESCRIPTION = "description";

    //measurement or fact
    static final String MF_MEASUREMENT_TYPE = "MeasurementType";
    static final String MF_MEASUREMENT_VALUE = "MeasurementValue";

    //vernacular
    static final String CN_VERNACULAR_NAME = "vernacularName";
    static final String CN_COUNTRY_CODE = "countryCode";

    //images
    static final String IMG_TITLE = "title";
    static final String IMG_FORMAT = "format";
    static final String IMG_MODIFIED = "modified";
    static final String IMG_PUBLISHER = "publisher";
    static final String IMG_DESCRIPTION = "description";
    static final String IMG_LOCALITY = "locality";
    static final String IMG_SUBJECT = "subject";
    static final String IMG_LATITUDE = "latitude";
    static final String IMG_LONGITUDE = "longitude";
    static final String IMG_REFERENCES = "references";

    //distribution
    static final String DIST_LOCALITY = "locality";
    static final String DIST_LOCATION_ID = "locationID";
    static final String DIST_COUNTRY_CODE = "countryCode";
    static final String DIST_ESTABLISHMENT_MEANS = "";
    static final String DIST_OCCURRENCE_REMARKS = "occurrenceRemarks";


    private final static String[] classificationColumns(){
        return new String[]{TAXON_ID, NAME_SCIENTIFIC_NAME_ID,
                NAME_SCIENTIFIC_NAME, RANK, TAX_PARENT_ID,
                NAME_AUTHORSHIP, TAX_FAMILY, TAX_GENUS,
                NAME_SPECIFIC_EPITHET,
                NAME_INFRASPECIFIC_EPITHET,
                NAME_PUBLISHED_IN
                };
    }

    private final static String[] referenceColumns(){
        return new String[]{TAXON_ID, IDENTIFIER, REF_BIBLIO_CITATION, REF_URI};
    }

    private final static String[] descriptionColumns(){
        return new String[]{TAXON_ID, DESC_TYPE, LANGUAGE, DESC_DESCRIPTION,
                CONTRIBUTOR, AUDIENCE, RIGHTS_HOLDER, CREATED, CREATOR, SOURCE,
                RIGHTS, LICENSE};
    }

    private final static String[] measurementOrFactColumns(){
        return new String[]{TAXON_ID, MF_MEASUREMENT_TYPE, MF_MEASUREMENT_VALUE,
                SOURCE, RIGHTS, LICENSE};
    }

    private final static String[] vernacularColumns(){
        return new String[]{TAXON_ID, CN_VERNACULAR_NAME, LANGUAGE, CN_COUNTRY_CODE,
                RIGHTS, LICENSE};
    }

    private final static String[] imageColumns(){
        return new String[]{TAXON_ID, IMG_TITLE, IDENTIFIER, RIGHTS, LICENSE, IMG_FORMAT,
                SOURCE, AUDIENCE, CREATOR, CREATED, IMG_MODIFIED, CONTRIBUTOR,
                IMG_PUBLISHER, IMG_DESCRIPTION, IMG_LOCALITY, IMG_SUBJECT, RIGHTS_HOLDER,
                IMG_LATITUDE, IMG_LONGITUDE, IMG_REFERENCES};
    }

    private final static String[] distributionColumns(){
        return new String[]{TAXON_ID, DIST_LOCALITY, DIST_LOCATION_ID, DIST_COUNTRY_CODE,
                DIST_ESTABLISHMENT_MEANS, SOURCE, DIST_OCCURRENCE_REMARKS};
    }


    private String tableName;
    private String[] columnNames;

// ************** CONSTRUCTOR *******************/

    private WfoContentExportTable(String tableName, String[] columnNames){
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