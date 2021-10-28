/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.dto.CategoricalDataDto;
import eu.etaxonomy.cdm.api.service.dto.DescriptionBaseDto;
import eu.etaxonomy.cdm.api.service.dto.DescriptionElementDto;
import eu.etaxonomy.cdm.api.service.dto.QuantitativeDataDto;
import eu.etaxonomy.cdm.api.service.dto.RowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.SpecimenRowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.StateDataDto;
import eu.etaxonomy.cdm.api.service.dto.StatisticalMeasurementValueDto;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Feature;
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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author k.luther
 * @since 01.10.2021
 */
public class DescriptiveDataSetServiceTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(DescriptiveDataSetServiceTest.class);


    private static final UUID T_LAPSANA_UUID = UUID.fromString("f65d47bd-4f49-4ab1-bc4a-bc4551eaa1a8");
    private static final UUID TN_LAPSANA_UUID = UUID.fromString("f4d29e9f-6484-4184-af2e-9704e96a17e3");

    private static final UUID T_LAPSANA_COMMUNIS_UUID = UUID.fromString("2a5ceebb-4830-4524-b330-78461bf8cb6b");

    private static final UUID T_LAPSANA_COMMUNIS_COMMUNIS_UUID = UUID.fromString("441a3c40-0c84-11de-8c30-0800200c9a66");

    private static final UUID T_LAPSANA_COMMUNIS_ADENOPHORA_UUID = UUID.fromString("e4acf200-63b6-11dd-ad8b-0800200c9a66");

    private static final UUID T_LAPSANA_COMMUNIS_ALPINA_UUID = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");

    private static final UUID CLASSIFICATION_UUID = UUID.fromString("4b266053-a841-4980-b548-3f21d8d7d712");

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

//        engine.setBatchMinFreeHeap(100 * 1024 * 1024);
        monitor = DefaultProgressMonitor.NewInstance();
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="StructuredDescriptionAggregationTest.xml"),
    })
    public void testGetRowWrapper(){
        createDefaultFeatureTree();
        DescriptiveDataSet dataSet = createTestDataset();
         commitAndStartNewTransaction();

        List<RowWrapperDTO<?>> result =  datasetService.getRowWrapper(dataSet.getUuid(), monitor);

        //There are 4 specimen descriptions and one literature description (taxon association)
        assertTrue(result.size() == 5);
        //check rowWrapper
        //specimen_1 2 categorical and 1 quantitative
        for (RowWrapperDTO row: result){
            if (row instanceof SpecimenRowWrapperDTO){
                SpecimenRowWrapperDTO specimen = (SpecimenRowWrapperDTO)row;
                if (specimen.getSpecimenDto().getLabel().equals("alpina specimen1")){
                    Set<DescriptionElementDto> elements = specimen.getDataValueForFeature(uuidFeatureLeafColor);
                    if (elements != null && !elements.isEmpty() ){
                        Iterator<DescriptionElementDto> it = elements.iterator();
                        DescriptionElementDto dto = null;
                        if (it.hasNext()){
                            dto = it.next();
                        }else{
                            Assert.fail("There is no element for feature leaf color, but should.");
                        }
                        if (dto instanceof CategoricalDataDto){
                            assertTrue("The states should contain one element", ((CategoricalDataDto)dto).getStates().size() == 1);
                            StateDataDto stateData = ((CategoricalDataDto)dto).getStates().iterator().next();
                            assertEquals(uuidLeafColorBlue, stateData.getState().getUuid());
                        }else{
                            Assert.fail("The element is not of type categorical data");
                        }
                    }else{
                        Assert.fail();
                    }


                    elements = specimen.getDataValueForFeature(uuidFeatureLeafLength);
                    if (elements != null && !elements.isEmpty() ){
                        Iterator<DescriptionElementDto> it = elements.iterator();
                        DescriptionElementDto dto = null;
                        if (it.hasNext()){
                            dto = it.next();
                        }else{
                            Assert.fail("There is no element for feature leaf length, but should.");
                        }
                        if (dto instanceof QuantitativeDataDto){
                            assertTrue("The statistical values should contain one element", ((QuantitativeDataDto)dto).getValues().size() == 1);
                            StatisticalMeasurementValueDto statValue = ((QuantitativeDataDto)dto).getValues().iterator().next();
                            assertEquals(new BigDecimal("5.0"), statValue.getValue());
                        }else{
                            Assert.fail("The element is not of type quantitative data");
                        }
                    }else{
                        Assert.fail();
                    }


                    elements = specimen.getDataValueForFeature(uuidFeatureLeafPA);
//                    assertTrue("The element should be categorical data ", element instanceof CategoricalDataDto);
//                    assertTrue("The statistical values should contain one element", ((CategoricalDataDto)element).getStates().size() == 1);
//                    StateDataDto stateData2 = ((CategoricalDataDto)element).getStates().iterator().next();
//                    assertEquals(State.uuidPresent, stateData2.getState().getUuid());

                }
            }
//            if (row instanceof TaxonRowWrapperDTO){
//                TaxonRowWrapperDTO taxonRow = (TaxonRowWrapperDTO)row;
//                Set<DescriptionElementDto> element = taxonRow.getDataValueForFeature(uuidFeatureLeafLength);
//                assertTrue(element instanceof QuantitativeDataDto);
//                assertTrue(((QuantitativeDataDto)element).getValues().size() == 2);
//            }
        }


        commitAndStartNewTransaction();

    }



    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="StructuredDescriptionAggregationTest.xml"),
    })
    public void addQuantitativeDataTest() {
        createDefaultFeatureTree();
        DescriptiveDataSet dataSet = createTestDataset();
        commitAndStartNewTransaction();

        List<RowWrapperDTO<?>> result = datasetService.getRowWrapper(dataSet.getUuid(), monitor);
        List<DescriptionBaseDto> descToUpdate = new ArrayList<>();
        UUID updatedDescription = null;
        int elementCount = 0;
        for (RowWrapperDTO row: result){
            if (row instanceof SpecimenRowWrapperDTO){
                SpecimenRowWrapperDTO specimen = (SpecimenRowWrapperDTO)row;
                DescriptionBaseDto descDto = specimen.getDescription();
                elementCount = descDto.getElements().size();
                Feature feature = (Feature)termService.find(uuidFeatureLeafLength);
                QuantitativeDataDto quantDto = new QuantitativeDataDto(FeatureDto.fromFeature(feature));
                TermDto typeDto = new TermDto(StatisticalMeasure.EXACT_VALUE().getUuid(), null, StatisticalMeasure.EXACT_VALUE().getTermType(), StatisticalMeasure.EXACT_VALUE().getPartOf() != null?StatisticalMeasure.EXACT_VALUE().getPartOf().getUuid(): null, StatisticalMeasure.EXACT_VALUE().getKindOf()!= null? StatisticalMeasure.EXACT_VALUE().getKindOf().getUuid(): null,
                        StatisticalMeasure.EXACT_VALUE().getVocabulary() != null? StatisticalMeasure.EXACT_VALUE().getVocabulary().getUuid(): null, null, StatisticalMeasure.EXACT_VALUE().getIdInVocabulary(), StatisticalMeasure.EXACT_VALUE().getTitleCache());
                StatisticalMeasurementValueDto statValue = new StatisticalMeasurementValueDto(typeDto, new BigDecimal("4.5"), null);
                Set<StatisticalMeasurementValueDto> values = new HashSet<>();
                values.add(statValue);
                quantDto.setValues(values);
                descDto.addElement(quantDto);
                descToUpdate.add(descDto);
                updatedDescription = descDto.getDescriptionUuid();

            }
        }
        descriptionService.mergeDescriptions(descToUpdate, dataSet.getUuid());

        commitAndStartNewTransaction();
        DescriptionBase description = descriptionService.load(updatedDescription);
        assertEquals(description.getElements().size(),elementCount +1);
        for(Object el: description.getElements()){
            if (el instanceof QuantitativeData){
                QuantitativeData descEl = (QuantitativeData)el;
                if (descEl.getFeature().getUuid().equals(uuidFeatureLeafLength)){
                    assertEquals(descEl.getExactValues().size(),1);
                }
            }
        }

    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="StructuredDescriptionAggregationTest.xml"),
    })
    @Ignore
    public void incompleteCategoricalDataTest() throws JvmLimitsException{
        createDefaultFeatureTree();
        DescriptiveDataSet dataSet = DescriptiveDataSet.NewInstance();
        datasetService.save(dataSet);

        SpecimenDescription specDescAlpina1 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen1");
        addCategoricalData(specDescAlpina1, uuidFeatureLeafColor, null);

        TaxonNode tnLapsana = taxonNodeService.find(TN_LAPSANA_UUID);
        Assert.assertNotNull(tnLapsana);
        dataSet.addTaxonSubtree(tnLapsana);

        @SuppressWarnings("unchecked")
        TermTree<Feature> descriptiveSystem = termTreeService.find(uuidFeatureTree);
        dataSet.setDescriptiveSystem(descriptiveSystem);
        commitAndStartNewTransaction();



        Taxon taxLapsanaCommunisAlpina = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        TaxonDescription aggrDescLapsanaCommunisAlpina = testTaxonDescriptions(taxLapsanaCommunisAlpina, 1);
        List<StateData> sdAlpinaLeafColor = testCategoricalData(uuidFeatureLeafColor, 1, aggrDescLapsanaCommunisAlpina, false);
        testState(sdAlpinaLeafColor, uuidLeafColorBlue, 0);
        testState(sdAlpinaLeafColor, uuidLeafColorYellow, 0);
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





    private DescriptiveDataSet createTestDataset() {
        DescriptiveDataSet dataSet = DescriptiveDataSet.NewInstance();
        dataSet.setLabel("Test dataset");
        datasetService.save(dataSet);

        SpecimenDescription specDescAlpina1 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen1");
        addCategoricalData(specDescAlpina1, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDescAlpina1, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), new BigDecimal("5.0"));
        addCategoricalData(specDescAlpina1, uuidFeatureLeafColor, uuidLeafColorBlue);

        SpecimenDescription specDescAlpina2 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen2");
        addCategoricalData(specDescAlpina2, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDescAlpina2, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), new BigDecimal("7.0"));
        addCategoricalData(specDescAlpina2, uuidFeatureLeafColor, uuidLeafColorBlue);

        SpecimenDescription specDescAlpina3 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen3");
        addCategoricalData(specDescAlpina3, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDescAlpina3, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), new BigDecimal("8.0"));

        SpecimenDescription specDescAdenophora = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ADENOPHORA_UUID, "adenophora specimen");
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

    private SpecimenDescription createSpecimenDescription(DescriptiveDataSet dataSet, UUID taxonUuid, String specLabel ) {
        Taxon taxon = (Taxon)taxonService.find(taxonUuid);
        TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
        DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
        specimen.setTitleCache(specLabel, true);
        IndividualsAssociation individualsAssociation = IndividualsAssociation.NewInstance(specimen);
        // TODO this has to be discussed; currently the description with the InidividualsAssociation is
        // needed in the dataset for performance reasons
        taxonDescription.addElement(individualsAssociation);
        dataSet.addDescription(taxonDescription);
        SpecimenDescription specDesc = SpecimenDescription.NewInstance(specimen);

        dataSet.addDescription(specDesc);
        return specDesc;
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
