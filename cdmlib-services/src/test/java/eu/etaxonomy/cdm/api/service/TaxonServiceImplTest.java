/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.IdentifiableServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.config.IncludedTaxonConfiguration;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.config.NameDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.SynonymDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO;
import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.metadata.SecReferenceHandlingEnum;
import eu.etaxonomy.cdm.model.metadata.SecReferenceHandlingSwapEnum;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
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
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 */
public class TaxonServiceImplTest extends CdmTransactionalIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByType
    private ITaxonService service;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    private IAgentService agentService;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private ITaxonNodeDao taxonNodeDao;

    @SpringBeanByType
    private ITaxonNodeService nodeService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

    @SpringBeanByType
    private IDescriptionDao descriptionDao;

    @SpringBeanByType
    private ITypeDesignationDao typeDesignationDao;

    @SpringBeanByType
    private IDescriptionElementDao descriptionElementDao;

    @SpringBeanByType
    private IMarkerService markerService;

    @SpringBeanByType
    private IOccurrenceService occurenceService;

    private Synonym synonym;
    private Synonym synonym2;

    private Taxon taxWithSyn;
    private Taxon tax2WithSyn;
    private Taxon taxWithoutSyn;
    private UUID uuidSyn;
    private UUID uuidTaxWithoutSyn;
    private UUID uuidSyn2;
    private UUID uuidTaxWithSyn;

    private static String[] genera = {"Carex", "Abies", "Belladonna", "Dracula", "Maria", "Calendula", "Polygala", "Vincia"};
    private static String[] epitheta = {"vulgaris", "magdalena", "officinalis", "alba", "negra", "communa", "alpina", "rotundifolia", "greutheriana", "helventica", "allemania", "franca"};
    private static String[] ranks = {"subsp", "var", "f"};

    public static UUID GENUS_NAME_UUID = UUID.fromString("8d761fc4-b509-42f4-9568-244161934336");
    public static UUID GENUS_UUID = UUID.fromString("bf4298a8-1735-4353-a210-244442e1bd62");
    public static UUID BASIONYM_UUID = UUID.fromString("7911c51d-ccb7-4708-8992-639eae58a0e3");
    public static UUID SPECIES1_UUID = UUID.fromString("f0eb77d9-76e0-47f4-813f-9b5605b78685");
    public static UUID SPECIES1_NAME_UUID = UUID.fromString("efd78713-126f-42e1-9070-a1ff83f12abf");
    public static UUID SYNONYM_NAME_UUID = UUID.fromString("b9cbaa74-dbe0-4930-8050-b7754ce85dc0");
    public static UUID SPECIES2_NAME_UUID = UUID.fromString("0267ab67-483e-4da5-b654-11013b242c22");
    public static UUID SPECIES2_UUID = UUID.fromString("e20eb549-ced6-4e79-9d74-44f0792a4929");
    public static UUID SYNONYM2_NAME_UUID = UUID.fromString("7c17c811-4201-454b-8108-7be7c91c0938");
    public static UUID SYNONYM2_UUID = UUID.fromString("2520b103-bd89-4ac1-99e4-e3bfcedfd4eb");
    public static UUID SPECIES5_NAME_UUID = UUID.fromString("0c6ecaac-804d-49e5-a33f-1b7ee77439e3");

/****************** TESTS *****************************/


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getTaxonByUuid(java.util.UUID)}.
     */
    @Test
    public final void testGetTaxonByUuid() {
        Taxon expectedTaxon = Taxon.NewInstance(null, null);
        UUID uuid = service.save(expectedTaxon).getUuid();
        TaxonBase<?> actualTaxon = service.find(uuid);
        assertEquals(expectedTaxon, actualTaxon);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getTaxonByUuid(java.util.UUID)}.
     */
    @Test
    public final void testGetTaxonByTitle() {
        TaxonName name = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(), "Abies", null, "alba", null, null, null, null, null);
        Taxon expectedTaxon = Taxon.NewInstance(name, null);
        expectedTaxon.setDoubtful(true);
        service.save(expectedTaxon);
        IdentifiableServiceConfiguratorImpl<TaxonBase> config = new IdentifiableServiceConfiguratorImpl<TaxonBase>();
        config.setTitleSearchString("Abies alba*");
        //doubtful taxa should be found
        Pager<TaxonBase> actualTaxa = service.findByTitle(config);
        assertEquals(expectedTaxon, actualTaxa.getRecords().get(0));

        //and other taxa as well
        expectedTaxon.setDoubtful(false);
        service.saveOrUpdate(expectedTaxon);
        actualTaxa = service.findByTitle(config);
        assertEquals(expectedTaxon, actualTaxa.getRecords().get(0));
    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#saveTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
     */
    @Test
    public final void testSaveTaxon() {
        Taxon expectedTaxon = Taxon.NewInstance(null, null);
        UUID uuid = service.save(expectedTaxon).getUuid();
        TaxonBase<?> actualTaxon = service.find(uuid);
        assertEquals(expectedTaxon, actualTaxon);
    }

    @Test
    public final void testSaveOrUpdateTaxon() {
        Taxon expectedTaxon = Taxon.NewInstance(null, null);
        UUID uuid = service.save(expectedTaxon).getUuid();
        TaxonBase<?> actualTaxon = service.find(uuid);
        assertEquals(expectedTaxon, actualTaxon);

        actualTaxon.setName(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()));
        try{
            service.saveOrUpdate(actualTaxon);
        }catch(Exception e){
            Assert.fail();
        }
    }

    @Test
    public final void testSaveOrUpdateTaxonWithMisappliedName() {

        Taxon expectedTaxon = Taxon.NewInstance(null, null);
        TaxonName misappliedNameName = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(), "Abies", null, "alba", null, null, null, null, null);

        UUID misappliedNameNameUuid = nameService.save(misappliedNameName).getUuid();
        misappliedNameName = nameService.find(misappliedNameNameUuid);
        SpecimenTypeDesignation typeDes = save(SpecimenTypeDesignation.NewInstance());
        DerivedUnit derivedUnit = DerivedUnit.NewPreservedSpecimenInstance();
        occurenceService.save(derivedUnit);

        FieldUnit fieldUnit = FieldUnit.NewInstance();
        DerivationEvent.NewSimpleInstance(fieldUnit, derivedUnit, DerivationEventType.ACCESSIONING());
        typeDes.setTypeSpecimen(derivedUnit);
        misappliedNameName.addTypeDesignation(typeDes, false);
        Taxon misappliedName = Taxon.NewInstance(misappliedNameName, null);
        UUID misappliedNameUuid = service.save(misappliedName).getUuid();
        misappliedName = (Taxon) service.find(misappliedNameUuid);
        expectedTaxon.addMisappliedName(misappliedName, null, null);
        UUID uuid = service.save(expectedTaxon).getUuid();
        TaxonBase<?> actualTaxon = service.find(uuid);
        assertEquals(expectedTaxon, actualTaxon);
        misappliedName.setSec(save(ReferenceFactory.newArticle()));

        try{
            service.saveOrUpdate(actualTaxon);
            misappliedName = (Taxon)service.find(misappliedNameUuid);
            Assert.assertNotNull(misappliedName.getSec());
        }catch(Exception e){
            Assert.fail();
        }
        commitAndStartNewTransaction(null);
        actualTaxon = service.find(uuid);
        ((Taxon)actualTaxon).getTaxonRelations(misappliedName).iterator().next().getFromTaxon().setSec(null);
        try{
            service.saveOrUpdate(actualTaxon);
            misappliedName = (Taxon)service.find(misappliedNameUuid);
            Assert.assertNull(misappliedName.getSec());
        }catch(Exception e){
            Assert.fail();
        }
    }


    @Test
    public final void testSwapSynonymAndAcceptedTaxon() throws FileNotFoundException{

        createTestDataSet();
        synonym.setSec(ReferenceFactory.newArticle());
        service.saveOrUpdate(synonym);
        UpdateResult result = service.swapSynonymAndAcceptedTaxon(synonym, taxWithSyn, true, false, SecReferenceHandlingSwapEnum.AlwaysDelete, null, null);

        // find forces flush
        Taxon tax = (Taxon)service.find(result.getCdmEntity().getUuid());
        MatchingTaxonConfigurator configurator = MatchingTaxonConfigurator.NewInstance();
        configurator.setTaxonNameTitle("Test3");
        List<TaxonBase> synList = service.findTaxaByName(configurator);

        if (synList.size() > 0){
            TaxonBase<?> syn = synList.get(0);
            assertTrue(tax.getSynonyms().contains(syn));
        }else{
            Assert.fail("There should be a synonym with name Test3");
        }
        assertTrue(tax.getName().getTitleCache().equals("Test2"));
    }

    @Test
    public final void testSwapSynonymAndAcceptedTaxonNewUuid() throws FileNotFoundException{

        createTestDataSet();
        synonym.setSec(save(ReferenceFactory.newArticle()));
        service.saveOrUpdate(synonym);

        UpdateResult result = service.swapSynonymAndAcceptedTaxon(synonym,
                taxWithSyn, true, true, SecReferenceHandlingSwapEnum.AlwaysDelete, null, null);

        // find forces flush
        Taxon tax = (Taxon)service.find(result.getCdmEntity().getUuid());
        MatchingTaxonConfigurator configurator = MatchingTaxonConfigurator.NewInstance();
        configurator.setTaxonNameTitle("Test3");
        @SuppressWarnings("rawtypes")
        List<TaxonBase> synList = service.findTaxaByName(configurator);

        if (synList.size() > 0){
            TaxonBase<?> syn = synList.get(0);
            assertTrue(tax.getSynonyms().contains(syn));
        }else{
            Assert.fail("There should be a synonym with name Test3");
        }
        assertTrue(tax.getName().getTitleCache().equals("Test2"));
    }

    @Test
    public final void testChangeSynonymToAcceptedTaxon() throws FileNotFoundException{

		createTestDataSet();

        UpdateResult result = new UpdateResult();
        try {
            result = service.changeSynonymToAcceptedTaxon(synonym, taxWithSyn, null, null, null, true);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Invocation of change method should not throw an exception");
        }
        taxWithSyn = null;
        //test flush (resave deleted object)
        TaxonBase<?> syn = service.find(uuidSyn);
        taxWithSyn = (Taxon)service.find(uuidTaxWithSyn);
        Taxon taxNew = (Taxon)service.find(result.getCdmEntity().getUuid());
        assertNull(syn);
        assertNotNull(taxWithSyn);
        assertNotNull(taxNew);

        Assert.assertEquals("New taxon should have 1 synonym relationship (the old homotypic synonym)", 1, ((Taxon)result.getCdmEntity()).getSynonyms().size());
    }

    @Test
    public final void testChangeSynonymToAcceptedTaxonWithSecHandlingAlwaysDelete(){
        Taxon genus = getTestTaxon();
        TaxonNode node = genus.getTaxonNodes().iterator().next();

        UpdateResult result = new UpdateResult();
        try {
            result = service.changeSynonymToAcceptedTaxon(SYNONYM2_UUID, SPECIES2_UUID, node.getUuid(), null, null, SecReferenceHandlingEnum.AlwaysDelete, true);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Invocation of change method should not throw an exception");
        }
        taxWithSyn = null;
        //test flush (resave deleted object)
        TaxonBase<?> syn = service.find(SYNONYM2_UUID);
        taxWithSyn = (Taxon)service.find(SPECIES2_UUID);
        TaxonNode taxNodeNew = nodeService.find(result.getCdmEntity().getUuid());
        Taxon taxNew = taxNodeNew.getTaxon();
        assertNull(syn);
        assertNotNull(taxWithSyn);
        assertNotNull(taxNew);
        assertNull(taxNew.getSec());
    }

    @Test
    public final void testChangeSynonymToAcceptedTaxonWithSecHandlingUseNewParentSec(){

        Taxon genus = getTestTaxon();
        TaxonNode node = genus.getTaxonNodes().iterator().next();
        UpdateResult result = new UpdateResult();
        TaxonBase<?> syn = service.find(SYNONYM2_UUID);
        Reference sec = save(ReferenceFactory.newBook());
        sec.setTitleCache("Flora Cuba", true);
        syn.setSec(sec);
        service.saveOrUpdate(syn);
        try {
            result = service.changeSynonymToAcceptedTaxon(SYNONYM2_UUID, SPECIES2_UUID, node.getUuid(), null, null, SecReferenceHandlingEnum.UseNewParentSec, true);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Invocation of change method should not throw an exception");
        }
        taxWithSyn = null;
        //test flush (resave deleted object)
        syn = service.find(SYNONYM2_UUID);
        taxWithSyn = (Taxon)service.find(SPECIES2_UUID);
        TaxonNode taxNodeNew = nodeService.find(result.getCdmEntity().getUuid());
        Taxon taxNew = taxNodeNew.getTaxon();
        assertNull(syn);
        assertNotNull(taxWithSyn);
        assertNotNull(taxNew);
        assertNotNull(taxNew.getSec());
        assertEquals(taxWithSyn.getSec(), taxNew.getSec());

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testMoveSynonymToAnotherTaxon.xml")
    public final void testChangeSynonymToAcceptedTaxonWithSecHandlingWarningSelect(){
        Taxon genus = getTestTaxon();
        TaxonNode node = genus.getTaxonNodes().iterator().next();

        UpdateResult result = new UpdateResult();
        TaxonBase<?> syn = service.find(SYNONYM2_UUID);
        Reference sec = save(ReferenceFactory.newBook());
        sec.setTitleCache("Flora Cuba", true);
        Reference newSec = ReferenceFactory.newBook();
        newSec.setTitleCache("Flora Hawaii", true);
        UUID newSecUuid = referenceDao.saveOrUpdate(newSec);
        syn.setSec(sec);
        service.saveOrUpdate(syn);
        try {
            result = service.changeSynonymToAcceptedTaxon(SYNONYM2_UUID, SPECIES2_UUID, node.getUuid(), newSecUuid, "23", SecReferenceHandlingEnum.KeepOrSelect, true);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Invocation of change method should not throw an exception");
        }
        taxWithSyn = null;
        //test flush (resave deleted object)
        syn = service.find(SYNONYM2_UUID);
        taxWithSyn = (Taxon)service.find(SPECIES2_UUID);
        TaxonNode taxNodeNew = nodeService.find(result.getCdmEntity().getUuid());
        Taxon taxNew = taxNodeNew.getTaxon();
        assertNull(syn);
        assertNotNull(taxWithSyn);
        assertNotNull(taxNew);
        assertNotNull(taxNew.getSec());
        assertEquals(newSec, taxNew.getSec());

    }


    @Test
    public final void testChangeSynonymToAcceptedTaxonSynonymForTwoTaxa(){
        try {
			createTestDataSet();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


        Taxon taxon = null;
        UpdateResult result = new UpdateResult();
        try {
            result = service.changeSynonymToAcceptedTaxon(synonym, taxWithSyn, null, null, null, true);
            service.save(taxon);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Invocation of change method should not throw an exception");
        }
        taxWithSyn = null;
        tax2WithSyn = null;

        //test flush (resave deleted object)
        TaxonBase<?> syn = service.find(uuidSyn);
        taxWithSyn = (Taxon)service.find(uuidTaxWithSyn);
        Taxon taxNew = (Taxon)service.find(result.getCdmEntity().getUuid());
        assertNull(syn);
        assertNotNull(taxWithSyn);
        assertNotNull(taxNew);

       // Assert.assertEquals("New taxon should have 1 synonym relationship (the old homotypic synonym)", 1, taxon.getSynonymRelations().size());
    }

    /**
     * Old implementation taken from {@link TaxonServiceImplBusinessTest} for old version of method.
     */
    @Test
    public final void testMoveSynonymToAnotherTaxon_OLD() {

        SynonymType heteroTypicSynonymType = SynonymType.HETEROTYPIC_SYNONYM_OF;
        Reference reference = save(ReferenceFactory.newGeneric());
        String referenceDetail = "test";

        INonViralName t1n = TaxonNameFactory.NewNonViralInstance(null);
        Taxon t1 = Taxon.NewInstance(t1n, reference);
        INonViralName t2n = TaxonNameFactory.NewNonViralInstance(null);
        Taxon t2 = Taxon.NewInstance(t2n, reference);
        INonViralName s1n = TaxonNameFactory.NewNonViralInstance(null);
        Synonym s1 = save(Synonym.NewInstance(s1n, reference));
        t1.addSynonym(s1, heteroTypicSynonymType);
        service.saveOrUpdate(t1);

        Synonym synonym = t1.getSynonyms().iterator().next();

        boolean keepReference = false;
        boolean moveHomotypicGroup = false;
        try {
            service.moveSynonymToAnotherTaxon(synonym, t2, moveHomotypicGroup, heteroTypicSynonymType, reference.getUuid(), referenceDetail, keepReference);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Method call should not throw exception");
        }

        Assert.assertTrue("t1 should have no synonyms", t1.getSynonyms().isEmpty());

        Set<Synonym> synonyms = t2.getSynonyms();
        Assert.assertTrue("t2 should have exactly one synonym", synonyms.size() == 1);

        synonym = synonyms.iterator().next();

        Assert.assertEquals(t2, synonym.getAcceptedTaxon());
        Assert.assertEquals(heteroTypicSynonymType, synonym.getType());
        Assert.assertEquals(reference, synonym.getSec());
        Assert.assertEquals(referenceDetail, synonym.getSecMicroReference());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testMoveSynonymToAnotherTaxon.xml")
    public final void testMoveSynonymToAnotherTaxon() throws Exception {
        final String[] tableNames = new String[]{};

//        printDataSet(System.err, new String[]{"AgentBase", "TaxonBase"});
//        printDataSet(System.err, new String[]{"TaxonNode"});

        UUID uuidNewTaxon = UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
        UUID uuidOldTaxon = UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
        UUID uuidSyn1 = UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSyn3 = UUID.fromString("3fba2b22-22ae-4291-af67-faab748a5232");
        UUID uuidSyn4 = UUID.fromString("f9b589c7-50cf-4df2-a52e-1b85eb7e4805");
        UUID uuidSyn5 = UUID.fromString("fcc0bcf8-8bac-43bd-9508-1e97821587dd");
        UUID uuidSyn6 = UUID.fromString("0ccd4e7c-6fbd-4b7c-bd47-29e45b92f34b");
        UUID uuidRef1 = UUID.fromString("336f9b38-698c-45d7-be7b-993ed3355bdc");
        UUID uuidRef2 = UUID.fromString("c8f49d1a-69e1-48a3-98bb-45d61f3da3e7");


        boolean moveHomotypicGroup = true;
        SynonymType newSynonymType = null;
        boolean keepReference = true;
        Reference newReference = null;
        String newReferenceDetail = null;

        Taxon newTaxon = (Taxon)service.load(uuidNewTaxon);
        Synonym homotypicSynonym = (Synonym)service.load(uuidSyn1);
        Assert.assertNotNull("Synonym should exist", homotypicSynonym);
        Assert.assertNotNull("Synonym should have 1 relation", homotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, homotypicSynonym.getAcceptedTaxon().getUuid());
        Taxon oldTaxon = homotypicSynonym.getAcceptedTaxon();

        try {
            service.moveSynonymToAnotherTaxon(homotypicSynonym, newTaxon, moveHomotypicGroup, newSynonymType, null, newReferenceDetail, keepReference);
            Assert.fail("Homotypic synonym move to other taxon should throw an exception");
        } catch (HomotypicalGroupChangeException e) {
            if (e.getMessage().contains("Synonym is in homotypic group with accepted taxon and other synonym(s). First remove synonym from homotypic group of accepted taxon before moving to other taxon")){
                //OK
                commitAndStartNewTransaction(tableNames);
            }else{
                Assert.fail("Unexpected exception occurred: " + e.getMessage());
            }
        }
        //Asserts
        homotypicSynonym = (Synonym)service.load(uuidSyn1);
        Assert.assertNotNull("Synonym should still exist", homotypicSynonym);
        Assert.assertNotNull("Synonym should still have 1 relation", homotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", oldTaxon, homotypicSynonym.getAcceptedTaxon());

        //test heterotypic synonym with other synonym in homotypic group
        newTaxon = (Taxon)service.load(uuidNewTaxon);
        Synonym heterotypicSynonym = (Synonym)service.load(uuidSyn3);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should have 1 relation", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, heterotypicSynonym.getAcceptedTaxon().getUuid());
        oldTaxon = heterotypicSynonym.getAcceptedTaxon();
        moveHomotypicGroup = false;

        try {
            service.moveSynonymToAnotherTaxon(heterotypicSynonym, newTaxon, moveHomotypicGroup, newSynonymType, null, newReferenceDetail, keepReference);
            Assert.fail("Heterotypic synonym move to other taxon should throw an exception");
        } catch (HomotypicalGroupChangeException e) {
            if (e.getMessage().contains("Synonym is in homotypic group with other synonym(s). Either move complete homotypic group or remove synonym from homotypic group prior to moving to other taxon")){
                //OK
                commitAndStartNewTransaction(tableNames);
            }else{
                Assert.fail("Unexpected exception occurred: " + e.getMessage());
            }
        }
        //Asserts
        heterotypicSynonym = (Synonym)service.load(uuidSyn3);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should have accepted taxon", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of single relation should still be the old taxon", oldTaxon, heterotypicSynonym.getAcceptedTaxon());


        //test heterotypic synonym with no other synonym in homotypic group
        //+ keep reference

        newTaxon = (Taxon)service.load(uuidNewTaxon);
        heterotypicSynonym = (Synonym)service.load(uuidSyn5);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should have accepted taxon", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, heterotypicSynonym.getAcceptedTaxon().getUuid());
        oldTaxon = heterotypicSynonym.getAcceptedTaxon();
        moveHomotypicGroup = false;

        try {
            service.moveSynonymToAnotherTaxon(heterotypicSynonym, newTaxon, moveHomotypicGroup, newSynonymType, null, newReferenceDetail, keepReference);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Move of single heterotypic synonym should not throw exception: " + e.getMessage());
        }
        //Asserts
        commitAndStartNewTransaction(tableNames);

        heterotypicSynonym = (Synonym)service.load(uuidSyn5);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should have accepted taxon", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of single relation should be new taxon", newTaxon, heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Old detail should be kept", "rel5", heterotypicSynonym.getSecMicroReference());

        //test heterotypic synonym with other synonym in homotypic group and moveHomotypicGroup="true"
        //+ new detail
        newTaxon = (Taxon)service.load(uuidNewTaxon);
        heterotypicSynonym = (Synonym)service.load(uuidSyn3);
        Reference ref1 = referenceDao.load(uuidRef1);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should have 1 relation", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, heterotypicSynonym.getAcceptedTaxon().getUuid());
        oldTaxon = heterotypicSynonym.getAcceptedTaxon();
        Assert.assertEquals("Detail should be ref1", ref1, heterotypicSynonym.getSec());
        Assert.assertEquals("Detail should be 'rel3'", "rel3", heterotypicSynonym.getSecMicroReference());
        TaxonName oldSynName3 = heterotypicSynonym.getName();

        Synonym heterotypicSynonym4 = (Synonym)service.load(uuidSyn4);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym4);
        Assert.assertNotNull("Synonym should have accepted taxon", heterotypicSynonym4.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of other synonym in group should be the old taxon", uuidOldTaxon, heterotypicSynonym4.getAcceptedTaxon().getUuid());
        Assert.assertSame("Homotypic group of both synonyms should be same", oldSynName3.getHomotypicalGroup() , heterotypicSynonym4.getName().getHomotypicalGroup() );

        moveHomotypicGroup = true;
        keepReference = false;

        try {
            service.moveSynonymToAnotherTaxon(heterotypicSynonym4, newTaxon, moveHomotypicGroup, newSynonymType, null, newReferenceDetail, keepReference);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Move with 'moveHomotypicGroup = true' should not throw exception: " + e.getMessage());
        }
        //Asserts
        commitAndStartNewTransaction(tableNames);
        heterotypicSynonym = (Synonym)service.load(uuidSyn3);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should still have accepted taxon", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of relation should be new taxon now", newTaxon, heterotypicSynonym.getAcceptedTaxon());
        TaxonName synName3 = heterotypicSynonym.getName();

        heterotypicSynonym = (Synonym)service.load(uuidSyn4);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should still have accepted taxon", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of relation should be new taxon now", newTaxon, heterotypicSynonym.getAcceptedTaxon());
        Assert.assertNull("Old citation should be removed", heterotypicSynonym.getSec());
        Assert.assertNull("Old detail should be removed", heterotypicSynonym.getSecMicroReference());
        TaxonName synName4 = heterotypicSynonym.getName();
        Assert.assertEquals("Homotypic group of both synonyms should be equal", synName3.getHomotypicalGroup() , synName4.getHomotypicalGroup() );
        Assert.assertSame("Homotypic group of both synonyms should be same", synName3.getHomotypicalGroup() , synName4.getHomotypicalGroup() );
        Assert.assertEquals("Homotypic group of both synonyms should be equal to old homotypic group", oldSynName3.getHomotypicalGroup() , synName3.getHomotypicalGroup() );

        //test single heterotypic synonym to homotypic synonym of new taxon
        //+ new reference
        newTaxon = (Taxon)service.load(uuidNewTaxon);
        Reference ref2 = referenceDao.load(uuidRef2);
        heterotypicSynonym = (Synonym)service.load(uuidSyn6);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should have accepted taxon", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, heterotypicSynonym.getAcceptedTaxon().getUuid());
        oldTaxon = heterotypicSynonym.getAcceptedTaxon();
        moveHomotypicGroup = false;
        keepReference = false;
        newReference = ref2;
        newReferenceDetail = "newRefDetail";
        newSynonymType = SynonymType.HOMOTYPIC_SYNONYM_OF;

        try {
            service.moveSynonymToAnotherTaxon(heterotypicSynonym, newTaxon, moveHomotypicGroup, newSynonymType, newReference.getUuid(), newReferenceDetail, keepReference);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Move of single heterotypic synonym should not throw exception: " + e.getMessage());
        }
        //Asserts
        commitAndStartNewTransaction(tableNames);
        heterotypicSynonym = (Synonym)service.load(uuidSyn6);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertNotNull("Synonym should still have accepted taxon", heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("Relationship type should be 'homotypic synonym'", newSynonymType, heterotypicSynonym.getType());
        Assert.assertEquals("Accepted taxon of single relation should be new taxon", newTaxon, heterotypicSynonym.getAcceptedTaxon());
        Assert.assertEquals("New citation should be ref2", ref2 ,heterotypicSynonym.getSec());
        Assert.assertEquals("New detail should be kept", "newRefDetail", heterotypicSynonym.getSecMicroReference());

        Assert.assertEquals("New taxon and new synonym should have equal homotypical group", heterotypicSynonym.getHomotypicGroup(), heterotypicSynonym.getAcceptedTaxon().getHomotypicGroup());
        Assert.assertSame("New taxon and new synonym should have same homotypical group", heterotypicSynonym.getHomotypicGroup(), heterotypicSynonym.getAcceptedTaxon().getHomotypicGroup());
    }

    @Test
    public final void testGetHeterotypicSynonymyGroups(){

        Rank rank = Rank.SPECIES();
        Reference ref1 = save(ReferenceFactory.newGeneric());
        //HomotypicalGroup group = HomotypicalGroup.NewInstance();
        Taxon taxon1 = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test3", null, null, null, null, null, null, null), null);
        Synonym synonym0 = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym1 = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym2 = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test4", null, null, null, null, null, null, null), null);
        synonym0.getName().setHomotypicalGroup(taxon1.getHomotypicGroup());
        synonym2.getName().setHomotypicalGroup(synonym1.getHomotypicGroup());
        //tax2.addHeterotypicSynonymName(synonym.getName());
        taxon1.addSynonym(synonym1, SynonymType.HETEROTYPIC_SYNONYM_OF);
        taxon1.addSynonym(synonym2, SynonymType.HETEROTYPIC_SYNONYM_OF);

        service.save(synonym1);
        service.save(synonym2);
        service.save(taxon1);

        List<List<Synonym>> heteroSyns = service.getHeterotypicSynonymyGroups(taxon1, null);
        Assert.assertEquals("There should be 1 heterotypic group", 1, heteroSyns.size());
        List<Synonym> synList = heteroSyns.get(0);
        Assert.assertEquals("There should be 2 heterotypic syns in group 1", 2, synList.size());

        //test sec
        synonym2.setSec(ref1);
        heteroSyns = service.getHeterotypicSynonymyGroups(taxon1, null);
        Assert.assertEquals("There should be 1 heterotypic group", 1, heteroSyns.size());
        synList = heteroSyns.get(0);
        Assert.assertEquals("getHeterotypicSynonymyGroups should be independent of sec reference", 2, synList.size());
    }

    @Test
    public final void testGetHomotypicSynonymsByHomotypicGroup(){

        Rank rank = Rank.SPECIES();
        Reference ref1 = save(ReferenceFactory.newGeneric());
        //HomotypicalGroup group = HomotypicalGroup.NewInstance();
        Taxon taxon1 = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test3", null, null, null, null, null, null, null), null);
        Synonym synonym0 = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym1 = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym2 = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test4", null, null, null, null, null, null, null), null);
        synonym0.getName().setHomotypicalGroup(taxon1.getHomotypicGroup());
        synonym2.getName().setHomotypicalGroup(synonym1.getHomotypicGroup());
        //tax2.addHeterotypicSynonymName(synonym.getName());
        taxon1.addSynonym(synonym0, SynonymType.HOMOTYPIC_SYNONYM_OF);
        taxon1.addSynonym(synonym1, SynonymType.HETEROTYPIC_SYNONYM_OF);
        taxon1.addSynonym(synonym2, SynonymType.HETEROTYPIC_SYNONYM_OF);

        service.save(synonym1);
        service.save(synonym2);
        service.save(taxon1);

        List<Synonym> homoSyns = service.getHomotypicSynonymsByHomotypicGroup(taxon1, null);
        Assert.assertEquals("There should be 1 heterotypic group", 1, homoSyns.size());
        Assert.assertSame("The homotypic synonym should be synonym0", synonym0, homoSyns.get(0));

        //test sec
        synonym0.setSec(ref1);
        homoSyns = service.getHomotypicSynonymsByHomotypicGroup(taxon1, null);
        Assert.assertEquals("getHeterotypicSynonymyGroups should be independent of sec reference", 1, homoSyns.size());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testDeleteSynonym.xml")
    //test delete synonym, but the name will not be deleted
    public final void testDeleteSynonymSynonymTaxonDontDeleteName(){
        final String[]tableNames = {
//                "TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
//                "HomotypicalGroup","HomotypicalGroup_AUD"
        };

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
        long nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be two relationship left in the database", 2, nRelations);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        SynonymDeletionConfigurator config = new SynonymDeletionConfigurator();
        config.setDeleteNameIfPossible(false);
        config.setNewHomotypicGroupIfNeeded(true);
        service.deleteSynonym(synonym1, config);

        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 1 synonym left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
        nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be no relationship left in the database", 1, nRelations);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testDeleteSynonym.xml")
    //test delete synonym and his name
    public final void testDeleteSynonymSynonymTaxonDeleteName(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
        long nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be 2 relationship left in the database", 2, nRelations);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        service.deleteSynonym(synonym1, new SynonymDeletionConfigurator());

        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 1 synonym left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should be 3 names left in the database", 3, nNames);
        nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be 1 relationship left in the database", 1, nRelations);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testDeleteSynonym.xml")
    //test remove synonym from taxon -> synonym and name still in the db and the synonymrelationship to the other taxon
    //test delete synonym -> all relationships are deleted, the name is deleted and the synonym itself
    public final void testDeleteSynonymSynonymTaxonBooleanRelToOneTaxon(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

        UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");

        Taxon taxon2 = (Taxon)service.load(uuidTaxon1);

        List<String> initStrat = new ArrayList<>();
        initStrat.add("markers");
        Synonym synonym1 = (Synonym)service.load(uuidSynonym1, initStrat);
        long nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be 2 relationship left in the database", 2, nRelations);

        taxon2.removeSynonym(synonym1, false);
        service.saveOrUpdate(taxon2);

        commitAndStartNewTransaction(null);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
        nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be 1 relationship left in the database", 1, nRelations);
        Marker marker1 = Marker.NewInstance(MarkerType.IMPORTED(), true);
        Marker marker2 = Marker.NewInstance(MarkerType.COMPUTED(), true);
        synonym1.addMarker(marker1);
        synonym1.addMarker(marker2);
        service.update(synonym1);
        synonym1 =(Synonym) service.load(uuidSynonym1);

        Set<Marker> markers = synonym1.getMarkers();
        Marker marker = markers.iterator().next();
        UUID markerUUID = marker.getUuid();
       // taxon2 = (Taxon)service.load(uuidTaxon2);
        synonym1 = (Synonym)service.load(uuidSynonym1);
        //the marker should not prevent the deletion
        DeleteResult result = service.deleteSynonym(synonym1, new SynonymDeletionConfigurator());
        if (!result.isOk()){
        	Assert.fail();
        }

        commitAndStartNewTransaction(tableNames);
        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 1 synonym left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should be 3 names left in the database", 3, nNames);
        nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be no relationship left in the database", 1, nRelations);
        marker = markerService.load(markerUUID);
        assertNull(marker);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testDeleteSynonym.xml")
    //this test is more or less obsolete since we have no synonym relationships anymore
    //test delete synonym, only for a special taxon, but because of other relationships it will not be deleted at all
    public final void testDeleteSynonymSynonymTaxonBooleanDeleteOneTaxon(){

        final String[]tableNames = {
//                "TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
//                "HomotypicalGroup","HomotypicalGroup_AUD"
        };
        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

        UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");

        Taxon taxon2 = (Taxon)service.load(uuidTaxon2);
        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        synonym1.setSec(save(ReferenceFactory.newArticle()));

        taxon2.addSynonym(synonym1, SynonymType.HETEROTYPIC_SYNONYM_OF);
        service.saveOrUpdate(synonym1);
        long nRelations = service.countSynonyms(true);
        //this was "3" when we still had synonym relationships
        Assert.assertEquals("There should be 2 relationship left in the database", 2, nRelations);
        service.deleteSynonym(synonym1, new SynonymDeletionConfigurator());

        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        //this was "2" when we still had synonym relationships
        Assert.assertEquals("There should still be 1 synonym left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        //was 3
        Assert.assertEquals("There should be 3 names left in the database", 3, nNames);
        nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be 1 related synonym left in the database", 1, nRelations);
    }

    private Reference save(Reference ref) {
        referenceDao.save(ref);
        return ref;
    }

    private Synonym save(Synonym syn) {
        service.save(syn);
        return syn;
    }

    private <S extends DescriptionBase<?>> S save(S newDescription) {
        descriptionDao.save(newDescription);
        return newDescription;
    }

    private <S extends TypeDesignationBase<?>> S save(S td) {
        typeDesignationDao.save(td);
        return td;
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testDeleteSynonym.xml")

    public final void testDeleteSynonymWithAnnotations(){
        final String[]tableNames = {
//                "TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
//                "HomotypicalGroup","HomotypicalGroup_AUD"
        };

        UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");

        Taxon taxon2 = (Taxon)service.load(uuidTaxon2);
        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        taxon2.addSynonym(synonym1, SynonymType.HETEROTYPIC_SYNONYM_OF);

        Annotation annotation = Annotation.NewDefaultLanguageInstance("test");
        synonym1.addAnnotation(annotation);
        service.saveOrUpdate(synonym1);

        DeleteResult result = service.deleteSynonym(synonym1, new SynonymDeletionConfigurator());
        if (result.isError()){
            Assert.fail();
        }
        this.commitAndStartNewTransaction(tableNames);
    }

    @Test
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")

    public final void testDeleteSynonymSynonymTaxonBooleanWithRelatedName(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        TaxonName name2 = nameService.load(uuidSynonymName2);
        UUID name3Uuid = synonym1.getName().getUuid();
        TaxonName name3 = nameService.load(name3Uuid);
        name3.addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null, null);

        service.saveOrUpdate(synonym1);

        long nRelations = nameService.listNameRelationships(null, 1000, 0, null, null).size();
        logger.info("number of name relations: " + nRelations);
        Assert.assertEquals("There should be 1 name relationship left in the database", 1, nRelations);
        SynonymDeletionConfigurator config = new SynonymDeletionConfigurator();

        service.deleteSynonym(synonym1, config);

        this.commitAndStartNewTransaction(tableNames);
        //synonym is deleted, but the name can not be deleted because of a name relationship
        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 1 synonyms left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should be 4 names left in the database (name is related to synonymName2)", 4, nNames);
        nRelations = service.countSynonyms(true);
        //may change with better implementation of countAllRelationships (see #2653)
        nRelations = nameService.listNameRelationships(null, 1000, 0, null, null).size();
        logger.info("number of name relations: " + nRelations);
        Assert.assertEquals("There should be 1 name relationship left in the database", 1, nRelations);

        //clean up database
        name2 = nameService.load(uuidSynonymName2);
        NameRelationship rel = CdmBase.deproxy(name2.getNameRelations().iterator().next(), NameRelationship.class);
        name2.removeNameRelationship(rel);
        nameService.save(name2);
        this.setComplete();
        this.endTransaction();
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanWithRelatedNameDeleteAllNameRelations(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        TaxonName name2 = nameService.load(uuidSynonymName2);
        UUID name3Uuid = synonym1.getName().getUuid();
        TaxonName name3 = nameService.load(name3Uuid);
        name3.addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null, null);

        service.saveOrUpdate(synonym1);

        long nRelations = nameService.listNameRelationships(null, 1000, 0, null, null).size();
        logger.info("number of name relations: " + nRelations);
        Assert.assertEquals("There should be 1 name relationship left in the database", 1, nRelations);
        SynonymDeletionConfigurator config = new SynonymDeletionConfigurator();
        NameDeletionConfigurator nameDeletionConfig = new NameDeletionConfigurator();
        nameDeletionConfig.setRemoveAllNameRelationships(true);
        config.setNameDeletionConfig(nameDeletionConfig);

        service.deleteSynonym(synonym1, config);

        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 1 synonyms left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should be 3 names left in the database ", 3, nNames);
        nRelations = service.countSynonyms(true);
        //may change with better implementation of countAllRelationships (see #2653)
        nRelations = nameService.listNameRelationships(null, 1000, 0, null, null).size();
        logger.info("number of name relations: " + nRelations);
        Assert.assertEquals("There should be no name relationship left in the database", 0, nRelations);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanWithRelatedNameIgnoreIsBasionym(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        TaxonName synName2 = nameService.load(uuidSynonymName2);
        UUID name3Uuid = synonym1.getName().getUuid();
        TaxonName synName1 = nameService.load(name3Uuid);
        synName1.addRelationshipFromName(synName2, NameRelationshipType.BASIONYM(), null, null);

        service.saveOrUpdate(synonym1);

        long nRelations = nameService.listNameRelationships(null, 1000, 0, null, null).size();
        logger.info("number of name relations: " + nRelations);
        Assert.assertEquals("There should be 1 name relationship left in the database", 1, nRelations);
        SynonymDeletionConfigurator config = new SynonymDeletionConfigurator();
        NameDeletionConfigurator nameDeletionConfig = new NameDeletionConfigurator();
        nameDeletionConfig.setIgnoreIsBasionymFor(true);
        config.setNameDeletionConfig(nameDeletionConfig);

        DeleteResult result =service.deleteSynonym(synonym1, config);
        if (!result.isOk()){
        	Assert.fail();
        }

        logger.debug(result);
        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 1 synonyms left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should be 3 names left in the database ", 3, nNames);
        nRelations = service.countSynonyms(true);
        //may change with better implementation of countAllRelationships (see #2653)
        nRelations = nameService.listNameRelationships(null, 1000, 0, null, null).size();
        logger.info("number of name relations: " + nRelations);
        Assert.assertEquals("There should be no name relationship left in the database", 0, nRelations);
    }

    @Test
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanWithRollback(){
//        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
//                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
        long nRelations = service.countSynonyms(true);

        //may change with better implementation of countAllRelationships (see #2653)

        logger.debug("");
        Assert.assertEquals("There should be 2 relationships in the database (the 2 synonym relationship) but no name relationship", 2, nRelations);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        TaxonName name2 = nameService.load(uuidSynonymName2);
        synonym1.getName().addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null, null);

        service.deleteSynonym(synonym1, new SynonymDeletionConfigurator());

        this.rollback();
//		printDataSet(System.out, tableNames);
        this.startNewTransaction();

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 2 synonyms left in the database", 2, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
        nRelations = service.countSynonyms(true);
        //may change with better implementation of countAllRelationships (see #2653)
        Assert.assertEquals("There should be 2 relationship in the database (the 2 synonym relationship) but no name relationship", 2, nRelations);
    }

    @Test
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanWithoutTransaction(){
        @SuppressWarnings("unused")
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonName","TaxonName_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
        long nRelations = service.countSynonyms(true);
        //may change with better implementation of countAllRelationships (see #2653)
        Assert.assertEquals("There should be 2 relationship in the database (the 2 synonym relationships) but no name relationship", 2, nRelations);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        TaxonName name2 = nameService.load(uuidSynonymName2);
        synonym1.getName().addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null, null);

        service.saveOrUpdate(synonym1);
        nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be two relationships in the database", 2, nRelations);
        this.setComplete();
        this.endTransaction();

//        printDataSet(System.out, tableNames);

        //out of wrapping transaction
        service.deleteSynonym(synonym1,  new SynonymDeletionConfigurator());

        this.startNewTransaction();

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 1 synonyms left in the database. The rollback on name delete should not lead to rollback in synonym delete.", 1, nSynonyms);
        nNames = nameService.count(TaxonName.class);
        Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
        nRelations = service.countSynonyms(true);
        Assert.assertEquals("There should be no taxon or synonym relationship in the database", 1, nRelations);
        nRelations = nameService.listNameRelationships(null, 1000, 0, null, null).size();
        Assert.assertEquals("There should be one name relationship in the database", 1, nRelations);
    }

    @Test
    @DataSet("TaxonServiceImplTest.testInferredSynonyms.xml")
    public void testCreateInferredSynonymy(){

        UUID classificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");
        Classification classification = classificationService.find(classificationUuid);

        //verify expected DB state
        List <Synonym> synonyms = service.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 2 (Acheontitia ciprosus and SynGenus)",
                2, synonyms.size());

        //load accepted species
        UUID acherontiaLachesisTaxonUuid = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");
        Taxon taxon = (Taxon)service.find(acherontiaLachesisTaxonUuid);

        //inferred epithet
        List<Synonym> inferredSynonyms = service.createInferredSynonyms(taxon, classification, SynonymType.INFERRED_EPITHET_OF, true);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals ("the name of inferred epithet should be SynOfAcherontia lachesis", "SynOfAcherontia lachesis syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());

        //inferred genus
        inferredSynonyms = service.createInferredSynonyms(taxon, classification, SynonymType.INFERRED_GENUS_OF, true);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals ("the name of inferred genus should be Acherontia ciprosus", "Acherontia ciprosus syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());

        //inferred combination
        inferredSynonyms = service.createInferredSynonyms(taxon, classification, SynonymType.POTENTIAL_COMBINATION_OF, true);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals ("the name of potential combination should be SynOfAcherontia ciprosus", "SynOfAcherontia ciprosus syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());
        //assertTrue("set of synonyms should contain an inferred Synonym ", synonyms.contains(arg0))

        //TODO test cases with infrageneric names and subspecies

        //TODO deduplication if name exists already or if both exists, inferred epithet/genus and potential combination

        //TODO misapplied name handling

        //TODO test references, sources and idInSource (etc.)

        //TODO test that only zoological names return inferred synonyms
    }

    @Test
    @DataSet("../../database/ClearDBDataSet.xml")
    public final void testTaxonDeletionConfig(){
        final String[]tableNames = {}
//                "Classification", "Classification_AUD",
//                "TaxonBase","TaxonBase_AUD",
//                "TaxonNode","TaxonNode_AUD",
//                "TaxonName","TaxonName_AUD",
//                "TaxonRelationship", "TaxonRelationship_AUD",
//                "TaxonDescription", "TaxonDescription_AUD",
//                "HomotypicalGroup","HomotypicalGroup_AUD",
//                "PolytomousKey","PolytomousKey_AUD",
//                "PolytomousKeyNode","PolytomousKeyNode_AUD",
//                "Media","Media_AUD",
//                "DescriptiveDataSet","DescriptiveDataSet_AUD",
//                "DescriptionElementBase","DescriptionElementBase_AUD",
//        		"DeterminationEvent","DeterminationEvent_AUD",
//        		"SpecimenOrObservationBase","SpecimenOrObservationBase_AUD"}
        ;

        commitAndStartNewTransaction(tableNames);
        getTestTaxon();
        commitAndStartNewTransaction(tableNames);
        int nTaxa = service.count(Taxon.class);

        Assert.assertEquals("There should be 4 taxa in the database", 4, nTaxa);
        Taxon parent = (Taxon)service.find(GENUS_UUID);
        Assert.assertNotNull("Parent taxon should exist", parent);
        Taxon child1 = (Taxon)service.find(SPECIES1_UUID);
        Assert.assertNotNull("Child taxon should exist", child1);
        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
        config.setDeleteTaxonNodes(false);
        config.setDeleteMisappliedNames(false);
        //try {
            //commitAndStartNewTransaction(tableNames);

        DeleteResult result = service.deleteTaxon(child1.getUuid(), config, null);
        if (result.isOk()){
            Assert.fail("Delete should throw an error as long as name is used in classification.");
        }

        nTaxa = service.count(Taxon.class);
        Assert.assertEquals("There should be 4 taxa in the database", 4, nTaxa);
        child1 = (Taxon)service.find(SPECIES1_UUID);
        Assert.assertNotNull("Child taxon should exist", child1);
        Assert.assertEquals("Child should belong to 1 node", 1, child1.getTaxonNodes().size());

        TaxonNode node = child1.getTaxonNodes().iterator().next();
        child1.addSource(IdentifiableSource.NewInstance(OriginalSourceType.Import));

        SpecimenOrObservationBase<?> identifiedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);
        DeterminationEvent.NewInstance(child1, identifiedUnit);
        //UUID eventUUID = eventService.save(determinationEvent);
        UUID identifiedUnitUUID = occurenceService.save(identifiedUnit).getUuid();

        TaxonNode parentNode = node.getParent();
        parentNode =CdmBase.deproxy(parentNode, TaxonNode.class);
        parentNode.deleteChildNode(node);
        nodeService.save(parentNode);
        //commitAndStartNewTransaction(tableNames);

       // try {

       result = service.deleteTaxon(child1
    		   .getUuid(), config, null);
       if (result.isOk()){
           	Assert.fail("Delete should throw an exception because of the determination event");
       }

        //determinationEvent = (DeterminationEvent)eventService.load(eventUUID);
        commitAndStartNewTransaction(tableNames);
        identifiedUnit = occurenceService.load(identifiedUnitUUID);

        occurenceService.delete(identifiedUnit);

        commitAndStartNewTransaction(tableNames);
        child1 = (Taxon)service.find(SPECIES1_UUID);

        assertEquals(0, child1.getTaxonNodes().size());
       // try {

         result = service.deleteTaxon(child1.getUuid(), config, null);

         if (!result.isOk()){
            Assert.fail("Delete should not throw an exception anymore");
         }

        nTaxa = service.count(Taxon.class);
        Assert.assertEquals("There should be 3 taxa in the database", 3, nTaxa);

        config.setDeleteTaxonNodes(true);
        Taxon child2 =(Taxon) service.find(SPECIES2_UUID);

       // try {
        result = service.deleteTaxon(child2.getUuid(), config, child2.getTaxonNodes().iterator().next().getClassification().getUuid());
        if (!result.isOk()){
            Assert.fail("Delete should not throw an exception");
        }

        //service.find(uuid);

        nTaxa = service.count(Taxon.class);
        Assert.assertEquals("There should be 2 taxa in the database",2, nTaxa);
//		nNames = nameService.count(TaxonName.class);
//		Assert.assertEquals("There should be 3 names left in the database", 3, nNames);
//		int nRelations = service.countAllRelationships();
//		Assert.assertEquals("There should be no relationship left in the database", 0, nRelations);
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testDeleteTaxon(){

        //create a small classification
        Taxon testTaxon = getTestTaxon();
        service.save(testTaxon).getUuid();


        //test
        Taxon speciesTaxon = (Taxon)service.find(SPECIES1_UUID);
        Iterator<TaxonDescription> descriptionIterator = speciesTaxon.getDescriptions().iterator();
        UUID descrUUID = null;
        UUID descrElementUUID = null;
        if (descriptionIterator.hasNext()){
            TaxonDescription descr = descriptionIterator.next();
            descrUUID = descr.getUuid();
            descrElementUUID = descr.getElements().iterator().next().getUuid();
        }
        IBotanicalName taxonName = nameService.find(SPECIES1_NAME_UUID);
        assertNotNull(taxonName);

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
        config.setDeleteNameIfPossible(false);

        DeleteResult result = service.deleteTaxon(speciesTaxon.getUuid(), config, speciesTaxon.getTaxonNodes().iterator().next().getClassification().getUuid());
        if (!result.isOk()){
        	Assert.fail();
        }
        commitAndStartNewTransaction(null);

        taxonName = nameService.find(SPECIES1_NAME_UUID);
        Taxon taxon = (Taxon)service.find(SPECIES1_UUID);

        //descriptionService.find(descrUUID);
        assertNull(descriptionService.find(descrUUID));
        assertNull(descriptionElementDao.load(descrElementUUID));
        //assertNull(synName);
        assertNotNull(taxonName);
        assertNull(taxon);
        config.setDeleteNameIfPossible(true);
        Taxon newTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
        service.save(newTaxon);
        result = service.deleteTaxon(newTaxon.getUuid()
        		, config, null);
        if (!result.isOk()){
        	Assert.fail();
        }
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testDeleteTaxonWithAnnotations(){

        //create a small classification
        Taxon testTaxon = getTestTaxon();
        service.save(testTaxon).getUuid();

        Taxon speciesTaxon = (Taxon)service.find(SPECIES1_UUID);
        Iterator<TaxonDescription> descriptionIterator = speciesTaxon.getDescriptions().iterator();
        UUID descrUUID = null;
        UUID descrElementUUID = null;
        if (descriptionIterator.hasNext()){
            TaxonDescription descr = descriptionIterator.next();
            descrUUID = descr.getUuid();
            descrElementUUID = descr.getElements().iterator().next().getUuid();
        }
        IBotanicalName taxonName = nameService.find(SPECIES1_NAME_UUID);
        assertNotNull(taxonName);

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
        config.setDeleteNameIfPossible(false);
        Annotation annotation = Annotation.NewDefaultLanguageInstance("test");
        speciesTaxon.addAnnotation(annotation);


        DeleteResult result = service.deleteTaxon(speciesTaxon.getUuid(), config, speciesTaxon.getTaxonNodes().iterator().next().getClassification().getUuid());
        if (!result.isOk()){
            Assert.fail();
        }
        commitAndStartNewTransaction(null);

        taxonName = nameService.find(SPECIES1_NAME_UUID);
        Taxon taxon = (Taxon)service.find(SPECIES1_UUID);

        //descriptionService.find(descrUUID);
        assertNull(descriptionService.find(descrUUID));
        assertNull(descriptionElementDao.load(descrElementUUID));
        //assertNull(synName);
        assertNotNull(taxonName);
        assertNull(taxon);
        config.setDeleteNameIfPossible(true);
        Taxon newTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
        service.save(newTaxon);
        result = service.deleteTaxon(newTaxon.getUuid()
                , config, null);
        if (!result.isOk()){
            Assert.fail();
        }
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testDeleteTaxonUsedInTaxonRelation(){

        //create a small classification
        Taxon testTaxon = getTestTaxon();
        service.save(testTaxon).getUuid();

        Taxon speciesTaxon = (Taxon)service.find(SPECIES1_UUID);
        Taxon speciesTaxon2 = (Taxon)service.find(SPECIES2_UUID);
        speciesTaxon.addTaxonRelation(speciesTaxon2, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null);

        IBotanicalName taxonName = nameService.find(SPECIES1_NAME_UUID);
        assertNotNull(taxonName);

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
        config.setDeleteNameIfPossible(false);
        config.setDeleteTaxonRelationships(false);


        DeleteResult result = service.deleteTaxon(speciesTaxon.getUuid(), config, speciesTaxon.getTaxonNodes().iterator().next().getClassification().getUuid());
        if (result.isOk()){
            Assert.fail();
        }
        commitAndStartNewTransaction(null);

        taxonName = nameService.find(SPECIES1_NAME_UUID);
        Taxon taxon = (Taxon)service.find(SPECIES1_UUID);

        assertNotNull(taxonName);
        assertNotNull(taxon);

        config.setDeleteNameIfPossible(false);
        config.setDeleteTaxonRelationships(true);


        result = service.deleteTaxon(speciesTaxon.getUuid(), config, speciesTaxon.getTaxonNodes().iterator().next().getClassification().getUuid());
        if (!result.isOk()){
            Assert.fail();
        }
        commitAndStartNewTransaction(null);

        config.setDeleteNameIfPossible(true);
        Taxon newTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
        service.save(newTaxon);
        result = service.deleteTaxon(newTaxon.getUuid()
                , config, null);
        if (!result.isOk()){
            Assert.fail();
        }
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="../../database/ClearDBDataSet.xml")
    public final void testDeleteTaxonDeleteSynonymRelations(){

    	final String[]tableNames = {
                 "Classification", "Classification_AUD",
                 "TaxonBase","TaxonBase_AUD",
                 "TaxonNode","TaxonNode_AUD",
                 "TaxonName","TaxonName_AUD"};
    	commitAndStartNewTransaction(tableNames);

    	//create a small classification
        Taxon testTaxon = getTestTaxon();
        service.save(testTaxon).getUuid();
        Taxon speciesTaxon = (Taxon)service.find(SPECIES2_UUID);

        Synonym synonym = speciesTaxon.getSynonyms().iterator().next();
        UUID synonymUuid = synonym.getUuid();
        service.countSynonyms(true);

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
        config.setDeleteSynonymsIfPossible(false);


        DeleteResult result = service.deleteTaxon(speciesTaxon.getUuid(), config, speciesTaxon.getTaxonNodes().iterator().next().getClassification().getUuid());
        if (!result.isOk()){
        	Assert.fail();
        }
        commitAndStartNewTransaction(null);

        Taxon taxon = (Taxon)service.find(SPECIES2_UUID);
        assertNull("The deleted taxon should no longer exist", taxon);

        Synonym syn = (Synonym)service.find(synonymUuid);
        assertNotNull("The synonym should still exist since DeleteSynonymsIfPossible was false", service.find(synonymUuid));
        assertNull("The synonym should not be attached to an accepted taxon anymore", syn.getAcceptedTaxon());
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testDeleteTaxonNameUsedInOtherContext(){

        //create a small classification
        Taxon testTaxon = getTestTaxon();
        service.save(testTaxon).getUuid();
        Taxon speciesTaxon = (Taxon)service.find(SPECIES1_UUID);

        IBotanicalName taxonName = nameService.find(SPECIES1_NAME_UUID);
        assertNotNull(taxonName);
        TaxonName fromName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        taxonName.addRelationshipFromName(fromName, NameRelationshipType.VALIDATED_BY_NAME(), null, null);
        nameService.save(fromName);

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
        config.setDeleteNameIfPossible(true);
        DeleteResult result = service.deleteTaxon(speciesTaxon.getUuid(), config, speciesTaxon.getTaxonNodes().iterator().next().getClassification().getUuid());
        if (!result.isOk()){
        	Assert.fail();
        }
        commitAndStartNewTransaction(null);

        taxonName = nameService.find(SPECIES1_NAME_UUID);
        Taxon taxon = (Taxon)service.find(SPECIES1_UUID);
        //because of the namerelationship the name cannot be deleted
        assertNotNull(taxonName);
        assertNull(taxon);
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testDeleteTaxonNameUsedInTwoClassificationsDeleteAllNodes(){
        commitAndStartNewTransaction(null);
        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
        //create a small classification
        Taxon testTaxon = getTestTaxon();

        UUID uuid = service.save(testTaxon).getUuid();
        //BotanicalName name = nameService.find(uuid);
        Set<TaxonNode> nodes = testTaxon.getTaxonNodes();
        TaxonNode node = nodes.iterator().next();
        List<TaxonNode> childNodes = node.getChildNodes();
        TaxonNode childNode = childNodes.iterator().next();
        UUID childUUID = childNode.getTaxon().getUuid();
        Classification secondClassification = getTestClassification("secondClassification");

        secondClassification.addChildTaxon(testTaxon, null, null);
        //delete the taxon in all classifications
        config.setDeleteInAllClassifications(true);
        DeleteResult result = service.deleteTaxon(testTaxon.getUuid(), config, null);
        if (!result.isOk()){
            Assert.fail();
        }
        commitAndStartNewTransaction(null);
        Taxon tax = (Taxon)service.find(uuid);
        assertNull(tax);
        Taxon childTaxon = (Taxon)service.find(childUUID);
        assertNull(childTaxon);
        commitAndStartNewTransaction(null);
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testDeleteTaxonNameUsedInTwoClassificationsDoNotDeleteAllNodes(){

        // delete the taxon only in second classification, this should delete only the nodes, not the taxa
        Taxon testTaxon = getTestTaxon();
        UUID uuid = service.save(testTaxon).getUuid();
        Classification secondClassification = getTestClassification("secondClassification");
        Set<TaxonNode> nodes = testTaxon.getTaxonNodes();
        TaxonNode node = nodes.iterator().next();
        List<TaxonNode> childNodes = node.getChildNodes();
        TaxonNode childNode = childNodes.iterator().next();
        UUID childUUID = childNode.getTaxon().getUuid();
        childNode = secondClassification.addChildTaxon(testTaxon, null, null);
        UUID childNodeUUID = childNode.getUuid();

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator() ;
        config.setDeleteInAllClassifications(false);
       //     try {
       DeleteResult result = service.deleteTaxon(testTaxon.getUuid(), config, secondClassification.getUuid());
/*                Assert.fail("The taxon should not be deletable because it is used in a second classification and the configuration is set to deleteInAllClassifications = false");
            } catch (DataChangeNoRollbackException e) {
                logger.debug(e.getMessage());
            }
  */

       if (result.isOk()){
           	Assert.fail("The taxon should not be deletable because it is used in a second classification and the configuration is set to deleteInAllClassifications = false");
        }

        //commitAndStartNewTransaction(null);
        Taxon tax = (Taxon)service.find(uuid);
        assertNotNull(tax);
        Taxon childTaxon = (Taxon)service.find(childUUID);
        assertNotNull(tax);
        //when calling delete taxon and the taxon can not be deleted the children should not be deleted as well. If children should be deleted call delete taxonnode
        node = nodeService.find(childNodeUUID);
        assertNotNull(node);
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testTaxonNodeDeletionConfiguratorMoveToParent(){
        //test childHandling MOVE_TO_PARENT:
        Taxon testTaxon = getTestTaxon();
        UUID uuid = service.save(testTaxon).getUuid();

        Taxon topMost = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY()), null);

        Iterator<TaxonNode> nodes = testTaxon.getTaxonNodes().iterator();
        TaxonNode node =nodes.next();
        Classification classification = node.getClassification();
        classification.addParentChild(topMost, testTaxon, null, null);
        UUID topMostUUID = service.save(topMost).getUuid();

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator() ;
        config.getTaxonNodeConfig().setChildHandling(ChildHandling.MOVE_TO_PARENT);


        DeleteResult result = service.deleteTaxon(testTaxon.getUuid(), config, classification.getUuid());
        if(!result.isOk()){
         	Assert.fail();
       	}

        commitAndStartNewTransaction(null);
        Taxon tax = (Taxon)service.find(uuid);
        assertNull(tax);
        tax = (Taxon)service.find(topMostUUID);
        Set<TaxonNode> topMostNodes = tax.getTaxonNodes();
        assertNotNull(topMostNodes);
        assertEquals("there should be one taxon node", 1, topMostNodes.size());
        nodes = topMostNodes.iterator();
        TaxonNode topMostNode = nodes.next();
        int size = topMostNode.getChildNodes().size();

        assertEquals(2, size);
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testTaxonNodeDeletionConfiguratorDeleteChildren(){
        //test childHandling DELETE:
        Taxon testTaxon = getTestTaxon();
        UUID uuid = service.save(testTaxon).getUuid();

        Taxon topMost = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY()), null);

        Iterator<TaxonNode> nodes = testTaxon.getTaxonNodes().iterator();
        TaxonNode node =nodes.next();
        UUID taxonNodeUUID = node.getUuid();
        Classification classification = node.getClassification();
        classification.addParentChild(topMost, testTaxon, null, null);
        UUID topMostUUID = service.save(topMost).getUuid();

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator() ;
        config.getTaxonNodeConfig().setChildHandling(ChildHandling.DELETE);

       // try {
        DeleteResult result = service.deleteTaxon(testTaxon.getUuid(), config, testTaxon.getTaxonNodes().iterator().next().getClassification().getUuid());
        if(!result.isOk()){
         	Assert.fail();
       	}
        commitAndStartNewTransaction(null);
        Taxon tax = (Taxon)service.find(uuid);
        assertNull(tax);
        tax = (Taxon)service.find(topMostUUID);
        Set<TaxonNode> topMostNodes = tax.getTaxonNodes();
        assertNotNull(topMostNodes);
        assertEquals("there should be one taxon node", 1, topMostNodes.size());
        nodes = topMostNodes.iterator();
        TaxonNode topMostNode = nodes.next();
        int size = topMostNode.getChildNodes().size();
        node = nodeService.find(taxonNodeUUID);
        assertNull(node);
        assertEquals(0, size);
    }


    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testTaxonDeletionConfiguratorDeleteMarker(){

        //test childHandling DELETE:
        Taxon testTaxon = getTestTaxon();
        UUID uuid = service.save(testTaxon).getUuid();

        Taxon topMost = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY()), null);

        Iterator<TaxonNode> nodes = testTaxon.getTaxonNodes().iterator();
        TaxonNode node =nodes.next();
        Classification classification = node.getClassification();
        classification.addParentChild(topMost, testTaxon, null, null);
        UUID topMostUUID = service.save(topMost).getUuid();
        Marker marker = Marker.NewInstance(testTaxon, true, MarkerType.IS_DOUBTFUL());
        testTaxon.addMarker(marker);
        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator() ;
        config.getTaxonNodeConfig().setChildHandling(ChildHandling.DELETE);

        DeleteResult result = service.deleteTaxon(testTaxon.getUuid(), config, node.getClassification().getUuid());

        if(!result.isOk()){
         	Assert.fail();
       	}
        commitAndStartNewTransaction(null);
        Taxon tax = (Taxon)service.find(uuid);
        assertNull(tax);
        tax = (Taxon)service.find(topMostUUID);
        Set<TaxonNode> topMostNodes = tax.getTaxonNodes();
        assertNotNull(topMostNodes);
        assertEquals("there should be one taxon node", 1, topMostNodes.size());
        nodes = topMostNodes.iterator();
        TaxonNode topMostNode = nodes.next();
        int size = topMostNode.getChildNodes().size();

        assertEquals(0, size);
    }


    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testTaxonDeletionConfiguratorTaxonWithMisappliedName(){

        Taxon testTaxon = getTestTaxon();
        UUID uuid = service.save(testTaxon).getUuid();

        Taxon misappliedName = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null);

        Iterator<TaxonNode> nodes = testTaxon.getTaxonNodes().iterator();
        TaxonNode node =nodes.next();
        testTaxon.addMisappliedName(misappliedName, null, null);
        UUID misappliedNameUUID = service.save(misappliedName).getUuid();

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator() ;
        config.setDeleteMisappliedNames(true);

        DeleteResult result  = service.deleteTaxon(testTaxon.getUuid(), config, node.getClassification().getUuid());
        if(!result.isOk()){
         	Assert.fail();
       	}
        commitAndStartNewTransaction(null);
        Taxon tax = (Taxon)service.find(uuid);
        assertNull(tax);
        tax = (Taxon)service.find(misappliedNameUUID);
        //TODO: is that correct or should it be deleted because there is no relation to anything
        assertNull(tax);

    }
    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testTaxonDeletionConfiguratorTaxonWithMisappliedNameDoNotDelete(){

        Taxon testTaxon = getTestTaxon();
        UUID uuid = service.save(testTaxon).getUuid();

        Taxon misappliedName = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null);

        Iterator<TaxonNode> nodes = testTaxon.getTaxonNodes().iterator();
        TaxonNode node =nodes.next();
        testTaxon.addMisappliedName(misappliedName, null, null);
        UUID misappliedNameUUID = service.save(misappliedName).getUuid();

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator() ;
        config.setDeleteMisappliedNames(false);

        DeleteResult result = service.deleteTaxon(testTaxon.getUuid(), config, node.getClassification().getUuid());
        if(!result.isOk()){
         	Assert.fail();
       	}
        commitAndStartNewTransaction(null);
        Taxon tax = (Taxon)service.find(uuid);
        assertNull(tax);
        tax = (Taxon)service.find(misappliedNameUUID);
        //TODO: is that correct or should it be deleted because there is no relation to anything
        assertNotNull(tax);
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testTaxonDeletionConfiguratorTaxonMisappliedName(){

        Taxon testTaxon = getTestTaxon();
        UUID uuid = service.save(testTaxon).getUuid();

        Taxon misappliedNameTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null);

        Iterator<TaxonNode> nodes = testTaxon.getTaxonNodes().iterator();
        TaxonNode node =nodes.next();
        testTaxon.addMisappliedName(misappliedNameTaxon, null, null);
        UUID misappliedNameUUID = service.save(misappliedNameTaxon).getUuid();
        misappliedNameTaxon = (Taxon)service.find(misappliedNameUUID);
        UUID misNameUUID = misappliedNameTaxon.getName().getUuid();

        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator() ;

        service.deleteTaxon(misappliedNameTaxon.getUuid(), config,null);

        commitAndStartNewTransaction(null);
        Taxon tax = (Taxon)service.find(uuid);
        assertNotNull(tax);
        tax = (Taxon)service.find(misappliedNameUUID);
        IBotanicalName name = nameService.find(misNameUUID);

        assertNull(tax);
        assertNull(name);
    }

    @Test
    @DataSet(value="../../database/ClearDBDataSet.xml")
    public final void testListIncludedTaxa(){
    	Reference citation = null;
    	String microcitation = null;

    	//Data
    	Classification cl1 = Classification.NewInstance("testClassification1");
    	Classification cl2 = Classification.NewInstance("testClassification2");
    	Classification cl3 = Classification.NewInstance("testClassification3");

    	classificationService.save(cl1);
        classificationService.save(cl2);
        classificationService.save(cl3);

    	Taxon c1Genus = Taxon.NewInstance(null, null);c1Genus.setUuid(UUID.fromString("daa24f6f-7e38-4668-b385-10c789212e4e"));
    	Taxon c1Species = Taxon.NewInstance(null, null);c1Species.setUuid(UUID.fromString("1c1d0566-67d0-4806-bf23-ecf55f4b9118"));
    	Taxon c1SubSpecies1 = Taxon.NewInstance(null, null);c1SubSpecies1.setUuid(UUID.fromString("96ae2fad-76df-429f-b179-42e00838fea4"));
    	Taxon c1SubSpecies2 = Taxon.NewInstance(null, null);c1SubSpecies2.setUuid(UUID.fromString("5d3f6147-ca72-40e0-be8a-6c835a09a579"));
    	TaxonNode c1childNodeSpecies1 = cl1.addParentChild(c1Genus, c1Species, null, null);
    	nodeService.saveOrUpdate(c1childNodeSpecies1.getParent());
    	nodeService.saveOrUpdate(c1childNodeSpecies1);
    	TaxonNode c1childNodeSubSpecies1 =cl1.addParentChild(c1Species, c1SubSpecies1, null, null);
    	nodeService.saveOrUpdate(c1childNodeSubSpecies1);
    	TaxonNode c1childNodeSubSpecies2 =cl1.addParentChild(c1Species, c1SubSpecies2, null, null);
    	nodeService.saveOrUpdate(c1childNodeSubSpecies2);

    	Taxon c2Genus = Taxon.NewInstance(null, null);c2Genus.setUuid(UUID.fromString("ed0ec006-3ac8-4a12-ae13-fdf2a13dedbe"));
    	Taxon c2Species = Taxon.NewInstance(null, null);c2Species.setUuid(UUID.fromString("1027eb18-1c26-450e-a299-981b775ebc3c"));
    	Taxon c2SubSpecies1 = Taxon.NewInstance(null, null);c2SubSpecies1.setUuid(UUID.fromString("61f039c8-01f3-4f5d-8e16-1602139774e7"));
    	Taxon c2SubSpecies2 = Taxon.NewInstance(null, null);c2SubSpecies2.setUuid(UUID.fromString("2ed6b6f8-05f9-459a-a075-2bca57e3013e"));
    	TaxonNode c2childNodeSpecies1 = cl2.addParentChild(c2Genus, c2Species, null, null);
    	nodeService.saveOrUpdate(c2childNodeSpecies1.getParent());
        nodeService.saveOrUpdate(c2childNodeSpecies1);
    	TaxonNode c2childNodeSubSpecies1 = cl2.addParentChild(c2Species, c2SubSpecies1, null, null);
    	nodeService.saveOrUpdate(c2childNodeSubSpecies1);
    	TaxonNode c2childNodeSubSpecies2 = cl2.addParentChild(c2Species, c2SubSpecies2, null, null);
    	nodeService.saveOrUpdate(c2childNodeSubSpecies2);

    	Taxon c3Genus = Taxon.NewInstance(null, null);c3Genus.setUuid(UUID.fromString("407dfc8d-7a4f-4370-ada4-76c1a8279d1f"));
    	Taxon c3Species = Taxon.NewInstance(null, null);c3Species.setUuid(UUID.fromString("b6d34fc7-4aa7-41e5-b633-86f474edbbd5"));
    	Taxon c3SubSpecies1 = Taxon.NewInstance(null, null);c3SubSpecies1.setUuid(UUID.fromString("01c07585-a422-40cd-9339-a74c56901d9f"));
    	Taxon c3SubSpecies2 = Taxon.NewInstance(null, null);c3SubSpecies2.setUuid(UUID.fromString("390c8e23-e05f-4f89-b417-50cf080f4c91"));
    	TaxonNode c3childNodeSpecies1 = cl3.addParentChild(c3Genus, c3Species, null, null);
    	nodeService.saveOrUpdate(c3childNodeSpecies1.getParent());
        nodeService.saveOrUpdate(c3childNodeSpecies1);
    	TaxonNode c3childNodeSubSpecies1 = cl3.addParentChild(c3Species, c3SubSpecies1, null, null);
    	nodeService.saveOrUpdate(c3childNodeSubSpecies1);
    	TaxonNode c3childNodeSubSpecies2 = cl3.addParentChild(c3Species, c3SubSpecies2, null, null);
    	nodeService.saveOrUpdate(c3childNodeSubSpecies2);

      	Taxon c4Genus = Taxon.NewInstance(null, null);c4Genus.setUuid(UUID.fromString("bfd6bbdd-0116-4ab2-a781-9316224aad78"));
    	Taxon c4Species = Taxon.NewInstance(null, null);c4Species.setUuid(UUID.fromString("9347a3d9-5ece-4d64-9035-e8aaf5d3ee02"));
    	Taxon c4SubSpecies = Taxon.NewInstance(null, null);c4SubSpecies.setUuid(UUID.fromString("777aabbe-4c3a-449c-ab99-a91f2fec9f07"));

    	TaxonRelationship rel = c1Species.addTaxonRelation(c2Species, TaxonRelationshipType.CONGRUENT_TO(), citation, microcitation);
    	rel.setDoubtful(true);
    	c1Species.addTaxonRelation(c4Species, TaxonRelationshipType.INCLUDES(), citation, microcitation);
    	c2Species.addTaxonRelation(c1SubSpecies2, TaxonRelationshipType.INCLUDES(), citation, microcitation);

    	service.saveOrUpdate(c1Species);
       	service.saveOrUpdate(c2Species);
       	service.save(c4Species);
       	commitAndStartNewTransaction();

    	//Tests
       	//default starting at species 1
       	IncludedTaxaDTO dto = service.listIncludedTaxa(c1Species.getUuid(), new IncludedTaxonConfiguration(null, true, false));
    	Assert.assertNotNull("IncludedTaxaDTO", dto);
    	Assert.assertEquals("Result should contain 7 taxa: c1Species", 7, dto.getIncludedTaxa().size());
    	Assert.assertNotNull("date should not be null", dto.getDate());
//    	Assert.assertTrue(dto.contains(taxonUuid));
        //same without doubtful
    	dto = service.listIncludedTaxa(c1Species.getUuid(), new IncludedTaxonConfiguration(null, false, false));
    	Assert.assertEquals(4, dto.getIncludedTaxa().size());

    	//other example starting at Genus2
    	dto = service.listIncludedTaxa(c2Genus.getUuid(), new IncludedTaxonConfiguration(null, true, false));
    	Assert.assertEquals(8, dto.getIncludedTaxa().size());
    	//same without doubtful
    	dto = service.listIncludedTaxa(c2Genus.getUuid(), new IncludedTaxonConfiguration(null, false, false));
    	Assert.assertEquals(5, dto.getIncludedTaxa().size());

    	//only congruent
    	dto = service.listIncludedTaxa(c1Species.getUuid(), new IncludedTaxonConfiguration(null, true, true));
    	Assert.assertEquals(2, dto.getIncludedTaxa().size());
    	//same without doubtful
    	dto = service.listIncludedTaxa(c1Species.getUuid(), new IncludedTaxonConfiguration(null, false, true));
    	Assert.assertEquals(1, dto.getIncludedTaxa().size());
    }

    @Test
    public void testDeleteDescriptions(){
    	try {
			createTestDataSet();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	TaxonDescription description = save(TaxonDescription.NewInstance(taxWithoutSyn));
    	SpecimenOrObservationBase<IIdentifiableEntityCacheStrategy<FieldUnit>> specimen = FieldUnit.NewInstance();
    	UUID uuid = occurenceService.saveOrUpdate(specimen);
    	DescriptionElementBase element = IndividualsAssociation.NewInstance(specimen);
    	description.addElement(element);
    	service.saveOrUpdate(taxWithoutSyn);


    	Taxon tax = (Taxon)service.find(uuidTaxWithoutSyn);
    	Set<TaxonDescription> descr =  tax.getDescriptions();
    	assertEquals(1, descr.size());
    	description = descr.iterator().next();
    	UUID uuidDescr = description.getUuid();
    	UUID uuidDescEl = description.getElements().iterator().next().getUuid();

    	descriptionService.deleteDescription(description);
    	service.saveOrUpdate(tax);

    	description = (TaxonDescription) descriptionService.find(uuidDescr);
    	specimen = occurenceService.find(uuid);
    	assertNull(description);
    	DeleteResult result = occurenceService.delete(specimen);
    	assertTrue(result.isOk());

    }

    @Test
    public void testRemoveDescriptionsFromTaxa(){
        try {
            createTestDataSet();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Could not create test data");
        }
        TaxonDescription description = TaxonDescription.NewInstance(taxWithoutSyn);
        SpecimenOrObservationBase<IIdentifiableEntityCacheStrategy<FieldUnit>> specimen = FieldUnit.NewInstance();
        UUID uuid = occurenceService.saveOrUpdate(specimen);
        DescriptionElementBase element = IndividualsAssociation.NewInstance(specimen);
        description.addElement(element);
        descriptionDao.save(description);
        service.saveOrUpdate(taxWithoutSyn);

        Taxon tax = (Taxon)service.find(uuidTaxWithoutSyn);
        Set<TaxonDescription> descriptions =  tax.getDescriptions();
        assertEquals(1, descriptions.size());
        description = descriptions.iterator().next();
        UUID uuidDescr = description.getUuid();

        tax.removeDescription(description, true);
        service.saveOrUpdate(tax);

        description = (TaxonDescription) descriptionService.find(uuidDescr);
        specimen = occurenceService.find(uuid);
        assertNotNull(description);
        DeleteResult result = occurenceService.delete(specimen);
        assertTrue(result.isOk());
    }

    @Override
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="ClearDBDataSet.xml")
    public void createTestDataSet() throws FileNotFoundException {

        Rank rank = Rank.SPECIES();
        taxWithoutSyn = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test1", null, null, null, null, null, null, null), null);
        taxWithSyn = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test3", null, null, null, null, null, null, null), null);
        tax2WithSyn = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test5", null, null, null, null, null, null, null), null);
        synonym = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        synonym2 = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(rank, "Test4", null, null, null, null, null, null, null), null);
        synonym2.getName().setHomotypicalGroup(synonym.getHomotypicGroup());

        taxWithSyn.addSynonym(synonym, SynonymType.HETEROTYPIC_SYNONYM_OF);
        taxWithSyn.addSynonym(synonym2, SynonymType.HETEROTYPIC_SYNONYM_OF);

        uuidTaxWithoutSyn = service.save(taxWithoutSyn).getUuid();
        uuidSyn = service.save(synonym).getUuid();
        uuidSyn2 = service.save(synonym2).getUuid();
        uuidTaxWithSyn =service.save(taxWithSyn).getUuid();
    }

//    public static UUID DESCRIPTION1_UUID = UUID.fromString("f3e061f6-c5df-465c-a253-1e18ab4c7e50");
//    public static UUID DESCRIPTION2_UUID = UUID.fromString("1b009a40-ebff-4f7e-9f7f-75a850ba995d");

    public Taxon getTestTaxon(){

        int descrIndex = 6000;

        Person deCandolle = Person.NewInstance();
        deCandolle.setTitleCache("DC.", true);
        agentService.save(deCandolle);

        Reference sec = save(ReferenceFactory.newDatabase());
        sec.setTitleCache("Flora lunaea", true);
        Reference citationRef = save(ReferenceFactory.newBook());
        citationRef.setTitleCache("Sp. lunarum", true);

        //genus taxon with Name, combinationAuthor,
        IBotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        botName.setTitleCache("Hieracium L.", true);
        botName.setGenusOrUninomial("Hieracium");
        botName.setCombinationAuthorship(createPerson());
        botName.getCombinationAuthorship().setNomenclaturalTitleCache("L.", true);
        botName.setUuid(GENUS_NAME_UUID);
        Taxon genusTaxon = Taxon.NewInstance(botName, sec);
        genusTaxon.setUuid(GENUS_UUID);
        service.save(genusTaxon);
        //a name that is the basionym of genusTaxon's name
        TaxonName basionym = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        basionym.setTitleCache("Hieracilla DC.", true);
        basionym.setGenusOrUninomial("Hieracilla");
        basionym.setCombinationAuthorship(deCandolle);
        basionym.setUuid(BASIONYM_UUID);
        botName.addBasionym(basionym, null, null,"216", null);
        nameService.saveOrUpdate(basionym);
        //species taxon that is the child of genus taxon
        IBotanicalName botSpecies = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botSpecies.setTitleCache("Hieracium asturianum Pau", true);
        botSpecies.setGenusOrUninomial("Hieracium");
        botSpecies.setSpecificEpithet("asturianum");
        botSpecies.setCombinationAuthorship(createPerson());
        botSpecies.getCombinationAuthorship().setNomenclaturalTitleCache("Pau", true);
        botSpecies.setUuid(SPECIES1_NAME_UUID);
        Taxon childTaxon = Taxon.NewInstance(botSpecies, sec);
        childTaxon.setUuid(SPECIES1_UUID);
        TaxonDescription taxDesc = getTestDescription(descrIndex++);
        //taxDesc.setUuid(DESCRIPTION1_UUID);
        childTaxon.addDescription(taxDesc);
        service.saveOrUpdate(childTaxon);
        Classification classification = getTestClassification("TestClassification");
        TaxonNode child = classification.addParentChild(genusTaxon, childTaxon, citationRef, "456");
//            childTaxon.setTaxonomicParent(genusTaxon, citationRef, "456");
        classificationService.save(classification);
//        taxonNodeDao.saveOrUpdate(child);
        //homotypic synonym of childTaxon1
        IBotanicalName botSpecies4= TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botSpecies4.setTitleCache("Hieracium gueri DC.", true);
        botSpecies4.setGenusOrUninomial("Hieracium");
        botSpecies4.setSpecificEpithet("gueri");
        botSpecies4.setCombinationAuthorship(deCandolle);
        botSpecies4.setUuid(SYNONYM_NAME_UUID);
        Synonym homoSynonym = save(Synonym.NewInstance(botSpecies4, sec));

        childTaxon.addSynonym(homoSynonym, SynonymType.HOMOTYPIC_SYNONYM_OF);
        service.saveOrUpdate(childTaxon);

        //2nd child species taxon that is the child of genus taxon
        IBotanicalName botSpecies2= TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botSpecies2.setTitleCache("Hieracium wolffii Zahn", true);
        botSpecies2.setGenusOrUninomial("Hieracium");
        botSpecies2.setSpecificEpithet("wolffii");
        botSpecies2.setCombinationAuthorship(createPerson());
        botSpecies2.getCombinationAuthorship().setNomenclaturalTitleCache("Zahn", true);
        botSpecies2.setUuid(SPECIES2_NAME_UUID);
        Taxon childTaxon2 = Taxon.NewInstance(botSpecies2, sec);
        childTaxon2.setUuid(SPECIES2_UUID);
        classification.addParentChild(genusTaxon, childTaxon2, citationRef, "499");
        //childTaxon2.setTaxonomicParent(genusTaxon, citationRef, "499");
        service.saveOrUpdate(childTaxon2);
        //heterotypic synonym of childTaxon2
        IBotanicalName botSpecies3= TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botSpecies3.setTitleCache("Hieracium lupium DC.", true);
        botSpecies3.setGenusOrUninomial("Hieracium");
        botSpecies3.setSpecificEpithet("lupium");
        botSpecies3.setCombinationAuthorship(deCandolle);
        botSpecies3.setUuid(SYNONYM2_NAME_UUID);
        Synonym heteroSynonym = Synonym.NewInstance(botSpecies3, sec);
        heteroSynonym.setUuid(SYNONYM2_UUID);
        save(heteroSynonym);
        childTaxon2.addSynonym(heteroSynonym, SynonymType.HETEROTYPIC_SYNONYM_OF);
        service.saveOrUpdate(childTaxon2);
        //missaplied Name for childTaxon2
        IBotanicalName missName= TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        missName.setTitleCache("Hieracium lupium DC.", true);
        missName.setGenusOrUninomial("Hieracium");
        missName.setSpecificEpithet("lupium");
        missName.setCombinationAuthorship(deCandolle);
        missName.setUuid(SPECIES5_NAME_UUID);
        Taxon misappliedNameTaxon = Taxon.NewInstance(missName, sec);
        childTaxon2.addMisappliedName(misappliedNameTaxon, citationRef, "125");
        taxDesc = getTestDescription(descrIndex++);
       // taxDesc.setUuid(DESCRIPTION2_UUID);
        genusTaxon.addDescription(taxDesc);
        service.saveOrUpdate(genusTaxon);
        service.save(misappliedNameTaxon);

        return genusTaxon;
    }

    private Person createPerson() {
        Person person = Person.NewInstance();
        agentService.save(person);
        return person;
    }

    public TaxonDescription getTestDescription(int index){
        TaxonDescription taxonDescription = TaxonDescription.NewInstance();
        Language language = Language.DEFAULT();
        //taxonDescription.setId(index);

        //textData
        TextData textData = TextData.NewInstance();
        String descriptionText = "this is a desciption for a taxon";
        LanguageString languageString = LanguageString.NewInstance(descriptionText, language);
        textData.putText(languageString);
        taxonDescription.addElement(textData);

        //commonName

        String commonNameString = "Schönveilchen";
        CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameString, language);
        taxonDescription.addElement(commonName);

        return taxonDescription;
    }

    public Classification getTestClassification(String name){
        Classification classification = Classification.NewInstance(name);
        classificationService.save(classification);
        return classification;
    }
}
