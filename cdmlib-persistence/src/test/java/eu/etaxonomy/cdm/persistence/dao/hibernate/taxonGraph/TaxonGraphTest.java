/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @since Sep 27, 2018
 */
public class TaxonGraphTest extends CdmTransactionalIntegrationTest {

    enum EventType{
        INSERT, UPDATE;
    }

    @SpringBeanByType
    private ITaxonGraphDao taxonGraphDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    private ITaxonNameDao nameDao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

    @SpringBeanByType
    private IPreferenceDao prefDao;

    protected static UUID uuid_secRef = UUID.fromString("34e1ff99-63c4-4296-81b6-b20afb98902e");

    protected static UUID uuid_n_euglenophyceae = UUID.fromString("9928147d-4499-4ce9-bcf3-e4eaa13e509e");
    protected static UUID uuid_n_euglena = UUID.fromString("ab59d853-dd4f-4f80-bd7b-cf53bfd42d39");
    protected static UUID uuid_n_trachelomonas = UUID.fromString("5e3d015c-0a5c-4975-a3b0-334b4b47ff79");
    protected static UUID uuid_n_trachelomonas_a  = UUID.fromString("a798721a-e305-420d-aec1-e915ad1971e4");
    protected static UUID uuid_n_trachelomonas_o  = UUID.fromString("a2e7eeff-b844-4b3d-ab75-2a113b44573e");
    protected static UUID uuid_n_trachelomonas_o_var_d  = UUID.fromString("d8a0e3ad-2a4d-45ed-b874-f96616015f91");
    protected static UUID uuid_n_trachelomonas_s  = UUID.fromString("5b90bd58-7f76-45c4-9966-7f65e7bf0bb0");
    protected static UUID uuid_n_trachelomonas_s_var_a = UUID.fromString("192ad8a1-55ca-4379-87a1-3bbd04e8b880");
    protected static UUID uuid_n_trachelomonas_r_s = UUID.fromString("2d6e68bf-aba7-433d-8325-ea15f3e567f4");
    protected static UUID uuid_n_phacus_s = UUID.fromString("d59b8715-1b98-4da4-a42d-efcbe85b323c");

    protected static UUID uuid_t_euglenophyceae = UUID.fromString("4ea17d7a-17a3-41f0-8de6-e924494ecbae");
    protected static UUID uuid_t_euglena = UUID.fromString("1c69afd4-ae58-4913-8706-5c89729d38f4");
    protected static UUID uuid_t_trachelomonas = UUID.fromString("52b9a8e0-9133-4ee0-ba9f-84ca6e28d033");
    protected static UUID uuid_t_trachelomonas_a  = UUID.fromString("04443b64-f2e5-48c5-9069-9354f43ded9f");
    protected static UUID uuid_t_trachelomonas_o  = UUID.fromString("bdf75350-8361-4e33-a614-a4214cc3e90a");
    protected static UUID uuid_t_trachelomonas_o_var_d  = UUID.fromString("f54ad8cf-fe87-499d-826a-2c5a71551fcf");
    protected static UUID uuid_t_trachelomonas_s  = UUID.fromString("5dce8a09-c809-4027-a9ce-b70901e7b820");
    protected static UUID uuid_t_trachelomonas_s_var_a = UUID.fromString("3f14c528-e191-4a6f-b2a9-36c9a3fc7eee");

    public AbstractHibernateTaxonGraphProcessor taxonGraphProcessor(){
        AbstractHibernateTaxonGraphProcessor processor = new AbstractHibernateTaxonGraphProcessor() {
            @Override
            public Session getSession() {
                return nameDao.getSession();
            }
        };
        return processor;
    }

    protected void setUuidPref() {
        PrefKey key = TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID;
        prefDao.set(new CdmPreference(key.getSubject(), key.getPredicate(), TaxonGraphTest.uuid_secRef.toString()));
        commitAndStartNewTransaction();
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testNewTaxonName() throws TaxonGraphException{

        setUuidPref();

        Reference refX = ReferenceFactory.newBook();
        refX.setTitleCache("Ref-X", true);

        TaxonName n_t_argentinensis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "argentinensis", null, null, refX, null, null);
        n_t_argentinensis = nameDao.save(n_t_argentinensis);
        AbstractHibernateTaxonGraphProcessor processor = taxonGraphProcessor();
        Taxon singleTaxon = processor.assureSingleTaxon(n_t_argentinensis);
        processor.updateEdges(singleTaxon);
        commitAndStartNewTransaction();

         // printDataSet(System.err,"TaxonRelationship");
        Assert.assertTrue("a taxon should have been created", n_t_argentinensis.getTaxa().size() > 0);

        List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_t_argentinensis, nameDao.load(uuid_n_trachelomonas), true);
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testChangeNomRef() throws TaxonGraphException{

        setUuidPref();

        Reference refX = ReferenceFactory.newBook();
        refX.setTitleCache("Ref-X", true);

        TaxonName n_trachelomonas_a = nameDao.load(uuid_n_trachelomonas_a);
        Reference oldNomReference = n_trachelomonas_a.getNomenclaturalReference();
        n_trachelomonas_a.setNomenclaturalReference(refX);
        nameDao.saveOrUpdate(n_trachelomonas_a);
        AbstractHibernateTaxonGraphProcessor processor = taxonGraphProcessor();
        Taxon singleTaxon = processor.assureSingleTaxon(n_trachelomonas_a);
        processor.updateReferenceInEdges(singleTaxon, refX, oldNomReference);

//        Logger.getLogger("org.hibernate.SQL").setLevel(Level.TRACE);
        commitAndStartNewTransaction();

        // printDataSet(System.err,"TaxonRelationship");
        List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_a, nameDao.load(uuid_n_trachelomonas), true);
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testChangeRank() throws TaxonGraphException{

        setUuidPref();

        TaxonName n_trachelomonas_o_var_d = nameDao.load(uuid_n_trachelomonas_o_var_d);

        List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());

        n_trachelomonas_o_var_d.setRank(Rank.SPECIES());
        nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
        AbstractHibernateTaxonGraphProcessor processor = taxonGraphProcessor();
        Taxon singleTaxon = processor.assureSingleTaxon(n_trachelomonas_o_var_d);
        processor.updateEdges(singleTaxon);
        commitAndStartNewTransaction();

        // printDataSet(System.err,"TaxonRelationship");
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("The edge to Trachelomonas oviformis should have been deleted", 0, edges.size());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testChangeGenus() throws TaxonGraphException{

        setUuidPref();

        TaxonName n_trachelomonas_o_var_d = nameDao.load(uuid_n_trachelomonas_o_var_d);

        List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());
        edges = taxonGraphDao.edges(null, nameDao.load(uuid_n_euglena), true);
        Assert.assertEquals("No edges to 'Euglena' expected", 0, edges.size());

        n_trachelomonas_o_var_d.setGenusOrUninomial("Euglena");
        nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
        AbstractHibernateTaxonGraphProcessor processor = taxonGraphProcessor();
        Taxon singleTaxon = processor.assureSingleTaxon(n_trachelomonas_o_var_d);
        processor.updateEdges(singleTaxon);
        commitAndStartNewTransaction();

        // printDataSet(System.err,"TaxonRelationship");
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("The edge to Trachelomonas should have been deleted", 0, edges.size());
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("The edge to 'Trachelomonas oviformis' should have been deleted", 0, edges.size());
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_euglena), true);
        Assert.assertEquals("The edge to 'Euglena' should have been created", 1, edges.size());
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testChangeSpecificEpithet_of_InfraSpecific() throws TaxonGraphException{

        setUuidPref();

        TaxonName n_trachelomonas_o_var_d = nameDao.load(uuid_n_trachelomonas_o_var_d);

        List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas' expected", 1, edges.size());
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("One edge from 'Trachelomonas oviformis var. duplex' to 'Trachelomonas oviformis' expected", 1, edges.size());

        n_trachelomonas_o_var_d.setSpecificEpithet("alabamensis");
        nameDao.saveOrUpdate(n_trachelomonas_o_var_d);
        AbstractHibernateTaxonGraphProcessor processor = taxonGraphProcessor();
        Taxon singleTaxon = processor.assureSingleTaxon(n_trachelomonas_o_var_d);
        processor.updateEdges(singleTaxon);
        commitAndStartNewTransaction();

        // printDataSet(System.err,"TaxonRelationship");
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas), true);
        Assert.assertEquals("The edge to Trachelomonas should still exist", 1, edges.size());
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_o), true);
        Assert.assertEquals("The edge to Trachelomonas oviformis should have been deleted", 0, edges.size());
        edges = taxonGraphDao.edges(n_trachelomonas_o_var_d, nameDao.load(uuid_n_trachelomonas_a), true);
        Assert.assertEquals("The edge to Trachelomonas alabamensis should have been created", 1, edges.size());
    }


    @Override
    // @Test
    public void createTestDataSet() throws FileNotFoundException {

        TaxonRelationshipType relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();

        Session session = referenceDao.getSession();

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

        Reference otherSecRef = ReferenceFactory.newDatabase();
        otherSecRef.setTitleCache("Other sec ref", true);

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
        TaxonName n_trachelomonas_s = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "sydneyensis",  null, null, refE, null, null);
        n_trachelomonas_s.setUuid(uuid_n_trachelomonas_s);
        TaxonName n_trachelomonas_s_var_a = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY(), "Trachelomonas", null, "sydneyensis",  "acuminata", null, refG, null, null);
        n_trachelomonas_s_var_a.setUuid(uuid_n_trachelomonas_s_var_a);
        TaxonName n_trachelomonas_r_s = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY(), "Trachelomonas", null, "robusta", "sparsiornata", null, refG, null, null);
        n_trachelomonas_r_s.setUuid(uuid_n_trachelomonas_r_s);
        TaxonName n_phacus_s = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Phacus", null, "smulkowskianus", null, null, refG, null, null);
        n_phacus_s.setUuid(uuid_n_phacus_s);

        Taxon t_euglenophyceae = Taxon.NewInstance(n_euglenophyceae, secRef);
        t_euglenophyceae.setUuid(uuid_t_euglenophyceae);
        Taxon t_euglena = Taxon.NewInstance(n_euglena, secRef);
        t_euglena.setUuid(uuid_t_euglena);
        Taxon t_trachelomonas = Taxon.NewInstance(n_trachelomonas, secRef);
        Taxon other_t_trachelomonas = Taxon.NewInstance(n_trachelomonas, otherSecRef);
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

        for(Object cdm : new Object[]{
                // refs:
                secRef, refA, refB, refC, refD, refE, refF,
                // names without taxa
                n_phacus_s, n_trachelomonas_r_s,
                // taxa:
                t_euglenophyceae, t_euglena, t_trachelomonas, other_t_trachelomonas, t_trachelomonas_a, t_trachelomonas_o, t_trachelomonas_o_var_d,
                t_trachelomonas_s, t_trachelomonas_s_var_a}) {
            session.save(cdm);
        }

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
