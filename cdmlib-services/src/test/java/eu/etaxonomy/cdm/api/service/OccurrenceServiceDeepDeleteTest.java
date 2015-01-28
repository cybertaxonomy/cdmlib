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
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Ignore;
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
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDeleteDerivateHierarchyStepByStep(){
        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();

        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(FIELD_UNIT_UUID);
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);
        DnaSample dnaSample = (DnaSample) occurrenceService.load(DNA_SAMPLE_UUID);
        Sequence consensusSequence = sequenceService.load(SEQUENCE_UUID);

        //check initial state
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
        assertEquals("incorrect number of amplification results", 1, dnaSample.getAmplificationResults().size());
        assertEquals("number of sequences incorrect", 1, dnaSample.getSequences().size());
        assertEquals("incorrect number of single reads", 1, dnaSample.getAmplificationResults().iterator().next().getSingleReads().size());

        //delete single read -> should fail
        SingleRead singleRead = dnaSample.getAmplificationResults().iterator().next().getSingleReads().iterator().next();
        deleteResult = occurrenceService.deleteDerivateHierarchy(singleRead, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        //delete sequence -> should fail
        deleteResult = occurrenceService.deleteDerivateHierarchy(consensusSequence, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());

        //allow deletion of molecular data
        config.setDeleteMolecularData(true);

        deleteResult = occurrenceService.deleteDerivateHierarchy(singleRead, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertTrue(consensusSequence.getSingleReads().isEmpty());

        //delete sequence -> should fail
        deleteResult = occurrenceService.deleteDerivateHierarchy(consensusSequence, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals("number of sequences incorrect", 0, dnaSample.getSequences().size());


        //delete dna sample
        deleteResult = occurrenceService.deleteDerivateHierarchy(dnaSample, config);
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
        deleteResult = occurrenceService.deleteDerivateHierarchy(fieldUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertEquals(assertMessage, 0, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 0, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    @Test
    @Ignore
    public void createTestDataSet() throws FileNotFoundException {
        //how the XML was generated
        FieldUnit fieldUnit = FieldUnit.NewInstance();
        fieldUnit.setUuid(FIELD_UNIT_UUID);
        //sub derivates (DerivedUnit, DnaSample)
        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        derivedUnit.setUuid(DERIVED_UNIT_UUID);
        DnaSample dnaSample = DnaSample.NewInstance();
        dnaSample.setUuid(DNA_SAMPLE_UUID);

        //derivation events
        DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
        DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());

        //DNA (Sequence, SingleRead, Amplification)
        Sequence sequence = Sequence.NewInstance(dnaSample, "ATTCG", 5);
        sequence.setUuid(SEQUENCE_UUID);
        SingleRead singleRead = SingleRead.NewInstance();
        sequence.addSingleRead(singleRead);
        dnaSample.addSequence(sequence);
        AmplificationResult amplificationResult = AmplificationResult.NewInstance(dnaSample);
        amplificationResult.addSingleRead(singleRead);
        occurrenceService.save(fieldUnit);
        occurrenceService.save(derivedUnit);
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
                    "Sequence_SingleRead",
                    "SingleRead",
                    "AmplificationResult"
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
