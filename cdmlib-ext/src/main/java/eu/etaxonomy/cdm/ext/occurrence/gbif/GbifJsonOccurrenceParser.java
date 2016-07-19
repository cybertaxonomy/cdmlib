// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.gbif;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * Utility class which provides the functionality to convert a JSON response
 * resulting from a GBIF query for occurrences to the corresponding CDM entities.
 * @author pplitzner
 * @date 22.05.2014
 *
 */
public class GbifJsonOccurrenceParser {

    private static final Logger logger = Logger.getLogger(GbifJsonOccurrenceParser.class);

    private static final String DATASET_KEY = "datasetKey";
    private static final String DATASET_PROTOCOL = "protocol";

    private static final String KEY = "key";
    private static final String URL = "url";
    private static final String TYPE = "type";

    private static final String COUNTRY_CODE = "countryCode";
    private static final String LOCALITY = "locality";
    private static final String LONGITUDE = "decimalLongitude";
    private static final String LATITUDE = "decimalLatitude";
    private static final String GEOREFERENCE_PROTOCOL = "georeferenceProtocol";//reference system
    private static final String VERBATIM_ELEVATION = "verbatimElevation";
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String EVENT_DATE= "eventDate";
    private static final String RECORDED_BY= "recordedBy";//collector
    private static final String RECORD_NUMBER = "recordNumber";//collector number
    private static final String FIELD_NUMBER = "fieldNumber";//collector number
    private static final String EVENT_REMARKS = "eventRemarks";//gathering event description
    private static final String OCCURRENCE_REMARKS = "occurrenceRemarks";//ecology
    private static final String COLLECTION_CODE = "collectionCode";
    private static final String CATALOG_NUMBER = "catalogNumber";//accession number
    private static final String INSTITUTION_CODE = "institutionCode";


    protected static final String PUBLISHING_ORG_KEY = "publishingOrgKey";
    protected static final String PUBLISHING_COUNTRY = "publishingCountry";

    protected static final String EXTENSIONS = "extensions";
    protected static final String BASIS_OF_RECORD = "basisOfRecord";
    protected static final String INDIVIDUAL_COUNT = "individualCount";
    protected static final String TAXONKEY = "taxonKey";
    protected static final String KINGDOM_KEY = "kingdomKey";
    protected static final String PHYLUM_KEY = "phylumKey";
    protected static final String CLASS_KEY = "classKey";
    protected static final String ORDER_KEY = "orderKey";
    protected static final String FAMILY_KEY = "familyKey";
    protected static final String GENUS_KEY = "genusKey";
    protected static final String SPECIES_KEY = "speciesKey";
    protected static final String SCIENTIFIC_NAME = "scientificName";
    protected static final String KINGDOM =  "kingdom";
    protected static final String PHYLUM = "phylum";
    protected static final String ORDER = "order";
    protected static final String FAMILY  = "family";
    protected static final String GENUS = "genus";
    protected static final String SPECIES = "species";
    protected static final String GENERIC_NAME = "genericName";
    protected static final String SPECIFIC_EPITHET = "specificEpithet";
    protected static final String INFRASPECIFIC_EPITHET = "infraspecificEpithet";
    protected static final String TAXON_RANK = "taxonRank";
    protected static final String DATE_IDENTIFIED = "dateIdentified";
    protected static final String SCIENTIFIC_NAME_AUTHORSHIP = "scientificNameAuthorship";

    protected static final String ELEVATION = "elevation";
    protected static final String CONITNENT = "continent";
    protected static final String STATE_PROVINCE = "stateProvince";




    protected static final String ISSUES = "issues";
    protected static final String LAST_INTERPRETED = "lastInterpreted";
    protected static final String IDENTIFIERS = "identifiers";
    protected static final String FACTS = "facts";
    protected static final String RELATIONS = "relations";
    protected static final String GEODETICDATUM = "geodeticDatum";
    protected static final String CLASS = "class";

    protected static final String COUNTRY = "country";
    protected static final String NOMENCLATURAL_STATUS = "nomenclaturalStatus";
    protected static final String RIGHTSHOLDER = "rightsHolder";
    protected static final String IDEMTIFIER = "identifier";

    protected static final String NOMENCLATURALCODE = "nomenclaturalCode";
    protected static final String COUNTY = "county";

    protected static final String DATASET_NAME = "datasetName";
    protected static final String GBIF_ID = "gbifID";

    protected static final String OCCURENCE_ID = "occurrenceID";

    protected static final String TAXON_ID = "taxonID";
    protected static final String LICENCE = "license";

    protected static final String OWNER_INSTITUTION_CODE = "ownerInstitutionCode";
    protected static final String BIBLIOGRAPHIC_CITATION = "bibliographicCitation";
    protected static final String IDENTIFIED_BY = "identifiedBy";
    protected static final String COLLECTION_ID = "collectionID";





    /**
     * Parses the given {@link String} for occurrences.<br>
     * Note: The data structure of the GBIF response should not be changed.
     * @param jsonString JSON data as a String
     * @return the found occurrences as a collection of {@link GbifResponse}
     */
    public static Collection<GbifResponse> parseJsonRecords(String jsonString) {
        return parseJsonRecords(JSONObject.fromObject(jsonString));
    }

    /**
     * Parses the given {@link InputStream} for occurrences.
     * @param jsonString JSON data as an InputStream
     * @return the found occurrences as a collection of {@link GbifResponse}
     */
    public static Collection<GbifResponse> parseJsonRecords(InputStream inputStream) throws IOException{
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(inputStream, stringWriter);
        return parseJsonRecords(stringWriter.toString());
    }

    /**
     * Parses the given {@link JSONObject} for occurrences.<br>
     * Note: The data structure of the GBIF response should not be changed.
     * @param jsonString JSON data as an JSONObject
     * @return the found occurrences as a collection of {@link GbifResponse}
     */
    public static Collection<GbifResponse> parseJsonRecords(JSONObject jsonObject){
        return parseJsonRecords(jsonObject.getJSONArray("results"));
    }

    /**
     * Parses the given {@link JSONArray} for occurrences.
     * @param jsonString JSON data as an {@link JSONArray}
     * @return the found occurrences as a collection of {@link GbifResponse}
     */
    private static Collection<GbifResponse> parseJsonRecords(JSONArray jsonArray) {
        Collection<GbifResponse> results = new ArrayList<GbifResponse>();
        String[] tripleId = new String[3];
        for(Object o:jsonArray){
            //parse every record
            if(o instanceof JSONObject){
                String dataSetKey = null;
                GbifDataSetProtocol dataSetProtocol = null;
                DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
                NonViralName<NonViralName> name = null;
                JSONObject record = (JSONObject)o;

                if(record.has(DATASET_PROTOCOL)){
                    dataSetProtocol = GbifDataSetProtocol.parseProtocol(record.getString(DATASET_PROTOCOL));
                }
                if(record.has(DATASET_KEY)){
                    dataSetKey = record.getString(DATASET_KEY);
                }
                if(record.has(COUNTRY_CODE)){
                    String string = record.getString(COUNTRY_CODE);
                    Country country = Country.getCountryByIso3166A2(string);
                    if(country!=null){
                        derivedUnitFacade.setCountry(country);
                    }
                }
                if(record.has(LOCALITY)){
                    String string = record.getString(LOCALITY);
                    derivedUnitFacade.setLocality(string);
                }

                if (record.has("species")){
                    Rank rank = null;
                    String string;
                    if (record.has(TAXON_RANK)){
                        string= record.getString(TAXON_RANK);
                        try {
                            rank = Rank.getRankByName(string);
                        } catch (UnknownCdmTypeException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if (rank != null){
                        name = NonViralName.NewInstance(rank);
                        if (record.has(GENUS)){
                            name.setGenusOrUninomial(record.getString(GENUS));
                        }
                        if (record.has(SPECIFIC_EPITHET)){
                            name.setSpecificEpithet(record.getString(SPECIFIC_EPITHET));
                        }
                        if (record.has(INFRASPECIFIC_EPITHET)){
                            name.setSpecificEpithet(record.getString(INFRASPECIFIC_EPITHET));
                        }
                    }
                }



                // GPS location
                Point location = Point.NewInstance();
                derivedUnitFacade.setExactLocation(location);
                try {
                    if(record.has(LATITUDE)){
                        String lat = record.getString(LATITUDE);
                        location.setLatitudeByParsing(lat);
                    }
                    if(record.has(LONGITUDE)){
                        String lon = record.getString(LONGITUDE);
                        location.setLongitudeByParsing(lon);
                    }
                } catch (ParseException e) {
                    logger.error("Could not parse GPS coordinates", e);
                }
                if(record.has(GEOREFERENCE_PROTOCOL)){
                    String geo = record.getString(GEOREFERENCE_PROTOCOL);
                    ReferenceSystem referenceSystem = null;
                    //TODO: Is there another way than string comparison
                    //to check which reference system is used?
                    if(ReferenceSystem.WGS84().getLabel().contains(geo)){
                        referenceSystem = ReferenceSystem.WGS84();
                    }
                    else if(ReferenceSystem.GOOGLE_EARTH().getLabel().contains(geo)){
                        referenceSystem = ReferenceSystem.GOOGLE_EARTH();
                    }
                    else if(ReferenceSystem.GAZETTEER().getLabel().contains(geo)){
                        referenceSystem = ReferenceSystem.GAZETTEER();
                    }
                    location.setReferenceSystem(referenceSystem);
                }

                if(record.has(ELEVATION)){
                    try {
                        //parse integer and strip of unit
                        String string = record.getString(ELEVATION);
                        int length = string.length();
                        StringBuilder builder = new StringBuilder();
                        for(int i=0;i<length;i++){
                            if(Character.isDigit(string.charAt(i))){
                                builder.append(string.charAt(i));
                            }
                            else{
                                break;
                            }
                        }
                        derivedUnitFacade.setAbsoluteElevation(Integer.parseInt(builder.toString()));
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse elevation", e);
                    }
                }

                //Date (Gathering Period)
                TimePeriod timePeriod = TimePeriod.NewInstance();
                derivedUnitFacade.setGatheringPeriod(timePeriod);
                //TODO what happens with eventDate??
                if(record.has(YEAR)){
                    timePeriod.setStartYear(record.getInt(YEAR));
                }
                if(record.has(MONTH)){
                    timePeriod.setStartMonth(record.getInt(MONTH));
                }
                if(record.has(DAY)){
                    timePeriod.setStartDay(record.getInt(DAY));
                }
                if(record.has(RECORDED_BY)){
                    Person person = Person.NewTitledInstance(record.getString(RECORDED_BY));
                    //FIXME check data base if collector already present
                    derivedUnitFacade.setCollector(person);
                }

                //collector number (fieldNumber OR recordNumber)
                if(record.has(FIELD_NUMBER)){
                    derivedUnitFacade.setFieldNumber(record.getString(FIELD_NUMBER));
                }
                //collector number (fieldNumber OR recordNumber)
                if(record.has(RECORD_NUMBER)){
                    derivedUnitFacade.setFieldNumber(record.getString(RECORD_NUMBER));
                }

                if(record.has(EVENT_REMARKS)){
                    derivedUnitFacade.setGatheringEventDescription(record.getString(EVENT_REMARKS));
                }
                if(record.has(OCCURRENCE_REMARKS)){
                    derivedUnitFacade.setEcology(record.getString(OCCURRENCE_REMARKS));
                }
                if(record.has(COLLECTION_CODE)){
                    String collectionCode = record.getString(COLLECTION_CODE);
                    tripleId[2] = collectionCode;
                    //FIXME: check data base for existing collections
                    eu.etaxonomy.cdm.model.occurrence.Collection collection = eu.etaxonomy.cdm.model.occurrence.Collection.NewInstance();
                    collection.setCode(collectionCode);
                    derivedUnitFacade.setCollection(collection);
                }
                if(record.has(CATALOG_NUMBER)){
                    derivedUnitFacade.setAccessionNumber(record.getString(CATALOG_NUMBER));
                    tripleId[0]= record.getString(CATALOG_NUMBER);
                }
                if(record.has(INSTITUTION_CODE)){
                    derivedUnitFacade.setAccessionNumber(record.getString(INSTITUTION_CODE));
                    tripleId[1]= record.getString(INSTITUTION_CODE);
                }

                // create dataset URL
                URI uri = null;
                try {
                    uri = UriUtils.createUri(new URL(GbifQueryServiceWrapper.BASE_URL), "/v1/dataset/"+dataSetKey+"/endpoint", null, null);
                } catch (MalformedURLException e) {
                    logger.error("Endpoint URI could not be created!", e);
                } catch (URISyntaxException e) {
                    logger.error("Endpoint URI could not be created!", e);
                }
                results.add(new GbifResponse(derivedUnitFacade, uri, dataSetProtocol, tripleId, name));
            }
        }
        return results;
    }

    public static DataSetResponse parseOriginalDataSetUri(InputStream inputStream) throws IOException {
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(inputStream, stringWriter);
        return parseOriginalDataSetUri(stringWriter.toString());
    }

    public static DataSetResponse parseOriginalDataSetUri(String jsonString) {
        DataSetResponse response = new DataSetResponse();
        JSONArray jsonArray = JSONArray.fromObject(jsonString);
        Object next = jsonArray.iterator().next();
        if(next instanceof JSONObject){
            JSONObject jsonObject = (JSONObject)next;
            if(jsonObject.has(URL)){
                response.setEndpoint(URI.create(jsonObject.getString(URL)));
            }
            if(jsonObject.has(TYPE)){
                response.setProtocol(GbifDataSetProtocol.parseProtocol(jsonObject.getString(TYPE)));
            }
        }
        return response;
    }

}
