/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.ext.geo.CondensedDistributionComposer.SymbolUsage;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 15.06.2016
 */
public class CondensedDistributionComposerEuroMedTest extends TermTestBase {

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
    private List<Language> languages;

    private CondensedDistributionComposer composer;
    private CondensedDistributionConfiguration config;

    private Distribution bawueDistribution;

    @Before
    public void setUp(){

        @SuppressWarnings("unchecked")
        OrderedTermVocabulary<NamedArea>  voc = OrderedTermVocabulary.NewInstance(TermType.NamedArea);
        europe = NamedArea.NewInstance("", "Europe", "EU");
        voc.addTerm(europe);

        westEurope = NamedArea.NewInstance("", "West Europe", "WE");
        westEurope.setPartOf(europe);
        voc.addTerm(westEurope);
        setAsFallback(westEurope);

        //Germany
        germany = NamedArea.NewInstance("", "Germany", "GER");
        germany.setPartOf(europe);
        berlin = NamedArea.NewInstance("", "Berlin", "GER(B)");
        berlin.setPartOf(germany);
        bawue = NamedArea.NewInstance("", "Baden Württemberg", "GER(BW)");
        bawue.setPartOf(germany);
        voc.addTerm(germany);
        voc.addTerm(berlin);
        voc.addTerm(bawue);

        //France
        france = NamedArea.NewInstance("", "France", "FR");
        france.setPartOf(westEurope);
        ileDeFrance = NamedArea.NewInstance("", "Ile-de-France", "FR(J)");
        ileDeFrance.setPartOf(france);
        voc.addTerm(france);
        voc.addTerm(ileDeFrance);

        //Italy
        italy = NamedArea.NewInstance("", "Italy", "IT");
        italy.setPartOf(europe);
        voc.addTerm(italy);

        //Spain
        spain = NamedArea.NewInstance("", "Spain", "S");
        spain.setPartOf(europe);
        voc.addTerm(spain);

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

    @Test
    public void testEuroMedCondensedDistributionDefault() {
        createDefaultDistributions();

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, languages, config);

        Assert.assertEquals("● <b>GER(B BW)</b> ?IT [cFR(J) nS]", condensedDistribution.toString());
    }

    @Test
    public void testEuroMedCondensedDistributionWithDifferingSubareaStatus() {
        createDefaultDistributions();

        bawueDistribution.setStatus(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        Assert.assertEquals("● <b>GER(B</b> dBW<b>)</b> ?IT [cFR(J) nS]", composer.createCondensedDistribution(distributions, languages, config).toString());

        bawueDistribution.setStatus(PresenceAbsenceTerm.CASUAL());
        Assert.assertEquals("● <b>GER(B)</b> ?IT [cFR(J) aGER(BW) nS]", composer.createCondensedDistribution(distributions, languages, config).toString());

        //#9583
        distributions = new HashSet<>();
        distributions.add(Distribution.NewInstance(berlin, PresenceAbsenceTerm.PRESENT_DOUBTFULLY()));
        distributions.add(Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE()));
        Assert.assertEquals("<b>GER(</b>?B <b>BW)</b>", composer.createCondensedDistribution(distributions, languages, config).toString());

    }

    @Test
    public void testEuroMedCondensedDistributionWithParentStatus() {
        createDefaultDistributions();

        distributions.add(Distribution.NewInstance(france, PresenceAbsenceTerm.CASUAL()));

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, languages, config);

        Assert.assertEquals("● <b>GER(B BW)</b> ?IT [aFR(cJ) nS]", condensedDistribution.toString());
    }


    @Test
    public void testEuroMedCondensedDistributionFallback() {
//      distributions.add(Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()));

        Distribution nativeDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE());
        Distribution introducedDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.INTRODUCED());

        distributions.add(nativeDist);
        distributions.add(Distribution.NewInstance(westEurope, PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE()));

        Assert.assertEquals("dFR(J)", composer.createCondensedDistribution(distributions, languages, config).getHtmlString());

        distributions.remove(nativeDist);
        distributions.add(introducedDist);
        Assert.assertEquals("[iFR(J)]", composer.createCondensedDistribution(distributions, languages, config).getHtmlString());


    }

    @Test
    public void testEuroMedCondensedDistributionNotEndemicOrOnlyIntroduced() {

        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        Assert.assertEquals("<b>GER</b>", composer.createCondensedDistribution(
                distributions, languages, config).toString());

        distributions.add(Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMISM_UNKNOWN()));
        Assert.assertEquals("<b>GER</b>", composer.createCondensedDistribution(
                distributions, languages, config).toString());

        distributions.clear();
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.CASUAL()));
        Assert.assertEquals("[aGER]", composer.createCondensedDistribution(
                distributions, languages, config).toString());
    }

    @Test
    public void testCubaCondensedDistributionWithEMTestData() {
        createDefaultDistributions();

        distributions.add(Distribution.NewInstance(france, PresenceAbsenceTerm.CASUAL()));

        CondensedDistributionConfiguration config = CondensedDistributionConfiguration.NewCubaInstance();
        config.areaSymbolField = SymbolUsage.AbbrevLabel;
        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                distributions, languages, config);

        Assert.assertEquals("●<b>EU</b>(<b>GER</b>(<b>GER(B) GER(BW)</b>) a<b>FR</b>(c<b>FR(J)</b>) ?<b>IT</b> n<b>S</b>)", condensedDistribution.toString());
    }
}