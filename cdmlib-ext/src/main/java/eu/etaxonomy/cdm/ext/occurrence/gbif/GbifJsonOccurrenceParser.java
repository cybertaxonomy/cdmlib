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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.service.media.MediaInfoFileReader;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * Utility class which provides the functionality to convert a JSON response
 * resulting from a GBIF query for occurrences to the corresponding CDM entities.
 *
 * @author pplitzner
 * @since 22.05.2014
 */
public class GbifJsonOccurrenceParser {

    private static final Logger logger = LogManager.getLogger();

    private static final ObjectMapper mapper = new ObjectMapper();

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

    private static final String PLANTAE = "Plantae";
    private static final String ANIMALIA = "Animalia";
    private static final String FUNGI = "Fungi";
    private static final String BACTERIA = "Bacteria";
    private static final String MULTIMEDIA = "media";

    /**
     * Parses the given {@link String} for occurrences.<br>
     * Note: The data structure of the GBIF response should not be changed.
     * @param jsonString JSON data as a String
     * @return the found occurrences as a collection of {@link GbifResponse}
     */
    public static Collection<GbifResponse> parseJsonRecords(String jsonString) {
        JsonNode neoJsonNode = null;
        try {
            neoJsonNode = mapper.readTree(jsonString);
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return parseJsonNode(neoJsonNode);
    }

    /**
     * Parses the given {@link InputStream} for occurrences.
     * @param jsonString JSON data as an InputStream
     * @return the found occurrences as a collection of {@link GbifResponse}
     */
    public static Collection<GbifResponse> parseJsonRecords(InputStream inputStream) throws IOException{
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(inputStream, stringWriter, Charset.defaultCharset());
        return parseJsonRecords(stringWriter.toString());
    }

    /**
     * Parses the given {@link JsonNode} for occurrences.<br>
     * Note: The data structure of the GBIF response should not be changed.
     * @param jsonString JSON data as an JsonNode
     * @return the found occurrences as a collection of {@link GbifResponse}
     */
    public static Collection<GbifResponse> parseJsonNode(JsonNode jsonNode){
        return parseJsonResults(jsonNode.get("results"));
    }

    /**
     * Parses the given {@link JsonNode} for occurrences.
     * @param jsonNode JSON data as an {@link JsonNode}
     * @return the found occurrences as a collection of {@link GbifResponse}
     */
    private static Collection<GbifResponse> parseJsonResults(JsonNode jsonResult) {
        Collection<GbifResponse> results = new ArrayList<>();
        String[] tripleId = new String[3];
        String string;
        for(Object o:jsonResult){
            //parse every record
            tripleId = new String[3];
            if(o instanceof ObjectNode){
                String dataSetKey = null;
                GbifDataSetProtocol dataSetProtocol = null;
                DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
                TaxonName name = null;
                ObjectNode record = (ObjectNode)o;

                if(record.has(DATASET_PROTOCOL)){
                    dataSetProtocol = GbifDataSetProtocol.parseProtocol(record.get(DATASET_PROTOCOL).textValue());
                }
                if(record.has(DATASET_KEY)){
                    dataSetKey = record.get(DATASET_KEY).textValue();
                }
                if(record.has(COUNTRY_CODE)){
                    string = record.get(COUNTRY_CODE).textValue();
                    Country country = Country.getCountryByIso3166A2(string);
                    if(country!=null){
                        derivedUnitFacade.setCountry(country);
                    }
                }
                if(record.has(LOCALITY)){
                    string = record.get(LOCALITY).textValue();
                    derivedUnitFacade.setLocality(string);
                }

                if (record.has("species")){
                    Rank rank = null;

                    if (record.has(TAXON_RANK)){
                        string= record.get(TAXON_RANK).textValue();
                        try {
                            rank = Rank.getRankByLatinName(string);
                        } catch (UnknownCdmTypeException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if (rank != null){
                        if (record.has(NOMENCLATURALCODE)){
                            string = record.get(NOMENCLATURALCODE).textValue();

                            if (string.equals(NomenclaturalCode.ICZN.getTitleCache())){
                                name = TaxonNameFactory.NewZoologicalInstance(rank);
                            } else if (string.equals(NomenclaturalCode.ICNAFP.getTitleCache())) {
                                name = TaxonNameFactory.NewBotanicalInstance(rank);
                            } else if (string.equals(NomenclaturalCode.ICNP.getTitleCache())){
                                name = TaxonNameFactory.NewBacterialInstance(rank);
                            } else if (string.equals(NomenclaturalCode.ICNCP.getTitleCache())){
                                name = TaxonNameFactory.NewCultivarInstance(rank);
                            } else if (string.equals(NomenclaturalCode.ICVCN.getTitleCache())){
                                name = TaxonNameFactory.NewViralInstance(rank);
                            } else if (string.equals("ICN")){
                                name = TaxonNameFactory.NewBotanicalInstance(rank);
                            }
                        }else {
                            if (record.has(KINGDOM)){
                                String kingdom = record.get(KINGDOM).textValue();
                                if (kingdom.equals(PLANTAE)){
                                    name = TaxonNameFactory.NewBotanicalInstance(rank);
                                } else if (kingdom.equals(ANIMALIA)){
                                    name = TaxonNameFactory.NewZoologicalInstance(rank);
                                } else if (kingdom.equals(FUNGI)){
                                    name = TaxonNameFactory.NewBotanicalInstance(rank);
                                } else if (kingdom.equals(BACTERIA)){
                                    name = TaxonNameFactory.NewBacterialInstance(rank);
                                } else{
                                    name = TaxonNameFactory.NewNonViralInstance(rank);
                                }
                            } else{
                                name = TaxonNameFactory.NewNonViralInstance(rank);
                            }
                        }
                        if (name == null){
                            name = TaxonNameFactory.NewNonViralInstance(rank);
                        }
                        if (record.has(GENUS)){
                            name.setGenusOrUninomial(record.get(GENUS).textValue());
                        }
                        if (record.has(SPECIFIC_EPITHET)){
                            name.setSpecificEpithet(record.get(SPECIFIC_EPITHET).textValue());
                        }
                        if (record.has(INFRASPECIFIC_EPITHET)){
                            name.setInfraSpecificEpithet(record.get(INFRASPECIFIC_EPITHET).textValue());
                        }
                        if (record.has(SCIENTIFIC_NAME)){
                            name.setTitleCache(record.get(SCIENTIFIC_NAME).textValue(), true);
                        }
                    }
                    DeterminationEvent detEvent = DeterminationEvent.NewInstance();

                    if (record.has(IDENTIFIED_BY)){
                        Person determiner = Person.NewTitledInstance(record.get(IDENTIFIED_BY).textValue());
                        detEvent.setDeterminer(determiner);
                    }
                    detEvent.setTaxonName(name);
                    detEvent.setPreferredFlag(true);
                    derivedUnitFacade.addDetermination(detEvent);
                }

                // GPS location
                Point location = Point.NewInstance();
                derivedUnitFacade.setExactLocation(location);
                try {
                    if(record.has(LATITUDE)){
                        String lat = record.get(LATITUDE).textValue();
                        location.setLatitudeByParsing(lat);
                    }
                    if(record.has(LONGITUDE)){
                        String lon = record.get(LONGITUDE).textValue();
                        location.setLongitudeByParsing(lon);
                    }
                } catch (ParseException e) {
                    logger.error("Could not parse GPS coordinates", e);
                }
                if(record.has(GEOREFERENCE_PROTOCOL)){
                    String geo = record.get(GEOREFERENCE_PROTOCOL).textValue();
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
                        double elevation = record.get(ELEVATION).doubleValue();
                        /*int length = string.length();
                        StringBuilder builder = new StringBuilder();
                        for(int i=0;i<length;i++){
                            if(Character.isDigit(string.charAt(i))){
                                builder.append(string.charAt(i));
                            }
                            else{
                                break;
                            }
                        }*/
                        derivedUnitFacade.setAbsoluteElevation((int)elevation);
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse elevation", e);
                    }
                }

                //Date (Gathering Period)
                TimePeriod timePeriod = TimePeriod.NewInstance();
                derivedUnitFacade.setGatheringPeriod(timePeriod);
                //TODO what happens with eventDate??
                if(record.has(YEAR)){
                    timePeriod.setStartYear(record.get(YEAR).asInt());
                }
                if(record.has(MONTH)){
                    timePeriod.setStartMonth(record.get(MONTH).asInt());
                }
                if(record.has(DAY)){
                    timePeriod.setStartDay(record.get(DAY).asInt());
                }
                if(record.has(RECORDED_BY)){
                    Person person = Person.NewTitledInstance(record.get(RECORDED_BY).textValue());
                    //FIXME check data base if collector already present
                    derivedUnitFacade.setCollector(person);
                }

                //collector number (fieldNumber OR recordNumber)
                if(record.has(FIELD_NUMBER)){
                    derivedUnitFacade.setFieldNumber(record.get(FIELD_NUMBER).textValue());
                }
                //collector number (fieldNumber OR recordNumber)
                if(record.has(RECORD_NUMBER)){
                    derivedUnitFacade.setFieldNumber(record.get(RECORD_NUMBER).textValue());
                }

                if(record.has(EVENT_REMARKS)){
                    derivedUnitFacade.setGatheringEventDescription(record.get(EVENT_REMARKS).textValue());
                }
                if(record.has(OCCURRENCE_REMARKS)){
                    derivedUnitFacade.setEcology(record.get(OCCURRENCE_REMARKS).textValue());
                }
                if(record.has(COLLECTION_CODE)){
                    String collectionCode = record.get(COLLECTION_CODE).textValue();
                    tripleId[2] = collectionCode;
                    //FIXME: check data base for existing collections
                    eu.etaxonomy.cdm.model.occurrence.Collection collection = eu.etaxonomy.cdm.model.occurrence.Collection.NewInstance();
                    collection.setCode(collectionCode);
                    if(record.has(INSTITUTION_CODE)){
                        Institution institution = Institution.NewNamedInstance(record.get(INSTITUTION_CODE).textValue());
                        institution.setCode(record.get(INSTITUTION_CODE).textValue());
                        collection.setInstitute(institution);
                    }
                    derivedUnitFacade.setCollection(collection);
                }
                if(record.has(CATALOG_NUMBER)){
                    derivedUnitFacade.setCatalogNumber(record.get(CATALOG_NUMBER).textValue());
                    derivedUnitFacade.setAccessionNumber(record.get(CATALOG_NUMBER).textValue());
                    tripleId[0]= record.get(CATALOG_NUMBER).textValue();
                }
                if(record.has(INSTITUTION_CODE)){
                    derivedUnitFacade.setAccessionNumber(record.get(INSTITUTION_CODE).textValue());
                    tripleId[1]= record.get(INSTITUTION_CODE).textValue();
                }

                if (record.has(OCCURENCE_ID)){
                    IdentifiableSource source = IdentifiableSource.NewDataImportInstance((record.get(OCCURENCE_ID).textValue()));
                    derivedUnitFacade.addSource(source);
                }

                if (record.has(MULTIMEDIA)){
                    //TODO!!!!!
                    //http://ww2.bgbm.org/herbarium/images/B/-W/08/53/B_-W_08537%20-00%201__3.jpg
                    JsonNode multimediaNode = record.get(MULTIMEDIA);
                    ObjectNode mediaRecord;
                    SpecimenOrObservationType type = null;
                    for(Object object:multimediaNode){
                        //parse every record
                        Media media = Media.NewInstance();
                        URI uri = null;
                        CdmImageInfo imageInf = null;

                        if(object instanceof ObjectNode){
                            mediaRecord = (ObjectNode) object;

                            if (mediaRecord.has("identifier")){
                                try {

                                    uri = new URI(mediaRecord.get("identifier").textValue());
                                    imageInf = MediaInfoFileReader.legacyFactoryMethod(uri)
                                        .readBaseInfo()
                                        .getCdmImageInfo();
                                } catch (URISyntaxException |IOException | HttpException | IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                               // media.addIdentifier(mediaRecord.getString("identifier"), null);
                            }
                            if (mediaRecord.has("references")){


                            }
                            if (mediaRecord.has("format")){

                            }
                            if (mediaRecord.has("type")){
                                if (mediaRecord.get("type").equals("StillImage")){
                                    type = SpecimenOrObservationType.StillImage;
                                }
                            }
                        }
                        ImageFile imageFile = ImageFile.NewInstance(uri, null, imageInf);
                        MediaRepresentation representation = MediaRepresentation.NewInstance();

                        representation.addRepresentationPart(imageFile);
                        media.addRepresentation(representation);

                        derivedUnitFacade.addDerivedUnitMedia(media);
                    }
                    //identifier=http://ww2.bgbm.org/herbarium/images/B/-W/08/53/B_-W_08537%20-00%201__3.jpg
                    //references=http://ww2.bgbm.org/herbarium/view_biocase.cfm?SpecimenPK=136628
                    //format=image/jpeg
                    //type=StillImage
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
        IOUtils.copy(inputStream, stringWriter, Charset.defaultCharset());
        return parseOriginalDataSetUri(stringWriter.toString());
    }

    public static DataSetResponse parseOriginalDataSetUri(String jsonString) {
        DataSetResponse response = new DataSetResponse();

        JsonNode neoJsonNode = null;
        try {
            neoJsonNode = mapper.readTree(jsonString);
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (neoJsonNode != null) {
            if(neoJsonNode.has(URL)){
                response.setEndpoint(URI.create(neoJsonNode.get(URL).textValue()));
            }
            if(neoJsonNode.has(TYPE)){
                response.setProtocol(GbifDataSetProtocol.parseProtocol(neoJsonNode.get(TYPE).textValue()));
            }
        }
        return response;
    }
}
