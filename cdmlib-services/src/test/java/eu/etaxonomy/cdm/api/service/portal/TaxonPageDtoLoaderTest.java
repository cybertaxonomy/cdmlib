/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.dto.portal.CommonNameDto;
import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionTreeDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.IFactDto;
import eu.etaxonomy.cdm.api.dto.portal.IdentifierDto;
import eu.etaxonomy.cdm.api.dto.portal.IndividualsAssociationDto;
import eu.etaxonomy.cdm.api.dto.portal.MediaDto2;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonInteractionDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.HomotypicGroupDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaRepresentationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.NameRelationDTO;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.geo.DistributionInfoBuilderTest;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TemporalData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextFormatter;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author muellera
 * @since 26.02.2024
 */
public class TaxonPageDtoLoaderTest extends CdmTransactionalIntegrationTest {

    private UUID taxonUuid = UUID.fromString("075d1b8c-91d3-4b10-93b7-08ac872d09e8");
    private UUID taxonUuid1 = UUID.fromString("792fcc6a-4854-46bd-a6c5-20b578170d8d");
    private UUID taxonUuid2 = UUID.fromString("d040dfb2-90ce-4777-882f-03b88390e15b");
    private UUID specimenUuid1 = UUID.fromString("b2bc2edc-297b-44d7-96c7-2280a7fb0342");
    private UUID specimenUuid2 = UUID.fromString("c9c69fa0-1179-48e6-b03a-a843048b16e6");
    private UUID mediaUuid1 = UUID.fromString("7e6a7d5e-a579-4ba8-98b0-cd10b48d1c04");

    private UUID statusTreeUuid = UUID.fromString("4f018fe2-97c3-4818-9aa4-7a6ba70fc4a1");
    private UUID areaTreeUuid = UUID.fromString("39cae3d4-36e4-4cc7-84a2-b748bbcbe800");
    private UUID featureTreeUuid = UUID.fromString("d7089eb1-2fdc-4ec7-b5b5-2bb74f8fe578");

    private UUID td4Uuid = UUID.fromString("9582cd0e-caa4-4c4e-a1c6-ec37f8b6df86");
//    453923bb-b2a9-4843-a97f-9f02e6e13fbb
//    28df5e01-0248-40d4-bff0-618a5aae3a30

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private IAgentService agentService;

    @SpringBeanByType
    private IOccurrenceService occurrenceService;

    @SpringBeanByType
    private IDescriptionDao descriptionDao;

    @SpringBeanByType
    private IDescriptionElementDao descriptionElementDao;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private ITermTreeService termTreeService;

    @SpringBeanByType
    private IPortalService portalService;

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testSynonymy() {

        createTestData();
        commitAndStartNewTransaction();

        TaxonPageDtoConfiguration config = new TaxonPageDtoConfiguration();
        CondensedDistributionConfiguration cc = config.getDistributionInfoConfiguration().getCondensedDistributionConfiguration();
        cc.showAreaOfScopeLabel = true;
        config.setWithSpecimens(false);
        config.setTaxonUuid(taxonUuid);
        TaxonPageDto dto = portalService.taxonPageDto(config);

        Assert.assertNotNull(dto);
        List<TaggedText> list = dto.getTaggedName();
        Assert.assertEquals("Genus", list.get(0).getText());
        Assert.assertEquals("Genus species Mill. sec. My secbook", dto.getLabel());
        Assert.assertNull(dto.getKeys());
        //TODO check if there is not some duplication between nameDto and dto
        Assert.assertEquals("Basionym relations are not necessary", null, dto.getName().getRelatedNames());
        //... identifier
        Assert.assertNotNull(dto.getIdentifiers());
        IdentifierDto wfoIdentifier = dto.getIdentifiers().getItems().get(0);
        Assert.assertEquals("wfo-12345", wfoIdentifier.getIdentifier());
        Assert.assertEquals(IdentifierType.uuidWfoNameIdentifier, wfoIdentifier.getTypeUuid());
        Assert.assertEquals("WFO Name Identifier", wfoIdentifier.getType());
        Assert.assertEquals("https://www.worldfloraonline.org/taxon/wfo-12345", wfoIdentifier.getLink());
        Assert.assertNotNull(dto.getIdentifiers());

        //homotypic synonyms
        HomotypicGroupDTO homoSyns = dto.getHomotypicSynonyms();
        Assert.assertEquals(1, homoSyns.getSynonyms().getCount());
        TaxonBaseDto homoSyn = homoSyns.getSynonyms().getItems().get(0);
        Assert.assertEquals("Genusnovus species (Mill.) Noll. syn. sec. My secbook", homoSyn.getLabel());
        //... annotation on name and synonym
        Assert.assertEquals("The synonym must have 2 annotations, a synonym annotation and a name annotation",
                2, homoSyn.getAnnotations().getCount());
        Assert.assertNull("No homotypic syn sec should exist as the syn sec is same as acc sec", homoSyns.getSynSecSources());

        //... related homonym
        Assert.assertEquals(1, homoSyn.getRelatedNames().getCount());
        NameRelationDTO homonymRel = homoSyn.getRelatedNames().getItems().get(0);
        Assert.assertEquals("Genusnovus species Woll.", TaggedTextFormatter.createString(homonymRel.getNameLabel()));
        Assert.assertEquals(1, homonymRel.getAnnotations().getCount());
        Assert.assertEquals("Art. 5", homonymRel.getRuleConsidered());
        Assert.assertEquals("Shenzhen 2017", homonymRel.getCodeEdition());
        Assert.assertEquals("Turland, N.J., Wiersema, J.H., Barrie, F.R., Greuter, W., Hawksworth, D.L., Herendeen, P.S., Knapp, S., Kusber, W.-H., Li, D.-Z., Marhold, K., May, T.W., McNeill, J., Monro, A.M., Prado, J., Price, M.J. & Smith, G.F. (eds.) 2018: International Code of Nomenclature for algae, fungi, and plants (Shenzhen Code), adopted by the Nineteenth International Botanical Congress, Shenzhen, China, July 2017. Regnum Vegetabile 159. – Glash"+UTF8.U_UMLAUT+"tten: Koeltz Botanical Books",
                homonymRel.getCodeEditionSource().getLabel().get(0).getLabel());

        //heterotypic synonyms
        ContainerDto<HomotypicGroupDTO> heteroSynGroups = dto.getHeterotypicSynonymGroups();
        Assert.assertEquals(3, heteroSynGroups.getCount());
        //... first group
        HomotypicGroupDTO firstHeteroSynGroup = heteroSynGroups.getItems().get(0);
        Assert.assertEquals(2, firstHeteroSynGroup.getSynonyms().getCount());
        Assert.assertEquals(2, firstHeteroSynGroup.getSynSecSources().getCount());
        Assert.assertEquals("My hetero sec book: 48, My hetero sec book: 49", TaggedTextFormatter.createString(firstHeteroSynGroup.getTaggedSynSecs()));

        //... second group
        HomotypicGroupDTO secondHeteroSynGroup = heteroSynGroups.getItems().get(1);
        Assert.assertEquals(2, secondHeteroSynGroup.getSynonyms().getCount());
        Assert.assertNull("No syn sec should exist as all syn sec do not exist or are equal with taxon sec",
                secondHeteroSynGroup.getSynSecSources());
        Assert.assertEquals("", TaggedTextFormatter.createString(secondHeteroSynGroup.getTaggedSynSecs()));

        //... third group
        HomotypicGroupDTO thirdHeteroSynGroup = heteroSynGroups.getItems().get(2);
        Assert.assertEquals(2, thirdHeteroSynGroup.getSynonyms().getCount());
        Assert.assertEquals(2, thirdHeteroSynGroup.getSynSecSources().getCount());
        Assert.assertEquals("2012: My book4: 33, 2012: My book4", TaggedTextFormatter.createString(thirdHeteroSynGroup.getTaggedSynSecs()));

        //types

        //misapplications
        //TODO
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testFacts() {

        //create test data
        createTestData();
        commitAndStartNewTransaction();

        //test
        testAllFacts();
        testDistributionWithFilter();
    }

    private void testDistributionWithFilter() {
        //config general
        TaxonPageDtoConfiguration config = new TaxonPageDtoConfiguration();
        DistributionInfoConfiguration distConfig = config.getDistributionInfoConfiguration();
        CondensedDistributionConfiguration cc = distConfig.getCondensedDistributionConfiguration();
        cc.showAreaOfScopeLabel = true;
        config.setWithSpecimens(false);
        config.setTaxonUuid(taxonUuid);
        config.setFeatureTree(featureTreeUuid);

        config.setUseDtoLoading(true);
        testDistributionWithFilterDo(config);
        config.setUseDtoLoading(false);
        testDistributionWithFilterDo(config);
    }

    private void testDistributionWithFilterDo(TaxonPageDtoConfiguration config) {
        DistributionInfoConfiguration distConfig = config.getDistributionInfoConfiguration();

        //with area filter
        distConfig.setAreaTree(areaTreeUuid);
        distConfig.setStatusTree(null);
        testDistributionWithAreaFilter(config);

        //with status filter
        distConfig.setAreaTree(null);
        distConfig.setStatusTree(statusTreeUuid);
        testDistributionWithStatusFilter(config);
    }

    private void testDistributionWithAreaFilter(TaxonPageDtoConfiguration config) {

        TaxonPageDto dto = portalService.taxonPageDto(config);
        Assert.assertTrue("There should be no warnings", CdmUtils.isNullSafeEmpty(dto.getMessages()));

        //facts
        ContainerDto<FeatureDto> features = dto.getTaxonFacts();
        Assert.assertEquals("There should be 1 feature (distribution)",
                1, features.getCount());

        //... distribution
        FeatureDto distributionDto = features.getItems().get(0);
        Assert.assertEquals("Distribution", distributionDto.getLabel());

        ContainerDto<IFactDto> distributions = distributionDto.getFacts();
        Assert.assertEquals(1, distributions.getCount());
        IFactDto distribution = distributions.getItems().get(0);
        Assert.assertEquals(DistributionInfoDto.class.getSimpleName(), distribution.getClazz());
        DistributionInfoDto distributionInfo = (DistributionInfoDto)distribution;

        //... condensed distribution
        Assert.assertEquals("DEU", distributionInfo.getCondensedDistribution().getHtmlString());
        Assert.assertEquals("as=a:4daf4a,,0.1,&ad=country_earth%3Agmi_cntry:a:DEU&title=a:present", distributionInfo.getMapUriParams());

        //...tree
        DistributionTreeDto tree = (DistributionTreeDto)distributionInfo.getTree();
        Assert.assertEquals("Tree:1<Germany:present{Second ref article. – The journal. p 22}:0>", new DistributionInfoBuilderTest().tree2String(tree));
        Assert.assertEquals("Should be Germany only", 1, tree.getRootElement().children.size());
        TreeNode<Set<DistributionDto>, NamedAreaDto> germanyNode = tree.getRootElement().getChildren().get(0);
        Assert.assertEquals("Germany", germanyNode.getNodeId().getLabel());
        DistributionDto germanyDistribution = germanyNode.getData().iterator().next();
        Assert.assertEquals(1, germanyDistribution.getAnnotations().getCount());
        Assert.assertEquals("There should be 1 source (even if it has no name used in source)", 1, germanyDistribution.getSources().getCount());
    }

    private void testDistributionWithStatusFilter(TaxonPageDtoConfiguration config) {
        TaxonPageDto dto = portalService.taxonPageDto(config);
        Assert.assertTrue("There should be no warnings", CdmUtils.isNullSafeEmpty(dto.getMessages()));

        //facts
        ContainerDto<FeatureDto> features = dto.getTaxonFacts();
        Assert.assertEquals("There should be 1 feature (distribution)", 1, features.getCount());

        //... distribution
        FeatureDto distributionDto = features.getItems().get(0);
        Assert.assertEquals("Distribution", distributionDto.getLabel());

        ContainerDto<IFactDto> distributions = distributionDto.getFacts();
        Assert.assertEquals(1, distributions.getCount());
        IFactDto distribution = distributions.getItems().get(0);
        Assert.assertEquals(DistributionInfoDto.class.getSimpleName(), distribution.getClazz());
        DistributionInfoDto distributionInfo = (DistributionInfoDto)distribution;

        //... condensed distribution
        Assert.assertEquals("DEU", distributionInfo.getCondensedDistribution().getHtmlString());
        Assert.assertEquals("as=a:4daf4a,,0.1,&ad=country_earth%3Agmi_cntry:a:DEU&title=a:present", distributionInfo.getMapUriParams());

        //...tree
        DistributionTreeDto tree = (DistributionTreeDto)distributionInfo.getTree();
        Assert.assertEquals("Tree:1<Germany:present{Second ref article. – The journal. p 22}:0>", new DistributionInfoBuilderTest().tree2String(tree));
        Assert.assertEquals("Should be Germany only", 1, tree.getRootElement().children.size());
        TreeNode<Set<DistributionDto>, NamedAreaDto> germanyNode = tree.getRootElement().getChildren().get(0);
        Assert.assertEquals("Germany", germanyNode.getNodeId().getLabel());
        DistributionDto germanyDistribution = germanyNode.getData().iterator().next();
        Assert.assertEquals(1, germanyDistribution.getAnnotations().getCount());
        Assert.assertEquals("There should be 1 source (even if it has no name used in source)", 1, germanyDistribution.getSources().getCount());
    }

    /**
     * Tests all facts without feature tree filter, distribution area filter
     * and distributions status filter
     */
    private void testAllFacts() {

        TaxonPageDtoConfiguration config = new TaxonPageDtoConfiguration();
        DistributionInfoConfiguration distConfig = config.getDistributionInfoConfiguration();

        CondensedDistributionConfiguration cc = distConfig.getCondensedDistributionConfiguration();
        cc.showAreaOfScopeLabel = true;
        config.setWithSpecimens(false);
        config.setTaxonUuid(taxonUuid);
        config.addAnnotationType(AnnotationType.uuidUndefined);
        config.setLanguage(Language.GERMAN());

        //with entity loading
        testAllFactsDo(config, false, false); //with model instance loading
        testAllFactsDo(config, false, true); //with model instance loading and excluded marker
        //with dto loading;
        testAllFactsDo(config, true, false); //with dto loading
        //FIXME #10622 does not yet use dto loading
        testAllFactsDo(config, true, true); //with dto loading and excluded marker
    }

    private void testAllFactsDo(TaxonPageDtoConfiguration config, boolean isDto, boolean isExcludedMarker) {

        config.setUseDtoLoading(isDto);
        if (isExcludedMarker) {
            config.setExcludedFactDatasetMarkerTypes(new HashSet<>(Arrays.asList(new UUID[]{MarkerType.uuidComplete})));
        }else {
            config.setExcludedFactDatasetMarkerTypes(null);
        }

        TaxonPageDto dto = portalService.taxonPageDto(config);
        Assert.assertTrue("There should be no warnings", CdmUtils.isNullSafeEmpty(dto.getMessages()));

        Assert.assertNotNull(dto);
        List<TaggedText> list = dto.getTaggedName();
        Assert.assertEquals("Genus", list.get(0).getText());

        //facts
        ContainerDto<FeatureDto> features = dto.getTaxonFacts();
        Assert.assertEquals("There should be 9 features, distribution and description and common names",
                9, features.getCount());

        //... common taxon name
        int i = 0;
        FeatureDto commonNameDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically. Also as >1 common name exists it should use plural.",
                "Common Names", commonNameDto.getLabel());
        testCommonNames(commonNameDto);

        //... textData ("description")
        FeatureDto descriptionDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically. Also as >1 'Description' exists it should use plural and German as locale was set to German and German representation exists.",
                "Beschreibungen", descriptionDto.getLabel());
        testTextDataAndMedia(descriptionDto, isExcludedMarker);

        //... discussion
        FeatureDto discussionDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically",
                "Discussion", discussionDto.getLabel());
        testTextDataEmpty(discussionDto);

        //... distribution
        FeatureDto distributionDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically",
                "Distribution", distributionDto.getLabel());
        testDistributions(distributionDto, isExcludedMarker);

        //termporal data
        FeatureDto floweringDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically",
                "Flowering Season", floweringDto.getLabel());
        testTemporalData(floweringDto);

        //taxon interaction
        FeatureDto hostPlantDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically",
                "Host Plant", hostPlantDto.getLabel());
        testTaxonInteraction(hostPlantDto);

        //quantitative data("introduction")
        FeatureDto introductionDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically.",
                "Introduction", introductionDto.getLabel());
//        testQuantitativeData();

        //...categorical data ("Life-form")
        FeatureDto lifeFormDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically.",
                "Life-form", lifeFormDto.getLabel());
        testCategoricalData(lifeFormDto, isDto);

        //individuals association
        FeatureDto materialExaminedDto = features.getItems().get(i++);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically",
                "Materials Examined", materialExaminedDto.getLabel());
        testIndividualsAssociation(materialExaminedDto);

        //use data
        //TODO
    }

    private void testCategoricalData(FeatureDto lifeFormDto, boolean isDto) {
        Assert.assertEquals(1, lifeFormDto.getFacts().getCount());
        FactDto lifeForm = (FactDto)lifeFormDto.getFacts().getItems().get(0);
        //FIXME implement for dto
        if (!isDto) {
            Assert.assertEquals("Fact modifying State modifying State1", lifeForm.getTypedLabel().get(0).getLabel());
        }
        //...
    }

    private void testIndividualsAssociation(FeatureDto materialExaminedDto) {
        Assert.assertEquals(2, materialExaminedDto.getFacts().getCount());
        IndividualsAssociationDto materialExamined1 = (IndividualsAssociationDto)materialExaminedDto.getFacts().getItems().get(0);
        IndividualsAssociationDto materialExamined2 = (IndividualsAssociationDto)materialExaminedDto.getFacts().getItems().get(1);
        Assert.assertTrue("Currently we only compare by id. This may change in future",
                materialExamined1.getId()<materialExamined2.getId());
        IndividualsAssociationDto materialExaminedToCheck = (IndividualsAssociationDto)materialExaminedDto.getFacts().getItems().stream()
                .filter(f->((IndividualsAssociationDto)f).getOccurrenceUuid().equals(specimenUuid1))
                .findFirst().get();
        Assert.assertEquals("My specimen", materialExaminedToCheck.getOccurrence());
        Assert.assertEquals(specimenUuid1, materialExaminedToCheck.getOccurrenceUuid());
        //FIXME description can not yet be loaded by DTO only loader, see comment in TaxonFactsDtoLoader.loadFactsPerFeature()
//        Assert.assertEquals("Associated specimen description1", materialExamined1.getDescription());
    }

    private void testTaxonInteraction(FeatureDto hostPlantDto) {
        Assert.assertEquals(2, hostPlantDto.getFacts().getCount());
        TaxonInteractionDto hostPlant1 = (TaxonInteractionDto)hostPlantDto.getFacts().getItems().get(0);
        TaxonInteractionDto hostPlant2 = (TaxonInteractionDto)hostPlantDto.getFacts().getItems().get(1);
        Assert.assertTrue("Currently we only compare by id. This may change in future",
                hostPlant1.getId()<hostPlant2.getId());
        TaxonInteractionDto hostPlantToCheck = (TaxonInteractionDto)hostPlantDto.getFacts().getItems().stream()
                .filter(f->((TaxonInteractionDto)f).getTaxonUuid().equals(taxonUuid1))
                .findFirst().get();
        Assert.assertEquals("Genus species Mill. sec. My secbook", TaggedTextFormatter.createString(hostPlantToCheck.getTaxon()));
        Assert.assertEquals(taxonUuid1, hostPlantToCheck.getTaxonUuid());
        //FIXME description can not yet be loaded by DTO only loader, see comment in TaxonFactsDtoLoader.loadFactsPerFeature()
//        Assert.assertEquals("Taxon interaction description1", hostPlantToCheck.getDescription());
    }

    private void testTemporalData(FeatureDto floweringDto) {
        Assert.assertEquals(2, floweringDto.getFacts().getCount());
        FactDto flowering1 = (FactDto)floweringDto.getFacts().getItems().get(0);
        FactDto flowering2 = (FactDto)floweringDto.getFacts().getItems().get(1);
        Assert.assertTrue("Currently we only compare by id. This may change in future",
                flowering1.getId()<flowering2.getId());
        TypedLabel typedLabel1 = flowering1.getTypedLabel().get(0);
        String label1 = typedLabel1.getLabel();
        String label2 = flowering2.getTypedLabel().get(0).getLabel();
        String expectedLabel = "(10 Mar–)15 Apr–30 Jun(–20 Jul)";
        Assert.assertTrue(expectedLabel + "should be label of either flowering1 or flowering2",
                label1.equals(expectedLabel) || label2.equals(expectedLabel));
        Assert.assertEquals("TemporalData", typedLabel1.getCdmClass());
        Assert.assertNotNull(typedLabel1.getUuid());
    }

    private void testCommonNames(FeatureDto commonNameDto) {
        ContainerDto<IFactDto> commonNames = commonNameDto.getFacts();
        Assert.assertEquals(2, commonNames.getCount());
        CommonNameDto cn1 = (CommonNameDto)commonNames.getItems().get(0);
        CommonNameDto cn2 = (CommonNameDto)commonNames.getItems().get(1);
        Assert.assertEquals("For now common names without sortindex should be ordered alphabetically by name. This may change in future",
                "Meine Blume", cn1.getName());
        Assert.assertEquals("For now common names without sortindex should be ordered alphabetically by name. This may change in future",
                "My flower", cn2.getName());
        Assert.assertNull(cn2.getSources());
        Assert.assertEquals("cn1 should have the source of the indescription", 1, cn1.getSources().getCount());
    }

    private void testTextDataEmpty(FeatureDto discussionDto) {
        ContainerDto<IFactDto> discussions = discussionDto.getFacts();
        Assert.assertEquals(1, discussions.getCount());
        FactDto discussion1 = (FactDto)discussions.getItems().get(0);
        Assert.assertEquals(1, discussion1.getTypedLabel().size());
        TypedLabel typedLabel = discussion1.getTypedLabel().get(0);
        Assert.assertEquals("", typedLabel.getLabel());
        Assert.assertEquals("TextData", typedLabel.getCdmClass());
        Assert.assertNotNull(typedLabel.getUuid());
    }

    private void testTextDataAndMedia(FeatureDto descriptionDto, boolean isExcludedMarker) {

        ContainerDto<IFactDto> descriptions = descriptionDto.getFacts();
        Assert.assertEquals(isExcludedMarker? 3: 4, descriptions.getCount());
        FactDto description1 = (FactDto)descriptions.getItems().get(0);
        FactDto description2 = (FactDto)descriptions.getItems().get(1);
        FactDto NULL_FACT = new FactDto();  //to avoid null-warnings
        FactDto description3 = isExcludedMarker? NULL_FACT: (FactDto)descriptions.getItems().get(2);
        FactDto description4 = (FactDto)descriptions.getItems().get(isExcludedMarker? 2: 3);

        //test sorting
        Assert.assertNull("Current sorting should sort null to the top. This may change in future.",
                description1.getSortIndex());
        Assert.assertNull("Current sorting should sort null to the top. This may change in future.",
                description2.getSortIndex());
        Assert.assertTrue("Current sorting should work on id if no sortIndex is given. This may change in future.",
                description1.getId() < description2.getId());
        //TODO use typed label formatter (once implemented)
        if (!isExcludedMarker) {
            Assert.assertEquals("If sortindex is given it should be used for sorting.",
                    "My fourth description", description3.getTypedLabel().get(0).getLabel());
        }
        Assert.assertEquals("If sortindex is given it should be used for sorting.",
                "My third description", description4.getTypedLabel().get(0).getLabel());

        if (!isExcludedMarker) {
            FactDto td4Fact = description3; //renaming to original name td4 for better understanding
            Assert.assertEquals(td4Uuid, td4Fact.getUuid());
            TypedLabel td4TypedLabel = td4Fact.getTypedLabel().get(0);
            Assert.assertEquals(td4Uuid, td4TypedLabel.getUuid());
            Assert.assertEquals("TextData", td4TypedLabel.getCdmClass());
        }

        FactDto td3Fact = description4; //renaming to original name td3 for better understanding
        Assert.assertEquals(1, td3Fact.getSources().getCount());
        SourceDto source = td3Fact.getSources().getItems().get(0);
        Assert.assertEquals(1, source.getLabel().size());
        TypedLabel sourceTypedLabel = source.getLabel().get(0);
        Assert.assertEquals("DescriptionElementSource", sourceTypedLabel.getCdmClass());

        //media
        ContainerDto<MediaDto2> factMedia = description4.getMedia();
        Assert.assertEquals(2, factMedia.getCount());
        MediaDto2 media1 = factMedia.getItems().stream()
                .filter(m->m.getUuid().equals(mediaUuid1))
                .findFirst().get();
        Assert.assertEquals("Media title", media1.getLabel());  //this is computed from the path, may change in future
        //TODO supplemental data

        Assert.assertEquals(2, media1.getRepresentations().getCount());
        MediaRepresentationDTO rep = media1.getRepresentations().getItems().stream()
                .filter(r->r.getMimeType().equals("JPG2"))
                .findFirst().get();
        Assert.assertEquals("http://media.de/file/rep2.jpg", rep.getUri().toString());
        Assert.assertEquals((Integer)200, rep.getWidth());
        Assert.assertEquals("ImageFile", rep.getClazz());
    }

    private void testDistributions(FeatureDto distributionDto, boolean isExcludedMarker) {
        ContainerDto<IFactDto> distributions = distributionDto.getFacts();
        Assert.assertEquals(1, distributions.getCount());
        IFactDto distribution = distributions.getItems().get(0);
        Assert.assertEquals(DistributionInfoDto.class.getSimpleName(), distribution.getClazz());
        DistributionInfoDto distributionInfo = (DistributionInfoDto)distribution;
        //... condensed distribution
        //TODO maybe the order is not deterministic
        String expectedCondensedHtml = isExcludedMarker ? "DEU": "FRA – DEU";
        Assert.assertEquals(expectedCondensedHtml, distributionInfo.getCondensedDistribution().getHtmlString());
        //TODO probably the order is not deterministic, so we may need to check single parts only, same as in according builder test
        String mapUriParamsStart = distributionInfo.getMapUriParams().substring(0, 62);
        String mapUriParamsEnd = distributionInfo.getMapUriParams().replace(mapUriParamsStart, "");
        if(isExcludedMarker) {
            Assert.assertEquals("as=a:4daf4a,,0.1,&ad=country_earth%3Agmi_cntry:a:DEU&title=a:present", distributionInfo.getMapUriParams());
            //as the filtering generally seems to work we do not further test details here
            return;
        }else {
            Assert.assertTrue("was:" + mapUriParamsStart, "as=a:4daf4a,,0.1,|b:377eb8,,0.1,&ad=country_earth%3Agmi_cntry:".equals(mapUriParamsStart)
                    || "as=a:377eb8,,0.1,|b:4daf4a,,0.1,&ad=country_earth%3Agmi_cntry:".equals(mapUriParamsStart));
            Assert.assertTrue("End does not match, but is: " + mapUriParamsEnd, mapUriParamsEnd.matches("a:(FRA|DEU)\\|b:(FRA|DEU)&title=[ab]:present\\|[ab]:native%3A\\+doubtfully\\+native"));
        }
        //...tree
        DistributionTreeDto tree = (DistributionTreeDto)distributionInfo.getTree();
        Assert.assertEquals("Tree:2<FRA:native: doubtfully native{Miller, M.M. 1978: My French distribution. p 44 [accessed: 1792]}:0><Germany:present{Second ref article. – The journal. p 22}:0>", new DistributionInfoBuilderTest().tree2String(tree));
        Assert.assertEquals("Should be France and Germany", 2, tree.getRootElement().children.size());
        TreeNode<Set<DistributionDto>, NamedAreaDto> germanyNode = tree.getRootElement().getChildren().get(1);
        Assert.assertEquals("Germany", germanyNode.getNodeId().getLabel());
        DistributionDto germanyDistribution = germanyNode.getData().iterator().next();
        Assert.assertEquals(2, germanyDistribution.getAnnotations().getCount());
        Assert.assertEquals("Missing type annotation should exist",
                + 1, germanyDistribution.getAnnotations().getItems().stream()
                     .filter(a->"Missing Type Annotation".equals(a.getText())).count());
        Assert.assertEquals("There should be 1 source (even if it has no name used in source)", 1, germanyDistribution.getSources().getCount());

        //france
        TreeNode<Set<DistributionDto>, NamedAreaDto> franceNode = tree.getRootElement().getChildren().get(0);
        Assert.assertEquals("FRA", franceNode.getNodeId().getLabel());
        Assert.assertTrue("Size was not 1, but " + franceNode.getData().size(), franceNode.getData().size() == 1);
        DistributionDto franceDistributionDto = franceNode.getData().iterator().next();
        //...source
        Assert.assertEquals(1, franceDistributionDto.getSources().getCount());
        SourceDto source = franceDistributionDto.getSources().getItems().get(0);
        Assert.assertEquals("PrimaryTaxonomicSource", source.getType());
        Assert.assertEquals("44", source.getCitationDetail());
        Assert.assertEquals("1792", source.getAccessed());
        Assert.assertEquals("https://doi.org/10.10.0123/suf-456", source.getDoi());
        Assert.assertEquals("https://uri.org", source.getUri().toString());
        Assert.assertEquals("Genus insourcus", TaggedTextFormatter.createString(source.getNameInSource()));
    }

    private void createTestData() {
        Taxon taxon = createSynonymy();
        createFactualData(taxon);
    }

    private Taxon createSynonymy() {

        Person author = Person.NewInstance("Mill.", "Miller", "M.M.", "Michael");
        agentService.save(author);
        Reference nomRef = save(ReferenceFactory.newBook());
        nomRef.setTitle("My book");
        Reference nomRef2 = save(ReferenceFactory.newBook());
        nomRef2.setTitle("My book2");
        nomRef2.setDatePublished(TimePeriodParser.parseStringVerbatim("1973"));
        TaxonName accName = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Genus", null, "species", null, author, nomRef, "55", null);
        Reference secRef = save(ReferenceFactory.newBook());
        secRef.setTitle("My secbook");
        accName.addIdentifier(Identifier.NewInstance("wfo-12345", IdentifierType.IDENTIFIER_NAME_WFO()));
        Taxon taxon = Taxon.NewInstance(accName, secRef);
        taxon.setUuid(taxonUuid);
        taxonService.save(taxon);

        //homotyp. synonym
        Person author2 = Person.NewInstance("Noll.", "Noller", "N.N.", "Norman");
        agentService.save(author2);
        TaxonName homSynName = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Genusnovus", null, "species", null, author2, nomRef2, "66", accName.getHomotypicalGroup());
        homSynName.setBasionymAuthorship(author);
        Synonym homSyn = save(taxon.addHomotypicSynonymName(homSynName));
        accName.addBasionym(homSynName);
        //... annotation
        homSyn.addAnnotation(Annotation.NewInstance("HomSyn Annotation", AnnotationType.EDITORIAL(), Language.DEFAULT()));
        homSynName.addAnnotation(Annotation.NewInstance("HomSynName Annotation", AnnotationType.EDITORIAL(), Language.DEFAULT()));

        //... with homonym relation
        Person author3 = Person.NewInstance("Woll.", "Woller", "W.W.", "Wotan");
        agentService.save(author3);
        TaxonName earlierHomonym = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Genusnovus", null, "species", null, author3, nomRef, "666", null);
        NameRelationship rel = homSynName.addRelationshipToName(earlierHomonym, NameRelationshipType.LATER_HOMONYM());
        rel.setRuleConsidered("Art. 5");
        rel.setCodeEdition(NomenclaturalCodeEdition.ICN_2017_SHENZHEN);
        earlierHomonym.addAnnotation(Annotation.NewEditorialDefaultLanguageInstance("Homonym annotation"));
        nameService.save(earlierHomonym);

        //heterotyp. synonym
        TaxonName heteroSynName1 = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Genushetero", null, "hetero", null, author2, nomRef2, "99", null);
        Reference heteroSecRef = save(ReferenceFactory.newBook());
        heteroSecRef.setTitle("My hetero sec book");
        save(taxon.addHeterotypicSynonymName(heteroSynName1, heteroSecRef, "48", null));

        TaxonName heteroSynName1Recomb = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Newgenushetero", null, "hetero", null, author3, nomRef, "26", null);
        save(taxon.addHeterotypicSynonymName(heteroSynName1Recomb, heteroSecRef, "49",heteroSynName1.getHomotypicalGroup()));

        //heterotyp. synonym group 2
        Reference nomRef3 = save(ReferenceFactory.newBook());
        nomRef3.setTitle("My book3");
        nomRef3.setDatePublished(TimePeriodParser.parseStringVerbatim("2008"));

        TaxonName heteroSynName2 = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Second", null, "hetero", null, author2, nomRef3, "105", null);
        save(taxon.addHeterotypicSynonymName(heteroSynName2, taxon.getSec(), taxon.getSecMicroReference(), null));
        TaxonName heteroSynName2Recomb = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Newsecond", null, "hetero", null, author2, nomRef3, "105", null);
        save(taxon.addHeterotypicSynonymName(heteroSynName2Recomb, null, null, heteroSynName2.getHomotypicalGroup()));

        //heterotyp. synonym group 3
        Reference nomRef4 = save(ReferenceFactory.newBook());
        nomRef4.setTitle("My book4");
        nomRef4.setDatePublished(TimePeriodParser.parseStringVerbatim("2012"));

        TaxonName heteroSynName3 = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Third", null, "hetero", null, author2, nomRef4, "305", null);
        save(taxon.addHeterotypicSynonymName(heteroSynName3, nomRef4, null, null));
        TaxonName heteroSynName3Recomb = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Newthird", null, "hetero", null, author2, nomRef4, "308", null);
        save(taxon.addHeterotypicSynonymName(heteroSynName3Recomb, nomRef4, "33", heteroSynName3.getHomotypicalGroup()));

        return taxon;
    }

    private void createFactualData(Taxon taxon) {

        //feature tree
        createTermTrees();

        //factDatasets
        TaxonDescription factSet1 = save(TaxonDescription.NewInstance(taxon));
        TaxonDescription markedFactSet = save(TaxonDescription.NewInstance(taxon));
        markedFactSet.addMarker(MarkerType.COMPLETE(), true);

        //distributions
        Country.GERMANY().setSymbol("De");
        PresenceAbsenceTerm.PRESENT().setSymbol("");
        Distribution germany = save(Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.PRESENT()));
        germany.addAnnotation(Annotation.NewEditorialDefaultLanguageInstance("Editorial Annotation"));
        germany.addAnnotation(Annotation.NewInstance("Technical Annotation", AnnotationType.INTERNAL(), Language.DEFAULT()));
        germany.addAnnotation(Annotation.NewInstance("Missing Type Annotation", null, Language.DEFAULT()));
        //.... germany source
        Reference germanRef = save(ReferenceFactory.newArticle());
        Reference inJournal = save(ReferenceFactory.newJournal());
        germanRef.setInJournal(inJournal);
        germanRef.setTitle("Second ref article");
        inJournal.setTitle("The journal");
        germany.addPrimaryTaxonomicSource(germanRef, "22");

        factSet1.addElement(germany);

        Country.FRANCE().setSymbol("Fr");
//        PresenceAbsenceTerm.INTRODUCED().setSymbol("i");
        Distribution franceDist = save(Distribution.NewInstance(Country.FRANCE(), PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE()));
        markedFactSet.addElement(franceDist);

        //... sources
        //... ... primary
        Reference franceRef = save(ReferenceFactory.newBook());
        franceRef.setAuthorship(taxon.getName().getCombinationAuthorship());
        franceRef.setTitle("My French distribution");
        franceRef.setDatePublished(TimePeriodParser.parseStringVerbatim("1978"));
        franceRef.setDoi(DOI.fromRegistrantCodeAndSuffix("10.0123", "suf-456"));
        franceRef.setUri(URI.create("https://uri.org"));
        DescriptionElementSource source = franceDist.addPrimaryTaxonomicSource(franceRef, "44");
        source.setAccessed(TimePeriod.NewInstance(1792));
        TaxonName nameInSource = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        nameInSource.setGenusOrUninomial("Genus");
        nameInSource.setSpecificEpithet("insourcus");
        source.setNameUsedInSource(nameInSource);
        nameService.save(nameInSource);

        //... ... import
        Reference importRef = save(ReferenceFactory.newDatabase());
        importRef.setTitle("French distribution import");  //should not be shown in output
        franceDist.addImportSource("7777", "Distribution", importRef, "99");

        //text facts
        //data
        TextData td1 = save(TextData.NewInstance(Feature.DESCRIPTION(), "My first description", Language.DEFAULT(), null));
        TextData td2 = save(TextData.NewInstance(Feature.DESCRIPTION(), "My second description", Language.DEFAULT(), null));
        TextData td3 = save(TextData.NewInstance(Feature.DESCRIPTION(), "My third description", Language.DEFAULT(), null));
        td3.setSortIndex(2);
        td3.addPrimaryTaxonomicSource(franceRef, "63");
        TextData td4 = TextData.NewInstance(Feature.DESCRIPTION(), "My fourth description", Language.DEFAULT(), null);
        td4.setSortIndex(1);
        td4.setUuid(td4Uuid);
        save(td4);
        factSet1.addElements(td1, td2, td3);
        markedFactSet.addElements(td4);
        //... with media
        Media media1 = Media.NewInstance(URI.create("http://media.de/file.jpg"), 2, "JPG", "jpg");
        media1.setTitleCache("Media title", true);
        media1.setUuid(mediaUuid1);
        ImageFile image = ImageFile.NewInstance(URI.create("http://media.de/file/rep2.jpg"), 5, 100, 200);
        MediaRepresentation rep = MediaRepresentation.NewInstance("JPG2", "jpg");
        rep.addRepresentationPart(image);
        media1.addRepresentation(rep);
        Media media2 = Media.NewInstance(URI.create("http://media.de/file2.gif"), 3, "GIF", "gif");
        td3.addMedia(media1);
        td3.addMedia(media2);

        //empty text
        TextData emptyTd = save(TextData.NewInstance(Feature.DISCUSSION(), "", Language.DEFAULT(), null));
        factSet1.addElements(emptyTd);
        //annotation
        factSet1.addAnnotation(Annotation.NewInstance("Missing Type Annotation for empty", null, Language.DEFAULT()));
        factSet1.addMarker(MarkerType.IS_DOUBTFUL(), true);

        //common names
        CommonTaxonName cn1 = save(CommonTaxonName.NewInstance("My flower", Language.ENGLISH(), Country.UNITEDKINGDOMOFGREATBRITAINANDNORTHERNIRELAND()));
        CommonTaxonName cn2 = save(CommonTaxonName.NewInstance("Meine Blume", Language.GERMAN(), Country.GERMANY()));
        factSet1.addElements(cn1);
        TaxonDescription taxDesc2 = save(TaxonDescription.NewInstance(taxon));
        taxDesc2.addElement(cn2);
        Reference descRef = save(ReferenceFactory.newBook());
        descRef.setTitle("Common name description reference");
        //... with in-description source
        taxDesc2.addPrimaryTaxonomicSource(descRef, "91");

        //temporal data
        TemporalData temporalData1 = save(TemporalData.NewInstance(Feature.FLOWERING_PERIOD(),
                ExtendedTimePeriod.NewExtendedMonthAndDayInstance(4, 15, 6, 30, 3, 10, 7, 20)));
        TemporalData temporalData2 = save(TemporalData.NewInstance(Feature.FLOWERING_PERIOD(),
                ExtendedTimePeriod.NewExtendedMonthAndDayInstance(5, 1, 6, 15, 4, 1, 7, 1)));
        factSet1.addElements(temporalData1, temporalData2);

        //individual association
        DerivedUnit specimen1 = DerivedUnit.NewPreservedSpecimenInstance();
        specimen1.setTitleCache("My specimen", true);
        specimen1.setUuid(specimenUuid1);
        occurrenceService.save(specimen1);
        IndividualsAssociation indAss1 = save(IndividualsAssociation.NewInstance(specimen1));
        indAss1.putDescription(Language.DEFAULT(), "Associated specimen description1");
        indAss1.setFeature(Feature.MATERIALS_EXAMINED());

        DerivedUnit specimen2 = DerivedUnit.NewPreservedSpecimenInstance();
        specimen2.setTitleCache("My specimen2", true);
        specimen2.setUuid(specimenUuid2);
        occurrenceService.save(specimen2);
        IndividualsAssociation indAss2 = save(IndividualsAssociation.NewInstance(specimen2));
        indAss2.putDescription(Language.DEFAULT(), "Associated specimen description2");
        indAss2.setFeature(Feature.MATERIALS_EXAMINED());

        factSet1.addElements(indAss1, indAss2);

        //taxon interaction
        Taxon taxon1 = Taxon.NewInstance(taxon.getName(), taxon.getSec());
        taxon1.setUuid(taxonUuid1);
        taxonService.save(taxon1);
        TaxonInteraction taxInteract1 = save(TaxonInteraction.NewInstance(Feature.HOSTPLANT()));
        taxInteract1.setTaxon2(taxon1);
        taxInteract1.putDescription(Language.DEFAULT(), "Taxon interaction description1");

        TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        name2.setTitleCache("Name three Mill.", true);
        Taxon taxon2 = Taxon.NewInstance(name2, taxon.getSec());
        taxon2.setUuid(taxonUuid2);
        taxonService.save(taxon2);
        TaxonInteraction taxInteract2 = save(TaxonInteraction.NewInstance(Feature.HOSTPLANT()));
        taxInteract2.setTaxon2(taxon2);
        taxInteract2.putDescription(Language.DEFAULT(), "Taxon interaction description2");

        factSet1.addElements(taxInteract1, taxInteract2);

        //categorical data
        State state1 = State.NewInstance("State1", "State1", null);
        termService.save(state1);
        CategoricalData cd = save(CategoricalData.NewInstance(state1, Feature.LIFEFORM()));
        StateData stateData = cd.getStateData().get(0);
        stateData.putModifyingText(Language.DEFAULT(), "State modifying");
        cd.putModifyingText(Language.DEFAULT(), "Fact modifying");
        factSet1.addElements(cd);

        //quantitative data
        Feature feature = Feature.INTRODUCTION();
        QuantitativeData qd = save(QuantitativeData.NewMinMaxInstance(feature,
                MeasurementUnit.METER(), new BigDecimal(5), new BigDecimal(10)));
        factSet1.addElements(qd);

        //use data
        //TODO
    }

    private Reference save(Reference ref) {
        this.referenceDao.save(ref);
        return ref;
    }

    private Synonym save(Synonym syn) {
        taxonService.save(syn);
        return syn;
    }

    private void createTermTrees() {

        //feature tree
        @SuppressWarnings("unchecked")
        TermTree<Feature> featureTree = termTreeService.find(featureTreeUuid);
        if (featureTree == null) {
            featureTree = TermTree.NewInstance(TermType.Feature, Feature.class);
            featureTree.setUuid(featureTreeUuid);
            featureTree.getRoot().addChild(Feature.DISTRIBUTION());
            termTreeService.save(featureTree);
        }

        //area tree
        @SuppressWarnings("unchecked")
        TermTree<NamedArea> areaTree = termTreeService.find(areaTreeUuid);
        if (areaTree == null) {
            areaTree = TermTree.NewInstance(TermType.NamedArea, NamedArea.class);
            areaTree.setUuid(areaTreeUuid);
            areaTree.getRoot().addChild(Country.GERMANY());
            termTreeService.save(areaTree);
        }

        //status tree
        @SuppressWarnings("unchecked")
        TermTree<PresenceAbsenceTerm> statusTree = termTreeService.find(statusTreeUuid);
        if (statusTree == null) {
            statusTree = TermTree.NewInstance(TermType.PresenceAbsenceTerm, PresenceAbsenceTerm.class);
            statusTree.setUuid(statusTreeUuid);
            statusTree.getRoot().addChild(PresenceAbsenceTerm.PRESENT());
            termTreeService.save(statusTree);
        }
    }

    private <S extends DescriptionBase<?>> S save(S newDescription) {
        descriptionDao.save(newDescription);
        return newDescription;
    }

    private <S extends DescriptionElementBase> S save(S newElement) {
        descriptionElementDao.save(newElement);
        return newElement;
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
    }
}