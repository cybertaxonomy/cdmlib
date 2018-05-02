/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.ext.geo;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.common.StreamUtils;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @since 08.10.2008
 */
public class EditGeoServiceTest extends CdmTransactionalIntegrationTest {
    private static final Logger logger = Logger.getLogger(EditGeoServiceTest.class);

    private static final String EDIT_MAPSERVICE_URI_STING = "http://edit.africamuseum.be/edit_wp5/v1.2/rest_gen.php";
    private static URI editMapServiceUri;

    @SpringBeanByType
    private IDefinedTermDao termDao;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private IVocabularyService vocabService;

    @SpringBeanByType
    private GeoServiceAreaAnnotatedMapping mapping;

    @SpringBeanByType
    private IEditGeoService editGeoService;

    @SpringBeanByType
    private ITaxonService taxonService ;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        EditGeoServiceUtilities.setTermDao(termDao);
        System.setProperty("ONLY-A-TEST", "TRUE"); // allows EditGeoServiceUtilities to skip some line of code
        editMapServiceUri = new URI(EDIT_MAPSERVICE_URI_STING);
    }


//******************************************** TESTS**************

    @Test
    public void testGetWebServiceUrlCountry() throws MalformedURLException, IOException {
        Set<Distribution> distributions = new HashSet<Distribution>();
        Country germany = termService.findByIdInVocabulary("DEU", Country.uuidCountryVocabulary, Country.class);
//        germany = (Country)termService.find(665);
//        germany = (Country)termService.find(UUID.fromString("cbe7ce69-2952-4309-85dd-0d7d4a4830a1"));

//        germany = Country.GERMANY();

        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.PRESENT()));
        distributions.add(Distribution.NewInstance(termService.findByIdInVocabulary("DE", Country.uuidCountryVocabulary, Country.class), PresenceAbsenceTerm.INTRODUCED()));
        Map<PresenceAbsenceTerm, Color> presenceAbsenceColorMap = new HashMap<PresenceAbsenceTerm, Color>();
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.PRESENT(), Color.BLUE);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.INTRODUCED(), Color.BLACK);
        List<Language> languages = new ArrayList<Language>();

        boolean subAreaPreference = false;
        boolean statusOrderPreference = false;


        Collection<Distribution> filteredDistributions = DescriptionUtility.filterDistributions(
                distributions, null, true, statusOrderPreference, subAreaPreference);

        String result = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(filteredDistributions,
                mapping, null, null, languages );
        logger.warn(result);
        Assert.assertTrue("WebServiceUrl must contain country part for Germany", result.matches(".*ad=country_earth(%3A|:)gmi_cntry:.:DEU.*"));
    }

    @Test
    public void testGetWebServiceUrlTdwg() throws MalformedURLException, IOException {
        //String webServiceUrl = "http://www.test.de/webservice";
        Set<Distribution> distributions = new HashSet<Distribution>();
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("SPA"), PresenceAbsenceTerm.PRESENT()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("GER"), PresenceAbsenceTerm.INTRODUCED()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("14"), PresenceAbsenceTerm.CULTIVATED()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("BGM"), PresenceAbsenceTerm.ABSENT()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("FRA"), PresenceAbsenceTerm.ABSENT()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("IND-AP"), PresenceAbsenceTerm.PRESENT()));

        Map<PresenceAbsenceTerm, Color> presenceAbsenceColorMap = new HashMap<PresenceAbsenceTerm, Color>();
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.PRESENT(), Color.BLUE);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.INTRODUCED(), Color.BLACK);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.CULTIVATED(), Color.YELLOW);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.ABSENT(), Color.DARK_GRAY);
        String backLayer ="";
        presenceAbsenceColorMap = null;
        String bbox="-20,0,120,70";
        List<Language> languages = new ArrayList<Language>();

        boolean subAreaPreference = false;
        boolean statusOrderPreference = false;
        String result = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(
                distributions,
                mapping,
                null, // presenceAbsenceTermColors
                null, // projectToLayer
                languages );
        //TODO Set semantics is not determined
        //String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";
        logger.debug(result);
        assertTrue(result.matches(".*ad=tdwg[1-4].*"));
        assertTrue(result.matches(".*tdwg2:[a-d]:14[\\|&].*") );
        assertTrue(result.matches(".*[a-d]:FRA,BGM[\\|&].*") || result.matches(".*[a-d]:BGM,FRA[\\|&].*") );
        assertTrue(result.matches(".*[a-d]:GER[\\|&].*") );
        assertTrue(result.matches(".*[a-d]:SPA[\\|&].*") );
        assertTrue(result.matches(".*tdwg4:[a-d]:INDAP[\\|&].*") );
        //assertTrue(result.matches("0000ff"));
        //TODO continue

        // request map image from webservice
        subTestWithEditMapService(result);
    }

    @Test
    public void testGetWebServiceUrlCyprus() throws ClientProtocolException, IOException, URISyntaxException {
        makeCyprusAreas();
        Set<Distribution> distributions = new HashSet<Distribution>();
        distributions.add(Distribution.NewInstance(divisions.get("1"), PresenceAbsenceTerm.PRESENT()));
        distributions.add(Distribution.NewInstance(divisions.get("2"), PresenceAbsenceTerm.INTRODUCED()));
        distributions.add(Distribution.NewInstance(divisions.get("3"), PresenceAbsenceTerm.CULTIVATED()));
        distributions.add(Distribution.NewInstance(divisions.get("4"), PresenceAbsenceTerm.ABSENT()));
        distributions.add(Distribution.NewInstance(divisions.get("5"), PresenceAbsenceTerm.ABSENT()));
        distributions.add(Distribution.NewInstance(divisions.get("6"), PresenceAbsenceTerm.PRESENT()));

        Map<PresenceAbsenceTerm, Color> presenceAbsenceColorMap = new HashMap<PresenceAbsenceTerm, Color>();
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.PRESENT(), Color.BLUE);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.INTRODUCED(), Color.BLACK);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.CULTIVATED(), Color.YELLOW);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.ABSENT(), Color.DARK_GRAY);
        String backLayer ="";
        presenceAbsenceColorMap = null;
        String bbox="-20,0,120,70";
        List<Language> languages = new ArrayList<Language>();

        boolean subAreaPreference = false;
        boolean statusOrderPreference = false;
        String result = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(
                distributions,
                mapping,
                null, null, languages );
        //TODO Set semantics is not determined
        //String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";
        assertTrue(result.matches(".*ad=cyprusdivs%3Abdcode:.*"));
        assertTrue(result.matches(".*[a-d]:5,4[\\|&].*") || result.matches(".*[a-d]:4,5[\\|&].*") );
        assertTrue(result.matches(".*[a-d]:1,6[\\|&].*") || result.matches(".*[a-d]:6,1[\\|&].*") );
        assertTrue(result.matches(".*[a-d]:2[\\|&].*") );
        assertTrue(result.matches(".*[a-d]:3[\\|&].*") );

        // request map image from webservice
        subTestWithEditMapService(result);
    }

    private void subTestWithEditMapService(String queryString)throws MalformedURLException, IOException {
        if(UriUtils.isServiceAvailable(editMapServiceUri)){
            URL requestUrl = new URL(editMapServiceUri.toString() + "?img=false&bbox=-180,-90,180,90&ms=1000&" + queryString);
            logger.debug("editMapServiceUri: " + requestUrl);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.connect();
//            connection.setReadTimeout(10000);  //timeout after 10 sec, does not seem to work
            assertTrue(connection.getResponseCode() == 200);
            InputStream contentStream = connection.getInputStream();
            String content = StreamUtils.readToString(contentStream);
            logger.debug("EditMapService response body:\n" + content);
            assertTrue(content.startsWith("[{"));
            assertTrue(content.endsWith("}]"));
            assertTrue(content.matches(".*\"bbox\":.*"));
            assertTrue(content.matches(".*\"legend\":.*"));
            assertTrue(content.matches(".*\"layers\":.*"));
            assertTrue(content.matches(".*\"sld\":.*"));
            assertTrue(content.matches(".*\"geoserver\":.*"));
        }
    }

    public static final UUID uuidCyprusDivisionsVocabulary = UUID.fromString("2119f610-1f93-4d87-af28-40aeefaca100");
    private final Map<String, NamedArea> divisions = new HashMap<String, NamedArea>();

    private boolean makeCyprusAreas() throws IOException {
        //divisions


        NamedAreaType areaType = NamedAreaType.NATURAL_AREA();
        NamedAreaLevel areaLevel = NamedAreaLevel.NewInstance("Cyprus Division", "Cyprus Division", null);

        termService.saveOrUpdate(areaLevel);

        TermVocabulary<NamedArea> areaVocabulary = TermVocabulary.NewInstance(TermType.NamedArea, "Cyprus devisions", "Cyprus divisions", null, null);
        areaVocabulary.setUuid(uuidCyprusDivisionsVocabulary);


        for(int i = 1; i <= 8; i++){
            UUID divisionUuid = getNamedAreaUuid(String.valueOf(i));
            NamedArea division = this.newNamedArea(
                    divisionUuid,
                    "Division " + i,
                    "Cyprus: Division " + i,
                    String.valueOf(i), // id in vocab
                    areaType,
                    areaLevel,
                    areaVocabulary);
            divisions.put(String.valueOf(i), division);
        }

        vocabService.saveOrUpdate(areaVocabulary);
        commitAndStartNewTransaction(null);


        // import and map shapefile attributes from csv
        InputStream is = getClass().getClassLoader().getResourceAsStream("eu/etaxonomy/cdm/ext/geo/cyprusdivs.csv");
        List<String> idSearchFields = new ArrayList<String>();
        idSearchFields.add("bdcode");
        String wmsLayerName = "cyprusdivs";
        editGeoService.mapShapeFileToNamedAreas(new InputStreamReader(is), idSearchFields, wmsLayerName, uuidCyprusDivisionsVocabulary, null);

        divisions.clear();
        Set<DefinedTermBase> terms = vocabService.load(uuidCyprusDivisionsVocabulary).getTerms();
        for(DefinedTermBase dtb : terms){
            divisions.put(dtb.getIdInVocabulary(), (NamedArea) dtb);
        }


//		indigenousStatus = (PresenceTerm)getTermService().find(CyprusTransformer.indigenousUuid);
//		casualStatus = (PresenceTerm)getTermService().find(CyprusTransformer.casualUuid);
//		nonInvasiveStatus = (PresenceTerm)getTermService().find(CyprusTransformer.nonInvasiveUuid);
//		invasiveStatus = (PresenceTerm)getTermService().find(CyprusTransformer.invasiveUuid);
//		questionableStatus = (PresenceTerm)getTermService().find(CyprusTransformer.questionableUuid);

        return true;


    }

    public static final UUID uuidDivision1 = UUID.fromString("ab17eee9-1abb-4ce9-a9a2-563f840cdbfc");
    public static final UUID uuidDivision2 = UUID.fromString("c3606165-efb7-4224-a168-63e009eb4aa5");
    public static final UUID uuidDivision3 = UUID.fromString("750d4e07-e34b-491f-a7b7-09723afdc960");
    public static final UUID uuidDivision4 = UUID.fromString("8a858922-e8e5-4791-ad53-906e50633ec7");
    public static final UUID uuidDivision5 = UUID.fromString("16057133-d541-4ebd-81d4-cb92265ec54c");
    public static final UUID uuidDivision6 = UUID.fromString("fbf21230-4a42-4f4c-9af8-5da52123c264");
    public static final UUID uuidDivision7 = UUID.fromString("d31dd96a-36ea-4428-871c-d8552a9565ca");
    public static final UUID uuidDivision8 = UUID.fromString("236ea447-c3ab-486d-9e06-cc5907861acc");


    public UUID getNamedAreaUuid(String key) {
        if (StringUtils.isBlank(key)){return null;
        }else if (key.equalsIgnoreCase("1")){return uuidDivision1;
        }else if (key.equalsIgnoreCase("2")){return uuidDivision2;
        }else if (key.equalsIgnoreCase("3")){return uuidDivision3;
        }else if (key.equalsIgnoreCase("4")){return uuidDivision4;
        }else if (key.equalsIgnoreCase("5")){return uuidDivision5;
        }else if (key.equalsIgnoreCase("6")){return uuidDivision6;
        }else if (key.equalsIgnoreCase("7")){return uuidDivision7;
        }else if (key.equalsIgnoreCase("8")){return uuidDivision8;
        }else{
            return null;
        }
    }

    protected NamedArea newNamedArea(UUID uuid, String label, String text, String IdInVocabulary, NamedAreaType areaType, NamedAreaLevel level, TermVocabulary<NamedArea> voc){
        NamedArea namedArea = NamedArea.NewInstance(text, label, null);
        voc.addTerm(namedArea);
        namedArea.setType(areaType);
        namedArea.setLevel(level);
        namedArea.setUuid(uuid);
        namedArea.setIdInVocabulary(IdInVocabulary);
        return namedArea;
    }

    @Test
    public void testGetWebServiceUrlBangka() throws ClientProtocolException, IOException, URISyntaxException {
        NamedArea areaBangka = NamedArea.NewInstance("Bangka", "Bangka", null);
        TermVocabulary<NamedArea> voc = TermVocabulary.NewInstance(TermType.NamedArea, "test Voc", "test voc", null, null);
        voc.addTerm(areaBangka);

        GeoServiceArea geoServiceArea = new GeoServiceArea();
        String geoServiceLayer="vmap0_as_bnd_political_boundary_a";
        String layerFieldName ="nam";
        String areaValue = "PULAU BANGKA#SUMATERA SELATAN";
        geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
        geoServiceArea.add(geoServiceLayer, layerFieldName, "BALI");

        mapping.set(areaBangka, geoServiceArea);
        Set<Distribution> distributions = new HashSet<Distribution>();
        distributions.add(Distribution.NewInstance(areaBangka, PresenceAbsenceTerm.PRESENT()));

        Map<PresenceAbsenceTerm, Color> presenceAbsenceColorMap = new HashMap<PresenceAbsenceTerm, Color>();
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.PRESENT(), Color.BLUE);

        presenceAbsenceColorMap = null;
        List<Language> languages = new ArrayList<Language>();

        boolean subAreaPreference = false;
        boolean statusOrderPreference = false;
        String result = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(distributions,
                mapping,
                null, null, languages );
        //TODO Set semantics is not determined
        //String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";

        logger.debug(result);
        assertTrue(result.matches(".*ad=vmap0_as_bnd_political_boundary_a%3Anam:.*"));
        assertTrue(result.matches(".*(PULAU\\+BANGKA%23SUMATERA\\+SELATAN).*") );
        assertTrue(result.matches(".*(BALI).*") );

        // request map image from webservice
        subTestWithEditMapService(result);
    }


    @SuppressWarnings("deprecation")
//    @Test
    @DataSet( value="EditGeoServiceTest.getDistributionServiceRequestParameterString.xml")
//    @DataSets({
//        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
//        @DataSet("/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
//        @DataSet( value="EditGeoServiceTest.getDistributionServiceRequestParameterString.xml")
//    })
    public void getDistributionServiceRequestParameterString(){
        boolean subAreaPreference = false;
        boolean statusOrderPreference = false;
        Set<MarkerType> hideMarkedAreas = null;
        Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors = null;
        List<Language> langs = null;

        List<TaxonDescription> taxonDescriptions = new ArrayList<TaxonDescription>();
        TaxonDescription description1 = TaxonDescription.NewInstance();
        taxonDescriptions.add(description1);
        Distribution distribution1 = Distribution.NewInstance(Country.GERMANY(), null);
        description1.addElement(distribution1);
        Distribution distribution2 = Distribution.NewInstance(Country.FRANCEFRENCHREPUBLIC(), null);
        distribution2.setFeature(Feature.COMMON_NAME());
        description1.addElement(distribution2);

        Taxon taxon = (Taxon)taxonService.find(UUID.fromString("7598f5d4-1cf2-4269-ae99-2adb79ae167c"));
        TaxonDescription taxDesc = taxon.getDescriptions().iterator().next();
        for (DescriptionElementBase deb : taxDesc.getElements()){
            Distribution distribution = CdmBase.deproxy(deb, Distribution.class);
            NamedArea area = distribution.getArea();
            System.out.println(area.getTitleCache());
        }
        taxonDescriptions.addAll(taxon.getDescriptions());

        String distributions = editGeoService.getDistributionServiceRequestParameterString(taxonDescriptions,
                subAreaPreference, statusOrderPreference, hideMarkedAreas, presenceAbsenceTermColors, langs);
        System.out.println(distributions);
        Assert.assertTrue("Distribution string should contain the non-persited distribution Germany", distributions.contains("DEU"));
        Assert.assertFalse("Distribution string should contain France as it has a non-distribution feature", distributions.contains("FRA"));

//        CHE,POL
    }

    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {

        List<TaxonDescription> taxonDescriptions = new ArrayList<TaxonDescription>();
        TaxonDescription description1 = TaxonDescription.NewInstance();
        taxonDescriptions.add(description1);
        Distribution distribution1 = Distribution.NewInstance(Country.POLANDPOLISHPEOPLESREPUBLIC(), null);
        description1.addElement(distribution1);
        Distribution distribution2 = Distribution.NewInstance(Country.SWITZERLANDSWISSCONFEDERATION(), null);
//        distribution2.setFeature(Feature.COMMON_NAME());
        description1.addElement(distribution2);
        Taxon taxon = Taxon.NewInstance(null, null);
        taxon.setTitleCache("Dummy taxon", true);
        taxon.addDescription(description1);
        taxon.setUuid(UUID.fromString("7598f5d4-1cf2-4269-ae99-2adb79ae167c"));

        taxonService.save(taxon);


        setComplete();
        endTransaction();

        writeDbUnitDataSetFile(new String[] {
                "TAXONBASE",
                "DESCRIPTIONBASE", "DESCRIPTIONELEMENTBASE",
                "HIBERNATE_SEQUENCES" // IMPORTANT!!!
                },
                "getDistributionServiceRequestParameterString" );

    }


}
