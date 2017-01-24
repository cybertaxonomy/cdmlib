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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.FindTaxaAndNamesConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.ICdmMassIndexer;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @created 04.02.2009
 */
public class TaxonServiceSearchTaxaAndNamesTest extends CdmTransactionalIntegrationTest {

    private static Logger logger = Logger.getLogger(TaxonServiceSearchTaxaAndNamesTest.class);

    private static final String ABIES_BALSAMEA_UUID = "f65d47bd-4f49-4ab1-bc4a-bc4551eaa1a8";

    private static final String ABIES_ALBA_UUID = "7dbd5810-a3e5-44b6-b563-25152b8867f4";

    private static final String CLASSIFICATION_UUID = "2a5ceebb-4830-4524-b330-78461bf8cb6b";

    private static final String CLASSIFICATION_ALT_UUID = "d7c741e3-ae9e-4a7d-a566-9e3a7a0b51ce";

    private static final String D_ABIES_BALSAMEA_UUID = "900108d8-e6ce-495e-b32e-7aad3099135e";

    private static final String D_ABIES_ALBA_UUID = "ec8bba03-d993-4c85-8472-18b14942464b";

    private static final String D_ABIES_KAWAKAMII_SEC_KOMAROV_UUID = "e9d8c2fd-6409-46d5-9c2e-14a2bbb1b2b1";

    @SpringBeanByType
    private ITaxonService taxonService;
    @SpringBeanByType
    private ITermService termService;
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

    @SpringBeanByType
    private ITaxonNodeService nodeService;


    private NamedArea germany;
    private NamedArea france ;
    private NamedArea russia ;
    private NamedArea canada ;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        germany =  Country.GERMANY();
        france = Country.FRANCEFRENCHREPUBLIC();
        russia = Country.RUSSIANFEDERATION();
        canada = Country.CANADA();
    }

    @Test
    public void testDbUnitUsageTest() throws Exception {

        assertNotNull("taxonService should exist", taxonService);
        assertNotNull("nameService should exist", nameService);
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator)}
     * .
     */
    /**
     * This test permutes through all search mode combinations.
     *
     * 7 Accepted Taxa
     * 1 Synonym
     * 1 misapplied which is also in the set of accepted
     * 2 Names without taxa
     *
     */
    @Test
    @DataSet
    public final void testFindTaxaAndNames() {

        IFindTaxaAndNamesConfigurator<?> conf = new FindTaxaAndNamesConfiguratorImpl();
        conf.setTitleSearchString("Abies*");
        conf.setMatchMode(MatchMode.BEGINNING);

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, true, true, true);
        Pager<IdentifiableEntity> pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(10, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, true, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(4, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, true, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(9, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, true, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        //logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, false, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(10, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, false, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals("one synonym, two names without taxa and a misapplied name", 4, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, false, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(9, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, false, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(3, pager.getRecords().size());

        // =========================================================

        setTaxaAndNamesModes(conf, true, true, true, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        // logSearchResults(pager, Level.DEBUG);
        assertEquals(10, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, true, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, true, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(9, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, true, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, false, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(10, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, false, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, false, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(9, pager.getRecords().size());

        // only names without taxa
        // - Abies borisii-regis
        // - Abies lasio
        setTaxaAndNamesModes(conf, false, false, false, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        // =========================================================

        setTaxaAndNamesModes(conf, true, true, true, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(8, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, true, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals("one synonym and a misapplied name", 2, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, true, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(7, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, true, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, false, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(8, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, false, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, false, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(7, pager.getRecords().size());

        // only misapplied names
        // - Abies kawakamii sec. Komarov, V. L., Flora SSSR 29
        setTaxaAndNamesModes(conf, false, false, false, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        // =========================================================

        setTaxaAndNamesModes(conf, true, true, true, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(8, pager.getRecords().size());


        setTaxaAndNamesModes(conf, false, true, true, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, true, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(7, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, true, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(0, pager.getRecords().size());

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, false, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(8, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, false, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, false, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(7, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, false, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(0, pager.getRecords().size());
    }


    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator)}
     * .
     */
    /**
     * This test permutes through all search mode combinations with classification filter
     *
     * 1 accepted taxon
     * 1 Synonym
     * 1 misapplied
     * 2 names without taxa
     *
     */
    @Test
    @DataSet
    public final void testFindTaxaAndNames_with_classification() {

        IFindTaxaAndNamesConfigurator<?> conf = new FindTaxaAndNamesConfiguratorImpl();
        conf.setTitleSearchString("Abies*");
        conf.setClassification(classificationService.load(UUID.fromString(CLASSIFICATION_UUID)));
        conf.setMatchMode(MatchMode.BEGINNING);

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, true, true, true);
        Pager<IdentifiableEntity> pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(5, pager.getRecords().size());


        setTaxaAndNamesModes(conf, false, true, true, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(4, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, true, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(4, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, true, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        //logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, false, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(5, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, false, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(4, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, false, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(4, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, false, true, true);
        pager = taxonService.findTaxaAndNames(conf);
        assertEquals(3, pager.getRecords().size());

        // =========================================================

        setTaxaAndNamesModes(conf, true, true, true, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        // logSearchResults(pager, Level.DEBUG);
        assertEquals(4, pager.getRecords().size());


        setTaxaAndNamesModes(conf, false, true, true, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, true, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, true, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, false, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(4, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, false, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, false, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        // only names without taxa
        // - Abies borisii-regis
        // - Abies lasio
        setTaxaAndNamesModes(conf, false, false, false, false, true);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        // =========================================================

        setTaxaAndNamesModes(conf, true, true, true, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());


        setTaxaAndNamesModes(conf, false, true, true, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, true, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, true, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, false, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(3, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, false, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, false, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        // only misapplied names
        // - Abies kawakamii sec. Komarov, V. L., Flora SSSR 29
        setTaxaAndNamesModes(conf, false, false, false, true, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        // =========================================================

        setTaxaAndNamesModes(conf, true, true, true, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());


        setTaxaAndNamesModes(conf, false, true, true, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, true, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, true, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(0, pager.getRecords().size());

        // ---------------------------------------------------------

        setTaxaAndNamesModes(conf, true, true, false, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(2, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, true, false, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        setTaxaAndNamesModes(conf, true, false, false, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(1, pager.getRecords().size());

        setTaxaAndNamesModes(conf, false, false, false, false, false);
        pager = taxonService.findTaxaAndNames(conf);
        logSearchResults(pager, Level.DEBUG);
        assertEquals(0, pager.getRecords().size());
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator)}
     * .
     */
    @Test
    @DataSet
    public final void testFindTaxaAndNames_CommonName() {
     // pass 1
        IFindTaxaAndNamesConfigurator<?> configurator = new FindTaxaAndNamesConfiguratorImpl();
        configurator.setMatchMode(MatchMode.BEGINNING);
        configurator.setDoTaxa(true);
        configurator.setDoSynonyms(true);
        configurator.setDoNamesWithoutTaxa(true);
        configurator.setDoTaxaByCommonNames(true);
        configurator.setTitleSearchString("Balsam-Tanne");

        Pager<IdentifiableEntity> pager = taxonService.findTaxaAndNames(configurator);

        List<IdentifiableEntity> list = pager.getRecords();
        assertEquals(1, list.size());
        configurator.setTitleSearchString("Abies*");
        configurator.setDoTaxa(false);
        configurator.setDoSynonyms(false);
        configurator.setDoNamesWithoutTaxa(false);
        configurator.setDoTaxaByCommonNames(true);
        configurator.setDoMisappliedNames(true);
        configurator.setClassification(classificationService.load(UUID.fromString(CLASSIFICATION_UUID)));
        pager = taxonService.findTaxaAndNames(configurator);
        list = pager.getRecords();
        assertEquals(1, list.size());

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator)}
     * .
     */
    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public final void testFindTaxaAndNamesWithHybridFormula() {

        // pass 1
        IFindTaxaAndNamesConfigurator<?> configurator = new FindTaxaAndNamesConfiguratorImpl();
        configurator.setTitleSearchString("Achillea*");
        configurator.setMatchMode(MatchMode.BEGINNING);
        configurator.setDoTaxa(true);
        configurator.setDoSynonyms(true);
        configurator.setDoNamesWithoutTaxa(true);
        configurator.setDoTaxaByCommonNames(true);

        Pager<IdentifiableEntity> pager = taxonService.findTaxaAndNames(configurator);
    //    Assert.assertEquals("Expecting one taxon",1,pager.getRecords().size());
        List<IdentifiableEntity> list = pager.getRecords();
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#searchTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}
     * .
     */
    @Test
    @DataSet
    public final void testfindTaxaAndNamesForEditor() {
         IFindTaxaAndNamesConfigurator<?> configurator = new FindTaxaAndNamesConfiguratorImpl();
         configurator.setTitleSearchString("Abies bor*");
         configurator.setMatchMode(MatchMode.BEGINNING);
         configurator.setDoTaxa(true);
         configurator.setDoSynonyms(false);
         configurator.setDoNamesWithoutTaxa(true);
         configurator.setDoTaxaByCommonNames(false);

        List<UuidAndTitleCache<IdentifiableEntity>> list = taxonService.findTaxaAndNamesForEditor(configurator);

         Assert.assertEquals("Expecting one entity", 1, list.size());

         configurator.setTitleSearchString("silver fir");
         configurator.setMatchMode(MatchMode.BEGINNING);
         configurator.setDoTaxa(false);
         configurator.setDoSynonyms(false);
         configurator.setDoNamesWithoutTaxa(true);
         configurator.setDoTaxaByCommonNames(true);

         list = taxonService.findTaxaAndNamesForEditor(configurator);

         Assert.assertEquals("Expecting one entity", 1, list.size());

    }


    /**
     *
     * @param conf
     * @param doTaxa
     * @param doSynonyms
     * @param doCommonNames
     * @param doMisappliedNames
     * @param doNamesWithoutTaxa
     * @return
     */
    protected IFindTaxaAndNamesConfigurator<?> setTaxaAndNamesModes(
            IFindTaxaAndNamesConfigurator<?> conf,
            boolean doTaxa,
            boolean doSynonyms,
            boolean doCommonNames,
            boolean doMisappliedNames,
            boolean doNamesWithoutTaxa){

        conf.setDoTaxa(doTaxa);
        conf.setDoSynonyms(doSynonyms);
        conf.setDoTaxaByCommonNames(doCommonNames);
        conf.setDoMisappliedNames(doMisappliedNames);
        conf.setDoNamesWithoutTaxa(doNamesWithoutTaxa);

        return conf;
    }

    /**
     * @param pager
     */
    protected void logSearchResults(Pager<IdentifiableEntity> pager, Level level) {
        List<IdentifiableEntity> list = pager.getRecords();
        logger.debug("number of taxa: " + list.size());

        if (logger.isDebugEnabled()) {
            for (int i = 0; i < list.size(); i++) {
                String nameCache = "";
                if (list.get(i) instanceof NonViralName) {
                    nameCache = ((NonViralName<?>) list.get(i)).getNameCache();
                } else if (list.get(i) instanceof TaxonBase) {
                    TaxonNameBase<?,?> taxonNameBase = ((TaxonBase) list.get(i)).getName();
                    nameCache = HibernateProxyHelper.deproxy(taxonNameBase, NonViralName.class).getNameCache();
                } else {
                }
                logger.log(level, list.get(i).getClass() + "(" + i + ")" + ": Name Cache = " + nameCache + ", Title Cache = "
                        + list.get(i).getTitleCache());
            }
        }
    }

    /**
     * uncomment @Test annotation to create the dataset for this test
     */
    @Override
    // @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="BlankDataSet.xml")
    public final void createTestDataSet() throws FileNotFoundException {

        Classification europeanAbiesClassification = Classification.NewInstance("European Abies");
        europeanAbiesClassification.setUuid(UUID.fromString(CLASSIFICATION_UUID));
        classificationService.save(europeanAbiesClassification);

        Classification alternativeClassification = Classification.NewInstance("Abies alternative");
        alternativeClassification.setUuid(UUID.fromString(CLASSIFICATION_ALT_UUID));
        classificationService.save(alternativeClassification);

        Reference sec = ReferenceFactory.newBook();
        sec.setTitleCache("Kohlbecker, A., Testcase standart views, 2013", true);
        Reference sec_sensu = ReferenceFactory.newBook();
        sec_sensu.setTitleCache("Komarov, V. L., Flora SSSR 29", true);
        referenceService.save(sec);
        referenceService.save(sec_sensu);

        BotanicalName n_abies = BotanicalName.NewInstance(Rank.GENUS());
        n_abies.setNameCache("Abies", true);
        Taxon t_abies = Taxon.NewInstance(n_abies, sec);
        taxonService.save(t_abies);

        BotanicalName n_abies_alba = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_alba.setNameCache("Abies alba", true);
        Taxon t_abies_alba = Taxon.NewInstance(n_abies_alba, sec);
        t_abies_alba.setUuid(UUID.fromString(ABIES_ALBA_UUID));
        taxonService.save(t_abies_alba);

        BotanicalName n_abies_subalpina = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_subalpina.setNameCache("Abies subalpina", true);
        Synonym s_abies_subalpina = Synonym.NewInstance(n_abies_subalpina, sec);
        taxonService.save(s_abies_subalpina);

        BotanicalName n_abies_balsamea = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_balsamea.setNameCache("Abies balsamea", true);
        Taxon t_abies_balsamea = Taxon.NewInstance(n_abies_balsamea, sec);
        t_abies_balsamea.setUuid(UUID.fromString(ABIES_BALSAMEA_UUID));
        t_abies_balsamea.addSynonym(s_abies_subalpina, SynonymType.SYNONYM_OF());
        taxonService.save(t_abies_balsamea);

        BotanicalName n_abies_grandis = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_grandis.setNameCache("Abies grandis", true);
        Taxon t_abies_grandis = Taxon.NewInstance(n_abies_grandis, sec);
        taxonService.save(t_abies_grandis);

        BotanicalName n_abies_kawakamii = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_kawakamii.setNameCache("Abies kawakamii", true);
        Taxon t_abies_kawakamii = Taxon.NewInstance(n_abies_kawakamii, sec);
        t_abies_kawakamii.getTitleCache();
        taxonService.save(t_abies_kawakamii);

        //
        // 1 Misapplied Name
        //
        // abies_kawakamii_sensu_komarov as missapplied name for t_abies_balsamea
        Taxon t_abies_kawakamii_sensu_komarov = Taxon.NewInstance(n_abies_kawakamii, sec_sensu);
        taxonService.save(t_abies_kawakamii_sensu_komarov);
        t_abies_kawakamii_sensu_komarov.addTaxonRelation(t_abies_balsamea, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null);
        taxonService.saveOrUpdate(t_abies_kawakamii_sensu_komarov);

        BotanicalName n_abies_lasiocarpa = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_lasiocarpa.setNameCache("Abies lasiocarpa", true);
        Taxon t_abies_lasiocarpa = Taxon.NewInstance(n_abies_lasiocarpa, sec);
        taxonService.save(t_abies_lasiocarpa);

        // add 3 taxa to classifications
        europeanAbiesClassification.addChildTaxon(t_abies_balsamea, null, null);
        alternativeClassification.addChildTaxon(t_abies_lasiocarpa, null, null);
        classificationService.saveOrUpdate(europeanAbiesClassification);
        classificationService.saveOrUpdate(alternativeClassification);

        //
        // 2 Names without taxa
        //
        BotanicalName n_abies_borisiiregis = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_borisiiregis.setNameCache("Abies borisii-regis", true);
        nameService.save(n_abies_borisiiregis);

        BotanicalName n_abies_lasio = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_lasio.setNameCache("Abies lasio", true);
        nameService.save(n_abies_lasio);

        // A hybrid name not from Abies
        BotanicalName n_abies_millefolium_x_Achillea_nobilis = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_millefolium_x_Achillea_nobilis.setNameCache("Achillea millefolium × Achillea nobilis", true);
        Taxon t_abies_millefolium_x_Achillea_nobilis = Taxon.NewInstance(n_abies_millefolium_x_Achillea_nobilis, sec);
        taxonService.save(t_abies_millefolium_x_Achillea_nobilis);

        //
        // Description
        //
        TaxonDescription d_abies_alba = TaxonDescription.NewInstance(t_abies_alba);
        TaxonDescription d_abies_balsamea = TaxonDescription.NewInstance(t_abies_balsamea);

        d_abies_alba.setUuid(UUID.fromString(D_ABIES_ALBA_UUID));
        d_abies_balsamea.setUuid(UUID.fromString(D_ABIES_BALSAMEA_UUID));


        // CommonTaxonName
        d_abies_alba.addElement(CommonTaxonName.NewInstance("Wei"+UTF8.SHARP_S+"tanne", Language.GERMAN()));
        d_abies_alba.addElement(CommonTaxonName.NewInstance("silver fir", Language.ENGLISH()));
        d_abies_alba.addElement(Distribution
                .NewInstance(
                        germany,
                        PresenceAbsenceTerm.NATIVE()));
        d_abies_alba.addElement(Distribution
                .NewInstance(
                        russia,
                        PresenceAbsenceTerm.ABSENT()));

        // TextData
        d_abies_balsamea
            .addElement(TextData
                    .NewInstance(
                            "Die Balsam-Tanne (Abies balsamea) ist eine Pflanzenart aus der Gattung der Tannen (Abies). Sie wÃ¤chst im nordÃ¶stlichen Nordamerika, wo sie sowohl Tief- als auch Bergland besiedelt. Sie gilt als relativ anspruchslos gegenÃ¼ber dem Standort und ist frosthart. In vielen Teilen des natÃ¼rlichen Verbreitungsgebietes stellt sie die Klimaxbaumart dar.",
                            Language.GERMAN(), null));
        d_abies_balsamea
        .addElement(CommonTaxonName
                .NewInstance(
                        "Balsam-Tanne",
                        Language.GERMAN(), null));

        d_abies_balsamea
        .addElement(Distribution
                .NewInstance(
                        canada,
                        PresenceAbsenceTerm.PRESENT()));

        d_abies_balsamea
        .addElement(Distribution
                .NewInstance(
                        germany,
                        PresenceAbsenceTerm.NATIVE()));

        d_abies_balsamea
                .addElement(TextData
                        .NewInstance(
                                TaxonServiceSearchTestUtf8Constants.RUSSIAN_ABIES_ALBA_LONG,
                                Language.RUSSIAN(), null));
        d_abies_balsamea
        .addElement(CommonTaxonName
                .NewInstance(
                        TaxonServiceSearchTestUtf8Constants.RUSSIAN_ABIES_ALBA_SHORT,
                        Language.RUSSIAN(), null));
        descriptionService.saveOrUpdate(d_abies_balsamea);

        setComplete();
        endTransaction();


        writeDbUnitDataSetFile(new String[] {
            "TAXONBASE", "TAXONNAMEBASE",
            "TAXONRELATIONSHIP",
            "REFERENCE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE",
            "AGENTBASE", "HOMOTYPICALGROUP",
            "CLASSIFICATION", "TAXONNODE",
            "LANGUAGESTRING", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            });

    }

}
