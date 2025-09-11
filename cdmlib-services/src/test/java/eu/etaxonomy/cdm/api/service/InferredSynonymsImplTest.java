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

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * Test for {@link InferredSynonymsServicImpl}
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
    private IClassificationService classificationService;

    /****************** TESTS *****************************/

    @Test
    @DataSet
    public void testCreateInferredSynonymy(){

        UUID classificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");
        Classification classification = classificationService.find(classificationUuid);

        //verify expected DB state
        List <Synonym> synonyms = taxonService.list(Synonym.class, null, null, null, null);
        assertEquals("Number of synonyms should be 2 (Acheontitia ciprosus and SynGenus)",
                2, synonyms.size());

        //load accepted species
        UUID acherontiaLachesisTaxonUuid = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");
        Taxon taxon = (Taxon)taxonService.find(acherontiaLachesisTaxonUuid);

        //inferred epithet
        List<Synonym> inferredSynonyms = taxonService.createInferredSynonyms(taxon, classification, SynonymType.INFERRED_EPITHET_OF, true);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals ("the name of inferred epithet should be SynOfAcherontia lachesis", "SynOfAcherontia lachesis syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());

        //inferred genus
        inferredSynonyms = taxonService.createInferredSynonyms(taxon, classification, SynonymType.INFERRED_GENUS_OF, true);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals ("the name of inferred genus should be Acherontia ciprosus", "Acherontia ciprosus syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());

        //inferred combination
        inferredSynonyms = taxonService.createInferredSynonyms(taxon, classification, SynonymType.POTENTIAL_COMBINATION_OF, true);
        assertNotNull("there should be a new synonym ", inferredSynonyms);
        assertEquals ("the name of potential combination should be SynOfAcherontia ciprosus", "SynOfAcherontia ciprosus syn. sec. Sp. Pl.", inferredSynonyms.get(0).getTitleCache());
        //assertTrue("set of synonyms should contain an inferred Synonym ", synonyms.contains(arg0))

        //TODO test cases with infrageneric names and subspecies

        //TODO deduplication if name exists already or if both exists, inferred epithet/genus and potential combination

        //TODO misapplied name handling

        //TODO test references, sources and idInSource (etc.)

        //TODO test that only zoological names return inferred synonyms
    }

    @Override
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="ClearDBDataSet.xml")
    public void createTestDataSet() throws FileNotFoundException {
    }
}