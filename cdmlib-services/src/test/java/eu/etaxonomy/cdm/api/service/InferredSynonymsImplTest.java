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
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * Test for {@link InferredSynonymsServiceImpl}
 *
 * @author a.mueller
 * @since 11.09.2025 (copied from {@link TaxonServiceImplTest})
 */
public class InferredSynonymsImplTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IInferredSynonymsService inferredSynonymService;

    @SpringBeanByType
    private IClassificationService classificationService;

    private Classification classification;

    boolean includeMisapplied = true;

    boolean includeUnpublished = true;

    private static final UUID classificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");

    private static final UUID acherontiaLachesisTaxonUuid = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");
    private static final UUID acherontiaLachesisSublachesisTaxonUuid = UUID.fromString("665eb714-736b-4685-b400-2229462d28d7");

    @Before
    public void setUp() throws Exception {

        classification = classificationService.find(classificationUuid);
    }

    /****************** TESTS *****************************/

    @Test
    @DataSet
    public void testComputeInferredSynonymy(){

        //verify expected initial DB state
        List<Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 5", 5, synonyms.size());

        //load accepted species
//        Taxon taxon = (Taxon)taxonService.find(acherontiaLachesisTaxonUuid);
        UUID taxonUuid = acherontiaLachesisTaxonUuid;

        //inferred epithet
        includeMisapplied = false;
        List<Synonym> inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid,
                classificationUuid, SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished,
                null);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals("There should be 1 inferred epithet synonym", 1, inferredSynonyms.size());
        assertEquals("the name of inferred epithet should be SynOfAcherontia lachesis",
                "SynOfAcherontia lachesis syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());
        Synonym inferredSyn = inferredSynonyms.iterator().next();
        assertEquals(1, inferredSyn.getSources().size());
        IdentifiableSource source = inferredSyn.getSources().iterator().next();
        assertEquals("Inferred epithet", source.getIdNamespace());
        assertEquals("ALACH12; SYN45", source.getIdInSource());
        assertEquals(OriginalSourceType.Transformation, source.getType());

        commitAndStartNewTransaction();

        //... with MAN
        includeMisapplied = true;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished, null);
        assertEquals("There should still be only 1 inferred epithet synonym because genus Acherontia has no MAN.",
                1, inferredSynonyms.size());
        inferredSyn = inferredSynonyms.iterator().next();
        assertEquals(1, inferredSyn.getSources().size());
        source = inferredSyn.getSources().iterator().next();
        assertEquals("Inferred epithet", source.getIdNamespace());
        assertEquals("Sp. Pl.", source.getCitation().getTitleCache());
        assertEquals("ALACH12; SYN45", source.getIdInSource());
        assertEquals(OriginalSourceType.Transformation, source.getType());
        commitAndStartNewTransaction();

        //inferred genus
        includeMisapplied = false;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_GENUS_OF, includeMisapplied, includeUnpublished, null);
        assertEquals("There should be 1 inferred genus synonym (note: Acherontia lachesis should not be inferred as it is a duplicate for an existing synonym",
                1, inferredSynonyms.size());
        assertEquals("the name of inferred genus should be Acherontia balosus", "Acherontia balosus syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());
        inferredSyn = inferredSynonyms.iterator().next();
        assertEquals(1, inferredSyn.getSources().size());
        source = inferredSyn.getSources().iterator().next();
        assertEquals("Inferred genus", source.getIdNamespace());
        assertEquals("Sp. Pl.", source.getCitation().getTitleCache());
        assertEquals("BAL33; ALACH12", source.getIdInSource());
        assertEquals(OriginalSourceType.Transformation, source.getType());
        commitAndStartNewTransaction();

        //... with misapplication
        commitAndStartNewTransaction();
        includeMisapplied = true;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_GENUS_OF, includeMisapplied, includeUnpublished, null);
        assertEquals("There should be 2 inferred genus synonyms now (note: Acherontia lachensis should not be inferred as it is a duplicate for an existing synonym",
                2, inferredSynonyms.size());
        commitAndStartNewTransaction();

        //inferred combination
        includeMisapplied = false;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.POTENTIAL_COMBINATION_OF, includeMisapplied, includeUnpublished, null);
        assertEquals("There should be 2 potential combination synonyms (SynOfAcherontia ciprosus, SynOfAcherontia balosus)",
                2, inferredSynonyms.size());
        Synonym syn1 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia ciprosus")).findFirst().get();
        assertEquals("the name of potential combination should be SynOfAcherontia ciprosus",
                "SynOfAcherontia ciprosus syn. sec. Sp. Pl.", syn1.getTitleCache());
        Synonym syn2 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia balosus")).findFirst().get();
        assertEquals("the name of potential combination should be SynOfAcherontia balosus",
                "SynOfAcherontia balosus syn. sec. Sp. Pl.", syn2.getTitleCache());
        commitAndStartNewTransaction();

        //...with MAN
        includeMisapplied = true;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.POTENTIAL_COMBINATION_OF, includeMisapplied, includeUnpublished, null);
        inferredSynonyms.forEach(s->System.out.println(s.getTitleCache()));
        assertEquals("There should be 3 potential combination synonyms (SynOfAcherontia ciprosus, SynOfAcherontia balosus)",
                3, inferredSynonyms.size());
        syn1 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia ciprosus")).findFirst().get();
        assertEquals("the name of potential combination should be SynOfAcherontia ciprosus",
                "SynOfAcherontia ciprosus syn. sec. Sp. Pl.", syn1.getTitleCache());
        assertEquals(1, syn1.getSources().size());

        syn2 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia balosus")).findFirst().get();
        assertEquals("the name of potential combination should be SynOfAcherontia balosus",
                "SynOfAcherontia balosus syn. sec. Sp. Pl.", syn2.getTitleCache());
        assertEquals(1, syn2.getSources().size());

        Synonym syn3 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia erronus")).findFirst().get();
        assertEquals("the name of potential combination should be Heterontia erronus",
                "SynOfAcherontia erronus syn. sec. Sp. Pl.", syn3.getTitleCache());
        assertEquals(1, syn3.getSources().size());
        source = syn3.getSources().iterator().next();
        assertEquals("Potential combination", source.getIdNamespace());
        assertEquals("Sp. Pl.", source.getCitation().getTitleCache());
        assertEquals("MIS22; SYN45", source.getIdInSource());
        assertEquals(OriginalSourceType.Transformation, source.getType());

        //TODO test that only zoological names return inferred synonyms

        //TODO unpublished handling (not so important for now as FauEu has no unpublished)
    }

    @Test
    @DataSet
    public void testComputeInferredSynonymySubspecies(){

        //subspecies
        Taxon taxon = (Taxon)taxonService.find(acherontiaLachesisSublachesisTaxonUuid);
        UUID taxonUuid = acherontiaLachesisSublachesisTaxonUuid;

        //... inferred epithet
        includeMisapplied = false;
        List<Synonym> inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid,
                classificationUuid, SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished,
                null);
        assertEquals("There should be 2 inferred epithet synonym", 2, inferredSynonyms.size());
        Set<String> caches = inferredSynonyms.stream().map(s->s.getTitleCache()).collect(Collectors.toSet());
        assertTrue("Should contain Acherontia ciprosus subsp. sublachesis",
                caches.contains("Acherontia ciprosus subsp. sublachesis syn. sec. Sp. Pl."));
        assertTrue("Should contain Heterontia balosus subsp. sublachesis",
                caches.contains("Heterontia balosus subsp. sublachesis syn. sec. Sp. Pl."));

        //... with MAN
        commitAndStartNewTransaction();
        includeMisapplied = true;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished, null);
        assertEquals("There should be 3 inferred epithet synonym", 3, inferredSynonyms.size());

        //.... inferred genus
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_GENUS_OF, includeMisapplied, includeUnpublished, null);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals("There should be 1 inferred genus synonym (note: Acherontia lachensis should not be inferred as it is a duplicate for an existing synonym",
                1, inferredSynonyms.size());
        assertEquals("the name of inferred genus should be Acherontia lachesis", "Acherontia lachesis subsp. subsynonus syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());

        //... inferred combination
        inferredSynonyms = taxonService.createInferredSynonyms(taxon, classification,
                SynonymType.POTENTIAL_COMBINATION_OF, includeMisapplied);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
//        inferredSynonyms.stream().map(s->s.getName()).forEach(n->System.out.println(n.getTitleCache()));
        inferredSynonyms.stream().map(s->s.getTitleCache()).forEach(n->System.out.println(n));
        assertEquals("There should be 3 potential combination synonyms",
                3, inferredSynonyms.size());
        Synonym syn1 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("Acherontia ciprosus subsp. subsynonus")).findFirst().get();
        assertEquals("the name of potential combination should be Acherontia ciprosus subsp. subsynonus",
                "Acherontia ciprosus subsp. subsynonus syn. sec. Sp. Pl.", syn1.getTitleCache());
        Synonym syn2 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("Heterontia balosus subsp. subsynonus")).findFirst().get();
        assertEquals("the name of potential combination should be Heterontia balosus subsp. subsynonus",
                "Heterontia balosus subsp. subsynonus syn. sec. Sp. Pl.", syn2.getTitleCache());
        Synonym syn3 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("Misapplicatus erronus subsp. subsynonus")).findFirst().get();
        assertEquals("the name of potential combination should be Misapplicatus erronus subsp. subsynonus",
                "Misapplicatus erronus subsp. subsynonus syn. sec. Sp. Pl.", syn3.getTitleCache());
    }

    @Test
    @DataSet
    public void testComputeInferredSynonymyInfrageneric(){

        //infrageneric taxon
        UUID acherontiaBonsaTaxonUuid = UUID.fromString("46235ca5-3ee0-45e5-aed5-846081a0a528");
        UUID taxonUuid = acherontiaBonsaTaxonUuid;

        //create data
        UUID acherontiaLachesisTaxonUuid = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");
        Taxon species = (Taxon)taxonService.find(acherontiaLachesisTaxonUuid);
        TaxonNode speciesNode = species.getTaxonNode(classification);
        Taxon infraGenTaxon = (Taxon)taxonService.find(acherontiaBonsaTaxonUuid);
        speciesNode.getParent().addChildTaxon(infraGenTaxon, null).addChildNode(speciesNode, null, null);

        //... inferred epithet
        List<Synonym> inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid,
                classificationUuid, SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished,
                null);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals("There should be 1 inferred epithet synonym", 1, inferredSynonyms.size());
        assertEquals("the name of inferred epithet should be SynOfAcherontia subg. Bonsa",
                "SynOfAcherontia subg. Bonsa syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());

        //.... inferred genus
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_GENUS_OF, includeMisapplied, includeUnpublished, null);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals("There should be 1 inferred genus synonym", 1, inferredSynonyms.size());
        assertEquals("the name of inferred genus should be Acherontia subg. Synbonsa", "Acherontia subg. Synbonsa syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());

        //... inferred combination
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.POTENTIAL_COMBINATION_OF, includeUnpublished, includeUnpublished, null);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
//        inferredSynonyms.stream().map(s->s.getName()).forEach(n->System.out.println(n.getTitleCache()));
        inferredSynonyms.stream().map(s->s.getTitleCache()).forEach(n->System.out.println(n));
        assertEquals("There should be 1 potential combination", 1, inferredSynonyms.size());
        assertEquals("the name of potential combinatio should be SynOfAcherontia subg. Synbonsa",
                "SynOfAcherontia subg. Synbonsa syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());
    }

    @Test
    @DataSet
    public void testCreateInferredSynonyms_PersistAndDeduplicate(){

        //verify expected DB state
        List<Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 5", 5, synonyms.size());

        //inferred epithet
        includeMisapplied = false;
        List<Synonym> inferredSynonyms = inferredSynonymService.createInferredSynonyms(
                acherontiaLachesisTaxonUuid,
                classificationUuid, SynonymType.INFERRED_GENUS_OF, includeMisapplied,
                includeUnpublished, null);
        assertEquals("There should be 1 new synonym", 1, inferredSynonyms.size());
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 6 now", 6, synonyms.size());

//        Set<String> caches = inferredSynonymService.getDistinctNameCaches();
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(
                acherontiaLachesisTaxonUuid,
                classificationUuid, SynonymType.INFERRED_GENUS_OF, includeMisapplied,
                includeUnpublished, null);
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 6 now", 6, synonyms.size());
        assertEquals("The new synonym should not be created again", 0, inferredSynonyms.size());

        //...with MAN
        includeMisapplied = true;
        inferredSynonyms = inferredSynonymService.createInferredSynonyms(
                acherontiaLachesisTaxonUuid,
                classificationUuid, SynonymType.INFERRED_GENUS_OF, includeMisapplied, true, null);
        assertEquals("There should be 1 new synonym", 1, inferredSynonyms.size());
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 7 now", 7, synonyms.size());

        //... and again (no new record should be created)
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(
                acherontiaLachesisTaxonUuid,
                classificationUuid, SynonymType.INFERRED_GENUS_OF, includeMisapplied,
                includeUnpublished, null);
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 7 now", 7, synonyms.size());
        assertEquals("The new synonym should not be created again", 0, inferredSynonyms.size());
    }

    @Test
    @DataSet
    public void testCreateAllInferredSynonyms_PersistAndDeduplicate(){

        //verify expected DB state
        List<Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 5", 5, synonyms.size());

        //inferred epithet
        boolean doPersist = false;
        List<Synonym> inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, null, doPersist);
        assertEquals("There should be 6 new synonyms", 6, inferredSynonyms.size());
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should still be 5", 5, synonyms.size());
        commitAndStartNewTransaction();
////        Set<String> caches = inferredSynonymService.getDistinctNameCaches();
//        inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
//                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
//                includeUnpublished, null, doPersist);
//        synonyms = taxonService.list(Synonym.class, null, null, null, null);
//        assertEquals("Number of synonyms should still be 11 now", 11, synonyms.size());
//        assertEquals("The new synonym should not be created again", 0, inferredSynonyms.size());
//        commitAndStartNewTransaction();

        //...with persist
        doPersist = true;
        inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, null, doPersist);
        assertEquals("There should be 6 new synonyms", 6, inferredSynonyms.size());
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 11 now", 11, synonyms.size());
        commitAndStartNewTransaction();

        //... and again (no new record should be created)
        inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, null, doPersist);
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should still be 11", 11, synonyms.size());
        assertEquals("The new synonym should not be created again", 0, inferredSynonyms.size());
    }

    @Test
    @DataSet
    public void testCreateAllInferredSynonymsWithGetDistinctNameCaches(){

        //verify expected DB state
        List<Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 5", 5, synonyms.size());

        //without cache (default behaviour)
        boolean doPersist = false;
        Set<String> caches = null;
        List<Synonym> inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, caches, doPersist);
        assertEquals("There should be 6 new synonyms", 6, inferredSynonyms.size());
        commitAndStartNewTransaction();

        //with cache
        caches = inferredSynonymService.getDistinctNameCaches();
        caches.forEach(c->System.out.println(c));
        assertEquals("There should be 9 distinct nameCaches for names below genus", 9, caches.size());

        caches.add("SynOfAcherontia lachesis");
        doPersist = true;
        inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, caches, doPersist);
        assertEquals("There should be only 5 new synonyms now", 5, inferredSynonyms.size());
    }

    @Override
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="ClearDBDataSet.xml")
    public void createTestDataSet() throws FileNotFoundException {
    }
}