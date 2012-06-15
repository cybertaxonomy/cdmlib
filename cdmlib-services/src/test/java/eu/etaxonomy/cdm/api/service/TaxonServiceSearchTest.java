/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import com.mchange.util.AssertException;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.search.ICdmMassIndexer;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;


/**
 * @author a.babadshanjan, a.kohlbecker
 * @created 04.02.2009
 * @version 1.0
 */

public class TaxonServiceSearchTest extends CdmTransactionalIntegrationTest {

    private static final String CLASSIFICATION_UUID = "2a5ceebb-4830-4524-b330-78461bf8cb6b";

    private static final int NUM_OF_NEW_RADOM_ENTITIES = 1000;

    private static Logger logger = Logger.getLogger(TaxonServiceSearchTest.class);

    @SpringBeanByType
    private ITaxonService taxonService;
    @SpringBeanByType
    private IClassificationService classificationService;
    @SpringBeanByType
    private IReferenceService referenceService;
    @SpringBeanByType
    private IDescriptionService descriptionService;
    @SpringBeanByType
    private INameService nameService;
    @SpringBeanByType
    private ICdmMassIndexer indexer;

    private static final int BENCHMARK_ROUNDS = 300;

    @Test
    public void testDbUnitUsageTest() throws Exception {
        assertNotNull("taxonService should exist", taxonService);
        assertNotNull("nameService should exist", nameService);
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)}
     * .
     */
    @Test
    @DataSet
    public final void testFindTaxaAndNames() {

        // pass 1
        ITaxonServiceConfigurator configurator = new TaxonServiceConfiguratorImpl();
        configurator.setTitleSearchString("Abies*");
        configurator.setMatchMode(MatchMode.BEGINNING);
        configurator.setDoTaxa(true);
        configurator.setDoSynonyms(true);
        configurator.setDoNamesWithoutTaxa(true);
        configurator.setDoTaxaByCommonNames(true);

        Pager<IdentifiableEntity> pager = taxonService.findTaxaAndNames(configurator);
        List<IdentifiableEntity> list = pager.getRecords();

        if (logger.isDebugEnabled()) {
            for (int i = 0; i < list.size(); i++) {
                String nameCache = "";
                if (list.get(i) instanceof NonViralName) {
                    nameCache = ((NonViralName<?>) list.get(i)).getNameCache();
                } else if (list.get(i) instanceof TaxonBase) {
                    TaxonNameBase taxonNameBase = ((TaxonBase) list.get(i)).getName();
                    nameCache = ((NonViralName) taxonNameBase).getNameCache();
                } else {
                }
                logger.debug(list.get(i).getClass() + "(" + i + ")" + ": Name Cache = " + nameCache + ", Title Cache = "
                        + list.get(i).getTitleCache());
            }
        }

        logger.debug("number of taxa: " + list.size());
        assertEquals(7, list.size());

        // pass 2
//        configurator.setDoTaxaByCommonNames(false);
//        configurator.setDoMisappliedNames(true);
//        configurator.setClassification(classificationService.load(UUID.fromString(CLASSIFICATION_UUID)));
//        pager = taxonService.findTaxaAndNames(configurator);
//        list = pager.getRecords();
//        assertEquals(0, list.size());

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#searchTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}
     * .
     */
    @Test
    @DataSet
    public final void testSearchTaxaByName() {
        logger.warn("testSearchTaxaByName not yet implemented"); // TODO
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText() throws CorruptIndexException, IOException, ParseException {

//         printDataSet(System.out, new String[] {
//                 "DESCRIPTIONELEMENTBASE",
//                 "DESCRIPTIONBASE", "LANGUAGESTRING"});

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;
        pager = taxonService.findByDescriptionElementFullText(Distribution.class, "Abies", null, null, null, null);
        Assert.assertEquals("Expecting one entity", Integer.valueOf(1), pager.getCount());
        Assert.assertEquals("Abies alba sec. ", pager.getRecords().get(0).getEntity().getTitleCache());
        Assert.assertEquals("Abies alba sec. ", pager.getRecords().get(0).getDoc().get("inDescription.taxon.titleCache"));

        // modify the taxon
        TaxonBase taxon = pager.getRecords().get(0).getEntity();

        String newName = "Quercus robur";
        // NOTE setting the taxon.titleCache indirectly via the taxonName does
        // not work since this is not yet implemented into the CDM Library
        //
        // BotanicalName name = HibernateProxyHelper.deproxy(taxon.getName(),
        // BotanicalName.class);
        // name.setProtectedNameCache(false);
        // name.setGenusOrUninomial("Quercus");
        // name.setSpecificEpithet("robur");
        // name.setNameCache(newNameCache, true);
        // name.setFullTitleCache(newNameCache, true);
        // name.setTitleCache(newNameCache, true);
        //
        // ... so we will set it directly:
        taxon.setTitleCache(newName + " sec. ", true);

        taxonService.saveOrUpdate(taxon);
        commitAndStartNewTransaction(null);

        // printDataSet(System.err, new String[] {"TaxonBase", "TaxonNameBase"});

        taxon = taxonService.load(taxon.getUuid());
        Assert.assertEquals(newName + " sec. ", taxon.getTitleCache());

//        // test if new titleCache is found in the index, doc and entity
//        pager = taxonService.findByDescriptionElementFullText(DescriptionElementBase.class, "Weiß*", null, null, null, null);
//        Assert.assertEquals(newName + " sec. ", pager.getRecords().get(0).getEntity().getTitleCache());
//        Assert.assertEquals(newName + " sec. ", pager.getRecords().get(0).getDoc().get("inDescription.taxon.titleCache"));

    }

    /**
     *
     */
    private void refreshLuceneIndex() {

        commitAndStartNewTransaction(null);
        indexer.purge();
        indexer.reindex();
        commitAndStartNewTransaction(null);
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByCommonNameHqlBenchmark() throws CorruptIndexException, IOException, ParseException {

//        printDataSet(System.err, new String[] { "TaxonBase" });

        createRandomTaxonWithCommonName(NUM_OF_NEW_RADOM_ENTITIES);

        ITaxonServiceConfigurator configurator = new TaxonServiceConfiguratorImpl();
        configurator.setTitleSearchString("Weiß%");
        configurator.setMatchMode(MatchMode.BEGINNING);
        configurator.setDoTaxa(false);
        configurator.setDoSynonyms(false);
        configurator.setDoNamesWithoutTaxa(false);
        configurator.setDoTaxaByCommonNames(true);

        Pager<IdentifiableEntity> pager;

        long startMillis = System.currentTimeMillis();
        for (int indx = 0; indx < BENCHMARK_ROUNDS; indx++) {
            pager = taxonService.findTaxaAndNames(configurator);
            if (logger.isDebugEnabled()) {
                logger.debug("[" + indx + "]" + pager.getRecords().get(0).getTitleCache());
            }
        }
        double duration = ((double) (System.currentTimeMillis() - startMillis)) / BENCHMARK_ROUNDS;
        logger.info("Benchmark result - [find taxon by CommonName via HQL] : " + duration + "ms (" + BENCHMARK_ROUNDS + " benchmark rounds )");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByCommonNameLuceneBenchmark() throws CorruptIndexException, IOException, ParseException {

//        printDataSet(System.err, new String[] { "TaxonBase" });

        createRandomTaxonWithCommonName(NUM_OF_NEW_RADOM_ENTITIES);

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        long startMillis = System.currentTimeMillis();
        for (int indx = 0; indx < BENCHMARK_ROUNDS; indx++) {
            pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Weiß*", null, null, null, null);
            if (logger.isDebugEnabled()) {
                logger.debug("[" + indx + "]" + pager.getRecords().get(0).getEntity().getTitleCache());
            }
        }
        double duration = ((double) (System.currentTimeMillis() - startMillis)) / BENCHMARK_ROUNDS;
        logger.info("Benchmark result - [find taxon by CommonName via lucene] : " + duration + "ms (" + BENCHMARK_ROUNDS + " benchmark rounds )");
    }

    //@Test
    @DataSet("BlankDataSet.xml")
    public final void createDataSet() {

//        Classification classification = Classification.NewInstance("European Abies for testing");
//        classification.setUuid(UUID.fromString(CLASSIFICATION_UUID));
//        classificationService.save(classification);

        Reference sec = ReferenceFactory.newBook();
        referenceService.save(sec);

        BotanicalName n_abies = BotanicalName.NewInstance(Rank.GENUS());
        n_abies.setNameCache("Abies", true);
        Taxon t_abies = Taxon.NewInstance(n_abies, sec);
        taxonService.save(t_abies);

        BotanicalName n_abies_alba = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_alba.setNameCache("Abies alba", true);
        Taxon t_abies_alba = Taxon.NewInstance(n_abies_alba, sec);
        taxonService.save(t_abies_alba);

        BotanicalName n_abies_balsamea = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_balsamea.setNameCache("Abies balsamea", true);
        Taxon t_abies_balsamea = Taxon.NewInstance(n_abies_balsamea, sec);
        taxonService.save(t_abies_balsamea);

        BotanicalName n_abies_grandis = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_grandis.setNameCache("Abies grandis", true);
        Taxon t_abies_grandis = Taxon.NewInstance(n_abies_grandis, sec);
        taxonService.save(t_abies_grandis);

        BotanicalName n_abies_kawakamii = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_kawakamii.setNameCache("Abies kawakamii", true);
        Taxon t_abies_kawakamii = Taxon.NewInstance(n_abies_kawakamii, sec);
        taxonService.save(t_abies_kawakamii);

        BotanicalName n_abies_subalpina = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_subalpina.setNameCache("Abies subalpina", true);
        Synonym s_abies_subalpina = Synonym.NewInstance(n_abies_subalpina, sec);
        taxonService.save(s_abies_subalpina);

        BotanicalName n_abies_lasiocarpa = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_lasiocarpa.setNameCache("Abies lasiocarpa", true);
        Taxon t_abies_lasiocarpa = Taxon.NewInstance(n_abies_lasiocarpa, sec);
        t_abies_lasiocarpa.addSynonym(s_abies_subalpina, SynonymRelationshipType.SYNONYM_OF());
        taxonService.save(t_abies_lasiocarpa);

        TaxonDescription d_abies_alba = TaxonDescription.NewInstance(t_abies_alba);
        d_abies_alba.addElement(CommonTaxonName.NewInstance("Weißtanne", Language.GERMAN()));
        d_abies_alba.addElement(CommonTaxonName.NewInstance("silver fir", Language.ENGLISH()));

        TaxonDescription d_abies_balsamea = TaxonDescription.NewInstance(t_abies_balsamea);
        d_abies_balsamea
                .addElement(TextData
                        .NewInstance(
                                "Die Balsam-Tanne (Abies balsamea) ist eine Pflanzenart aus der Gattung der Tannen (Abies). Sie wächst im nordöstlichen Nordamerika, wo sie sowohl Tief- als auch Bergland besiedelt. Sie gilt als relativ anspruchslos gegenüber dem Standort und ist frosthart. In vielen Teilen des natürlichen Verbreitungsgebietes stellt sie die Klimaxbaumart dar.",
                                Language.GERMAN(), null));
        d_abies_balsamea
                .addElement(TextData
                        .NewInstance(
                                "Бальзам ньыв (лат. Abies balsamea) – быдмассэзлӧн пожум котырись ньыв увтырын торья вид. Ньывпуыс быдмӧ 14–20 метра вылына да овлӧ 10–60 см кыза диаметрын. Ньывпу пантасьӧ Ойвыв Америкаын.",
                                Language.RUSSIAN(), null));

        descriptionService.save(d_abies_alba);

        setComplete();
        endTransaction();

        printDataSet(System.out, new String[] { "TAXONBASE", "TAXONNAMEBASE", "SYNONYMRELATIONSHIP", "REFERENCE", "DESCRIPTIONELEMENTBASE",
                "DESCRIPTIONBASE", "AGENTBASE", "HOMOTYPICALGROUP"/*, "CLASSIFICATION", "LANGUAGESTRING", "DEFINEDTERMBASE"*/ });
        //TODO remove "DEFINEDTERMBASE" once term loading problems are fixed for tests, maybe with unitils 3.x, remove "DEFINEDTERMBASE"  also from the DataFile !!!
    }

    /**
     * @param numberOfNew
     *
     */
    private void createRandomTaxonWithCommonName(int numberOfNew) {

        logger.debug(String.format("creating %1$s random taxan with CommonName", numberOfNew));

        Reference sec = ReferenceFactory.newBook();
        referenceService.save(sec);

        for (int i = numberOfNew; i < numberOfNew; i++) {
            RandomStringUtils.randomAlphabetic(10);
            String radomName = RandomStringUtils.randomAlphabetic(5) + " " + RandomStringUtils.randomAlphabetic(10);
            String radomCommonName = RandomStringUtils.randomAlphabetic(10);

            BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
            name.setNameCache(radomName, true);
            Taxon taxon = Taxon.NewInstance(name, sec);
            taxonService.save(taxon);

            TaxonDescription description = TaxonDescription.NewInstance(taxon);
            description.addElement(CommonTaxonName.NewInstance(radomCommonName, Language.GERMAN()));
            descriptionService.save(description);
        }

        commitAndStartNewTransaction(null);

    }

}
