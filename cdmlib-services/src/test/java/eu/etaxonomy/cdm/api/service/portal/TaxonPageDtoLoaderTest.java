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
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.geo.DistributionInfoBuilderTest;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author muellera
 * @since 26.02.2024
 */
public class TaxonPageDtoLoaderTest extends CdmTransactionalIntegrationTest {

    private UUID taxonUuid1 = UUID.fromString("075d1b8c-91d3-4b10-93b7-08ac872d09e8");

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IPortalService portalService;

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void test() {
        createTestData();
        commitAndStartNewTransaction();
        TaxonPageDtoConfiguration config = new TaxonPageDtoConfiguration();
        config.setWithSpecimens(false);
        config.setTaxonUuid(taxonUuid1);
        TaxonPageDto dto = portalService.taxonPageDto(config);

        Assert.assertNotNull(dto);
        List<TaggedText> list = dto.getTaggedName();
        Assert.assertEquals("Genus", list.get(0).getText());

        //facts
        ContainerDto<FeatureDto> features = dto.getTaxonFacts();
        Assert.assertEquals("There should be 3 features, distribution and description and common names",
                3, features.getCount());

        //... common taxon name
        FeatureDto commonNameDto = features.getItems().get(0);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically. Also as >1 common name exists it should use plural.",
                "Common Names", commonNameDto.getLabel());
        testCommonNames(commonNameDto);

        //... textData
        FeatureDto descriptionDto = features.getItems().get(1);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically. Also as >1 'Description' exists it should use plural.",
                "Descriptions", descriptionDto.getLabel());
        testDescription(descriptionDto);

        //... distribution
        FeatureDto distributionDto = features.getItems().get(2);
        Assert.assertEquals("As no feature tree is defined features should be sorted alphabetically",
                "Distribution", distributionDto.getLabel());
        testDistributions(distributionDto);

        //...categorical data

        //quantitative data

        //termporal data

        //individuals association

        //taxon interaction

        //use data
    }

    private void testCommonNames(FeatureDto commonNameDto) {
        ContainerDto<IFactDto> commonNames = commonNameDto.getFacts();
        Assert.assertEquals(2, commonNames.getCount());
        CommonNameDto cn1 = (CommonNameDto)commonNames.getItems().get(0);
        CommonNameDto cn2 = (CommonNameDto)commonNames.getItems().get(1);
        Assert.assertEquals("For now common names without sortindex are should be ordered alphabetically by name. This may change in future",
                "Meine Blume", cn1.getName());
        Assert.assertEquals("For now common names without sortindex are should be ordered alphabetically by name. This may change in future",
                "My flower", cn2.getName());
    }

    private void testDescription(FeatureDto descriptionDto) {
        ContainerDto<IFactDto> descriptions = descriptionDto.getFacts();
        Assert.assertEquals(4, descriptions.getCount());
        FactDto description1 = (FactDto)descriptions.getItems().get(0);
        FactDto description2 = (FactDto)descriptions.getItems().get(1);
        FactDto description3 = (FactDto)descriptions.getItems().get(2);
        FactDto description4 = (FactDto)descriptions.getItems().get(3);
        Assert.assertNull("Current sorting should sort null to the top. This may change in future.",
                description1.getSortIndex());
        Assert.assertNull("Current sorting should sort null to the top. This may change in future.",
                description2.getSortIndex());
        Assert.assertTrue("Current sorting should work on id if no sortIndex is given. This may change in future.",
                description1.getId() < description2.getId());
        //TODO use typed label formatter (once implemented)
        Assert.assertEquals("If sortindex is given it should be used for sorting.",
                "My fourth description", description3.getTypedLabel().get(0).getLabel().toString());
        Assert.assertEquals("If sortindex is given it should be used for sorting.",
                "My third description", description4.getTypedLabel().get(0).getLabel().toString());

        //media
        Assert.assertEquals(2, description4.getMedia().getCount());

    }

    private void testDistributions(FeatureDto distributionDto) {
        ContainerDto<IFactDto> distributions = distributionDto.getFacts();
        Assert.assertEquals(1, distributions.getCount());
        IFactDto distribution = distributions.getItems().get(0);
        Assert.assertEquals(DistributionInfoDto.class.getSimpleName(), distribution.getClazz());
        DistributionInfoDto distributionInfo = (DistributionInfoDto)distribution;
        //... condensed distribution
        //TODO this still fails
        System.out.println(distributionInfo.getCondensedDistribution().getHtmlString() + ": TODO");
        //TODO probably the order is not deterministic, so we may need to check single parts only, same as in according builder test
        String mapUriParamsStart = distributionInfo.getMapUriParams().substring(0, 50);
        String mapUriParamsEnd = distributionInfo.getMapUriParams().replace(mapUriParamsStart, "");
        Assert.assertEquals("as=a:,,0.1,|b:,,0.1,&ad=country_earth%3Agmi_cntry:", mapUriParamsStart);
        Assert.assertTrue("End does not match, but is: " + mapUriParamsEnd, mapUriParamsEnd.matches("a:(FRA|DEU)\\|b:(FRA|DEU)&title=[ab]:present\\|[ab]:introduced"));
        //...tree
        DistributionTreeDto tree = (DistributionTreeDto)distributionInfo.getTree();
        Assert.assertEquals("Tree:2<FRA:introduced{Miller, M.M. 1978: My French distribution. p 44}:0><Germany:present{}:0>", new DistributionInfoBuilderTest().tree2String(tree));
        Assert.assertEquals("Should be France and Germany", 2, tree.getRootElement().children.size());
        TreeNode<Set<DistributionDto>, NamedAreaDto> germanyNode = tree.getRootElement().getChildren().get(1);
        Assert.assertEquals("Germany", germanyNode.getNodeId().getLabel());
        Assert.assertEquals(1, germanyNode.getData().iterator().next().getAnnotations().getCount());
    }

    private void createTestData() {
        Person author = Person.NewInstance("Mill.", "Miller", "M.M.", "Michael");
        Reference nomRef = ReferenceFactory.newBook();
        nomRef.setTitle("My book");
        TaxonName accName = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(),
                "Genus", null, "species", null, author, nomRef, "55", null);
        Reference secRef = ReferenceFactory.newBook();
        secRef.setTitle("My secbook");
        Taxon taxon = Taxon.NewInstance(accName, secRef);
        taxon.setUuid(taxonUuid1);
        taxonService.save(taxon);

        //distributions
        TaxonDescription taxDesc = TaxonDescription.NewInstance(taxon);
        Country.GERMANY().setSymbol("De");
        PresenceAbsenceTerm.PRESENT().setSymbol("");
        Distribution germany = Distribution.NewInstance(Country.GERMANY(), PresenceAbsenceTerm.PRESENT());
        germany.addAnnotation(Annotation.NewEditorialDefaultLanguageInstance("Abc Annotation"));
        germany.addAnnotation(Annotation.NewInstance("Technical Annotation", AnnotationType.TECHNICAL(), Language.DEFAULT()));

        taxDesc.addElement(germany);
        Country.FRANCE().setSymbol("Fr");
        PresenceAbsenceTerm.INTRODUCED().setSymbol("i");
        Distribution franceDist = Distribution.NewInstance(Country.FRANCE(), PresenceAbsenceTerm.INTRODUCED());
        taxDesc.addElement(franceDist);

        //... sources
        //... ... primary
        Reference franceRef = ReferenceFactory.newBook();
        franceRef.setAuthorship(author);
        franceRef.setTitle("My French distribution");
        franceRef.setDatePublished(TimePeriodParser.parseStringVerbatim("1978"));
        franceDist.addPrimaryTaxonomicSource(franceRef, "44");
        //... ... import
        Reference importRef = ReferenceFactory.newDatabase();
        importRef.setTitle("French distribution import");  //should not be shown in output
        franceDist.addImportSource("7777", "Distribution", importRef, "99");

        //text facts
        TextData td1 = TextData.NewInstance(Feature.DESCRIPTION(), "My first description", Language.DEFAULT(), null);
        TextData td2 = TextData.NewInstance(Feature.DESCRIPTION(), "My second description", Language.DEFAULT(), null);
        TextData td3 = TextData.NewInstance(Feature.DESCRIPTION(), "My third description", Language.DEFAULT(), null);
        td3.setSortIndex(2);
        td3.addPrimaryTaxonomicSource(franceRef, "63");
        TextData td4 = TextData.NewInstance(Feature.DESCRIPTION(), "My fourth description", Language.DEFAULT(), null);
        td4.setSortIndex(1);
        taxDesc.addElements(td1, td2, td3, td4);
        //... with media
        Media media1 = Media.NewInstance(URI.create("http://media.de/file.jpg"), 2, "JPG", "jpg");
        Media media2 = Media.NewInstance(URI.create("http://media.de/file2.gif"), 3, "GIF", "gif");
        td3.addMedia(media1);
        td3.addMedia(media2);

        //common names
        CommonTaxonName cn1 = CommonTaxonName.NewInstance("My flower", Language.ENGLISH(), Country.UNITEDKINGDOMOFGREATBRITAINANDNORTHERNIRELAND());
        CommonTaxonName cn2 = CommonTaxonName.NewInstance("Meine Blume", Language.GERMAN(), Country.GERMANY());
        taxDesc.addElements(cn1, cn2);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
    }
}