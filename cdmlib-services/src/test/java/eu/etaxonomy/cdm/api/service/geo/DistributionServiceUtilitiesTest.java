/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionTreeDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.api.service.portal.PortalDtoLoader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.kohlbecker
 * @since Jan 27, 2014
 */
public class DistributionServiceUtilitiesTest extends TermTestBase {

    private Collection<Distribution> distributions = null;

    private Collection<Distribution> filteredDistributions = null;
    private boolean subAreaPreference = false;
    private boolean statusOrderPreference = false;
    private Set<MarkerType> hideMarkedAreas = null;
    private NamedArea berlin = null;

    private Collector<NamedArea, ?, List<NamedArea>> toList = Collectors.toList();
    private static final boolean NO_PREFER_AGGREGATED = false;

    //for distribution tree tests
    private List<DistributionDto> distributionDtos;
    private NamedArea euroMed;
    private NamedArea europe;
    private NamedArea westEurope;
    private NamedArea germany;
    private NamedArea bawue;
    private NamedArea saar;
    private NamedArea france;
    private NamedArea ileDeFrance;
    private NamedArea italy;
    private NamedArea spain;

    private Distribution euroMedDist;
    private Distribution europeDist;
    private Distribution westEuropeDist;
    private Distribution germanyDist;
    private Distribution bawueDist;
    private Distribution berlinDist;
    private Distribution saarDist;
    private Distribution franceDist;
    private Distribution ileDeFranceDist;
    private Distribution italyDist;
    private Distribution spainDist;

    private MarkerType fallbackMarkerType;
    private Reference ref1;

    private Set<NamedAreaLevel> omitLevels;
    private Set<MarkerType> fallBackAreaMarkerTypes;
    private boolean fallbackWithSourceOnly;
    private boolean neverUseFallbackAreasAsParents;
    private Set<MarkerType> alternativeRootAreaMarkerTypes = null;
    private DistributionOrder distributionOrder;

    private SetMap<NamedArea,NamedArea> parentAreaMap;
    private List<Language> languages;

    private DistributionDto bawueDistribution;

    private TermTree<NamedArea> areaTree;

    @Before
    public void setup(){  //for testFilterXXX
        distributions = new ArrayList<>();

        berlin = NamedArea.NewInstance("Berlin", "Berlin", "BER");
        berlin.setPartOf(Country.GERMANY());
    }

    //copied from CondensedDistributionComposerEuroMedTest
    public void setupTreeTest(){
        omitLevels = new HashSet<>();
        fallBackAreaMarkerTypes = new HashSet<>();
        neverUseFallbackAreasAsParents = false;
        alternativeRootAreaMarkerTypes = new HashSet<>();
        distributionOrder = DistributionOrder.LABEL;

        distributionDtos = new ArrayList<>();
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
        setAsFallback(westEuropeNode, westEurope);

        //Germany
        germany = NamedArea.NewInstance("", "Germany", "GER");
        TermNode<NamedArea> germanyNode = europeNode.addChild(germany);
        berlin = NamedArea.NewInstance("", "Berlin", "GER(B)");
        germanyNode.addChild(berlin);
        bawue = NamedArea.NewInstance("", "Baden Wuerttemberg", "GER(BW)");
        germanyNode.addChild(bawue);
        saar = NamedArea.NewInstance("", "Saarland", "GER(S)");
        germanyNode.addChild(saar);
        voc.addTerm(germany);
        voc.addTerm(berlin);
        voc.addTerm(bawue);
        voc.addTerm(saar);

        //saar is also child of fallback area west europe (which here does not include Germany)
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

        ref1 = ReferenceFactory.newGeneric();
        ref1.setTitleCache("Test Ref1", true);

        parentAreaMap = areaTree.getParentMap();

        distributions = new HashSet<>();

        languages = new ArrayList<>();

//        IDefinedTermDao termDao = null; //TODO
//        loader = new DistributionTreeDtoLoader(termDao);
    }

    private void setAsFallback(TermNode<NamedArea> westEuropeNode, NamedArea westEurope) {
        fallbackMarkerType = MarkerType.NewInstance("Fallback area", "Fallback area", "fba");
        fallbackMarkerType.setUuid(MarkerType.uuidFallbackArea);   //as long as it is not an official CDM marker type yet
        westEuropeNode.addMarker(fallbackMarkerType, true);
        if (westEurope != null) {
            westEurope.addMarker(fallbackMarkerType, true);
        }
    }

    private void createDefaultDistributions() {
        europeDist = Distribution.NewInstance(europe, PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA());
        germanyDist = Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE());
        bawueDist = Distribution.NewInstance(bawue, PresenceAbsenceTerm.NATIVE());
        berlinDist = Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE());
        italyDist = Distribution.NewInstance(italy, PresenceAbsenceTerm.PRESENT_DOUBTFULLY());
        ileDeFranceDist = Distribution.NewInstance(ileDeFrance, PresenceAbsenceTerm.CULTIVATED());
        spainDist = Distribution.NewInstance(spain, PresenceAbsenceTerm.NATURALISED());
        westEuropeDist = null;
        saarDist = null;
        addDefaultDistributions();
    }

    private void createDefaultDistributionDtos() {
        distributionDtos.clear();
        distributionDtos.add(dist2Dto(europeDist, parentAreaMap));
        distributionDtos.add(dist2Dto(germanyDist, parentAreaMap));
        bawueDistribution = dist2Dto(bawueDist, parentAreaMap);
        distributionDtos.add(bawueDistribution);  //TODO needed?
        distributionDtos.add(dist2Dto(berlinDist, parentAreaMap));
        distributionDtos.add(dist2Dto(italyDist, parentAreaMap));
        distributionDtos.add(dist2Dto(ileDeFranceDist, parentAreaMap));
        distributionDtos.add(dist2Dto(spainDist, parentAreaMap));
        if (westEuropeDist != null) {
            distributionDtos.add(dist2Dto(westEuropeDist, parentAreaMap));
        }
        if (saarDist != null) {
            distributionDtos.add(dist2Dto(saarDist, parentAreaMap));
        }
    }

    //only needed for testFilterDistributions_multipleParents which uses these data
    private void addDefaultDistributions() {
        distributions.clear();
        distributions.add(europeDist);
        distributions.add(germanyDist);
        distributions.add(bawueDist);
        distributions.add(berlinDist);
        distributions.add(italyDist);
        distributions.add(ileDeFranceDist);
        distributions.add(spainDist);
        if (westEuropeDist != null) {
            distributions.add(westEuropeDist);
        }
        if (saarDist != null) {
            distributions.add(saarDist);
        }
    }

    private DistributionDto dist2Dto(Distribution distribution, SetMap<NamedArea, NamedArea> parentAreaMap) {
        DistributionDto dto = new DistributionDto(distribution, parentAreaMap);
        PortalDtoLoader.loadBaseData(distribution, dto);
        return dto;
    }

    private void createWesternEuropeDistribution() {
        westEuropeDist = Distribution.NewInstance(westEurope, PresenceAbsenceTerm.NATURALISED());
    }

    private void createAndAddSaarAndWestEuropeDistribution() {
        createWesternEuropeDistribution();
        saarDist = Distribution.NewInstance(saar, PresenceAbsenceTerm.NATURALISED());
        distributions.add(westEuropeDist);
        distributions.add(saarDist);
    }

    @Test
    public void testFilterDistributions_aggregated(){

        /* 1.
         * Aggregated elements are preferred over entered or imported elements
         * if the according flag is set to true.
         * (Aggregated description elements are identified by belonging to descriptions
         * which have the type DescriptionType#AGGREGATED_DISTRIBUTION).
         * This means if a non-aggregated status information exist for the same
         * area for which aggregated data is available, the aggregated data has to be
         * given preference over other data.
         * Note by AM: be aware that according to #5050 the preference of aggregated
         * distributions is not valid anymore (for the E+M use-case). However, the functionality
         * might be interesting for future use-cases.
         */
        TaxonDescription aggregatedDescription = TaxonDescription.NewInstance();
        aggregatedDescription.addType(DescriptionType.AGGREGATED_DISTRIBUTION);

        Distribution germanyNative = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.NATIVE());
        distributions.add(germanyNative);

        Distribution germanyIntroduced = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.INTRODUCED());
        aggregatedDescription.addElement(germanyIntroduced);

        distributions.add(germanyIntroduced);

        statusOrderPreference= true;
        boolean preferAggregated = true;
        TermTree<NamedArea> areaTree = null;
        filteredDistributions = DistributionServiceUtilities.filterDistributions(distributions,
                areaTree, hideMarkedAreas, preferAggregated, statusOrderPreference, subAreaPreference, false, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals("expecting to see computed status INTRODUCED even it has lower preference than NATIVE", PresenceAbsenceTerm.INTRODUCED(), filteredDistributions.iterator().next().getStatus());

       /* distributions for parent areas are only
        * removed if direct sub areas have the same status and if subAreaPreference=TRUE which is not the case here
        */
        TaxonDescription aggParentDescription = TaxonDescription.NewInstance();
        aggParentDescription.addType(DescriptionType.AGGREGATED_DISTRIBUTION);

        Distribution parentComputedDistribution = Distribution.NewInstance(berlin, PresenceAbsenceTerm.INTRODUCED());
        aggParentDescription.addElement(parentComputedDistribution);
        distributions.add(parentComputedDistribution);

        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, hideMarkedAreas, preferAggregated,
                statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(2, filteredDistributions.size());
    }

    @Test
    public void testFilterDistributions_statusOrderPreference(){
        statusOrderPreference = true;
        TermTree<NamedArea> areaTree = null;

        /*
         * Status order preference rule: In case of multiple distribution status
         * (PresenceAbsenceTermBase) for the same area the status with the
         * highest order is preferred, see
         * DefinedTermBase.compareTo(DefinedTermBase)
         */
        distributions.add(Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.NATIVE()));
        distributions.add(Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.INTRODUCED()));
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, hideMarkedAreas, false, statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(PresenceAbsenceTerm.NATIVE(), filteredDistributions.iterator().next().getStatus());
    }

    @Test
    public void testFilterDistributions_subAreaPreference(){
        subAreaPreference = true;
        TermTree<NamedArea> areaTree = null;

        /*
         * Sub area preference rule: If there is an area with a direct sub area
         * and both areas have the same status only the information on
         * the sub area should be reported, whereas the super area should be
         * ignored.
         * TODO Note by AM: originally this test distinguished 3 situation "no",
         * "mixed" and "all" computed. As there was no difference in the results anymore
         * at a certain time, the 2 later ones were deleted from this test, also
         * because they were still using the old concept of a "COMPUTED"-Marker.
         */
        Distribution distGermany = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.NATIVE());
        Distribution distBerlin = Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE());

        distributions.add(distGermany);
        distributions.add(distBerlin);
        filteredDistributions = DistributionServiceUtilities.filterDistributions(distributions, areaTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(berlin, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_markedAreaFilter(){
        /*
         * Marked area filter: Skip distributions where the area has a Marker
         * with one of the specified MarkerTypes
         */
        NamedArea germany = NamedArea.NewInstance("Germany", "Germany", "GER");
        NamedArea france = NamedArea.NewInstance("France", "France", "FRA");
        NamedArea belgium = NamedArea.NewInstance("Belgium", "Belgium", "BEL");

        Distribution distGermany = Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE());
        Distribution distFrance = Distribution.NewInstance(france, PresenceAbsenceTerm.NATIVE());
        Distribution distBelgium = Distribution.NewInstance(belgium, PresenceAbsenceTerm.NATIVE());
        distributions.add(distGermany);
        distributions.add(distFrance);
        distributions.add(distBelgium);

        belgium.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
        france.addMarker(Marker.NewInstance(MarkerType.IMPORTED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        hideMarkedAreas.add(MarkerType.IMPORTED());
        TermTree<NamedArea> areaTree = null;

        filteredDistributions = DistributionServiceUtilities.filterDistributions(distributions,
                areaTree, hideMarkedAreas, false,
                statusOrderPreference, subAreaPreference, true, false);
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(germany, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_fallbackArea_hidden(){

        NamedArea jugoslavia = NamedArea.NewInstance("", "Former Yugoslavia", "Ju");
        NamedArea serbia = NamedArea.NewInstance("", "Serbia", "Sr");
        jugoslavia.addIncludes(serbia);


        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        Distribution distSerbia = Distribution.NewInstance(serbia, PresenceAbsenceTerm.NATIVE());

        distributions.add(distSerbia);
        distributions.add(distJugoslavia);

        // using TO_BE_CHECKED to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        TermTree<NamedArea> areaTree = null;

        boolean keepFallBackOnlyIfNoSubareaDataExists = true;
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree,
                hideMarkedAreas,
                NO_PREFER_AGGREGATED,
                statusOrderPreference,
                subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, false);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(serbia, filteredDistributions.iterator().next().getArea());

        keepFallBackOnlyIfNoSubareaDataExists = false;
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree,
                hideMarkedAreas,
                NO_PREFER_AGGREGATED,
                statusOrderPreference,
                subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, false);

        Assert.assertEquals(2, filteredDistributions.size());
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea()).collect(toList).contains(serbia));
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea()).collect(toList).contains(jugoslavia));
    }

    @Test
    public void testFilterDistributions_fallbackArea_recursive(){

        NamedArea jugoslavia = NamedArea.NewInstance("", "Former Yugoslavia", "Ju");
        NamedArea serbia = NamedArea.NewInstance("", "Serbia", "Sr");
        jugoslavia.addIncludes(serbia);
        NamedArea partOfSerbia = NamedArea.NewInstance("", "Part-of-Serbia", "PoS");
        serbia.addIncludes(partOfSerbia);

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        Distribution distSerbia = Distribution.NewInstance(serbia, PresenceAbsenceTerm.NATIVE());
        Distribution distPartOfSerbia = Distribution.NewInstance(partOfSerbia, PresenceAbsenceTerm.NATIVE());

        distributions.add(distJugoslavia);

        // using TO_BE_CHECKED to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
        serbia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        TermTree<NamedArea> areaTree = null;

        boolean keepFallBackOnlyIfNoSubareaDataExists = true;
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, false);

        Assert.assertEquals(1, filteredDistributions.size());


        keepFallBackOnlyIfNoSubareaDataExists = false;
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, false);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(jugoslavia, filteredDistributions.iterator().next().getArea());

        keepFallBackOnlyIfNoSubareaDataExists = true;
        distributions.add(distSerbia);
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, false);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(serbia, filteredDistributions.iterator().next().getArea());

        keepFallBackOnlyIfNoSubareaDataExists = false;
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, false);
        Assert.assertEquals(2, filteredDistributions.size());
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea()).collect(toList).contains(jugoslavia));
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea()).collect(toList).contains(serbia));

        distributions.add(distPartOfSerbia);
        keepFallBackOnlyIfNoSubareaDataExists = true;
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, false);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(partOfSerbia, filteredDistributions.iterator().next().getArea());

        keepFallBackOnlyIfNoSubareaDataExists = false;
        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, false);
        Assert.assertEquals(3, filteredDistributions.size());
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea()).collect(toList).contains(jugoslavia));
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea()).collect(toList).contains(serbia));
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea()).collect(toList).contains(partOfSerbia));
    }


    @Test
    public void testFilterDistributions_fallbackArea_shown_1(){

        boolean preferAggregated = false;

        NamedArea jugoslavia = NamedArea.NewInstance("Former Yugoslavia ", "", "Ju");
        jugoslavia.setIdInVocabulary("Ju");
        NamedArea serbia = NamedArea.NewInstance("Serbia", "", "Sr");
        serbia.setIdInVocabulary("Sr");
        jugoslavia.addIncludes(serbia);

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        Distribution distSerbia = Distribution.NewInstance(serbia, PresenceAbsenceTerm.NATIVE());

        distributions.add(distSerbia);
        distributions.add(distJugoslavia);

        // using TO_BE_CHECKED to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
        // this hides serbia so jugoslavia does not become fallback but is still hidden area
        serbia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        TermTree<NamedArea> areaTree = null;

        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree,
                hideMarkedAreas,
                preferAggregated,
                statusOrderPreference,
                subAreaPreference,
                true, false);

        Assert.assertEquals(0, filteredDistributions.size());
//        Assert.assertEquals(jugoslavia, filteredDistributions.iterator().next().getArea());
    }

    @Test
    public void testFilterDistributions_fallbackArea_shown_2(){

        NamedArea jugoslavia = NamedArea.NewInstance("Former Yugoslavia ", "", "Ju");
        jugoslavia.setIdInVocabulary("Ju");
        NamedArea serbia = NamedArea.NewInstance("Serbia", "", "Sr");
        serbia.setIdInVocabulary("Sr");
        jugoslavia.addIncludes(serbia);

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        distributions.add(distJugoslavia);
        // no Distribution for any of the sub areas of jugoslavia, so it should be shown

        // using TO_BE_CHECKED as hidden area marker to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        TermTree<NamedArea> areaTree = null;

        filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree,
                hideMarkedAreas,
                false,
                statusOrderPreference,
                subAreaPreference,
                true, false);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(jugoslavia, filteredDistributions.iterator().next().getArea());
    }


    @Test
    public void testFilterDistributions_multipleParents(){
        subAreaPreference = true;
        statusOrderPreference = true;
        boolean ignoreDistributionStatusUndefined = false;
        boolean keepFallBackOnlyIfNoSubareaDataExists = true;

        setupTreeTest();  //we use tree test setup here
        TermTree<NamedArea> areaTree = this.areaTree;
        createDefaultDistributions();

        //test default (without 2 parents)
        distributions.remove(bawueDist);
        distributions.remove(berlinDist);
        filteredDistributions = DistributionServiceUtilities.filterDistributions(distributions, areaTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, ignoreDistributionStatusUndefined);
        Assert.assertEquals(4, filteredDistributions.size());
        List<NamedArea> areaList = filteredDistributions.stream().map(fd->fd.getArea()).collect(toList);
        Assert.assertTrue(areaList.contains(germany));
        Assert.assertTrue(areaList.contains(ileDeFrance));
        Assert.assertTrue(areaList.contains(italy));
        Assert.assertTrue(areaList.contains(spain));

        //add Saar which is child of Germany and West Europe
        createAndAddSaarAndWestEuropeDistribution();
        filteredDistributions = DistributionServiceUtilities.filterDistributions(distributions, areaTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists, ignoreDistributionStatusUndefined);
        Assert.assertEquals(4, filteredDistributions.size());
        List<NamedArea> areaList2 = filteredDistributions.stream().map(fd->fd.getArea()).collect(toList);
        Assert.assertTrue(areaList2.contains(saar));
        Assert.assertFalse("Should not contain West Europe as it is Saar parent", areaList2.contains(westEurope));
        Assert.assertFalse("Should not contain Germany as it is Saar parent", areaList2.contains(germany));
        Assert.assertTrue(areaList2.contains(ileDeFrance));
        Assert.assertTrue(areaList.contains(italy));
        Assert.assertTrue(areaList.contains(spain));
    }

    @Test
    public <TN extends TreeNode<Set<DistributionDto>,NamedAreaDto>> void testBuildOrderedTreeDto() {
        testBuildOrderedTreeDto(false);
        testBuildOrderedTreeDto(true);
    }

    private <TN extends TreeNode<Set<DistributionDto>,NamedAreaDto>> void testBuildOrderedTreeDto(boolean withSecondMethod) {
        setupTreeTest();

//        IDefinedTermDao termDao = null; //TODO
//        DistributionTreeDtoLoader loader = new DistributionTreeDtoLoader(termDao);

        createDefaultDistributions();
        createDefaultDistributionDtos();
        Assert.assertEquals("Tree:1<Europe:endemic{}:4"
                + "<Germany:native{}:2"
                +     "<Baden Wuerttemberg:native{}:0>"
                +     "<Berlin:native{}:0>>"
                + "<Italy:doubtfully present{}:0>"
                + "<Spain:naturalised{}:0>"
                + "<West Europe:-:1<France:-:1<Ile-de-France:cultivated{}:0>>>"
                + ">",
                tree2String(buildTree(withSecondMethod)));

        //with fallback
        fallBackAreaMarkerTypes.add(fallbackMarkerType);
        Assert.assertEquals("Tree:1<Europe:endemic{}:4"
                + "<France:-:1<Ile-de-France:cultivated{}:0>>"
                + "<Germany:native{}:2"
                +     "<Baden Wuerttemberg:native{}:0>"
                +     "<Berlin:native{}:0>>"
                + "<Italy:doubtfully present{}:0>"
                + "<Spain:naturalised{}:0>>",
                tree2String(buildTree(withSecondMethod)));

        //change status
        bawueDist.setStatus(PresenceAbsenceTerm.CASUAL());
        createDefaultDistributionDtos();
        Assert.assertEquals("Tree:1<Europe:endemic{}:4"
                + "<France:-:1<Ile-de-France:cultivated{}:0>>"
                + "<Germany:native{}:2"
                +     "<Baden Wuerttemberg:casual{}:0>"
                +     "<Berlin:native{}:0>>"
                + "<Italy:doubtfully present{}:0>"
                + "<Spain:naturalised{}:0>>",
                tree2String(buildTree(withSecondMethod)));

        //add fallback status
        createWesternEuropeDistribution();
        createDefaultDistributionDtos();
        Assert.assertEquals("Tree:1<Europe:endemic{}:5"
                + "<France:-:1<Ile-de-France:cultivated{}:0>>"
                + "<Germany:native{}:2"
                +     "<Baden Wuerttemberg:casual{}:0>"
                +     "<Berlin:native{}:0>>"
                + "<Italy:doubtfully present{}:0>"
                + "<Spain:naturalised{}:0>"
                + "<West Europe:naturalised{}:0>"  //TODO discuss if it should appear already
                + ">",
                tree2String(buildTree(withSecondMethod)));

        //add fallback reference
        Reference ref1 = ReferenceFactory.newGeneric();
        ref1.setTitleCache("Test Ref1", true);
        westEuropeDist.addPrimaryTaxonomicSource(ref1, "55");
        createDefaultDistributionDtos();
        Assert.assertEquals("Tree:1<Europe:endemic{}:5"
                + "<France:-:1<Ile-de-France:cultivated{}:0>>"
                + "<Germany:native{}:2"
                +     "<Baden Wuerttemberg:casual{}:0>"
                +     "<Berlin:native{}:0>>"
                + "<Italy:doubtfully present{}:0>"
                + "<Spain:naturalised{}:0>"
                + "<West Europe:naturalised{Test Ref1: 55}:0>"
                + ">",
                tree2String(buildTree(withSecondMethod)));
    }

    private DistributionTreeDto buildTree(boolean withSecondMethod) {
        return DistributionServiceUtilities.buildOrderedTreeDto(omitLevels,
                distributionDtos, parentAreaMap, areaTree,
                fallBackAreaMarkerTypes, alternativeRootAreaMarkerTypes,
                neverUseFallbackAreasAsParents, distributionOrder, null, withSecondMethod);
    }

    private String tree2String(DistributionTreeDto tree) {
        StringBuilder sb = new StringBuilder();
        TreeNode<Set<DistributionDto>, NamedAreaDto> root = tree.getRootElement();
        Assert.assertNotNull("root should exist", root);
        Assert.assertNull("root nodeId is null as it does not represent an area", root.getNodeId());
        sb.append("Tree:" + root.getNumberOfChildren());
        for (TreeNode<Set<DistributionDto>,NamedAreaDto> node : root.getChildren()) {
            node2String(node, sb);
        }
        return sb.toString();
    }

    private void node2String(TreeNode<Set<DistributionDto>,NamedAreaDto> node, StringBuilder sb) {

        sb.append("<")
          .append(node.getNodeId().getLabel()+":");
        if (CdmUtils.isNullSafeEmpty(node.getData())) {
            sb.append("-");
        }else {
            Set<DistributionDto> data = node.getData();
            boolean isFirst = true;
            for (DistributionDto date : data) {
                if (!isFirst) {
                    sb.append("|");
                }
                sb.append(date.getStatus().getLabel());
                sb.append("{");
                if (date.getSources() != null) {
                    for (SourceDto source : date.getSources().getItems()) {
                        List<TypedLabel> typedLabel = source.getLabel();
                        String label = typedLabel.get(0).getLabel();
                        sb.append(label);
                    }
                }
                sb.append("}");
                isFirst = false;
            }
        }
        sb.append(":" + node.getNumberOfChildren());
        for (TreeNode<Set<DistributionDto>,NamedAreaDto> child : node.getChildren()) {
            node2String(child, sb);
        }
        sb.append(">");
    }
}