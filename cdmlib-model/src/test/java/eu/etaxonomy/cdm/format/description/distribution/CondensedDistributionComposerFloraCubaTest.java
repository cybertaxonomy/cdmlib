/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description.distribution;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistributionComposer.SymbolUsage;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Tests for {@link FloraCubaCondensedDistributionComposerOld}
 * @author a.mueller
 * @since 07.04.2016
 */
public class CondensedDistributionComposerFloraCubaTest extends TermTestBase {

    private CondensedDistributionConfiguration config;

    private static OrderedTermVocabulary<PresenceAbsenceTerm> statusVoc;
    private static OrderedTermVocabulary<NamedArea> cubaAreasVocabualary;
    private static SetMap<NamedArea,NamedArea> parentAreaMap;

    private static NamedArea cuba;
    private static NamedArea westernCuba;
    private static NamedArea eastCuba;
    private static NamedArea centralCuba;
    private static NamedArea pinarDelRio;
    private static NamedArea artemisa;
    private static NamedArea habana;
    private static NamedArea mayabeque;
    private static NamedArea matanzas;
    private static NamedArea isla_juventud;

    private static NamedArea holguin;
    private static NamedArea guantanamo;

    private static NamedArea bahamas;
    private static NamedArea oldWorld;

    private static UUID uuidStatusOccasionallyCultivated = UUID.fromString("936c3f9a-6099-4322-9792-0a72c6c2ce25");
    private static UUID uuidStatusNotNativButNaturalised = UUID.fromString("a1e26234-831e-4190-9fe3-011aca09ddba");
    private static UUID uuidStatusDoubtfullyNativeError = UUID.fromString("71b72e24-c2b6-44a5-bdab-39f083bf0f06");

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        defineSymbolsForExistingTerms();
        statusVoc = OrderedTermVocabulary.NewInstance(TermType.PresenceAbsenceTerm);
        statusVoc.addTerm(makeStatusTerm("occasionally cultivated","oc","(c)",false, uuidStatusOccasionallyCultivated));
        statusVoc.addTerm(makeStatusTerm("not native but possibly naturalised","p","p", false,uuidStatusNotNativButNaturalised));
        statusVoc.addTerm(makeStatusTerm("doubtfully native: reported in error","df","-d",false,uuidStatusDoubtfullyNativeError));

        makeAreas();
    }

    private static void defineSymbolsForExistingTerms() {
        PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA().setSymbol("\u25CF");
        PresenceAbsenceTerm.NATIVE().setSymbol("");
        PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE().setSymbol("?");
        PresenceAbsenceTerm.PRESENT_DOUBTFULLY().setSymbol("?");
        PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE().setSymbol("d");
        PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR().setSymbol("-");
        PresenceAbsenceTerm.REPORTED_IN_ERROR().setSymbol("-");
        PresenceAbsenceTerm.CASUAL().setSymbol("a");
        PresenceAbsenceTerm.CULTIVATED().setSymbol("c");
        PresenceAbsenceTerm.NATURALISED().setSymbol("n");
        PresenceAbsenceTerm.CULTIVATED_PRESENCE_QUESTIONABLE().setSymbol("?c");
        PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR().setSymbol("-c");
        PresenceAbsenceTerm.NATIVE().setSymbol("");
        //Cuba specific
//      statusSymbols.put(UUID.fromString("936c3f9a-6099-4322-9792-0a72c6c2ce25"), "(c)");
//      //endemic, doubtfully present
//      statusSymbols.put(UUID.fromString("5f954f08-267a-4928-b073-12328f74c187"), "?e");
//      //non-native and doubtfully naturalised
//      statusSymbols.put(UUID.fromString("a1e26234-831e-4190-9fe3-011aca09ddba"), "p");
//      //rare casual
//      statusSymbols.put(UUID.fromString("8914ce0d-7d31-4c88-8317-985f2b3a493b"), "(a)");
//      //?non-native and doubtfully naturalised
//      statusSymbols.put(UUID.fromString("9e0b413b-5a68-4e5b-91f2-227b4f832466"), "?p");
//      //?adventive (casual) alien
//      statusSymbols.put(UUID.fromString("c42ca644-1773-4230-a2ee-328a5d4a21ab"), "?a");
//      //endemic, reported in error
//      statusSymbols.put(UUID.fromString("679b215d-c231-4ee2-ae12-3ffc3dd528ad"), "-e");
//      //naturalised, reported in error
//      statusSymbols.put(UUID.fromString("8d918a37-3add-4e1c-a233-c37dbee209aa"), "-n");
//      //non-native and doubtfully naturalised, reported in error
//      statusSymbols.put(UUID.fromString("b9153d90-9e31-465a-a28c-79077a8ed4c2"), "-p");
//      //adventive alien , reported in error
//      statusSymbols.put(UUID.fromString("9b910b7b-43e3-4260-961c-6063b11cb7dc"), "-a");
//      //doubtfully native: reported in error
//      statusSymbols.put(UUID.fromString("71b72e24-c2b6-44a5-bdab-39f083bf0f06"), "-d");
    }

    @Before
    public void setUp(){
        config = CondensedDistributionConfiguration.NewCubaInstance();
    }

// ********************* TESTS ******************************/

    @Test
    public void testCreateCondensedDistribution() {
        CondensedDistributionComposer composer = new CondensedDistributionComposer();

        Set<Distribution> filteredDistributions = new HashSet<>();
        filteredDistributions.add(Distribution.NewInstance(cuba, PresenceAbsenceTerm.NATURALISED()));
        filteredDistributions.add(Distribution.NewInstance(eastCuba, statusVoc.findTermByUuid(uuidStatusOccasionallyCultivated)));
        filteredDistributions.add(Distribution.NewInstance(westernCuba, statusVoc.findTermByUuid(uuidStatusDoubtfullyNativeError)));
        filteredDistributions.add(Distribution.NewInstance(pinarDelRio, PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR()));
        filteredDistributions.add(Distribution.NewInstance(holguin, PresenceAbsenceTerm.NATURALISED()));
        filteredDistributions.add(Distribution.NewInstance(bahamas, PresenceAbsenceTerm.NATIVE()));
        filteredDistributions.add(Distribution.NewInstance(oldWorld, PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE()));

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                filteredDistributions, parentAreaMap, null, config);
        String condensedString = condensedDistribution.toString();

        Assert.assertEquals("Condensed string for Cuba differs",
                "n<b>Cu</b>(-d<b>CuW</b>(-c<b>PR*</b>) (c)<b>CuE</b>(n<b>Ho</b>))" + config.outOfScopeAreasSeperator + "<b>Bah</b> ?<b>VM</b>",
                condensedString);

        //TODO work in progress
    }

    @Test
    public void testCreateCondensedDistributionOrderSubAreas() {

        CondensedDistributionComposer composer = new CondensedDistributionComposer();

        Set<Distribution> filteredDistributions = new HashSet<>();
        filteredDistributions.add(Distribution.NewInstance(cuba, PresenceAbsenceTerm.NATURALISED()));
        filteredDistributions.add(Distribution.NewInstance(eastCuba, statusVoc.findTermByUuid(uuidStatusOccasionallyCultivated)));
        filteredDistributions.add(Distribution.NewInstance(westernCuba, statusVoc.findTermByUuid(uuidStatusDoubtfullyNativeError)));

        //pinarDelRio, artemisa, habana, mayabeque, matanzas, isla_juventud
        filteredDistributions.add(Distribution.NewInstance(matanzas, PresenceAbsenceTerm.NATIVE()));
        filteredDistributions.add(Distribution.NewInstance(artemisa, PresenceAbsenceTerm.NATIVE()));
        filteredDistributions.add(Distribution.NewInstance(pinarDelRio, PresenceAbsenceTerm.NATIVE()));
        filteredDistributions.add(Distribution.NewInstance(isla_juventud, PresenceAbsenceTerm.NATIVE()));
        filteredDistributions.add(Distribution.NewInstance(mayabeque, PresenceAbsenceTerm.NATIVE()));
        filteredDistributions.add(Distribution.NewInstance(habana, PresenceAbsenceTerm.NATIVE()));

        filteredDistributions.add(Distribution.NewInstance(guantanamo, PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR()));
        filteredDistributions.add(Distribution.NewInstance(holguin, PresenceAbsenceTerm.NATURALISED()));
        filteredDistributions.add(Distribution.NewInstance(bahamas, PresenceAbsenceTerm.NATIVE()));
        filteredDistributions.add(Distribution.NewInstance(oldWorld, PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE()));

        config.areasBold = false;
        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                filteredDistributions, parentAreaMap, null, config);

        Assert.assertEquals("Condensed string for Cuba differs",
                "nCu(-dCuW(PR* Art Hab* May Mat IJ) (c)CuE(nHo -cGu))" + config.outOfScopeAreasSeperator + "Bah ?VM",
                condensedDistribution.toString());

        //TODO work in progress

        //test against default configuration (E+M) to see if the result looks reasonable
        //this should better be done CondensedDistributionComposerEuroMedTest but we have the test data here, therefore we keep the test here
        config = CondensedDistributionConfiguration.NewDefaultInstance();
        config.statusSymbolField = SymbolUsage.Symbol1;
        condensedDistribution = composer.createCondensedDistribution(filteredDistributions, parentAreaMap, null, config);
        Assert.assertEquals("Condensed string for Cuba differs",
                "n (c)CuE -dCuW(<b>Art Hab* IJ Mat May PR*</b>) [(c)CuE(-cGu nHo)]" + config.outOfScopeAreasSeperator + "<b>Bah</b> ?VM",
                condensedDistribution.toString());
        //Note: CuE is handled as both native and introduced as (c) is not yet defined as "introduced" status. Currently
        //      introduced status are hardcoded in PresenceAbsence term. This will probably change in future by becoming an own attribute like "isPresent"
    }

    private static boolean makeAreas(){

        TermTree<NamedArea> areaTree = TermTree.NewInstance(TermType.NamedArea, NamedArea.class);

        //vocabulary
        UUID cubaAreasVocabularyUuid = UUID.fromString("c81e3c7b-3c01-47d1-87cf-388de4b1908c");
        String label = "Cuba Areas";
        String abbrev = null;

        cubaAreasVocabualary = OrderedTermVocabulary.NewOrderedInstance(TermType.NamedArea, NamedArea.class, label, label, abbrev, null);

        cubaAreasVocabualary.setUuid(cubaAreasVocabularyUuid);

        //Cuba
        label = "Cuba";
        abbrev = "Cu";
        UUID uuid = UUID.fromString("d0144a6e-0e17-4a1d-bce5-d464a2aa7229");
        cuba = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        TermNode<NamedArea> cubaNode = areaTree.getRoot().addChild(cuba);

        //Western Cuba
        label = "Western Cuba";
        abbrev = "CuW";
        uuid = UUID.randomUUID();
        westernCuba = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);

        //Central Cuba
        label = "Central Cuba";
        abbrev = "CuC";
        uuid = UUID.randomUUID();
        centralCuba = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        TermNode<NamedArea> centralCubaNode = cubaNode.addChild(centralCuba);

        //East Cuba
        label = "East Cuba";
        abbrev = "CuE";
        uuid = UUID.randomUUID();
        eastCuba = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        TermNode<NamedArea> eastCubaNode = cubaNode.addChild(eastCuba);

        TermNode<NamedArea> westernCubaNode = cubaNode.addChild(westernCuba);

        //Pinar del Río PR
        label = "Pinar del Río";
        abbrev = "PR*";
        uuid = UUID.randomUUID();
        pinarDelRio = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCubaNode.addChild(pinarDelRio);

        //Artemisa
        label = "Artemisa";
        abbrev = "Art";
        uuid = UUID.randomUUID();
        artemisa = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCubaNode.addChild(artemisa);

        //Ciudad de la Habana
        label = "Ciudad de la Habana";
        abbrev = "Hab*";
        uuid = UUID.randomUUID();
        habana = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCubaNode.addChild(habana);

        //Ciudad de la Habana
        label = "Mayabeque";
        abbrev = "May";
        uuid = UUID.randomUUID();
        mayabeque = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCubaNode.addChild(mayabeque);

        //Matanzas Mat
        label = "Matanzas";
        abbrev = "Mat";
        uuid = UUID.randomUUID();
        matanzas = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCubaNode.addChild(matanzas);

        //Isla de la Juventud IJ
        label = "Isla de la Juventud";
        abbrev = "IJ";
        uuid = UUID.randomUUID();
        isla_juventud = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        westernCubaNode.addChild(isla_juventud);

        //Provinces - Central
        //Villa Clara VC
        label = "Villa Clara";
        abbrev = "VC";
        uuid = UUID.randomUUID();
        NamedArea area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCubaNode.addChild(area);

        //Cienfuegos Ci VC
        label = "Cienfuegos";
        abbrev = "Ci";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCubaNode.addChild(area);

        //Sancti Spiritus SS
        label = "Sancti Spíritus";
        abbrev = "SS";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCubaNode.addChild(area);

        //Ciego de Ávila CA
        label = "Ciego de Ávila";
        abbrev = "CA";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCubaNode.addChild(area);

        //Camagüey Cam
        label = "Camagüey";
        abbrev = "Cam";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCubaNode.addChild(area);

        //Las Tunas LT
        label = "Las Tunas";
        abbrev = "LT";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        centralCubaNode.addChild(area);

        //Provinces - East
        //Granma Gr
        label = "Granma";
        abbrev = "Gr";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        eastCubaNode.addChild(area);

        //Holguín Ho
        label = "Holguín";
        abbrev = "Ho";
        uuid = UUID.randomUUID();
        holguin = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        eastCubaNode.addChild(holguin);

        //Santiago de Cuba SC
        label = "Santiago de Cuba";
        abbrev = "SC";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        eastCubaNode.addChild(area);

        //Guantánamo Gu
        label = "Guantánamo";
        abbrev = "Gu";
        uuid = UUID.randomUUID();
        guantanamo = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        eastCubaNode.addChild(guantanamo);

        //other Greater Antilles (Cuba, Española, Jamaica, Puerto Rico)
        //Española Esp (=Haiti + Dominican Republic)
        label = "Española";
        abbrev = "Esp";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(area);

        //Jamaica Ja
        label = "Jamaica";
        abbrev = "Ja";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(area);

        //Puerto Rico PR
        label = "Puerto Rico";
        abbrev = "PRc";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(area);

        //Lesser Antilles Men
        label = "Lesser Antilles";
        abbrev = "Men";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(area);

        //Bahamas
        label = "Bahamas";
        abbrev = "Bah";
        uuid = UUID.randomUUID();
        bahamas = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(bahamas);

        //Cayman Islands
        label = "Cayman Islands"; //[Trinidad, Tobago, Curaçao, Margarita, ABC Isl. => S. America];
        abbrev = "Cay";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(area);

        //World
        //N America
        label = "N America"; //(incl. Mexico)
        abbrev = "AmN";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(area);

        //Central America
        label = "Central America";
        abbrev = "AmC";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(area);

        //S America
        label = "S America";
        abbrev = "AmS";
        uuid = UUID.randomUUID();
        area = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(area);

        //Old World
        label = "Old World ";
        abbrev = "VM";
        uuid = UUID.randomUUID();
        oldWorld = getNamedArea(uuid, label, abbrev, cubaAreasVocabualary);
        areaTree.getRoot().addChild(oldWorld);

        parentAreaMap = areaTree.getParentMap();

        return true;
    }

    private static NamedArea getNamedArea(UUID uuid, String label, String abbrev, TermVocabulary<NamedArea> vocabulary) {
        NamedArea result = NamedArea.NewInstance(label, label, abbrev);
        result.setUuid(uuid);
        vocabulary.addTerm(result);
        result.setIdInVocabulary(abbrev);
        return result;

    }

    private static PresenceAbsenceTerm makeStatusTerm(String desc, String abbrev, String symbol, boolean absent, UUID uuid) {
        PresenceAbsenceTerm result = PresenceAbsenceTerm.NewPresenceInstance(desc, desc, abbrev);
        result.setAbsenceTerm(absent);
        result.setUuid(uuid);
        result.setSymbol(symbol);
        return result;
    }
}