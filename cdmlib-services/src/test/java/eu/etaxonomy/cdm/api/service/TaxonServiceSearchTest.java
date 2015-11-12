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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.FindTaxaAndNamesConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.ICdmMassIndexer;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearchException;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
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
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.babadshanjan, a.kohlbecker
 * @created 04.02.2009
 */
@Ignore
public class TaxonServiceSearchTest extends CdmTransactionalIntegrationTest {

    private static final String ABIES_BALSAMEA_UUID = "f65d47bd-4f49-4ab1-bc4a-bc4551eaa1a8";

    private static final String ABIES_ALBA_UUID = "7dbd5810-a3e5-44b6-b563-25152b8867f4";

    private static final String CLASSIFICATION_UUID = "2a5ceebb-4830-4524-b330-78461bf8cb6b";

    private static final String CLASSIFICATION_ALT_UUID = "d7c741e3-ae9e-4a7d-a566-9e3a7a0b51ce";

    private static final String D_ABIES_BALSAMEA_UUID = "900108d8-e6ce-495e-b32e-7aad3099135e";

    private static final String D_ABIES_ALBA_UUID = "ec8bba03-d993-4c85-8472-18b14942464b";

    private static final String D_ABIES_KAWAKAMII_SEC_KOMAROV_UUID = "e9d8c2fd-6409-46d5-9c2e-14a2bbb1b2b1";
    private static final int NUM_OF_NEW_RADOM_ENTITIES = 1000;

    private static Logger logger = Logger.getLogger(TaxonServiceSearchTest.class);



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

    private static final int BENCHMARK_ROUNDS = 300;

    private Set<Class<? extends CdmBase>> typesToIndex = null;

    private NamedArea germany;
    private NamedArea france ;
    private NamedArea russia ;
    private NamedArea canada ;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        typesToIndex = new HashSet<Class<? extends CdmBase>>();
        typesToIndex.add(DescriptionElementBase.class);
        typesToIndex.add(TaxonBase.class);
        typesToIndex.add(TaxonRelationship.class);

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
    @Test
    @DataSet
    public final void testFindTaxaAndNames() {

        // pass 1
        IFindTaxaAndNamesConfigurator<?> configurator = new FindTaxaAndNamesConfiguratorImpl();
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
                    TaxonNameBase<?,?> taxonNameBase = ((TaxonBase) list.get(i)).getName();
                    nameCache = HibernateProxyHelper.deproxy(taxonNameBase, NonViralName.class).getNameCache();
                } else {
                }
                logger.debug(list.get(i).getClass() + "(" + i + ")" + ": Name Cache = " + nameCache + ", Title Cache = "
                        + list.get(i).getTitleCache());
            }
        }

        logger.debug("number of taxa: " + list.size());
        assertEquals(9, list.size());
        configurator.setTitleSearchString("Balsam-Tanne");
        pager = taxonService.findTaxaAndNames(configurator);
        list = pager.getRecords();
        assertEquals(1, list.size());
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
         IFindTaxaAndNamesConfigurator<?> configurator = new FindTaxaAndNamesConfiguratorImpl();
         configurator.setTitleSearchString("Abies*");
         configurator.setMatchMode(MatchMode.BEGINNING);
         configurator.setDoTaxa(false);
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

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testPurgeIndex() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByFullText(null, "Abies", null, null, true, null, null, null, null); // --> 7
        Assert.assertEquals("Expecting 8 entities", Integer.valueOf(8), pager.getCount());

        indexer.purge(null);
        commitAndStartNewTransaction(null);

        pager = taxonService.findByFullText(null, "Abies", null, null, true, null, null, null, null); // --> 0
        Assert.assertEquals("Expecting no entities since the index has been purged", Integer.valueOf(0), pager.getCount());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_CommonName() throws CorruptIndexException, IOException,
            ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"tanne", null, null, null,
                false, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching for CommonTaxonName", Integer.valueOf(1),
                pager.getCount());

        // the description containing the Nulltanne has no taxon attached,
        // taxon.id = null
        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Nulltanne", null, null, null,
                false, null, null, null, null);
        Assert.assertEquals("Expecting no entity when searching for 'Nulltanne' ", Integer.valueOf(0), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"tanne", null, null,
                Arrays.asList(new Language[] { Language.GERMAN() }), false, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching in German", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"tanne", null, null,
                Arrays.asList(new Language[] { Language.RUSSIAN() }), false, null, null, null, null);
        Assert.assertEquals("Expecting no entity when searching in Russian", Integer.valueOf(0), pager.getCount());

    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_Distribution() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;
        // by Area
        pager = taxonService.findByDescriptionElementFullText(null, "Canada", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching for arae 'Canada'", Integer.valueOf(1), pager.getCount());
        // by Status
        pager = taxonService.findByDescriptionElementFullText(null, "present", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching for status 'present'", Integer.valueOf(1), pager.getCount());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_wildcard() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"*", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching for CommonTaxonName", Integer.valueOf(1), pager.getCount());
    }

    /**
     * Regression test for #3113 (hibernate search: wildcard query can cause BooleanQuery$TooManyClauses: maxClauseCount is set to 1024)
     *
     * @throws CorruptIndexException
     * @throws IOException
     * @throws ParseException
     */
    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_TooManyClauses() throws CorruptIndexException, IOException, ParseException {

        // generate 1024 terms to reproduce the bug
        TaxonDescription description = (TaxonDescription) descriptionService.find(UUID.fromString(D_ABIES_ALBA_UUID));
        Set<String> uniqueRandomStrs = new HashSet<String>(1024);
        while(uniqueRandomStrs.size() < 1024){
            uniqueRandomStrs.add(RandomStringUtils.random(10, true, false));
        }
        for(String rndStr: uniqueRandomStrs){
            description.addElement(CommonTaxonName.NewInstance("Rot" + rndStr, Language.DEFAULT()));
        }
        descriptionService.saveOrUpdate(description);
        commitAndStartNewTransaction(null);

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Expecting all 1024 entities grouped into one SearchResult item when searching for Rot*", 1, pager.getCount().intValue());
    }

    /**
     * Regression test for #3116 (fulltext search: always only one page of results)
     *
     * @throws CorruptIndexException
     * @throws IOException
     * @throws ParseException
     */
    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFullText_Paging() throws CorruptIndexException, IOException, ParseException {

        Reference sec = ReferenceFactory.newDatabase();
        referenceService.save(sec);

        Set<String> uniqueRandomStrs = new HashSet<String>(1024);
        int numOfItems = 100;
        while(uniqueRandomStrs.size() < numOfItems){
            uniqueRandomStrs.add(RandomStringUtils.random(5, true, false));
        }

        for(String rndStr: uniqueRandomStrs){

            Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SERIES()), sec);
            taxon.setTitleCache("Tax" + rndStr, true);
            taxonService.save(taxon);

            TaxonDescription description = TaxonDescription.NewInstance(taxon);
            description.addElement(CommonTaxonName.NewInstance("Rot" + rndStr, Language.DEFAULT()));
            descriptionService.saveOrUpdate(description);
        }

        commitAndStartNewTransaction(new String[]{"TAXONBASE"});
        refreshLuceneIndex();

        int pageSize = 10;

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, null, null, false, pageSize, null, null, null);
        Assert.assertEquals("unexpeted number of pages", Integer.valueOf(numOfItems / pageSize), pager.getPagesAvailable());
        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, null, null, false, pageSize, 9, null, null);
        Assert.assertNotNull("last page must have records", pager.getRecords());
        Assert.assertNotNull("last item on last page must exist", pager.getRecords().get(0));
        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, null, null, false, pageSize, 10, null, null);
        Assert.assertNotNull("last page + 1 must not have any records", pager.getRecords());
    }

    /**
     * test for max score and sort by score of hit groups
     * with all matches per taxon in a single TextData  element
     * see {@link #testFullText_ScoreAndOrder_2()} for the complement
     * test with matches in multiple TextData per taxon
     *
     * @throws CorruptIndexException
     * @throws IOException
     * @throws ParseException
     */
    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    @Ignore // test fails, maybe the assumptions made here are not compatible with the lucene scoring mechanism see http://lucene.apache.org/core/3_6_1/scoring.html
    public final void testFullText_ScoreAndOrder_1() throws CorruptIndexException, IOException, ParseException {

        int numOfTaxa = 3;

        UUID[] taxonUuids = new UUID[numOfTaxa];
        StringBuilder text = new StringBuilder();

        for(int i = 0; i < numOfTaxa; i++){

            Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(null), null);
            taxon.setTitleCache("Taxon_" + i, true);
            taxonUuids[i] = taxon.getUuid();
            taxonService.save(taxon);

            text.append(" ").append("Rot");
            TaxonDescription description = TaxonDescription.NewInstance(taxon);
            description.addElement(TextData.NewInstance(text.toString(), Language.DEFAULT(), null));
            descriptionService.saveOrUpdate(description);
        }

        commitAndStartNewTransaction(null);
        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Rot", null, null, null, false, null, null, null, null);
        for(int i = 0; i < numOfTaxa; i++){
            Assert.assertEquals("taxa should be orderd by relevance (= score)", taxonUuids[numOfTaxa - i - 1], pager.getRecords().get(i).getEntity().getUuid());
        }
        Assert.assertEquals("max score should be equal to the score of the first element", pager.getRecords().get(0).getMaxScore(), pager.getRecords().get(0).getScore(), 0);
    }

    /**
     * test for max score and sort by score of hit groups
     * with all matches per taxon in a multiple TextData elements
     * see {@link #testFullText_ScoreAndOrder_1()} for the complement
     * test with matches in a single TextData per taxon
     *
     * @throws CorruptIndexException
     * @throws IOException
     * @throws ParseException
     */
    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    @Ignore // test fails, maybe the assumptions made here are not compatible with the lucene scoring mechanism see http://lucene.apache.org/core/3_6_1/scoring.html
    public final void testFullText_ScoreAndOrder_2() throws CorruptIndexException, IOException, ParseException {

        int numOfTaxa = 3;

        UUID[] taxonUuids = new UUID[numOfTaxa];

        for(int i = 0; i < numOfTaxa; i++){

            Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(null), null);
            taxon.setTitleCache("Taxon_" + i, true);
            taxonUuids[i] = taxon.getUuid();
            taxonService.save(taxon);

            TaxonDescription description = TaxonDescription.NewInstance(taxon);
            for(int k = 0; k < i; k++){
                description.addElement(TextData.NewInstance("Rot", Language.DEFAULT(), null));
            }
            descriptionService.saveOrUpdate(description);
        }

        commitAndStartNewTransaction(null);
        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Rot", null, null, null, false, null, null, null, null);
        for(int i = 0; i < numOfTaxa; i++){
            Assert.assertEquals("taxa should be orderd by relevance (= score)", taxonUuids[numOfTaxa - i - 1], pager.getRecords().get(i).getEntity().getUuid());
        }
        Assert.assertEquals("max score should be equal to the score of the first element", pager.getRecords().get(0).getMaxScore(), pager.getRecords().get(0).getScore(), 0);
    }


    /**
     * @throws CorruptIndexException
     * @throws IOException
     * @throws ParseException
     * @throws LuceneMultiSearchException
     */
    @Test
    @DataSet
    public final void testFullText_Grouping() throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        TaxonDescription description = (TaxonDescription) descriptionService.find(UUID.fromString(D_ABIES_ALBA_UUID));
        Set<String> uniqueRandomStrs = new HashSet<String>(1024);
        int numOfItems = 100;
        while(uniqueRandomStrs.size() < numOfItems){
            uniqueRandomStrs.add(RandomStringUtils.random(5, true, false));
        }
        for(String rndStr: uniqueRandomStrs){
            description.addElement(CommonTaxonName.NewInstance("Rot" + rndStr, Language.DEFAULT()));
        }
        descriptionService.saveOrUpdate(description);

        commitAndStartNewTransaction(new String[]{"DESCRIPTIONELEMENTBASE"});

        refreshLuceneIndex();

        int pageSize = 10;

        Pager<SearchResult<TaxonBase>> pager;
        boolean highlightFragments = true;

        // test with findByDescriptionElementFullText
        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, null, null, highlightFragments, pageSize, null, null, null);
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertEquals("All matches should be grouped into a single SearchResult element", 1, pager.getRecords().size());
        Assert.assertEquals("The count property of the pager must be set correctly", 1, pager.getCount().intValue());
        Map<String, String[]> highlightMap = pager.getRecords().get(0).getFieldHighlightMap();
        // maxDocsPerGroup is defined in LuceneSearch and defaults to 10
        int maxDocsPerGroup = 10;
        Assert.assertEquals("expecting 10 highlighted fragments of field 'name'", maxDocsPerGroup, highlightMap.get("name").length);

        // test with findByEverythingFullText
        pager = taxonService.findByEverythingFullText( "Rot*", null, null, highlightFragments, pageSize, null, null, null);
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertEquals("All matches should be grouped into a single SearchResult element", 1, pager.getRecords().size());
        Assert.assertEquals("The count property of the pager must be set correctly", 1, pager.getCount().intValue());
        highlightMap = pager.getRecords().get(0).getFieldHighlightMap();
        // maxDocsPerGroup is defined in LuceneSearch and defaults to 10
        maxDocsPerGroup = 10;
        Assert.assertEquals("expecting 10 highlighted fragments of field 'name'", maxDocsPerGroup, highlightMap.get("name").length);

    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    @Ignore
    public final void testFindByDescriptionElementFullText_TextData() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Abies", null, null, null, false, null, null, null, null);
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertEquals("Expecting one entity when searching for any TextData", Integer.valueOf(1), pager.getCount());
        Assert.assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standart views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());
        Assert.assertTrue("Expecting two docs, one for RUSSIAN and one for GERMAN", pager.getRecords().get(0).getDocs().size() == 2);
        Assert.assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standart views, 2013", pager.getRecords().get(0).getDocs().iterator().next().get("inDescription.taxon.titleCache"));


        pager = taxonService.findByDescriptionElementFullText(null, "Abies", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching for any type", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(null, "Abies", null, Arrays.asList(new Feature[]{Feature.UNKNOWN()}), null, false, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching for any type and for Feature DESCRIPTION", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(null, "Abies", null, Arrays.asList(new Feature[]{Feature.CHROMOSOME_NUMBER()}), null, false, null, null, null, null);
        Assert.assertEquals("Expecting no entity when searching for any type and for Feature CHROMOSOME_NUMBER", Integer.valueOf(0), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(null, "Abies", null, Arrays.asList(new Feature[]{Feature.CHROMOSOME_NUMBER(), Feature.UNKNOWN()}), null, false, null, null, null, null);
        Assert.assertEquals("Expecting no entity when searching for any type and for Feature DESCRIPTION or CHROMOSOME_NUMBER", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(Distribution.class, "Abies", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Expecting no entity when searching for Distribution", Integer.valueOf(0), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Бальзам", null, null, Arrays.asList(new Language[]{}), false, null, null, null, null);
        Assert.assertEquals("Expecting one entity", Integer.valueOf(1), pager.getCount());
        Assert.assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standart views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Бальзам", null, null, Arrays.asList(new Language[]{Language.RUSSIAN()}), false, null, null, null, null);
        Assert.assertEquals("Expecting one entity", Integer.valueOf(1), pager.getCount());
        Assert.assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standart views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Бальзам", null, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        Assert.assertEquals("Expecting no entity", Integer.valueOf(0), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, null, Arrays.asList(new Language[]{Language.GERMAN(), Language.RUSSIAN()}), false, null, null, null, null);
        Assert.assertEquals("Expecting one entity", Integer.valueOf(1), pager.getCount());
        Assert.assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standart views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_MultipleWords() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        // Pflanzenart aus der Gattung der Tannen

        Pager<SearchResult<TaxonBase>> pager;
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Pflanzenart Tannen", null, null, null, false, null, null, null, null);
        Assert.assertEquals("OR search : Expecting one entity", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Pflanzenart Wespen", null, null, null, false, null, null, null, null);
        Assert.assertEquals("OR search : Expecting one entity", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "+Pflanzenart +Tannen", null, null, null, false, null, null, null, null);
        Assert.assertEquals("AND search : Expecting one entity", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "+Pflanzenart +Wespen", null, null, null, false, null, null, null, null);
        Assert.assertEquals("AND search : Expecting no entity", Integer.valueOf(0), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "\"Pflanzenart aus der Gattung der Tannen\"", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Phrase search : Expecting one entity", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "\"Pflanzenart aus der Gattung der Wespen\"", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Phrase search : Expecting one entity", Integer.valueOf(0), pager.getCount());


    }


    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_modify_DescriptionElement() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;
        //
        // modify the DescriptionElement
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, null, Arrays.asList(new Language[]{Language.GERMAN(), Language.RUSSIAN()}), false, null, null, null, null);
        Assert.assertTrue("Expecting only one doc", pager.getRecords().get(0).getDocs().size() == 1);
        Document indexDocument = pager.getRecords().get(0).getDocs().iterator().next();
        String[] descriptionElementUuidStr = indexDocument.getValues("uuid");
        String[] inDescriptionUuidStr = indexDocument.getValues("inDescription.uuid");
        // is only one uuid!
        DescriptionElementBase textData = descriptionService.getDescriptionElementByUuid(UUID.fromString(descriptionElementUuidStr[0]));

        ((TextData)textData).removeText(Language.GERMAN());
        ((TextData)textData).putText(Language.SPANISH_CASTILIAN(), "abeto bals"+UTF8.SMALL_A_ACUTE+"mico");

        descriptionService.saveDescriptionElement(textData);
        commitAndStartNewTransaction(null);
//        printDataSet(System.out, new String[] {
//                "DESCRIPTIONELEMENTBASE", "LANGUAGESTRING", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING" }
//        );

        //
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, null, Arrays.asList(new Language[]{Language.GERMAN(), Language.RUSSIAN()}), false, null, null, null, null);
        Assert.assertEquals("The german 'Balsam-Tanne' TextData should no longer be indexed", Integer.valueOf(0), pager.getCount());
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "abeto", null, null, Arrays.asList(new Language[]{Language.SPANISH_CASTILIAN()}), false, null, null, null, null);
        Assert.assertEquals("expecting to find the SPANISH_CASTILIAN 'abeto bals"+UTF8.SMALL_A_ACUTE+"mico'", Integer.valueOf(1), pager.getCount());
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "bals"+UTF8.SMALL_A_ACUTE+"mico", null, null, null, false, null, null, null, null);
        Assert.assertEquals("expecting to find the SPANISH_CASTILIAN 'abeto bals"+UTF8.SMALL_A_ACUTE+"mico'", Integer.valueOf(1), pager.getCount());

        //
        // modify the DescriptionElement via the Description object
        DescriptionBase<?> description = descriptionService.find(UUID.fromString(inDescriptionUuidStr[0]));
        Set<DescriptionElementBase> elements = description.getElements();
        for( DescriptionElementBase elm : elements){
            if(elm.getUuid().toString().equals(descriptionElementUuidStr[0])){
                ((TextData)elm).removeText(Language.SPANISH_CASTILIAN());
                ((TextData)elm).putText(Language.POLISH(), "Jod"+UTF8.POLISH_L+"a balsamiczna");
            }
        }
        descriptionService.saveOrUpdate(description);
        commitAndStartNewTransaction(null);
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "abeto", null, null, Arrays.asList(new Language[]{Language.SPANISH_CASTILIAN()}), false, null, null, null, null);
        Assert.assertEquals("The spanish 'abeto bals"+UTF8.SMALL_A_ACUTE+"mico' TextData should no longer be indexed", Integer.valueOf(0), pager.getCount());
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "balsamiczna", null, null, Arrays.asList(new Language[]{Language.POLISH()}), false, null, null, null, null);
        Assert.assertEquals("expecting to find the POLISH 'Jod"+UTF8.POLISH_L+"a balsamiczna'", Integer.valueOf(1), pager.getCount());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_modify_Taxon() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;
        Taxon t_abies_balsamea = (Taxon)taxonService.find(UUID.fromString(ABIES_BALSAMEA_UUID));
        TaxonDescription d_abies_balsamea = (TaxonDescription)descriptionService.find(UUID.fromString(D_ABIES_BALSAMEA_UUID));

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        Assert.assertEquals("expecting to find the GERMAN 'Balsam-Tanne'", Integer.valueOf(1), pager.getCount());

        // exchange the Taxon with another one via the Taxon object
        // 1.) remove existing description:
        t_abies_balsamea.removeDescription(d_abies_balsamea);

        taxonService.saveOrUpdate(t_abies_balsamea);
        commitAndStartNewTransaction(null);

        t_abies_balsamea = (Taxon)taxonService.find(t_abies_balsamea.getUuid());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        Assert.assertEquals("'Balsam-Tanne' should no longer be found", Integer.valueOf(0), pager.getCount());

        // 2.) create new description and add to taxon:
        TaxonDescription d_abies_balsamea_new = TaxonDescription.NewInstance();
        d_abies_balsamea_new
                .addElement(TextData
                        .NewInstance(
                                "Die Balsamtanne ist mit bis zu 30 m Höhe ein mittelgro"+UTF8.SHARP_S+"er Baum und kann bis zu 200 Jahre alt werden",
                                Language.GERMAN(), null));
        t_abies_balsamea.addDescription(d_abies_balsamea_new);
        // set authorshipCache to null to avoid validation exception,
        // this is maybe not needed in future,  see ticket #3344
        BotanicalName abies_balsamea = HibernateProxyHelper.deproxy(t_abies_balsamea.getName(), BotanicalName.class);
        abies_balsamea.setAuthorshipCache(null);
        taxonService.saveOrUpdate(t_abies_balsamea);
        commitAndStartNewTransaction(null);

        printDataSet(System.out, new String[] {
                "DESCRIPTIONBASE"
        });

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "mittelgro"+UTF8.SHARP_S+"er Baum", null, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        Assert.assertEquals("the taxon should be found via the new Description", Integer.valueOf(1), pager.getCount());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_modify_Classification() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        // put taxon into other classification, new taxon node
        Classification classification = classificationService.find(UUID.fromString(CLASSIFICATION_UUID));
        Classification alternateClassification = classificationService.find(UUID.fromString(CLASSIFICATION_ALT_UUID));

        // TODO: why is the test failing when the childNode is already retrieved here, and not after the following four lines?
        //TaxonNode childNode = classification.getChildNodes().iterator().next();

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        Assert.assertEquals("expecting to find the GERMAN 'Balsam-Tanne' even if filtering by classification", Integer.valueOf(1), pager.getCount());
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", alternateClassification, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        Assert.assertEquals("GERMAN 'Balsam-Tanne' should NOT be found in other classification", Integer.valueOf(0), pager.getCount());

        // check for the right taxon node
        TaxonNode childNode = classification.getChildNodes().iterator().next();
        Assert.assertEquals("expecting Abies balsamea sec.", childNode.getTaxon().getUuid().toString(), ABIES_BALSAMEA_UUID);
        Assert.assertEquals("expecting default classification", childNode.getClassification().getUuid().toString(), CLASSIFICATION_UUID);

        // moving the taxon around, the rootnode is only a proxy
        alternateClassification.setRootNode(HibernateProxyHelper.deproxy(alternateClassification.getRootNode(), TaxonNode.class));
        alternateClassification.addChildNode(childNode, null, null);

        classificationService.saveOrUpdate(alternateClassification);
        commitAndStartNewTransaction(null);

//        printDataSet(System.out, new String[] {
//            "TAXONBASE", "TAXONNODE", "CLASSIFICATION"
//        });

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", alternateClassification, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        Assert.assertEquals("GERMAN 'Balsam-Tanne' should now be found in other classification", Integer.valueOf(1), pager.getCount());

        classification.getChildNodes().clear();
        classificationService.saveOrUpdate(classification);
        commitAndStartNewTransaction(null);

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", classification, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        Assert.assertEquals("Now the GERMAN 'Balsam-Tanne' should NOT be found in original classification", Integer.valueOf(0), pager.getCount());

    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_CategoricalData() throws CorruptIndexException, IOException, ParseException {

        // add CategoricalData
        DescriptionBase d_abies_balsamea = descriptionService.find(UUID.fromString(D_ABIES_BALSAMEA_UUID));
        // Categorical data
        CategoricalData cdata = CategoricalData.NewInstance();
        cdata.setFeature(Feature.DESCRIPTION());
        State state = State.NewInstance("green", "green", "gn");

        StateData statedata = StateData.NewInstance(state);
        statedata.putModifyingText(Language.ENGLISH(), "always, even during winter");
        cdata.addStateData(statedata);
        d_abies_balsamea.addElement(cdata);

        UUID termUUID = termService.save(state).getUuid();
        descriptionService.save(d_abies_balsamea);

        commitAndStartNewTransaction(null);

//        printDataSet(System.out, new String[] {
//                 "STATEDATA", "STATEDATA_DEFINEDTERMBASE", "STATEDATA_LANGUAGESTRING", "LANGUAGESTRING"});

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;
        pager = taxonService.findByDescriptionElementFullText(CategoricalData.class, "green", null, null, null, false, null, null, null, null);
        Assert.assertEquals("Expecting one entity", Integer.valueOf(1), pager.getCount());
        Assert.assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standart views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());
        Assert.assertTrue("Expecting only one doc", pager.getRecords().get(0).getDocs().size() == 1);
        Assert.assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standart views, 2013", pager.getRecords().get(0).getDocs().iterator().next().get("inDescription.taxon.titleCache"));


        //TODO modify the StateData
        TaxonBase taxon = pager.getRecords().get(0).getEntity();

        String newName = "Quercus robur";
        taxon.setTitleCache(newName + " sec. ", true);

        taxonService.saveOrUpdate(taxon);
        commitAndStartNewTransaction(null);

        taxon = taxonService.find(taxon.getUuid());
        Assert.assertEquals(newName + " sec. ", taxon.getTitleCache());
        DefinedTermBase term = termService.find(termUUID);

        termService.delete(term);

    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText_Highlighting() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Abies", null, null, null, true, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching for any TextData", Integer.valueOf(1), pager.getCount());
        SearchResult<TaxonBase> searchResult = pager.getRecords().get(0);
        Assert.assertTrue("the map of highlighted fragments should contain at least one item", searchResult.getFieldHighlightMap().size() > 0);
        String[] fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        Assert.assertTrue("first fragments should contains serch term", fragments[0].contains("<B>Abies</B>"));

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Pflanzenart Tannen", null, null, null, true, null, null, null, null);
        searchResult = pager.getRecords().get(0);
        Assert.assertTrue("Phrase search : Expecting at least one item in highlighted fragments", searchResult.getFieldHighlightMap().size() > 0);
        fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        Assert.assertTrue("first fragments should contains serch term", fragments[0].contains("<B>Pflanzenart</B>") || fragments[0].contains("<B>Tannen</B>"));

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "+Pflanzenart +Tannen", null, null, null, true, null, null, null, null);
        searchResult = pager.getRecords().get(0);
        Assert.assertTrue("Phrase search : Expecting at least one item in highlighted fragments", searchResult.getFieldHighlightMap().size() > 0);
        fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        Assert.assertTrue("first fragments should contains serch term", fragments[0].contains("<B>Pflanzenart</B>") && fragments[0].contains("<B>Tannen</B>"));

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "\"Pflanzenart aus der Gattung der Tannen\"", null, null, null, true, null, null, null, null);
        searchResult = pager.getRecords().get(0);
        Assert.assertTrue("Phrase search : Expecting at least one item in highlighted fragments", searchResult.getFieldHighlightMap().size() > 0);
        fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        Assert.assertTrue("first fragments should contains serch term", fragments[0].contains("<B>Pflanzenart</B> <B>aus</B> <B>der</B> <B>Gattung</B> <B>der</B> <B>Tannen</B>"));

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Gatt*", null, null, null, true, null, null, null, null);
        searchResult = pager.getRecords().get(0);
        Assert.assertTrue("Wildcard search : Expecting at least one item in highlighted fragments", searchResult.getFieldHighlightMap().size() > 0);
        fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        Assert.assertTrue("first fragments should contains serch term", fragments[0].contains("<B>Gatt"));
    }


    @Test
    @DataSet
    public final void testFindByFullText() throws CorruptIndexException, IOException, ParseException {

        refreshLuceneIndex();

        Classification europeanAbiesClassification = classificationService.find(UUID.fromString(CLASSIFICATION_UUID));

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByFullText(null, "Abies", null, null, true, null, null, null, null); // --> 7
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertEquals("Expecting 8 entities", Integer.valueOf(8), pager.getCount());

        pager = taxonService.findByFullText(Taxon.class, "Abies", null, null, true, null, null, null, null); // --> 6
        Assert.assertEquals("Expecting 7 entities", Integer.valueOf(7), pager.getCount());

        pager = taxonService.findByFullText(Synonym.class, "Abies", null, null, true, null, null, null, null); // --> 1
        Assert.assertEquals("Expecting 1 entity", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByFullText(TaxonBase.class, "sec", null, null, true, null, null, null, null); // --> 7
        Assert.assertEquals("Expecting 8 entities", Integer.valueOf(8), pager.getCount());

        pager = taxonService.findByFullText(null, "genus", null, null, true, null, null, null, null); // --> 1
        Assert.assertEquals("Expecting 1 entity", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByFullText(Taxon.class, "subalpina", null, null, true, null, null, null, null); // --> 0
        Assert.assertEquals("Expecting 0 entities", Integer.valueOf(0), pager.getCount());

        // synonym in classification ???
    }

    @Test
    @DataSet
    public final void testPrepareByAreaSearch() throws IOException, ParseException {

        List<PresenceAbsenceTerm> statusFilter = new ArrayList<PresenceAbsenceTerm>();
        List<NamedArea> areaFilter = new ArrayList<NamedArea>();
        areaFilter.add(germany);
        areaFilter.add(canada);
        areaFilter.add(russia);

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDistribution(areaFilter, statusFilter, null, 20, 0, null, null);
        Assert.assertEquals("Expecting 2 entities", Integer.valueOf(2), Integer.valueOf(pager.getRecords().size()));

    }

    @Test
    @DataSet
    public final void testFindTaxaAndNamesByFullText() throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        Classification alternateClassification = classificationService.find(UUID.fromString(CLASSIFICATION_ALT_UUID));


        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        Assert.assertEquals("doTaxa & doSynonyms", Integer.valueOf(8), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.allOf(TaxaAndNamesSearchMode.class),
                "Abies", null, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        Assert.assertEquals("all search modes", Integer.valueOf(8), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.allOf(TaxaAndNamesSearchMode.class),
                "Abies", alternateClassification, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        Assert.assertEquals("all search modes, filtered by alternateClassification", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, null, null, null, true, null, null, null, null);
        Assert.assertEquals("Expecting 1 entity", Integer.valueOf(1), pager.getCount());
        SearchResult<TaxonBase> searchResult = pager.getRecords().get(0);
        Assert.assertEquals(Synonym.class, searchResult.getEntity().getClass());
        // Abies subalpina sec. Kohlbecker, A., Testcase standart views, 2013


        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxaByCommonNames),
                "Abies", null, null, null, null, true, null, null, null, null);
        Assert.assertEquals("Expecting 0 entity", Integer.valueOf(0), pager.getCount());


        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxaByCommonNames),
                "Tanne", null, null, null, null, true, null, null, null, null);
        Assert.assertEquals("Expecting 1 entity", Integer.valueOf(1), Integer.valueOf(pager.getRecords().size()));
        Assert.assertEquals("Expecting 1 entity", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                "kawakamii", null, null, null, null, true, null, null, null, null);
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertEquals("Expecting 1 entity", Integer.valueOf(1), pager.getCount());

    }

    @Test
    @DataSet
    public final void testFindTaxaAndNamesByFullText_Sort() throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        List<OrderHint> orderHints = new ArrayList<OrderHint>();

        String[] docFields2log = new String[]{"id"};

        // SortById
        orderHints.addAll(OrderHint.ORDER_BY_ID);
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa),
                "Abies", null, null, null, null, true, null, null, orderHints, null);
//        logSearchResults(pager, Level.DEBUG, docFields2log);
        int lastId = -1;
        for(SearchResult<TaxonBase> rs : pager.getRecords()){
            if(lastId != -1){
                Assert.assertTrue("results not sorted by id", lastId < rs.getEntity().getId());
            }
            lastId = rs.getEntity().getId();
        }

        orderHints.addAll(OrderHint.ORDER_BY_ID);
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, null, null, null, true, null, null, orderHints, null);
//        logSearchResults(pager, Level.DEBUG, docFields2log);

        lastId = -1;
        for(SearchResult<TaxonBase> rs : pager.getRecords()){
            if(lastId != -1){
                Assert.assertTrue("results not sorted by id", lastId < rs.getEntity().getId());
            }
            lastId = rs.getEntity().getId();
        }

        // Sortby NOMENCLATURAL_SORT_ORDER TODO make assertions !!!
        orderHints.clear();
        orderHints.addAll(OrderHint.NOMENCLATURAL_SORT_ORDER);
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, null, null, null, true, null, null, orderHints, null);
        logSearchResults(pager, Level.DEBUG, null);

    }

    @Test
    @DataSet
    public final void testFindTaxaAndNamesByFullText_AreaFilter() throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        Set<NamedArea> a_germany_canada_russia = new HashSet<NamedArea>();
        a_germany_canada_russia.add(germany);
        a_germany_canada_russia.add(canada);
        a_germany_canada_russia.add(russia);

        Set<NamedArea> a_russia = new HashSet<NamedArea>();
        a_russia.add(russia);

        Set<PresenceAbsenceTerm> present = new HashSet<PresenceAbsenceTerm>();
        present.add(PresenceAbsenceTerm.PRESENT());

        Set<PresenceAbsenceTerm> present_native = new HashSet<PresenceAbsenceTerm>();
        present_native.add(PresenceAbsenceTerm.PRESENT());
        present_native.add(PresenceAbsenceTerm.NATIVE());

        Set<PresenceAbsenceTerm> absent = new HashSet<PresenceAbsenceTerm>();
        absent.add(PresenceAbsenceTerm.ABSENT());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa),
                "Abies", null, a_germany_canada_russia, null, null, true, null, null, null, null);
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertEquals("taxa with matching area filter", Integer.valueOf(2), pager.getCount());

        // abies_kawakamii_sensu_komarov as missapplied name for t_abies_balsamea
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        Assert.assertEquals("synonyms with matching area filter", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, a_germany_canada_russia, null, null, true, null, null, null, null);
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertEquals("taxa and synonyms with matching area filter", Integer.valueOf(3), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        Assert.assertEquals("taxa and synonyms with matching area & status filter 1", Integer.valueOf(3), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, a_germany_canada_russia, present, null, true, null, null, null, null);
        Assert.assertEquals("taxa and synonyms with matching area & status filter 2", Integer.valueOf(2), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, a_russia, present, null, true, null, null, null, null);
        Assert.assertEquals("taxa and synonyms with non matching area & status filter", Integer.valueOf(0), pager.getCount());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxaByCommonNames),
                "Tanne", null, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        Assert.assertEquals("ByCommonNames with area filter", Integer.valueOf(1), pager.getCount());

        // abies_kawakamii_sensu_komarov as misapplied name for t_abies_balsamea
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                "Abies", null, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        Assert.assertEquals("misappliedNames with matching area & status filter", Integer.valueOf(1), pager.getCount());


        // 1. remove existing taxon relation
        Taxon t_abies_balsamea = (Taxon)taxonService.find(UUID.fromString(ABIES_BALSAMEA_UUID));
        Set<TaxonRelationship> relsTo = t_abies_balsamea.getRelationsToThisTaxon();
        Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(relsTo.size()));
        TaxonRelationship taxonRelation = relsTo.iterator().next();
        t_abies_balsamea.removeTaxonRelation(taxonRelation);
        taxonService.saveOrUpdate(t_abies_balsamea);
        commitAndStartNewTransaction(null);

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                "Abies", null, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        Assert.assertEquals("misappliedNames with matching area & status filter, should match nothing now", Integer.valueOf(0), pager.getCount());

        // 2. now add abies_kawakamii_sensu_komarov as misapplied name for t_abies_alba and search for misapplications in russia: ABSENT
        Taxon t_abies_kawakamii_sensu_komarov = (Taxon)taxonService.find(UUID.fromString(D_ABIES_KAWAKAMII_SEC_KOMAROV_UUID));
        Taxon t_abies_alba = (Taxon)taxonService.find(UUID.fromString(ABIES_ALBA_UUID));
        t_abies_alba.addMisappliedName(t_abies_kawakamii_sensu_komarov, null, null);
        taxonService.update(t_abies_alba);
        commitAndStartNewTransaction(null);

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                "Abies", null, a_germany_canada_russia, absent, null, true, null, null, null, null);
        Assert.assertEquals("misappliedNames with matching area & status filter, should find one", Integer.valueOf(1), pager.getCount());

    }

    /**
     * Regression test for #3119: fulltext search: Entity always null whatever search
     *
     * @throws CorruptIndexException
     * @throws IOException
     * @throws ParseException
     * @throws LuceneMultiSearchException
     */
    @Test
    @DataSet
    public final void testFindByEverythingFullText() throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        // via Taxon
        pager = taxonService.findByEverythingFullText("Abies", null, null, true, null, null, null, null);
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertTrue("Expecting at least 7 entities for 'Abies'", pager.getCount() > 7);
        Assert.assertNotNull("Expecting entity", pager.getRecords().get(0).getEntity());
        Assert.assertEquals("Expecting Taxon entity", Taxon.class, pager.getRecords().get(0).getEntity().getClass());

        // via DescriptionElement
        pager = taxonService.findByEverythingFullText("present", null, null, true, null, null, null, null);
        Assert.assertEquals("Expecting one entity when searching for area 'present'", Integer.valueOf(1), pager.getCount());
        Assert.assertNotNull("Expecting entity", pager.getRecords().get(0).getEntity());
        Assert.assertEquals("Expecting Taxon entity", Taxon.class, pager.getRecords().get(0).getEntity().getClass());
        Assert.assertEquals("Expecting Taxon ", ABIES_BALSAMEA_UUID, pager.getRecords().get(0).getEntity().getUuid().toString());

    }


    @Test
    @DataSet
    public final void findByEveryThingFullText() throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByEverythingFullText("genus", null, null,  false, null, null, null, null); // --> 1
        Assert.assertEquals("Expecting 1 entity", Integer.valueOf(1), pager.getCount());

        //FIXME FAILS: abies balamea is returned twice, see also testFullText_Grouping()
        pager = taxonService.findByEverythingFullText("Balsam-Tanne", null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        logSearchResults(pager, Level.DEBUG, null);
        Assert.assertEquals("expecting to find the Abies balsamea via the GERMAN DescriptionElements", Integer.valueOf(1), pager.getCount());

        pager = taxonService.findByEverythingFullText("Abies", null, null, true, null, null, null, null);
        Assert.assertEquals("Expecting 8 entities", Integer.valueOf(8), pager.getCount());
        SearchResult<TaxonBase> searchResult = pager.getRecords().get(0);
        Assert.assertTrue("the map of highlighted fragments should contain at least one item", searchResult.getFieldHighlightMap().size() > 0);
        String[] fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        Assert.assertTrue("first fragments should contains serch term", fragments[0].toLowerCase().contains("<b>abies</b>"));
    }

//    @SuppressWarnings("rawtypes")
//    @Test
//    @DataSet
//    public final void benchmarkFindTaxaAndNamesHql() throws CorruptIndexException, IOException, ParseException {
//
//        createRandomTaxonWithCommonName(NUM_OF_NEW_RADOM_ENTITIES);
//
//        IFindTaxaAndNamesConfigurator configurator = new FindTaxaAndNamesConfiguratorImpl();
//        configurator.setTitleSearchString("Wei"+UTF8.SHARP_S+"%");
//        configurator.setMatchMode(MatchMode.BEGINNING);
//        configurator.setDoTaxa(false);
//        configurator.setDoSynonyms(false);
//        configurator.setDoNamesWithoutTaxa(false);
//        configurator.setDoTaxaByCommonNames(true);
//
//        Pager<IdentifiableEntity> pager;
//
//        long startMillis = System.currentTimeMillis();
//        for (int indx = 0; indx < BENCHMARK_ROUNDS; indx++) {
//            pager = taxonService.findTaxaAndNames(configurator);
//            if (logger.isDebugEnabled()) {
//                logger.debug("[" + indx + "]" + pager.getRecords().get(0).getTitleCache());
//            }
//        }
//        double duration = ((double) (System.currentTimeMillis() - startMillis)) / BENCHMARK_ROUNDS;
//        logger.info("Benchmark result - [find taxon by CommonName via HQL] : " + duration + "ms (" + BENCHMARK_ROUNDS + " benchmark rounds )");
//    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void benchmarkFindByCommonNameHql() throws CorruptIndexException, IOException, ParseException {

//        printDataSet(System.err, new String[] { "TaxonBase" });

        createRandomTaxonWithCommonName(NUM_OF_NEW_RADOM_ENTITIES);

        IFindTaxaAndNamesConfigurator configurator = new FindTaxaAndNamesConfiguratorImpl();
        configurator.setTitleSearchString("Wei"+UTF8.SHARP_S+"%");
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
    public final void benchmarkFindByCommonNameLucene() throws CorruptIndexException, IOException, ParseException {

        createRandomTaxonWithCommonName(NUM_OF_NEW_RADOM_ENTITIES);

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        long startMillis = System.currentTimeMillis();
        for (int indx = 0; indx < BENCHMARK_ROUNDS; indx++) {
            pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"*", null, null, null, false, null, null, null, null);
            if (logger.isDebugEnabled()) {
                logger.debug("[" + indx + "]" + pager.getRecords().get(0).getEntity().getTitleCache());
            }
        }
        double duration = ((double) (System.currentTimeMillis() - startMillis)) / BENCHMARK_ROUNDS;
        logger.info("Benchmark result - [find taxon by CommonName via lucene] : " + duration + "ms (" + BENCHMARK_ROUNDS + " benchmark rounds )");
    }

    /**
     * uncomment @Test annotation to create the dataset for this test
     */
    @Override
    //    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="BlankDataSet.xml")
    public final void createTestDataSet() throws FileNotFoundException {

        Classification europeanAbiesClassification = Classification.NewInstance("European Abies");
        europeanAbiesClassification.setUuid(UUID.fromString(CLASSIFICATION_UUID));
        classificationService.save(europeanAbiesClassification);

        Classification alternativeClassification = Classification.NewInstance("Abies alternative");
        alternativeClassification.setUuid(UUID.fromString(CLASSIFICATION_ALT_UUID));
        classificationService.save(alternativeClassification);

        Reference<?> sec = ReferenceFactory.newBook();
        sec.setTitleCache("Kohlbecker, A., Testcase standart views, 2013", true);
        Reference<?> sec_sensu = ReferenceFactory.newBook();
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
        t_abies_balsamea.addSynonym(s_abies_subalpina, SynonymRelationshipType.SYNONYM_OF());
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

        // abies_kawakamii_sensu_komarov as missapplied name for t_abies_balsamea
        Taxon t_abies_kawakamii_sensu_komarov = Taxon.NewInstance(n_abies_kawakamii, sec_sensu);
        taxonService.save(t_abies_kawakamii_sensu_komarov);
        t_abies_kawakamii_sensu_komarov.addTaxonRelation(t_abies_balsamea, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null);
        taxonService.saveOrUpdate(t_abies_kawakamii_sensu_komarov);

        BotanicalName n_abies_lasiocarpa = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_lasiocarpa.setNameCache("Abies lasiocarpa", true);
        Taxon t_abies_lasiocarpa = Taxon.NewInstance(n_abies_lasiocarpa, sec);
        taxonService.save(t_abies_lasiocarpa);

        // add taxa to classifications
        europeanAbiesClassification.addChildTaxon(t_abies_balsamea, null, null);
        alternativeClassification.addChildTaxon(t_abies_lasiocarpa, null, null);
        classificationService.saveOrUpdate(europeanAbiesClassification);
        classificationService.saveOrUpdate(alternativeClassification);

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
            "SYNONYMRELATIONSHIP", "TAXONRELATIONSHIP",
            "REFERENCE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE",
            "AGENTBASE", "HOMOTYPICALGROUP",
            "CLASSIFICATION", "TAXONNODE",
            "LANGUAGESTRING", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            });

    }

    /**
     *
     */
    private void refreshLuceneIndex() {

//        commitAndStartNewTransaction(null);
        commit();
        endTransaction();
        indexer.purge(DefaultProgressMonitor.NewInstance());
        indexer.reindex(typesToIndex, DefaultProgressMonitor.NewInstance());
        startNewTransaction();
//        commitAndStartNewTransaction(null);
    }

    /**
     * @param numberOfNew
     *
     */
    private void createRandomTaxonWithCommonName(int numberOfNew) {

        logger.debug(String.format("creating %1$s random taxan with CommonName", numberOfNew));

        commitAndStartNewTransaction(null);

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

    private <T extends CdmBase> void logSearchResults(Pager<SearchResult<T>> pager, Level level, String[] docFields){
        if(level == null){
            level = Level.DEBUG;
        }
        if(logger.isEnabledFor(level)){
            StringBuilder b = new StringBuilder();
            b.append("\n");
            int i = 0;
            for(SearchResult sr : pager.getRecords()){

                b.append(" ").append(i++).append(" - ");
                b.append("score:").append(sr.getScore()).append(", ");

                if(docFields != null){
                    b.append("docs : ");
                    for(Document doc : sr.getDocs()) {
                        b.append("<");
                        for(String f : docFields){
                            b.append(f).append(":").append(Arrays.toString(doc.getValues(f)));
                        }
                        b.append(">");
                    }
                }

                CdmBase entity = sr.getEntity();
                if(entity == null){
                    b.append("NULL");
                } else {
                    b.append(entity.getClass().getSimpleName()).
                        append(" [").append(entity.getId()).
                        append(" | ").append(entity.getUuid()).append("] : ").
                        append(entity.toString());

                }
                b.append("\n");
            }
            logger.log(level, b);
        }
    }

}
