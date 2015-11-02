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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.FindOccurrencesConfigurator;
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
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
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

        Person derivationActor = Person.NewTitledInstance("Derivation Actor");
        String derivationDescription = "Derivation Description";
        Institution derivationInstitution = Institution.NewInstance();
        TimePeriod derivationTimePeriod = TimePeriod.NewInstance(2015);

        DerivationEvent originalDerivedFromEvent = DerivationEvent.NewSimpleInstance(specimenA, dnaSample, DerivationEventType.DNA_EXTRACTION());

        originalDerivedFromEvent.setActor(derivationActor);
        originalDerivedFromEvent.setDescription(derivationDescription);
        originalDerivedFromEvent.setInstitution(derivationInstitution);
        originalDerivedFromEvent.setTimeperiod(derivationTimePeriod);

        occurrenceService.moveDerivate(specimenA, specimenB, dnaSample);
        assertTrue("DerivationEvent not removed from source!", specimenA.getDerivationEvents().isEmpty());
        assertEquals("DerivationEvent not moved to source!", 1, specimenB.getDerivationEvents().size());

        DerivationEvent derivationEvent = specimenB.getDerivationEvents().iterator().next();
        assertEquals("Moved DerivationEvent not of same type!", DerivationEventType.DNA_EXTRACTION(), derivationEvent.getType());
        assertEquals(derivationActor, derivationEvent.getActor());
        assertEquals(derivationDescription, derivationEvent.getDescription());
        assertEquals(derivationInstitution, derivationEvent.getInstitution());
        assertEquals(derivationTimePeriod, derivationEvent.getTimeperiod());
        assertEquals(DerivationEventType.DNA_EXTRACTION(), derivationEvent.getType());

        assertEquals("Wrong number of derivation originals!", 1, derivationEvent.getOriginals().size());
        SpecimenOrObservationBase<?> newOriginal = derivationEvent.getOriginals().iterator().next();
        assertEquals("Target of moved object not correct", specimenB, newOriginal);
        assertEquals("Wrong number of derivatives!", 1, derivationEvent.getDerivatives().size());

        DerivedUnit movedDerivate = derivationEvent.getDerivatives().iterator().next();
        assertEquals("Moved derivate has wrong type", SpecimenOrObservationType.DnaSample, movedDerivate.getRecordBasis());
        assertNotEquals("DerivationEvent 'derivedFrom' has not been changed after moving", originalDerivedFromEvent, movedDerivate.getDerivedFrom());

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="BlankDataSet.xml")
    public void testMoveDerivateNoParent(){
        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        FieldUnit fieldUnit = FieldUnit.NewInstance();

        occurrenceService.saveOrUpdate(fieldUnit);
        occurrenceService.saveOrUpdate(derivedUnit);

        assertEquals("DerivationEvent not moved to source!", 0, fieldUnit.getDerivationEvents().size());
        occurrenceService.moveDerivate(null, fieldUnit, derivedUnit);
        assertEquals("DerivationEvent not moved to source!", 1, fieldUnit.getDerivationEvents().size());

        DerivationEvent derivationEvent = fieldUnit.getDerivationEvents().iterator().next();
        assertNull(derivationEvent.getType());

        assertEquals("Wrong number of derivation originals!", 1, derivationEvent.getOriginals().size());
        SpecimenOrObservationBase<?> newOriginal = derivationEvent.getOriginals().iterator().next();
        assertEquals("Target of moved object not correct", fieldUnit, newOriginal);
        assertEquals("Wrong number of derivatives!", 1, derivationEvent.getDerivatives().size());
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

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testDeleteIndividualAssociatedAndTypeSpecimen.xml")
    public void testDeleteIndividualAssociatedAndTypeSpecimen(){
        final UUID taxonDEscriptionUuid = UUID.fromString("a87a893e-2ea8-427d-a26b-dbd2515d6b8a");
        final UUID botanicalNameUuid = UUID.fromString("a604774e-d66a-4d47-b9d1-d0e38a8c787a");
        final UUID fieldUnitUuid = UUID.fromString("67e81ca8-ff91-4df6-bf48-e4600c7f15a2");
        final UUID derivedUnitUuid = UUID.fromString("d229713b-0123-4f15-bffc-76ae45c37564");

        //        //how the XML was generated
//        FieldUnit fieldUnit = FieldUnit.NewInstance();
//        fieldUnit.setUuid(fieldUnitUuid);
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(derivedUnitUuid);
//
//        //derivation events
//        DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
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
//        taxon.setUuid(taxonUuid);
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
//            e.printStackTrace();
//        }

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(derivedUnitUuid);
        BotanicalName name = (BotanicalName) nameService.load(botanicalNameUuid);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(taxonDEscriptionUuid);
        //check initial state (IndividualsAssociation)
        DescriptionElementBase descriptionElement = taxonDescription.getElements().iterator().next();
        assertTrue("wrong type of description element", descriptionElement.isInstanceOf(IndividualsAssociation.class));
        assertEquals("associated specimen is incorrect", associatedFieldUnit, ((IndividualsAssociation)descriptionElement).getAssociatedSpecimenOrObservation());
        //check initial state (Type Designation)
        Set<TypeDesignationBase> typeDesignations = name.getTypeDesignations();
        TypeDesignationBase<?> typeDesignation = typeDesignations.iterator().next();
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
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testListAssociatedAndTypedTaxa.xml")
    public void testListAssociatedAndTypedTaxa(){
        UUID fieldUnitUuid = UUID.fromString("b359ff20-98de-46bf-aa43-3e10bb072cd4");
        UUID typeSpecimenUuid = UUID.fromString("0f8608c7-ffe7-40e6-828c-cb3382580878");
        UUID taxonDescriptionUuid = UUID.fromString("d77db2d4-45a1-4aa1-ab34-f33395f54965");
        UUID taxonUuid = UUID.fromString("b0de794c-8cb7-4369-8f83-870ca37abbe0");
//        //how the XML was generated
//        FieldUnit associatedFieldUnit = FieldUnit.NewInstance();
//        associatedFieldUnit.setUuid(fieldUnitUuid);
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit typeSpecimen = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        typeSpecimen.setUuid(typeSpecimenUuid);
//
//        //derivation events
//        DerivationEvent.NewSimpleInstance(associatedFieldUnit, typeSpecimen, DerivationEventType.ACCESSIONING());
//
//        occurrenceService.save(associatedFieldUnit);
//        occurrenceService.save(typeSpecimen);
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
//        taxon.setUuid(taxonUuid);
//        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
//        taxonDescription.setUuid(taxonDescriptionUuid);
//        //add voucher
//        taxonDescription.addElement(IndividualsAssociation.NewInstance(associatedFieldUnit));
//        taxon.addDescription(taxonDescription);
//        //add type designation to name
//        name.addTypeDesignation(typeDesignation, false);
//        //add another taxon description to taxon which is not associated with a specimen thus should not be taken into account
//        taxon.addDescription(TaxonDescription.NewInstance());
//        taxonService.saveOrUpdate(taxon);
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//        try {
//            writeDbUnitDataSetFile(new String[]{
//                                   "SpecimenOrObservationBase",
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
//                    "HomotypicalGroup"}, "testListAssociatedAndTypedTaxa");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        System.out.println("associatedFieldUnit.getUuid() " + associatedFieldUnit.getUuid());
//        System.out.println("typeSpecimen.getUuid() "+typeSpecimen.getUuid());
//        System.out.println("taxonDescription.getUuid() "+taxonDescription.getUuid());
//        System.out.println("taxon.getUuid() "+taxon.getUuid());


        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(typeSpecimenUuid);
        Taxon taxon = (Taxon) taxonService.load(taxonUuid);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(taxonDescriptionUuid);
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
        Set<?> taxonBases = typifiedNames.iterator().next().getTaxonBases();
        assertEquals("number of taxa incorrect", 1, taxonBases.size());
        Object next = taxonBases.iterator().next();
        assertTrue(next instanceof CdmBase && ((CdmBase)next).isInstanceOf(Taxon.class));
        assertEquals("Typed taxon is incorrect", taxon, next);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithSpecimenDescription.xml")
    public void testIsDeletableWithSpecimenDescription(){
        UUID derivedUnitUuid = UUID.fromString("68095c8e-025d-49f0-8bb2-ed36378b75c3");
        UUID specimenDescriptionUuid = UUID.fromString("4094f947-ce84-47b1-bad5-e57e33239d3c");
//        //how the XML was generated
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(derivedUnitUuid);
//        SpecimenDescription specimenDescription = SpecimenDescription.NewInstance();
//        specimenDescription.setUuid(specimenDescriptionUuid);
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
//            e.printStackTrace();
//        }


        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnitUuid);
        SpecimenDescription specimenDescription = (SpecimenDescription) descriptionService.load(specimenDescriptionUuid);

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;
        //delete derivedUnit1
        deleteResult = occurrenceService.isDeletable(derivedUnit, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());

        //allow deletion from Descriptions
        config.setDeleteFromDescription(true);
        deleteResult = occurrenceService.isDeletable(derivedUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        occurrenceService.delete(derivedUnit, config);
        specimenDescription =  (SpecimenDescription) descriptionService.find(specimenDescriptionUuid);

        assertNull(specimenDescription);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithDescribedSpecimenInTaxonDescription.xml")
    public void testIsDeletableWithDescribedSpecimenInTaxonDescription(){
        UUID fieldUnitUuid = UUID.fromString("d656a004-38ee-404c-810a-87ffb0ab16c2");
//        //how the XML was generated
//        FieldUnit fieldUnit = FieldUnit.NewInstance();
//        fieldUnit.setUuid(fieldUnitUuid);
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//
//        //derivation events
//        DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
//
//        occurrenceService.save(fieldUnit);
//        occurrenceService.save(derivedUnit);
//
//        // create taxon with name and two taxon descriptions
//        //(one with a "described" voucher specimen, and an empty one)
//        Taxon taxon = Taxon.NewInstance(BotanicalName.PARSED_NAME("Campanula patual sec L."), null);
//        taxonDescription.setUuid(taxonDescriptionUuid);
//        //add voucher
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
//            }, "testIsDeletableWithDescribedSpecimenInTaxonDescription");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;
        //check deletion of field unit -> should fail because of voucher specimen (describedSpecimen) in TaxonDescription
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());

        //allow deletion from TaxonDescription and deletion of child derivates
        config.setDeleteFromDescription(true);
        config.setDeleteChildren(true);
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithIndividualsAssociationTaxonDescription.xml")
    public void testIsDeletableWithIndividualsAssociationTaxonDescription(){
        UUID fieldUnitUuid = UUID.fromString("7978b978-5100-4c7a-82ef-3a23e0f3c723");
        UUID taxonDescriptionUuid = UUID.fromString("d4b0d561-6e7e-4fd8-bf3c-925530f949eb");
//        //how the XML was generated
//        FieldUnit fieldUnit = FieldUnit.NewInstance();
//        fieldUnit.setUuid(fieldUnitUuid);
//
//        occurrenceService.save(fieldUnit);
//
//        // create taxon with name and two taxon descriptions (one with
//        // IndividualsAssociations and a "described" voucher specimen, and an
//        // empty one)
//        Taxon taxon = Taxon.NewInstance(BotanicalName.PARSED_NAME("Campanula patual sec L."), null);
//        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
//        taxonDescription.setUuid(taxonDescriptionUuid);
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
//            }, "testIsDeletableWithIndividualsAssociationTaxonDescription");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(taxonDescriptionUuid);
        IndividualsAssociation individualsAssociation = (IndividualsAssociation) taxonDescription.getElements().iterator().next();

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;
        //check deletion of field unit -> should fail because of IndividualAssociation
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(individualsAssociation));

        //allow deletion of individuals association
        config.setDeleteFromIndividualsAssociation(true);
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithTypeDesignation.xml")
    public void testIsDeletableWithTypeDesignation(){
        UUID derivedUnitUuid = UUID.fromString("f7fd1dc1-3c93-42a7-8279-cde5bfe37ea0");
        UUID botanicalNameUuid = UUID.fromString("7396430c-c932-4dd3-a45a-40c2808b132e");
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(derivedUnitUuid);
//
//        occurrenceService.save(derivedUnit);
//
//        //create name with type specimen
//        BotanicalName name = BotanicalName.PARSED_NAME("Campanula patual sec L.");
//        name.setUuid(botanicalNameUuid);
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
//            e.printStackTrace();
//        }



        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(derivedUnitUuid);

        //create name with type specimen
        BotanicalName name = (BotanicalName) nameService.load(botanicalNameUuid);
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
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testIsDeletableWithChildren.xml")

    public void testIsDeletableWithChildren(){
        UUID fieldUnitUuid = UUID.fromString("92ada058-4c14-4131-8ecd-b82dc1dd2882");
        UUID derivedUnitUuid = UUID.fromString("896dffdc-6809-4914-8950-5501fee1c0fd");
        UUID dnaSampleUuid = UUID.fromString("7efd1d66-ac7f-4202-acdf-a72cbb9c3a21");
        //if this test fails be sure to check if there are left-over elements in the DB
        //e.g. clear by adding "<AMPLIFICATIONRESULT/>" to the data set XML

//        //how the XML was generated
//        FieldUnit fieldUnit = FieldUnit.NewInstance();
//        fieldUnit.setUuid(fieldUnitUuid);
//        //sub derivates (DerivedUnit, DnaSample)
//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setUuid(derivedUnitUuid);
//        DnaSample dnaSample = DnaSample.NewInstance();
//        dnaSample.setUuid(dnaSampleUuid);
//
//        //derivation events
//        DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
//        DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());
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
//            e.printStackTrace();
//        }
        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        //sub derivates (DerivedUnit, DnaSample)
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnitUuid);
        DnaSample dnaSample = (DnaSample) occurrenceService.load(dnaSampleUuid);

        //derivation events
        DerivationEvent fieldUnitToDerivedUnitEvent = fieldUnit.getDerivationEvents().iterator().next();
        DerivationEvent derivedUnitToDnaSampleEvent = derivedUnit.getDerivationEvents().iterator().next();

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();

        DeleteResult deleteResult = null;
        //check deletion of DnaSample
        deleteResult = occurrenceService.isDeletable(dnaSample, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());

        //check deletion of Specimen
        deleteResult = occurrenceService.isDeletable(derivedUnit, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(derivedUnitToDnaSampleEvent));

        //check deletion of fieldUnit
        deleteResult = occurrenceService.isDeletable(fieldUnit, config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(fieldUnitToDerivedUnitEvent));



        //check deletion of Specimen
        config.setDeleteChildren(true);
        deleteResult = occurrenceService.isDeletable(derivedUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(derivedUnitToDnaSampleEvent));

        //check deletion of fieldUnit
        config.setDeleteFromDescription(true);
        deleteResult = occurrenceService.isDeletable(fieldUnit, config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testFindOcurrences.xml")
    public void testFindOccurrences(){
        UUID derivedUnit1Uuid = UUID.fromString("843bc8c9-c0fe-4735-bf40-82f1996dcefb");
        UUID derivedUnit2Uuid = UUID.fromString("40cd9cb1-7c74-4e7d-a1f8-8a1e0314e940");
        UUID dnaSampleUuid = UUID.fromString("364969a6-2457-4e2e-ae1e-29a6fcaa741a");
        UUID tissueUuid = UUID.fromString("b608613c-1b5a-4882-8b14-d643b6fc5998");

        UUID taxonUuid = UUID.fromString("dfca7629-8a60-4d51-998d-371897f725e9");

//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setTitleCache("testUnit1");
//        derivedUnit.setAccessionNumber("ACC1");
//        DerivedUnit derivedUnit2 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
//        derivedUnit2.setTitleCache("testUnit2");
//        derivedUnit2.setBarcode("ACC2");
//        DerivedUnit dnaSample = DerivedUnit.NewInstance(SpecimenOrObservationType.DnaSample);
//        dnaSample.setTitleCache("dna");
//        dnaSample.setCatalogNumber("ACC1");
//        DerivedUnit tissue = DerivedUnit.NewInstance(SpecimenOrObservationType.TissueSample);
//        tissue.setTitleCache("tissue");
//
//        DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());
//
//        derivedUnit.setUuid(derivedUnit1Uuid);
//        derivedUnit2.setUuid(derivedUnit2Uuid);
//        dnaSample.setUuid(dnaSampleUuid);
//        tissue.setUuid(tissueUuid);
//
//        occurrenceService.save(derivedUnit);
//        occurrenceService.save(derivedUnit2);
//        occurrenceService.save(dnaSample);
//        occurrenceService.save(tissue);
//
//        Taxon taxon = Taxon.NewInstance(BotanicalName.PARSED_NAME("Campanula patual"), null);
//        taxon.setUuid(taxonUuid);
//        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
//        taxonDescription.setUuid(UUID.fromString("272d4d28-662c-468e-94d8-16993fab91ba"));
//        //add voucher
//        taxonDescription.addElement(IndividualsAssociation.NewInstance(derivedUnit));
//        taxonDescription.addElement(IndividualsAssociation.NewInstance(tissue));
//        taxon.addDescription(taxonDescription);
//        taxonService.saveOrUpdate(taxon);
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
//                    "DescriptionElementBase",
//                    "DescriptionBase",
//                    "TaxonBase",
//                    "TypeDesignationBase",
//                    "TaxonNameBase",
//                    "TaxonNameBase_TypeDesignationBase",
//                    "HomotypicalGroup",
//                    "TeamOrPersonBase"
//            }, "testFindOcurrences");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        SpecimenOrObservationBase derivedUnit1 = occurrenceService.load(derivedUnit1Uuid);
        SpecimenOrObservationBase derivedUnit2 = occurrenceService.load(derivedUnit2Uuid);
        SpecimenOrObservationBase tissue = occurrenceService.load(tissueUuid);
        SpecimenOrObservationBase dnaSample = occurrenceService.load(dnaSampleUuid);
        Taxon taxon = (Taxon) taxonService.load(taxonUuid);

        assertNotNull(derivedUnit1);
        assertNotNull(derivedUnit2);
        assertNotNull(tissue);
        assertNotNull(dnaSample);
        assertNotNull(taxon);

        //wildcard search => all derivates
        FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
        config.setTitleSearchString("*");
        assertEquals(4, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> allDerivates = occurrenceService.findByTitle(config).getRecords();
        assertEquals(4, allDerivates.size());
        assertTrue(allDerivates.contains(derivedUnit1));
        assertTrue(allDerivates.contains(derivedUnit2));
        assertTrue(allDerivates.contains(tissue));
        assertTrue(allDerivates.contains(dnaSample));

        //queryString search => 2 derivates
        config = new FindOccurrencesConfigurator();
        config.setTitleSearchString("test*");
//        config.setClazz(SpecimenOrObservationBase.class);
        assertEquals(2, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> queryStringDerivates = occurrenceService.findByTitle(config).getRecords();
        assertEquals(2, queryStringDerivates.size());
        assertTrue(queryStringDerivates.contains(derivedUnit1));
        assertTrue(queryStringDerivates.contains(derivedUnit2));

        //class search => 0 results
        config = new FindOccurrencesConfigurator();
        config.setClazz(FieldUnit.class);
        assertEquals(0, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> fieldUnits = occurrenceService.findByTitle(config).getRecords();
        assertEquals(0, fieldUnits.size());

        //class search => 4 results
        config = new FindOccurrencesConfigurator();
        config.setClazz(DerivedUnit.class);
        assertEquals(4, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> derivedUnits = occurrenceService.findByTitle(config).getRecords();
        assertEquals(4, derivedUnits.size());
        assertTrue(derivedUnits.contains(derivedUnit1));
        assertTrue(derivedUnits.contains(derivedUnit2));
        assertTrue(derivedUnits.contains(tissue));
        assertTrue(derivedUnits.contains(dnaSample));

        //significant identifier search
        config = new FindOccurrencesConfigurator();
        config.setClazz(DerivedUnit.class);
        config.setSignificantIdentifier("ACC1");
        assertEquals(2, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> accessionedUnits = occurrenceService.findByTitle(config).getRecords();
        assertEquals(2, accessionedUnits.size());
        assertTrue(accessionedUnits.contains(derivedUnit1));
        assertFalse(accessionedUnits.contains(derivedUnit2));
        assertFalse(accessionedUnits.contains(tissue));
        assertTrue(accessionedUnits.contains(dnaSample));

        config = new FindOccurrencesConfigurator();
        config.setClazz(DerivedUnit.class);
        config.setSignificantIdentifier("ACC2");
        assertEquals(1, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> barcodedUnits = occurrenceService.findByTitle(config).getRecords();
        assertEquals(1, barcodedUnits.size());
        assertFalse(barcodedUnits.contains(derivedUnit1));
        assertTrue(barcodedUnits.contains(derivedUnit2));
        assertFalse(barcodedUnits.contains(tissue));
        assertFalse(barcodedUnits.contains(dnaSample));


        //recordBasis search => 1 Fossil
        config = new FindOccurrencesConfigurator();
        config.setSpecimenType(SpecimenOrObservationType.Fossil);
        assertEquals(1, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> fossils = occurrenceService.findByTitle(config).getRecords();
        assertEquals(1, fossils.size());
        assertTrue(fossils.contains(derivedUnit1));

        //taxon determination search => 2 associated specimens
        config = new FindOccurrencesConfigurator();
        config.setClazz(DerivedUnit.class);
        config.setAssociatedTaxonUuid(taxon.getUuid());
        assertEquals(2, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> associatedSpecimens = occurrenceService.findByTitle(config).getRecords();
        assertEquals(2, associatedSpecimens.size());
        assertTrue(associatedSpecimens.contains(derivedUnit1));
        assertTrue(associatedSpecimens.contains(tissue));

        //taxon determination search (indirectly associated) => 3 associated specimens
        config = new FindOccurrencesConfigurator();
        config.setClazz(DerivedUnit.class);
        config.setAssociatedTaxonUuid(taxon.getUuid());
        config.setRetrieveIndirectlyAssociatedSpecimens(true);
        assertEquals(3, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> indirectlyAssociatedSpecimens = occurrenceService.findByTitle(config).getRecords();
        assertEquals(3, indirectlyAssociatedSpecimens.size());
        assertTrue(indirectlyAssociatedSpecimens.contains(derivedUnit1));
        assertTrue(indirectlyAssociatedSpecimens.contains(dnaSample));
        assertTrue(indirectlyAssociatedSpecimens.contains(tissue));

        //using the super class will lead to 0 results because listByAssociatedTaxon does type matching which obviously does not understand inheritance
        config = new FindOccurrencesConfigurator();
        config.setClazz(SpecimenOrObservationBase.class);
        config.setAssociatedTaxonUuid(taxon.getUuid());
        assertEquals(0, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> specimens = occurrenceService.findByTitle(config).getRecords();
        assertEquals(0, specimens.size());

    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {
        UUID derivedUnit1Uuid = UUID.fromString("843bc8c9-c0fe-4735-bf40-82f1996dcefb");
        UUID derivedUnit2Uuid = UUID.fromString("40cd9cb1-7c74-4e7d-a1f8-8a1e0314e940");
        UUID dnaSampleUuid = UUID.fromString("364969a6-2457-4e2e-ae1e-29a6fcaa741a");
        UUID tissueUuid = UUID.fromString("b608613c-1b5a-4882-8b14-d643b6fc5998");

        UUID taxonUuid = UUID.fromString("dfca7629-8a60-4d51-998d-371897f725e9");

        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        derivedUnit.setAccessionNumber("ACC1");
        derivedUnit.setTitleCache("testUnit1", true);
        DerivedUnit derivedUnit2 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        derivedUnit2.setBarcode("ACC2");
        derivedUnit2.setTitleCache("testUnit2", true);
        DerivedUnit dnaSample = DerivedUnit.NewInstance(SpecimenOrObservationType.DnaSample);
        dnaSample.setTitleCache("dna", true);
        dnaSample.setCatalogNumber("ACC1");
        DerivedUnit tissue = DerivedUnit.NewInstance(SpecimenOrObservationType.TissueSample);
        tissue.setTitleCache("tissue", true);

        DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample, DerivationEventType.DNA_EXTRACTION());

        derivedUnit.setUuid(derivedUnit1Uuid);
        derivedUnit2.setUuid(derivedUnit2Uuid);
        dnaSample.setUuid(dnaSampleUuid);
        tissue.setUuid(tissueUuid);

        occurrenceService.save(derivedUnit);
        occurrenceService.save(derivedUnit2);
        occurrenceService.save(dnaSample);
        occurrenceService.save(tissue);

        Taxon taxon = Taxon.NewInstance(BotanicalName.PARSED_NAME("Campanula patual"), null);
        taxon.setUuid(taxonUuid);
        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
        taxonDescription.setUuid(UUID.fromString("272d4d28-662c-468e-94d8-16993fab91ba"));
        //add voucher
        taxonDescription.addElement(IndividualsAssociation.NewInstance(derivedUnit));
        taxonDescription.addElement(IndividualsAssociation.NewInstance(tissue));
        taxon.addDescription(taxonDescription);
        taxonService.saveOrUpdate(taxon);

        commitAndStartNewTransaction(null);

        setComplete();
        endTransaction();


        try {
            writeDbUnitDataSetFile(new String[] {
                    "SpecimenOrObservationBase",
                    "SpecimenOrObservationBase_DerivationEvent",
                    "DerivationEvent",
                    "DescriptionElementBase",
                    "DescriptionBase",
                    "TaxonBase",
                    "TypeDesignationBase",
                    "TaxonNameBase",
                    "TaxonNameBase_TypeDesignationBase",
                    "HomotypicalGroup",
                    "TeamOrPersonBase"
            }, "testFindOcurrences");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
