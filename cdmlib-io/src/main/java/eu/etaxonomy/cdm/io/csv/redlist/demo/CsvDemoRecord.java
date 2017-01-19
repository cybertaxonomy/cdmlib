/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.csv.redlist.demo;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.oppermann
 * @date 18.10.2012
 *
 */
public class CsvDemoRecord extends CsvDemoRecordBase{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CsvDemoRecord.class);

    private String scientificNameId;
    private String scientificName;
    private String taxonomicStatus;
    private final CsvDemoId datasetId;
    private String taxonConceptID;
    private String datasetName;
    private ArrayList<String> synonyms;
    private String threadStatus;
    private ArrayList<String> countryCodes;
    private ArrayList<String> headlines;
    private Boolean isHeadLinePrinted;
    private List<Feature> features;
    private List<List<String>> featuresCells;
    private final CsvDemoExportConfigurator config;
    private String authorshipCache;
    private String rank;
    private String parentUuid;
    private String lastUpdated;
    private String externalID;

    /**
     *
     * @param metaDataRecord
     * @param config
     */
    public CsvDemoRecord(CsvDemoMetaDataRecord metaDataRecord, CsvDemoExportConfigurator config){
        super(metaDataRecord, config);
        this.config = config;
        datasetId = new CsvDemoId(config);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.dwca.out.DwcaRecordBase#registerKnownFields()
     */
    @Override
    protected void registerKnownFields(){
        try {
            addKnownField(TermUri.DWC_SCIENTIFIC_NAME);
            addKnownField(TermUri.DWC_TAXONOMIC_STATUS);
            addKnownField(TermUri.DWC_DATASET_NAME);
            addKnownField("countryCode", "http://rs.tdwg.org/dwc/terms/countryCode");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(PrintWriter writer) {
        if(isHeadLinePrinted() != null && isHeadLinePrinted() == true){
            printHeadline(writer, setHeadlines(), TermUri.DWC_DATASET_NAME);
            isHeadLinePrinted=false;
        }
        if(config.isClassification()){
            print(datasetName, writer, IS_FIRST, TermUri.DWC_DATASET_NAME);
        }
        if(config.isDoTaxonConceptExport()){
            if(config.isTaxonName()){
                print(scientificName, writer, IS_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
            }
        }else{
            if(config.isTaxonName()){
                print(scientificName, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
            }
        }
        if(config.isTaxonNameID()){
            print(scientificNameId, writer, IS_NOT_FIRST, TermUri.DWC_DATASET_ID);
        }
        if(config.isAuthor()){
            print(authorshipCache, writer, IS_NOT_FIRST, TermUri.DC_CREATOR);
        }
        if(config.isRank()){
            print(rank, writer, IS_NOT_FIRST, TermUri.DWC_TAXON_RANK);
        }
        if(config.isTaxonConceptID()){
            print(taxonConceptID, writer, IS_NOT_FIRST, TermUri.DWC_TAXON_CONCEPT_ID);
        }
        if(config.isTaxonStatus()){
            print(taxonomicStatus, writer, IS_NOT_FIRST, TermUri.DWC_TAXONOMIC_STATUS);
        }
        if(config.isAcceptedName()){
            //TODO print accepted Name
        }
        if(config.isParentID()){
            print(parentUuid, writer, IS_NOT_FIRST, TermUri.DWC_PARENT_NAME_USAGE_ID);
        }
        if(config.isSynonyms()){
            print(synonyms, TermUri.DWC_SCIENTIFIC_NAME, writer);
        }
        if(config.isDistributions()){
            print(countryCodes, TermUri.DWC_COUNTRY_CODE, writer);
        }
        if(config.isRedlistFeatures()){
            if(features != null ||featuresCells != null || !featuresCells.isEmpty()){
                for(List<String> featureList : featuresCells) {
                    print((ArrayList<String>)featureList, TermUri.DWC_LIFESTAGE, writer);
                }
            }
        }
        if(config.isExternalID()){
            print(externalID, writer, IS_NOT_FIRST,TermUri.DWC_ORIGINAL_NAME_USAGE_ID);
        }
        if(config.isLastChange()){
            print(lastUpdated, writer, IS_NOT_FIRST,TermUri.DC_MODIFIED);
        }
        writer.println();
    }


    //--------------Getter-Setter-Methods------------------//

/*    public String getDatasetName() {
        return datasetName;
    }*/
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }
    public String getScientificName() {
        return scientificName;
    }
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
/*    public String getTaxonomicStatus() {
        return taxonomicStatus;
    }*/
    public void setTaxonomicStatus(String taxonomicStatus) {
        this.taxonomicStatus = taxonomicStatus;
    }
/*    public String getDatasetId() {
        return datasetId.getId();
    }*/
    public void setDatasetId(Classification classification) {
        this.datasetId.setId(classification);
    }
    public void setSynonyms(ArrayList<String> synonyms){
        this.synonyms = synonyms;
    }
/*    public ArrayList<String> getSynonyms(){
        return synonyms;
    }*/
/*    public String getThreadStatus() {
        return threadStatus;
    }*/
    public void setThreadStatus(String threadStatus) {
        this.threadStatus = threadStatus;
    }

    public void setCountryCode(ArrayList<String> countryCodes) {
        this.countryCodes = countryCodes;
    }

    //FIXME: hard coded header lines
    private ArrayList<String> setHeadlines(){
//      if(config.isDoDemoExport()){
            headlines = new ArrayList<String>();

            if(config.isClassification()){
                headlines.add("Classification");
            }
            if(config.isTaxonName()){
                headlines.add("Wissenschaftlicher Name");
            }
            if(config.isTaxonNameID()){
                headlines.add("Taxon ID");
            }
            if(config.isAuthor()){
                headlines.add("Autor");
            }
            if(config.isRank()){
                headlines.add("Rang");
            }
            if(config.isTaxonStatus()){
                headlines.add("Taxon Status");
            }
            if(config.isAcceptedName()){
                headlines.add("Akzeptierter Name");
            }
            if(config.isTaxonConceptID()){
                headlines.add("Taxon Konzept ID");
            }
            if(config.isParentID()){
                headlines.add("Parent ID");
            }
            if(config.isSynonyms()){
                headlines.add("Synonym");
            }
            if(config.isDistributions()){
                headlines.add("Distribution");
            }
            if(features != null){
                if(!features.isEmpty()){
                    for(Feature f : features) {
                        headlines.add(f.getLabel());
                    }
                }
            }
            if(config.isExternalID()){
                headlines.add("External ID");
            }
            if(config.isLastChange()){
                headlines.add("Letztes Update");
            }
//      }else if(config.isDoTaxonConceptExport()){
//          headlines = new ArrayList<String>();
//          Collections.addAll(headlines, "Taxon Name UUID","Taxon Name","Author","Rank","Taxon Status","Accepted Name", "Taxon Concept ID", "Parent ID", "Last Change");
//      }
        return headlines;
    }
    @JsonIgnore
    public Boolean isHeadLinePrinted() {
        return isHeadLinePrinted;
    }

    public void setHeadLinePrinted(Boolean isHeadLinePrinted) {
        this.isHeadLinePrinted = isHeadLinePrinted;
    }

    public void setScientificNameId(String scientificNameId) {
        this.scientificNameId = scientificNameId;
    }

    public void setPrintFeatures(List<Feature> features) {
        this.features = features;

    }

    public void setFeatures(List<List<String>> featureCells) {
        this.featuresCells = featureCells;

    }

    public void setAuthorName(String authorshipCache) {
        this.authorshipCache = authorshipCache;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setParentUUID(String parentUuid) {
        this.parentUuid = parentUuid;

    }

    public void setLastChange(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setTaxonConceptID(String taxonConceptID) {
        this.taxonConceptID = taxonConceptID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }
       /**
     * @return the scientificNameId
     */
/*    public String getScientificNameId() {
        return scientificNameId;
    }*/

    /**
     * @return the taxonConceptID
     */
    public String getTaxonConceptID() {
        return taxonConceptID;
    }
/*
    public ArrayList<String> getCountryCodes() {
        return countryCodes;
    }
*/
    /**
     * @return the authorshipCache
     */
    public String getAuthor() {
        return authorshipCache;
    }

    /**
     * @return the rank
     */
    public String getRank() {
        return rank;
    }

    /**
     * @return the parentUuid
     */
    public String getParentUuid() {
        return parentUuid;
    }

    /**
     * @return the lastUpdated
     */
    public String getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @return the externalID
     */
    public String getExternalID() {
        return externalID;
    }

}
