/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 21.11.2019
 */
@Ignore //preliminary ignore as it does not always work (depending on other tests)
public class StructuredDescriptionAggregationTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(StructuredDescriptionAggregationTest.class);

    private static final UUID T_LAPSANA_UUID = UUID.fromString("f65d47bd-4f49-4ab1-bc4a-bc4551eaa1a8");
    private static final UUID TN_LAPSANA_UUID = UUID.fromString("f4d29e9f-6484-4184-af2e-9704e96a17e3");

    private static final UUID T_LAPSANA_COMMUNIS_UUID = UUID.fromString("2a5ceebb-4830-4524-b330-78461bf8cb6b");

    private static final UUID T_LAPSANA_COMMUNIS_COMMUNIS_UUID = UUID.fromString("441a3c40-0c84-11de-8c30-0800200c9a66");

    private static final UUID T_LAPSANA_COMMUNIS_ADENOPHORA_UUID = UUID.fromString("e4acf200-63b6-11dd-ad8b-0800200c9a66");

    private static final UUID T_LAPSANA_COMMUNIS_ALPINA_UUID = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");

    private static final UUID CLASSIFICATION_UUID = UUID.fromString("4b266053-a841-4980-b548-3f21d8d7d712");

    private static final UUID T_LAPSANA_COMMUNIS_ALPINA_SPEC1_UUID = UUID.fromString("0de17403-aeef-4138-a220-9914e7c46f5a");
    private static final UUID T_LAPSANA_COMMUNIS_ALPINA_SPEC2_UUID = UUID.fromString("9e680e26-9e12-47a9-807c-743525c9171e");
    private static final UUID T_LAPSANA_COMMUNIS_ALPINA_SPEC3_UUID = UUID.fromString("3cdaa12f-7508-4073-b8d7-afca77a6be34");
    private static final UUID T_LAPSANA_COMMUNIS_ADENOPHORA_SPEC1_UUID = UUID.fromString("83788037-7a03-4e46-98ca-e8801096b216");


    private static final UUID uuidFeatureLeafPA = UUID.fromString("c4dfd16f-f2ed-45e0-8f4d-7fe1ae880510");
    private static UUID uuidFeatureLeafLength = UUID.fromString("3c19b50b-4a8e-467e-b7d4-89ebc05a33e1");
    private static UUID uuidFeatureLeafColor = UUID.fromString("1e8f503c-5aeb-4788-b4f9-84128f7141c7");

    private static UUID uuidLeafColorBlue = UUID.fromString("9b4df19d-f89d-4788-9d71-d1f6f7cae910");
    private static UUID uuidLeafColorYellow = UUID.fromString("4cf0881b-0e7b-489a-9fdb-adbe6ae4e0ae");

    private static UUID uuidFeatureTree = UUID.fromString("c8a29a94-2754-4d78-9faa-dff3e1387b2d");


    @SpringBeanByType
    private ICdmRepository repository;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private ITermTreeService termTreeService;

    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IOccurrenceService occurrenceService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private IDescriptiveDataSetService datasetService;

    private StructuredDescriptionAggregation engine;

    private IProgressMonitor monitor;

    @Before
    public void setUp() {
        engine = new StructuredDescriptionAggregation();
//        engine.setBatchMinFreeHeap(100 * 1024 * 1024);
        monitor = DefaultProgressMonitor.NewInstance();
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="StructuredDescriptionAggregationTest.xml"),
    })
    public void reaggregationTest() throws JvmLimitsException{
        createDefaultFeatureTree();
        DescriptiveDataSet dataSet = createTestDataset();
        commitAndStartNewTransaction();

        StructuredDescriptionAggregationConfiguration config = createConfig(dataSet);

        // 1st aggregation
        UpdateResult result = engine.invoke(config, repository);
        testStatusOk(result);
        commitAndStartNewTransaction();
        testAggregatedDescription(false, false);

        addSomeDataToFirstAggregation();
        commitAndStartNewTransaction();
        testAggregatedDescription(true, false);

        // 2nd aggregation
        result = engine.invoke(config, repository);
        testStatusOk(result);
        commitAndStartNewTransaction();
        testAggregatedDescription(false, false);
    }

    private void addSomeDataToFirstAggregation() {
        Taxon taxLapsanaCommunisAlpina = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        TaxonDescription taxonDescription = taxLapsanaCommunisAlpina.getDescriptions().stream()
                .filter(desc->desc.getTypes().contains(DescriptionType.AGGREGATED_STRUC_DESC))
                .filter(desc->!desc.getTypes().contains(DescriptionType.CLONE_FOR_SOURCE))
                .findFirst().get();

        addCategoricalData(taxonDescription, uuidFeatureLeafPA, State.uuidPresent);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="StructuredDescriptionAggregationTest.xml"),
    })
    public void incompleteQuantitativeDataTest() throws JvmLimitsException{
        createDefaultFeatureTree();
        DescriptiveDataSet dataSet = DescriptiveDataSet.NewInstance();
        datasetService.save(dataSet);

        SpecimenDescription specDescAlpina1 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen1", T_LAPSANA_COMMUNIS_ALPINA_SPEC1_UUID);
        addQuantitativeData(specDescAlpina1, uuidFeatureLeafLength, StatisticalMeasure.MIN(), new BigDecimal("5.0"));

        SpecimenDescription specDescAlpina2 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen2", T_LAPSANA_COMMUNIS_ALPINA_SPEC2_UUID);
        addQuantitativeData(specDescAlpina2, uuidFeatureLeafLength, StatisticalMeasure.MAX(), new BigDecimal("7.0"));

        TaxonNode tnLapsana = taxonNodeService.find(TN_LAPSANA_UUID);
        Assert.assertNotNull(tnLapsana);
        dataSet.addTaxonSubtree(tnLapsana);

        @SuppressWarnings("unchecked")
        TermTree<Feature> descriptiveSystem = termTreeService.find(uuidFeatureTree);
        dataSet.setDescriptiveSystem(descriptiveSystem);
        commitAndStartNewTransaction();

        StructuredDescriptionAggregationConfiguration config = createConfig(dataSet);

        UpdateResult result = engine.invoke(config, repository);
        testStatusOk(result);

        Taxon taxLapsanaCommunisAlpina = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        TaxonDescription aggrDescLapsanaCommunisAlpina = testTaxonDescriptions(taxLapsanaCommunisAlpina, 1);
        testQuantitativeData(uuidFeatureLeafLength, null, new BigDecimal("0.0"), new BigDecimal("7.0"), null, aggrDescLapsanaCommunisAlpina);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="StructuredDescriptionAggregationTest.xml"),
    })
    public void incompleteCategoricalDataTest() throws JvmLimitsException{
        createDefaultFeatureTree();
        DescriptiveDataSet dataSet = DescriptiveDataSet.NewInstance();
        datasetService.save(dataSet);

        SpecimenDescription specDescAlpina1 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen1", T_LAPSANA_COMMUNIS_ALPINA_SPEC1_UUID);
        addCategoricalData(specDescAlpina1, uuidFeatureLeafColor, null);

        TaxonNode tnLapsana = taxonNodeService.find(TN_LAPSANA_UUID);
        Assert.assertNotNull(tnLapsana);
        dataSet.addTaxonSubtree(tnLapsana);

        @SuppressWarnings("unchecked")
        TermTree<Feature> descriptiveSystem = termTreeService.find(uuidFeatureTree);
        dataSet.setDescriptiveSystem(descriptiveSystem);
        commitAndStartNewTransaction();

        StructuredDescriptionAggregationConfiguration config = createConfig(dataSet);

        UpdateResult result = engine.invoke(config, repository);
        testStatusOk(result);

        Taxon taxLapsanaCommunisAlpina = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        TaxonDescription aggrDescLapsanaCommunisAlpina = testTaxonDescriptions(taxLapsanaCommunisAlpina, 1);
        List<StateData> sdAlpinaLeafColor = testCategoricalData(uuidFeatureLeafColor, 1, aggrDescLapsanaCommunisAlpina, false);
        testState(sdAlpinaLeafColor, uuidLeafColorBlue, 0);
        testState(sdAlpinaLeafColor, uuidLeafColorYellow, 0);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="StructuredDescriptionAggregationTest.xml"),
    })
    public void aggregationTest() throws JvmLimitsException{
        createDefaultFeatureTree();
        DescriptiveDataSet dataSet = createTestDataset();
        commitAndStartNewTransaction();

        StructuredDescriptionAggregationConfiguration config = createConfig(dataSet);

        UpdateResult result = engine.invoke(config, repository);
        commitAndStartNewTransaction();
        testStatusOk(result);
        testAggregatedDescription(false, false);

        config.setIncludeLiterature(true);

        result = engine.invoke(config, repository);
        commitAndStartNewTransaction();
        testStatusOk(result);
        testAggregatedDescription(false, true);  //with literature
    }

    private void testStatusOk(UpdateResult result) {
        if (result.getStatus() != UpdateResult.Status.OK){
            Assert.fail("Aggregation should have status OK but was " + result.toString());
            for (Exception ex : result.getExceptions()){
                ex.printStackTrace();
            }
        }
    }

    private void addLiterature(DescriptiveDataSet dataSet) {

        //literature description
        Taxon taxon = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        TaxonDescription literatureDescription = TaxonDescription.NewInstance(taxon);
        literatureDescription.addType(DescriptionType.SECONDARY_DATA);
        addQuantitativeData(literatureDescription, uuidFeatureLeafLength, new BigDecimal("4.5"), new BigDecimal("6.5"));
        addCategoricalData(literatureDescription, uuidFeatureLeafColor, uuidLeafColorBlue);
        dataSet.addDescription(literatureDescription);
    }

    private void testAggregatedDescription(boolean withAddedData, boolean withLiterature) {

        //L. communis alpina
        Taxon taxLapsanaCommunisAlpina = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        int nElement = withAddedData ? 4 : 3;
        TaxonDescription aggrDescLapsanaCommunisAlpina = testTaxonDescriptions(taxLapsanaCommunisAlpina, nElement);

        List<StateData> stateData = testCategoricalData(uuidFeatureLeafPA, 1, aggrDescLapsanaCommunisAlpina, withAddedData);
        testState(stateData, State.uuidPresent, 3);
        List<StateData> sdAlpinaLeafColor = testCategoricalData(uuidFeatureLeafColor, 1, aggrDescLapsanaCommunisAlpina, false);
        int litLeafColorBlue = withLiterature? 1: 0;
        testState(sdAlpinaLeafColor, uuidLeafColorBlue, 2+litLeafColorBlue);
        testState(sdAlpinaLeafColor, uuidLeafColorYellow, 0);
        BigDecimal count = withLiterature? null : new BigDecimal("3");
        BigDecimal avg = withLiterature? null : new BigDecimal("6.666667");
        BigDecimal min = withLiterature? new BigDecimal("4.5") : new BigDecimal("5.0");
        testQuantitativeData(uuidFeatureLeafLength, count, min,
                new BigDecimal("8.0"), avg, aggrDescLapsanaCommunisAlpina);
        //... sources
        int intLit = withLiterature? 1 : 0;
        Assert.assertEquals(3+intLit, aggrDescLapsanaCommunisAlpina.getSources().size());
        SpecimenOrObservationBase<?> specLcommunisAlpina1 = CdmBase.deproxy(occurrenceService.find(T_LAPSANA_COMMUNIS_ALPINA_SPEC1_UUID));
        Assert.assertEquals("Spec1 must have 2 descriptions now. The primary one and the cloned.", 2, specLcommunisAlpina1.getSpecimenDescriptions().size());
        Assert.assertEquals(1, specLcommunisAlpina1.getSpecimenDescriptions().stream().filter(d->d.isCloneForSource()).count());
        DescriptionBase<?> clonedDesc = specLcommunisAlpina1.getDescriptions().stream().filter(d->d.isCloneForSource()).findFirst().get();
        DescriptionBase<?> sourceDescription = getSingleSpecimenDescriptionSource(aggrDescLapsanaCommunisAlpina, T_LAPSANA_COMMUNIS_ALPINA_SPEC1_UUID);
        Assert.assertEquals(clonedDesc, sourceDescription);

        //L. communis adenophora
        Taxon taxLapsanaCommunisAdenophora = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_ADENOPHORA_UUID);
        TaxonDescription aggrDescLapsanaCommunisAdenophora = testTaxonDescriptions(taxLapsanaCommunisAdenophora, 3);
        testState(testCategoricalData(uuidFeatureLeafPA, 1, aggrDescLapsanaCommunisAdenophora, false), State.uuidPresent, 1);
        List<StateData> sdAdenophoraLeafColor = testCategoricalData(uuidFeatureLeafColor, 1, aggrDescLapsanaCommunisAdenophora, false);
        testState(sdAdenophoraLeafColor, uuidLeafColorBlue, 0);
        testState(sdAdenophoraLeafColor, uuidLeafColorYellow, 1);
        testQuantitativeData(uuidFeatureLeafLength, new BigDecimal("1"), new BigDecimal("10.0"),
                new BigDecimal("10.0"), new BigDecimal("10.0"), aggrDescLapsanaCommunisAdenophora);

        //L. communis
        Taxon taxLapsanaCommunis = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_UUID);
        TaxonDescription aggrDescLapsanaCommunis = testTaxonDescriptions(taxLapsanaCommunis, 3);
        testState(testCategoricalData(uuidFeatureLeafPA, 1, aggrDescLapsanaCommunis, false), State.uuidPresent, 4);
        List<StateData> sdCommunisLeafColor = testCategoricalData(uuidFeatureLeafColor, 2, aggrDescLapsanaCommunis, false);
        testState(sdCommunisLeafColor, uuidLeafColorBlue, 2 + intLit);
        testState(sdCommunisLeafColor, uuidLeafColorYellow, 1);
        count = withLiterature? null : new BigDecimal("4");
        avg = withLiterature? null : new BigDecimal("7.5");
        testQuantitativeData(uuidFeatureLeafLength, count, min,
                new BigDecimal("10.0"), avg, aggrDescLapsanaCommunis);
        //... sources
        Assert.assertEquals(2, aggrDescLapsanaCommunis.getSources().size());
        Map<UUID, List<TaxonDescription>> taxonDescriptionMap = getSourceTaxonDescriptionMap(aggrDescLapsanaCommunis);
        Assert.assertEquals(2, taxonDescriptionMap.size());
        Assert.assertEquals(1, taxonDescriptionMap.get(T_LAPSANA_COMMUNIS_ALPINA_UUID).size());
        Assert.assertEquals(1, taxonDescriptionMap.get(T_LAPSANA_COMMUNIS_ADENOPHORA_UUID).size());
        Assert.assertNotEquals(aggrDescLapsanaCommunisAlpina, taxonDescriptionMap.get(T_LAPSANA_COMMUNIS_ALPINA_UUID).get(0));

        //Lapsana
        Taxon taxLapsana = (Taxon)taxonService.find(T_LAPSANA_UUID);
        TaxonDescription aggrDescLapsana = testTaxonDescriptions(taxLapsana, 3);
        testState(testCategoricalData(uuidFeatureLeafPA, 1, aggrDescLapsana, false), State.uuidPresent, 4);
        List<StateData> sdLapsanLeafColor = testCategoricalData(uuidFeatureLeafColor, 2, aggrDescLapsana, false);
        testState(sdLapsanLeafColor, uuidLeafColorBlue, 2 + intLit);
        testState(sdLapsanLeafColor, uuidLeafColorYellow, 1);
        count = withLiterature? null : new BigDecimal("4");
        avg = withLiterature? null : new BigDecimal("7.5");
        testQuantitativeData(uuidFeatureLeafLength, count, min,
                new BigDecimal("10.0"), avg, aggrDescLapsana);
        //... sources
        Assert.assertEquals(1, aggrDescLapsana.getSources().size());
        taxonDescriptionMap = getSourceTaxonDescriptionMap(aggrDescLapsana);
        Assert.assertEquals(1, taxonDescriptionMap.size());
        Assert.assertEquals(1, taxonDescriptionMap.get(T_LAPSANA_COMMUNIS_UUID).size());
        Assert.assertNotEquals(aggrDescLapsanaCommunis, taxonDescriptionMap.get(T_LAPSANA_COMMUNIS_UUID).get(0));

        //total description count
        List<DescriptionBase> descs = descriptionService.list(null, null, null, null, null);
        Assert.assertEquals("Should have 4 specimen desc, 1 literature desc, 2 individual association holder, "
                + "4 aggregated descriptions, 4 cloned specimen descriptions, (3/4 cloned aggregated descriptions?) = 18/19",
                18+intLit, descs.size());

    }

    private Map<UUID, List<TaxonDescription>> getSourceTaxonDescriptionMap(TaxonDescription desc) {
        return desc.getSources().stream().filter(s->(s.getCdmSource() instanceof TaxonDescription))
            .map(s->CdmBase.deproxy(s.getCdmSource(), TaxonDescription.class))
            .collect(Collectors.groupingBy(fDescToDescribedUuid));
    }

    private static Function<DescriptionBase<?>, UUID> fDescToDescribedUuid =
            ((Function<DescriptionBase<?>, IDescribable<?>>)(d->d.isInstanceOf(SpecimenDescription.class)? d.getDescribedSpecimenOrObservation(): CdmBase.deproxy(d, TaxonDescription.class).getTaxon()))
            .andThen(IDescribable::getUuid);

    private DescriptionBase<?> getSingleSpecimenDescriptionSource(
            TaxonDescription aggrDescLapsanaCommunisAlpina, UUID specimenUuid) {

        Map<UUID, List<DescriptionBase<?>>> map = aggrDescLapsanaCommunisAlpina.getSources().stream()
            .map(src->(DescriptionBase<?>)src.getCdmSource())
            .filter(o-> o != null)
            .collect(Collectors.groupingBy(fDescToDescribedUuid));
        Assert.assertEquals(1, map.get(specimenUuid).size());
        return map.get(specimenUuid).get(0);

    }

    private StructuredDescriptionAggregationConfiguration createConfig(DescriptiveDataSet dataSet) {
        TaxonNodeFilter filter = TaxonNodeFilter.NewSubtreeInstance(TN_LAPSANA_UUID);
        StructuredDescriptionAggregationConfiguration config =
                StructuredDescriptionAggregationConfiguration.NewInstance(filter, monitor);
        config.setDatasetUuid(dataSet.getUuid());
        config.setAggregationMode(AggregationMode.byWithinTaxonAndToParent());
        config.setIncludeLiterature(false);
        return config;
    }

    private DescriptiveDataSet createTestDataset() {
        DescriptiveDataSet dataSet = DescriptiveDataSet.NewInstance();
        dataSet.setLabel("Test dataset");
        datasetService.save(dataSet);

        //L. communis alpina spec1
        SpecimenDescription specDescAlpina1 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen1", T_LAPSANA_COMMUNIS_ALPINA_SPEC1_UUID);
        addCategoricalData(specDescAlpina1, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDescAlpina1, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), new BigDecimal("5.0"));
        addCategoricalData(specDescAlpina1, uuidFeatureLeafColor, uuidLeafColorBlue);

        //L. communis alpina spec2
        SpecimenDescription specDescAlpina2 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen2", T_LAPSANA_COMMUNIS_ALPINA_SPEC2_UUID);
        addCategoricalData(specDescAlpina2, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDescAlpina2, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), new BigDecimal("7.0"));
        addCategoricalData(specDescAlpina2, uuidFeatureLeafColor, uuidLeafColorBlue);

        //L. communis alpina spec3
        SpecimenDescription specDescAlpina3 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen3", T_LAPSANA_COMMUNIS_ALPINA_SPEC3_UUID);
        addCategoricalData(specDescAlpina3, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDescAlpina3, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), new BigDecimal("8.0"));

        //L. communis adenophora
        SpecimenDescription specDescAdenophora = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ADENOPHORA_UUID, "adenophora specimen", T_LAPSANA_COMMUNIS_ADENOPHORA_SPEC1_UUID);
        addCategoricalData(specDescAdenophora, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDescAdenophora, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), new BigDecimal("10.0"));
        addCategoricalData(specDescAdenophora, uuidFeatureLeafColor, uuidLeafColorYellow);

        TaxonNode tnLapsana = taxonNodeService.find(TN_LAPSANA_UUID);
        Assert.assertNotNull(tnLapsana);
        dataSet.addTaxonSubtree(tnLapsana);

        @SuppressWarnings("unchecked")
        TermTree<Feature> descriptiveSystem = termTreeService.find(uuidFeatureTree);
        dataSet.setDescriptiveSystem(descriptiveSystem);

        addLiterature(dataSet);
        return dataSet;
    }

    private TaxonDescription testTaxonDescriptions(Taxon taxon, int elementSize){
        List<TaxonDescription> taxonDescriptions = taxon.getDescriptions().stream()
                .filter(desc->desc.getTypes().contains(DescriptionType.AGGREGATED_STRUC_DESC))
                .filter(desc->!desc.getTypes().contains(DescriptionType.CLONE_FOR_SOURCE))
                .collect(Collectors.toList());

        Assert.assertEquals(1, taxonDescriptions.size());
        TaxonDescription aggrDesc = taxonDescriptions.iterator().next();
        Set<DescriptionElementBase> elements = aggrDesc.getElements();
        Assert.assertEquals(elementSize, elements.size());
        return aggrDesc;
    }

    private void testQuantitativeData(UUID featureUuid, BigDecimal sampleSize, BigDecimal min,
            BigDecimal max, BigDecimal avg, TaxonDescription aggrDesc) {
        List<QuantitativeData> quantitativeDatas = aggrDesc.getElements().stream()
                .filter(element->element.getFeature().getUuid().equals(featureUuid))
                .map(catData->CdmBase.deproxy(catData, QuantitativeData.class))
                .collect(Collectors.toList());
        Assert.assertEquals(1, quantitativeDatas.size());
        QuantitativeData leafLength = quantitativeDatas.iterator().next();
        Assert.assertEquals(sampleSize, leafLength.getSampleSize());
        Assert.assertEquals(min, leafLength.getMin());
        Assert.assertEquals(max, leafLength.getMax());
        Assert.assertEquals(avg, leafLength.getAverage());
    }


    private List<StateData> testCategoricalData(UUID featureUuid, int stateDataCount, TaxonDescription taxonDescription, boolean withAddedData) {
        List<CategoricalData> categoricalDatas = taxonDescription.getElements().stream()
                .filter(element->element.getFeature().getUuid().equals(featureUuid))
                .map(catData->CdmBase.deproxy(catData, CategoricalData.class))
                .collect(Collectors.toList());
        int nCD = withAddedData ? 2 : 1;
        Assert.assertEquals(nCD, categoricalDatas.size());
        CategoricalData categoricalData;
        if (withAddedData){
            categoricalData = categoricalDatas.stream().filter(cd->cd.getStateData().get(0).getCount() != null ).findFirst().get();
        }else{
            categoricalData = categoricalDatas.iterator().next(); // categoricalDatas.stream().filter(cd->cd.getStateData().size() != 1).collect(Collectors.toList());
        }
        List<StateData> stateDatas = categoricalData.getStateData();
        Assert.assertEquals(stateDataCount, stateDatas.size());
        return stateDatas;
    }

    private void testState(List<StateData> stateDatas, UUID stateUuid, Integer stateDataCount){
        List<StateData> filteredStateDatas = stateDatas.stream()
                .filter(stateData->stateData.getState()!=null && stateData.getState().getUuid().equals(stateUuid))
                .collect(Collectors.toList());
        if(stateDataCount==0){
            // non-existence test
            Assert.assertEquals(0, filteredStateDatas.size());
            return;
        }
        Assert.assertEquals(1, filteredStateDatas.size());
        StateData stateData = filteredStateDatas.iterator().next();
        Assert.assertEquals(stateDataCount, stateData.getCount());
        Assert.assertEquals(stateUuid, stateData.getState().getUuid());
    }

    private void addQuantitativeData(DescriptionBase<?> desc, UUID uuidFeature, StatisticalMeasure type, BigDecimal value) {
        Feature feature = (Feature)termService.find(uuidFeature);
        QuantitativeData qd = QuantitativeData.NewInstance(feature);
        StatisticalMeasurementValue smv = StatisticalMeasurementValue.NewInstance(type, value);
        qd.addStatisticalValue(smv);
        desc.addElement(qd);
    }

    private void addQuantitativeData(DescriptionBase<?> desc, UUID uuidFeature, BigDecimal min, BigDecimal max) {
        Feature feature = (Feature)termService.find(uuidFeature);
        QuantitativeData qd = QuantitativeData.NewInstance(feature);
        StatisticalMeasurementValue smv = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MIN(), min);
        qd.addStatisticalValue(smv);
        smv = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MAX(), max);
        qd.addStatisticalValue(smv);
        desc.addElement(qd);
    }

    private void addCategoricalData(DescriptionBase<?> desc, UUID featureUuid, UUID stateUUID) {
        Feature feature = (Feature)termService.find(featureUuid);
        State state = (State)termService.find(stateUUID);
        CategoricalData cd = CategoricalData.NewInstance(state, feature);
        desc.addElement(cd);
    }

    private SpecimenDescription createSpecimenDescription(DescriptiveDataSet dataSet, UUID taxonUuid, String specLabel, UUID specimenUuid ) {
        Taxon taxon = (Taxon)taxonService.find(taxonUuid);
        DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
        specimen.setTitleCache(specLabel, true);
        specimen.setUuid(specimenUuid);
        TaxonDescription taxonDescription = taxon.getDescriptions(DescriptionType.INDIVIDUALS_ASSOCIATION).stream()
            .findFirst()
            .orElseGet(()->{
                TaxonDescription td = TaxonDescription.NewInstance(taxon);
                td.addType(DescriptionType.INDIVIDUALS_ASSOCIATION);
                td.setTitleCache("Specimens used by " + dataSet.getTitleCache() + " for " + getTaxonLabel(taxon), true);
                return td;}
             );
        IndividualsAssociation individualsAssociation = IndividualsAssociation.NewInstance(specimen);
        // TODO this has to be discussed; currently the description with the InidividualsAssociation is
        // needed in the dataset for performance reasons
        taxonDescription.addElement(individualsAssociation);
        dataSet.addDescription(taxonDescription);
        SpecimenDescription specDesc = SpecimenDescription.NewInstance(specimen);

        dataSet.addDescription(specDesc);
        return specDesc;
    }

    private String getTaxonLabel(Taxon taxon) {
        if (taxon.getName() != null){
            return taxon.getName().getTitleCache();
        }else{
            return taxon.getTitleCache();
        }
    }

    private Feature createFeature(UUID uuid, String label, boolean isQuantitative) {
        Feature feature = Feature.NewInstance("", label, null);
        feature.setUuid(uuid);
        feature.setSupportsQuantitativeData(isQuantitative);
        feature.setSupportsCategoricalData(!isQuantitative);
        feature.setSupportsTextData(false);
        termService.save(feature);
        return feature;
    }

    private State createState(String label, UUID uuid) {
        State state = State.NewInstance("", label, "");
        state.getTitleCache();  //for better debugging
        state.setUuid(uuid);
        termService.save(state);
        return state;
    }

    private void createDefaultFeatureTree() {
        //feature tree
        //leaf p/a
        //  leaf length
        //  leaf color
        TermTree<Feature> featureTree = TermTree.NewFeatureInstance();
        featureTree.setUuid(uuidFeatureTree);
        Feature featureLeafPA = createFeature(uuidFeatureLeafPA, "LeafPA", false);
        TermNode<Feature> leafPANode = featureTree.getRoot().addChild(featureLeafPA);
        Feature featureLeafLength = createFeature(uuidFeatureLeafLength, "LeafLength", true);
        leafPANode.addChild(featureLeafLength);
        Feature featureLeafColor = createFeature(uuidFeatureLeafColor, "LeafColor", false);
        leafPANode.addChild(featureLeafColor);
        State yellow = createState("Yellow", uuidLeafColorYellow);
        State blue = createState("Blue", uuidLeafColorBlue);
        TermVocabulary<State> stateVoc = TermVocabulary.NewInstance(TermType.State, State.class, "", "Colors", null, null);
        stateVoc.addTerm(yellow);
        stateVoc.addTerm(blue);
        featureLeafColor.addSupportedCategoricalEnumeration(stateVoc);
        vocabularyService.save(stateVoc);
    }

//    @Test
    @Override
    public void createTestDataSet() throws FileNotFoundException {

        // --- References --- //
        Reference sec = ReferenceFactory.newDatabase();
        sec.setTitleCache("Test", true);
        Reference nomRef = ReferenceFactory.newBook();
        sec.setTitleCache("Sp.Pl.", true);

        referenceService.save(sec);
        referenceService.save(nomRef);


        // --- Taxa --- //
        //  Lapsana
        //        L. communis
        //            L. communis subsp. communis
        //            L. communis subsp. adenophora
        //            L. communis subsp. alpina
        //  Sonchella
        //        S. dentata
        //        S. stenoma
        IBotanicalName n_lapsana = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        n_lapsana.setTitleCache("Lapsana", true);
        Taxon t_lapsana = Taxon.NewInstance(n_lapsana, sec);
        t_lapsana.setUuid(T_LAPSANA_UUID);
        taxonService.saveOrUpdate(t_lapsana);

        IBotanicalName n_lapsana_communis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        n_lapsana_communis.setTitleCache("L. communis", true);
        Taxon t_lapsana_communis = Taxon.NewInstance(n_lapsana_communis, sec);
        t_lapsana_communis.setUuid(T_LAPSANA_COMMUNIS_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis);

        IBotanicalName n_lapsana_communis_communis = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
        n_lapsana_communis_communis.setTitleCache("L. communis subsp. communis", true);
        Taxon t_lapsana_communis_communis = Taxon.NewInstance(n_lapsana_communis_communis, sec);
        t_lapsana_communis_communis.setUuid(T_LAPSANA_COMMUNIS_COMMUNIS_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis_communis);

        IBotanicalName n_lapsana_communis_adenophora = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
        n_lapsana_communis_adenophora.setTitleCache("L. communis subsp. adenophora", true);
        Taxon t_lapsana_communis_adenophora = Taxon.NewInstance(n_lapsana_communis_adenophora, sec);
        t_lapsana_communis_adenophora.setUuid(T_LAPSANA_COMMUNIS_ADENOPHORA_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis_adenophora);

        IBotanicalName n_lapsana_communis_alpina = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
        n_lapsana_communis_alpina.setTitleCache("L. communis subsp. alpina", true);
        Taxon t_lapsana_communis_alpina = Taxon.NewInstance(n_lapsana_communis_alpina, sec);
        t_lapsana_communis_alpina.setUuid(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis_alpina);

        // --- Classification --- //
        Classification classification = Classification.NewInstance("TestClassification");
        classification.setUuid(CLASSIFICATION_UUID);
        classificationService.save(classification);
        TaxonNode node_lapsana = classification.addChildTaxon(t_lapsana, sec, null);
        node_lapsana.setUuid(TN_LAPSANA_UUID);
        TaxonNode node_lapsana_communis = node_lapsana.addChildTaxon(t_lapsana_communis, sec, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_communis, sec, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_adenophora, sec, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_alpina, sec, null);
        classificationService.saveOrUpdate(classification);

        commitAndStartNewTransaction(null);

        writeDbUnitDataSetFile(new String[] {
                "TAXONBASE", "TAXONNAME","CLASSIFICATION",  "TAXONNODE","HOMOTYPICALGROUP",
                "REFERENCE", "AGENTBASE",
                "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE",
                "LANGUAGESTRING",
                "HIBERNATE_SEQUENCES"
         });
    }


}
