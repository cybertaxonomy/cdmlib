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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
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
public class OccurrenceServiceTest extends CdmTransactionalIntegrationTest {

    private static final UUID FIELD_UNIT_UUID = UUID.fromString("b5f58da5-4442-4001-9d13-33f41518b72a");
    private static final UUID DERIVED_UNIT_UUID = UUID.fromString("448be6e7-f19c-4a10-9a0a-97aa005f817d");
    private static final UUID DNA_SAMPLE_UUID = UUID.fromString("bee4212b-aff1-484e-845f-065c7d6216af");
    private static final UUID SEQUENCE_UUID = UUID.fromString("0b867369-de8c-4837-a708-5b7d9f6091be");
    private static final UUID TAXON_UUID = UUID.fromString("e93b3840-326c-446e-89f1-a7611f17c0e8");
    private static final UUID TAXON_DESCRIPTION_UUID = UUID.fromString("a87a893e-2ea8-427d-a26b-dbd2515d6b8a");
    private static final UUID BOTANICAL_NAME_UUID = UUID.fromString("a604774e-d66a-4d47-b9d1-d0e38a8c787a");

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(OccurrenceServiceTest.class);

    @SpringBeanByType
    private IOccurrenceService occurrenceService;

    @SpringBeanByType
    private ISequenceService sequenceService;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

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
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurenceServiceTest.move.xml")
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
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurenceServiceTest.move.xml")
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
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testDeleteIndividualAssociatedAndTypeSpecimen.xml")
    public void testDeleteIndividualAssociatedAndTypeSpecimen(){
//        //how the XML was generated
//        FieldUnit fieldUnit = FieldUnit.NewInstance();
//        fieldUnit.setUuid(FIELD_UNIT_UUID);
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(DERIVED_UNIT_UUID);
//
//        //derivation events
//        DerivationEvent derivationEventFUtoDU = DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
//
//        occurrenceService.save(fieldUnit);
//        occurrenceService.save(derivedUnit);
//
//        //create name with type specimen
//        BotanicalName name = BotanicalName.PARSED_NAME("Campanula patual sec L.");
//        name.setUuid(BOTANICAL_NAME_UUID);
//        SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
//        typeDesignation.setTypeSpecimen(derivedUnit);
//        //add type designation to name
//        name.addTypeDesignation(typeDesignation, false);
//
//        // create taxon with name and two taxon descriptions (one with
//        // IndividualsAssociations and a "described" voucher specimen, and an
//        // empty one)
//        Taxon taxon = Taxon.NewInstance(name, null);
//        taxon.setUuid(TAXON_UUID);
//        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
//        taxonDescription.setUuid(TAXON_DESCRIPTION_UUID);
//        //add voucher
//        taxonDescription.addElement(IndividualsAssociation.NewInstance(fieldUnit));
//        taxon.addDescription(taxonDescription);
//        //add another taxon description to taxon which is not associated with a specimen thus should not be taken into account
//        taxon.addDescription(TaxonDescription.NewInstance());
//        taxonService.saveOrUpdate(taxon);
//
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "SpecimenOrObservationBase",
//                    "SpecimenOrObservationBase_DerivationEvent",
//                    "DerivationEvent",
//                    "Sequence",
//                    "Sequence_SingleRead",
//                    "SingleRead",
//                    "AmplificationResult",
//                    "DescriptionElementBase",
//                    "DescriptionBase",
//                    "TaxonBase",
//                    "TypeDesignationBase",
//                    "TaxonNameBase",
//                    "TaxonNameBase_TypeDesignationBase",
//                    "HomotypicalGroup"
//            }, "testDeleteIndividualAssociatedAndTypeSpecimen");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(FIELD_UNIT_UUID);
        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);
        BotanicalName name = (BotanicalName) nameService.load(BOTANICAL_NAME_UUID);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(TAXON_DESCRIPTION_UUID);
        //check initial state (IndividualsAssociation)
        DescriptionElementBase descriptionElement = taxonDescription.getElements().iterator().next();
        assertTrue("wrong type of description element", descriptionElement.isInstanceOf(IndividualsAssociation.class));
        assertEquals("associated specimen is incorrect", associatedFieldUnit, ((IndividualsAssociation)descriptionElement).getAssociatedSpecimenOrObservation());
        //check initial state (Type Designation)
        Set<TypeDesignationBase> typeDesignations = name.getTypeDesignations();
        TypeDesignationBase typeDesignation = typeDesignations.iterator().next();
        assertTrue("wrong type of type designation", typeDesignation.isInstanceOf(SpecimenTypeDesignation.class));
        assertEquals("type specimen is incorrect", typeSpecimen, ((SpecimenTypeDesignation)typeDesignation).getTypeSpecimen());

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        //delete type specimen from type designation
        config.setDeleteFromTypeDesignation(true);
        occurrenceService.delete(typeSpecimen, config);
        assertTrue(((SpecimenTypeDesignation)typeDesignation).getTypeSpecimen()==null);

        //delete associated field unit from IndividualsAssociation
        config.setDeleteFromIndividualsAssociation(true);
        occurrenceService.delete(associatedFieldUnit, config);
        assertTrue(((IndividualsAssociation)descriptionElement).getAssociatedSpecimenOrObservation()==null);

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceService.loadData.xml")
    public void testLoadData(){
        String fieldUnitUuid = "5a31df5a-2e4d-40b1-8d4e-5754736ae7ef";
        String derivedUnitUuid = "18f70977-5d9c-400a-96c4-0cb7a2cd287e";

        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(UUID.fromString(fieldUnitUuid));
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(UUID.fromString(derivedUnitUuid));

        assertFalse(fieldUnit.getDerivationEvents().iterator().next().getDerivatives().isEmpty());
        assertTrue(derivedUnit.getDerivedFrom()!=null);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testDeleteDerivateHierarchyStepByStep.xml")
    public void testDeleteDerivateHierarchyStepByStep(){
//        //how the XML was generated
//        FieldUnit fieldUnit = FieldUnit.NewInstance();
//        fieldUnit.setUuid(FIELD_UNIT_UUID);
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(DERIVED_UNIT_UUID);
//        DnaSample dnaSample = DnaSample.NewInstance();
//        dnaSample.setUuid(DNA_SAMPLE_UUID);
//
//        //derivation events
//        DerivationEvent derivationEventFUtoDU = DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
//        DerivationEvent derivationEventDUtoDNA = DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());
//
//        //DNA (Sequence, SingleRead, Amplification)
//        Sequence sequence = Sequence.NewInstance(dnaSample, "ATTCG", 5);
//        sequence.setUuid(SEQUENCE_UUID);
//        SingleRead singleRead = SingleRead.NewInstance();
//        sequence.addSingleRead(singleRead);
//        dnaSample.addSequence(sequence);
//        AmplificationResult amplificationResult = AmplificationResult.NewInstance(dnaSample);
//        amplificationResult.addSingleRead(singleRead);
//        occurrenceService.save(fieldUnit);
//        occurrenceService.save(derivedUnit);
//        occurrenceService.save(dnaSample);
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "SpecimenOrObservationBase",
//                    "SpecimenOrObservationBase_DerivationEvent",
//                    "DerivationEvent",
//                    "Sequence",
//                    "Sequence_SingleRead",
//                    "SingleRead",
//                    "AmplificationResult"
//            }, "testDeleteDerivateHierarchyStepByStep");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }


        String assertMessage = "Incorrect number of specimens after deletion.";
        DeleteResult deleteResult = null;
        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        config.setDeleteChildren(false);

        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(FIELD_UNIT_UUID);
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);
        DnaSample dnaSample = (DnaSample) occurrenceService.load(DNA_SAMPLE_UUID);
        Sequence consensusSequence = sequenceService.load(SEQUENCE_UUID);

        //check initial state
        assertEquals(assertMessage, 3, occurrenceService.count(SpecimenOrObservationBase.class));
        assertEquals(assertMessage, 1, occurrenceService.count(FieldUnit.class));
        assertEquals(assertMessage, 2, occurrenceService.count(DerivedUnit.class));
        assertEquals(assertMessage, 1, occurrenceService.count(DnaSample.class));
        assertEquals("number of sequences incorrect", 1, dnaSample.getSequences().size());

        //delete sequence
        deleteResult = occurrenceService.deleteDerivateHierarchy(consensusSequence, config);
        assertTrue(deleteResult.toString(), DeleteResult.DeleteStatus.OK.equals(deleteResult.getStatus()));
        assertEquals("number of sequences incorrect", 0, dnaSample.getSequences().size());


        //delete dna sample
        deleteResult = occurrenceService.deleteDerivateHierarchy(dnaSample, config);
        //should fail because of amplification result and sequence
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        config.setDeleteMolecularData(true);
        //should work now
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
    @DataSet
    public void testListAssociatedAndTypedTaxa(){
//        //how the XML was generated
//        FieldUnit associatedFieldUnit = FieldUnit.NewInstance();
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit typeSpecimen = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        DerivedUnit voucherSpecimen = DerivedUnit.NewInstance(SpecimenOrObservationType.HumanObservation);
//        DnaSample dnaSample = DnaSample.NewInstance();
//        //description for voucher specimen (with InidividualsAssociation to type specimen just to make it complex ;) )
//        SpecimenDescription voucherSpecimenDescription = SpecimenDescription.NewInstance(voucherSpecimen);
//        voucherSpecimenDescription.addElement(IndividualsAssociation.NewInstance(typeSpecimen));
//
//        //derivation events
//        DerivationEvent.NewSimpleInstance(associatedFieldUnit, typeSpecimen, DerivationEventType.ACCESSIONING());
//        DerivationEvent.NewSimpleInstance(associatedFieldUnit, voucherSpecimen, DerivationEventType.ACCESSIONING());
//        DerivationEvent.NewSimpleInstance(typeSpecimen, dnaSample, DerivationEventType.DNA_EXTRACTION());
//
//        //DNA (Sequence, SingleRead, Amplification)
//        Sequence consensusSequence = Sequence.NewInstance(dnaSample, "ATTCG", 5);
//        SingleRead singleRead = SingleRead.NewInstance();
//        consensusSequence.addSingleRead(singleRead);
//        dnaSample.addSequence(consensusSequence);
//        Amplification amplification = Amplification.NewInstance(dnaSample);
//        amplification.addSingleRead(singleRead);
//        occurrenceService.save(associatedFieldUnit);
//        occurrenceService.save(typeSpecimen);
//        occurrenceService.save(dnaSample);
//
//        //create name with type specimen
//        BotanicalName name = BotanicalName.PARSED_NAME("Campanula patual sec L.");
//        SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
//        typeDesignation.setTypeSpecimen(typeSpecimen);
//
//        // create taxon with name and two taxon descriptions (one with
//        // IndividualsAssociations and a "described" voucher specimen, and an
//        // empty one)
//        Taxon taxon = Taxon.NewInstance(name, null);
//        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
//        //add voucher
//        taxonDescription.setDescribedSpecimenOrObservation(voucherSpecimen);
//        taxonDescription.addElement(IndividualsAssociation.NewInstance(associatedFieldUnit));
//        taxon.addDescription(taxonDescription);
//        //add type designation to name
//        name.addTypeDesignation(typeDesignation, false);
//        //add another taxon description to taxon which is not associated with a specimen thus should not be taken into account
//        taxon.addDescription(TaxonDescription.NewInstance());
//        taxonService.saveOrUpdate(taxon);
//
//
//        commitAndStartNewTransaction(new String[]{"SpecimenOrObservationBase",
//                "SpecimenOrObservationBase_DerivationEvent",
//                "DerivationEvent",
//                "Sequence",
//                "Sequence_SingleRead",
//                "SingleRead",
//                "Amplification",
//                "Amplification_SingleRead",
//                "DescriptionElementBase",
//                "DescriptionBase",
//                "TaxonBase",
//                "TypeDesignationBase",
//                "TaxonNameBase",
//                "TaxonNameBase_TypeDesignationBase",
//                "HomotypicalGroup"});
//
//        System.out.println("associatedFieldUnit.getUuid() " + associatedFieldUnit.getUuid());
//        System.out.println("typeSpecimen.getUuid() "+typeSpecimen.getUuid());
//        System.out.println("voucherSpecimen.getUuid() "+voucherSpecimen.getUuid());
//        System.out.println("taxonDescription.getUuid() "+taxonDescription.getUuid());


        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(FIELD_UNIT_UUID);
        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);
        Taxon taxon = (Taxon) taxonService.load(TAXON_UUID);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(TAXON_DESCRIPTION_UUID);
        //check for FieldUnit (IndividualsAssociation)
        java.util.Collection<IndividualsAssociation> individualsAssociations = occurrenceService.listIndividualsAssociations(associatedFieldUnit, null, null, null,null);
        assertEquals("Number of individuals associations is incorrect", 1, individualsAssociations.size());
        IndividualsAssociation individualsAssociation = individualsAssociations.iterator().next();
        assertTrue("association has wrong type", individualsAssociation.getInDescription().isInstanceOf(TaxonDescription.class));
        TaxonDescription retrievedTaxonDescription = HibernateProxyHelper.deproxy(individualsAssociation.getInDescription(), TaxonDescription.class);
        assertEquals(taxonDescription, retrievedTaxonDescription);
        assertEquals("Associated taxon is incorrect", taxon, retrievedTaxonDescription.getTaxon());


        //check for DerivedUnit (Type Designation should exist)
        java.util.Collection<SpecimenTypeDesignation> typeDesignations = occurrenceService.listTypeDesignations(typeSpecimen, null, null, null,null);
        assertEquals("Number of type designations is incorrect", 1, typeDesignations.size());
        SpecimenTypeDesignation specimenTypeDesignation = typeDesignations.iterator().next();
        Set<TaxonNameBase> typifiedNames = specimenTypeDesignation.getTypifiedNames();
        assertEquals("number of typified names is incorrect", 1, typifiedNames.size());
        Set taxonBases = typifiedNames.iterator().next().getTaxonBases();
        assertEquals("number of taxa incorrect", 1, taxonBases.size());
        Object next = taxonBases.iterator().next();
        assertTrue(next instanceof CdmBase && ((CdmBase)next).isInstanceOf(Taxon.class));
        assertEquals("Typed taxon is incorrect", taxon, next);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithSpecimenDescription.xml")
    public void testIsDeletableWithSpecimenDescription(){
//        //how the XML was generated
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(DERIVED_UNIT_UUID);
//        SpecimenDescription specimenDescription = SpecimenDescription.NewInstance();
//        derivedUnit.addDescription(specimenDescription);
//        occurrenceService.save(derivedUnit);
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
//                    "Sequence_SingleRead",
//                    "SingleRead",
//                    "AmplificationResult",
//                    "DescriptionElementBase",
//                    "DescriptionBase",
//                    "TaxonBase",
//                    "TypeDesignationBase",
//                    "TaxonNameBase",
//                    "TaxonNameBase_TypeDesignationBase",
//                    "HomotypicalGroup"
//            }, "testIsDeletableWithSpecimenDescription");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }


        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);
        SpecimenDescription specimenDescription = (SpecimenDescription) derivedUnit.getDescriptions().iterator().next();

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;
        //delete derivedUnit1
        deleteResult = occurrenceService.isDeletable(derivedUnit, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());

        //allow deletion from Descriptions
        config.setDeleteFromDescription(true);
        deleteResult = occurrenceService.isDeletable(derivedUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(specimenDescription));
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithIndividualsAssociationTaxonDescription.xml")
    public void testIsDeletableWithIndividualsAssociationTaxonDescription(){
//        //how the XML was generated
//        FieldUnit fieldUnit = FieldUnit.NewInstance();
//        fieldUnit.setUuid(FIELD_UNIT_UUID);
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(DERIVED_UNIT_UUID);
//
//        //derivation events
//        DerivationEvent derivationEventFUtoDU = DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
//
//        occurrenceService.save(fieldUnit);
//        occurrenceService.save(derivedUnit);
//
//        // create taxon with name and two taxon descriptions (one with
//        // IndividualsAssociations and a "described" voucher specimen, and an
//        // empty one)
//        Taxon taxon = Taxon.NewInstance(BotanicalName.PARSED_NAME("Campanula patual sec L."), null);
//        taxon.setUuid(TAXON_UUID);
//        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
//        taxonDescription.setUuid(TAXON_DESCRIPTION_UUID);
//        //add voucher
//        taxonDescription.addElement(IndividualsAssociation.NewInstance(fieldUnit));
//        taxonDescription.setDescribedSpecimenOrObservation(derivedUnit);
//        taxon.addDescription(taxonDescription);
//        //add another taxon description to taxon which is not associated with a specimen thus should not be taken into account
//        taxon.addDescription(TaxonDescription.NewInstance());
//        taxonService.saveOrUpdate(taxon);
//
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "SpecimenOrObservationBase",
//                    "SpecimenOrObservationBase_DerivationEvent",
//                    "DerivationEvent",
//                    "Sequence",
//                    "Sequence_SingleRead",
//                    "SingleRead",
//                    "AmplificationResult",
//                    "DescriptionElementBase",
//                    "DescriptionBase",
//                    "TaxonBase",
//                    "TypeDesignationBase",
//                    "TaxonNameBase",
//                    "TaxonNameBase_TypeDesignationBase",
//                    "HomotypicalGroup"
//            }, "testIsDeletableWithIndividualsAssociationTaxonDescription");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(FIELD_UNIT_UUID);
        DerivedUnit voucherSpecimen = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);
        DerivationEvent derivationEventFUtoDU = associatedFieldUnit.getDerivationEvents().iterator().next();

        // create taxon with name and two taxon descriptions (one with
        // IndividualsAssociations and a "described" voucher specimen, and an
        // empty one)
        Taxon taxon = (Taxon) taxonService.load(TAXON_UUID);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(TAXON_DESCRIPTION_UUID);
        IndividualsAssociation individualsAssociation = (IndividualsAssociation) taxonDescription.getElements().iterator().next();

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;
        //check deletion of field unit -> should fail because of IndividualAssociation
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(individualsAssociation));
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(derivationEventFUtoDU));

        //allow deletion of individuals association
        config.setDeleteFromIndividualsAssociation(true);
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit, config);
        //should still fail because the field unit has children
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(individualsAssociation));
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(derivationEventFUtoDU));

        //check deletion of voucher specimen -> should fail because of TaxonDescription
        deleteResult = occurrenceService.isDeletable(voucherSpecimen, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(taxonDescription));

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithTypeDesignation.xml")
    public void testIsDeletableWithTypeDesignation(){
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(DERIVED_UNIT_UUID);
//
//        occurrenceService.save(derivedUnit);
//
//        //create name with type specimen
//        BotanicalName name = BotanicalName.PARSED_NAME("Campanula patual sec L.");
//        name.setUuid(BOTANICAL_NAME_UUID);
//        SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
//        typeDesignation.setTypeSpecimen(derivedUnit);
//        //add type designation to name
//        name.addTypeDesignation(typeDesignation, false);
//
//        nameService.saveOrUpdate(name);
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "SpecimenOrObservationBase",
//                    "SpecimenOrObservationBase_DerivationEvent",
//                    "DerivationEvent",
//                    "Sequence",
//                    "Sequence_SingleRead",
//                    "SingleRead",
//                    "AmplificationResult",
//                    "DescriptionElementBase",
//                    "DescriptionBase",
//                    "TaxonBase",
//                    "TypeDesignationBase",
//                    "TaxonNameBase",
//                    "TaxonNameBase_TypeDesignationBase",
//                    "HomotypicalGroup"
//            }, "testIsDeletableWithTypeDesignation");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }



        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);

        //create name with type specimen
        BotanicalName name = (BotanicalName) nameService.load(BOTANICAL_NAME_UUID);
        SpecimenTypeDesignation typeDesignation = (SpecimenTypeDesignation) name.getTypeDesignations().iterator().next();

        //add type designation to name
        name.addTypeDesignation(typeDesignation, false);

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;

        //check deletion of specimen
        deleteResult = occurrenceService.isDeletable(typeSpecimen, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(typeDesignation));

        //allow deletion of type designation
        config.setDeleteFromTypeDesignation(true);

        deleteResult = occurrenceService.isDeletable(typeSpecimen, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(typeDesignation));
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithChildren.xml")
    public void testIsDeletableWithChildren(){
//        //how the XML was generated
//        FieldUnit fieldUnit = FieldUnit.NewInstance();
//        fieldUnit.setUuid(FIELD_UNIT_UUID);
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(DERIVED_UNIT_UUID);
//        DnaSample dnaSample = DnaSample.NewInstance();
//        dnaSample.setUuid(DNA_SAMPLE_UUID);
//
//        //derivation events
//        DerivationEvent derivationEventFUtoDU = DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
//        DerivationEvent derivationEventDUtoDNA = DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());
//
//        occurrenceService.save(fieldUnit);
//        occurrenceService.save(derivedUnit);
//        occurrenceService.save(dnaSample);
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
//            }, "testIsDeletableWithChildren");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(FIELD_UNIT_UUID);
        //sub derivates (DerivedUnit, DnaSample)
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(DERIVED_UNIT_UUID);
        DnaSample dnaSample = (DnaSample) occurrenceService.load(DNA_SAMPLE_UUID);
        //derivation events
        DerivationEvent fieldUnitToDerivedUnitEvent = fieldUnit.getDerivationEvents().iterator().next();
        DerivationEvent derivedUnitToDnaSampleEvent = derivedUnit.getDerivationEvents().iterator().next();

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();

        DeleteResult deleteResult = null;
        //check deletion of DnaSample
        deleteResult = occurrenceService.isDeletable(dnaSample, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(derivedUnitToDnaSampleEvent));

        //check deletion of Specimen
        deleteResult = occurrenceService.isDeletable(derivedUnit, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(derivedUnitToDnaSampleEvent));
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(fieldUnitToDerivedUnitEvent));

        //allow deletion of children
        config.setDeleteChildren(true);

        deleteResult = occurrenceService.isDeletable(derivedUnit, config);
        assertTrue(deleteResult.isOk());

        //check deletion of fieldUnit
        deleteResult = occurrenceService.isDeletable(fieldUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(fieldUnitToDerivedUnitEvent));
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
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
        DerivationEvent derivationEventFUtoDU = DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
        DerivationEvent derivationEventDUtoDNA = DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());

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

        //create name with type specimen
        BotanicalName name = BotanicalName.PARSED_NAME("Campanula patual sec L.");
        name.setUuid(BOTANICAL_NAME_UUID);
        SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
        typeDesignation.setTypeSpecimen(derivedUnit);
        //add type designation to name
        name.addTypeDesignation(typeDesignation, false);

        // create taxon with name and two taxon descriptions (one with
        // IndividualsAssociations and a "described" voucher specimen, and an
        // empty one)
        Taxon taxon = Taxon.NewInstance(name, null);
        taxon.setUuid(TAXON_UUID);
        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
        taxonDescription.setUuid(TAXON_DESCRIPTION_UUID);
        //add voucher
        taxonDescription.addElement(IndividualsAssociation.NewInstance(fieldUnit));
        taxon.addDescription(taxonDescription);
        //add another taxon description to taxon which is not associated with a specimen thus should not be taken into account
        taxon.addDescription(TaxonDescription.NewInstance());
        taxonService.saveOrUpdate(taxon);


        commitAndStartNewTransaction(null);

        setComplete();
        endTransaction();


        writeDbUnitDataSetFile(new String[] {
                "SpecimenOrObservationBase",
                "SpecimenOrObservationBase_DerivationEvent",
                "DerivationEvent",
                "Sequence",
                "Sequence_SingleRead",
                "SingleRead",
                "AmplificationResult",
                "DescriptionElementBase",
                "DescriptionBase",
                "TaxonBase",
                "TypeDesignationBase",
                "TaxonNameBase",
                "TaxonNameBase_TypeDesignationBase",
                "HomotypicalGroup"
        }, "testIsDeletableWithChildren");
    }

}
