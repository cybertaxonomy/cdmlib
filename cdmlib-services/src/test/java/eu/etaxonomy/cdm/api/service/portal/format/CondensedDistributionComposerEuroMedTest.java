/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal.format;

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

import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistribution;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.SymbolUsage;
import eu.etaxonomy.cdm.common.UTF8;
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
    private NamedArea saar;
    private NamedArea france;
    private NamedArea ileDeFrance;
    private NamedArea italy;
    private NamedArea spain;

    private Set<Distribution> distributions;
    private TermTree<NamedArea> areaTree;
    private List<Language> languages;

    private CondensedDistributionComposer composer;
    private CondensedDistributionConfiguration config;

    private Distribution bawueDistribution;

    private static final String endemic = UTF8.BLACK_CIRCLE.toString();

    private Map<UUID,PresenceAbsenceTerm> iucnStatus;

    @Before
    public void setUp(){

        areaTree = TermTree.NewInstance(TermType.NamedArea, NamedArea.class);

        @SuppressWarnings("unchecked")
        OrderedTermVocabulary<NamedArea>  voc = OrderedTermVocabulary.NewInstance(TermType.NamedArea);
        europe = NamedArea.NewInstance("", "Europe", "EU");
        voc.addTerm(europe);
        TermNode<NamedArea> europeNode = areaTree.getRoot().addChild(europe);

        //fallback
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
        saar = NamedArea.NewInstance("", "Saarland", "GER(S)");
        germanyNode.addChild(saar);
        voc.addTerm(germany);
        voc.addTerm(berlin);
        voc.addTerm(bawue);
        voc.addTerm(saar);

        //saar is also child of fallback area West Europe (which here does not include Germany)
        TermNode<NamedArea> saarWE = westEuropeNode.addChild(saar);

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

//        areaTree = areaTree.getTerm2NodeMap();

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

    private void removeFallback(NamedArea area) {
        area.removeMarker(MarkerType.uuidFallbackArea);
    }

    private void setAsAlternativeRootNotFallback(NamedArea area) {
        area.removeMarker(MarkerType.uuidFallbackArea);
        MarkerType alternativeRootAreaMarkerType = MarkerType.NewInstance("Alternative root", "Alternative root", "ara");
        alternativeRootAreaMarkerType.setUuid(MarkerType.uuidAlternativeRootArea);   //as long as it is not an official CDM marker type yet
        area.addMarker(alternativeRootAreaMarkerType, true);
    }

    private void createDefaultDistributions() {
        PresenceAbsenceTerm endemicStatus = PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA();
        Assert.assertEquals(endemic, endemicStatus.getSymbol());
        distributions.add(Distribution.NewInstance(europe, endemicStatus));
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        bawueDistribution = Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE());
        distributions.add(bawueDistribution);

        distributions.add(Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE()));

        distributions.add(Distribution.NewInstance(italy, PresenceAbsenceTerm.PRESENT_DOUBTFULLY()));

        distributions.add(Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.CULTIVATED()));

        distributions.add(Distribution.NewInstance(spain, PresenceAbsenceTerm.NATURALISED()));
    }

    private void createIucnDistributions() {
        iucnStatus = createIucnStatus();  //may be removed once iucn status are part of test data

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
                distributions, areaTree, languages, config);

        Assert.assertEquals(endemic + " <b>GER(B BW)</b> ?IT [cFR(J) nS]", condensedDistribution.toString());
    }

    @Test
    public void testEuroMedCondensedDistributionWithDifferingSubareaStatus() {
        createDefaultDistributions();

        bawueDistribution.setStatus(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        Assert.assertEquals(endemic + " <b>GER(B</b> dBW<b>)</b> ?IT [cFR(J) nS]",
                composer.createCondensedDistribution(distributions, areaTree, languages, config).toString());

        bawueDistribution.setStatus(PresenceAbsenceTerm.CASUAL());
        Assert.assertEquals(endemic +" <b>GER(B)</b> ?IT [cFR(J) aGER(BW) nS]",
                composer.createCondensedDistribution(distributions, areaTree, languages, config).toString());

        //#9583
        distributions = new HashSet<>();
        distributions.add(Distribution.NewInstance(berlin, PresenceAbsenceTerm.PRESENT_DOUBTFULLY()));
        distributions.add(Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE()));
        Assert.assertEquals("<b>GER(</b>?B <b>BW)</b>",
                composer.createCondensedDistribution(distributions, areaTree, languages, config).toString());

    }

    @Test
    public void testEuroMedCondensedDistributionWithParentStatus() {
        createDefaultDistributions();

        distributions.add(Distribution.NewInstance(france, PresenceAbsenceTerm.CASUAL()));

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, areaTree, languages, config);

        Assert.assertEquals(endemic + " <b>GER(B BW)</b> ?IT [aFR(cJ) nS]", condensedDistribution.toString());
    }

    @Test
    public void testEuroMedCondensedDistributionFallback() {
//      distributions.add(Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()));

        Distribution nativeDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        Distribution introducedDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.INTRODUCED());

        distributions.add(nativeDist);
        distributions.add(Distribution.NewInstance(westEurope, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE()));

        Assert.assertEquals("dFR(J)", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        distributions.remove(nativeDist);
        distributions.add(introducedDist);
        Assert.assertEquals("[iFR(J)]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());
    }

    @Test
    public void testEuroMedCondensedDistributionWithDuplicateNodes() {

        distributions.add(Distribution.NewInstance(westEurope, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE()));
//        Assert.assertEquals("Fallback WE should show up only if none of the childs has data"
//                ,"dWE", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        saar.getRepresentations().iterator().next().setAbbreviatedLabel("S");
        Distribution saarDist = Distribution.NewInstance(saar, PresenceAbsenceTerm.NATIVE());
        distributions.add(saarDist);
        Assert.assertEquals("Fallback WE should show up only if none of the children has data",
                "<b>GER(S)</b>", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());
        setAsFallback(germany);
        Assert.assertEquals("Also GER should not show up anymore",
                "<b>S</b>", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());
        removeFallback(germany);

        Distribution nativeFranceDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        distributions.add(nativeFranceDist);
        Assert.assertEquals("Still the same",
                "dFR(J) <b>GER(S)</b>", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        saarDist.setStatus(PresenceAbsenceTerm.INTRODUCED());
        nativeFranceDist.setStatus(PresenceAbsenceTerm.CULTIVATED());
        Assert.assertEquals("Still WE should not show up though it has native data.",
                "[cFR(J) iGER(S)]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());



//        Distribution introducedDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.INTRODUCED());
//
//        distributions.add(nativeDist);
//        distributions.add(Distribution.NewInstance(westEurope, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE()));
//
//        Assert.assertEquals("dFR(J)", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());
//
//        distributions.remove(nativeDist);
//        distributions.add(introducedDist);
//        Assert.assertEquals("[iFR(J)]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

    }

    @Test
    public void testEuroMedCondensedDistributionAlternativeRootArea() {
        setAsAlternativeRootNotFallback(westEurope);
        config.alternativeRootAreaMarkers = new HashSet<>();
        config.alternativeRootAreaMarkers.add(MarkerType.uuidAlternativeRootArea);

        //no root
        Distribution nativeDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        distributions.add(nativeDist);
        Assert.assertEquals("dFR(J)", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //only alternative root
        Distribution alternativeRoot = Distribution.NewInstance(westEurope, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA());
        distributions.add(alternativeRoot);
        Assert.assertEquals(endemic + " dFR(J)", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //both roots, this behavior may change in future
        Distribution realRoot = Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA());
        distributions.add(realRoot);
        Assert.assertEquals("For now we do not handle a non empty alternative root as alternative root",
                endemic +" " + endemic + "WE(dFR(J))", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //only real root
        distributions.remove(alternativeRoot);
        Assert.assertEquals(endemic + " dFR(J)", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //with introduced distribution
        //..only real root
        Distribution introducedDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.INTRODUCED());
        distributions.add(introducedDist);
        distributions.remove(nativeDist);
        Assert.assertEquals(endemic + " [iFR(J)]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //..with both roots, this may change in future (and does not make sense in reality)
        distributions.add(alternativeRoot);
        Assert.assertEquals(endemic +" " + endemic + "WE [iWE(FR(J))]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //..only with alternative root
        distributions.remove(realRoot);
        Assert.assertEquals(endemic + " [iFR(J)]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //.. with no root
        distributions.remove(alternativeRoot);
        Assert.assertEquals("[iFR(J)]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());


        //add sibbling to alternative root
        //and do the same again as above
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        bawueDistribution = Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE());
        distributions.add(bawueDistribution);
        distributions.add(nativeDist);
        distributions.remove(introducedDist);

        //no root
        Assert.assertEquals("dFR(J) <b>GER(BW)</b>", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //only alternative root, should not happen, as the alternative should never have data when a sibbling also has data
        distributions.add(alternativeRoot);
        Assert.assertEquals("<b>GER(BW)</b> " + endemic + "WE(dFR(J))", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //both roots, this behavior may change in future and should not happen, only root or alt. root should have data
        distributions.add(realRoot);
        Assert.assertEquals("For now we do not handle a non empty alternative root as alternative root",
                endemic +" <b>GER(BW)</b> " + endemic + "WE(dFR(J))", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //only real root
        distributions.remove(alternativeRoot);
        Assert.assertEquals(endemic + " dFR(J) <b>GER(BW)</b>", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //with introduced distribution
        //..only real root
        distributions.add(introducedDist);
        distributions.remove(nativeDist);
        Assert.assertEquals(endemic + " <b>GER(BW)</b> [iFR(J)]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //..with both roots, this may change in future (and does not make sense in reality)
        distributions.add(alternativeRoot);
        Assert.assertEquals(endemic +" <b>GER(BW)</b> " + endemic + "WE [iWE(FR(J))]", composer.createCondensedDistribution(distributions, areaTree, languages, config).getHtmlString());

        //..only with alternative root, does not make much sense (see above)
        distributions.remove(realRoot);
        Assert.assertEquals("<b>GER(BW)</b> " + endemic + "WE [iFR(J)]", composer.createCondensedDistribution(distributions,
                areaTree, languages, config).getHtmlString());

        //.. with no root
        distributions.remove(alternativeRoot);
        Assert.assertEquals("<b>GER(BW)</b> [iFR(J)]", composer.createCondensedDistribution(distributions,
                areaTree, languages, config).getHtmlString());

    }

    @Test
    public void testEuroMedCondensedDistributionNotEndemicOrOnlyIntroduced() {

        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        Assert.assertEquals("<b>GER</b>", composer.createCondensedDistribution(
                distributions, areaTree, languages, config).toString());

        distributions.add(Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMISM_UNKNOWN()));
        Assert.assertEquals("<b>GER</b>", composer.createCondensedDistribution(
                distributions, areaTree, languages, config).toString());

        distributions.clear();
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.CASUAL()));
        Assert.assertEquals("[aGER]", composer.createCondensedDistribution(
                distributions, areaTree, languages, config).toString());
    }

    @Test
    public void testCubaCondensedDistributionWithEMTestData() {
        createDefaultDistributions();

        distributions.add(Distribution.NewInstance(france, PresenceAbsenceTerm.CASUAL()));

        CondensedDistributionConfiguration config = CondensedDistributionConfiguration.NewCubaInstance();
        config.areaSymbolField = SymbolUsage.AbbrevLabel;
        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, areaTree, languages, config);

        Assert.assertEquals(endemic + "<b>EU</b>(<b>GER</b>(<b>GER(B) GER(BW)</b>) a<b>FR</b>(c<b>FR(J)</b>) ?<b>IT</b> n<b>S</b>)", condensedDistribution.toString());
    }

    @Test
    public void testEuroMedCondensedIucnDistribution() {
        createIucnDistributions();

        config = CondensedDistributionConfiguration.NewIucnInstance();
        config.areaSymbolField = SymbolUsage.AbbrevLabel;
        config.statusSymbolField = SymbolUsage.Symbol1;


        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, areaTree, languages, config);
        Assert.assertEquals("CR FR(J:CR) GER:RE(B:RE BW:CR) IT:LC S:RE", condensedDistribution.toString());

        distributions.add(Distribution.NewInstance(france, iucnStatus.get(uuidCriticallyEndangered)));
        condensedDistribution = composer.createCondensedDistribution(
                distributions, areaTree, languages, config);
        Assert.assertEquals("CR FR:CR(J:CR) GER:RE(B:RE BW:CR) IT:LC S:RE", condensedDistribution.toString());

    }
}