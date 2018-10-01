/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.taxonGraph;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @since Sep 27, 2018
 *
 */
public class TaxonGraphTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonGraphService taxonGraphService;

    @SpringBeanByType
    private TaxonGraphObserver taxonGraphObserver;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private ITaxonService taxonService;

    UUID uuid_secRef = UUID.fromString("34e1ff99-63c4-4296-81b6-b20afb98902e");

    UUID uuid_n_euglenophyceae = UUID.fromString("9928147d-4499-4ce9-bcf3-e4eaa13e509e");
    UUID uuid_n_euglena = UUID.fromString("ab59d853-dd4f-4f80-bd7b-cf53bfd42d39");
    UUID uuid_n_trachelomonas = UUID.fromString("5e3d015c-0a5c-4975-a3b0-334b4b47ff79");
    UUID uuid_n_trachelomonas_a  = UUID.fromString("a798721a-e305-420d-aec1-e915ad1971e4");
    UUID uuid_n_trachelomonas_o  = UUID.fromString("a2e7eeff-b844-4b3d-ab75-2a113b44573e");
    UUID uuid_n_trachelomonas_o_var_d  = UUID.fromString("d8a0e3ad-2a4d-45ed-b874-f96616015f91");
    UUID uuid_n_trachelomonas_s  = UUID.fromString("5b90bd58-7f76-45c4-9966-7f65e7bf0bb0");
    UUID uuid_n_trachelomonas_s_var_a = UUID.fromString("192ad8a1-55ca-4379-87a1-3bbd04e8b880");

    UUID uuid_t_euglenophyceae = UUID.fromString("4ea17d7a-17a3-41f0-8de6-e924494ecbae");
    UUID uuid_t_euglena = UUID.fromString("1c69afd4-ae58-4913-8706-5c89729d38f4");
    UUID uuid_t_trachelomonas = UUID.fromString("52b9a8e0-9133-4ee0-ba9f-84ca6e28d033");
    UUID uuid_t_trachelomonas_a  = UUID.fromString("04443b64-f2e5-48c5-9069-9354f43ded9f");
    UUID uuid_t_trachelomonas_o  = UUID.fromString("bdf75350-8361-4e33-a614-a4214cc3e90a");
    UUID uuid_t_trachelomonas_o_var_d  = UUID.fromString("f54ad8cf-fe87-499d-826a-2c5a71551fcf");
    UUID uuid_t_trachelomonas_s  = UUID.fromString("5dce8a09-c809-4027-a9ce-b70901e7b820");
    UUID uuid_t_trachelomonas_s_var_a = UUID.fromString("3f14c528-e191-4a6f-b2a9-36c9a3fc7eee");

//    static boolean isObserverRegistred = false;

//    @Before
//    public void registerObserver() {
//        if(!isObserverRegistred){
//            CdmPostDataChangeObservableListener.getDefault().register(taxonGraphObserver);
//            isObserverRegistred = true;
//        }
//    }

    @Before
    public void setSecRef(){
        taxonGraphService.setSecReferenceUUID(uuid_secRef);
    }


    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testnewTaxonName() throws TaxonGraphException{

        Reference refX = ReferenceFactory.newBook();
        refX.setTitleCache("Ref-X", true);

        TaxonName n_t_argentinensis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "argentinensis", null, null, refX, null, null);
        n_t_argentinensis = nameService.save(n_t_argentinensis);
        taxonGraphService.onNewTaxonName(n_t_argentinensis);
        commitAndStartNewTransaction();

         // printDataSet(System.err,"TaxonRelationship");
        Assert.assertTrue("a taxon should have been created", n_t_argentinensis.getTaxa().size() > 0);

        List<TaxonGraphEdgeDTO> edges = taxonGraphService.edges(n_t_argentinensis, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testChangeNomRef() throws TaxonGraphException{

        Reference refX = ReferenceFactory.newBook();
        refX.setTitleCache("Ref-X", true);

        TaxonName n_trachelomonas_a = nameService.load(uuid_n_trachelomonas_a);
        Reference oldNomReference = n_trachelomonas_a.getNomenclaturalReference();
        n_trachelomonas_a.setNomenclaturalReference(refX);
        n_trachelomonas_a = nameService.save(n_trachelomonas_a);
        taxonGraphService.onNomReferenceChange(n_trachelomonas_a, oldNomReference);

        Logger.getLogger("org.hibernate.SQL").setLevel(Level.TRACE);
        commitAndStartNewTransaction();

        // printDataSet(System.err,"TaxonRelationship");
        List<TaxonGraphEdgeDTO> edges = taxonGraphService.edges(n_trachelomonas_a, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testChangeRank() throws TaxonGraphException{

        TaxonName n_trachelomonas_o_var_d = nameService.load(uuid_n_trachelomonas_o_var_d);

        List<TaxonGraphEdgeDTO> edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());

        n_trachelomonas_o_var_d.setRank(Rank.SPECIES());
        nameService.saveOrUpdate(n_trachelomonas_o_var_d);
        taxonGraphService.onNameOrRankChange(nameService.load(uuid_n_trachelomonas_o_var_d)); // TODO reloading needed?
        commitAndStartNewTransaction();

        // printDataSet(System.err,"TaxonRelationship");
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("The edge to Trachelomonas oviformis should have been deleted", 0, edges.size());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testChangeGenus() throws TaxonGraphException{

        TaxonName n_trachelomonas_o_var_d = nameService.load(uuid_n_trachelomonas_o_var_d);

        List<TaxonGraphEdgeDTO> edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());
        edges = taxonGraphService.edges(null, nameService.load(uuid_n_euglena), true);
        Assert.assertEquals("No edges to 'Euglena' expected", 0, edges.size());

        n_trachelomonas_o_var_d.setGenusOrUninomial("Euglena");
        nameService.saveOrUpdate(n_trachelomonas_o_var_d);
        taxonGraphService.onNameOrRankChange(nameService.load(uuid_n_trachelomonas_o_var_d)); // TODO reloading needed?
        commitAndStartNewTransaction();

        // printDataSet(System.err,"TaxonRelationship");
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("The edge to Trachelomonas should have been deleted", 0, edges.size());
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("The edge to 'Trachelomonas oviformis' should have been deleted", 0, edges.size());
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_euglena), true);
        Assert.assertEquals("The edge to 'Euglena' should have been created", 1, edges.size());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testChangeSepcificEpithet() throws TaxonGraphException{

        TaxonName n_trachelomonas_o_var_d = nameService.load(uuid_n_trachelomonas_o_var_d);

        List<TaxonGraphEdgeDTO> edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());

        n_trachelomonas_o_var_d.setSpecificEpithet("alabamensis");
        nameService.saveOrUpdate(n_trachelomonas_o_var_d);
        taxonGraphService.onNameOrRankChange(nameService.load(uuid_n_trachelomonas_o_var_d)); // TODO reloading needed?
        commitAndStartNewTransaction();

        // printDataSet(System.err,"TaxonRelationship");
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("The edge to Trachelomonas oviformis should have been deleted", 0, edges.size());
        edges = taxonGraphService.edges(n_trachelomonas_o_var_d, nameService.load(uuid_n_trachelomonas_a), true);
        Assert.assertEquals("The edge to Trachelomonas alabamensis should have been created", 1, edges.size());
    }


    @Override
    // @Test
    public void createTestDataSet() throws FileNotFoundException {

        TaxonRelationshipType relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();

        // graph:
        //
        // - Euglenophyceae Ref-A
        //   +-- Trachelomonas Ref-B
        //       +-- Trachelomonas alabamensis Ref-C
        //       +-- Trachelomonas oviformis Ref-D in Ref-C
        //       +-- +-- Trachelomonas oviformis var. duplex Ref-D in Ref-C
        //       +-- Trachelomonas sydneyensis Ref-E
        //       +-- +-- Trachelomonas sydneyensis var. acuminata Ref-F

        Reference secRef = ReferenceFactory.newDatabase();
        secRef.setTitleCache("secRef", true);

        secRef.setUuid(uuid_secRef);
        Reference refA = ReferenceFactory.newBook();
        refA.setTitleCache("Ref-A", true);
        Reference refB = ReferenceFactory.newBook();
        refB.setTitleCache("Ref-B", true);
        Reference refC = ReferenceFactory.newArticle();
        refC.setTitleCache("Ref-C", true);
        Reference refD = ReferenceFactory.newSection();
        refD.setTitleCache("Ref-D in Ref-C", true);
        refD.setInReference(refC);
        Reference refE = ReferenceFactory.newBook();
        refE.setTitleCache("Ref-E", true);
        Reference refF = ReferenceFactory.newBook();
        refF.setTitleCache("Ref-F", true);
        Reference refG = ReferenceFactory.newBook();
        refG.setTitleCache("Ref-G", true);

        TaxonName n_euglenophyceae = TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY(), "Euglenophyceae", null, null, null, null, refA, null, null);
        n_euglenophyceae.setUuid(uuid_n_euglenophyceae);
        TaxonName n_euglena = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Euglena", null, null, null, null, refA, null, null);
        n_euglena.setUuid(uuid_n_euglena);
        TaxonName n_trachelomonas = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Trachelomonas", null, null, null, null, refB, null, null);
        n_trachelomonas.setUuid(uuid_n_trachelomonas);
        TaxonName n_trachelomonas_a = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "alabamensis",  null, null, refC, null, null);
        n_trachelomonas_a.setUuid(uuid_n_trachelomonas_a);
        TaxonName n_trachelomonas_o = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "oviformis", null, null, refD, null, null);
        n_trachelomonas_o.setUuid(uuid_n_trachelomonas_o);
        TaxonName n_trachelomonas_o_var_d = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY(), "Trachelomonas", null, "oviformis", "duplex", null, refD, null, null);
        n_trachelomonas_o_var_d.setUuid(uuid_n_trachelomonas_o_var_d);
        TaxonName n_trachelomonas_s = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "alabamensis",  null, null, refE, null, null);
        n_trachelomonas_s.setUuid(uuid_n_trachelomonas_s);
        TaxonName n_trachelomonas_s_var_a = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY(), "Trachelomonas", null, "alabamensis",  "acuminata", null, refG, null, null);
        n_trachelomonas_s_var_a.setUuid(uuid_n_trachelomonas_s_var_a);

        Taxon t_euglenophyceae = Taxon.NewInstance(n_euglenophyceae, secRef);
        t_euglenophyceae.setUuid(uuid_t_euglenophyceae);
        Taxon t_euglena = Taxon.NewInstance(n_euglena, secRef);
        t_euglena.setUuid(uuid_t_euglena);
        Taxon t_trachelomonas = Taxon.NewInstance(n_trachelomonas, secRef);
        t_trachelomonas.setUuid(uuid_t_trachelomonas);
        Taxon t_trachelomonas_a = Taxon.NewInstance(n_trachelomonas_a, secRef);
        t_trachelomonas_a.setUuid(uuid_t_trachelomonas_a);
        Taxon t_trachelomonas_o = Taxon.NewInstance(n_trachelomonas_o, secRef);
        t_trachelomonas_o.setUuid(uuid_t_trachelomonas_o);
        Taxon t_trachelomonas_o_var_d = Taxon.NewInstance(n_trachelomonas_o_var_d, secRef);
        t_trachelomonas_o_var_d.setUuid(uuid_t_trachelomonas_o_var_d);
        Taxon t_trachelomonas_s = Taxon.NewInstance(n_trachelomonas_s, secRef);
        t_trachelomonas_s.setUuid(uuid_t_trachelomonas_s);
        Taxon t_trachelomonas_s_var_a = Taxon.NewInstance(n_trachelomonas_s_var_a, secRef);
        t_trachelomonas_s_var_a.setUuid(uuid_t_trachelomonas_s_var_a);

        referenceService.save(Arrays.asList(secRef, refA, refB, refC, refD, refE, refF));
        taxonService.save(Arrays.asList(t_euglenophyceae, t_euglena, t_trachelomonas, t_trachelomonas_a, t_trachelomonas_o, t_trachelomonas_o_var_d, t_trachelomonas_s, t_trachelomonas_s_var_a));

        List<TaxonRelationship> taxonRels = new ArrayList<>();

        taxonRels.add(t_trachelomonas_o_var_d.addTaxonRelation(t_trachelomonas, relType, refC, null));
        taxonRels.add(t_trachelomonas_o_var_d.addTaxonRelation(t_trachelomonas_o, relType, refC, null));

        taxonRels.add(t_trachelomonas_o.addTaxonRelation(t_trachelomonas, relType, refC, null));

        taxonRels.add(t_trachelomonas_s_var_a.addTaxonRelation(t_trachelomonas, relType, n_trachelomonas_s_var_a.getNomenclaturalReference(), null));
        taxonRels.add(t_trachelomonas_s_var_a.addTaxonRelation(t_trachelomonas_s, relType, n_trachelomonas_s_var_a.getNomenclaturalReference(), null));

        taxonRels.add(t_trachelomonas_s.addTaxonRelation(t_trachelomonas, relType, n_trachelomonas_s.getNomenclaturalReference(), null));

        taxonRels.add(t_trachelomonas_a.addTaxonRelation(t_trachelomonas, relType, n_trachelomonas_a.getNomenclaturalReference(), null));

        taxonRels.add(t_trachelomonas.addTaxonRelation(t_euglenophyceae, relType, n_trachelomonas.getNomenclaturalReference(), null));
        taxonRels.add(t_euglena.addTaxonRelation(t_euglenophyceae, relType, n_euglena.getNomenclaturalReference(), null));

        commitAndStartNewTransaction();

        String fileNameAppendix = null;
        writeDbUnitDataSetFile(new String[] {
            "TAXONBASE", "TAXONNAME", "HomotypicalGroup", "Reference",
            "TaxonRelationship",
            "LANGUAGESTRING",
            "HIBERNATE_SEQUENCES", // IMPORTANT!!!
            },
            fileNameAppendix, false );

    }

}
