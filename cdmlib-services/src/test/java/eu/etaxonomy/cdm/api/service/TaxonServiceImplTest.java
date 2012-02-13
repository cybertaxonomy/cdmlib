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
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class TaxonServiceImplTest extends CdmTransactionalIntegrationTest {
    private static final Logger logger = Logger.getLogger(TaxonServiceImplTest.class);

    @SpringBeanByType
    private ITaxonService service;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private IReferenceService referenceService;

/****************** TESTS *****************************/

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getTaxonByUuid(java.util.UUID)}.
     */
    @Test
    public final void testGetTaxonByUuid() {
        Taxon expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        UUID uuid = service.save(expectedTaxon);
        TaxonBase<?> actualTaxon = service.find(uuid);
        assertEquals(expectedTaxon, actualTaxon);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#saveTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
     */
    @Test
    public final void testSaveTaxon() {
        Taxon expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        UUID uuid = service.save(expectedTaxon);
        TaxonBase<?> actualTaxon = service.find(uuid);
        assertEquals(expectedTaxon, actualTaxon);
    }

    @Test
    public final void testSaveOrUpdateTaxon() {
        Taxon expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        UUID uuid = service.save(expectedTaxon);
        TaxonBase<?> actualTaxon = service.find(uuid);
        assertEquals(expectedTaxon, actualTaxon);

        actualTaxon.setName(BotanicalName.NewInstance(Rank.SPECIES()));
        try{
            service.saveOrUpdate(actualTaxon);
        }catch(Exception e){
            Assert.fail();
        }
    }
    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#removeTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
     */
    @Test
    public final void testRemoveTaxon() {
    	Rank rank = Rank.SPECIES();
        Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(rank), null);
        UUID uuid = service.save(taxon);
        service.delete(taxon);
        TaxonBase<?> actualTaxon = service.find(uuid);
        assertNull(actualTaxon);
    }


    @Test
    public final void testPrintDataSet() {
        //printDataSet(System.out);
    }

    @Test
    public final void testMakeTaxonSynonym() {
        Rank rank = Rank.SPECIES();
        Taxon tax1 = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test1", null, null, null, null, null, null, null), null);
        Synonym synonym = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        tax1.addHomotypicSynonym(synonym, null, null);
        UUID uuidTaxon = service.save(tax1);
        UUID uuidSyn = service.save(synonym);

        service.swapSynonymAndAcceptedTaxon(synonym, tax1);

        TaxonBase<?> tax = service.find(uuidTaxon);
        TaxonBase<?> syn = service.find(uuidSyn);
        HomotypicalGroup groupTest = tax.getHomotypicGroup();
        HomotypicalGroup groupTest2 = syn.getHomotypicGroup();
        assertEquals(groupTest, groupTest2);
    }

    @Test
    public final void testChangeSynonymToAcceptedTaxon(){
        Rank rank = Rank.SPECIES();
        //HomotypicalGroup group = HomotypicalGroup.NewInstance();
        Taxon taxWithoutSyn = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test1", null, null, null, null, null, null, null), null);
        Taxon taxWithSyn = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test3", null, null, null, null, null, null, null), null);
        Synonym synonym = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym2 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test4", null, null, null, null, null, null, null), null);
        synonym2.getName().setHomotypicalGroup(synonym.getHomotypicGroup());
        //tax2.addHeterotypicSynonymName(synonym.getName());
        taxWithSyn.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
        taxWithSyn.addSynonym(synonym2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

        service.save(taxWithoutSyn);
        UUID uuidSyn = service.save(synonym);
        service.save(synonym2);
        service.save(taxWithSyn);

        Taxon taxon = null;
        try {
            taxon = service.changeSynonymToAcceptedTaxon(synonym, taxWithSyn, true, true, null, null);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Invocation of change method should not throw an exception");
        }
        //test flush (resave deleted object)
        TaxonBase<?> syn = service.find(uuidSyn);
        assertNull(syn);
        Assert.assertEquals("New taxon should have 1 synonym relationship (the old homotypic synonym)", 1, taxon.getSynonymRelations().size());
    }


    /**
     * Old implementation taken from {@link TaxonServiceImplBusinessTest} for old version of method.
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#moveSynonymToAnotherTaxon(eu.etaxonomy.cdm.model.taxon.SynonymRelationship, eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
     */
    @Test
    public final void testMoveSynonymToAnotherTaxon_OLD() {
        SynonymRelationshipType heteroTypicSynonymRelationshipType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
        Reference<?> reference = ReferenceFactory.newGeneric();
        String referenceDetail = "test";

        NonViralName<?> t1n = NonViralName.NewInstance(null);
        Taxon t1 = Taxon.NewInstance(t1n, reference);
        NonViralName<?> t2n = NonViralName.NewInstance(null);
        Taxon t2 = Taxon.NewInstance(t2n, reference);
        NonViralName<?> s1n = NonViralName.NewInstance(null);
        Synonym s1 = Synonym.NewInstance(s1n, reference);
        t1.addSynonym(s1, heteroTypicSynonymRelationshipType);

        SynonymRelationship synonymRelation = t1.getSynonymRelations().iterator().next();

        boolean keepReference = false;
        boolean moveHomotypicGroup = false;
        try {
            service.moveSynonymToAnotherTaxon(synonymRelation, t2, moveHomotypicGroup, heteroTypicSynonymRelationshipType, reference, referenceDetail, keepReference);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Method call should not throw exception");
        }

        Assert.assertTrue("t1 should have no synonym relationships", t1.getSynonymRelations().isEmpty());

        Set<SynonymRelationship> synonymRelations = t2.getSynonymRelations();
        Assert.assertTrue("t2 should have exactly one synonym relationship", synonymRelations.size() == 1);

        synonymRelation = synonymRelations.iterator().next();

        Assert.assertEquals(t2, synonymRelation.getAcceptedTaxon());
        Assert.assertEquals(heteroTypicSynonymRelationshipType, synonymRelation.getType());
        Assert.assertEquals(reference, synonymRelation.getCitation());
        Assert.assertEquals(referenceDetail, synonymRelation.getCitationMicroReference());
    }

    @Test
    @DataSet("TaxonServiceImplTest.testMoveSynonymToAnotherTaxon.xml")
    public final void testMoveSynonymToAnotherTaxon() {
        final String[] tableNames = new String[]{"SynonymRelationship"};

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
        SynonymRelationshipType newSynonymRelationshipType = null;
        boolean keepReference = true;
        Reference<?> newReference = null;
        String newReferenceDetail = null;

        Taxon newTaxon = (Taxon)service.load(uuidNewTaxon);
        Synonym homotypicSynonym = (Synonym)service.load(uuidSyn1);
        Assert.assertNotNull("Synonym should exist", homotypicSynonym);
        Assert.assertEquals("Synonym should have 1 relation", 1, homotypicSynonym.getSynonymRelations().size());
        SynonymRelationship rel = homotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, rel.getAcceptedTaxon().getUuid());
        Taxon oldTaxon = rel.getAcceptedTaxon();

        try {
            service.moveSynonymToAnotherTaxon(rel, newTaxon, moveHomotypicGroup, newSynonymRelationshipType, newReference, newReferenceDetail, keepReference);
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
        Assert.assertEquals("Synonym should still have 1 relation", 1, homotypicSynonym.getSynonymRelations().size());
        rel = homotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", oldTaxon, rel.getAcceptedTaxon());

        //test heterotypic synonym with other synonym in homotypic group
        newTaxon = (Taxon)service.load(uuidNewTaxon);
        Synonym heterotypicSynonym = (Synonym)service.load(uuidSyn3);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym);
        Assert.assertEquals("Synonym should have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, rel.getAcceptedTaxon().getUuid());
        oldTaxon = rel.getAcceptedTaxon();
        moveHomotypicGroup = false;

        try {
            service.moveSynonymToAnotherTaxon(rel, newTaxon, moveHomotypicGroup, newSynonymRelationshipType, newReference, newReferenceDetail, keepReference);
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
        Assert.assertEquals("Synonym should still have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of single relation should still be the old taxon", oldTaxon, rel.getAcceptedTaxon());


        //test heterotypic synonym with no other synonym in homotypic group
        //+ keep reference
        newTaxon = (Taxon)service.load(uuidNewTaxon);
        heterotypicSynonym = (Synonym)service.load(uuidSyn5);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym);
        Assert.assertEquals("Synonym should have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, rel.getAcceptedTaxon().getUuid());
        oldTaxon = rel.getAcceptedTaxon();
        moveHomotypicGroup = false;


        try {
            service.moveSynonymToAnotherTaxon(rel, newTaxon, moveHomotypicGroup, newSynonymRelationshipType, newReference, newReferenceDetail, keepReference);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Move of single heterotypic synonym should not throw exception: " + e.getMessage());
        }
        //Asserts
        commitAndStartNewTransaction(tableNames);
        heterotypicSynonym = (Synonym)service.load(uuidSyn5);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertEquals("Synonym should still have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of single relation should be new taxon", newTaxon, rel.getAcceptedTaxon());
        Assert.assertEquals("Old detail should be kept", "rel5", rel.getCitationMicroReference());


        //test heterotypic synonym with other synonym in homotypic group and moveHomotypicGroup="true"
        //+ new detail
        newTaxon = (Taxon)service.load(uuidNewTaxon);
        heterotypicSynonym = (Synonym)service.load(uuidSyn3);
        Reference<?> ref1 = referenceService.load(uuidRef1);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym);
        Assert.assertEquals("Synonym should have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, rel.getAcceptedTaxon().getUuid());
        oldTaxon = rel.getAcceptedTaxon();
        Assert.assertEquals("Detail should be ref1", ref1, rel.getCitation());
        Assert.assertEquals("Detail should be 'rel3'", "rel3", rel.getCitationMicroReference());
        TaxonNameBase<?,?> oldSynName3 = heterotypicSynonym.getName();

        Synonym heterotypicSynonym4 = (Synonym)service.load(uuidSyn4);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym4);
        Assert.assertEquals("Synonym should have 1 relation", 1, heterotypicSynonym4.getSynonymRelations().size());
        SynonymRelationship rel4 = heterotypicSynonym4.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of other synonym in group should be the old taxon", uuidOldTaxon, rel4.getAcceptedTaxon().getUuid());
        Assert.assertSame("Homotypic group of both synonyms should be same", oldSynName3.getHomotypicalGroup() , heterotypicSynonym4.getName().getHomotypicalGroup() );

        moveHomotypicGroup = true;
        keepReference = false;

        try {
            service.moveSynonymToAnotherTaxon(rel, newTaxon, moveHomotypicGroup, newSynonymRelationshipType, newReference, newReferenceDetail, keepReference);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Move with 'moveHomotypicGroup = true' should not throw exception: " + e.getMessage());
        }
        //Asserts
        commitAndStartNewTransaction(tableNames);
        heterotypicSynonym = (Synonym)service.load(uuidSyn3);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertEquals("Synonym should still have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of relation should be new taxon now", newTaxon, rel.getAcceptedTaxon());
        TaxonNameBase<?,?> synName3 = rel.getSynonym().getName();

        heterotypicSynonym = (Synonym)service.load(uuidSyn4);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertEquals("Synonym should still have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of relation should be new taxon now", newTaxon, rel.getAcceptedTaxon());
        Assert.assertNull("Old citation should be removed", rel.getCitation());
        Assert.assertNull("Old detail should be removed", rel.getCitationMicroReference());
        TaxonNameBase<?,?> synName4 = rel.getSynonym().getName();
        Assert.assertEquals("Homotypic group of both synonyms should be equal", synName3.getHomotypicalGroup() , synName4.getHomotypicalGroup() );
        Assert.assertSame("Homotypic group of both synonyms should be same", synName3.getHomotypicalGroup() , synName4.getHomotypicalGroup() );
        Assert.assertEquals("Homotypic group of both synonyms should be equal to old homotypic group", oldSynName3.getHomotypicalGroup() , synName3.getHomotypicalGroup() );


        //test single heterotypic synonym to homotypic synonym of new taxon
        //+ new reference
        newTaxon = (Taxon)service.load(uuidNewTaxon);
        Reference<?> ref2 = (Reference<?>)referenceService.load(uuidRef2);
        heterotypicSynonym = (Synonym)service.load(uuidSyn6);
        Assert.assertNotNull("Synonym should exist", heterotypicSynonym);
        Assert.assertEquals("Synonym should have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Accepted taxon of single relation should be the old taxon", uuidOldTaxon, rel.getAcceptedTaxon().getUuid());
        oldTaxon = rel.getAcceptedTaxon();
        moveHomotypicGroup = false;
        keepReference = false;
        newReference = ref2;
        newReferenceDetail = "newRefDetail";
        newSynonymRelationshipType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();

        try {
            service.moveSynonymToAnotherTaxon(rel, newTaxon, moveHomotypicGroup, newSynonymRelationshipType, newReference, newReferenceDetail, keepReference);
        } catch (HomotypicalGroupChangeException e) {
            Assert.fail("Move of single heterotypic synonym should not throw exception: " + e.getMessage());
        }
        //Asserts
        commitAndStartNewTransaction(tableNames);
        heterotypicSynonym = (Synonym)service.load(uuidSyn6);
        Assert.assertNotNull("Synonym should still exist", heterotypicSynonym);
        Assert.assertEquals("Synonym should still have 1 relation", 1, heterotypicSynonym.getSynonymRelations().size());
        rel = heterotypicSynonym.getSynonymRelations().iterator().next();
        Assert.assertEquals("Relationship type should be 'homotypic synonym'", newSynonymRelationshipType, rel.getType());
        Assert.assertEquals("Accepted taxon of single relation should be new taxon", newTaxon, rel.getAcceptedTaxon());
        Assert.assertEquals("New citation should be ref2", ref2 ,rel.getCitation());
        Assert.assertEquals("New detail should be kept", "newRefDetail", rel.getCitationMicroReference());

        Assert.assertEquals("New taxon and new synonym should have equal homotypical group", rel.getSynonym().getHomotypicGroup(), rel.getAcceptedTaxon().getHomotypicGroup());
        Assert.assertSame("New taxon and new synonym should have same homotypical group", rel.getSynonym().getHomotypicGroup(), rel.getAcceptedTaxon().getHomotypicGroup());
    }



    @Test
    public final void testGetHeterotypicSynonymyGroups(){
        Rank rank = Rank.SPECIES();
        Reference<?> ref1 = ReferenceFactory.newGeneric();
        //HomotypicalGroup group = HomotypicalGroup.NewInstance();
        Taxon taxon1 = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test3", null, null, null, null, null, null, null), null);
        Synonym synonym0 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym1 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym2 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test4", null, null, null, null, null, null, null), null);
        synonym0.getName().setHomotypicalGroup(taxon1.getHomotypicGroup());
        synonym2.getName().setHomotypicalGroup(synonym1.getHomotypicGroup());
        //tax2.addHeterotypicSynonymName(synonym.getName());
        taxon1.addSynonym(synonym1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
        taxon1.addSynonym(synonym2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

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
        Reference<?> ref1 = ReferenceFactory.newGeneric();
        //HomotypicalGroup group = HomotypicalGroup.NewInstance();
        Taxon taxon1 = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test3", null, null, null, null, null, null, null), null);
        Synonym synonym0 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym1 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
        Synonym synonym2 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test4", null, null, null, null, null, null, null), null);
        synonym0.getName().setHomotypicalGroup(taxon1.getHomotypicGroup());
        synonym2.getName().setHomotypicalGroup(synonym1.getHomotypicGroup());
        //tax2.addHeterotypicSynonymName(synonym.getName());
        taxon1.addSynonym(synonym0, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
        taxon1.addSynonym(synonym1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
        taxon1.addSynonym(synonym2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

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
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBoolean(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
                "SynonymRelationship","SynonymRelationship_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};
//		BotanicalName taxonName1 = BotanicalName.NewInstance(Rank.SPECIES());
//		taxonName1.setTitleCache("TaxonName1",true);
//		BotanicalName taxonName2 = BotanicalName.NewInstance(Rank.SPECIES());
//		taxonName2.setTitleCache("TaxonName2",true);
//		BotanicalName synonymName1 = BotanicalName.NewInstance(Rank.SPECIES());
//		synonymName1.setTitleCache("Synonym1",true);
//		BotanicalName synonymName2 = BotanicalName.NewInstance(Rank.SPECIES());
//		synonymName2.setTitleCache("Synonym2",true);
//
//		Reference<?> sec = null;
//		Taxon taxon1 = Taxon.NewInstance(taxonName1, sec);
//		Taxon taxon2 = Taxon.NewInstance(taxonName2, sec);
//		Synonym synonym1 = Synonym.NewInstance(synonymName1, sec);
//		Synonym synonym2 = Synonym.NewInstance(synonymName2, sec);
//
//		SynonymRelationship rel1 = taxon1.addSynonym(synonym1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
//		SynonymRelationship rel = taxon2.addSynonym(synonym1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
//		rel.setProParte(true);
//		rel1.setProParte(true);
//
//		service.save(taxon1);
//		service.save(synonym2);
//
//		this.setComplete();
//		this.endTransaction();
//
//
        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

//		UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
//		UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
//		UUID uuidSynonym2=UUID.fromString("f8d86dc9-5f18-4877-be46-fbb9412465e4");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        service.deleteSynonym(synonym1, null, true, true);

        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 1 synonym left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
        int nRelations = service.countAllRelationships();
        Assert.assertEquals("There should be no relationship left in the database", 0, nRelations);
    }

    @Test
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanRelToOneTaxon(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
                "SynonymRelationship","SynonymRelationship_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

        UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
        UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
//		UUID uuidSynonym2=UUID.fromString("f8d86dc9-5f18-4877-be46-fbb9412465e4");

        Taxon taxon2 = (Taxon)service.load(uuidTaxon2);
        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);

        taxon2.removeSynonym(synonym1, false);
        service.saveOrUpdate(taxon2);
        this.setComplete();
        this.endTransaction();

        this.startNewTransaction();
        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
        int nRelations = service.countAllRelationships();
        Assert.assertEquals("There should be 1 relationship left in the database", 1, nRelations);

        taxon2 = (Taxon)service.load(uuidTaxon2);
        synonym1 = (Synonym)service.load(uuidSynonym1);

        service.deleteSynonym(synonym1, null, true, true);

        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 1 synonym left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
        nRelations = service.countAllRelationships();
        Assert.assertEquals("There should be no relationship left in the database", 0, nRelations);

    }

    @Test
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanDeleteOneTaxon(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
                "SynonymRelationship","SynonymRelationship_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

        UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
        UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonym2=UUID.fromString("f8d86dc9-5f18-4877-be46-fbb9412465e4");

        Taxon taxon1 = (Taxon)service.load(uuidTaxon1);
        Taxon taxon2 = (Taxon)service.load(uuidTaxon2);
        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);

        service.deleteSynonym(synonym1, taxon1, true, true);

        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 2 synonyms left in the database (synonym is related to taxon2)", 2, nSynonyms);
        nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should be 4 names left in the database (name not deleted as synonym was not deleted)", 4, nNames);
        int nRelations = service.countAllRelationships();
        Assert.assertEquals("There should be 1 relationship left in the database", 1, nRelations);

    }

    @Test
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanWithRelatedName(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
                "SynonymRelationship","SynonymRelationship_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);

        UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
        UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonym2=UUID.fromString("f8d86dc9-5f18-4877-be46-fbb9412465e4");
        UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        TaxonNameBase name2 = (TaxonNameBase)nameService.load(uuidSynonymName2);
        synonym1.getName().addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null);

        service.deleteSynonym(synonym1, null, true, true);

        this.commitAndStartNewTransaction(tableNames);

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 1 synonyms left in the database", 1, nSynonyms);
        nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should be 4 names left in the database (name is related to synonymName2)", 4, nNames);
        int nRelations = service.countAllRelationships();
        //may change with better implementation of countAllRelationships (see #2653)
        Assert.assertEquals("There should be 1 relationship left in the database (the name relationship)", 1, nRelations);

        //clean up database
        name2 = (TaxonNameBase)nameService.load(uuidSynonymName2);
        NameRelationship rel = CdmBase.deproxy(name2.getNameRelations().iterator().next(), NameRelationship.class);
        name2.removeNameRelationship(rel);
        nameService.save(name2);
        this.setComplete();
        this.endTransaction();

    }

    @Test
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanWithRollback(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
                "SynonymRelationship","SynonymRelationship_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
        int nRelations = service.countAllRelationships();
        //may change with better implementation of countAllRelationships (see #2653)
        Assert.assertEquals("There should be 2 relationship in the database (the 2 synonym relationship) but no name relationship", 2, nRelations);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        TaxonNameBase name2 = (TaxonNameBase)nameService.load(uuidSynonymName2);
        synonym1.getName().addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null);

        service.deleteSynonym(synonym1, null, true, true);

        this.rollback();
//		printDataSet(System.out, tableNames);
        this.startNewTransaction();

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 2 synonyms left in the database", 2, nSynonyms);
        nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
        nRelations = service.countAllRelationships();
        //may change with better implementation of countAllRelationships (see #2653)
        Assert.assertEquals("There should be 2 relationship in the database (the 2 synonym relationship) but no name relationship", 2, nRelations);

    }

    @Test
    @DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
    public final void testDeleteSynonymSynonymTaxonBooleanWithoutTransaction(){
        final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
                "SynonymRelationship","SynonymRelationship_AUD",
                "HomotypicalGroup","HomotypicalGroup_AUD"};

        int nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
        int nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
        int nRelations = service.countAllRelationships();
        //may change with better implementation of countAllRelationships (see #2653)
        Assert.assertEquals("There should be 2 relationship in the database (the 2 synonym relationship) but no name relationship", 2, nRelations);

        UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
        UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");

        Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
        TaxonNameBase name2 = (TaxonNameBase)nameService.load(uuidSynonymName2);
        synonym1.getName().addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null);

        service.saveOrUpdate(synonym1);

        this.setComplete();
        this.endTransaction();

        printDataSet(System.out, tableNames);

        //out of wrapping transaction
        service.deleteSynonym(synonym1, null, true, true);

        this.startNewTransaction();

        nSynonyms = service.count(Synonym.class);
        Assert.assertEquals("There should still be 1 synonyms left in the database. The rollback on name delete should not lead to rollback in synonym delete.", 1, nSynonyms);
        nNames = nameService.count(TaxonNameBase.class);
        Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
        nRelations = service.countAllRelationships();
        //may change with better implementation of countAllRelationships (see #2653)
        Assert.assertEquals("There should be 1 name relationship and no synonym relationship in the database", 1, nRelations);

    }

}
