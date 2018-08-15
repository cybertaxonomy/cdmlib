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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.query.AssignmentStatus;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author pplitzner
 * @since 31.03.2014
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
    private ITaxonNodeService taxonNodeService;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

    private Reference getReference() {
        Reference result = ReferenceFactory.newGeneric();
        result.setTitle("some generic reference");
        return result;
    }


    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurenceServiceTest.move.xml")
    public void testMoveDerivate() {
        DerivedUnit specimenA = (DerivedUnit) occurrenceService.load(UUID
                .fromString("35cfb0b3-588d-4eee-9db6-ac9caa44e39a"));
        DerivedUnit specimenB = (DerivedUnit) occurrenceService.load(UUID
                .fromString("09496534-efd0-44c8-b1ce-01a34a8a0229"));
        DerivedUnit dnaSample = (DnaSample) occurrenceService.load(UUID
                .fromString("5995f852-0e78-405c-b849-d923bd6781d9"));

        occurrenceService.saveOrUpdate(specimenA);
        occurrenceService.saveOrUpdate(specimenB);
        occurrenceService.saveOrUpdate(dnaSample);

        Person derivationActor = Person.NewTitledInstance("Derivation Actor");
        String derivationDescription = "Derivation Description";
        Institution derivationInstitution = Institution.NewInstance();
        TimePeriod derivationTimePeriod = TimePeriod.NewInstance(2015);

        DerivationEvent originalDerivedFromEvent = DerivationEvent.NewSimpleInstance(specimenA, dnaSample,
                DerivationEventType.DNA_EXTRACTION());

        originalDerivedFromEvent.setActor(derivationActor);
        originalDerivedFromEvent.setDescription(derivationDescription);
        originalDerivedFromEvent.setInstitution(derivationInstitution);
        originalDerivedFromEvent.setTimeperiod(derivationTimePeriod);

        occurrenceService.moveDerivate(specimenA, specimenB, dnaSample);
        assertTrue("DerivationEvent not removed from source!", specimenA.getDerivationEvents().isEmpty());
        assertEquals("DerivationEvent not moved to source!", 1, specimenB.getDerivationEvents().size());

        DerivationEvent derivationEvent = specimenB.getDerivationEvents().iterator().next();
        assertEquals("Moved DerivationEvent not of same type!", DerivationEventType.DNA_EXTRACTION(),
                derivationEvent.getType());
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
        assertEquals("Moved derivate has wrong type", SpecimenOrObservationType.DnaSample,
                movedDerivate.getRecordBasis());
        assertNotEquals("DerivationEvent 'derivedFrom' has not been changed after moving", originalDerivedFromEvent,
                movedDerivate.getDerivedFrom());

    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "../../database/BlankDataSet.xml")
    public void testMoveDerivateNoParent() {
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
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurenceServiceTest.move.xml")
    public void testMoveSequence() {
        DnaSample dnaSampleA = (DnaSample) occurrenceService.load(UUID
                .fromString("5995f852-0e78-405c-b849-d923bd6781d9"));
        DnaSample dnaSampleB = (DnaSample) occurrenceService.load(UUID
                .fromString("85fccc2f-c796-46b3-b2fc-6c9a4d68cfda"));
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
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testDeleteIndividualAssociatedAndTypeSpecimen.xml")
    public void testDeleteIndividualAssociatedAndTypeSpecimen() {
        final UUID taxonDEscriptionUuid = UUID.fromString("a87a893e-2ea8-427d-a26b-dbd2515d6b8a");
        final UUID botanicalNameUuid = UUID.fromString("a604774e-d66a-4d47-b9d1-d0e38a8c787a");
        final UUID fieldUnitUuid = UUID.fromString("67e81ca8-ff91-4df6-bf48-e4600c7f15a2");
        final UUID derivedUnitUuid = UUID.fromString("d229713b-0123-4f15-bffc-76ae45c37564");

        // //how the XML was generated
        // FieldUnit fieldUnit = FieldUnit.NewInstance();
        // fieldUnit.setUuid(fieldUnitUuid);
        // //sub derivates (DerivedUnit, DnaSample)
        // DerivedUnit derivedUnit =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        // derivedUnit.setUuid(derivedUnitUuid);
        //
        // //derivation events
        // DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit,
        // DerivationEventType.ACCESSIONING());
        //
        // occurrenceService.save(fieldUnit);
        // occurrenceService.save(derivedUnit);
        //
        // //create name with type specimen
        // BotanicalName name =
        // BotanicalName.PARSED_NAME("Campanula patual sec L.");
        // name.setUuid(BOTANICAL_NAME_UUID);
        // SpecimenTypeDesignation typeDesignation =
        // SpecimenTypeDesignation.NewInstance();
        // typeDesignation.setTypeSpecimen(derivedUnit);
        // //add type designation to name
        // name.addTypeDesignation(typeDesignation, false);
        //
        // // create taxon with name and two taxon descriptions (one with
        // // IndividualsAssociations and a "described" voucher specimen, and an
        // // empty one)
        // Taxon taxon = Taxon.NewInstance(name, null);
        // taxon.setUuid(taxonUuid);
        // TaxonDescription taxonDescription = TaxonDescription.NewInstance();
        // taxonDescription.setUuid(TAXON_DESCRIPTION_UUID);
        // //add voucher
        // taxonDescription.addElement(IndividualsAssociation.NewInstance(fieldUnit));
        // taxon.addDescription(taxonDescription);
        // //add another taxon description to taxon which is not associated with
        // a specimen thus should not be taken into account
        // taxon.addDescription(TaxonDescription.NewInstance());
        // taxonService.saveOrUpdate(taxon);
        //
        //
        // commitAndStartNewTransaction(null);
        //
        // setComplete();
        // endTransaction();
        //
        //
        // try {
        // writeDbUnitDataSetFile(new String[] {
        // "SpecimenOrObservationBase",
        // "SpecimenOrObservationBase_DerivationEvent",
        // "DerivationEvent",
        // "Sequence",
        // "Sequence_SingleRead",
        // "SingleRead",
        // "AmplificationResult",
        // "DescriptionElementBase",
        // "DescriptionBase",
        // "TaxonBase",
        // "TypeDesignationBase",
        // "TaxonName",
        // "TaxonName_TypeDesignationBase",
        // "HomotypicalGroup"
        // }, "testDeleteIndividualAssociatedAndTypeSpecimen");
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(derivedUnitUuid);
        IBotanicalName name = nameService.load(botanicalNameUuid);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(taxonDEscriptionUuid);
        // check initial state (IndividualsAssociation)
        DescriptionElementBase descriptionElement = taxonDescription.getElements().iterator().next();
        assertTrue("wrong type of description element", descriptionElement.isInstanceOf(IndividualsAssociation.class));
        assertEquals("associated specimen is incorrect", associatedFieldUnit,
                ((IndividualsAssociation) descriptionElement).getAssociatedSpecimenOrObservation());
        // check initial state (Type Designation)
        Set<TypeDesignationBase> typeDesignations = name.getTypeDesignations();
        TypeDesignationBase<?> typeDesignation = typeDesignations.iterator().next();
        assertTrue("wrong type of type designation", typeDesignation.isInstanceOf(SpecimenTypeDesignation.class));
        assertEquals("type specimen is incorrect", typeSpecimen,
                ((SpecimenTypeDesignation) typeDesignation).getTypeSpecimen());

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        // delete type specimen from type designation
        config.setDeleteFromTypeDesignation(true);
        occurrenceService.delete(typeSpecimen, config);
        assertTrue(((SpecimenTypeDesignation) typeDesignation).getTypeSpecimen() == null);

        // delete associated field unit from IndividualsAssociation
        config.setDeleteFromIndividualsAssociation(true);
        occurrenceService.delete(associatedFieldUnit, config);
        assertTrue(((IndividualsAssociation) descriptionElement).getAssociatedSpecimenOrObservation() == null);

    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceService.loadData.xml")
    public void testLoadData() {
        String fieldUnitUuid = "5a31df5a-2e4d-40b1-8d4e-5754736ae7ef";
        String derivedUnitUuid = "18f70977-5d9c-400a-96c4-0cb7a2cd287e";

        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(UUID.fromString(fieldUnitUuid));
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(UUID.fromString(derivedUnitUuid));

        assertFalse(fieldUnit.getDerivationEvents().iterator().next().getDerivatives().isEmpty());
        assertTrue(derivedUnit.getDerivedFrom() != null);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testListAssociatedAndTypedTaxa.xml")
    public void testListIndividualsAssociatensAndSpecimenTypeDesignations(){
        UUID fieldUnitUuid = UUID.fromString("b359ff20-98de-46bf-aa43-3e10bb072cd4");
        UUID typeSpecimenUuid = UUID.fromString("0f8608c7-ffe7-40e6-828c-cb3382580878");
        UUID taxonDescriptionUuid = UUID.fromString("d77db2d4-45a1-4aa1-ab34-f33395f54965");
        UUID taxonUuid = UUID.fromString("b0de794c-8cb7-4369-8f83-870ca37abbe0");
        // //how the XML was generated
        // FieldUnit associatedFieldUnit = FieldUnit.NewInstance();
        // associatedFieldUnit.setUuid(fieldUnitUuid);
        // //sub derivates (DerivedUnit, DnaSample)
        // DerivedUnit typeSpecimen =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        // typeSpecimen.setUuid(typeSpecimenUuid);
        //
        // //derivation events
        // DerivationEvent.NewSimpleInstance(associatedFieldUnit, typeSpecimen,
        // DerivationEventType.ACCESSIONING());
        //
        // occurrenceService.save(associatedFieldUnit);
        // occurrenceService.save(typeSpecimen);
        //
        // //create name with type specimen
        // BotanicalName name =
        // BotanicalName.PARSED_NAME("Campanula patual sec L.");
        // SpecimenTypeDesignation typeDesignation =
        // SpecimenTypeDesignation.NewInstance();
        // typeDesignation.setTypeSpecimen(typeSpecimen);
        //
        // // create taxon with name and two taxon descriptions (one with
        // // IndividualsAssociations and a "described" voucher specimen, and an
        // // empty one)
        // Taxon taxon = Taxon.NewInstance(name, null);
        // taxon.setUuid(taxonUuid);
        // TaxonDescription taxonDescription = TaxonDescription.NewInstance();
        // taxonDescription.setUuid(taxonDescriptionUuid);
        // //add voucher
        // taxonDescription.addElement(IndividualsAssociation.NewInstance(associatedFieldUnit));
        // taxon.addDescription(taxonDescription);
        // //add type designation to name
        // name.addTypeDesignation(typeDesignation, false);
        // //add another taxon description to taxon which is not associated with
        // a specimen thus should not be taken into account
        // taxon.addDescription(TaxonDescription.NewInstance());
        // taxonService.saveOrUpdate(taxon);
        //
        // commitAndStartNewTransaction(null);
        //
        // setComplete();
        // endTransaction();
        //
        // try {
        // writeDbUnitDataSetFile(new String[]{
        // "SpecimenOrObservationBase",
        // "SpecimenOrObservationBase_DerivationEvent",
        // "DerivationEvent",
        // "Sequence",
        // "Sequence_SingleRead",
        // "SingleRead",
        // "AmplificationResult",
        // "DescriptionElementBase",
        // "DescriptionBase",
        // "TaxonBase",
        // "TypeDesignationBase",
        // "TaxonName",
        // "TaxonName_TypeDesignationBase",
        // "HomotypicalGroup"}, "testListAssociatedAndTypedTaxa");
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }
        //
        // System.out.println("associatedFieldUnit.getUuid() " +
        // associatedFieldUnit.getUuid());
        // System.out.println("typeSpecimen.getUuid() "+typeSpecimen.getUuid());
        // System.out.println("taxonDescription.getUuid() "+taxonDescription.getUuid());
        // System.out.println("taxon.getUuid() "+taxon.getUuid());

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(typeSpecimenUuid);
        Taxon taxon = (Taxon) taxonService.load(taxonUuid);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(taxonDescriptionUuid);
        // check for FieldUnit (IndividualsAssociation)
        java.util.Collection<IndividualsAssociation> individualsAssociations = occurrenceService
                .listIndividualsAssociations(associatedFieldUnit, null, null, null, null);
        assertEquals("Number of individuals associations is incorrect", 1, individualsAssociations.size());
        IndividualsAssociation individualsAssociation = individualsAssociations.iterator().next();
        assertTrue("association has wrong type",
                individualsAssociation.getInDescription().isInstanceOf(TaxonDescription.class));
        TaxonDescription retrievedTaxonDescription = HibernateProxyHelper.deproxy(
                individualsAssociation.getInDescription(), TaxonDescription.class);
        assertEquals(taxonDescription, retrievedTaxonDescription);
        assertEquals("Associated taxon is incorrect", taxon, retrievedTaxonDescription.getTaxon());

        // check for DerivedUnit (Type Designation should exist)
        java.util.Collection<SpecimenTypeDesignation> typeDesignations = occurrenceService.listTypeDesignations(
                typeSpecimen, null, null, null, null);
        assertEquals("Number of type designations is incorrect", 1, typeDesignations.size());
        SpecimenTypeDesignation specimenTypeDesignation = typeDesignations.iterator().next();
        Set<TaxonName> typifiedNames = specimenTypeDesignation.getTypifiedNames();
        assertEquals("number of typified names is incorrect", 1, typifiedNames.size());
        Set<?> taxonBases = typifiedNames.iterator().next().getTaxonBases();
        assertEquals("number of taxa incorrect", 1, taxonBases.size());
        Object next = taxonBases.iterator().next();
        assertTrue(next instanceof CdmBase && ((CdmBase) next).isInstanceOf(Taxon.class));
        assertEquals("Typed taxon is incorrect", taxon, next);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testIsDeletableWithSpecimenDescription.xml")
    public void testIsDeletableWithSpecimenDescription() {
        UUID derivedUnitUuid = UUID.fromString("68095c8e-025d-49f0-8bb2-ed36378b75c3");
        UUID specimenDescriptionUuid = UUID.fromString("4094f947-ce84-47b1-bad5-e57e33239d3c");
        // //how the XML was generated
        // DerivedUnit derivedUnit =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        // derivedUnit.setUuid(derivedUnitUuid);
        // SpecimenDescription specimenDescription =
        // SpecimenDescription.NewInstance();
        // specimenDescription.setUuid(specimenDescriptionUuid);
        // derivedUnit.addDescription(specimenDescription);
        // occurrenceService.save(derivedUnit);
        //
        // commitAndStartNewTransaction(null);
        //
        // setComplete();
        // endTransaction();
        //
        // try {
        // writeDbUnitDataSetFile(new String[] {
        // "SpecimenOrObservationBase",
        // "SpecimenOrObservationBase_DerivationEvent",
        // "DerivationEvent",
        // "Sequence",
        // "Sequence_SingleRead",
        // "SingleRead",
        // "AmplificationResult",
        // "DescriptionElementBase",
        // "DescriptionBase",
        // "TaxonBase",
        // "TypeDesignationBase",
        // "TaxonName",
        // "TaxonName_TypeDesignationBase",
        // "HomotypicalGroup"
        // }, "testIsDeletableWithSpecimenDescription");
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnitUuid);
        SpecimenDescription specimenDescription = (SpecimenDescription) descriptionService
                .load(specimenDescriptionUuid);

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;
        // delete derivedUnit1
        deleteResult = occurrenceService.isDeletable(derivedUnit.getUuid(), config);
        //deletion of specimen description should always work because there are no
        //specimen description without a specimen
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        occurrenceService.delete(derivedUnit, config);
        specimenDescription = (SpecimenDescription) descriptionService.find(specimenDescriptionUuid);

        assertNull(specimenDescription);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testIsDeletableWithDescribedSpecimenInTaxonDescription.xml")
    public void testIsDeletableWithDescribedSpecimenInTaxonDescription() {
        UUID fieldUnitUuid = UUID.fromString("d656a004-38ee-404c-810a-87ffb0ab16c2");
        // //how the XML was generated
        // FieldUnit fieldUnit = FieldUnit.NewInstance();
        // fieldUnit.setUuid(fieldUnitUuid);
        // DerivedUnit derivedUnit =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        //
        // //derivation events
        // DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit,
        // DerivationEventType.ACCESSIONING());
        //
        // occurrenceService.save(fieldUnit);
        // occurrenceService.save(derivedUnit);
        //
        // // create taxon with name and two taxon descriptions
        // //(one with a "described" voucher specimen, and an empty one)
        // Taxon taxon =
        // Taxon.NewInstance(BotanicalName.PARSED_NAME("Campanula patual sec L."),
        // null);
        // taxonDescription.setUuid(taxonDescriptionUuid);
        // //add voucher
        // taxonDescription.setDescribedSpecimenOrObservation(derivedUnit);
        // taxon.addDescription(taxonDescription);
        // //add another taxon description to taxon which is not associated with
        // a specimen thus should not be taken into account
        // taxon.addDescription(TaxonDescription.NewInstance());
        // taxonService.saveOrUpdate(taxon);
        //
        //
        // commitAndStartNewTransaction(null);
        //
        // setComplete();
        // endTransaction();
        //
        //
        // try {
        // writeDbUnitDataSetFile(new String[] {
        // "SpecimenOrObservationBase",
        // "SpecimenOrObservationBase_DerivationEvent",
        // "DerivationEvent",
        // "Sequence",
        // "Sequence_SingleRead",
        // "SingleRead",
        // "AmplificationResult",
        // "DescriptionElementBase",
        // "DescriptionBase",
        // "TaxonBase",
        // "TypeDesignationBase",
        // "TaxonName",
        // "TaxonName_TypeDesignationBase",
        // "HomotypicalGroup"
        // }, "testIsDeletableWithDescribedSpecimenInTaxonDescription");
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;
        // check deletion of field unit -> should fail because of voucher
        // specimen (describedSpecimen) in TaxonDescription
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit.getUuid(), config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());

        // allow deletion from TaxonDescription and deletion of child derivates
        config.setDeleteFromDescription(true);
        config.setDeleteChildren(true);
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit.getUuid(), config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testIsDeletableWithIndividualsAssociationTaxonDescription.xml")
    public void testIsDeletableWithIndividualsAssociationTaxonDescription() {
        UUID fieldUnitUuid = UUID.fromString("7978b978-5100-4c7a-82ef-3a23e0f3c723");
        UUID taxonDescriptionUuid = UUID.fromString("d4b0d561-6e7e-4fd8-bf3c-925530f949eb");
        // //how the XML was generated
        // FieldUnit fieldUnit = FieldUnit.NewInstance();
        // fieldUnit.setUuid(fieldUnitUuid);
        //
        // occurrenceService.save(fieldUnit);
        //
        // // create taxon with name and two taxon descriptions (one with
        // // IndividualsAssociations and a "described" voucher specimen, and an
        // // empty one)
        // Taxon taxon =
        // Taxon.NewInstance(BotanicalName.PARSED_NAME("Campanula patual sec L."),
        // null);
        // TaxonDescription taxonDescription = TaxonDescription.NewInstance();
        // taxonDescription.setUuid(taxonDescriptionUuid);
        // //add voucher
        // taxonDescription.addElement(IndividualsAssociation.NewInstance(fieldUnit));
        // taxon.addDescription(taxonDescription);
        // //add another taxon description to taxon which is not associated with
        // a specimen thus should not be taken into account
        // taxon.addDescription(TaxonDescription.NewInstance());
        // taxonService.saveOrUpdate(taxon);
        //
        //
        // commitAndStartNewTransaction(null);
        //
        // setComplete();
        // endTransaction();
        //
        //
        // try {
        // writeDbUnitDataSetFile(new String[] {
        // "SpecimenOrObservationBase",
        // "SpecimenOrObservationBase_DerivationEvent",
        // "DerivationEvent",
        // "Sequence",
        // "Sequence_SingleRead",
        // "SingleRead",
        // "AmplificationResult",
        // "DescriptionElementBase",
        // "DescriptionBase",
        // "TaxonBase",
        // "TypeDesignationBase",
        // "TaxonName",
        // "TaxonName_TypeDesignationBase",
        // "HomotypicalGroup"
        // }, "testIsDeletableWithIndividualsAssociationTaxonDescription");
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        FieldUnit associatedFieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        TaxonDescription taxonDescription = (TaxonDescription) descriptionService.load(taxonDescriptionUuid);
        IndividualsAssociation individualsAssociation = (IndividualsAssociation) taxonDescription.getElements()
                .iterator().next();

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;
        // check deletion of field unit -> should fail because of
        // IndividualAssociation
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit.getUuid(), config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(individualsAssociation));

        // allow deletion of individuals association
        config.setDeleteFromIndividualsAssociation(true);
        deleteResult = occurrenceService.isDeletable(associatedFieldUnit.getUuid(), config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());

    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testIsDeletableWithTypeDesignation.xml")
    public void testIsDeletableWithTypeDesignation() {
        UUID derivedUnitUuid = UUID.fromString("f7fd1dc1-3c93-42a7-8279-cde5bfe37ea0");
        UUID botanicalNameUuid = UUID.fromString("7396430c-c932-4dd3-a45a-40c2808b132e");
        // DerivedUnit derivedUnit =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        // derivedUnit.setUuid(derivedUnitUuid);
        //
        // occurrenceService.save(derivedUnit);
        //
        // //create name with type specimen
        // BotanicalName name =
        // BotanicalName.PARSED_NAME("Campanula patual sec L.");
        // name.setUuid(botanicalNameUuid);
        // SpecimenTypeDesignation typeDesignation =
        // SpecimenTypeDesignation.NewInstance();
        // typeDesignation.setTypeSpecimen(derivedUnit);
        // //add type designation to name
        // name.addTypeDesignation(typeDesignation, false);
        //
        // nameService.saveOrUpdate(name);
        //
        // commitAndStartNewTransaction(null);
        //
        // setComplete();
        // endTransaction();
        //
        //
        // try {
        // writeDbUnitDataSetFile(new String[] {
        // "SpecimenOrObservationBase",
        // "SpecimenOrObservationBase_DerivationEvent",
        // "DerivationEvent",
        // "Sequence",
        // "Sequence_SingleRead",
        // "SingleRead",
        // "AmplificationResult",
        // "DescriptionElementBase",
        // "DescriptionBase",
        // "TaxonBase",
        // "TypeDesignationBase",
        // "TaxonName",
        // "TaxonName_TypeDesignationBase",
        // "HomotypicalGroup"
        // }, "testIsDeletableWithTypeDesignation");
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        DerivedUnit typeSpecimen = (DerivedUnit) occurrenceService.load(derivedUnitUuid);

        // create name with type specimen
        IBotanicalName name = nameService.load(botanicalNameUuid);
        SpecimenTypeDesignation typeDesignation = (SpecimenTypeDesignation) name.getTypeDesignations().iterator()
                .next();

        // add type designation to name
        name.addTypeDesignation(typeDesignation, false);

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
        DeleteResult deleteResult = null;

        // check deletion of specimen
        deleteResult = occurrenceService.isDeletable(typeSpecimen.getUuid(), config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(typeDesignation));

        // allow deletion of type designation
        config.setDeleteFromTypeDesignation(true);

        deleteResult = occurrenceService.isDeletable(typeSpecimen.getUuid(), config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testIsDeletableWithChildren.xml")
    public void testIsDeletableWithChildren() {
        UUID fieldUnitUuid = UUID.fromString("92ada058-4c14-4131-8ecd-b82dc1dd2882");
        UUID derivedUnitUuid = UUID.fromString("896dffdc-6809-4914-8950-5501fee1c0fd");
        UUID dnaSampleUuid = UUID.fromString("7efd1d66-ac7f-4202-acdf-a72cbb9c3a21");
        // if this test fails be sure to check if there are left-over elements
        // in the DB
        // e.g. clear by adding "<AMPLIFICATIONRESULT/>" to the data set XML

        // //how the XML was generated
        // FieldUnit fieldUnit = FieldUnit.NewInstance();
        // fieldUnit.setUuid(fieldUnitUuid);
        // //sub derivates (DerivedUnit, DnaSample)
        // DerivedUnit derivedUnit =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        // derivedUnit.setUuid(derivedUnitUuid);
        // DnaSample dnaSample = DnaSample.NewInstance();
        // dnaSample.setUuid(dnaSampleUuid);
        //
        // //derivation events
        // DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit,
        // DerivationEventType.ACCESSIONING());
        // DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample,
        // DerivationEventType.DNA_EXTRACTION());
        //
        // occurrenceService.save(fieldUnit);
        // occurrenceService.save(derivedUnit);
        // occurrenceService.save(dnaSample);
        //
        // commitAndStartNewTransaction(null);
        //
        // setComplete();
        // endTransaction();
        //
        // try {
        // writeDbUnitDataSetFile(new String[] {
        // "SpecimenOrObservationBase",
        // "SpecimenOrObservationBase_DerivationEvent",
        // "DerivationEvent",
        // "Sequence",
        // "SingleRead",
        // "SingleReadAlignment",
        // "Amplification",
        // "AmplificationResult",
        // "DescriptionElementBase",
        // "DescriptionBase",
        // "TaxonBase",
        // "TypeDesignationBase",
        // "TaxonName",
        // "TaxonName_TypeDesignationBase",
        // "HomotypicalGroup"
        // }, "testIsDeletableWithChildren");
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }
        FieldUnit fieldUnit = (FieldUnit) occurrenceService.load(fieldUnitUuid);
        // sub derivates (DerivedUnit, DnaSample)
        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnitUuid);
        DnaSample dnaSample = (DnaSample) occurrenceService.load(dnaSampleUuid);

        // derivation events
        DerivationEvent fieldUnitToDerivedUnitEvent = fieldUnit.getDerivationEvents().iterator().next();
        DerivationEvent derivedUnitToDnaSampleEvent = derivedUnit.getDerivationEvents().iterator().next();

        SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();

        DeleteResult deleteResult = null;
        // check deletion of DnaSample
        deleteResult = occurrenceService.isDeletable(dnaSample.getUuid(), config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());

        // check deletion of Specimen
        deleteResult = occurrenceService.isDeletable(derivedUnit.getUuid(), config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(derivedUnitToDnaSampleEvent));

        // check deletion of fieldUnit
        deleteResult = occurrenceService.isDeletable(fieldUnit.getUuid(), config);
        assertFalse(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(fieldUnitToDerivedUnitEvent));

        // check deletion of Specimen
        config.setDeleteChildren(true);
        deleteResult = occurrenceService.isDeletable(derivedUnit.getUuid(), config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());
        assertTrue(deleteResult.toString(), deleteResult.getRelatedObjects().contains(derivedUnitToDnaSampleEvent));

        // check deletion of fieldUnit
        config.setDeleteFromDescription(true);
        deleteResult = occurrenceService.isDeletable(fieldUnit.getUuid(), config);
        assertTrue(deleteResult.toString(), deleteResult.isOk());

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="OccurrenceServiceTest.testListAssociatedTaxaAndListByAssociatedTaxon.xml")
    public void testListAssociatedTaxaAndListByAssociatedTaxon(){
        UUID associatedSpecimenUuid = UUID.fromString("6478a387-bc77-4f1b-bfab-671ad786a27e");
        UUID unassociatedSpecimenUuid = UUID.fromString("820e1af6-9bff-4244-97d3-81fd9a49c91c");
        UUID typeSpecimenUuid = UUID.fromString("b6f31b9f-f9e2-4bc7-883e-35bd6a9978b4");
        UUID taxonUuid = UUID.fromString("5613698d-840b-4034-b9b1-1302938e183b");
//      DerivedUnit typeSpecimen = DerivedUnit.NewInstance(SpecimenOrObservationType.TissueSample);
//      DerivedUnit associatedSpecimen = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//      DerivedUnit unassociatedSpecimen = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
//      typeSpecimen.setUuid(typeSpecimenUuid);
//      associatedSpecimen.setUuid(associatedSpecimenUuid);
//      unassociatedSpecimen.setUuid(unassociatedSpecimenUuid);
//
//      occurrenceService.save(typeSpecimen);
//      occurrenceService.save(associatedSpecimen);
//      occurrenceService.save(unassociatedSpecimen);
//
//      BotanicalName name = BotanicalName.PARSED_NAME("Campanula patula");
//      SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
//      typeDesignation.setTypeSpecimen(typeSpecimen);
//      name.addTypeDesignation(typeDesignation, false);
//
//      Taxon taxon = Taxon.NewInstance(name, null);
//      taxon.setUuid(taxonUuid);
//      TaxonDescription taxonDescription = TaxonDescription.NewInstance();
////      taxonDescription.setUuid(taxonDescriptionUuid);
//
//      taxonDescription.addElement(IndividualsAssociation.NewInstance(associatedSpecimen));
//      taxon.addDescription(taxonDescription);
//
//      taxonService.saveOrUpdate(taxon);
//
//      commitAndStartNewTransaction(null);
//
//      setComplete();
//      endTransaction();
//
//      try {
//          writeDbUnitDataSetFile(new String[]{
//                                 "SpecimenOrObservationBase",
//                  "SpecimenOrObservationBase_DerivationEvent",
//                  "DerivationEvent",
//                  "Sequence",
//                  "Sequence_SingleRead",
//                  "SingleRead",
//                  "AmplificationResult",
//                  "DescriptionElementBase",
//                  "DescriptionBase",
//                  "TaxonBase",
//                  "TypeDesignationBase",
//                  "TaxonName",
//                  "TaxonName_TypeDesignationBase",
//                  "TeamOrPersonBase",
//                  "HomotypicalGroup"}, "testListAssociatedTaxaAndListByAssociatedTaxon");
//      } catch (FileNotFoundException e) {
//          e.printStackTrace();
//      }
        //check initial state
        SpecimenOrObservationBase typeSpecimen = occurrenceService.load(typeSpecimenUuid);
        SpecimenOrObservationBase associatedSpecimen = occurrenceService.load(associatedSpecimenUuid);
        SpecimenOrObservationBase unassociatedSpecimen = occurrenceService.load(unassociatedSpecimenUuid);
        Taxon taxon = (Taxon) taxonService.load(taxonUuid);

        assertNotNull(typeSpecimen);
        assertNotNull(associatedSpecimen);
        assertNotNull(unassociatedSpecimen);
        assertNotNull(taxon);

        //check association (IndividualsAssociations + TypeDesignations) specimen -> taxon (name)

        //unassociated specimen
        java.util.Collection<TaxonBase<?>> associatedTaxa = occurrenceService.listAssociatedTaxa(unassociatedSpecimen, null, null, null, null);
        assertNotNull(associatedTaxa);
        assertTrue(associatedTaxa.isEmpty());

        //type specimen
        associatedTaxa = occurrenceService.listAssociatedTaxa(typeSpecimen, null, null, null, null);
        assertNotNull(associatedTaxa);
        assertEquals(1, associatedTaxa.size());
        assertEquals(taxon, associatedTaxa.iterator().next());

        //associated specimen
        associatedTaxa = occurrenceService.listAssociatedTaxa(associatedSpecimen, null, null, null, null);
        assertNotNull(associatedTaxa);
        assertEquals(1, associatedTaxa.size());
        assertEquals(taxon, associatedTaxa.iterator().next());


        //check association (IndividualsAssociations + TypeDesignations) taxon (name) -> specimen
        List<DerivedUnit> byAssociatedTaxon = occurrenceService.listByAssociatedTaxon(DerivedUnit.class, null, taxon, null, null, null, null, null);
        assertNotNull(byAssociatedTaxon);
        assertEquals(2, byAssociatedTaxon.size());
        assertTrue(byAssociatedTaxon.contains(associatedSpecimen));
        assertTrue(byAssociatedTaxon.contains(typeSpecimen));
        assertTrue(!byAssociatedTaxon.contains(unassociatedSpecimen));

    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testFindOcurrences.xml")
    public void testFindOccurrences() {
        UUID derivedUnit1Uuid = UUID.fromString("843bc8c9-c0fe-4735-bf40-82f1996dcefb");
        UUID derivedUnit2Uuid = UUID.fromString("40cd9cb1-7c74-4e7d-a1f8-8a1e0314e940");
        UUID dnaSampleUuid = UUID.fromString("364969a6-2457-4e2e-ae1e-29a6fcaa741a");
        UUID dnaSampleWithSequenceUuid = UUID.fromString("571d4e9a-0736-4da3-ad4a-a2df427a1f01");
        UUID tissueUuid = UUID.fromString("b608613c-1b5a-4882-8b14-d643b6fc5998");

        UUID taxonUuid = UUID.fromString("dfca7629-8a60-4d51-998d-371897f725e9");

        // DerivedUnit derivedUnit =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        // derivedUnit.setTitleCache("testUnit1");
        // derivedUnit.setAccessionNumber("ACC1");
        // DerivedUnit derivedUnit2 =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        // derivedUnit2.setTitleCache("testUnit2");
        // derivedUnit2.setBarcode("ACC2");
        // DerivedUnit dnaSample =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.DnaSample);
        // dnaSample.setTitleCache("dna");
        // dnaSample.setCatalogNumber("ACC1");
        // DerivedUnit tissue =
        // DerivedUnit.NewInstance(SpecimenOrObservationType.TissueSample);
        // tissue.setTitleCache("tissue");
        //
        // DerivationEvent.NewSimpleInstance(derivedUnit, dnaSample,
        // DerivationEventType.DNA_EXTRACTION());
        //
        // derivedUnit.setUuid(derivedUnit1Uuid);
        // derivedUnit2.setUuid(derivedUnit2Uuid);
        // dnaSample.setUuid(dnaSampleUuid);
        // tissue.setUuid(tissueUuid);
        //
        // occurrenceService.save(derivedUnit);
        // occurrenceService.save(derivedUnit2);
        // occurrenceService.save(dnaSample);
        // occurrenceService.save(tissue);
        //
        // Taxon taxon =
        // Taxon.NewInstance(BotanicalName.PARSED_NAME("Campanula patual"),
        // null);
        // taxon.setUuid(taxonUuid);
        // TaxonDescription taxonDescription = TaxonDescription.NewInstance();
        // taxonDescription.setUuid(UUID.fromString("272d4d28-662c-468e-94d8-16993fab91ba"));
        // //add voucher
        // taxonDescription.addElement(IndividualsAssociation.NewInstance(derivedUnit));
        // taxonDescription.addElement(IndividualsAssociation.NewInstance(tissue));
        // taxon.addDescription(taxonDescription);
        // taxonService.saveOrUpdate(taxon);
        //
        // commitAndStartNewTransaction(null);
        //
        // setComplete();
        // endTransaction();
        //
        //
        // try {
        // writeDbUnitDataSetFile(new String[] {
        // "SpecimenOrObservationBase",
        // "SpecimenOrObservationBase_DerivationEvent",
        // "DerivationEvent",
        // "DescriptionElementBase",
        // "DescriptionBase",
        // "TaxonBase",
        // "TypeDesignationBase",
        // "TaxonName",
        // "TaxonName_TypeDesignationBase",
        // "HomotypicalGroup",
        // "TeamOrPersonBase"
        // }, "testFindOcurrences");
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        SpecimenOrObservationBase derivedUnit1 = occurrenceService.load(derivedUnit1Uuid);
        SpecimenOrObservationBase derivedUnit2 = occurrenceService.load(derivedUnit2Uuid);
        SpecimenOrObservationBase tissue = occurrenceService.load(tissueUuid);
        SpecimenOrObservationBase dnaSample = occurrenceService.load(dnaSampleUuid);
        SpecimenOrObservationBase dnaSampleWithSequence = occurrenceService.load(dnaSampleWithSequenceUuid);
        Taxon taxon = (Taxon) taxonService.load(taxonUuid);

        assertNotNull(derivedUnit1);
        assertNotNull(derivedUnit2);
        assertNotNull(tissue);
        assertNotNull(dnaSample);
        assertNotNull(taxon);

        // wildcard search => all derivates
        FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
        config.setTitleSearchString("*");
        assertEquals(5, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> allDerivates = occurrenceService.findByTitle(config).getRecords();
        assertEquals(5, allDerivates.size());
        assertTrue(allDerivates.contains(derivedUnit1));
        assertTrue(allDerivates.contains(derivedUnit2));
        assertTrue(allDerivates.contains(tissue));
        assertTrue(allDerivates.contains(dnaSample));

        // queryString search => 2 derivates
        config = new FindOccurrencesConfigurator();
        config.setTitleSearchString("test*");
        // config.setClazz(SpecimenOrObservationBase.class);
        assertEquals(2, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> queryStringDerivates = occurrenceService.findByTitle(config).getRecords();
        assertEquals(2, queryStringDerivates.size());
        assertTrue(queryStringDerivates.contains(derivedUnit1));
        assertTrue(queryStringDerivates.contains(derivedUnit2));

        // class search => 4 results
        config = new FindOccurrencesConfigurator();
        config.setClazz(SpecimenOrObservationBase.class);
        assertEquals(5, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> specimenOrObservationBases = occurrenceService.findByTitle(config).getRecords();
        assertEquals(5, specimenOrObservationBases.size());

        // class search => 0 results
        config = new FindOccurrencesConfigurator();
        config.setClazz(FieldUnit.class);
        assertEquals(0, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> fieldUnits = occurrenceService.findByTitle(config).getRecords();
        assertEquals(0, fieldUnits.size());

        // class search => 4 results
        config = new FindOccurrencesConfigurator();
        config.setClazz(DerivedUnit.class);
        assertEquals(5, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> derivedUnits = occurrenceService.findByTitle(config).getRecords();
        assertEquals(5, derivedUnits.size());
        assertTrue(derivedUnits.contains(derivedUnit1));
        assertTrue(derivedUnits.contains(derivedUnit2));
        assertTrue(derivedUnits.contains(tissue));
        assertTrue(derivedUnits.contains(dnaSample));

        // significant identifier search
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

        // recordBasis search => 1 Fossil
        config = new FindOccurrencesConfigurator();
        config.setSpecimenType(SpecimenOrObservationType.Fossil);
        assertEquals(1, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> fossils = occurrenceService.findByTitle(config).getRecords();
        assertEquals(1, fossils.size());
        assertTrue(fossils.contains(derivedUnit1));

        // taxon determination search => 2 associated specimens
        config = new FindOccurrencesConfigurator();
        config.setClazz(DerivedUnit.class);
        config.setAssociatedTaxonUuid(taxon.getUuid());
        assertEquals(2, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> associatedSpecimens = occurrenceService.findByTitle(config).getRecords();
        assertEquals(2, associatedSpecimens.size());
        assertTrue(associatedSpecimens.contains(derivedUnit1));
        assertTrue(associatedSpecimens.contains(tissue));

        // taxon determination search (indirectly associated) => 3 associated
        // specimens
        config = new FindOccurrencesConfigurator();
        config.setClazz(DerivedUnit.class);
        config.setAssociatedTaxonUuid(taxon.getUuid());
        config.setRetrieveIndirectlyAssociatedSpecimens(true);
        assertEquals(3, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> indirectlyAssociatedSpecimens = occurrenceService.findByTitle(config)
                .getRecords();
        assertEquals(3, indirectlyAssociatedSpecimens.size());
        assertTrue(indirectlyAssociatedSpecimens.contains(derivedUnit1));
        assertTrue(indirectlyAssociatedSpecimens.contains(dnaSample));
        assertTrue(indirectlyAssociatedSpecimens.contains(tissue));

        // taxon association search
        config = new FindOccurrencesConfigurator();
        config.setClazz(SpecimenOrObservationBase.class);
        config.setAssociatedTaxonUuid(taxon.getUuid());
        assertEquals(2, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> specimensOrObservations = occurrenceService.findByTitle(config).getRecords();
        assertEquals(2, specimensOrObservations.size());
        assertTrue(specimensOrObservations.contains(tissue));
        assertTrue(specimensOrObservations.contains(derivedUnit1));

        //test assignment status
        //all specimen
        config = new FindOccurrencesConfigurator();
        config.setAssignmentStatus(AssignmentStatus.ALL_SPECIMENS);
        assertEquals(5, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> allSpecimens = occurrenceService.findByTitle(config).getRecords();
        assertEquals(5, allSpecimens.size());
        assertTrue(allSpecimens.contains(derivedUnit1));
        assertTrue(allSpecimens.contains(derivedUnit2));
        assertTrue(allSpecimens.contains(tissue));
        assertTrue(allSpecimens.contains(dnaSample));

        //assigned specimen
        config = new FindOccurrencesConfigurator();
        config.setAssignmentStatus(AssignmentStatus.ASSIGNED_SPECIMENS);
        assertEquals(2, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> assignedSpecimens = occurrenceService.findByTitle(config).getRecords();
        assertEquals(2, assignedSpecimens.size());
        assertTrue(assignedSpecimens.contains(derivedUnit1));
        assertTrue(assignedSpecimens.contains(tissue));

        //unassigned specimen
        config = new FindOccurrencesConfigurator();
        config.setAssignmentStatus(AssignmentStatus.UNASSIGNED_SPECIMENS);
        assertEquals(3, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> unAssignedSpecimens = occurrenceService.findByTitle(config).getRecords();
        assertEquals(3, unAssignedSpecimens.size());
        assertTrue(unAssignedSpecimens.contains(derivedUnit2));
        assertTrue(unAssignedSpecimens.contains(dnaSample));

        //ignore assignment status because taxon uuid is set
        config = new FindOccurrencesConfigurator();
        config.setAssociatedTaxonUuid(taxon.getUuid());
        config.setAssignmentStatus(AssignmentStatus.UNASSIGNED_SPECIMENS);
        assertEquals(2, occurrenceService.countOccurrences(config));
        List<SpecimenOrObservationBase> ignoreAssignmentStatusSpecimens = occurrenceService.findByTitle(config).getRecords();
        assertEquals(2, ignoreAssignmentStatusSpecimens.size());
        assertTrue(ignoreAssignmentStatusSpecimens.contains(derivedUnit1));
        assertTrue(ignoreAssignmentStatusSpecimens.contains(tissue));



        List<DerivedUnit> findByAccessionNumber = occurrenceService.findByAccessionNumber("ACC_DNA", 10, 1, null, null);

        assertEquals(1, findByAccessionNumber.size());
       // assertTrue(findByAccessionNumber.contains(derivedUnit1));
        assertTrue(findByAccessionNumber.contains(dnaSampleWithSequence));


    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest-testAllKindsOfSpecimenAssociations.xml")
    public void testListUuidAndTitleCacheByAssociatedTaxon() {
        UUID taxonNodeUuid = UUID.fromString("6b8b6ff9-66e4-4496-8e5a-7d03bdf9a076");
        /**
         * Structure is as follows:
         *
         * Taxon ----IndividualsAssociation---> DnaSample
         * Taxon ----TypeDesignation---> Fossil
         * Taxon ----Determination ---> PreservedSpecimenA
         *
         * Taxon ---> Taxon Name ----Determination ---> PreservedSpecimenB
         *
         * Taxon ---> Synonym ---> SynonymName ----Determination---> PreservedSpecimenC
         *
         * Orphan Name (not associated with any taxon) ----Determination ---> PreservedSpecimenD
         */

        //UUIDS
        UUID derivedUnitDeterminationTaxonUuid = UUID.fromString("941b8b22-1925-4b91-8ff8-97114499bb22");
        UUID derivedUnitDeterminationNameUuid = UUID.fromString("0cdc7a57-6f55-45c8-b3e5-523748c381e7");
        UUID derivedUnitDeterminationSynonymNameUuid = UUID.fromString("d940a940-8caf-4a52-b1d8-ba4aad7ddae2");
        UUID derivedUnitDeterminationOrphanNameUuid = UUID.fromString("587b7297-7d59-4f59-8ef3-c7a559cadeca");
        UUID tissueUuidNoAssociationUuid = UUID.fromString("93e94260-5107-4b2c-9ce4-da9e1a4e7cb9");
        UUID dnaSampleUuidIndividualsAssociationUuid = UUID.fromString("1fb53903-c9b9-4078-8297-5b86aec7fe21");
        UUID fossilTypeDesignationUuid = UUID.fromString("42ec8dcf-a923-4256-bbd5-b0d10f4de5e2");
        UUID taxonUuid = UUID.fromString("07cc47a5-1a63-46a1-8366-0d59d2b90d5b");

        /*
         * search for taxon node
         * should retrieve all specimens associated via
         *  - type designations (fossil)
         *  - individuals associations (dnaSample)
         *  - determinations on
         *   - taxon (specimenA)
         *   - taxon name (specimenA, specimenB)
         */
        FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
        config.setAssociatedTaxonUuid(taxonUuid);
        Collection<SpecimenNodeWrapper> specimens = occurrenceService
                .listUuidAndTitleCacheByAssociatedTaxon(Collections.singletonList(taxonNodeUuid), null, null);
        List<UUID> uuidList = specimens.stream().map(specimen ->
        specimen.getUuidAndTitleCache().getUuid()).collect(Collectors.toList());
        assertTrue(uuidList.contains(derivedUnitDeterminationNameUuid));
        assertTrue(uuidList.contains(derivedUnitDeterminationTaxonUuid));
        assertFalse(uuidList.contains(derivedUnitDeterminationSynonymNameUuid));
        assertTrue(uuidList.contains(dnaSampleUuidIndividualsAssociationUuid));
        assertTrue(uuidList.contains(fossilTypeDesignationUuid));
        assertFalse(uuidList.contains(tissueUuidNoAssociationUuid));
        assertFalse(uuidList.contains(derivedUnitDeterminationOrphanNameUuid));
        assertEquals("Wrong number of associated specimens", 4, specimens.size());
    }

    /**
     * This will test the retrieval of specimens that are in any way associated
     * with a taxon resp. taxon name via type designation, determination event
     * or individuals associations. It will also consider synonym relationships.
     */
    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest-testAllKindsOfSpecimenAssociations.xml")
    public void testAllKindsOfSpecimenAssociations() {

        /**
         * Structure is as follows:
         *
         * Taxon ----IndividualsAssociation---> DnaSample
         * Taxon ----TypeDesignation---> Fossil
         * Taxon ----Determination ---> PreservedSpecimenA
         *
         * Taxon ---> Taxon Name ----Determination ---> PreservedSpecimenB
         *
         * Taxon ---> Synonym ---> SynonymName ----Determination---> PreservedSpecimenC
         *
         * Orphan Name (not associated with any taxon) ----Determination ---> PreservedSpecimenD
         */

        //UUIDS
        UUID derivedUnitDeterminationTaxonUuid = UUID.fromString("941b8b22-1925-4b91-8ff8-97114499bb22");
        UUID derivedUnitDeterminationNameUuid = UUID.fromString("0cdc7a57-6f55-45c8-b3e5-523748c381e7");

        UUID derivedUnitDeterminationSynonymUuid = UUID.fromString("8eb94a7d-c802-49a7-bc10-c26de20a52c2");
        UUID derivedUnitDeterminationSynonymNameUuid = UUID.fromString("d940a940-8caf-4a52-b1d8-ba4aad7ddae2");

        UUID derivedUnitDeterminationOrphanNameUuid = UUID.fromString("587b7297-7d59-4f59-8ef3-c7a559cadeca");

        UUID tissueUuidNoAssociationUuid = UUID.fromString("93e94260-5107-4b2c-9ce4-da9e1a4e7cb9");
        UUID dnaSampleUuidIndividualsAssociationUuid = UUID.fromString("1fb53903-c9b9-4078-8297-5b86aec7fe21");
        UUID fossilTypeDesignationUuid = UUID.fromString("42ec8dcf-a923-4256-bbd5-b0d10f4de5e2");

        UUID taxonUuid = UUID.fromString("07cc47a5-1a63-46a1-8366-0d59d2b90d5b");
        UUID synoymUuid = UUID.fromString("c16bcd9b-7d18-4fb5-af60-f9ef14c1d3a9");

        UUID taxonNameUuid = UUID.fromString("e59b95c0-9ad6-48be-af62-a982ba72b917");
        UUID synonymNameUuid = UUID.fromString("39f04b2a-b8bd-46e8-9102-ab665c64ec8e");
        UUID orphanNameUuid = UUID.fromString("d8e56365-3ad9-4b0e-88bf-acaaab223a9b");

        //load cdm entities
        DerivedUnit derivedUnitDeterminationTaxon = (DerivedUnit) occurrenceService.load(derivedUnitDeterminationTaxonUuid);
        DerivedUnit derivedUnitDeterminationName = (DerivedUnit) occurrenceService.load(derivedUnitDeterminationNameUuid);
        DerivedUnit derivedUnitDeterminationSynonym = (DerivedUnit) occurrenceService.load(derivedUnitDeterminationSynonymUuid);
        DerivedUnit derivedUnitDeterminationSynonymName = (DerivedUnit) occurrenceService.load(derivedUnitDeterminationSynonymNameUuid);
        DerivedUnit derivedUnitDeterminationOrphanName = (DerivedUnit) occurrenceService.load(derivedUnitDeterminationOrphanNameUuid);
        DerivedUnit tissueUuidNoAssociation = (DerivedUnit) occurrenceService.load(tissueUuidNoAssociationUuid);
        DnaSample dnaSampleUuidIndividualsAssociation = (DnaSample) occurrenceService.load(dnaSampleUuidIndividualsAssociationUuid);
        DerivedUnit fossilTypeDesignation = (DerivedUnit) occurrenceService.load(fossilTypeDesignationUuid);
        Taxon taxon = HibernateProxyHelper.deproxy(taxonService.load(taxonUuid), Taxon.class);
        Synonym synonym = (Synonym) taxonService.load(synoymUuid);
        TaxonName taxonName = nameService.load(taxonNameUuid);
        TaxonName synonymName = nameService.load(synonymNameUuid);
        TaxonName orphanName = nameService.load(orphanNameUuid);

        //check initial state
        assertNotNull(derivedUnitDeterminationTaxon);
        assertNotNull(derivedUnitDeterminationName);
        assertNotNull(derivedUnitDeterminationSynonym);
        assertNotNull(derivedUnitDeterminationSynonymName);
        assertNotNull(derivedUnitDeterminationOrphanName);
        assertNotNull(tissueUuidNoAssociation);
        assertNotNull(dnaSampleUuidIndividualsAssociation);
        assertNotNull(fossilTypeDesignation);
        assertNotNull(taxon);
        assertNotNull(synonym);
        assertNotNull(taxonName);
        assertNotNull(synonymName);
        assertNotNull(orphanName);

        /*
         * search for taxon
         * should retrieve all specimens associated via
         *  - type designations (fossil)
         *  - individuals associations (dnaSample)
         *  - determinations on
         *   - taxon (specimenA)
         *   - taxon name (specimenA, specimenB)
         *   - synonym names (specimenC)
         */
        FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
        config.setAssociatedTaxonUuid(taxonUuid);
        List<SpecimenOrObservationBase> specimens = occurrenceService.findByTitle(config).getRecords();
        assertTrue(specimens.contains(derivedUnitDeterminationName));
        assertTrue(specimens.contains(derivedUnitDeterminationTaxon));
        assertTrue(specimens.contains(derivedUnitDeterminationSynonymName));
        assertTrue(specimens.contains(dnaSampleUuidIndividualsAssociation));
        assertTrue(specimens.contains(fossilTypeDesignation));
        assertTrue(!specimens.contains(tissueUuidNoAssociation));
        assertTrue(!specimens.contains(derivedUnitDeterminationOrphanName));
        assertEquals("Wrong number of associated specimens", 5, specimens.size());

        /*
         * search for taxon name
         * should retrieve all specimens associated via
         *  - determinations on
         *   - taxon name (specimenA, specimenB)
         */
        config = new FindOccurrencesConfigurator();
        config.setAssociatedTaxonNameUuid(taxonNameUuid);
        specimens = occurrenceService.findByTitle(config).getRecords();
        assertTrue(specimens.contains(derivedUnitDeterminationName));
        assertTrue(specimens.contains(derivedUnitDeterminationTaxon));
        assertTrue(!specimens.contains(derivedUnitDeterminationSynonymName));
        assertTrue(!specimens.contains(dnaSampleUuidIndividualsAssociation));
        assertTrue(!specimens.contains(fossilTypeDesignation));
        assertTrue(!specimens.contains(tissueUuidNoAssociation));
        assertTrue(!specimens.contains(derivedUnitDeterminationOrphanName));
        assertEquals("Wrong number of associated specimens", 2, specimens.size());

        /*
         * search for synonym name
         * should retrieve all specimens associated via
         *  - determinations on
         *   - synonym names (specimenC)
         */
        config = new FindOccurrencesConfigurator();
        config.setAssociatedTaxonNameUuid(synonymNameUuid);
        specimens = occurrenceService.findByTitle(config).getRecords();
        assertTrue(!specimens.contains(derivedUnitDeterminationName));
        assertTrue(!specimens.contains(derivedUnitDeterminationTaxon));
        assertTrue(specimens.contains(derivedUnitDeterminationSynonymName));
        assertTrue(!specimens.contains(dnaSampleUuidIndividualsAssociation));
        assertTrue(!specimens.contains(fossilTypeDesignation));
        assertTrue(!specimens.contains(tissueUuidNoAssociation));
        assertTrue(!specimens.contains(derivedUnitDeterminationOrphanName));
        assertEquals("Wrong number of associated specimens", 1, specimens.size());

        /*
         * search for orphan name
         * should retrieve all specimens associated via
         *  - determinations on
         *   - taxon name (specimenD)
         */
        config = new FindOccurrencesConfigurator();
        config.setAssociatedTaxonNameUuid(orphanNameUuid);
        specimens = occurrenceService.findByTitle(config).getRecords();
        assertTrue(!specimens.contains(derivedUnitDeterminationName));
        assertTrue(!specimens.contains(derivedUnitDeterminationTaxon));
        assertTrue(!specimens.contains(derivedUnitDeterminationSynonymName));
        assertTrue(!specimens.contains(dnaSampleUuidIndividualsAssociation));
        assertTrue(!specimens.contains(fossilTypeDesignation));
        assertTrue(!specimens.contains(tissueUuidNoAssociation));
        assertTrue(specimens.contains(derivedUnitDeterminationOrphanName));
        assertEquals("Wrong number of associated specimens", 1, specimens.size());

//        //DERIVATIVES
//        //determination: taxon
//        DerivedUnit derivedUnitDeterminationTaxon = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
//        derivedUnitDeterminationTaxon.setTitleCache("Derived Unit determined as taxon");
//        //determination: taxon name
//        DerivedUnit derivedUnitDeterminationName = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
//        derivedUnitDeterminationName.setTitleCache("Derived Unit determined as name");
//        //determination: synonym
//        DerivedUnit derivedUnitDeterminationSynonym = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
//        derivedUnitDeterminationSynonym.setTitleCache("Derived Unit determined as synonym");
//        //determination: synonym name
//        DerivedUnit derivedUnitDeterminationSynonymName = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
//        derivedUnitDeterminationSynonymName.setTitleCache("Derived Unit determined as synonym name");
//        //determination: orphan name
//        DerivedUnit derivedUnitDeterminationOrphanName = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
//        derivedUnitDeterminationOrphanName.setTitleCache("Derived Unit determined as orphan name");
//        //no association
//        DerivedUnit tissueUuidNoAssociation = DerivedUnit.NewInstance(SpecimenOrObservationType.TissueSample);
//        tissueUuidNoAssociation.setTitleCache("tissue sample no association");
//        //individuals association with taxon
//        DerivedUnit dnaSampleUuidIndividualsAssociation = DerivedUnit.NewInstance(SpecimenOrObservationType.DnaSample);
//        dnaSampleUuidIndividualsAssociation.setTitleCache("dna associated via IndividualsAssociation");
//        //type specimen of taxon
//        DerivedUnit fossilTypeDesignation = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        fossilTypeDesignation.setTitleCache("Fossil with type designation");
//
//        derivedUnitDeterminationTaxon.setUuid(derivedUnitDeterminationTaxonUuid);
//        derivedUnitDeterminationName.setUuid(derivedUnitDeterminationNameUuid);
//        derivedUnitDeterminationSynonym.setUuid(derivedUnitDeterminationSynonymUuid);
//        derivedUnitDeterminationSynonymName.setUuid(derivedUnitDeterminationSynonymNameUuid);
//        derivedUnitDeterminationOrphanName.setUuid(derivedUnitDeterminationOrphanNameUuid);
//        tissueUuidNoAssociation.setUuid(tissueUuidNoAssociationUuid);
//        dnaSampleUuidIndividualsAssociation.setUuid(dnaSampleUuidIndividualsAssociationUuid);
//        fossilTypeDesignation.setUuid(fossilTypeDesignationUuid);
//
//        occurrenceService.save(derivedUnitDeterminationTaxon);
//        occurrenceService.save(derivedUnitDeterminationName);
//        occurrenceService.save(derivedUnitDeterminationSynonym);
//        occurrenceService.save(derivedUnitDeterminationSynonymName);
//        occurrenceService.save(derivedUnitDeterminationOrphanName);
//        occurrenceService.save(tissueUuidNoAssociation);
//        occurrenceService.save(dnaSampleUuidIndividualsAssociation);
//        occurrenceService.save(fossilTypeDesignation);
//
//        //NAMES
//        TaxonName taxonName = TaxonNameFactory.PARSED_BOTANICAL("Campanula patual");
//        TaxonName synonymName = TaxonNameFactory.PARSED_BOTANICAL("Syno nyma");
//        TaxonName orphanName = TaxonNameFactory.PARSED_BOTANICAL("Orphanus lonelia");
//        taxonName.setUuid(taxonNameUuid);
//        synonymName.setUuid(synonymNameUuid);
//        orphanName.setUuid(orphanNameUuid);
//
//        //TAXON
//        Taxon taxon = Taxon.NewInstance(taxonName, null);
//        taxon.setUuid(taxonUuid);
//
//        Classification classification = Classification.NewInstance("Test Classification");
//        TaxonNode taxonNode = classification.addChildTaxon(taxon, null, null);
//        taxonNode.setUuid(taxonNodeUuid);
//
//        //SYNONYM
//        Synonym synonym = Synonym.NewInstance(synonymName, null);
//        synonym.setUuid(synoymUuid);
//        taxon.addSynonym(synonym, SynonymType.HOMOTYPIC_SYNONYM_OF());
//
//        //IndividualsAssociation
//        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
//        IndividualsAssociation association = IndividualsAssociation.NewInstance(dnaSampleUuidIndividualsAssociation);
//        association.setFeature(Feature.SPECIMEN());
//        taxonDescription.addElement(association);
//        taxon.addDescription(taxonDescription);
//
//        //DETERMINATION EVENTS
//        DeterminationEvent.NewInstance(taxon, derivedUnitDeterminationTaxon);
//        DeterminationEvent.NewInstance(taxonName, derivedUnitDeterminationName);
//        //        DeterminationEvent.NewInstance(synonym, derivedUnitDeterminationSynonym);//TODO determinationa on synonym not possible?
//        DeterminationEvent.NewInstance(synonymName, derivedUnitDeterminationSynonymName);
//        DeterminationEvent.NewInstance(orphanName, derivedUnitDeterminationOrphanName);
//
//        //type designation
//        SpecimenTypeDesignation specimenTypeDesignation = SpecimenTypeDesignation.NewInstance();
//        specimenTypeDesignation.setTypeSpecimen(fossilTypeDesignation);
//        taxonName.addTypeDesignation(specimenTypeDesignation, false);
//
//        classificationService.save(classification);
//        taxonService.saveOrUpdate(taxon);
//        taxonNodeService.save(taxonNode);
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//        "SpecimenOrObservationBase",
//        "SpecimenOrObservationBase_DerivationEvent",
//        "DerivationEvent",
//        "DescriptionElementBase",
//        "DescriptionBase",
//        "TaxonBase",
//        "TaxonNode",
//        "Classification",
//        "TypeDesignationBase",
//        "TaxonName",
//        "TaxonName_TypeDesignationBase",
//        "HomotypicalGroup",
//        "TeamOrPersonBase",
//        "LanguageString",
//        "DeterminationEvent"
//            }, "testAllKindsOfSpecimenAssociations");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


    }


    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "OccurrenceServiceTest.testDnaSampleDesignation.xml")
    public void testDnaSampleDesignation(){
        DefinedTerm sampleDesignationTermType = (DefinedTerm) termService.load(UUID.fromString("fadeba12-1be3-4bc7-9ff5-361b088d86fc"));

        UUID dnaSampleUuid = UUID.fromString("4bee91b9-23d8-438b-8569-6d6aaa5b6587");
        DnaSample dnaSample = HibernateProxyHelper.deproxy(occurrenceService.load(dnaSampleUuid), DnaSample.class);
        assertEquals(1, dnaSample.getIdentifiers().size());
        Identifier<?> identifier = dnaSample.getIdentifiers().iterator().next();
        assertEquals("NK 2088", identifier.getIdentifier());
        assertEquals(sampleDesignationTermType, identifier.getType());

        //change identifier, save and reload
        identifier.setIdentifier("WB10");
        occurrenceService.saveOrUpdate(dnaSample);
        SpecimenOrObservationBase<?> dnaSampleReloaded = occurrenceService.load(dnaSampleUuid);
        assertEquals(1, dnaSample.getIdentifiers().size());
        Identifier<?> identifierReloaded = dnaSample.getIdentifiers().iterator().next();
        assertEquals("WB10", identifierReloaded.getIdentifier());
        assertEquals(sampleDesignationTermType, identifierReloaded.getType());


//        DefinedTerm sampleDesignationTermType = (DefinedTerm) termService.load(UUID.fromString("fadeba12-1be3-4bc7-9ff5-361b088d86fc"));
//        //UUIDS
//        UUID dnaSampleUuid = UUID.fromString("4bee91b9-23d8-438b-8569-6d6aaa5b6587");
//        DerivedUnit dnaSample = DnaSample.NewInstance();
//        dnaSample.setUuid(dnaSampleUuid);
//        Identifier.NewInstance(dnaSample, "NK 2088", sampleDesignationTermType);
//
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
//                    "IDENTIFIER",
//                    "SpecimenOrObservationBase_Identifier"
//            }, "testDnaSampleDesignation");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }
    @Override
//  @Test
  public void createTestDataSet() throws FileNotFoundException {
      //UUIDS
      UUID derivedUnitDeterminationTaxonUuid = UUID.fromString("941b8b22-1925-4b91-8ff8-97114499bb22");
      UUID derivedUnitDeterminationNameUuid = UUID.fromString("0cdc7a57-6f55-45c8-b3e5-523748c381e7");

      UUID derivedUnitDeterminationSynonymUuid = UUID.fromString("8eb94a7d-c802-49a7-bc10-c26de20a52c2");
      UUID derivedUnitDeterminationSynonymNameUuid = UUID.fromString("d940a940-8caf-4a52-b1d8-ba4aad7ddae2");

      UUID derivedUnitDeterminationOrphanNameUuid = UUID.fromString("587b7297-7d59-4f59-8ef3-c7a559cadeca");

      UUID tissueUuidNoAssociationUuid = UUID.fromString("93e94260-5107-4b2c-9ce4-da9e1a4e7cb9");
      UUID dnaSampleUuidIndividualsAssociationUuid = UUID.fromString("1fb53903-c9b9-4078-8297-5b86aec7fe21");
      UUID fossilTypeDesignationUuid = UUID.fromString("42ec8dcf-a923-4256-bbd5-b0d10f4de5e2");

      UUID taxonNodeUuid = UUID.fromString("6b8b6ff9-66e4-4496-8e5a-7d03bdf9a076");
      UUID taxonUuid = UUID.fromString("07cc47a5-1a63-46a1-8366-0d59d2b90d5b");
      UUID synoymUuid = UUID.fromString("c16bcd9b-7d18-4fb5-af60-f9ef14c1d3a9");

      UUID taxonNameUuid = UUID.fromString("e59b95c0-9ad6-48be-af62-a982ba72b917");
      UUID synonymNameUuid = UUID.fromString("39f04b2a-b8bd-46e8-9102-ab665c64ec8e");
      UUID orphanNameUuid = UUID.fromString("d8e56365-3ad9-4b0e-88bf-acaaab223a9b");

      //DERIVATIVES
      //determination: taxon
      DerivedUnit derivedUnitDeterminationTaxon = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
      derivedUnitDeterminationTaxon.setTitleCache("Derived Unit determined as taxon");
      //determination: taxon name
      DerivedUnit derivedUnitDeterminationName = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
      derivedUnitDeterminationName.setTitleCache("Derived Unit determined as name");
      //determination: synonym
      DerivedUnit derivedUnitDeterminationSynonym = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
      derivedUnitDeterminationSynonym.setTitleCache("Derived Unit determined as synonym");
      //determination: synonym name
      DerivedUnit derivedUnitDeterminationSynonymName = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
      derivedUnitDeterminationSynonymName.setTitleCache("Derived Unit determined as synonym name");
      //determination: orphan name
      DerivedUnit derivedUnitDeterminationOrphanName = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
      derivedUnitDeterminationOrphanName.setTitleCache("Derived Unit determined as orphan name");
      //no association
      DerivedUnit tissueUuidNoAssociation = DerivedUnit.NewInstance(SpecimenOrObservationType.TissueSample);
      tissueUuidNoAssociation.setTitleCache("tissue sample no association");
      //individuals association with taxon
      DerivedUnit dnaSampleUuidIndividualsAssociation = DerivedUnit.NewInstance(SpecimenOrObservationType.DnaSample);
      dnaSampleUuidIndividualsAssociation.setTitleCache("dna associated via IndividualsAssociation");
      //type specimen of taxon
      DerivedUnit fossilTypeDesignation = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
      fossilTypeDesignation.setTitleCache("Fossil with type designation");

      derivedUnitDeterminationTaxon.setUuid(derivedUnitDeterminationTaxonUuid);
      derivedUnitDeterminationName.setUuid(derivedUnitDeterminationNameUuid);
      derivedUnitDeterminationSynonym.setUuid(derivedUnitDeterminationSynonymUuid);
      derivedUnitDeterminationSynonymName.setUuid(derivedUnitDeterminationSynonymNameUuid);
      derivedUnitDeterminationOrphanName.setUuid(derivedUnitDeterminationOrphanNameUuid);
      tissueUuidNoAssociation.setUuid(tissueUuidNoAssociationUuid);
      dnaSampleUuidIndividualsAssociation.setUuid(dnaSampleUuidIndividualsAssociationUuid);
      fossilTypeDesignation.setUuid(fossilTypeDesignationUuid);

      occurrenceService.save(derivedUnitDeterminationTaxon);
      occurrenceService.save(derivedUnitDeterminationName);
      occurrenceService.save(derivedUnitDeterminationSynonym);
      occurrenceService.save(derivedUnitDeterminationSynonymName);
      occurrenceService.save(derivedUnitDeterminationOrphanName);
      occurrenceService.save(tissueUuidNoAssociation);
      occurrenceService.save(dnaSampleUuidIndividualsAssociation);
      occurrenceService.save(fossilTypeDesignation);

      //NAMES
      TaxonName taxonName = TaxonNameFactory.PARSED_BOTANICAL("Campanula patual");
      TaxonName synonymName = TaxonNameFactory.PARSED_BOTANICAL("Syno nyma");
      TaxonName orphanName = TaxonNameFactory.PARSED_BOTANICAL("Orphanus lonelia");
      taxonName.setUuid(taxonNameUuid);
      synonymName.setUuid(synonymNameUuid);
      orphanName.setUuid(orphanNameUuid);

      //TAXON
      Taxon taxon = Taxon.NewInstance(taxonName, null);
      taxon.setUuid(taxonUuid);

      Classification classification = Classification.NewInstance("Test Classification");
      TaxonNode taxonNode = classification.addChildTaxon(taxon, null, null);
      taxonNode.setUuid(taxonNodeUuid);

      //SYNONYM
      Synonym synonym = Synonym.NewInstance(synonymName, null);
      synonym.setUuid(synoymUuid);
      taxon.addSynonym(synonym, SynonymType.HOMOTYPIC_SYNONYM_OF());

      //IndividualsAssociation
      TaxonDescription taxonDescription = TaxonDescription.NewInstance();
      IndividualsAssociation association = IndividualsAssociation.NewInstance(dnaSampleUuidIndividualsAssociation);
      association.setFeature(Feature.SPECIMEN());
      taxonDescription.addElement(association);
      taxon.addDescription(taxonDescription);

      //DETERMINATION EVENTS
      DeterminationEvent.NewInstance(taxon, derivedUnitDeterminationTaxon);
      DeterminationEvent.NewInstance(taxonName, derivedUnitDeterminationName);
      //        DeterminationEvent.NewInstance(synonym, derivedUnitDeterminationSynonym);//TODO determinationa on synonym not possible?
      DeterminationEvent.NewInstance(synonymName, derivedUnitDeterminationSynonymName);
      DeterminationEvent.NewInstance(orphanName, derivedUnitDeterminationOrphanName);

      //type designation
      SpecimenTypeDesignation specimenTypeDesignation = SpecimenTypeDesignation.NewInstance();
      specimenTypeDesignation.setTypeSpecimen(fossilTypeDesignation);
      taxonName.addTypeDesignation(specimenTypeDesignation, false);

      classificationService.saveOrUpdate(classification);
      taxonService.saveOrUpdate(taxon);
      taxonNodeService.saveOrUpdate(taxonNode);

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
                  "TaxonNode",
                  "Classification",
                  "TypeDesignationBase",
                  "TaxonName",
                  "TaxonName_TypeDesignationBase",
                  "HomotypicalGroup",
                  "TeamOrPersonBase",
                  "LanguageString",
                  "DeterminationEvent"
          }, "testAllKindsOfSpecimenAssociations");
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      }

  }
}
