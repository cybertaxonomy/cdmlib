/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto.InfoPart;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.StreamUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * TODO can't we make this service (termservice and mapping) independent and
 * not require {@link CdmTransactionalIntegrationTest}?
 *
 * @author muellera
 * @since 03.03.2024
 */
public class AreaMapServiceParameterBuilderTest extends CdmTransactionalIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

    private static final String EDIT_MAPSERVICE_URI_STRING = "https://edit.africamuseum.be/edit_wp5/v1.2/rest_gen.php";
    private static URI editMapServiceUri;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private ICommonService commonService;

    @SpringBeanByType
    private GeoServiceAreaAnnotatedMapping mapping;

    private AreaMapServiceParameterBuilder builder;

    @Before
    public void setup() throws Exception {
        builder = new AreaMapServiceParameterBuilder();
        System.setProperty("ONLY-A-TEST", "TRUE"); // allows EditGeoServiceUtilities to skip some line of code
        editMapServiceUri = new URI(EDIT_MAPSERVICE_URI_STRING);
    }

    @Test
    public void testGetWebServiceUrlCountry() {

        Set<Distribution> distributions = new HashSet<>();
        Country germany = termService.findByIdInVocabulary("DEU", Country.uuidCountryVocabulary, Country.class);

        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.PRESENT()));
        distributions.add(Distribution.NewInstance(termService.findByIdInVocabulary("DE", Country.uuidCountryVocabulary, Country.class), PresenceAbsenceTerm.INTRODUCED()));
        Map<UUID, Color> presenceAbsenceColorMap = new HashMap<>();
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.uuidPresent, Color.BLUE);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.uuidIntroduced, Color.BLACK);
        List<Language> languages = new ArrayList<>();

        boolean subAreaPreference = false;
        boolean statusOrderPreference = false;
        TermTree<NamedArea> areaTree = null;
        TermTree<PresenceAbsenceTerm> statusTree = null;
        Set<MarkerType> fallbackAreaMarkerTypes = null;
        DistributionInfoConfiguration config = new DistributionInfoConfiguration();
        config.setInfoParts(EnumSet.of(InfoPart.mapUriParams));

        String result = new DistributionInfoBuilder(languages, commonService).build(
                config, distributions, areaTree, statusTree, presenceAbsenceColorMap,
                mapping).getMapUriParams();
        logger.warn(result);
        Assert.assertTrue("WebServiceUrl must contain country part for Germany, but was " + result,
                result.matches(".*ad=country_earth(%3A|:)gmi_cntry:.:DEU.*"));
    }

    @Test
    public void testGetWebServiceUrlTdwg() throws MalformedURLException, IOException {
        //String webServiceUrl = "http://www.test.de/webservice";
        Set<Distribution> distributions = new HashSet<>();
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("SPA"), PresenceAbsenceTerm.PRESENT()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("GER"), PresenceAbsenceTerm.INTRODUCED()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("14"), PresenceAbsenceTerm.CULTIVATED()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("BGM"), PresenceAbsenceTerm.ABSENT()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("FRA"), PresenceAbsenceTerm.ABSENT()));
        distributions.add(Distribution.NewInstance(termService.getAreaByTdwgAbbreviation("IND-AP"), PresenceAbsenceTerm.PRESENT()));

        Map<PresenceAbsenceTerm, Color> presenceAbsenceColorMap = new HashMap<>();
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.PRESENT(), Color.BLUE);
        //we take the color for "introduced from the term default color (#ff7f00)
        //to test the correct fallback behavior
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.CULTIVATED(), Color.YELLOW);
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.ABSENT(), Color.DARK_GRAY);
//      String backLayer ="";
//      String bbox="-20,0,120,70";
        List<Language> languages = new ArrayList<>();

//        boolean subAreaPreference = false;
//        boolean statusOrderPreference = false;
        String result = builder.buildFromEntities(
                distributions,
                mapping,
                presenceAbsenceColorMap,
                null, // projectToLayer
                languages );
        //TODO Set semantics is not determined
        //String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";
        logger.debug(result);
        assertTrue("but is: " + result, result.matches(".*ad=tdwg[1-4].*"));
        assertTrue("but is: " + result, result.matches(".*tdwg2:[a-d]:14[\\|&].*") );
        assertTrue("but is: " + result, result.matches(".*[a-d]:FRA,BGM[\\|&].*") || result.matches(".*[a-d]:BGM,FRA[\\|&].*") );
        assertTrue("but is: " + result, result.matches(".*[a-d]:GER[\\|&].*") );
        assertTrue("but is: " + result, result.matches(".*[a-d]:SPA[\\|&].*") );
        assertTrue("but is: " + result, result.matches(".*tdwg4:[a-d]:INDAP[\\|&].*") );

        //colors
        assertTrue("should include #ffff00 (yellow) for cultivated. But was: " + result, result.matches(".*[a-d]:ffff00,,0\\.1,.*"));
        assertTrue("should include #404040 (dark grey) for absent. But was: " + result, result.matches(".*[a-d]:404040,,0\\.1,.*"));
        assertTrue("should include #ff7f00 (flush orange) for introduced. But was: " + result, result.matches(".*[a-d]:ff7f00,,0\\.1,.*"));
        assertTrue("should include #0000ff (blue) for present. But was: " + result, result.matches(".*[a-d]:0000ff,,0\\.1,.*"));

        //assertTrue(result.matches("0000ff"));
        //TODO continue

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

    @Test
    public void testGetWebServiceUrlBangka() throws ClientProtocolException, IOException {

        NamedArea areaBangka = NamedArea.NewInstance("Bangka", "Bangka", null);
        TermVocabulary<NamedArea> voc = TermVocabulary.NewInstance(TermType.NamedArea,
                NamedArea.class, "test Voc", "test voc", null, null);
        voc.addTerm(areaBangka);

        GeoServiceArea geoServiceArea = new GeoServiceArea();
        String geoServiceLayer="vmap0_as_bnd_political_boundary_a";
        String layerFieldName ="nam";
        String areaValue = "PULAU BANGKA#SUMATERA SELATAN";
        geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
        geoServiceArea.add(geoServiceLayer, layerFieldName, "BALI");
        mapping.set(areaBangka, geoServiceArea);

        Set<Distribution> distributions = new HashSet<>();
        distributions.add(Distribution.NewInstance(areaBangka, PresenceAbsenceTerm.PRESENT()));

        Map<PresenceAbsenceTerm,Color> presenceAbsenceColorMap = new HashMap<>();
        presenceAbsenceColorMap.put(PresenceAbsenceTerm.PRESENT(), Color.BLUE);

        List<Language> languages = new ArrayList<>();

        String result = builder.buildFromEntities(distributions,
                mapping,
                presenceAbsenceColorMap, null, languages );

        //TODO Set semantics is not determined
        //String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";

        logger.debug(result);
        assertTrue("but is: " + result, result.matches(".*ad=vmap0_as_bnd_political_boundary_a%3Anam:.*"));
        assertTrue("but is: " + result, result.matches(".*(PULAU\\+BANGKA%23SUMATERA\\+SELATAN).*") );
        assertTrue("but is: " + result, result.matches(".*(BALI).*") );

        // request map image from webservice
        subTestWithEditMapService(result);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}