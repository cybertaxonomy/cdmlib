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
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author pplitzner
 * @date 31.03.2014
 *
 */
public class OccurenceServiceTest extends CdmTransactionalIntegrationTest {
    private static final Logger logger = Logger.getLogger(OccurenceServiceTest.class);

    @SpringBeanByType
    private IOccurrenceService occurrenceService;

    @SpringBeanByType
    private ISequenceService sequenceService;

    @Test
    public void testMoveDerivate(){
        DerivedUnit specimenA = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        DerivedUnit specimenB = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        DerivedUnit dnaSample = DerivedUnit.NewInstance(SpecimenOrObservationType.DnaSample);

        occurrenceService.saveOrUpdate(specimenA);
        occurrenceService.saveOrUpdate(specimenB);
        occurrenceService.saveOrUpdate(dnaSample);

        DerivationEvent.NewSimpleInstance(specimenA, dnaSample, DerivationEventType.DNA_EXTRACTION());

        occurrenceService.moveDerivate(specimenA, specimenB, dnaSample);
        assertTrue("DerivationEvent not removed from source!", specimenA.getDerivationEvents().isEmpty());
        assertEquals("DerivationEvent not moved to source!", 1, specimenB.getDerivationEvents().size());
        DerivationEvent derivationEvent = specimenB.getDerivationEvents().iterator().next();
        assertEquals("Moved DerivationEvent not of same type!", DerivationEventType.DNA_EXTRACTION(), derivationEvent.getType());
        assertEquals("Wrong number of derivation originals!", 1, derivationEvent.getOriginals().size());
        SpecimenOrObservationBase<?> newOriginal = derivationEvent.getOriginals().iterator().next();
        assertEquals("Origin of moved object not correct", specimenB, newOriginal);
        assertEquals("Wrong number of derivatives!", 1, derivationEvent.getDerivatives().size());
        DerivedUnit derivedUnit = derivationEvent.getDerivatives().iterator().next();
        assertEquals("Moved derivate has wrong type", SpecimenOrObservationType.DnaSample, derivedUnit.getRecordBasis());

    }

    @Test
    public void testMoveSequence(){
        DnaSample dnaSampleA = DnaSample.NewInstance();
        DnaSample dnaSampleB = DnaSample.NewInstance();
        String consensusSequence = "ATTCG";
        Sequence sequence = Sequence.NewInstance(consensusSequence);

        occurrenceService.saveOrUpdate(dnaSampleA);
        occurrenceService.saveOrUpdate(dnaSampleB);
        sequenceService.saveOrUpdate(sequence);

        dnaSampleA.addSequence(sequence);

        occurrenceService.moveSequence(dnaSampleA, dnaSampleB, sequence);
        assertEquals("Number of sequences is wrong", 0, dnaSampleA.getSequences().size());
        assertEquals("Number of sequences is wrong", 1, dnaSampleB.getSequences().size());
        Sequence next = dnaSampleB.getSequences().iterator().next();
        assertEquals("Sequences are not equals", sequence, next);
        assertEquals("Sequences are not equals", consensusSequence, next.getSequenceString());
    }
}
