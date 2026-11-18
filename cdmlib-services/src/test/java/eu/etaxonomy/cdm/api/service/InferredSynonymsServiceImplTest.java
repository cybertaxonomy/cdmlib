/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * Test for {@link InferredSynonymsServiceImpl}
 *
 * @author a.mueller
 * @since 11.09.2025 (copied from {@link TaxonServiceImplTest})
 */
public class InferredSynonymsServiceImplTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IInferredSynonymsService inferredSynonymService;

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

    private Classification classification;

    boolean includeMisapplied = true;

    boolean includeUnpublished = true;

    private static final UUID classificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");

    private static final UUID acherontiaLachesisTaxonUuid = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");
    private static final UUID acherontiaLachesisSublachesisTaxonUuid = UUID.fromString("665eb714-736b-4685-b400-2229462d28d7");

    @BeforeEach
    public void setUp() throws Exception {

        classification = classificationService.find(classificationUuid);
    }

    /****************** TESTS *****************************/

    @Test
    @CdmDataSet
    public void testComputeInferredSynonymy(){

        //verify expected initial DB state
        List<Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(5, synonyms.size(), "Number of synonyms should be 5");

        //load accepted species
//        Taxon taxon = (Taxon)taxonService.find(acherontiaLachesisTaxonUuid);
        UUID taxonUuid = acherontiaLachesisTaxonUuid;

        //inferred epithet
        includeMisapplied = false;
        List<Synonym> inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid,
                classificationUuid, SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished,
                null);
        assertNotNull(inferredSynonyms, "there should be a new synonym ");
        assertEquals(1, inferredSynonyms.size(), "There should be 1 inferred epithet synonym");
        assertEquals("SynOfAcherontia lachesis syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache(), "the name of inferred epithet should be SynOfAcherontia lachesis");
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
        assertEquals(1, inferredSynonyms.size(), "There should still be only 1 inferred epithet synonym because genus Acherontia has no MAN.");
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
        assertEquals(1, inferredSynonyms.size(), "There should be 1 inferred genus synonym (note: Acherontia lachesis should not be inferred as it is a duplicate for an existing synonym");
        assertEquals("Acherontia balosus syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache(), "the name of inferred genus should be Acherontia balosus");
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
        assertEquals(2, inferredSynonyms.size(), "There should be 2 inferred genus synonyms now (note: Acherontia lachensis should not be inferred as it is a duplicate for an existing synonym");
        commitAndStartNewTransaction();

        //inferred combination
        includeMisapplied = false;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.POTENTIAL_COMBINATION_OF, includeMisapplied, includeUnpublished, null);
        assertEquals(2, inferredSynonyms.size(), "There should be 2 potential combination synonyms (SynOfAcherontia ciprosus, SynOfAcherontia balosus)");
        Synonym syn1 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia ciprosus")).findFirst().get();
        assertEquals("SynOfAcherontia ciprosus syn. sec. Sp. Pl.", syn1.getTitleCache(), "the name of potential combination should be SynOfAcherontia ciprosus");
        Synonym syn2 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia balosus")).findFirst().get();
        assertEquals("SynOfAcherontia balosus syn. sec. Sp. Pl.", syn2.getTitleCache(), "the name of potential combination should be SynOfAcherontia balosus");
        commitAndStartNewTransaction();

        //...with MAN
        includeMisapplied = true;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.POTENTIAL_COMBINATION_OF, includeMisapplied, includeUnpublished, null);
//        inferredSynonyms.forEach(s->System.out.println(s.getTitleCache()));
        assertEquals(3, inferredSynonyms.size(), "There should be 3 potential combination synonyms (SynOfAcherontia ciprosus, SynOfAcherontia balosus)");
        syn1 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia ciprosus")).findFirst().get();
        assertEquals("SynOfAcherontia ciprosus syn. sec. Sp. Pl.", syn1.getTitleCache(), "the name of potential combination should be SynOfAcherontia ciprosus");
        assertEquals(1, syn1.getSources().size());

        syn2 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia balosus")).findFirst().get();
        assertEquals("SynOfAcherontia balosus syn. sec. Sp. Pl.", syn2.getTitleCache(), "the name of potential combination should be SynOfAcherontia balosus");
        assertEquals(1, syn2.getSources().size());

        Synonym syn3 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("SynOfAcherontia erronus")).findFirst().get();
        assertEquals("SynOfAcherontia erronus syn. sec. Sp. Pl.", syn3.getTitleCache(), "the name of potential combination should be Heterontia erronus");
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
    @CdmDataSet
    public void testComputeInferredSynonymySubspecies(){

        //subspecies
        Taxon taxon = (Taxon)taxonService.find(acherontiaLachesisSublachesisTaxonUuid);
        UUID taxonUuid = acherontiaLachesisSublachesisTaxonUuid;

        //... inferred epithet
        includeMisapplied = false;
        List<Synonym> inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid,
                classificationUuid, SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished,
                null);
        assertEquals(2, inferredSynonyms.size(), "There should be 2 inferred epithet synonym");
        Set<String> caches = inferredSynonyms.stream().map(s->s.getTitleCache()).collect(Collectors.toSet());
        assertTrue(caches.contains("Acherontia ciprosus subsp. sublachesis syn. sec. Sp. Pl."), "Should contain Acherontia ciprosus subsp. sublachesis");
        assertTrue(caches.contains("Heterontia balosus subsp. sublachesis syn. sec. Sp. Pl."), "Should contain Heterontia balosus subsp. sublachesis");

        //... with MAN
        commitAndStartNewTransaction();
        includeMisapplied = true;
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished, null);
        assertEquals(3, inferredSynonyms.size(), "There should be 3 inferred epithet synonym");

        //.... inferred genus
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_GENUS_OF, includeMisapplied, includeUnpublished, null);
        assertNotNull(inferredSynonyms, "there should be a new synonym ");
        assertEquals(1, inferredSynonyms.size(), "There should be 1 inferred genus synonym (note: Acherontia lachensis should not be inferred as it is a duplicate for an existing synonym");
        assertEquals("Acherontia lachesis subsp. subsynonus syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache(), "the name of inferred genus should be Acherontia lachesis");

        //... inferred combination
        inferredSynonyms = taxonService.createInferredSynonyms(taxon, classification,
                SynonymType.POTENTIAL_COMBINATION_OF, includeMisapplied);
        assertNotNull(inferredSynonyms, "there should be a new synonym ");
//        inferredSynonyms.stream().map(s->s.getTitleCache()).forEach(n->System.out.println(n));
        assertEquals(3, inferredSynonyms.size(), "There should be 3 potential combination synonyms");
        Synonym syn1 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("Acherontia ciprosus subsp. subsynonus")).findFirst().get();
        assertEquals("Acherontia ciprosus subsp. subsynonus syn. sec. Sp. Pl.", syn1.getTitleCache(), "the name of potential combination should be Acherontia ciprosus subsp. subsynonus");
        Synonym syn2 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("Heterontia balosus subsp. subsynonus")).findFirst().get();
        assertEquals("Heterontia balosus subsp. subsynonus syn. sec. Sp. Pl.", syn2.getTitleCache(), "the name of potential combination should be Heterontia balosus subsp. subsynonus");
        Synonym syn3 = inferredSynonyms.stream().filter(s->s.getName().getNameCache().equals("Misapplicatus erronus subsp. subsynonus")).findFirst().get();
        assertEquals("Misapplicatus erronus subsp. subsynonus syn. sec. Sp. Pl.", syn3.getTitleCache(), "the name of potential combination should be Misapplicatus erronus subsp. subsynonus");
    }

    @Test
    @CdmDataSet
    public void testComputeInferredSynonymyInfrageneric(){

        //infrageneric taxon
        UUID acherontiaBonsaTaxonUuid = UUID.fromString("46235ca5-3ee0-45e5-aed5-846081a0a528");
        UUID taxonUuid = acherontiaBonsaTaxonUuid;

        //create data
        UUID acherontiaLachesisTaxonUuid = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");
        Taxon species = (Taxon)taxonService.find(acherontiaLachesisTaxonUuid);
        TaxonNode speciesNode = species.getTaxonNode(classification);
        Taxon infraGenTaxon = (Taxon)taxonService.find(acherontiaBonsaTaxonUuid);
        TaxonNode newChild = speciesNode.getParent().addChildTaxon(infraGenTaxon, null);
        newChild.addChildNode(speciesNode, null, null);
        taxonNodeService.save(newChild);

        //... inferred epithet
        List<Synonym> inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid,
                classificationUuid, SynonymType.INFERRED_EPITHET_OF, includeMisapplied, includeUnpublished,
                null);
        assertNotNull(inferredSynonyms, "there should be a new synonym ");
        assertEquals(1, inferredSynonyms.size(), "There should be 1 inferred epithet synonym");
        assertEquals("SynOfAcherontia subg. Bonsa syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache(), "the name of inferred epithet should be SynOfAcherontia subg. Bonsa");

        //.... inferred genus
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.INFERRED_GENUS_OF, includeMisapplied, includeUnpublished, null);
        assertNotNull(inferredSynonyms, "there should be a new synonym ");
        assertEquals(1, inferredSynonyms.size(), "There should be 1 inferred genus synonym");
        assertEquals("Acherontia subg. Synbonsa syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache(), "the name of inferred genus should be Acherontia subg. Synbonsa");

        //... inferred combination
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(taxonUuid, classificationUuid,
                SynonymType.POTENTIAL_COMBINATION_OF, includeUnpublished, includeUnpublished, null);
        assertNotNull(inferredSynonyms, "there should be a new synonym ");
//        inferredSynonyms.stream().map(s->s.getTitleCache()).forEach(n->System.out.println(n));
        assertEquals(1, inferredSynonyms.size(), "There should be 1 potential combination");
        assertEquals("SynOfAcherontia subg. Synbonsa syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache(), "the name of potential combinatio should be SynOfAcherontia subg. Synbonsa");
    }

    @Test
    @CdmDataSet
    public void testCreateInferredSynonyms_PersistAndDeduplicate(){

        //verify expected DB state
        List<Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(5, synonyms.size(), "Number of synonyms should be 5");

        //inferred epithet
        includeMisapplied = false;
        List<Synonym> inferredSynonyms = inferredSynonymService.createInferredSynonyms(
                acherontiaLachesisTaxonUuid,
                classificationUuid, SynonymType.INFERRED_GENUS_OF, includeMisapplied,
                includeUnpublished, null);
        assertEquals(1, inferredSynonyms.size(), "There should be 1 new synonym");
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(6, synonyms.size(), "Number of synonyms should be 6 now");

//        Set<String> caches = inferredSynonymService.getDistinctNameCaches();
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(
                acherontiaLachesisTaxonUuid,
                classificationUuid, SynonymType.INFERRED_GENUS_OF, includeMisapplied,
                includeUnpublished, null);
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(6, synonyms.size(), "Number of synonyms should be 6 now");
        assertEquals(0, inferredSynonyms.size(), "The new synonym should not be created again");

        //...with MAN
        includeMisapplied = true;
        inferredSynonyms = inferredSynonymService.createInferredSynonyms(
                acherontiaLachesisTaxonUuid,
                classificationUuid, SynonymType.INFERRED_GENUS_OF, includeMisapplied, true, null);
        assertEquals(1, inferredSynonyms.size(), "There should be 1 new synonym");
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(7, synonyms.size(), "Number of synonyms should be 7 now");

        //... and again (no new record should be created)
        inferredSynonyms = inferredSynonymService.computeInferredSynonyms(
                acherontiaLachesisTaxonUuid,
                classificationUuid, SynonymType.INFERRED_GENUS_OF, includeMisapplied,
                includeUnpublished, null);
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(7, synonyms.size(), "Number of synonyms should be 7 now");
        assertEquals(0, inferredSynonyms.size(), "The new synonym should not be created again");
    }

    @Test
    @CdmDataSet
    public void testCreateAllInferredSynonyms_PersistAndDeduplicate(){

        //verify expected DB state
        List<Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(5, synonyms.size(), "Number of synonyms should be 5");

        //inferred epithet
        boolean doPersist = false;
        List<Synonym> inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, null, doPersist);
        assertEquals(6, inferredSynonyms.size(), "There should be 6 new synonyms");
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(5, synonyms.size(), "Number of synonyms should still be 5");
        commitAndStartNewTransaction();
////        Set<String> caches = inferredSynonymService.getDistinctNameCaches();
//        inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
//                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
//                includeUnpublished, null, doPersist);
//        synonyms = taxonService.list(Synonym.class, null, null, null, null);
//        assertEquals(11, synonyms.size(), "Number of synonyms should still be 11 now");
//        assertEquals(0, inferredSynonyms.size(), "The new synonym should not be created again");
//        commitAndStartNewTransaction();

        //...with persist
        doPersist = true;
        inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, null, doPersist);
        assertEquals(6, inferredSynonyms.size(), "There should be 6 new synonyms");
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(11, synonyms.size(), "Number of synonyms should be 11 now");
        commitAndStartNewTransaction();

        //... and again (no new record should be created)
        inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, null, doPersist);
        synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(11, synonyms.size(), "Number of synonyms should still be 11");
        assertEquals(0, inferredSynonyms.size(), "The new synonym should not be created again");
    }

    @Test
    @CdmDataSet
    public void testCreateAllInferredSynonymsWithGetDistinctNameCaches(){

        //verify expected DB state
        List<Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals(5, synonyms.size(), "Number of synonyms should be 5");

        //without cache (default behaviour)
        boolean doPersist = false;
        Set<String> caches = null;
        List<Synonym> inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, caches, doPersist);
        assertEquals(6, inferredSynonyms.size(), "There should be 6 new synonyms");
        commitAndStartNewTransaction();

        //with cache
        caches = inferredSynonymService.getDistinctNameCaches();
//        caches.forEach(c->System.out.println(c));
        assertEquals(9, caches.size(), "There should be 9 distinct nameCaches for names below genus");

        caches.add("SynOfAcherontia lachesis");
        doPersist = true;
        inferredSynonyms = inferredSynonymService.createAllInferredSynonyms(
                acherontiaLachesisTaxonUuid, classificationUuid, includeMisapplied,
                includeUnpublished, caches, doPersist);
        assertEquals(5, inferredSynonyms.size(), "There should be only 5 new synonyms now");
    }

    @Override
    @CdmDataSet(cleanSweep = true, value = "ClearDBDataSet.xml")
    public void createTestDataSet() throws FileNotFoundException {
    }
}