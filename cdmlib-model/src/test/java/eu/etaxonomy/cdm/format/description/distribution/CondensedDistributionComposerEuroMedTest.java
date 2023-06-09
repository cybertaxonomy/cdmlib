/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistributionComposer.SymbolUsage;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 15.06.2016
 */
public class CondensedDistributionComposerEuroMedTest extends TermTestBase {

    private UUID uuidRegionallyExtinct = UUID.fromString("bb52103b-78de-4a55-9c34-6f9e315d3783");
    private UUID uuidCriticallyEndangered = UUID.fromString("d37660ac-9848-4008-af30-cf7c71962414");
    private UUID uuidEndangered = UUID.fromString("be71cfb4-cbc1-4367-b0f7-91d5a38aa2ce");
    private UUID uuidLeastConcern = UUID.fromString("658580d8-78be-462b-bd03-cd1fc5625676");

    private NamedArea europe;
    private NamedArea westEurope;
    private NamedArea germany;
    private NamedArea berlin;
    private NamedArea bawue;
    private NamedArea france;
    private NamedArea ileDeFrance;
    private NamedArea italy;
    private NamedArea spain;

    private Set<Distribution> distributions;
    private SetMap<NamedArea,NamedArea> parentAreaMap;
    private List<Language> languages;

    private CondensedDistributionComposer composer;
    private CondensedDistributionConfiguration config;

    private Distribution bawueDistribution;

    private static final String endemic = UTF8.BLACK_CIRCLE.toString();

    @Before
    public void setUp(){

        TermTree<NamedArea> areaTree = TermTree.NewInstance(TermType.NamedArea, NamedArea.class);

        @SuppressWarnings("unchecked")
        OrderedTermVocabulary<NamedArea>  voc = OrderedTermVocabulary.NewInstance(TermType.NamedArea);
        europe = NamedArea.NewInstance("", "Europe", "EU");
        voc.addTerm(europe);
        TermNode<NamedArea> europeNode = areaTree.getRoot().addChild(europe);

        westEurope = NamedArea.NewInstance("", "West Europe", "WE");
        TermNode<NamedArea> westEuropeNode = europeNode.addChild(westEurope);
        voc.addTerm(westEurope);
        setAsFallback(westEurope);

        //Germany
        germany = NamedArea.NewInstance("", "Germany", "GER");
        TermNode<NamedArea> germanyNode = europeNode.addChild(germany);
        berlin = NamedArea.NewInstance("", "Berlin", "GER(B)");
        germanyNode.addChild(berlin);
        bawue = NamedArea.NewInstance("", "Baden Württemberg", "GER(BW)");
        germanyNode.addChild(bawue);
        voc.addTerm(germany);
        voc.addTerm(berlin);
        voc.addTerm(bawue);

        //France
        france = NamedArea.NewInstance("", "France", "FR");
        TermNode<NamedArea> franceNode = westEuropeNode.addChild(france);
        ileDeFrance = NamedArea.NewInstance("", "Ile-de-France", "FR(J)");
        franceNode.addChild(ileDeFrance);
        voc.addTerm(france);
        voc.addTerm(ileDeFrance);

        //Italy
        italy = NamedArea.NewInstance("", "Italy", "IT");
        europeNode.addChild(italy);
        voc.addTerm(italy);

        //Spain
        spain = NamedArea.NewInstance("", "Spain", "S");
        europeNode.addChild(spain);
        voc.addTerm(spain);

        parentAreaMap = areaTree.getParentMap();

        distributions = new HashSet<>();

        languages = new ArrayList<>();

        composer = new CondensedDistributionComposer();
        config = CondensedDistributionConfiguration.NewDefaultInstance();
        config.areaSymbolField = SymbolUsage.AbbrevLabel;
        config.statusSymbolField = SymbolUsage.Symbol1;
    }

    private void setAsFallback(NamedArea area) {
        MarkerType fallbackMarkerType = MarkerType.NewInstance("Fallback area", "Fallback area", "fba");
        fallbackMarkerType.setUuid(MarkerType.uuidFallbackArea);   //as long as it is not an official CDM marker type yet
        area.addMarker(fallbackMarkerType, true);
    }

    private void setAsAlternativeRootNotFallback(NamedArea area) {
        area.removeMarker(MarkerType.uuidFallbackArea);
        MarkerType alternativeRootAreaMarkerType = MarkerType.NewInstance("Alternative root", "Alternative root", "ara");
        alternativeRootAreaMarkerType.setUuid(MarkerType.uuidAlternativeRootArea);   //as long as it is not an official CDM marker type yet
        area.addMarker(alternativeRootAreaMarkerType, true);
    }

    private void createDefaultDistributions() {
        distributions.add(Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()));
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        bawueDistribution = Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE());
        distributions.add(bawueDistribution);

        distributions.add(Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE()));

        distributions.add(Distribution.NewInstance(italy, PresenceAbsenceTerm.PRESENT_DOUBTFULLY()));

        distributions.add(Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.CULTIVATED()));

        distributions.add(Distribution.NewInstance(spain, PresenceAbsenceTerm.NATURALISED()));
    }

    private void createIucnDistributions() {
        Map<UUID,PresenceAbsenceTerm> iucnStatus = createIucnStatus();  //may be removed once iucn status are part of test data

        distributions.add(Distribution.NewInstance(europe, iucnStatus.get(uuidCriticallyEndangered)));
        distributions.add(Distribution.NewInstance(germany, iucnStatus.get(uuidEndangered)));
        distributions.add(Distribution.NewInstance(bawue, iucnStatus.get(uuidCriticallyEndangered)));
        distributions.add(Distribution.NewInstance(berlin, iucnStatus.get(uuidEndangered)));
        distributions.add(Distribution.NewInstance(italy, iucnStatus.get(uuidLeastConcern)));
        distributions.add(Distribution.NewInstance(ileDeFrance, iucnStatus.get(uuidCriticallyEndangered)));
        distributions.add(Distribution.NewInstance(spain, iucnStatus.get(uuidRegionallyExtinct)));
    }

    private Map<UUID,PresenceAbsenceTerm> createIucnStatus() {
        Map<UUID,PresenceAbsenceTerm> result = new HashMap<>();
        addIucnTerm(result, "Regionally Extinct", "RE", uuidRegionallyExtinct);
        addIucnTerm(result, "Critically Endangered", "CR", uuidCriticallyEndangered);
        addIucnTerm(result, "Endangered", "RE", uuidEndangered);
        addIucnTerm(result, "Least Concern", "LC", uuidLeastConcern);
        return result;
    }

    private void addIucnTerm(Map<UUID, PresenceAbsenceTerm> result, String label, String symbol, UUID uuid) {
        PresenceAbsenceTerm iucnTerm = PresenceAbsenceTerm.NewPresenceInstance(label, label, symbol);
        iucnTerm.setUuid(uuid);
        iucnTerm.setIdInVocabulary(symbol);
        iucnTerm.setSymbol(symbol);
        iucnTerm.setSymbol2(symbol);
        result.put(iucnTerm.getUuid(), iucnTerm);
    }

    @Test
    public void testEuroMedCondensedDistributionDefault() {
        createDefaultDistributions();

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, parentAreaMap, languages, config);

        Assert.assertEquals(endemic + " <b>GER(B BW)</b> ?IT [cFR(J) nS]", condensedDistribution.toString());
    }

    @Test
    public void testEuroMedCondensedDistributionWithDifferingSubareaStatus() {
        createDefaultDistributions();

        bawueDistribution.setStatus(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        Assert.assertEquals(endemic + " <b>GER(B</b> dBW<b>)</b> ?IT [cFR(J) nS]",
                composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).toString());

        bawueDistribution.setStatus(PresenceAbsenceTerm.CASUAL());
        Assert.assertEquals(endemic +" <b>GER(B)</b> ?IT [cFR(J) aGER(BW) nS]",
                composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).toString());

        //#9583
        distributions = new HashSet<>();
        distributions.add(Distribution.NewInstance(berlin, PresenceAbsenceTerm.PRESENT_DOUBTFULLY()));
        distributions.add(Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE()));
        Assert.assertEquals("<b>GER(</b>?B <b>BW)</b>",
                composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).toString());

    }

    @Test
    public void testEuroMedCondensedDistributionWithParentStatus() {
        createDefaultDistributions();

        distributions.add(Distribution.NewInstance(france, PresenceAbsenceTerm.CASUAL()));

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, parentAreaMap, languages, config);

        Assert.assertEquals(endemic + " <b>GER(B BW)</b> ?IT [aFR(cJ) nS]", condensedDistribution.toString());
    }


    @Test
    public void testEuroMedCondensedDistributionFallback() {
//      distributions.add(Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()));

        Distribution nativeDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        Distribution introducedDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.INTRODUCED());

        distributions.add(nativeDist);
        distributions.add(Distribution.NewInstance(westEurope, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE()));

        Assert.assertEquals("dFR(J)", composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).getHtmlString());

        distributions.remove(nativeDist);
        distributions.add(introducedDist);
        Assert.assertEquals("[iFR(J)]", composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).getHtmlString());
    }

    @Test
    public void testEuroMedCondensedDistributionAlternativeArea() {
        setAsAlternativeRootNotFallback(westEurope);
        config.alternativeRootAreaMarkers = new HashSet<>();
        config.alternativeRootAreaMarkers.add(MarkerType.uuidAlternativeRootArea);

        Distribution nativeDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        distributions.add(nativeDist);
        Assert.assertEquals("dFR(J)", composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).getHtmlString());

        Distribution alternativeRoot = Distribution.NewInstance(westEurope, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA());
        distributions.add(alternativeRoot);
        Assert.assertEquals(endemic + " dFR(J)", composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).getHtmlString());

        Distribution realRoot = Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA());
        distributions.add(realRoot);
        Assert.assertEquals("For we do not handle a non empty alternative root as alternative root",
                endemic +" " + endemic + "WE(dFR(J))", composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).getHtmlString());

        distributions.remove(alternativeRoot);
        Assert.assertEquals(endemic + " dFR(J)", composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).getHtmlString());

        Distribution introducedDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.INTRODUCED());
        distributions.add(introducedDist);
        distributions.remove(nativeDist);
        Assert.assertEquals(endemic + " [iFR(J)]", composer.createCondensedDistribution(distributions, parentAreaMap, languages, config).getHtmlString());

    }

    @Test
    public void testEuroMedCondensedDistributionNotEndemicOrOnlyIntroduced() {

        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        Assert.assertEquals("<b>GER</b>", composer.createCondensedDistribution(
                distributions, parentAreaMap, languages, config).toString());

        distributions.add(Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMISM_UNKNOWN()));
        Assert.assertEquals("<b>GER</b>", composer.createCondensedDistribution(
                distributions, parentAreaMap, languages, config).toString());

        distributions.clear();
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.CASUAL()));
        Assert.assertEquals("[aGER]", composer.createCondensedDistribution(
                distributions, parentAreaMap, languages, config).toString());
    }

    @Test
    public void testCubaCondensedDistributionWithEMTestData() {
        createDefaultDistributions();

        distributions.add(Distribution.NewInstance(france, PresenceAbsenceTerm.CASUAL()));

        CondensedDistributionConfiguration config = CondensedDistributionConfiguration.NewCubaInstance();
        config.areaSymbolField = SymbolUsage.AbbrevLabel;
        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, parentAreaMap, languages, config);

        Assert.assertEquals(endemic + "<b>EU</b>(<b>GER</b>(<b>GER(B) GER(BW)</b>) a<b>FR</b>(c<b>FR(J)</b>) ?<b>IT</b> n<b>S</b>)", condensedDistribution.toString());
    }

    @Test
    public void testEuroMedCondensedIucnDistribution() {
        createIucnDistributions();

        config = CondensedDistributionConfiguration.NewIucnInstance();
        config.areaSymbolField = SymbolUsage.AbbrevLabel;
        config.statusSymbolField = SymbolUsage.Symbol1;


        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, parentAreaMap, languages, config);

        Assert.assertEquals("CR FR:CR(J) GER:RE(B:RE BW:CR) IT:LC S:RE", condensedDistribution.toString());
    }
}