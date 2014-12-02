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

import java.util.UUID;

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
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author pplitzner
 * @date 31.03.2014
 *
 */
public class OccurrenceServiceTest extends CdmTransactionalIntegrationTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(OccurrenceServiceTest.class);

    @SpringBeanByType
    private IOccurrenceService occurrenceService;

    @SpringBeanByType
    private ISequenceService sequenceService;

    @SpringBeanByType
    private ITaxonService taxonService;

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
    @DataSet(value="OccurenceServiceTest.move.xml")
    public void testMoveDerivate(){
        DerivedUnit specimenA = (DerivedUnit) occurrenceService.load(UUID.fromString("35cfb0b3-588d-4eee-9db6-ac9caa44e39a"));
        DerivedUnit specimenB = (DerivedUnit) occurrenceService.load(UUID.fromString("09496534-efd0-44c8-b1ce-01a34a8a0229"));
        DerivedUnit dnaSample = (DnaSample) occurrenceService.load(UUID.fromString("5995f852-0e78-405c-b849-d923bd6781d9"));


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
    @DataSet(value="OccurenceServiceTest.move.xml")
    public void testMoveSequence(){
        DnaSample dnaSampleA = (DnaSample) occurrenceService.load(UUID.fromString("5995f852-0e78-405c-b849-d923bd6781d9"));
        DnaSample dnaSampleB = (DnaSample) occurrenceService.load(UUID.fromString("85fccc2f-c796-46b3-b2fc-6c9a4d68cfda"));
        String consensusSequence = "ATTCG";
        Sequence sequence = sequenceService.load(UUID.fromString("6da4f378-9861-4338-861b-7b8073763e7a"));

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
    @DataSet //loads OccurrenceServiceTest.xml as base DB
    public void testDeleteDerivateHierarchy_StepByStep(){
        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        config.setDeleteChildren(false);
        config.setShiftHierarchyUp(false);

        //check initial state
        DnaSample dnaSample = (DnaSample) occurrenceService.load(UUID.fromString("2f0e4257-0ce5-4518-b23d-8d87bb04ff7d"));
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
        assertEquals("number of sequences incorrect", 1, dnaSample.getSequences().size());

        //delete sequence
        Sequence consensusSequence = sequenceService.load(UUID.fromString("e3cfdf82-d6bf-4b26-b172-6a057ea3651d"));
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
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(UUID.fromString("a1658d40-d407-4c44-818e-8aabeb0a84d8"));
        deleteResult = occurrenceService.deleteDerivateHierarchy(derivedUnit, config);
        assertEquals("Deletion status not OK.", DeleteResult.DeleteStatus.OK, deleteResult.getStatus());
        assertEquals(assertMessage, 1, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 0, occurrenceService.count(DnaSample.class));

        //delete field unit
        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(UUID.fromString("54a44310-e00a-45d3-aaf0-c0713cc12b45"));
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

    @Test
    @DataSet(value="OccurrenceServiceTest.testListAssociatedAndTypedTaxa.xml")
    public void testListAssociatedAndTypedTaxa(){
        //how the XML was generated
//        FieldUnit associatedFieldUnit = FieldUnit.NewInstance();
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit typeSpecimen = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        DnaSample dnaSample = DnaSample.NewInstance();
//
//        //derivation events
//        DerivationEvent.NewSimpleInstance(associatedFieldUnit, typeSpecimen, DerivationEventType.ACCESSIONING());
//        DerivationEvent.NewSimpleInstance(typeSpecimen, dnaSample, DerivationEventType.DNA_EXTRACTION());
//
//        //DNA (Sequence, SingleRead)
//        Sequence consensusSequence = Sequence.NewInstance(dnaSample, "ATTCG", 5);
//        SingleRead singleRead = SingleRead.NewInstance();
//        consensusSequence.addSingleRead(singleRead);
//        dnaSample.addSequence(consensusSequence);
//        occurrenceService.save(associatedFieldUnit);
//        occurrenceService.save(typeSpecimen);
//        occurrenceService.save(dnaSample);
//        //create name with type specimen
//        BotanicalName name = BotanicalName.PARSED_NAME("Campanula patual sec L.");
//        SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
//        typeDesignation.setTypeSpecimen(typeSpecimen);
//
//        //create taxon with name and taxon description
//        Taxon taxon = Taxon.NewInstance(name, null);
//        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
//        taxonDescription.addElement(IndividualsAssociation.NewInstance(associatedFieldUnit));
//        taxon.addDescription(taxonDescription);
//        //add type designation to name
//        name.addTypeDesignation(typeDesignation, false);
//        //add another taxon description to taxon to create which should not be taken into account
//        taxon.addDescription(TaxonDescription.NewInstance());
//        taxonService.saveOrUpdate(taxon);
//
//
//        commitAndStartNewTransaction(new String[]{"SpecimenOrObservationBase",
//                "DerivationEvent",
//                "Sequence",
//                "Sequence_SingleRead",
//                "SingleRead",
//                "DescriptionElementBase",
//                "DescriptionBase",
//                "TaxonBase",
//                "TypeDesignationBase",
//                "TaxonNameBase",
//                "TaxonNameBase_TypeDesignationBase",
//                "HomotypicalGroup"});

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(UUID.fromString("54a44310-e00a-45d3-aaf0-c0713cc12b45"));
        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(UUID.fromString("a1658d40-d407-4c44-818e-8aabeb0a84d8"));
        Taxon taxon = (Taxon) taxonService.load(UUID.fromString("222ebc0a-6b7c-4aab-93c6-f32e99e94e89"));
        //check for FieldUnit (IndividualsAssociation)
        java.util.Collection<TaxonBase<?>> associatedTaxa = occurrenceService.listAssociatedTaxa(associatedFieldUnit, null, null, null,null);
        assertEquals("Number of associated taxa is incorrect", 1, associatedTaxa.size());
        TaxonBase<?> associatedTaxon = associatedTaxa.iterator().next();
        assertEquals("Associated taxon is incorrect", taxon, associatedTaxon);


        //check for DerivedUnit (Type Designation should exist)
        java.util.Collection<TaxonBase<?>> typedTaxa = occurrenceService.listTypedTaxa(typeSpecimen, null, null, null,null);
        assertEquals("Number of typed taxa is incorrect", 1, typedTaxa.size());
        TaxonBase<?> typedTaxon = typedTaxa.iterator().next();
        assertEquals("Typed taxon is incorrect", taxon, typedTaxon);
    }

    @Deprecated
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
        commitAndStartNewTransaction(new String[]{"SpecimenOrObservationBase",
                "DerivationEvent",
                "Sequence",
                "Sequence_SingleRead",
                "SingleRead"});
        return fieldUnit;
    }

}
