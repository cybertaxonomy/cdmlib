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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
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
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
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
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 21.11.2019
 */
public class StructuredDescriptionAggregationTest extends CdmTransactionalIntegrationTest {

    private static Logger logger = Logger.getLogger(DistributionAggregationTest.class);

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
        engine = new StructuredDescriptionAggregation();
        monitor = DefaultProgressMonitor.NewInstance();

    }


    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="StructuredDescriptionAggregationTest.xml"),
    })
    public void aggregationTest() throws JvmLimitsException{

        createDefaultFeatureTree();

        DescriptiveDataSet dataSet = DescriptiveDataSet.NewInstance();
        datasetService.save(dataSet);

        SpecimenDescription specDesc = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen1");
        addCategoricalData(specDesc, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDesc, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), 5.0f);

        SpecimenDescription specDesc2 = createSpecimenDescription(dataSet, T_LAPSANA_COMMUNIS_ALPINA_UUID, "alpina specimen2");
        addCategoricalData(specDesc2, uuidFeatureLeafPA, State.uuidPresent);
        addQuantitativeData(specDesc2, uuidFeatureLeafLength, StatisticalMeasure.EXACT_VALUE(), 7.0f);

        TaxonNode tnLapsana = taxonNodeService.find(TN_LAPSANA_UUID);
        Assert.assertNotNull(tnLapsana);
        dataSet.addTaxonSubtree(tnLapsana);

        @SuppressWarnings("unchecked")
        TermTree<Feature> descriptiveSystem = termTreeService.find(uuidFeatureTree);
        dataSet.setDescriptiveSystem(descriptiveSystem);

        commitAndStartNewTransaction();

        TaxonNodeFilter filter = TaxonNodeFilter.NewSubtreeInstance(TN_LAPSANA_UUID);
        StructuredDescriptionAggregationConfiguration config =
                StructuredDescriptionAggregationConfiguration.NewInstance(filter, monitor);
        config.setDatasetUuid(dataSet.getUuid());
        config.setAggregateToHigherRanks(true);
        UpdateResult result = engine.invoke(config, repository);
        Assert.assertEquals(UpdateResult.Status.OK, result.getStatus());

        Taxon taxAlpina = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_ALPINA_UUID);
        testAggregatedDescription(taxAlpina);

        Taxon taxLapsanaCommunis = (Taxon)taxonService.find(T_LAPSANA_COMMUNIS_UUID);
        testAggregatedDescription(taxLapsanaCommunis);

        Taxon taxLapsana = (Taxon)taxonService.find(T_LAPSANA_UUID);
        testAggregatedDescription(taxLapsana);

    }

    private void testAggregatedDescription(Taxon taxon) {
        Set<TaxonDescription> taxonDescriptions = taxon.getDescriptions().stream()
                .filter(desc->desc.getTypes().contains(DescriptionType.AGGREGATED_STRUC_DESC))
                .collect(Collectors.toSet());

        Assert.assertEquals(1, taxonDescriptions.size());
        TaxonDescription aggrDesc = taxonDescriptions.iterator().next();
        Set<DescriptionElementBase> elements = aggrDesc.getElements();
        Assert.assertEquals(2, elements.size());

        Set<CategoricalData> paElements = elements.stream()
                .filter(element->element.getFeature().getUuid().equals(uuidFeatureLeafPA))
                .map(catData->CdmBase.deproxy(catData, CategoricalData.class))
                .collect(Collectors.toSet());
        Assert.assertEquals(1, paElements.size());
        CategoricalData paElement = paElements.iterator().next();
        List<StateData> stateDatas = paElement.getStateData();
        Assert.assertEquals(1, stateDatas.size());
        StateData stateData = stateDatas.iterator().next();
        Assert.assertEquals((Integer)2, stateData.getCount());
        Assert.assertEquals(State.uuidPresent, stateData.getState().getUuid());

        Set<QuantitativeData> leafLengths = elements.stream()
                .filter(element->element.getFeature().getUuid().equals(uuidFeatureLeafLength))
                .map(catData->CdmBase.deproxy(catData, QuantitativeData.class))
                .collect(Collectors.toSet());
        Assert.assertEquals(1, leafLengths.size());
        QuantitativeData leafLength = leafLengths.iterator().next();
        Assert.assertEquals((Float)2f, leafLength.getSampleSize());
        Assert.assertEquals((Float)5f, leafLength.getMin());
        Assert.assertEquals((Float)7f, leafLength.getMax());
        Assert.assertEquals((Float)6f, leafLength.getAverage());
    }

    private void addQuantitativeData(SpecimenDescription specDesc, UUID uuidFeature, StatisticalMeasure type, float value) {
        Feature feature = (Feature)termService.find(uuidFeature);
        QuantitativeData qd = QuantitativeData.NewInstance(feature);
        StatisticalMeasurementValue smv = StatisticalMeasurementValue.NewInstance(type, value);
        qd.addStatisticalValue(smv);
        specDesc.addElement(qd);
    }

    private void addCategoricalData(SpecimenDescription specDesc, UUID featureUuid, UUID stateUUID) {
        Feature featureLeafPA = (Feature)termService.find(featureUuid);
        State statePresent = (State)termService.find(stateUUID);
        CategoricalData cdPresent = CategoricalData.NewInstance(statePresent, featureLeafPA);
        specDesc.addElement(cdPresent);
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

    private State createState(String label) {
        State state = State.NewInstance("", label, "");
        state.getTitleCache();  //for better debugging
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
        State yellow = createState("Yellow");
        State blue = createState("Blue");
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
