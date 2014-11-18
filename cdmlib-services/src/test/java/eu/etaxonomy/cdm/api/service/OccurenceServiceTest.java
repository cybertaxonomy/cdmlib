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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

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
    public void testGetNonCascadedAssociatedElements(){
        //Collection
        Collection collection = Collection.NewInstance();
        Collection subCollection = Collection.NewInstance();
        subCollection.setSuperCollection(collection);

        Institution institution = Institution.NewInstance();
        institution.addType(DefinedTerm.NewInstitutionTypeInstance("Research and teaching", "botanical garden", "BGBM"));
        collection.setInstitute(institution);

        //Source
        Reference<?> article = ReferenceFactory.newArticle(getReference(), Person.NewInstance(), "title", "pages", "series", "volume", TimePeriod.NewInstance(2014));
        IdentifiableSource source = IdentifiableSource.NewPrimarySourceInstance(article, "microCitation");

        //FieldUnit
        FieldUnit fieldUnit = FieldUnit.NewInstance();
        Person primaryCollector = Person.NewInstance();
        primaryCollector.setLifespan(TimePeriod.NewInstance(2014));
        fieldUnit.setPrimaryCollector(primaryCollector);
        fieldUnit.addSource(source);

        //GatheringEvent
        GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
        fieldUnit.setGatheringEvent(gatheringEvent);
        gatheringEvent.putLocality(Language.ENGLISH(), "locality");
        gatheringEvent.setExactLocation(Point.NewInstance(22.4, -34.2,
                ReferenceSystem.NewInstance("MyReferenceSystem", "label", "labelAbbrev"), 33));
        gatheringEvent.setCountry(Country.GERMANY());
        gatheringEvent.addCollectingArea(NamedArea.EUROPE());

        //Derived Unit
        MediaSpecimen mediaSpecimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
        mediaSpecimen.setCollection(collection);
        BotanicalName storedUnder = BotanicalName.NewInstance(Rank.SPECIES());
        mediaSpecimen.setStoredUnder(storedUnder);
        PreservationMethod preservation = PreservationMethod.NewInstance(null, "My preservation");
        preservation.setMedium(DefinedTerm.NewDnaMarkerInstance("medium", "medium", "medium"));//dummy defined term
        mediaSpecimen.setPreservation(preservation);

        //DerivationEvent
        DerivationEvent event = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
        event.addOriginal(fieldUnit);
        event.addDerivative(mediaSpecimen);

        //SpecOrObservationBase
        fieldUnit.setSex(DefinedTerm.SEX_FEMALE());
        fieldUnit.setLifeStage(DefinedTerm.NewStageInstance("Live stage", "stage", null));
        fieldUnit.setKindOfUnit(DefinedTerm.NewKindOfUnitInstance("Kind of unit", "Kind of unit", null));
        fieldUnit.putDefinition(Language.ENGLISH(), "definition");

        //Determination
        DeterminationEvent determinationEvent = DeterminationEvent.NewInstance(getTaxon(), mediaSpecimen);
        determinationEvent.setModifier(DefinedTerm.NewModifierInstance("modifierDescription", "modifierLabel", "mofifierLabelAbbrev"));
        determinationEvent.setPreferredFlag(true);
        Reference<?> reference = getReference();
        determinationEvent.addReference(reference);

        /*NonCascaded
         * SOOB
         *  - sex (FEMALE)
         *  - stage (Live stage)
         *  - kindOfUnit (Kind of unit)
         * GatheringEvent
         *  - country (GERMANY)
         *  - collectingArea (EUROPE)
         *  DerivedUnit
         *  - storedUnder (botanical name)
         *  DerivedUnit-> Collection -> institiute
         *  - type (botanical garden)
         *
         * */

        assertEquals("Incorrect number of non cascaded CDM entities", 9, occurrenceService.getNonCascadedAssociatedElements(fieldUnit).size());
        assertEquals("Incorrect number of non cascaded CDM entities", 9, occurrenceService.getNonCascadedAssociatedElements(mediaSpecimen).size());

    }
    private Reference<?> getReference() {
        Reference<?> result = ReferenceFactory.newGeneric();
        result.setTitle("some generic reference");
        return result;
   }
   private Taxon getTaxon() {
       Reference<?> sec = getReference();
       TaxonNameBase<?,?> name = BotanicalName.NewInstance(Rank.GENUS());
       Taxon taxon = Taxon.NewInstance(name, sec);
       return taxon;

   }

    @Test
    public void testMoveDerivate(){
        DerivedUnit specimenA = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        DerivedUnit specimenB = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        DerivedUnit dnaSample = DerivedUnit.NewInstance(SpecimenOrObservationType.DnaSample);

        occurrenceService.saveOrUpdate(specimenA);
        occurrenceService.saveOrUpdate(specimenB);
        occurrenceService.saveOrUpdate(dnaSample);

        DerivationEvent originalDerivedFromEvent = DerivationEvent.NewSimpleInstance(specimenA, dnaSample, DerivationEventType.DNA_EXTRACTION());

        occurrenceService.moveDerivate(specimenA, specimenB, dnaSample);
        assertTrue("DerivationEvent not removed from source!", specimenA.getDerivationEvents().isEmpty());
        assertEquals("DerivationEvent not moved to source!", 1, specimenB.getDerivationEvents().size());
        DerivationEvent derivationEvent = specimenB.getDerivationEvents().iterator().next();
        assertEquals("Moved DerivationEvent not of same type!", DerivationEventType.DNA_EXTRACTION(), derivationEvent.getType());
        assertEquals("Wrong number of derivation originals!", 1, derivationEvent.getOriginals().size());
        SpecimenOrObservationBase<?> newOriginal = derivationEvent.getOriginals().iterator().next();
        assertEquals("Origin of moved object not correct", specimenB, newOriginal);
        assertEquals("Wrong number of derivatives!", 1, derivationEvent.getDerivatives().size());
        DerivedUnit movedDerivate = derivationEvent.getDerivatives().iterator().next();
        assertEquals("Moved derivate has wrong type", SpecimenOrObservationType.DnaSample, movedDerivate.getRecordBasis());
        assertNotEquals("DerivationEvent 'derivedFrom' has not been changed after moving", originalDerivedFromEvent, movedDerivate.getDerivedFrom());

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

//    @Test
//    public void testDeleteDerivateHierarchy_FieldUnit(){
//        String assertMessage = "Incorrect number of specimens after deletion.";
//
//        FieldUnit fieldUnit = initDerivateHierarchy();
//
//        //delete field unit
//        occurrenceService.deleteDerivateHierarchy(fieldUnit);
//        commit();
//        assertEquals(assertMessage, 0, occurrenceService.count(SpecimenOrObservationBase.class));
//        assertEquals(assertMessage, 0, occurrenceService.count(FieldUnit.class));
//        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
//        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
//    }

//    @Test
//    public void testDeleteDerivateHierarchy_DerivedUnit(){
//        String assertMessage = "Incorrect number of specimens after deletion.";
//
//        FieldUnit fieldUnit = initDerivateHierarchy();
//
//        //delete derived unit
//        DerivedUnit derivedUnit = fieldUnit.getDerivationEvents().iterator().next().getDerivatives().iterator().next();
//        occurrenceService.deleteDerivateHierarchy(derivedUnit);
//        commit();
//        assertEquals(assertMessage, 1, occurrenceService.count(SpecimenOrObservationBase.class));
//        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
//        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
//        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
//    }


    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="BlankDataSet.xml")
    public void testDeleteDerivateHierarchy_StepByStep(){
        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        config.setDeleteChildren(false);
        config.setShiftHierarchyUp(false);
        FieldUnit fieldUnit = initDerivateHierarchy();

        //check initial state
        DnaSample dnaSample = (DnaSample) fieldUnit.getDerivationEvents().iterator().next().getDerivatives().iterator().next()
                .getDerivationEvents().iterator().next().getDerivatives().iterator().next();
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
        assertEquals("number of sequences incorrect", 1, dnaSample.getSequences().size());

        //delete sequence
        Sequence consensusSequence = ((DnaSample)fieldUnit.getDerivationEvents().iterator().next().getDerivatives().iterator().next()
                .getDerivationEvents().iterator().next().getDerivatives().iterator().next()).getSequences().iterator().next();
        deleteResult = occurrenceService.deleteDerivateHierarchy(consensusSequence, config);
        assertEquals("Deletion status not OK.", DeleteResult.DeleteStatus.OK, deleteResult.getStatus());
        assertEquals("number of sequences incorrect", 0, dnaSample.getSequences().size());


        //delete dna sample
        deleteResult = occurrenceService.deleteDerivateHierarchy(dnaSample, config);
        assertEquals("Deletion status not OK.", DeleteResult.DeleteStatus.OK, deleteResult.getStatus());
        assertEquals(assertMessage, 2, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete derived unit
        DerivedUnit derivedUnit = fieldUnit.getDerivationEvents().iterator().next().getDerivatives().iterator().next();
        deleteResult = occurrenceService.deleteDerivateHierarchy(derivedUnit, config);
        assertEquals("Deletion status not OK.", DeleteResult.DeleteStatus.OK, deleteResult.getStatus());
        assertEquals(assertMessage, 1, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete field unit
        deleteResult = occurrenceService.deleteDerivateHierarchy(fieldUnit, config);
        assertEquals("Deletion status not OK.", DeleteResult.DeleteStatus.OK, deleteResult.getStatus());
        assertEquals(assertMessage, 0, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 0, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));
    }

//    @Test
//    public void testDeleteDerivateHierarchy_Sequence(){
//        String assertMessage = "Incorrect number of specimens after deletion.";
//
//        FieldUnit fieldUnit = initDerivateHierarchy();
//
//        //delete sequence
//        Sequence consensusSequence = ((DnaSample)fieldUnit.getDerivationEvents().iterator().next().getDerivatives().iterator().next()
//        .getDerivationEvents().iterator().next().getDerivatives().iterator().next()).getSequences().iterator().next();
//        occurrenceService.deleteDerivateHierarchy(consensusSequence);
//        commit();
//        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
//        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
//        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
//        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
//    }

    private FieldUnit initDerivateHierarchy(){
        FieldUnit fieldUnit = FieldUnit.NewInstance();
        //sub derivates (DerivedUnit, DnaSample)
        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        DnaSample dnaSample = DnaSample.NewInstance();

        //derivation events
        DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
        DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());

        //DNA (Sequence, SingleRead)
        Sequence consensusSequence = Sequence.NewInstance(dnaSample, "ATTCG", 5);
        SingleRead singleRead = SingleRead.NewInstance();
        consensusSequence.addSingleRead(singleRead);
        dnaSample.addSequence(consensusSequence);
        occurrenceService.save(fieldUnit);
        occurrenceService.save(derivedUnit);
        occurrenceService.save(dnaSample);
        commitAndStartNewTransaction(null);
        return fieldUnit;
    }

}
