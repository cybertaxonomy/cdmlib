// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 * @date 07.04.2016
 *
 */
public class FloraCubaCondensedDistributionComposerTest {

    private static OrderedTermVocabulary<PresenceAbsenceTerm> statusVoc;
    private static OrderedTermVocabulary<NamedArea> cubaAreasVocabualary;
    private static NamedArea cuba;
    private static NamedArea westernCuba;
    private static NamedArea eastCuba;
    private static NamedArea centralCuba;
    private static NamedArea pinarDelRio;
    private static NamedArea holguin;
    private static NamedArea guantanamo;

    private static NamedArea bahamas;
    private static NamedArea oldWorld;

    private static UUID uuidStatusOccasionallyCultivated = UUID.fromString("936c3f9a-6099-4322-9792-0a72c6c2ce25");
    private static UUID uuidStatusNotNativButNaturalised = UUID.fromString("a1e26234-831e-4190-9fe3-011aca09ddba");
    private static UUID uuidStatusDoubtfullyNativeError = UUID.fromString("71b72e24-c2b6-44a5-bdab-39f083bf0f06");


    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DefaultTermInitializer initializer = new DefaultTermInitializer();
        initializer.initialize();

        statusVoc = OrderedTermVocabulary.NewInstance(TermType.PresenceAbsenceTerm);
        statusVoc.addTerm(makeStatusTerm("occasionally cultivated","(c)",false, uuidStatusOccasionallyCultivated));
        statusVoc.addTerm(makeStatusTerm("not native but possibly naturalised","p",false,uuidStatusNotNativButNaturalised));
        statusVoc.addTerm(makeStatusTerm("doubtfully native: reported in error","-d",false,uuidStatusDoubtfullyNativeError));

        makeAreas();
    }


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.ext.geo.FloraCubaCondensedDistributionComposer#createCondensedDistribution(java.util.Collection, java.util.List)}.
     */
    @Test
    public void testCreateCondensedDistribution() {
        FloraCubaCondensedDistributionComposer composer = new FloraCubaCondensedDistributionComposer();

        Set<Distribution> filteredDistributions = new HashSet<Distribution>();
        filteredDistributions.add(Distribution.NewInstance(cuba, PresenceAbsenceTerm.NATURALISED()));
        filteredDistributions.add(Distribution.NewInstance(eastCuba, statusVoc.findTermByUuid(uuidStatusOccasionallyCultivated)));
        filteredDistributions.add(Distribution.NewInstance(pinarDelRio, PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR()));
        filteredDistributions.add(Distribution.NewInstance(holguin, PresenceAbsenceTerm.NATURALISED()));
        filteredDistributions.add(Distribution.NewInstance(bahamas, PresenceAbsenceTerm.NATIVE()));
        filteredDistributions.add(Distribution.NewInstance(oldWorld, PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE()));

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(filteredDistributions, null);
        String condensedString = condensedDistribution.toString();
        System.out.println(condensedString);
    }



    private static boolean makeAreas(){

        //vocabulary
        UUID cubaAreasVocabularyUuid = UUID.fromString("c81e3c7b-3c01-47d1-87cf-388de4b1908c");
        String label = "Cuba Areas";
        String abbrev = null;
        cubaAreasVocabualary = OrderedTermVocabulary.NewInstance(TermType.NamedArea, label, label, abbrev, null);
        cubaAreasVocabualary.setUuid(cubaAreasVocabularyUuid);

        //Cuba
        label = "Cuba";
        abbrev = "Cu";
        UUID uuid = UUID.randomUUID();
        cuba = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Western Cuba
        label = "Western Cuba";
        abbrev = "CuW";
        uuid = UUID.randomUUID();
        westernCuba = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        cuba.addIncludes(westernCuba);

        //Central Cuba
        label = "Central Cuba";
        abbrev = "CuC";
        uuid = UUID.randomUUID();
        centralCuba = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        cuba.addIncludes(centralCuba);

        //East Cuba
        label = "East Cuba";
        abbrev = "CuE";
        uuid = UUID.randomUUID();
        eastCuba = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        cuba.addIncludes(eastCuba);

        //Pinar del Río PR
        label = "Pinar del Río";
        abbrev = "PR*";
        uuid = UUID.randomUUID();
        pinarDelRio = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCuba.addIncludes(pinarDelRio);

        //Artemisa
        label = "Artemisa";
        abbrev = "Art";
        uuid = UUID.randomUUID();
        NamedArea area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCuba.addIncludes(area);

        //Ciudad de la Habana
        label = "Ciudad de la Habana";
        abbrev = "Hab*";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCuba.addIncludes(area);

        //Ciudad de la Habana
        label = "Mayabeque";
        abbrev = "May";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCuba.addIncludes(area);

        //Matanzas Mat
        label = "Matanzas";
        abbrev = "Mat";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCuba.addIncludes(area);

        //Isla de la Juventud IJ
        label = "Isla de la Juventud";
        abbrev = "IJ";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCuba.addIncludes(area);

        //Provinces - Central
        //Villa Clara VC
        label = "Villa Clara";
        abbrev = "VC";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCuba.addIncludes(area);

        //Cienfuegos Ci VC
        label = "Cienfuegos";
        abbrev = "Ci";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCuba.addIncludes(area);

        //Sancti Spiritus SS
        label = "Sancti Spíritus";
        abbrev = "SS";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCuba.addIncludes(area);

        //Ciego de Ávila CA
        label = "Ciego de Ávila";
        abbrev = "CA";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCuba.addIncludes(area);

        //Camagüey Cam
        label = "Camagüey";
        abbrev = "Cam";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCuba.addIncludes(area);

        //Las Tunas LT
        label = "Las Tunas";
        abbrev = "LT";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCuba.addIncludes(area);

        //Provinces - East
        //Granma Gr
        label = "Granma";
        abbrev = "Gr";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        eastCuba.addIncludes(area);

        //Holguín Ho
        label = "Holguín";
        abbrev = "Ho";
        uuid = UUID.randomUUID();
        holguin = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        eastCuba.addIncludes(holguin);

        //Santiago de Cuba SC
        label = "Santiago de Cuba";
        abbrev = "SC";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        eastCuba.addIncludes(area);

        //Guantánamo Gu
        label = "Guantánamo";
        abbrev = "Gu";
        uuid = UUID.randomUUID();
        guantanamo = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        eastCuba.addIncludes(guantanamo);

        //other Greater Antilles (Cuba, Española, Jamaica, Puerto Rico)
        //Española Esp (=Haiti + Dominican Republic)
        label = "Española";
        abbrev = "Esp";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Jamaica Ja
        label = "Jamaica";
        abbrev = "Ja";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Puerto Rico PR
        label = "Puerto Rico";
        abbrev = "PRc";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Lesser Antilles Men
        label = "Lesser Antilles";
        abbrev = "Men";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Bahamas
        label = "Bahamas";
        abbrev = "Bah";
        uuid = UUID.randomUUID();
        bahamas = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Cayman Islands
        label = "Cayman Islands"; //[Trinidad, Tobago, Curaçao, Margarita, ABC Isl. => S. America];
        abbrev = "Cay";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //World
        //N America
        label = "N America"; //(incl. Mexico)
        abbrev = "AmN";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Central America
        label = "Central America";
        abbrev = "AmC";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);


        //S America
        label = "S America";
        abbrev = "AmS";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Old World
        label = "Old World ";
        abbrev = "VM";
        uuid = UUID.randomUUID();
        oldWorld = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        return true;
    }

    private static NamedArea getNamedArea(UUID uuid, String label, String abbrev, TermVocabulary<NamedArea> vocabulary) {
        NamedArea result = NamedArea.NewInstance(label, label, abbrev);
        result.setUuid(uuid);
        vocabulary.addTerm(result);
        result.setIdInVocabulary(abbrev);
        return result;

    }


    private static PresenceAbsenceTerm makeStatusTerm(String desc, String abbrev, boolean absent, UUID uuid) {
        PresenceAbsenceTerm result = PresenceAbsenceTerm.NewPresenceInstance(desc, desc, abbrev);
        result.setAbsenceTerm(absent);
        result.setUuid(uuid);
        return result;
    }


}
