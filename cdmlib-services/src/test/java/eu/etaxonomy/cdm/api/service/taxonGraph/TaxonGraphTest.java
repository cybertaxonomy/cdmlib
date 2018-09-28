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

import org.junit.Assert;
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
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

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
    UUID uuid_n_trachelomonas = UUID.fromString("5e3d015c-0a5c-4975-a3b0-334b4b47ff79");
    UUID uuid_n_trachelomonas_a  = UUID.fromString("a798721a-e305-420d-aec1-e915ad1971e4");
    UUID uuid_n_trachelomonas_o_var_d  = UUID.fromString("d8a0e3ad-2a4d-45ed-b874-f96616015f91");
    UUID uuid_n_trachelomonas_s  = UUID.fromString("5b90bd58-7f76-45c4-9966-7f65e7bf0bb0");
    UUID uuid_n_trachelomonas_s_var_a = UUID.fromString("192ad8a1-55ca-4379-87a1-3bbd04e8b880");

    UUID uuid_t_euglenophyceae = UUID.fromString("4ea17d7a-17a3-41f0-8de6-e924494ecbae");
    UUID uuid_t_trachelomonas = UUID.fromString("52b9a8e0-9133-4ee0-ba9f-84ca6e28d033");
    UUID uuid_t_trachelomonas_a  = UUID.fromString("04443b64-f2e5-48c5-9069-9354f43ded9f");
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

    @Test
    @DataSet
    public void testnewTaxonName() throws TaxonGraphException{

        taxonGraphService.setSecReferenceUUID(uuid_secRef);
        Reference refX = ReferenceFactory.newBook();
        refX.setTitleCache("Ref-X", true);

        TaxonName n_t_argentinensis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "argentinensis", null, null, refX, null, null);
        n_t_argentinensis = nameService.save(n_t_argentinensis);
        taxonGraphService.onNewTaxonName(n_t_argentinensis);
        taxonService.getSession().flush();

        Assert.assertTrue("a taxon should have been created", n_t_argentinensis.getTaxa().size() > 0);

        List<TaxonGraphEdgeDTO> edges = taxonGraphService.edges(n_t_argentinensis, nameService.load(uuid_n_trachelomonas), true);
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());
    }


    @Override
    @Test
    public void createTestDataSet() throws FileNotFoundException {

        // graph:
        //
        // - Euglenophyceae
        //   +-- Trachelomonas
        //       +-- Trachelomonas alabamensis
        //       +-- Trachelomonas oviformis var. duplex

        // Further names not in the graph:
         // Trachelomonas sydneyensis
        //  Trachelomonas sydneyensis var. acuminata

        Reference secRef = ReferenceFactory.newDatabase();
        secRef.setTitleCache("secRef", true);

        secRef.setUuid(uuid_secRef);
        Reference refA = ReferenceFactory.newBook();
        refA.setTitleCache("Ref-A", true);
        Reference refB = ReferenceFactory.newBook();
        refA.setTitleCache("Ref-B", true);
        Reference refC = ReferenceFactory.newBook();
        refA.setTitleCache("Ref-C", true);
        Reference refD = ReferenceFactory.newBookSection(refC, null, "Ref-D", null);
        Reference refE = ReferenceFactory.newBook();
        refA.setTitleCache("Ref-E", true);
        Reference refF = ReferenceFactory.newBook();
        refA.setTitleCache("Ref-F", true);

        TaxonName n_euglenophyceae = TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY(), "Euglenophyceae", null, null, null, null, refA, null, null);
        n_euglenophyceae.setUuid(uuid_n_euglenophyceae);
        TaxonName n_trachelomonas = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Trachelomonas", null, null, null, null, refB, null, null);
        n_trachelomonas.setUuid(uuid_n_trachelomonas);
        TaxonName n_trachelomonas_a = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "alabamensis",  null, null, refC, null, null);
        n_trachelomonas_a.setUuid(uuid_n_trachelomonas_a);
        TaxonName n_trachelomonas_o_var_d = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY(), "Trachelomonas", null, "oviformis", "duplex", null, refD, null, null);
        n_trachelomonas_o_var_d.setUuid(uuid_n_trachelomonas_o_var_d);
        TaxonName n_trachelomonas_s = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "alabamensis",  null, null, refE, null, null);
        n_trachelomonas_s.setUuid(uuid_n_trachelomonas_s);
        TaxonName n_trachelomonas_s_var_a = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY(), "Trachelomonas", null, "alabamensis",  "acuminata", null, refF, null, null);
        n_trachelomonas_s_var_a.setUuid(uuid_n_trachelomonas_s_var_a);

        Taxon t_euglenophyceae = Taxon.NewInstance(n_euglenophyceae, secRef);
        t_euglenophyceae.setUuid(uuid_t_euglenophyceae);
        Taxon t_trachelomonas = Taxon.NewInstance(n_trachelomonas, secRef);
        t_trachelomonas.setUuid(uuid_t_trachelomonas);
        Taxon t_trachelomonas_a = Taxon.NewInstance(n_trachelomonas_a, secRef);
        t_trachelomonas_a.setUuid(uuid_t_trachelomonas_a);
        Taxon t_trachelomonas_o_var_d = Taxon.NewInstance(n_trachelomonas_o_var_d, secRef);
        t_trachelomonas_o_var_d.setUuid(uuid_t_trachelomonas_o_var_d);
        Taxon t_trachelomonas_s = Taxon.NewInstance(n_trachelomonas_s, secRef);
        t_trachelomonas_s.setUuid(uuid_t_trachelomonas_s);
        Taxon t_trachelomonas_s_var_a = Taxon.NewInstance(n_trachelomonas_s_var_a, secRef);
        t_trachelomonas_s_var_a.setUuid(uuid_t_trachelomonas_s_var_a);

        referenceService.save(Arrays.asList(secRef, refA, refB, refC, refD, refE, refF));
        taxonService.save(Arrays.asList(t_euglenophyceae, t_trachelomonas, t_trachelomonas_a, t_trachelomonas_o_var_d, t_trachelomonas_s, t_trachelomonas_s_var_a));

        List<TaxonRelationship> taxonRels = new ArrayList<>();
        taxonRels.add(t_trachelomonas_o_var_d.addTaxonRelation(t_trachelomonas, TaxonGraphService.RELTYPE, n_trachelomonas_o_var_d.getNomenclaturalReference(), null));
        taxonRels.add(t_trachelomonas_a.addTaxonRelation(t_trachelomonas, TaxonGraphService.RELTYPE, n_trachelomonas_a.getNomenclaturalReference(), null));
        taxonRels.add(t_trachelomonas.addTaxonRelation(t_euglenophyceae, TaxonGraphService.RELTYPE, n_trachelomonas.getNomenclaturalReference(), null));
        for(TaxonRelationship trel : taxonRels){
            taxonService.getSession().merge(trel);
        }
        taxonService.getSession().flush();

        setComplete();
        endTransaction();

        String fileNameAppendix = null;
        writeDbUnitDataSetFile(new String[] {
            "TAXONBASE", "TAXONNAME", "HomotypicalGroup", "Reference",
            "TaxonRelationship",
            "LANGUAGESTRING",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix, false );

    }

}
