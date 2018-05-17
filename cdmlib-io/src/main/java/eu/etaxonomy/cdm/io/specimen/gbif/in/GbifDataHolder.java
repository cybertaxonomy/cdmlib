/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.gbif.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.ext.occurrence.gbif.GbifResponse;
import eu.etaxonomy.cdm.io.specimen.SpecimenDataHolder;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206DataHolder;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author k.luther
 * @since 15.07.2016
 *
 */
public class GbifDataHolder extends SpecimenDataHolder{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Abcd206DataHolder.class);

    //per import
    protected List<SpecimenTypeDesignationStatus> statusList;
    protected List<String> knownGbifElements = new ArrayList<String>();
    protected HashMap<String,String> allGbifElements = new HashMap<String,String>();
    public List<String> gatheringAgentList;
    protected List<String> gatheringTeamList;
    private GbifResponse response;

    /*per unit
    protected List<Identification> identificationList;
    private List<HashMap<String, String>> atomisedIdentificationList;
    protected Map<String, String> namedAreaList;
    protected List<String[]> referenceList;
    protected List<String> multimediaObjects;
    protected List<String> docSources;
    protected List<String> associatedUnitIds;

    private String nomenclatureCode;
    protected String institutionCode;
    protected String collectionCode;
    protected String unitID;
    protected String recordBasis;
    protected String kindOfUnit;
    protected String accessionNumber;
    protected String fieldNumber;
    protected Double longitude;
    protected Double latitude;
    protected String locality;
    protected String languageIso;
    protected String country;
    protected String isocountry;
    protected Integer depth;
    protected Integer altitude;
    protected String unitNotes;
    protected String gatheringNotes;
    protected String gatheringDateText;
    protected String gatheringElevationText;
    protected String gatheringElevation;
    protected String gatheringElevationMax;
    protected String gatheringElevationMin;
    protected String gatheringElevationUnit;
    protected String gatheringSpatialDatum;
    protected String gatheringCoordinateErrorMethod;
    */


    protected String key;
    protected String datasetKey;
    protected String publishingOrgKey;
    protected String publishingCountry;
    protected String protocol;
    protected String lastCrawled;
    protected String lastParsed;
    protected String extensions;
    protected String basisOfRecord;
    protected String individualCount;
    protected String taxonKey;
    protected String kingdomKey;
    protected String phylumKey;
    protected String classKey;
    protected String orderKey;
    protected String familyKey;
    protected String genusKey;
    protected String speciesKey;
    protected String scientificName;
    protected String kingdom;
    protected String phylum;
    protected String order;
    protected String family;
    protected String genus;
    protected String species;
    protected String genericName;
    protected String specificEpithet;
    protected String taxonRank;
    protected String dateIdentified;
    protected Double decimalLongitude;
    protected Double decimalLatitude;
    protected String elevation;
    protected String continent;
    protected String stateProvince;
    protected String year;
    protected String month;
    protected String day;
    protected String eventDate;
    protected String[] issues;
   protected String lastInterpreted;
   protected String identifiers;
   protected String[] facts;
   protected String[] relations;
   protected String geodeticDatum;
   protected String className;
   protected String countryCode;
   protected String country;
   protected String nomenclaturalStatus;
   protected String rightsHolder;
   protected String identifier;
   protected String recordNumber;
   protected String nomenclaturalCode;
   protected String county;
   protected String locality;
   protected String datasetName;
   protected String gbifID;
   protected String collectionCode;
   protected String occurrenceID;
   protected String type;
   protected String taxonID;
   protected String license;
   protected String catalogNumber;
   protected String recordedBy;
    protected String institutionCode;
    protected String ownerInstitutionCode;
    protected String bibliographicCitation;
    protected String identifiedBy;
    protected String collectionID;



    @Override
    public void reset() {

        key = null;
        datasetKey = null;
        publishingOrgKey = null;
        publishingCountry = null;
        protocol = null;
        lastCrawled = null;
        lastParsed = null;
        extensions = null;
        basisOfRecord = null;
        individualCount = null;
        taxonKey = null;
        kingdomKey = null;
        phylumKey = null;
        classKey = null;
        orderKey = null;
        familyKey = null;
        genusKey = null;
        speciesKey = null;
        scientificName = null;
        kingdom = null;
        phylum = null;
        order = null;
        family = null;
        genus = null;
        species = null;
        genericName = null;
        specificEpithet = null;
        taxonRank = null;
        dateIdentified = null;
        decimalLongitude = null;
        decimalLatitude = null;
        elevation = null;
        continent = null;
        stateProvince = null;
        year = null;
        month = null;
        day = null;
        eventDate = null;
        issues = null;
       lastInterpreted = null;
       identifiers = null;
       facts =  null;
       relations = null;
       geodeticDatum = null;
       className = null;
       countryCode = null;
       country = null;
       nomenclaturalStatus = null;
       rightsHolder = null;
       identifier = null;
       recordNumber = null;
       nomenclaturalCode = null;
       county = null;
       locality = null;
       datasetName = null;
       gbifID = null;
       collectionCode = null;
       occurrenceID = null;
       type = null;
       taxonID = null;
       license = null;
       catalogNumber = null;
       recordedBy = null;
       institutionCode = null;
       ownerInstitutionCode = null;
       bibliographicCitation = null;
       identifiedBy = null;
       collectionID = null;
    }



    /**
     * @return the nomenclatureCode
     */
    @Override
    public String getNomenclatureCode() {
        return nomenclaturalCode;
    }



    /**
     * @param nomenclatureCode the nomenclatureCode to set
     */
    @Override
    public void setNomenclatureCode(String nomenclatureCode) {
        this.nomenclaturalCode = nomenclatureCode;
    }





}
