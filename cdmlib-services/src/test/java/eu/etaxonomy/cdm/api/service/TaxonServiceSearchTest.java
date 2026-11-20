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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.service.config.FindTaxaAndNamesConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.ICdmMassIndexer;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearchException;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
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
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author a.babadshanjan, a.kohlbecker
 * @since 04.02.2009
 */
public class TaxonServiceSearchTest extends CdmServicesTransactionalSpringIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

    private static final int BENCHMARK_ROUNDS = 300;

    private static final UUID ABIES_BALSAMEA_UUID = UUID.fromString("f65d47bd-4f49-4ab1-bc4a-bc4551eaa1a8");
    private static final UUID ABIES_ALBA_UUID = UUID.fromString("7dbd5810-a3e5-44b6-b563-25152b8867f4");
    private static final UUID CLASSIFICATION_UUID = UUID.fromString("2a5ceebb-4830-4524-b330-78461bf8cb6b");
    private static final UUID CLASSIFICATION_ALT_UUID = UUID.fromString("d7c741e3-ae9e-4a7d-a566-9e3a7a0b51ce");
    private static final UUID ABIES_SUBALPINA_UUID = UUID.fromString("9fee273c-c819-4f1f-913a-cd910465df51");
    private static final UUID ABIES_LASIOCARPA_UUID = UUID.fromString("9ce1fecf-c1ad-4127-be01-85d5d9f847ce");
    private static final UUID ABIES_KAWAKAMII_SEC_KOMAROV_UUID = UUID.fromString("e9d8c2fd-6409-46d5-9c2e-14a2bbb1b2b1");

    private static final UUID ROOTNODE_CLASSIFICATION_5000 = UUID.fromString("a8266e45-091f-432f-87ae-c625e6aa9bbc");

    private static final UUID DESC_ABIES_BALSAMEA_UUID = UUID.fromString("900108d8-e6ce-495e-b32e-7aad3099135e");
    private static final UUID DESC_ABIES_ALBA_UUID = UUID.fromString("ec8bba03-d993-4c85-8472-18b14942464b");

    private static final int NUM_OF_NEW_RADOM_ENTITIES = 1000;

    private boolean includeUnpublished = true;

    @Autowired
    private ITaxonService taxonService;
    @Autowired
    private ITermService termService;
    @Autowired
    private IClassificationService classificationService;
    @Autowired
    private IReferenceService referenceService;
    @Autowired
    private IDescriptionService descriptionService;
    @Autowired
    private IDescriptionElementService descriptionElementService;
    @Autowired
    private INameService nameService;
    @Autowired
    private ITaxonNodeService nodeService;

    @Autowired
    private ICdmMassIndexer indexer;

    private Set<Class<? extends CdmBase>> typesToIndex = null;

    private NamedArea germany;
    private NamedArea russia;
    private NamedArea canada;

    @BeforeEach
    public void setUp() throws Exception {
        typesToIndex = new HashSet<>();
        typesToIndex.add(DescriptionElementBase.class);
        typesToIndex.add(TaxonBase.class);
        typesToIndex.add(TaxonRelationship.class);

        germany = Country.GERMANY();
        russia = Country.RUSSIANFEDERATION();
        canada = Country.CANADA();

        includeUnpublished = true;
    }

    @Test
    public void testDbUnitUsageTest() throws Exception {
        assertNotNull(taxonService, "taxonService should exist");
        assertNotNull(nameService, "nameService should exist");
    }

    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_CommonName() throws IOException,
            LuceneParseException {

        TaxonNode subtree = null;
        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"tanne",
                null, subtree, null, null,
                false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for CommonTaxonName");

        // the description containing the Nulltanne has no taxon attached,
        // taxon.id = null
        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Nulltanne", null, subtree, null, null,
                false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting no entity when searching for 'Nulltanne' ");

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"tanne", null, subtree, null,
                Arrays.asList(new Language[] { Language.GERMAN() }), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching in German");

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"tanne", null, subtree, null,
                Arrays.asList(new Language[] { Language.RUSSIAN() }), false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting no entity when searching in Russian");
    }

    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_Distribution() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        // by Area
        Pager<SearchResult<TaxonBase>>  pager = taxonService.findByDescriptionElementFullText(null, "Canada", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for arae 'Canada'");
        // by Status
        pager = taxonService.findByDescriptionElementFullText(null, "present", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for status 'present'");
    }

    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_wildcard() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"*", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for CommonTaxonName");
    }

    /**
     * Regression test for #3113 (hibernate search: wildcard query can cause BooleanQuery$TooManyClauses: maxClauseCount is set to 1024)
     *
     * @throws IOException
     * @throws LuceneParseException
     */
    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_TooManyClauses() throws IOException, LuceneParseException {

        TaxonNode subtree = null;

        // generate 1024 terms to reproduce the bug
        TaxonDescription description = (TaxonDescription) descriptionService.find(DESC_ABIES_ALBA_UUID);
        Set<String> uniqueRandomStrs = new HashSet<>(1024);
        while(uniqueRandomStrs.size() < 1024){
            uniqueRandomStrs.add(RandomStringUtils.insecure().next(10, true, false));
        }
        for(String rndStr: uniqueRandomStrs){
            description.addElement(CommonTaxonName.NewInstance("Rot" + rndStr, Language.DEFAULT()));
        }
        commitAndStartNewTransaction(null);

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting all 1024 entities grouped into one SearchResult item when searching for Rot*");
    }

    /**
     * Regression test for #3116 (fulltext search: always only one page of results)
     *
     * @throws IOException
     * @throws LuceneParseException
     */
    @Test
    @CdmDataSet(cleanSweep = true)
    public final void testFullText_Paging() throws IOException, LuceneParseException {

        TaxonNode subtree = null;
        Reference sec = ReferenceFactory.newDatabase();
        referenceService.save(sec);

        Set<String> uniqueRandomStrs = new HashSet<>(1024);
        int numOfItems = 100;
        while(uniqueRandomStrs.size() < numOfItems){
            uniqueRandomStrs.add(RandomStringUtils.insecure().next(5, true, false));
        }

        for(String rndStr: uniqueRandomStrs){

            Taxon taxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SERIES()), sec);
            taxon.setTitleCache("Tax" + rndStr, true);
            taxonService.save(taxon);

            TaxonDescription description = TaxonDescription.NewInstance(taxon);
            description.addElement(CommonTaxonName.NewInstance("Rot" + rndStr, Language.DEFAULT()));
            descriptionService.save(description);
        }

        commitAndStartNewTransaction(new String[]{/*"TAXONBASE"*/});
        refreshLuceneIndex();

        int pageSize = 10;

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, subtree, null, null, false, pageSize, null, null, null);
        assertEquals(Integer.valueOf(numOfItems / pageSize), pager.getPagesAvailable(), "unexpeted number of pages");
        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, subtree, null, null, false, pageSize, 9, null, null);
        assertNotNull(pager.getRecords(), "last page must have records");
        assertNotNull(pager.getRecords().get(0), "last item on last page must exist");
        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, subtree, null, null, false, pageSize, 10, null, null);
        assertNotNull(pager.getRecords(), "last page + 1 must not have any records");
    }

    /**
     * test for max score and sort by score of hit groups
     * with all matches per taxon in a single TextData  element
     * see {@link #testFullText_ScoreAndOrder_2()} for the complement
     * test with matches in multiple TextData per taxon
     *
     * @throws IOException
     * @throws LuceneParseException
     */
    @Test
    @CdmDataSet
    @Disabled // test fails, maybe the assumptions made here are not compatible with the lucene scoring mechanism see http://lucene.apache.org/core/3_6_1/scoring.html
    public final void testFullText_ScoreAndOrder_1() throws IOException, LuceneParseException {

        TaxonNode subtree = null;
        int numOfTaxa = 3;

        UUID[] taxonUuids = new UUID[numOfTaxa];
        StringBuilder text = new StringBuilder();

        for(int i = 0; i < numOfTaxa; i++){

            Taxon taxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(null), null);
            taxon.setTitleCache("Taxon_" + i, true);
            taxonUuids[i] = taxon.getUuid();
            taxonService.save(taxon);

            text.append(" ").append("Rot");
            TaxonDescription description = TaxonDescription.NewInstance(taxon);
            description.addElement(TextData.NewInstance(text.toString(), Language.DEFAULT(), null));
            descriptionService.save(description);
        }

        commitAndStartNewTransaction(null);
        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(TextData.class, "Rot", null, subtree, null, null, false, null, null, null, null);
        for(int i = 0; i < numOfTaxa; i++){
            assertEquals(taxonUuids[numOfTaxa - i - 1], pager.getRecords().get(i).getEntity().getUuid(), "taxa should be orderd by relevance (= score)");
        }
        assertEquals(pager.getRecords().get(0).getMaxScore(), pager.getRecords().get(0).getScore(), 0, "max score should be equal to the score of the first element");
    }

    /**
     * test for max score and sort by score of hit groups
     * with all matches per taxon in a multiple TextData elements
     * see {@link #testFullText_ScoreAndOrder_1()} for the complement
     * test with matches in a single TextData per taxon
     *
     * @throws IOException
     * @throws LuceneParseException
     */
    @Test
    @CdmDataSet
    @Disabled // test fails, maybe the assumptions made here are not compatible with the lucene scoring mechanism see http://lucene.apache.org/core/3_6_1/scoring.html
    public final void testFullText_ScoreAndOrder_2() throws IOException, LuceneParseException {

        TaxonNode subtree = null;
        int numOfTaxa = 3;

        UUID[] taxonUuids = new UUID[numOfTaxa];

        for(int i = 0; i < numOfTaxa; i++){

            Taxon taxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(null), null);
            taxon.setTitleCache("Taxon_" + i, true);
            taxonUuids[i] = taxon.getUuid();
            taxonService.save(taxon);

            TaxonDescription description = TaxonDescription.NewInstance(taxon);
            for(int k = 0; k < i; k++){
                description.addElement(TextData.NewInstance("Rot", Language.DEFAULT(), null));
            }
            descriptionService.save(description);
        }

        commitAndStartNewTransaction(null);
        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(TextData.class, "Rot", null, subtree, null, null, false, null, null, null, null);
        for(int i = 0; i < numOfTaxa; i++){
            assertEquals(taxonUuids[numOfTaxa - i - 1], pager.getRecords().get(i).getEntity().getUuid(), "taxa should be orderd by relevance (= score)");
        }
        assertEquals(pager.getRecords().get(0).getMaxScore(), pager.getRecords().get(0).getScore(), 0, "max score should be equal to the score of the first element");
    }

    /**
     * @throws IOException
     * @throws LuceneParseException
     * @throws LuceneMultiSearchException
     */
    @Test
    @CdmDataSet
    public final void testFullText_Grouping() throws IOException, LuceneParseException, LuceneMultiSearchException {

        TaxonNode subtree = null;
        TaxonDescription description = (TaxonDescription) descriptionService.find(DESC_ABIES_ALBA_UUID);
        Set<String> uniqueRandomStrs = new HashSet<>(1024);
        int numOfItems = 100;
        while(uniqueRandomStrs.size() < numOfItems){
            uniqueRandomStrs.add(RandomStringUtils.insecure().next(5, true, false));
        }
        for(String rndStr: uniqueRandomStrs){
            description.addElement(CommonTaxonName.NewInstance("Rot" + rndStr, Language.DEFAULT()));
        }

        commitAndStartNewTransaction(new String[]{"DESCRIPTIONELEMENTBASE"});

        refreshLuceneIndex();

        int pageSize = 10;

         boolean highlightFragments = true;

        // test with findByDescriptionElementFullText
        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Rot*", null, subtree, null, null, highlightFragments, pageSize, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(1, pager.getRecords().size(), "All matches should be grouped into a single SearchResult element");
        assertEquals(1, pager.getCount().intValue(), "The count property of the pager must be set correctly");
        Map<String, String[]> highlightMap = pager.getRecords().get(0).getFieldHighlightMap();
        // maxDocsPerGroup is defined in LuceneSearch and defaults to 10
        int maxDocsPerGroup = 10;
        assertEquals(maxDocsPerGroup, highlightMap.get("name").length, "expecting 10 highlighted fragments of field 'name'");

        // test with findByEverythingFullText
        pager = taxonService.findByEverythingFullText( "Rot*", null, subtree, includeUnpublished, null, highlightFragments, pageSize, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(1, pager.getRecords().size(), "All matches should be grouped into a single SearchResult element");
        assertEquals(1, pager.getCount().intValue(), "The count property of the pager must be set correctly");
        highlightMap = pager.getRecords().get(0).getFieldHighlightMap();
        // maxDocsPerGroup is defined in LuceneSearch and defaults to 10
        maxDocsPerGroup = 10;
        assertEquals(maxDocsPerGroup, highlightMap.get("name").length, "expecting 10 highlighted fragments of field 'name'");
    }

    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_TextData() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(TextData.class, "Abies", null, subtree, null, null, false, null, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for any TextData");
        assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standard views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());
        assertTrue(pager.getRecords().get(0).getDocs().size() == 2, "Expecting two docs, one for RUSSIAN and one for GERMAN");
        assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standard views, 2013", pager.getRecords().get(0).getDocs().iterator().next().get("inDescription.taxon.titleCache"));

        pager = taxonService.findByDescriptionElementFullText(null, "Abies", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for any type");

        pager = taxonService.findByDescriptionElementFullText(null, "Abies", null, subtree, Arrays.asList(new Feature[]{Feature.UNKNOWN()}), null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for any type and for Feature DESCRIPTION");

        pager = taxonService.findByDescriptionElementFullText(null, "Abies", null, subtree, Arrays.asList(new Feature[]{Feature.CHROMOSOME_NUMBER()}), null, false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting no entity when searching for any type and for Feature CHROMOSOME_NUMBER");

        pager = taxonService.findByDescriptionElementFullText(null, "Abies", null, subtree, Arrays.asList(new Feature[]{Feature.CHROMOSOME_NUMBER(), Feature.UNKNOWN()}), null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting no entity when searching for any type and for Feature DESCRIPTION or CHROMOSOME_NUMBER");

        pager = taxonService.findByDescriptionElementFullText(Distribution.class, "Abies", null, subtree, null, null, false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting no entity when searching for Distribution");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Бальзам", null, subtree, null, Arrays.asList(new Language[]{}), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity");
        assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standard views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Бальзам", null, subtree, null, Arrays.asList(new Language[]{Language.RUSSIAN()}), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity");
        assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standard views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Бальзам", null, subtree, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting no entity");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, subtree, null, Arrays.asList(new Language[]{Language.GERMAN(), Language.RUSSIAN()}), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity");
        assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standard views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());
    }

    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_MultipleWords() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        // Pflanzenart aus der Gattung der Tannen
        long start = System.currentTimeMillis();

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(TextData.class, "Pflanzenart Tannen", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "OR search : Expecting one entity");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Pflanzenart Wespen", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "OR search : Expecting one entity");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "+Pflanzenart +Tannen", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "AND search : Expecting one entity");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "+Pflanzenart +Wespen", null, subtree, null, null, false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "AND search : Expecting no entity");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "\"Pflanzenart aus der Gattung der Tannen\"", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Phrase search : Expecting one entity");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "\"Pflanzenart aus der Gattung der Wespen\"", null, subtree, null, null, false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Phrase search : Expecting one entity");

        logger.info("testFindByDescriptionElementFullText_MultipleWords() duration: " + (System.currentTimeMillis() - start) + "ms");
    }

    @Test
    @CdmDataSet(cleanSweep = true)
    public final void testFindByDescriptionElementFullText_modify_DescriptionElement() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        //
        // modify the DescriptionElement
        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, subtree, null, Arrays.asList(new Language[]{Language.GERMAN(), Language.RUSSIAN()}), false, null, null, null, null);
        assertTrue(pager.getRecords().size() > 0, "Search did not return any results");
        assertTrue(pager.getRecords().get(0).getDocs().size() == 1, "Expecting only one doc");
        Document indexDocument = pager.getRecords().get(0).getDocs().iterator().next();
        String[] descriptionElementUuidStr = indexDocument.getValues("uuid");
        String[] inDescriptionUuidStr = indexDocument.getValues("inDescription.uuid");
        // is only one uuid!
        DescriptionElementBase textData = descriptionElementService.find(UUID.fromString(descriptionElementUuidStr[0]));

        ((TextData)textData).removeText(Language.GERMAN());
        ((TextData)textData).putText(Language.SPANISH_CASTILIAN(), "abeto bals"+UTF8.SMALL_A_ACUTE+"mico");

        descriptionElementService.save(textData);
        commitAndStartNewTransaction(null);
//        printDataSet(System.out, new String[] {
//                "DESCRIPTIONELEMENTBASE", "LANGUAGESTRING", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING" }
//        );

        //
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, subtree, null, Arrays.asList(new Language[]{Language.GERMAN(), Language.RUSSIAN()}), false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "The german 'Balsam-Tanne' TextData should no longer be indexed");
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "abeto", null, subtree, null, Arrays.asList(new Language[]{Language.SPANISH_CASTILIAN()}), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "expecting to find the SPANISH_CASTILIAN 'abeto bals"+UTF8.SMALL_A_ACUTE+"mico'");
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "bals"+UTF8.SMALL_A_ACUTE+"mico", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "expecting to find the SPANISH_CASTILIAN 'abeto bals"+UTF8.SMALL_A_ACUTE+"mico'");

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
        commitAndStartNewTransaction(null);

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "abeto", null, subtree, null, Arrays.asList(new Language[]{Language.SPANISH_CASTILIAN()}), false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "The spanish 'abeto bals"+UTF8.SMALL_A_ACUTE+"mico' TextData should no longer be indexed");
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "balsamiczna", null, subtree, null, Arrays.asList(new Language[]{Language.POLISH()}), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "expecting to find the POLISH 'Jod"+UTF8.POLISH_L+"a balsamiczna'");
    }

    @Test
    @CdmDataSet(cleanSweep = true)
    public final void testFindByDescriptionElementFullText_modify_Taxon() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;
        List<Language> german = List.of(Language.GERMAN());

        Taxon t_abies_balsamea = (Taxon)taxonService.find(ABIES_BALSAMEA_UUID);
        TaxonDescription d_abies_balsamea = (TaxonDescription)descriptionService.find(DESC_ABIES_BALSAMEA_UUID);

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne",
                null, subtree, null, german, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "expecting to find the GERMAN 'Balsam-Tanne'");

        // exchange the Taxon with another one via the Taxon object
        // 1.) remove existing description:
        t_abies_balsamea.removeDescription(d_abies_balsamea);

        commitAndStartNewTransaction(null);

        t_abies_balsamea = (Taxon)taxonService.find(t_abies_balsamea.getUuid());

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne",
                null, subtree, null, german, false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "'Balsam-Tanne' should no longer be found");

        // 2.) create new description and add to taxon:
        TaxonDescription d_abies_balsamea_new = TaxonDescription.NewInstance();
        descriptionService.save(d_abies_balsamea_new);
        d_abies_balsamea_new
                .addElement(TextData
                        .NewInstance(
                                "Die Balsamtanne ist mit bis zu 30 m Höhe ein mittelgro"+UTF8.SHARP_S+"er Baum und kann bis zu 200 Jahre alt werden",
                                Language.GERMAN(), null));
        t_abies_balsamea.addDescription(d_abies_balsamea_new);
        // set authorshipCache to null to avoid validation exception,
        // this is maybe not needed in future,  see ticket #3344
        IBotanicalName abies_balsamea = CdmBase.deproxy(t_abies_balsamea.getName(), TaxonName.class);
        abies_balsamea.setAuthorshipCache(null);
        commitAndStartNewTransaction(null);

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "mittelgro"+UTF8.SHARP_S+"er Baum",
                null, subtree, null, german, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "the taxon should be found via the new Description");
    }

    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_modify_Classification() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        // put taxon into other classification, new taxon node
        Classification classification = classificationService.find(CLASSIFICATION_UUID);
        Classification alternateClassification = classificationService.find(CLASSIFICATION_ALT_UUID);

        // TODO: why is the test failing when the childNode is already retrieved here, and not after the following four lines?
        //TaxonNode childNode = classification.getChildNodes().iterator().next();

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", null, subtree, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "expecting to find the GERMAN 'Balsam-Tanne' even if filtering by classification");
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne", alternateClassification, subtree, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "GERMAN 'Balsam-Tanne' should NOT be found in other classification");

        // check for the right taxon node
        TaxonNode childNode = classification.getChildNodes().iterator().next();
        assertEquals(childNode.getTaxon().getUuid(), ABIES_BALSAMEA_UUID, "expecting Abies balsamea sec.");
        assertEquals(childNode.getClassification().getUuid(), CLASSIFICATION_UUID, "expecting default classification");

        // moving the taxon around, the rootnode is only a proxy
        alternateClassification.setRootNode(HibernateProxyHelper.deproxy(alternateClassification.getRootNode()));
        alternateClassification.addChildNode(childNode, null, null);
        commitAndStartNewTransaction(null);

//        printDataSet(System.out, new String[] {
//            "TAXONBASE", "TAXONNODE", "CLASSIFICATION"
//        });

        // reload classification
        classification = classificationService.find(CLASSIFICATION_UUID);
        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne",
                alternateClassification, subtree, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "GERMAN 'Balsam-Tanne' should now be found in other classification");

        classification.getChildNodes().clear();
        commitAndStartNewTransaction(null);

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Balsam-Tanne",
                classification, subtree, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Now the GERMAN 'Balsam-Tanne' should NOT be found in original classification");

    }

    @SuppressWarnings("rawtypes")
    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_CategoricalData() throws IOException, LuceneParseException {

        TaxonNode subtree = null;
        // add CategoricalData
        DescriptionBase d_abies_balsamea = descriptionService.find(DESC_ABIES_BALSAMEA_UUID);
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

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(CategoricalData.class, "green", null, subtree, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity");
        assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standard views, 2013", pager.getRecords().get(0).getEntity().getTitleCache());
        assertTrue(pager.getRecords().get(0).getDocs().size() == 1, "Expecting only one doc");
        assertEquals("Abies balsamea sec. Kohlbecker, A., Testcase standard views, 2013", pager.getRecords().get(0).getDocs().iterator().next().get("inDescription.taxon.titleCache"));

        //TODO modify the StateData
        TaxonBase taxon = pager.getRecords().get(0).getEntity();

        String newName = "Quercus robur";
        taxon.setTitleCache(newName + " sec. ", true);
        commitAndStartNewTransaction(null);

        taxon = taxonService.find(taxon.getUuid());
        assertEquals(newName + " sec. ", taxon.getTitleCache());
        DefinedTermBase term = termService.find(termUUID);

        termService.delete(term);
    }

    @Test
    @CdmDataSet
    public final void testFindByDescriptionElementFullText_Highlighting() throws IOException, LuceneParseException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDescriptionElementFullText(TextData.class, "Abies", null, subtree, null, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for any TextData");
        SearchResult<TaxonBase> searchResult = pager.getRecords().get(0);
        assertTrue(searchResult.getFieldHighlightMap().size() > 0, "the map of highlighted fragments should contain at least one item");
        String[] fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        assertTrue(fragments[0].contains("<B>Abies</B>"), "first fragments should contains serch term");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Pflanzenart Tannen", null, subtree, null, null, true, null, null, null, null);
        searchResult = pager.getRecords().get(0);
        assertTrue(searchResult.getFieldHighlightMap().size() > 0, "Phrase search : Expecting at least one item in highlighted fragments");
        fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        assertTrue(fragments[0].contains("<B>Pflanzenart</B>") || fragments[0].contains("<B>Tannen</B>"), "first fragments should contains serch term");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "+Pflanzenart +Tannen", null, subtree, null, null, true, null, null, null, null);
        searchResult = pager.getRecords().get(0);
        assertTrue(searchResult.getFieldHighlightMap().size() > 0, "Phrase search : Expecting at least one item in highlighted fragments");
        fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        assertTrue(fragments[0].contains("<B>Pflanzenart</B>") && fragments[0].contains("<B>Tannen</B>"), "first fragments should contains serch term");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "\"Pflanzenart aus der Gattung der Tannen\"", null, subtree, null, null, true, null, null, null, null);
        searchResult = pager.getRecords().get(0);
        assertTrue(searchResult.getFieldHighlightMap().size() > 0, "Phrase search : Expecting at least one item in highlighted fragments");
        fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        assertTrue(fragments[0].contains("<B>Pflanzenart</B> <B>aus</B> <B>der</B> <B>Gattung</B> <B>der</B> <B>Tannen</B>"), "first fragments should contains serch term");

        pager = taxonService.findByDescriptionElementFullText(TextData.class, "Gatt*", null, subtree, null, null, true, null, null, null, null);
        searchResult = pager.getRecords().get(0);
        assertTrue(searchResult.getFieldHighlightMap().size() > 0, "Wildcard search : Expecting at least one item in highlighted fragments");
        fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        assertTrue(fragments[0].contains("<B>Gatt"), "first fragments should contains serch term");
    }

    @Test
    @CdmDataSet(cleanSweep = true)
    public final void testFindByFullText() throws IOException, LuceneParseException {

        refreshLuceneIndex();

        classificationService.find(CLASSIFICATION_UUID);
        TaxonNode subtree = null;

        boolean NO_UNPUBLISHED = false;

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByFullText(null, "Abies", null, subtree, includeUnpublished, null, true, null, null, null, null); // --> 7
//        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(8, pager.getCount().intValue(), "Expecting 8 entities");

        //subtree
        subtree = nodeService.find(ROOTNODE_CLASSIFICATION_5000);
        pager = taxonService.findByFullText(null, "Abies", null, subtree, includeUnpublished, null, true, null, null, null, null); // --> 0
        assertEquals(2, pager.getCount().intValue(), "Expecting 2 entities");
        subtree = null;

        pager = taxonService.findByFullText(null, "Abies", null, subtree, NO_UNPUBLISHED, null, true, null, null, null, null); // --> 7
//        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(6, pager.getCount().intValue(), "Expecting 6 entities");
        Synonym abiesSubalpina = (Synonym)taxonService.find(ABIES_SUBALPINA_UUID);

        //accepted published, syn not published
        abiesSubalpina.getAcceptedTaxon().setPublish(true);
        commitAndStartNewTransaction();
        pager = taxonService.findByFullText(null, "Abies", null, subtree, NO_UNPUBLISHED, null, true, null, null, null, null); // --> 7
        assertEquals(7, pager.getCount().intValue(), "Expecting 7 entities");

        //accepted published, syn published
        abiesSubalpina = (Synonym)taxonService.find(abiesSubalpina.getUuid());
        abiesSubalpina.setPublish(true);
        commitAndStartNewTransaction();
        pager = taxonService.findByFullText(null, "Abies", null, subtree, NO_UNPUBLISHED, null, true, null, null, null, null); // --> 7
        assertEquals(8, pager.getCount().intValue(), "Expecting 8 entities");

        //accepted not published, syn published
        abiesSubalpina = (Synonym)taxonService.find(abiesSubalpina.getUuid());
        abiesSubalpina.getAcceptedTaxon().setPublish(false);
        commitAndStartNewTransaction();
        pager = taxonService.findByFullText(null, "Abies", null, subtree, NO_UNPUBLISHED, null, true, null, null, null, null); // --> 7
        assertEquals(6, pager.getCount().intValue(), "Expecting 6 entities. Synonym and accepted should not be found, though synonym is published");

        pager = taxonService.findByFullText(Taxon.class, "Abies", null, subtree, includeUnpublished, null, true, null, null, null, null); // --> 6
        assertEquals(7, pager.getCount().intValue(), "Expecting 7 entities");

        pager = taxonService.findByFullText(Synonym.class, "Abies", null, subtree, includeUnpublished, null, true, null, null, null, null); // --> 1
        assertEquals(1, pager.getCount().intValue(), "Expecting 1 entity");
        pager = taxonService.findByFullText(Synonym.class, "Abies", null, subtree, NO_UNPUBLISHED, null, true, null, null, null, null); // --> 1
        assertEquals(0, pager.getCount().intValue(), "Expecting 0 entity");

        pager = taxonService.findByFullText(TaxonBase.class, "sec", null, subtree, includeUnpublished, null, true, null, null, null, null); // --> 7
        assertEquals(9, pager.getCount().intValue(), "Expecting 8 entities");

        pager = taxonService.findByFullText(null, "genus", null, subtree, includeUnpublished, null, true, null, null, null, null); // --> 1
        assertEquals(1, pager.getCount().intValue(), "Expecting 1 entity");

        pager = taxonService.findByFullText(Taxon.class, "subalpina", null, subtree, includeUnpublished, null, true, null, null, null, null); // --> 0
        assertEquals(0, pager.getCount().intValue(), "Expecting 0 entities");

        // synonym in classification ???
    }

    @Test
    @CdmDataSet
    public final void testPrepareByAreaSearch() throws IOException, LuceneParseException {

        List<PresenceAbsenceTerm> statusFilter = new ArrayList<>();
        List<NamedArea> areaFilter = new ArrayList<>();
        areaFilter.add(germany);
        areaFilter.add(canada);
        areaFilter.add(russia);

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByDistribution(areaFilter, statusFilter, null, null, 20, 0, null, null);
        assertEquals(Integer.valueOf(2), Integer.valueOf(pager.getRecords().size()), "Expecting 2 entities");

    }

    @Test
    @CdmDataSet
    public final void testFindTaxaAndNamesByFullText_synonymClassificationSubtree() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        Classification classification = null;
        TaxonNode subtree = null;

        //
        Pager<SearchResult<TaxonBase>> pager;
        EnumSet<TaxaAndNamesSearchMode> taxaAndSynonyms = EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished);
        pager = taxonService.findTaxaAndNamesByFullText(
                taxaAndSynonyms, "Abies", classification, subtree, null, null, null, true, null, null, null, null);
        assertEquals(8, pager.getCount().intValue(), "doTaxa & doSynonyms & unpublished");
        TaxonBase result3 = pager.getRecords().get(3).getEntity();

        //test paging
        pager = taxonService.findTaxaAndNamesByFullText(
                taxaAndSynonyms, "Abies", classification, subtree, null, null, null, true, 2, 1, null, null);
        assertEquals(2, pager.getRecords().size(), "doTaxa & doSynonyms & unpublished & second page & page size 2");
        TaxonBase result1 = pager.getRecords().get(1).getEntity();
        assertEquals(result3, result1, "Second result in second page with size=2 must be the same as 4th result in full result");

        EnumSet<TaxaAndNamesSearchMode> taxaOnly = EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.includeUnpublished);

        //classification
        classification = classificationService.find(CLASSIFICATION_UUID);
        pager = taxonService.findTaxaAndNamesByFullText(
                taxaAndSynonyms, "Abies", classification, subtree, null, null, null, true, null, null, null, null);
        assertEquals(2, pager.getCount().intValue(), "doTaxa & doSynonyms & unpublished");
            //taxa only
        pager = taxonService.findTaxaAndNamesByFullText(
                taxaOnly, "Abies", classification, subtree, null, null, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "doTaxa & unpublished");
            //synonyms only
        pager = taxonService.findTaxaAndNamesByFullText(
                taxaOnly, "Abies", classification, subtree, null, null, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "doSynonyms & unpublished");

        classification = null;

        //subtree
       subtree = nodeService.find(ROOTNODE_CLASSIFICATION_5000);
       pager = taxonService.findTaxaAndNamesByFullText(
               taxaAndSynonyms, "Abies", classification, subtree, null, null, null, true, null, null, null, null);
       assertEquals(2, pager.getCount().intValue(), "doTaxa & doSynonyms & unpublished");
         //taxa only
       pager = taxonService.findTaxaAndNamesByFullText(
               taxaOnly, "Abies", classification, subtree, null, null, null, true, null, null, null, null);
       assertEquals(1, pager.getCount().intValue(), "doTaxa & unpublished");
       subtree = null;

    }

    @Test
    @CdmDataSet
    public final void testFindTaxaAndNamesByFullText() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Classification alternateClassification = classificationService.find(CLASSIFICATION_ALT_UUID);
        Synonym abiesSubalpina = (Synonym)taxonService.find(ABIES_SUBALPINA_UUID);

        Pager<SearchResult<TaxonBase>> pager;
        EnumSet<TaxaAndNamesSearchMode> modes = EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished);
        pager = taxonService.findTaxaAndNamesByFullText(
                modes, "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(8, pager.getCount().intValue(), "doTaxa & doSynonyms & unpublished");
//      logPagerRecords(pager, Level.DEBUG);

         //unpublished
        pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(6, pager.getCount().intValue(), "doTaxa & doSynonyms, published only");

        //accepted published, syn not published
        abiesSubalpina.getAcceptedTaxon().setPublish(true);
        commitAndStartNewTransaction();
        pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(7, pager.getCount().intValue(), "doTaxa & doSynonyms, accepted published");

        //accepted published, syn published
        abiesSubalpina = (Synonym)taxonService.find(abiesSubalpina.getUuid());
        abiesSubalpina.setPublish(true);
        commitAndStartNewTransaction();
        pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(8, pager.getCount().intValue(), "Expecting 8 entities");

        //accepted not published, syn published
        abiesSubalpina = (Synonym)taxonService.find(abiesSubalpina.getUuid());
        abiesSubalpina.getAcceptedTaxon().setPublish(false);
        commitAndStartNewTransaction();
        pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(6, pager.getCount().intValue(), "Expecting 6 entities. Synonym and accepted should not be found, though synonym is published");

        EnumSet<TaxaAndNamesSearchMode> searchMode = EnumSet.allOf(TaxaAndNamesSearchMode.class);
        pager = taxonService.findTaxaAndNamesByFullText(
                searchMode, "Abies", null, subtree, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        assertEquals(8, pager.getCount().intValue(), "all search modes");
        searchMode.remove(TaxaAndNamesSearchMode.includeUnpublished);
        pager = taxonService.findTaxaAndNamesByFullText(
                searchMode, "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(6, pager.getCount().intValue(), "all search modes except unpublished");

        pager = taxonService.findTaxaAndNamesByFullText(EnumSet.allOf(TaxaAndNamesSearchMode.class),
                "Abies", alternateClassification, subtree, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        assertEquals(1, pager.getCount().intValue(), "all search modes, filtered by alternateClassification");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(2, pager.getCount().intValue(), "Expecting 2 entity");
        Set<UUID> uuids = getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "The real synonym should be contained");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "The pro parte synonym should be contained");
        //without published
        pager = taxonService.findTaxaAndNamesByFullText(EnumSet.of(TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting 0 entities");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxaByCommonNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting 0 entity");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxaByCommonNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Tanne", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(1, pager.getRecords().size(), "Expecting 1 entity");
        assertEquals(1, pager.getCount().intValue(), "Expecting 1 entity");
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxaByCommonNames),
                "Tanne", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(0, pager.getRecords().size(), "Expecting 0 entity");

        //misapplied names
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.includeUnpublished),
                "kawakamii", (Classification)null, subtree, null, null, null, true, null, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting 1 entity");
        //unpublish accepted taxon
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                "kawakamii", (Classification)null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting 0 entities");
        //published accepted taxon/misapplied name
        Taxon abiesBalsamea = (Taxon)taxonService.find(ABIES_BALSAMEA_UUID);
        abiesBalsamea.setPublish(true);
        commitAndStartNewTransaction();
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                "kawakamii", (Classification)null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting 1 entities");
        //unpublished misapplied name
        Taxon misapplied = (Taxon)taxonService.find(ABIES_KAWAKAMII_SEC_KOMAROV_UUID);
        misapplied.setPublish(false);
        commitAndStartNewTransaction();
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                "kawakamii", (Classification)null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting 0 entities");

    }

    @Test
    @CdmDataSet
    public final void testFindTaxaAndNamesByFullText_wildcard() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Pager<SearchResult<TaxonBase>> pager;
         pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "Abi*", null, subtree, null, null, null, true, null, null, null, null);
//      logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(6, pager.getCount().intValue(), "doTaxa & doSynonyms published only");
        pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "*bies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(6, pager.getCount().intValue(), "doTaxa & doSynonyms, published only");
        // logFreeTextSearchResults(pager, Level.ERROR, null);
        pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "?bies", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(6, pager.getCount().intValue(), "doTaxa & doSynonyms, published only");
        // logFreeTextSearchResults(pager, Level.ERROR, null);
        pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "*", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(7, pager.getCount().intValue(), "doTaxa & doSynonyms, published only");
    }

    @Test
    @CdmDataSet
    // @Disabled // FIXME: fails due  org.apache.lucene.queryparser.classic.ParseException: Cannot parse 'relatedFrom.titleCache:()': Encountered " ")" ") "" at line 1, column 24.
    public final void testFindTaxaAndNamesByFullText_empty_querString() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Pager<SearchResult<TaxonBase>> pager;
        pager = taxonService.findTaxaAndNamesByFullText(EnumSet.of(TaxaAndNamesSearchMode.doTaxa),
                "", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(7, pager.getCount().intValue(), "doTaxa, published only");

        pager = taxonService.findTaxaAndNamesByFullText(TaxaAndNamesSearchMode.taxaAndSynonyms(),
                "", null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(7, pager.getCount().intValue(), "doTaxa & doSynonyms, published only");

        pager = taxonService.findTaxaAndNamesByFullText(EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doMisappliedNames),
                null, null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(7, pager.getCount().intValue(), "doTaxa & doMisappliedNames published only");

        pager = taxonService.findTaxaAndNamesByFullText(EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.doTaxaByCommonNames),
                null, null, subtree, null, null, null, true, null, null, null, null);
        assertEquals(7, pager.getCount().intValue(), "doTaxa & doMisappliedNames & doTaxaByCommonNames , published only");
        // logFreeTextSearchResults(pager, Level.ERROR, null);
    }

    @Test
    @CdmDataSet
    //test for https://dev.e-taxonomy.eu/redmine/issues/7486
    public final void testFindTaxaAndNamesByFullText_synonymsAndMisapplied_7486() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        //misapplied names
        Pager<SearchResult<TaxonBase>> pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", (Classification)null, subtree, null, null, null, true, null, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(3, pager.getCount().intValue(), "Expecting 3 entity");
        Set<UUID> uuids = getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "The real synonym should be contained");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "The pro parte synonym should be contained");
        assertTrue(uuids.contains(ABIES_KAWAKAMII_SEC_KOMAROV_UUID), "The misapplied name should be contained");

        //test with area filter
        Set<NamedArea> area_germany = new HashSet<>();
        area_germany.add(germany);
        Set<PresenceAbsenceTerm> statusNative = new HashSet<>();
        statusNative.add(PresenceAbsenceTerm.NATIVE());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", (Classification)null, subtree, area_germany, statusNative, null, true, null, null, null, null);
        assertEquals(3, pager.getCount().intValue(), "Expecting 3 entity");
        uuids = getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "The real synonym should be contained");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "The pro parte synonym should be contained");
        assertTrue(uuids.contains(ABIES_KAWAKAMII_SEC_KOMAROV_UUID), "The misapplied name should be contained");

        //test failing area filter (test for a distribution/status that does not exist in DB)
        Set<PresenceAbsenceTerm> absent = new HashSet<>();
        absent.add(PresenceAbsenceTerm.ABSENT());

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", (Classification)null, subtree, area_germany, absent, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "Expecting 0 entity");
        uuids = getTaxonUuidSet(pager);

        //test failing area filter (test for a distribution/status that is attached to taxon which has no synonym and not misapplied name)
        Set<NamedArea> area_russia = new HashSet<>();
        area_russia.add(russia);
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", (Classification)null, subtree, area_russia, absent, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting only the accepted taxon Abies alba to be contained");
        uuids = getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_ALBA_UUID), "The accepted synonym should be contained");
    }

    @Test
    @CdmDataSet
    public final void testFindTaxaAndNamesByFullText_PhraseQuery() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Pager<SearchResult<TaxonBase>> pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "\"Abies alba\"", null, subtree, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        assertEquals(1, pager.getCount().intValue(), "doTaxa & doSynonyms with simple phrase query");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "\"Abies al*\"", null, subtree, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        assertEquals(1, pager.getCount().intValue(), "doTaxa & doSynonyms with complex phrase query");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "\"Abies*\"", null, subtree, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        assertEquals(8, pager.getCount().intValue(), "doTaxa & doSynonyms with simple phrase query");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "\"Abies*\"", null, subtree, null, null, null, true, null, null, null, null);
//        logPagerRecords(pager, Level.DEBUG);
        assertEquals(8, pager.getCount().intValue(), "doTaxa & doSynonyms with simple phrase query");

    }

    @Test
    @CdmDataSet
    public final void testFindTaxaAndNamesByFullText_Sort() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        List<OrderHint> orderHints = new ArrayList<>();

//        String[] docFields2log = new String[]{"id"};

        // SortById
        orderHints.addAll(OrderHint.ORDER_BY_ID.asList());
        Pager<SearchResult<TaxonBase>> pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa),
                "Abies", null, subtree, null, null, null, true, null, null, orderHints, null);
//        logSearchResults(pager, Level.DEBUG, docFields2log);
        int lastId = -1;
        for(SearchResult<TaxonBase> rs : pager.getRecords()){
            if(lastId != -1){
                assertTrue(lastId < rs.getEntity().getId(), "results not sorted by id");
            }
            lastId = rs.getEntity().getId();
        }

        orderHints.addAll(OrderHint.ORDER_BY_ID.asList());
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, subtree, null, null, null, true, null, null, orderHints, null);
//        logSearchResults(pager, Level.DEBUG, docFields2log);

        lastId = -1;
        for(SearchResult<TaxonBase> rs : pager.getRecords()){
            if(lastId != -1){
                assertTrue(lastId < rs.getEntity().getId(), "results not sorted by id");
            }
            lastId = rs.getEntity().getId();
        }

        // Sortby NOMENCLATURAL_SORT_ORDER TODO make assertions !!!
        orderHints.clear();
        orderHints.addAll(OrderHint.NOMENCLATURAL_SORT_ORDER.asList());
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms),
                "Abies", null, subtree, null, null, null, true, null, null, orderHints, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);

    }

    @Test
    @CdmDataSet
    public final void testFindTaxaAndNamesByFullText_AreaFilter_7487() throws IOException, LuceneParseException, LuceneMultiSearchException {

        //refresh index
        refreshLuceneIndex();

        //search parameters (subtree and distribution)
        TaxonNode subtree = null;
        Set<NamedArea> a_germany_canada_russia = new HashSet<>();
        a_germany_canada_russia.add(germany);
        a_germany_canada_russia.add(canada);
        a_germany_canada_russia.add(russia);
        Set<PresenceAbsenceTerm> present_native = new HashSet<>();
        present_native.add(PresenceAbsenceTerm.PRESENT());
        present_native.add(PresenceAbsenceTerm.NATIVE());

        //search
        Pager<SearchResult<TaxonBase>> pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);

        //assert
        assertEquals(2, pager.getCount().intValue(), "Synonyms with matching area filter");
        Set<UUID> uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "Synonym of balsamea should be in");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "Pro parte synonym of balsamea should be in");

        //change pro parte syn => partial syn
        Taxon t_abies_balsamea = (Taxon)taxonService.find(ABIES_BALSAMEA_UUID);
        Set<TaxonRelationship> relsTo = t_abies_balsamea.getProParteAndPartialSynonymRelations();
        assertEquals(1, relsTo.size());
        TaxonRelationship taxonRelation = relsTo.iterator().next();
        taxonRelation.setType(TaxonRelationshipType.PARTIAL_SYNONYM_FOR());
        commitAndStartNewTransaction(null);

        //should give same results as above
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        assertEquals(2, pager.getCount().intValue(), "Synonyms with matching area filter");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "Synonym of balsamea should be in");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "Partial synonym of balsamea should be in");

        //MISAPPLIED
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);

        //test with ordinary MAN
        assertEquals(3, pager.getCount().intValue(), "misappliedNames with matching area & status filter");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_KAWAKAMII_SEC_KOMAROV_UUID), "Misapplied name should be in");
        assertTrue(uuids.contains(ABIES_BALSAMEA_UUID), "Abies balsamea should be in");
        assertTrue(uuids.contains(ABIES_ALBA_UUID), "Abies alba should be in");

        //change MAN to proparte MAN
        t_abies_balsamea = (Taxon)taxonService.find(ABIES_BALSAMEA_UUID);
        relsTo = t_abies_balsamea.getMisappliedNameRelations();
        assertEquals(1, relsTo.size());
        taxonRelation = relsTo.iterator().next();
        assertEquals(taxonRelation.getFromTaxon().getUuid(), ABIES_KAWAKAMII_SEC_KOMAROV_UUID);
        taxonRelation.setType(TaxonRelationshipType.PRO_PARTE_MISAPPLIED_NAME_FOR());
        commitAndStartNewTransaction(null);

        //should still give the same result
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        assertEquals(3, pager.getCount().intValue(), "misapplied names with matching area & status filter");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_KAWAKAMII_SEC_KOMAROV_UUID), "Pro parte misapplied name should be in");
        assertTrue(uuids.contains(ABIES_BALSAMEA_UUID), "Abies balsamea should be in");
        assertTrue(uuids.contains(ABIES_ALBA_UUID), "Abies alba should be in");

        //change proparte MAN to partial MAN
        t_abies_balsamea = (Taxon)taxonService.find(ABIES_BALSAMEA_UUID);
        relsTo = t_abies_balsamea.getMisappliedNameRelations();
        assertEquals(1, relsTo.size());
        taxonRelation = relsTo.iterator().next();
        assertEquals(taxonRelation.getFromTaxon().getUuid(), ABIES_KAWAKAMII_SEC_KOMAROV_UUID);
        taxonRelation.setType(TaxonRelationshipType.PARTIAL_MISAPPLIED_NAME_FOR());
        commitAndStartNewTransaction(null);

        //should still give the same result
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        assertEquals(3, pager.getCount().intValue(), "misapplied names with matching area & status filter");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_KAWAKAMII_SEC_KOMAROV_UUID), "Partial misapplied name should be in");
        assertTrue(uuids.contains(ABIES_BALSAMEA_UUID), "Abies balsamea should be in");
        assertTrue(uuids.contains(ABIES_ALBA_UUID), "Abies alba should be in");

    }

    @Test
    @CdmDataSet
    public final void testFindTaxaAndNamesByFullText_AreaFilter() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Set<NamedArea> a_germany_canada_russia = new HashSet<>();
        a_germany_canada_russia.add(germany);
        a_germany_canada_russia.add(canada);
        a_germany_canada_russia.add(russia);

        Set<NamedArea> a_russia = new HashSet<>();
        a_russia.add(russia);

        Set<PresenceAbsenceTerm> present = new HashSet<>();
        present.add(PresenceAbsenceTerm.PRESENT());

        Set<PresenceAbsenceTerm> present_native = new HashSet<>();
        present_native.add(PresenceAbsenceTerm.PRESENT());
        present_native.add(PresenceAbsenceTerm.NATIVE());

        Set<PresenceAbsenceTerm> absent = new HashSet<>();
        absent.add(PresenceAbsenceTerm.ABSENT());

        Pager<SearchResult<TaxonBase>> pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, null, null, true, null, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(2, pager.getCount().intValue(), "Synonyms with matching area filter");
        Set<UUID> uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_ALBA_UUID), "Abies alba (accepted with distribution) should be in");
        assertTrue(uuids.contains(ABIES_BALSAMEA_UUID), "Abies balsamea (accepted with distribution) should be in");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        assertEquals(2, pager.getCount().intValue(), "Synonyms with matching area filter");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "Synonym of balsamea should be in");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "Pro parte synonym of balsamea should be in");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, null, null, true, null, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertEquals(4, pager.getCount().intValue(), "taxa and synonyms with matching area filter");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_ALBA_UUID), "Accepted taxon with area should be in");
        assertTrue(uuids.contains(ABIES_BALSAMEA_UUID), "Accepted taxon with area should be in");
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "Synonym of balsamea should be in");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "Pro parte synonym of balsamea should be in");
        assertFalse(uuids.contains(ABIES_KAWAKAMII_SEC_KOMAROV_UUID), "Misapplied name should NOT be in");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        assertEquals(4, pager.getCount().intValue(), "taxa and synonyms with matching area & status filter 4");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "Synonym of balsamea should be in");
        assertTrue(uuids.contains(ABIES_ALBA_UUID), "Accepted taxon with area should be in");
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "Synonym of balsamea should be in");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "Pro parte synonym of balsamea should be in");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present, null, true, null, null, null, null);
        assertEquals(3, pager.getCount().intValue(), "taxa and synonyms with matching area & status filter 3");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_BALSAMEA_UUID), "Abies balsamea (accepted taxon) should be in");
        assertTrue(uuids.contains(ABIES_SUBALPINA_UUID), "Synonym of balsamea should be in");
        assertTrue(uuids.contains(ABIES_LASIOCARPA_UUID), "Pro parte synonym of balsamea should be in");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxa, TaxaAndNamesSearchMode.doSynonyms, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_russia, present, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "taxa and synonyms with non matching area & status filter");

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doTaxaByCommonNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Tanne", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "ByCommonNames with area filter");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_BALSAMEA_UUID), "Abies balsamea should be in");

        // abies_kawakamii_sensu_komarov as misapplied name for t_abies_balsamea
        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "misappliedNames with matching area & status filter");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_KAWAKAMII_SEC_KOMAROV_UUID), "Misapplied name should  be in");

        // 1. remove existing taxon relation
        Taxon t_abies_balsamea = (Taxon)taxonService.find(ABIES_BALSAMEA_UUID);
        Set<TaxonRelationship> relsTo = t_abies_balsamea.getMisappliedNameRelations();
        assertEquals(1, relsTo.size());
        TaxonRelationship taxonRelation = relsTo.iterator().next();
        t_abies_balsamea.removeTaxonRelation(taxonRelation);
        commitAndStartNewTransaction(null);

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, present_native, null, true, null, null, null, null);
        assertEquals(0, pager.getCount().intValue(), "misappliedNames with matching area & status filter, should match nothing now");

        // 2. now add abies_kawakamii_sensu_komarov as misapplied name for t_abies_alba and search for misapplications in Russia: ABSENT
        Taxon t_abies_kawakamii_sensu_komarov = (Taxon)taxonService.find(ABIES_KAWAKAMII_SEC_KOMAROV_UUID);
        Taxon t_abies_alba = (Taxon)taxonService.find(ABIES_ALBA_UUID);
        t_abies_alba.addMisappliedName(t_abies_kawakamii_sensu_komarov, null, null);
        commitAndStartNewTransaction(null);

        pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames, TaxaAndNamesSearchMode.includeUnpublished),
                "Abies", null, subtree, a_germany_canada_russia, absent, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "misappliedNames with matching area & status filter, should find one");
        uuids = this.getTaxonUuidSet(pager);
        assertTrue(uuids.contains(ABIES_KAWAKAMII_SEC_KOMAROV_UUID), "Misapplied name should  be in");

    }

    @Test
    @CdmDataSet
    //https://dev.e-taxonomy.eu/redmine/issues/5477
    // for general testing of misapplied names see also #testFindTaxaAndNamesByFullText_synonymsAndMisapplied_7486
    public final void testFindTaxaAndNamesByFullText_AreaFilter_issue5477() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;
        Set<NamedArea> a_germany_canada_russia = new HashSet<>();
        a_germany_canada_russia.add(germany);
        a_germany_canada_russia.add(canada);
        a_germany_canada_russia.add(russia);

        Set<PresenceAbsenceTerm> absent = new HashSet<>();
        absent.add(PresenceAbsenceTerm.ABSENT());

        Taxon t_abies_kawakamii_sensu_komarov = (Taxon)taxonService.find(ABIES_KAWAKAMII_SEC_KOMAROV_UUID); //id=5006
        Taxon t_abies_alba = (Taxon)taxonService.find(ABIES_ALBA_UUID); //id=5001
        t_abies_alba.addMisappliedName(t_abies_kawakamii_sensu_komarov, null, null);
        commitAndStartNewTransaction(null);

        Pager<SearchResult<TaxonBase>> pager = taxonService.findTaxaAndNamesByFullText(
                EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                "Abies", null, subtree, a_germany_canada_russia, absent, null, true, null, null, null, null);

        if (pager.getCount().intValue() == 1) {
            assertEquals(1, pager.getCount().intValue(), "MisappliedNames with matching area & status filter, should find one");
            assertEquals(5006, pager.getRecords().get(0).getEntity().getId(), "Misapplied name found should be taxon with id 5006");
        }else {
            //run the query again after refreshing the lucene index to test if it is
            //only an index refresh problem or a general problem
            refreshLuceneIndex();
            pager = taxonService.findTaxaAndNamesByFullText(
                    EnumSet.of(TaxaAndNamesSearchMode.doMisappliedNames),
                    "Abies", null, subtree, a_germany_canada_russia, absent, null, true, null, null, null, null);
            if (pager.getCount().intValue() == 1) {
                fail("The index was only correctly updated after fully refreshing the lucene index");
            } else {
                fail("The query on the lucene index generally does not work anymore");
            }
        }
    }

    /**
     * Regression test for #3119: fulltext search: Entity always null whatever search
     *
     * @throws IOException
     * @throws LuceneParseException
     * @throws LuceneMultiSearchException
     */
    @Test
    @CdmDataSet
    public final void testFindByEverythingFullText() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;
        EnumSet<TaxaAndNamesSearchMode> mode = TaxaAndNamesSearchMode.taxaAndSynonymsWithUnpublished();
        // via Taxon
        Pager<SearchResult<TaxonBase>>pager = taxonService.findByEverythingFullText("Abies", null, subtree, includeUnpublished, null, true, null, null, null, null);
//        Pager<SearchResult<TaxonBase>> pager = taxonService.findTaxaAndNamesByFullText(mode,
//                "Abies", null, null, null, null, true, null, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
        assertTrue(pager.getCount() > 7, "Expecting at least 7 entities for 'Abies'");
        assertNotNull(pager.getRecords().get(0).getEntity(), "Expecting entity");
//        assertEquals(Taxon.class, pager.getRecords().get(0).getEntity().getClass(), "Expecting Taxon entity");

        // via DescriptionElement
        pager = taxonService.findByEverythingFullText("present", null, subtree, includeUnpublished, null, true, null, null, null, null);
        //this is not covered by findTaxaAndNamesByFullText
//        pager = taxonService.findTaxaAndNamesByFullText(mode,
//                "present", null, null, null, null, true, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting one entity when searching for area 'present'");
        assertNotNull(pager.getRecords().get(0).getEntity(), "Expecting entity");
        assertEquals(Taxon.class, CdmBase.deproxy(pager.getRecords().get(0).getEntity()).getClass(), "Expecting Taxon entity");
        assertEquals(ABIES_BALSAMEA_UUID, pager.getRecords().get(0).getEntity().getUuid(), "Expecting Taxon ");
    }

    @Test
    @CdmDataSet
    public final void findByEveryThingFullText() throws IOException, LuceneParseException, LuceneMultiSearchException {

        refreshLuceneIndex();
        TaxonNode subtree = null;

        Classification classification = null;
        EnumSet<TaxaAndNamesSearchMode> mode = TaxaAndNamesSearchMode.taxaAndSynonymsWithUnpublished();

        Pager<SearchResult<TaxonBase>> pager = taxonService.findByEverythingFullText("genus", null, subtree, includeUnpublished, null, false, null, null, null, null); // --> 1
//        Pager<SearchResult<TaxonBase>> pager = taxonService.findTaxaAndNamesByFullText(mode,
//                "genus", classification, null, null, null, false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "Expecting 1 entity");

        //FIXME FAILS: abies balamea is returned twice, see also testFullText_Grouping()
        pager = taxonService.findByEverythingFullText("Balsam", null, subtree, includeUnpublished, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        logFreeTextSearchResults(pager, Level.DEBUG, null);
//        pager = taxonService.findTaxaAndNamesByFullText(EnumSet.allOf(TaxaAndNamesSearchMode.class),
//                "Balsam", classification, null, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        assertEquals(1, pager.getCount().intValue(), "expecting to find the Abies balsamea via the GERMAN DescriptionElements");

        //TODO fieldHighlight does not yet work
        pager = taxonService.findByEverythingFullText("Abies", null, subtree, includeUnpublished, null, true, null, null, null, null);
//        pager = taxonService.findTaxaAndNamesByFullText(mode,
//                "Abies", classification, null, null, Arrays.asList(new Language[]{Language.GERMAN()}), false, null, null, null, null);
        assertEquals(8, pager.getCount().intValue(), "Expecting 8 entities");
        SearchResult<TaxonBase> searchResult = pager.getRecords().get(0);
        assertTrue(searchResult.getFieldHighlightMap().size() > 0, "the map of highlighted fragments should contain at least one item");
        String[] fragments = searchResult.getFieldHighlightMap().values().iterator().next();
        assertTrue(fragments[0].toLowerCase().contains("<b>abies</b>"), "first fragments should contains serch term");
    }

//    @SuppressWarnings("rawtypes")
//    @Test
//    @CdmDataSet
//    public final void benchmarkFindTaxaAndNamesHql() throws IOException, LuceneParseException {
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
    @CdmDataSet
    public final void benchmarkFindByCommonNameHql() {

//        printDataSet(System.err, new String[] { "TaxonBase" });

        createRandomTaxonWithCommonName(NUM_OF_NEW_RADOM_ENTITIES);

        IFindTaxaAndNamesConfigurator configurator = FindTaxaAndNamesConfiguratorImpl.NewInstance();
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

    @Test
    @CdmDataSet
    public final void benchmarkFindByCommonNameLucene() throws IOException, LuceneParseException {
        TaxonNode subtree = null;
        createRandomTaxonWithCommonName(NUM_OF_NEW_RADOM_ENTITIES);

        refreshLuceneIndex();

        Pager<SearchResult<TaxonBase>> pager;

        long startMillis = System.currentTimeMillis();
        for (int indx = 0; indx < BENCHMARK_ROUNDS; indx++) {
            pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Wei"+UTF8.SHARP_S+"*", null, subtree, null, null, false, null, null, null, null);
            if (logger.isDebugEnabled()) {
                logger.debug("[" + indx + "]" + pager.getRecords().get(0).getEntity().getTitleCache());
            }
        }
        double duration = ((double) (System.currentTimeMillis() - startMillis)) / BENCHMARK_ROUNDS;
        logger.info("Benchmark result - [find taxon by CommonName via lucene] : " + duration + "ms (" + BENCHMARK_ROUNDS + " benchmark rounds )");
    }

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
     */
    private void createRandomTaxonWithCommonName(int numberOfNew) {

        logger.debug(String.format("creating %1$s random taxan with CommonName", numberOfNew));

        commitAndStartNewTransaction(null);

        Reference sec = ReferenceFactory.newBook();
        referenceService.save(sec);

        for (int i = numberOfNew; i < numberOfNew; i++) {
            RandomStringUtils.insecure().nextAlphabetic(10);
            String radomName = RandomStringUtils.insecure().nextAlphabetic(5) + " " + RandomStringUtils.insecure().nextAlphabetic(10);
            String radomCommonName = RandomStringUtils.insecure().nextAlphabetic(10);

            IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
            name.setNameCache(radomName, true);
            Taxon taxon = Taxon.NewInstance(name, sec);
            taxonService.save(taxon);

            TaxonDescription description = TaxonDescription.NewInstance(taxon);
            description.addElement(CommonTaxonName.NewInstance(radomCommonName, Language.GERMAN()));
            descriptionService.save(description);
        }

        commitAndStartNewTransaction(null);
    }

    private <T extends CdmBase> void logFreeTextSearchResults(Pager<SearchResult<T>> pager, Level level, String[] docFields){
        if(level == null){
            level = Level.DEBUG;
        }
        if(logger.isEnabled(level)){
            StringBuilder b = new StringBuilder();
            b.append("\n");
            int i = 0;
            for(SearchResult<?> sr : pager.getRecords()){

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

    private Set<UUID> getTaxonUuidSet(Pager<SearchResult<TaxonBase>> pager) {
        Set<UUID> result = new HashSet<>();
        for (SearchResult<TaxonBase> searchResult : pager.getRecords()){
            result.add(searchResult.getEntity().getUuid());
        }
        return result;
    }

    /**
     * uncomment @Test annotation to create the dataset for this test
     */
    @Override
//    @Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void createTestDataSet() throws FileNotFoundException {

        Classification europeanAbiesClassification = Classification.NewInstance("European Abies");
        europeanAbiesClassification.setUuid(CLASSIFICATION_UUID);
        classificationService.save(europeanAbiesClassification);

        Classification alternativeClassification = Classification.NewInstance("Abies alternative");
        alternativeClassification.setUuid(CLASSIFICATION_ALT_UUID);
        classificationService.save(alternativeClassification);

        Reference sec = ReferenceFactory.newBook();
        sec.setTitleCache("Kohlbecker, A., Testcase standard views, 2013", true);
        Reference sec_sensu = ReferenceFactory.newBook();
        sec_sensu.setTitleCache("Komarov, V. L., Flora SSSR 29", true);
        referenceService.save(sec);
        referenceService.save(sec_sensu);

        IBotanicalName n_abies = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        n_abies.setNameCache("Abies", true);
        Taxon t_abies = Taxon.NewInstance(n_abies, sec);
        taxonService.save(t_abies);

        IBotanicalName n_abies_alba = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        n_abies_alba.setNameCache("Abies alba", true);
        Taxon t_abies_alba = Taxon.NewInstance(n_abies_alba, sec);
        t_abies_alba.setUuid(ABIES_ALBA_UUID);
        taxonService.save(t_abies_alba);

        IBotanicalName n_abies_subalpina = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        n_abies_subalpina.setNameCache("Abies subalpina", true);
        Synonym s_abies_subalpina = Synonym.NewInstance(n_abies_subalpina, sec);
        taxonService.save(s_abies_subalpina);

        IBotanicalName n_abies_balsamea = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        n_abies_balsamea.setNameCache("Abies balsamea", true);
        Taxon t_abies_balsamea = Taxon.NewInstance(n_abies_balsamea, sec);
        t_abies_balsamea.setUuid(ABIES_BALSAMEA_UUID);
        t_abies_balsamea.addSynonym(s_abies_subalpina, SynonymType.SYNONYM_OF);
        taxonService.save(t_abies_balsamea);

        IBotanicalName n_abies_grandis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        n_abies_grandis.setNameCache("Abies grandis", true);
        Taxon t_abies_grandis = Taxon.NewInstance(n_abies_grandis, sec);
        taxonService.save(t_abies_grandis);

        IBotanicalName n_abies_kawakamii = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        n_abies_kawakamii.setNameCache("Abies kawakamii", true);
        Taxon t_abies_kawakamii = Taxon.NewInstance(n_abies_kawakamii, sec);
        t_abies_kawakamii.getTitleCache();
        taxonService.save(t_abies_kawakamii);

        // abies_kawakamii_sensu_komarov as missapplied name for t_abies_balsamea
        Taxon t_abies_kawakamii_sensu_komarov = Taxon.NewInstance(n_abies_kawakamii, sec_sensu);
        taxonService.save(t_abies_kawakamii_sensu_komarov);
        t_abies_kawakamii_sensu_komarov.addTaxonRelation(t_abies_balsamea, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null);

        IBotanicalName n_abies_lasiocarpa = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        n_abies_lasiocarpa.setNameCache("Abies lasiocarpa", true);
        Taxon t_abies_lasiocarpa = Taxon.NewInstance(n_abies_lasiocarpa, sec);
        taxonService.save(t_abies_lasiocarpa);

        // add taxa to classifications
        europeanAbiesClassification.addChildTaxon(t_abies_balsamea, null, null);
        alternativeClassification.addChildTaxon(t_abies_lasiocarpa, null, null);

        //
        // Description
        //
        TaxonDescription d_abies_alba = TaxonDescription.NewInstance(t_abies_alba);
        TaxonDescription d_abies_balsamea = TaxonDescription.NewInstance(t_abies_balsamea);

        d_abies_alba.setUuid(DESC_ABIES_ALBA_UUID);
        d_abies_balsamea.setUuid(DESC_ABIES_BALSAMEA_UUID);

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
        descriptionService.save(d_abies_balsamea);

        setComplete();
        endTransaction();

        writeDbUnitDataSetFile(new String[] {
            "TAXONBASE", "TAXONNAME",
            "TAXONRELATIONSHIP",
            "REFERENCE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE",
            "AGENTBASE", "HOMOTYPICALGROUP",
            "CLASSIFICATION", "TAXONNODE",
            "LANGUAGESTRING", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            });

    }

}
