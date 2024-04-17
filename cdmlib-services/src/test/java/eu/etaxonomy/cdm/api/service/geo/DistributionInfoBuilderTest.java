/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionTreeDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.api.dto.portal.config.IAnnotatableLoaderConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermNodeDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermTreeDto;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.portal.DistributionDtoLoader;
import eu.etaxonomy.cdm.api.service.portal.TermTreeDtoLoader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.kohlbecker
 * @since Jan 27, 2014
 */
public class DistributionInfoBuilderTest extends CdmTransactionalIntegrationTest {

    private static final boolean NO_PREFER_AGGREGATED = false;

    private static final UUID uuidGermany = UUID.fromString("cbe7ce69-2952-4309-85dd-0d7d4a4830a1");  //can be changed once Country.uuidGermany is public
    private static final UUID uuidFrance = UUID.fromString("4c49d9d3-6bc3-481a-93c6-c8156cba25fe");
    private static final UUID uuidEurope = UUID.fromString("3b69f979-408c-4080-b573-0ad78a315610");

    @SpringBeanByType
    private ICommonService commonService;

    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @SpringBeanByType
    private ITermService termService;

    private Collection<Distribution> distributions = null;

    private Collection<DistributionDto> filteredDistributions = null;
    private boolean subAreaPreference = false;
    private boolean statusOrderPreference = false;
    private Set<MarkerType> hideMarkedAreas = null;
    private NamedArea berlin = null;

    private Collector<UUID, ?, List<UUID>> toList = Collectors.toList();

    //for distribution tree tests
    private List<DistributionDto> distributionDtos;
    private NamedArea europe;
    private NamedArea westEurope;
    private NamedArea germany;
    private NamedArea bawue;
    private NamedArea saar;
    private NamedArea france;
    private NamedArea ileDeFrance;
    private NamedArea italy;
    private NamedArea spain;

    private Distribution europeDist;
    private Distribution westEuropeDist;
    private Distribution germanyDist;
    private Distribution bawueDist;
    private Distribution berlinDist;
    private Distribution saarDist;
    private Distribution ileDeFranceDist;
    private Distribution italyDist;
    private Distribution spainDist;

    private MarkerType fallbackMarkerType;
    private Reference ref1;

    private Set<UUID> omitLevels;
    private Set<UUID> fallBackAreaMarkerTypes;
    private boolean neverUseFallbackAreasAsParents;
    private Set<UUID> alternativeRootAreaMarkerTypes = null;
    private DistributionOrder distributionOrder;

    private SetMap<NamedAreaDto,NamedAreaDto> parentAreaMap;

    private DistributionDto bawueDistribution;

    private TermTree<NamedArea> areaTree;

    @Before
    public void setup(){  //for testFilterXXX
        distributions = new ArrayList<>();
    }

    //copied from CondensedDistributionComposerEuroMedTest
    public void setupTreeTest(){
        omitLevels = new HashSet<>();
        fallBackAreaMarkerTypes = new HashSet<>();
        neverUseFallbackAreasAsParents = true;   //default according to distribution info tree config
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

        parentAreaMap = TermTreeDtoLoader.getTerm2ParentMap(TermTreeDtoLoader.INSTANCE().fromEntity(areaTree), NamedAreaDto.class);

        distributions = new HashSet<>();

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
        IAnnotatableLoaderConfiguration config = new IAnnotatableLoaderConfiguration() {

            @Override
            public Set<UUID> getMarkerTypes() {
                return new HashSet<>();
            }

            @Override
            public Set<UUID> getAnnotationTypes() {
                return new HashSet<>();
            }
        };
        distributionDtos.clear();
        distributionDtos.add(dist2Dto(europeDist, config));
        distributionDtos.add(dist2Dto(germanyDist, config));
        bawueDistribution = dist2Dto(bawueDist, config);
        distributionDtos.add(bawueDistribution);  //TODO needed?
        distributionDtos.add(dist2Dto(berlinDist, config));
        distributionDtos.add(dist2Dto(italyDist, config));
        distributionDtos.add(dist2Dto(ileDeFranceDist, config));
        distributionDtos.add(dist2Dto(spainDist, config));
        if (westEuropeDist != null) {
            distributionDtos.add(dist2Dto(westEuropeDist, config));
        }
        if (saarDist != null) {
            distributionDtos.add(dist2Dto(saarDist, config));
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

    private DistributionDto dist2Dto(Distribution distribution, IAnnotatableLoaderConfiguration config) {
        return DistributionDtoLoader.INSTANCE().fromEntity(distribution, config);
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

        /* Aggregated elements are preferred over entered or imported elements
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

        //create distribution (introduced) for aggregated description
        TaxonDescription aggregatedDescription = TaxonDescription.NewInstance();
        aggregatedDescription.addType(DescriptionType.AGGREGATED_DISTRIBUTION);
        NamedArea germany = (NamedArea)termService.find(uuidGermany);
        Distribution germanyIntroduced = Distribution.NewInstance(germany, PresenceAbsenceTerm.INTRODUCED());
        aggregatedDescription.addElement(germanyIntroduced);
        distributions.add(germanyIntroduced);
        //... and non-aggregated (native)
        Distribution germanyNative = Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE());
        distributions.add(germanyNative);

        //area tree
        berlin = NamedArea.NewInstance("Berlin", "Berlin", "BER");
        TermTree<NamedArea> areaTree = TermTree.NewInstance(TermType.NamedArea, NamedArea.class);
        TermNode<NamedArea> germanyNode = areaTree.getRoot().addChild(germany);
        germanyNode.addChild(berlin);

        //filter
        statusOrderPreference= true;
        boolean preferAggregated = true;
        TermTree<PresenceAbsenceTerm> statusTree = null;
        filteredDistributions = filterDistributions(distributions,
                areaTree, statusTree, hideMarkedAreas, preferAggregated,
                statusOrderPreference, subAreaPreference, false);

        //test
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals("expecting to see computed status INTRODUCED even it has lower preference than NATIVE",
                PresenceAbsenceTerm.INTRODUCED().getUuid(), filteredDistributions.iterator().next().getStatus().getUuid());

       /* Distributions for parent areas are only
        * removed if direct sub areas have the same status and if subAreaPreference=TRUE
        * which is not the case here.
        */
        TaxonDescription aggParentDescription = TaxonDescription.NewInstance();
        aggParentDescription.addType(DescriptionType.AGGREGATED_DISTRIBUTION);
        Distribution parentComputedDistribution = Distribution.NewInstance(berlin, PresenceAbsenceTerm.INTRODUCED());
        aggParentDescription.addElement(parentComputedDistribution);
        distributions.add(parentComputedDistribution);

        //filter
        filteredDistributions = filterDistributions(
                distributions, areaTree, null, hideMarkedAreas, preferAggregated,
                statusOrderPreference, subAreaPreference, true);

        //test
        Assert.assertEquals(2, filteredDistributions.size());
    }

    public Set<DistributionDto> filterDistributions(Collection<Distribution> distributions,
            TermTree<NamedArea> areaTree, TermTree<PresenceAbsenceTerm> statusTree, Set<MarkerType> hideMarkedAreas,
            boolean preferAggregated, boolean useStatusOrderPreference, boolean doSubAreaPreference,
            boolean keepFallBackOnlyIfNoSubareaDataExists) {

        TermTreeDto areaTreeDto = TermTreeDtoLoader.INSTANCE().fromEntity(areaTree);
        TermTreeDto statusTreeDto = TermTreeDtoLoader.INSTANCE().fromEntity(statusTree);
        SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap = TermTreeDtoLoader.getTerm2NodeMap(areaTreeDto, NamedAreaDto.class);
        SetMap<NamedAreaDto,NamedAreaDto> area2ParentAreaMap = TermTreeDtoLoader.getTerm2ParentMap(areaTreeDto, NamedAreaDto.class);
        Set<UUID> hideMarkerAreaTypeUuids = hideMarkedAreas == null ? null :
            hideMarkedAreas.stream().map(mt->mt.getUuid()).collect(Collectors.toSet());

        Collection<DistributionDto> dists =
                distributions.stream().map(d->new DistributionInfoBuilder(null,null)
                        .toDistributionDto(d)).collect(Collectors.toSet());

        Set<DistributionDto> result = new DistributionInfoBuilder(null, commonService)
                .filterDistributions(dists, areaTreeDto,
                    statusTreeDto, hideMarkerAreaTypeUuids, preferAggregated,
                    useStatusOrderPreference, doSubAreaPreference,
                    keepFallBackOnlyIfNoSubareaDataExists,
                    area2TermNodesMap, area2ParentAreaMap);
        return result;
    }

    @Test
    public void testFilterDistributions_statusOrderPreference(){

        /*
         * Status order preference rule: In case of multiple distribution status
         * (PresenceAbsenceTermBase) for the same area the status with the
         * highest order is preferred, see
         * DefinedTermBase.compareTo(DefinedTermBase)
         */

        //create data
        NamedArea germany = (NamedArea)termService.find(uuidGermany);
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        distributions.add(Distribution.NewInstance(germany, PresenceAbsenceTerm.INTRODUCED()));

        //filter
        statusOrderPreference = true;
        TermTree<NamedArea> areaTree = null;
        TermTree<PresenceAbsenceTerm> statusTree = null;  //Note: currently not yet used for ordering
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree, hideMarkedAreas, false, statusOrderPreference, subAreaPreference, true);

        //Test
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(PresenceAbsenceTerm.NATIVE().getUuid(), filteredDistributions.iterator().next().getStatus().getUuid());
    }

    @Test
    public void testFilterDistributions_subAreaPreference(){

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

        //create data
        NamedArea germany = (NamedArea)termService.find(uuidGermany);
        NamedArea europe = (NamedArea)termService.find(uuidEurope);
        NamedArea france = (NamedArea)termService.find(uuidFrance);

        subAreaPreference = true;
        TermTree<NamedArea> areaTree = TermTree.NewInstance(TermType.NamedArea);
        TermTree<PresenceAbsenceTerm> statusTree = null;

        berlin = NamedArea.NewInstance("Berlin", "Berlin", "BER");
        TermNode<NamedArea> germanyNode = areaTree.getRoot().addChild(germany);
        germanyNode.addChild(berlin);
        //add europe, but not yet as parent of Germany
        TermNode<NamedArea> europeNode = areaTree.getRoot().addChild(europe);

        Distribution distGermany = Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE());
        Distribution distBerlin = Distribution.NewInstance(berlin, PresenceAbsenceTerm.NATIVE());
        distributions.add(distGermany);
        distributions.add(distBerlin);

        //filter
        filteredDistributions = filterDistributions(distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference, subAreaPreference, true);
        //... test
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(berlin.getUuid(), filteredDistributions.iterator().next().getArea().getUuid());

        //add distribution for Europe (not yet as parent of Germany)
        Distribution distEurope = Distribution.NewInstance(europe, PresenceAbsenceTerm.NATIVE());
        distributions.add(distEurope);
        filteredDistributions = filterDistributions(distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference, subAreaPreference, true);
        //... test
        Assert.assertEquals("Europe should be also in", 2, filteredDistributions.size());
        Set<UUID> areaUuids = filteredDistributions.stream().map(d->d.getArea().getUuid()).collect(Collectors.toSet());
        Assert.assertTrue("Europe should be also in", areaUuids.contains(europe.getUuid()));
        Assert.assertTrue("Berlin should still be in", areaUuids.contains(berlin.getUuid()));

        //now add Europe as parent of Germany => Europe should be removed
        europeNode.addChild(germanyNode);
        filteredDistributions = filterDistributions(distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference, subAreaPreference, true);
        //... test
        Assert.assertEquals("Europe should be removed as it is a parent of Germany and ancestor of berlin",
                1, filteredDistributions.size());
        Assert.assertEquals(berlin.getUuid(), filteredDistributions.iterator().next().getArea().getUuid());

        //now remove Germany from distributions => Europe should still be removed as it is an ancestor of berlin
        distributions.remove(distGermany);
        filteredDistributions = filterDistributions(distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference, subAreaPreference, true);
        //... test
        Assert.assertEquals("Europe should be removed as it is an ancestor of berlin", 1, filteredDistributions.size());

        //do not remove other area
        Distribution distFrance = Distribution.NewInstance(france, PresenceAbsenceTerm.NATIVE());
        europeNode.addChild(france);
        distributions.add(distFrance);
        filteredDistributions = filterDistributions(distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference, subAreaPreference, true);
        //... test
        Assert.assertEquals(2, filteredDistributions.size());
    }

    @Test
    public void testFilterDistributions_markedAreaFilter(){

        /*
         * Marked area filter: Skip distributions where the area has a Marker
         * with one of the specified MarkerTypes
         */
        @SuppressWarnings("unchecked")
        TermVocabulary<NamedArea> areaVoc = TermVocabulary.NewInstance(TermType.NamedArea);
        NamedArea germany = NamedArea.NewInstance("Germany", "Germany", "GER");
        areaVoc.addTerm(germany);
        NamedArea france = NamedArea.NewInstance("France", "France", "FRA");
        areaVoc.addTerm(france);
        NamedArea belgium = NamedArea.NewInstance("Belgium", "Belgium", "BEL");
        areaVoc.addTerm(belgium);

        france.addMarker(Marker.NewInstance(MarkerType.IMPORTED(), true));
        france.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
        belgium.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        vocabularyService.save(areaVoc);
        commitAndStartNewTransaction();

        Distribution distGermany = Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE());
        Distribution distFrance = Distribution.NewInstance(france, PresenceAbsenceTerm.NATIVE());
        Distribution distBelgium = Distribution.NewInstance(belgium, PresenceAbsenceTerm.NATIVE());
        distributions.add(distGermany);
        distributions.add(distFrance);
        distributions.add(distBelgium);

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        hideMarkedAreas.add(MarkerType.IMPORTED());
        TermTree<NamedArea> areaTree = null;
        TermTree<PresenceAbsenceTerm> statusTree = null;

        filteredDistributions = filterDistributions(distributions,
                areaTree, statusTree, hideMarkedAreas, false,
                statusOrderPreference, subAreaPreference, true);

        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(germany.getUuid(), filteredDistributions.iterator().next().getArea().getUuid());
    }

    @Test
    public void testFilterDistributions_fallbackArea_hidden(){

        //create data
        @SuppressWarnings("unchecked")
        TermVocabulary<NamedArea> areaVoc = TermVocabulary.NewInstance(TermType.NamedArea);
        NamedArea jugoslavia = NamedArea.NewInstance("", "Former Yugoslavia", "Ju");
        areaVoc.addTerm(jugoslavia);

        NamedArea serbia = NamedArea.NewInstance("", "Serbia", "Sr");
        jugoslavia.addIncludes(serbia);
        areaVoc.addTerm(serbia);
        vocabularyService.save(areaVoc);
        commitAndStartNewTransaction();

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        Distribution distSerbia = Distribution.NewInstance(serbia, PresenceAbsenceTerm.NATIVE());

        distributions.add(distSerbia);
        distributions.add(distJugoslavia);

        // using TO_BE_CHECKED to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        TermTree<NamedArea> areaTree = null;
        TermTree<PresenceAbsenceTerm> statusTree = null;

        boolean keepFallBackOnlyIfNoSubareaDataExists = true;

        //filter
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas,
                NO_PREFER_AGGREGATED,
                statusOrderPreference,
                subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists);

        //test
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(serbia.getUuid(), filteredDistributions.iterator().next().getArea().getUuid());


        //filter
        keepFallBackOnlyIfNoSubareaDataExists = false;
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas,
                NO_PREFER_AGGREGATED,
                statusOrderPreference,
                subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists);

        //test
        Assert.assertEquals(2, filteredDistributions.size());
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea().getUuid()).collect(toList).contains(serbia.getUuid()));
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea().getUuid()).collect(toList).contains(jugoslavia.getUuid()));
    }

    @Test
    public void testFilterDistributions_fallbackArea_recursive(){

        @SuppressWarnings("unchecked")
        TermVocabulary<NamedArea> areaVoc = TermVocabulary.NewInstance(TermType.NamedArea);
        NamedArea jugoslavia = NamedArea.NewInstance("", "Former Yugoslavia", "Ju");
        NamedArea serbia = NamedArea.NewInstance("", "Serbia", "Sr");
        jugoslavia.addIncludes(serbia);
        NamedArea partOfSerbia = NamedArea.NewInstance("", "Part-of-Serbia", "PoS");
        serbia.addIncludes(partOfSerbia);
        areaVoc.addTerm(jugoslavia);
        areaVoc.addTerm(serbia);
        areaVoc.addTerm(partOfSerbia);

        // using TO_BE_CHECKED to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));
        serbia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        vocabularyService.save(areaVoc);
        commitAndStartNewTransaction();

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        Distribution distSerbia = Distribution.NewInstance(serbia, PresenceAbsenceTerm.NATIVE());
        Distribution distPartOfSerbia = Distribution.NewInstance(partOfSerbia, PresenceAbsenceTerm.NATIVE());
        distributions.add(distJugoslavia);

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        TermTree<NamedArea> areaTree = null;
        TermTree<PresenceAbsenceTerm> statusTree = null;

        boolean keepFallBackOnlyIfNoSubareaDataExists = true;
        //filter
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists);

        //test
        Assert.assertEquals(1, filteredDistributions.size());

        //filter
        keepFallBackOnlyIfNoSubareaDataExists = false;
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists);

        //test
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(jugoslavia.getUuid(), filteredDistributions.iterator().next().getArea().getUuid());

        //filter
        keepFallBackOnlyIfNoSubareaDataExists = true;
        distributions.add(distSerbia);
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists);

        //test
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(serbia.getUuid(), filteredDistributions.iterator().next().getArea().getUuid());

        //filter
        keepFallBackOnlyIfNoSubareaDataExists = false;
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists);

        //test
        Assert.assertEquals(2, filteredDistributions.size());
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea().getUuid()).collect(toList).contains(jugoslavia.getUuid()));
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea().getUuid()).collect(toList).contains(serbia.getUuid()));

        //filter
        distributions.add(distPartOfSerbia);
        keepFallBackOnlyIfNoSubareaDataExists = true;
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists);

        //test
        Assert.assertEquals(1, filteredDistributions.size());
        Assert.assertEquals(partOfSerbia.getUuid(), filteredDistributions.iterator().next().getArea().getUuid());

        //filter
        keepFallBackOnlyIfNoSubareaDataExists = false;
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas, NO_PREFER_AGGREGATED,
                statusOrderPreference, subAreaPreference,
                keepFallBackOnlyIfNoSubareaDataExists);

        //test
        Assert.assertEquals(3, filteredDistributions.size());
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea().getUuid()).collect(toList).contains(jugoslavia.getUuid()));
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea().getUuid()).collect(toList).contains(serbia.getUuid()));
        Assert.assertTrue(filteredDistributions.stream().map(d->d.getArea().getUuid()).collect(toList).contains(partOfSerbia.getUuid()));
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
        TermTree<PresenceAbsenceTerm> statusTree = null;

        filteredDistributions = filterDistributions(
                distributions, areaTree,
                statusTree,
                hideMarkedAreas,
                preferAggregated,
                statusOrderPreference,
                subAreaPreference,
                true);

        Assert.assertEquals(0, filteredDistributions.size());
//        Assert.assertEquals(jugoslavia, filteredDistributions.iterator().next().getArea());
    }

    //NOTE: also tests the area tree retrieval
    @Test
    public void testFilterDistributions_fallbackArea_shown_2(){

        //create
        @SuppressWarnings("unchecked")
        TermVocabulary<NamedArea> areaVoc = TermVocabulary.NewInstance(TermType.NamedArea);
        NamedArea jugoslavia = NamedArea.NewInstance("Former Yugoslavia ", "", "Ju");
        jugoslavia.setIdInVocabulary("Ju");
        areaVoc.addTerm(jugoslavia);

        NamedArea serbia = NamedArea.NewInstance("Serbia", "", "Sr");
        serbia.setIdInVocabulary("Sr");
        jugoslavia.addIncludes(serbia);
        areaVoc.addTerm(serbia);
        vocabularyService.save(areaVoc);
        commitAndStartNewTransaction();

        Distribution distJugoslavia = Distribution.NewInstance(jugoslavia, PresenceAbsenceTerm.NATIVE());
        distributions.add(distJugoslavia);
        // no Distribution for any of the sub areas of jugoslavia, so it should be shown

        // using TO_BE_CHECKED as hidden area marker to mark Ju as fallback area
        jugoslavia.addMarker(Marker.NewInstance(MarkerType.TO_BE_CHECKED(), true));

        hideMarkedAreas = new HashSet<>();
        hideMarkedAreas.add(MarkerType.TO_BE_CHECKED());
        TermTree<NamedArea> areaTree = null;
        TermTree<PresenceAbsenceTerm> statusTree = null;

        //filter
        filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas,
                false,
                statusOrderPreference,
                subAreaPreference,
                true);

        //test
        Assert.assertEquals("Ju should be included as subarea has no data", 1, filteredDistributions.size());
        Assert.assertEquals(jugoslavia.getUuid(), filteredDistributions.iterator().next().getArea().getUuid());
    }

    @Test
    public void testFilterDistributions_multipleParents(){
        subAreaPreference = true;
        statusOrderPreference = true;
        TermTree<PresenceAbsenceTerm> statusTree = null;
        boolean keepFallBackOnlyIfNoSubareaDataExists = true;

        setupTreeTest();  //we use tree test setup here
        TermTree<NamedArea> areaTree = this.areaTree;
        createDefaultDistributions();

        //test default (without 2 parents)
        distributions.remove(bawueDist);
        distributions.remove(berlinDist);
        filteredDistributions = filterDistributions(distributions, areaTree,
                statusTree, hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference,
                subAreaPreference, keepFallBackOnlyIfNoSubareaDataExists);
        Assert.assertEquals(4, filteredDistributions.size());
        List<UUID> areaList = filteredDistributions.stream().map(fd->fd.getArea().getUuid()).collect(Collectors.toList());
        Assert.assertTrue(areaList.contains(germany.getUuid()));
        Assert.assertTrue(areaList.contains(ileDeFrance.getUuid()));
        Assert.assertTrue(areaList.contains(italy.getUuid()));
        Assert.assertTrue(areaList.contains(spain.getUuid()));

        //add Saar which is child of Germany and West Europe
        createAndAddSaarAndWestEuropeDistribution();
        filteredDistributions = filterDistributions(distributions, areaTree,
                statusTree, hideMarkedAreas, NO_PREFER_AGGREGATED, statusOrderPreference,
                subAreaPreference, keepFallBackOnlyIfNoSubareaDataExists);
        Assert.assertEquals(4, filteredDistributions.size());
        List<UUID> areaList2 = filteredDistributions.stream().map(fd->fd.getArea().getUuid()).collect(Collectors.toList());
        Assert.assertTrue(areaList2.contains(saar.getUuid()));
        Assert.assertFalse("Should not contain West Europe as it is Saar parent", areaList2.contains(westEurope.getUuid()));
        Assert.assertFalse("Should not contain Germany as it is Saar parent", areaList2.contains(germany.getUuid()));
        Assert.assertTrue(areaList2.contains(ileDeFrance.getUuid()));
        Assert.assertTrue(areaList.contains(italy.getUuid()));
        Assert.assertTrue(areaList.contains(spain.getUuid()));
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
        fallBackAreaMarkerTypes.add(MarkerType.uuidFallbackArea);
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
                + "<West Europe:naturalised{}:0>"  //TODO discuss if it should appear already (even if not having a source)
                + ">",
                tree2String(buildTree(withSecondMethod)));

        //... with allow fallback area as parent
        neverUseFallbackAreasAsParents = false;
        createWesternEuropeDistribution();
        createDefaultDistributionDtos();
        Assert.assertEquals("Tree:1<Europe:endemic{}:4"
                + "<Germany:native{}:2"
                +     "<Baden Wuerttemberg:casual{}:0>"
                +     "<Berlin:native{}:0>>"
                + "<Italy:doubtfully present{}:0>"
                + "<Spain:naturalised{}:0>"
                + "<West Europe:naturalised{}:1"
                +     "<France:-:1<Ile-de-France:cultivated{}:0>>>"
                + ">",
                tree2String(buildTree(withSecondMethod)));
        neverUseFallbackAreasAsParents = true;

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
                distributionDtos, parentAreaMap, TermTreeDtoLoader.INSTANCE().fromEntity(areaTree),
                fallBackAreaMarkerTypes, alternativeRootAreaMarkerTypes,
                neverUseFallbackAreasAsParents, distributionOrder, null, withSecondMethod);
    }

    public String tree2String(DistributionTreeDto tree) {
        StringBuilder sb = new StringBuilder();
        TreeNode<Set<DistributionDto>, NamedAreaDto> root = tree.getRootElement();
        Assert.assertNotNull("root should exist", root);
        Assert.assertNull("root nodeId is null as it does not represent an area", root.getNodeId());
        sb.append("Tree:" + root.getNumberOfChildren());
        for (TreeNode<Set<DistributionDto>,NamedAreaDto> node : root.getChildren()) {
            node2String(node, sb);
        }
//        System.out.println(sb);
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
                        if (!typedLabel.isEmpty()) {  //should never be empty
                            String label = typedLabel.get(0).getLabel();
                            sb.append(label);
                        }
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

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}