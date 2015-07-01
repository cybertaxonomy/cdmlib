// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author pplitzner
 * @date 31.03.2014
 *
 */
public class OccurrenceServiceDeepDeleteTest extends CdmTransactionalIntegrationTest {

    private final UUID FIELD_UNIT_UUID = UUID.fromString("b5f58da5-4442-4001-9d13-33f41518b72a");
    private final UUID DERIVED_UNIT_UUID = UUID.fromString("448be6e7-f19c-4a10-9a0a-97aa005f817d");
    private final UUID DNA_SAMPLE_UUID = UUID.fromString("bee4212b-aff1-484e-845f-065c7d6216af");
    private final UUID SEQUENCE_UUID = UUID.fromString("0b867369-de8c-4837-a708-5b7d9f6091be");

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(OccurrenceServiceDeepDeleteTest.class);

    @SpringBeanByType
    private IOccurrenceService occurrenceService;

    @SpringBeanByType
    private ISequenceService sequenceService;

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testDeleteDerivateHierarchyStepByStep.xml")
    public void testDeepDelete_FieldUnit(){


        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        config.setDeleteMolecularData(true);
        config.setDeleteChildren(true);

        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(FIELD_UNIT_UUID);
        DnaSample dnaSample = (DnaSample) occurrenceService.load(DNA_SAMPLE_UUID);

        //check initial state
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
        assertEquals("incorrect number of amplification results", 1, dnaSample.getAmplificationResults().size());
        assertEquals("number of sequences incorrect", 1, dnaSample.getSequences().size());
        assertEquals("incorrect number of single reads", 1, dnaSample.getAmplificationResults().iterator().next().getSingleReads().size());

        //delete field unit
        deleteResult = occurrenceService.deleteDerivateHierarchy(fieldUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 0, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 0, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDeepDelete_DerivedUnit(){
        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        config.setDeleteMolecularData(true);
        config.setDeleteChildren(true);

        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);
        DnaSample dnaSample = (DnaSample) occurrenceService.load(DNA_SAMPLE_UUID);

        //check initial state
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
        assertEquals("incorrect number of amplification results", 1, dnaSample.getAmplificationResults().size());
        assertEquals("number of sequences incorrect", 1, dnaSample.getSequences().size());
        assertEquals("incorrect number of single reads", 1, dnaSample.getAmplificationResults().iterator().next().getSingleReads().size());

        //delete derived unit
        deleteResult = occurrenceService.deleteDerivateHierarchy(derivedUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 1, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDeepDelete_DnaSample(){
        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        config.setDeleteMolecularData(true);
        config.setDeleteChildren(true);

        DnaSample dnaSample = (DnaSample) occurrenceService.load(DNA_SAMPLE_UUID);

        //check initial state
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
        assertEquals("incorrect number of amplification results", 1, dnaSample.getAmplificationResults().size());
        assertEquals("number of sequences incorrect", 1, dnaSample.getSequences().size());
        assertEquals("incorrect number of single reads", 1, dnaSample.getAmplificationResults().iterator().next().getSingleReads().size());

        //delete dna sample
        deleteResult = occurrenceService.deleteDerivateHierarchy(dnaSample, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 2, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
    }


    @Test
    @DataSet(value="OccurrenceServiceDeepDeleteTest.testDeleteStepByStep.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDeleteDerivateHierarchyStepByStep(){
        UUID fieldUnitUuid = UUID.fromString("4d91a9bc-2af7-40f8-b6e6-545305301807");
        UUID derivedUnitUuid = UUID.fromString("f9c57904-e512-4927-90ad-f3833cdef967");
        UUID tissueSampleUuid = UUID.fromString("14b92fce-1236-455b-ba46-2a7e35d9230e");
        UUID dnaSampleUuid = UUID.fromString("60c31688-edec-4796-aa2f-28a7ea12256b");
        UUID sequenceUuid = UUID.fromString("24804b67-d6f7-48e5-811a-e7240230d305");

        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();

        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnitUuid);
        DerivedUnit tissueSample = (DerivedUnit) occurrenceService.load(tissueSampleUuid);
        DnaSample dnaSample = (DnaSample) occurrenceService.load(dnaSampleUuid);
        Sequence consensusSequence = sequenceService.load(sequenceUuid);

        //check initial state
        assertNotNull(fieldUnit);
        assertNotNull(derivedUnit);
        assertNotNull(tissueSample);
        assertNotNull(dnaSample);
        assertNotNull(consensusSequence);

        assertEquals(assertMessage, 4, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 3, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
        assertEquals("incorrect number of amplification results", 1, dnaSample.getAmplificationResults().size());
        assertEquals("number of sequences incorrect", 1, dnaSample.getSequences().size());
        assertEquals("incorrect number of single reads", 1, dnaSample.getAmplificationResults().iterator().next().getSingleReads().size());
        assertEquals("incorrect number of single reads", 1, consensusSequence.getSingleReads().size());
        assertEquals(consensusSequence.getSingleReads().iterator().next(), dnaSample.getAmplificationResults().iterator().next().getSingleReads().iterator().next());

        //delete sequence -> should fail
        deleteResult = occurrenceService.deleteDerivateHierarchy(consensusSequence, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());

        //allow deletion of molecular data
        config.setDeleteMolecularData(true);

        SingleRead singleRead = dnaSample.getAmplificationResults().iterator().next().getSingleReads().iterator().next();
        deleteResult = occurrenceService.deleteSingleRead(singleRead, consensusSequence);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("incorrect number of single reads", 0, dnaSample.getAmplificationResults().iterator().next().getSingleReads().size());
        assertEquals("incorrect number of single reads", 0, consensusSequence.getSingleReads().size());

        //delete sequence -> should fail
        deleteResult = occurrenceService.deleteDerivateHierarchy(consensusSequence, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("number of sequences incorrect", 0, dnaSample.getSequences().size());


        //delete dna sample
        deleteResult = occurrenceService.deleteDerivateHierarchy(dnaSample, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete tissue sample
        deleteResult = occurrenceService.deleteDerivateHierarchy(tissueSample, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 2, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete derived unit
        deleteResult = occurrenceService.deleteDerivateHierarchy(derivedUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 1, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete field unit
        deleteResult = occurrenceService.deleteDerivateHierarchy(fieldUnit, config);

        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 0, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 0, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
    }

    @Override
    @Test
//    @Ignore
    public void createTestDataSet() throws FileNotFoundException {
        UUID fieldUnitUuid = UUID.fromString("4d91a9bc-2af7-40f8-b6e6-545305301807");
        UUID derivedUnitUuid = UUID.fromString("f9c57904-e512-4927-90ad-f3833cdef967");
        UUID tissueSampleUuid = UUID.fromString("14b92fce-1236-455b-ba46-2a7e35d9230e");
        UUID dnaSampleUuid = UUID.fromString("60c31688-edec-4796-aa2f-28a7ea12256b");
        UUID sequenceUuid = UUID.fromString("24804b67-d6f7-48e5-811a-e7240230d305");

        //how the XML was generated
        FieldUnit fieldUnit = FieldUnit.NewInstance();
        fieldUnit.setUuid(fieldUnitUuid);
        //sub derivates (DerivedUnit, DnaSample)
        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        derivedUnit.setUuid(derivedUnitUuid);
        DerivedUnit tissueSample = DerivedUnit.NewInstance(SpecimenOrObservationType.TissueSample);
        tissueSample.setUuid(tissueSampleUuid);
        DnaSample dnaSample = (DnaSample) DerivedUnit.NewInstance(SpecimenOrObservationType.DnaSample);
        dnaSample.setUuid(dnaSampleUuid);
        Sequence sequence = Sequence.NewInstance("");
        sequence.setUuid(sequenceUuid);
        SingleRead singleRead1 = SingleRead.NewInstance();

        dnaSample.addSequence(sequence);
        sequence.addSingleRead(singleRead1);
        AmplificationResult amplificationResult = AmplificationResult.NewInstance(dnaSample);
        amplificationResult.addSingleRead(singleRead1);

        //derivation events
        DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
        DerivationEvent.NewSimpleInstance(derivedUnit, tissueSample, DerivationEventType.TISSUE_SAMPLING());
        DerivationEvent.NewSimpleInstance(tissueSample, dnaSample, DerivationEventType.DNA_EXTRACTION());

        occurrenceService.save(fieldUnit);
        occurrenceService.save(derivedUnit);
        occurrenceService.save(tissueSample);
        occurrenceService.save(dnaSample);

        commitAndStartNewTransaction(null);

        setComplete();
        endTransaction();

        try {
            writeDbUnitDataSetFile(new String[] {
                    "SpecimenOrObservationBase",
                    "SpecimenOrObservationBase_DerivationEvent",
                    "DerivationEvent",
                    "Sequence",
                    "SingleRead",
                    "SingleReadAlignment",
                    "Amplification",
                    "AmplificationResult",
                    "DescriptionElementBase",
                    "DescriptionBase",
                    "TaxonBase",
                    "TypeDesignationBase",
                    "TaxonNameBase",
                    "TaxonNameBase_TypeDesignationBase",
                    "HomotypicalGroup"
            }, "testDeleteStepByStep");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
