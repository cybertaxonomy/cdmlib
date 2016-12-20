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
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.molecular.ISingleReadDao;
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
    private ISingleReadDao singleReadDao;

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
        deleteResult = occurrenceService.delete(fieldUnit, config);
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
        deleteResult = occurrenceService.delete(derivedUnit, config);
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
        deleteResult = occurrenceService.delete(dnaSample, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 2, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceDeepDeleteTest.testDeepDelete_SingleRead.xml")
    public void testDeepDelete_SingleRead(){
        UUID sequenceA1Uuid = UUID.fromString("3db46d26-94ef-4759-aad8-42d0b9aea9b6");
        UUID sequenceA2Uuid = UUID.fromString("afa3771c-2b9d-46d7-82e0-8b9c050706e3");
        UUID sequenceB1Uuid = UUID.fromString("d7199db5-708e-470a-a573-9c760dd07cd1");
        UUID sequenceB2Uuid = UUID.fromString("1cb83575-38ea-4a8c-9418-d87163f425ce");
        UUID singleReadAUuid = UUID.fromString("82f538a1-2274-4d55-b27b-ed2f004ab5cd");
        UUID singleReadBUuid = UUID.fromString("fc74199a-89dc-40a0-9cbd-08cebebff4b5");

        //how the XML was generated
//        Sequence sequenceA1 = Sequence.NewInstance("A");
//        Sequence sequenceA2 = Sequence.NewInstance("T");
//        Sequence sequenceB1 = Sequence.NewInstance("C");
//        Sequence sequenceB2 = Sequence.NewInstance("G");
//
//        SingleRead singleReadA = SingleRead.NewInstance();
//        SingleRead singleReadB = SingleRead.NewInstance();
//
//        sequenceA1.setUuid(sequenceA1Uuid);
//        sequenceA2.setUuid(sequenceA2Uuid);
//        sequenceB1.setUuid(sequenceB1Uuid);
//        sequenceB1.setUuid(sequenceB2Uuid);
//        singleReadA.setUuid(singleReadAUuid);
//        singleReadB.setUuid(singleReadBUuid);
//
//        SingleReadAlignment.NewInstance(sequenceA1, singleReadA);
//        SingleReadAlignment.NewInstance(sequenceA2, singleReadA);
//        SingleReadAlignment.NewInstance(sequenceB1, singleReadB);
//        SingleReadAlignment.NewInstance(sequenceB2, singleReadB);
//
//        sequenceService.save(sequenceA1);
//        sequenceService.save(sequenceA2);
//        sequenceService.save(sequenceB1);
//        sequenceService.save(sequenceB2);
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "SpecimenOrObservationBase",
//                    "SpecimenOrObservationBase_DerivationEvent",
//                    "DerivationEvent",
//                    "Sequence",
//                    "SingleRead",
//                    "SingleReadAlignment",
//                    "Amplification",
//                    "AmplificationResult",
//                    "DescriptionElementBase",
//                    "DescriptionBase",
//                    "TaxonBase",
//                    "TypeDesignationBase",
//                    "TaxonNameBase",
//                    "TaxonNameBase_TypeDesignationBase",
//                    "HomotypicalGroup"
//            }, "testDeepDelete_SingleRead");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        config.setDeleteMolecularData(true);
        config.setDeleteChildren(true);

        Sequence sequenceA1 = sequenceService.load(sequenceA1Uuid);
        Sequence sequenceA2 = sequenceService.load(sequenceA2Uuid);
        Sequence sequenceB1 = sequenceService.load(sequenceB1Uuid);
        Sequence sequenceB2 = sequenceService.load(sequenceB2Uuid);
        SingleRead singleReadA = singleReadDao.load(singleReadAUuid);
        SingleRead singleReadB = singleReadDao.load(singleReadBUuid);

        //check initial state
        assertNotNull(sequenceA1);
        assertNotNull(sequenceA2);
        assertNotNull(sequenceB1);
        assertNotNull(sequenceB2);
        assertNotNull(singleReadA);
        assertNotNull(singleReadB);
        assertEquals("number of sequences incorrect", 4, sequenceService.count(Sequence.class));
        assertEquals("incorrect number of single reads", 2, singleReadDao.count());

        //A: delete singleRead
        //delete singleReadA from sequenceA1 (singleReadA should NOT be deleted)
        deleteResult = sequenceService.deleteSingleRead(singleReadA, sequenceA1);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("incorrect number of single reads", 2, singleReadDao.count());
        assertEquals(0, sequenceA1.getSingleReadAlignments().size());
        assertNotNull(singleReadDao.load(singleReadAUuid));

        //delete singleReadA from sequenceA2 (singleReadA should be deleted)
        deleteResult = sequenceService.deleteSingleRead(singleReadA, sequenceA2);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("incorrect number of single reads", 1, singleReadDao.count());
        assertEquals(0, sequenceA2.getSingleReadAlignments().size());
        assertTrue(singleReadDao.load(singleReadAUuid)==null);

        //B: delete sequence
        //delete sequenceB1 (singleReadB should NOT be deleted)
        deleteResult = sequenceService.delete(sequenceB1Uuid);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("incorrect number of single reads", 1, singleReadDao.count());
        assertNotNull(singleReadDao.load(singleReadBUuid));

        //delete sequenceB1 (singleReadB should be deleted)
        deleteResult = sequenceService.delete(sequenceB2Uuid);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("incorrect number of single reads", 0, singleReadDao.count());
        assertTrue(singleReadDao.load(singleReadBUuid)==null);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceDeepDeleteTest.testDeepDelete_FieldUnitWithSiblingDerivatives.xml")
    public void testDeepDelete_FieldUnitWithSiblingDerivatives(){
        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        config.setDeleteChildren(true);

        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(FIELD_UNIT_UUID);

        //check initial state
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));

        //delete field unit
        deleteResult = occurrenceService.delete(fieldUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 0, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 0, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
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

        //allow deletion of molecular data
        config.setDeleteMolecularData(true);

        SingleRead singleRead = dnaSample.getAmplificationResults().iterator().next().getSingleReads().iterator().next();
        deleteResult = sequenceService.deleteSingleRead(singleRead, consensusSequence);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("incorrect number of single reads", 0, dnaSample.getAmplificationResults().iterator().next().getSingleReads().size());
        assertEquals("incorrect number of single reads", 0, consensusSequence.getSingleReads().size());
        assertEquals("incorrect number of single reads", 0, singleReadDao.count());

        //delete sequence
        deleteResult = sequenceService.delete(consensusSequence);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("number of sequences incorrect", 0, dnaSample.getSequences().size());


        //delete dna sample
        deleteResult = occurrenceService.delete(dnaSample, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete tissue sample
        deleteResult = occurrenceService.delete(tissueSample, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 2, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete derived unit
        deleteResult = occurrenceService.delete(derivedUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 1, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete field unit
        deleteResult = occurrenceService.delete(fieldUnit, config);

        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 0, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 0, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
    }

    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {
        UUID sequenceA1Uuid = UUID.fromString("3db46d26-94ef-4759-aad8-42d0b9aea9b6");
        UUID sequenceA2Uuid = UUID.fromString("afa3771c-2b9d-46d7-82e0-8b9c050706e3");
        UUID sequenceB1Uuid = UUID.fromString("d7199db5-708e-470a-a573-9c760dd07cd1");
        UUID sequenceB2Uuid = UUID.fromString("1cb83575-38ea-4a8c-9418-d87163f425ce");
        UUID singleReadAUuid = UUID.fromString("82f538a1-2274-4d55-b27b-ed2f004ab5cd");
        UUID singleReadBUuid = UUID.fromString("fc74199a-89dc-40a0-9cbd-08cebebff4b5");

        //how the XML was generated
        Sequence sequenceA1 = Sequence.NewInstance("A");
        Sequence sequenceA2 = Sequence.NewInstance("T");
        Sequence sequenceB1 = Sequence.NewInstance("C");
        Sequence sequenceB2 = Sequence.NewInstance("G");

        SingleRead singleReadA = SingleRead.NewInstance();
        SingleRead singleReadB = SingleRead.NewInstance();

        sequenceA1.setUuid(sequenceA1Uuid);
        sequenceA2.setUuid(sequenceA2Uuid);
        sequenceB1.setUuid(sequenceB1Uuid);
        sequenceB1.setUuid(sequenceB2Uuid);
        singleReadA.setUuid(singleReadAUuid);
        singleReadB.setUuid(singleReadBUuid);

        SingleReadAlignment.NewInstance(sequenceA1, singleReadA);
        SingleReadAlignment.NewInstance(sequenceA2, singleReadA);
        SingleReadAlignment.NewInstance(sequenceB1, singleReadB);
        SingleReadAlignment.NewInstance(sequenceB2, singleReadB);

        sequenceService.save(sequenceA1);
        sequenceService.save(sequenceA2);
        sequenceService.save(sequenceB1);
        sequenceService.save(sequenceB2);

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
            }, "testDeepDelete_SingleRead");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println(sequenceA1.getUuid());
        System.out.println(sequenceA2.getUuid());
        System.out.println(sequenceB1.getUuid());
        System.out.println(sequenceB2.getUuid());
        System.out.println(singleReadA.getUuid());
        System.out.println(singleReadB.getUuid());
    }
}
