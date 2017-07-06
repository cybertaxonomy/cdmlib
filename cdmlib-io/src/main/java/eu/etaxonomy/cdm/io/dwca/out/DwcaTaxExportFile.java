/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import eu.etaxonomy.cdm.io.dwca.TermUri;

/**
 * @author a.mueller
 * @date 25.06.2017
 *
 */
public enum DwcaTaxExportFile {
      METADATA(DwcaMetaDataExport.fileName, null),
      EML(DwcaEmlExport.fileName, null),
      TAXON(DwcaTaxonExport.fileName, taxonColumns()),
      DISTRIBUTION(DwcaDistributionExport.fileName, distributionColumns()),
      DESCRIPTION(DwcaDescriptionExport.fileName, null),
      IMAGE(DwcaImageExport.fileName, null),
      REFERENCE(DwcaReferenceExport.fileName, null),
      RESOURCE_RELATION(DwcaResourceRelationExport.fileName, null),
      TYPES(DwcaTypesExport.fileName, null),
      VERNACULAR(DwcaVernacularExport.fileName, null),
      REPORT("_exportReport.txt", null),


//    SCIENTIFIC_NAME("ScientificName", nameColumns()),
//    NAME_RELATIONSHIP("NameRelationship",nameRelationColumns()),


    ;


    final static TermUri[] taxonColumns(){
        return new TermUri[]{TermUri.DC_IDENTIFIER, TermUri.DWC_SCIENTIFIC_NAME_ID, TermUri.DWC_ACCEPTED_NAME_USAGE_ID,
            TermUri.DWC_SCIENTIFIC_NAME, TermUri.DWC_TAXON_RANK, TermUri.DWC_TAXONOMIC_STATUS, TermUri.DWC_ORIGINAL_NAME_USAGE_ID,
            TermUri.DWC_NAME_ACCORDING_TO_ID, TermUri.DWC_NAME_ACCORDING_TO_ID, TermUri.DWC_NAME_PUBLISHED_IN_ID,
            TermUri.DWC_TAXON_CONCEPT_ID, TermUri.DWC_ACCEPTED_NAME_USAGE, TermUri.DWC_PARENT_NAME_USAGE,
            TermUri.DWC_ORIGINAL_NAME_USAGE,
            TermUri.DWC_NAME_ACCORDING_TO, TermUri.DWC_NAME_PUBLISHED_IN,
            TermUri.DWC_HIGHER_CLASSIFICATION, TermUri.DWC_KINGDOM, TermUri.DWC_PHYLUM, TermUri.DWC_CLASS,
            TermUri.DWC_ORDER,  TermUri.DWC_FAMILY, TermUri.DWC_GENUS, TermUri.DWC_SUBGENUS,
            TermUri.TDWG_UNINOMIAL, TermUri.TDWG_GENUSPART, TermUri.TDWG_INFRAGENERICEPITHET,
            TermUri.DWC_SPECIFIC_EPI, TermUri.DWC_INFRA_SPECIFIC_EPI,
            TermUri.DWC_VERBATIM_TAXON_RANK, TermUri.DWC_VERNACULAR_NAME, TermUri.DWC_NOMENCLATURAL_CODE,
            TermUri.DWC_NOMENCLATURAL_STATUS, TermUri.DWC_TAXON_REMARKS, TermUri.DC_MODIFIED,
            TermUri.DC_LANGUAGE, TermUri.DC_RIGHTS, TermUri.DC_RIGHTS_HOLDER, TermUri.DC_ACCESS_RIGHTS,
            TermUri.DC_BIBLIOGRAPHIC_CITATION, TermUri.DWC_INFORMATION_WITHHELD, TermUri.DWC_DATASET_NAME,
            TermUri.DC_SOURCE};
    }


    /**
     * @return
     */
    final static TermUri[] distributionColumns() {
        return new TermUri[]{
                TermUri.DC_IDENTIFIER,  //Fixme this should be the FK to taxon
                TermUri.DWC_LOCATION_ID, TermUri.DWC_LOCALITY,
                TermUri.DWC_COUNTRY_CODE, TermUri.DWC_LIFESTAGE,
                TermUri.IUCN_THREAD_STATUS, TermUri.DWC_OCCURRENCE_STATUS,
                TermUri.DWC_ESTABLISHMENT_MEANS, TermUri.GBIF_APPENDIX_CITES,
                TermUri.DWC_EVENT_DATE,
                TermUri.DWC_START_DAY_OF_YEAR, TermUri.DWC_END_DAY_OF_YEAR,
                TermUri.DC_SOURCE, TermUri.DWC_OCCURRENCE_REMARKS,
        };
    }


    private String tableName;
    private TermUri[] columnNames;

// ************** CONSTRUCTOR *******************/

    private DwcaTaxExportFile(String tableName, TermUri[] columnNames){
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

// ****************** GETTER / SETTER *************/

    public String getTableName() {return tableName;}

    public int getSize(){ return columnNames.length;}

    public TermUri[] getColumnNames(){return columnNames;}

    public String[] getColumnNamesString(){
        String[] result = new String[columnNames.length];
        for (int i= 0; i < columnNames.length; i++){
            result[i] = columnNames[i].toString();
        }

        return result;
    }


    /**
     * @param taxonId
     * @return
     */
    public int getIndex(TermUri columnName) {
        int index= 0;
        for(TermUri column : getColumnNames()){
            if (column.equals(columnName)){
                return index;
            }
            index++;
        }
        return -1;
    }
}
