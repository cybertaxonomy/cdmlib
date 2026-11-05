/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.filter.Restriction;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.AuditEventSort;
import eu.etaxonomy.cdm.persistence.query.GroupByCount;
import eu.etaxonomy.cdm.persistence.query.GroupByDate;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.NativeSqlOrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @author ben.clark
 */
public class TaxonDaoHibernateImplTest extends CdmTransactionalSpringIntegrationTest {

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private ITaxonNodeDao taxonNodeDao;

    @Autowired
    private IClassificationDao classificationDao;

    @Autowired
    private IReferenceDao referenceDao;

    @Autowired
    private IDefinedTermDao definedTermDao;

    private UUID uuid = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
    private UUID sphingidae = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");
    private UUID acherontia = UUID.fromString("c5cc8674-4242-49a4-aada-72d63194f5fa");
    private UUID rethera = UUID.fromString("a9f42927-e507-4fda-9629-62073a908aae");
    private UUID retheraSecCdmtest = UUID.fromString("a9f42927-e507-4fda-9629-62073a908aae");
    private UUID atroposAgassiz = UUID.fromString("d75b2e3d-7394-4ada-b6a5-93175b8751c1"); // a Synonym
    private UUID atroposOken = UUID.fromString("6bfedf25-6dbc-4d5c-9d56-84f9052f3b2a");  // a Synonym
    private UUID atroposLeach = UUID.fromString("3da4ab34-6c50-4586-801e-732615899b07"); // a Synonym
    private UUID acherontiaLachesis = UUID.fromString("b04cc9cb-2b4a-4cc4-a94a-3c93a2158b06");
    private UUID aus = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");

    private UUID UUID_ACHERONTIA_NODE = UUID.fromString("56b10cf0-9522-407e-9f90-0c2dba263c94");
    private UUID UUID_CLASSIFICATION2 = UUID.fromString("a71467a6-74dc-4148-9530-484628a5ab0e");

    private AuditEvent previousAuditEvent;
    private AuditEvent mostRecentAuditEvent;

    private UUID northernAmericaUuid;
    private UUID southernAmericaUuid;

    private UUID classificationUuid;

    private boolean includeUnpublished = true;
    private boolean NO_UNPUBLISHED = false;

    private static final boolean doTaxa = true;
    private static final boolean noTaxa = false;
    private static final boolean doSynonyms = true;
    private static final boolean noSynonyms = false;
    private static final boolean doMisapplied = true;
    private static final boolean noMisapplied = false;
    private static final boolean doCommonNames = true;
    private static final boolean noCommonNames = false;
    private static final boolean doAuthors = true;
    private static final boolean noAuthors = false;

//    @SuppressWarnings("unused")
//    private static final String[] TABLE_NAMES = new String[] {
//        "HOMOTYPICALGROUP", "HOMOTYPICALGROUP_AUD", "REFERENCE", "REFERENCE_AUD", "TAXONBASE", "TAXONBASE_AUD"
//        , "TAXONNAME", "TAXONNAME_AUD", "TAXONRELATIONSHIP", "TAXONRELATIONSHIP_AUD" };

    @BeforeEach
    public void setUp() {

        previousAuditEvent = new AuditEvent();
        previousAuditEvent.setRevisionNumber(1025);
        previousAuditEvent.setUuid(UUID.fromString("a680fab4-365e-4765-b49e-768f2ee30cda"));
        mostRecentAuditEvent = new AuditEvent();
        mostRecentAuditEvent.setRevisionNumber(1026);
        mostRecentAuditEvent.setUuid(UUID.fromString("afe8e761-8545-497b-9134-6a6791fc0b0d"));
        AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)

        northernAmericaUuid = UUID.fromString("2757e726-d897-4546-93bd-7951d203bf6f");
        southernAmericaUuid = UUID.fromString("6310b3ba-96f4-4855-bb5b-326e7af188ea");

        classificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");
        includeUnpublished = true;
    }

    @AfterEach
    public void tearDown() {
        AuditEventContextHolder.clearContext();
    }

    @Test
    @CdmDataSet
    public void testInit() {
        logger.warn("testInit()");
        assertNotNull(taxonDao, "Instance of ITaxonDao expected");
        assertNotNull(referenceDao, "Instance of IReferenceDao expected");
    }

    @Test
    @CdmDataSet
    public void testGetTaxaByName() {

        includeUnpublished= true;

        Reference sec = referenceDao.findById(1);
        assert sec != null : "sec must exist";

        List<? extends TaxonBase> results = taxonDao.getTaxaByName("Aus", includeUnpublished, sec);
        assertNotNull(results, "getTaxaByName should return a List");
        //assertFalse(results.isEmpty(), "The list should not be empty");
        assertEquals(1, results.size());

        String queryString = "A*";
        MatchMode matchMode = MatchMode.BEGINNING;
        results = taxonDao.getTaxaByName(doTaxa, noSynonyms, false, false, false, queryString, null, null, matchMode,
                null, includeUnpublished, null, null, null, null);

        assertNotNull(results, "getTaxaByName should return a List");

        int numberOfTaxaByName_A = 5;

        //logger.setLevel(Level.DEBUG); //FIXME #######################
        if (logger.isDebugEnabled()) {
            for (int i = 0; i < results.size(); i++) {
                String nameCache = "";
                TaxonName taxonName= results.get(i).getName();
                nameCache = taxonName.getNameCache();
                logger.debug(results.get(i).getClass() + "(" + i +")" +
                        ": Name Cache = " + nameCache + ", Title Cache = " + results.get(i).getTitleCache());
            }
        }

        assertEquals(numberOfTaxaByName_A, results.size());

        includeUnpublished = false;
        results = taxonDao.getTaxaByName(doTaxa, noSynonyms, false, false, false, queryString, null, null, matchMode,
                null, includeUnpublished, null, null, null, null);

        assertEquals(numberOfTaxaByName_A, results.size());  // no unpublished yet


        //System.err.println("Species group: " + Rank.SPECIESGROUP().getId() + "Species: " + Rank.SPECIES().getId() + "Section Botany: "+ Rank.SECTION_BOTANY());

//		assertEquals(results.get(0).getTitleCache(), "Abies sec. ???");
//		assertEquals(results.get(1).getTitleCache(), "Abies Mill.");
//		assertEquals(results.get(2).getTitleCache(), "Abies mill. sec. ???");
//		assertEquals(results.get(3).getTitleCache(), "Abies alba sec. ???");
//		assertEquals(results.get(4).getTitleCache(), "Abies alba Michx. sec. ???");
//		assertEquals(results.get(5).getTitleCache(), "Abies alba Mill. sec. ???");

        includeUnpublished = true;
        //TODO what is the difference to the first test?
        results = taxonDao.getTaxaByName(doTaxa, noSynonyms, false, false, false, queryString, null, null, matchMode,
                null, includeUnpublished, null, null, null, null);

        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(numberOfTaxaByName_A, results.size());

        queryString = "Aus";
        matchMode = MatchMode.EXACT;
        results = taxonDao.getTaxaByName(doTaxa, noSynonyms, false, false, false, queryString, null, null, matchMode,
                null, includeUnpublished, null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(1, results.size(), "Results list should contain one entity");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testGetTaxaByNameWithMisappliedNames(){

        Classification classification = classificationDao.load(classificationUuid);
        TaxonNode subtree = null;
        /* NOTE:
         * The testdata contains 3 misapplied names (1. nameCache = Aus, 2. nameCache = Rethera, 3. nameCache = Daphnis),
         * two contained in the classification used in this test,
         * the other one is not contained in any classification. This later case is the more general situation.
         * Misapplied names should be found regardless of whether they are contained in a classification or not.
         */
        //two accepted taxa starting with R in classification "TestBaum"
        List<TaxonBase> results = taxonDao.getTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "R*", classification, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(2, results.size(), "There should be 2 Taxa");

        //three taxa, 2 accepted and 1 misapplied name starting with R
        results = taxonDao.getTaxaByName(doTaxa, noSynonyms, doMisapplied, noCommonNames, false, "R*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(3, results.size(), "There should be 3 Taxa");

        //now even the orphaned synonym is found (#9047)
        results = taxonDao.getTaxaByName(doTaxa, doSynonyms, doMisapplied, noCommonNames, false, "A*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(13, results.size(), "There should be 13 Taxa");

        //two accepted taxa in classification and 1 misapplied name with accepted name in classification
        results = taxonDao.getTaxaByName(doTaxa, doSynonyms, doMisapplied, noCommonNames, false, "R*", classification, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(3, results.size(), "There should be 3 Taxa");
        //same with unpublished
        includeUnpublished = false;
        results = taxonDao.getTaxaByName(doTaxa, doSynonyms, doMisapplied, noCommonNames, false, "R*", classification, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(3, results.size(), "There should be 3 Taxa");
        includeUnpublished = true;


        //same as above because all taxa, synonyms and misapplied names starting with R are in the classification
        results = taxonDao.getTaxaByName(doTaxa, doSynonyms, doMisapplied, noCommonNames, false, "R*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(3, results.size(), "There should be 3 Taxa");

        //find misapplied names with accepted taxon in the classification, the accepted taxa of two misapplied names are in the classification
        results = taxonDao.getTaxaByName(noTaxa, noSynonyms, doMisapplied, noCommonNames, false, "*", classification, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(2, results.size(), "There should be 2 Taxa");

        //find misapplied names beginning with R
        results = taxonDao.getTaxaByName(noTaxa, noSynonyms, doMisapplied, noCommonNames, false, "R*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(1, results.size(), "There should be 1 Taxa");

        //find all three misapplied names
        results = taxonDao.getTaxaByName(noTaxa, noSynonyms, doMisapplied, noCommonNames, false, "*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(3, results.size(), "There should be 3 Taxa");

    }
    /**
     * Test for all not covered possibilities of searches
     */
    @Test
    @CdmDataSet(cleanSweep = true, value = "TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testGetTaxaByNameVariants(){
        TaxonNode subtree = null;
        List<TaxonBase> results = taxonDao.getTaxaByName(noTaxa, noSynonyms, noMisapplied, doCommonNames, false, "c*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(2, results.size(), "There should be 2 Taxa");

        results = taxonDao.getTaxaByName(noTaxa, noSynonyms, doMisapplied, doCommonNames, false, "R*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(1, results.size(), "There should be 1 Taxa");

        results = taxonDao.getTaxaByName(noTaxa, doSynonyms, doMisapplied, doCommonNames, false, "R*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(1, results.size(), "There should be 1 Taxa");

        results = taxonDao.getTaxaByName(noTaxa, doSynonyms, noMisapplied, doCommonNames, false, "c*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(2, results.size(), "There should be 2 Taxa");

        results = taxonDao.getTaxaByName(doTaxa, noSynonyms, noMisapplied, doCommonNames, false, "c*", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(2, results.size(), "There should be 2 Taxa");

        Classification classification = classificationDao.load(classificationUuid);
        results = taxonDao.getTaxaByName(noTaxa, noSynonyms, noMisapplied, doCommonNames, false, "c*", classification, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        assertEquals(1, results.size(), "There should be 1 Taxa");

        Set<NamedArea> namedAreas = new HashSet<>();
        namedAreas.add((NamedArea)definedTermDao.load(southernAmericaUuid));
        results = taxonDao.getTaxaByName(noTaxa, noSynonyms, noMisapplied, doCommonNames, false, "c*", null, subtree, MatchMode.BEGINNING,
                namedAreas, includeUnpublished, null, null, null, null);
        assertEquals(1, results.size(), "There should be 1 Taxa");
    }

    @Test
    @CdmDataSet
    public void testGetTaxaByNameForEditor() {
        TaxonNode subtree = null;
        Reference sec = referenceDao.findById(1);
        assert sec != null : "sec must exist";

        @SuppressWarnings("rawtypes")
        List<UuidAndTitleCache<? extends IdentifiableEntity>> results;

        results = taxonDao.getTaxaByNameForEditor(
                doTaxa, doSynonyms, noMisapplied, noCommonNames, false, includeUnpublished, noAuthors, "Acher", null, subtree, MatchMode.BEGINNING, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertFalse(results.isEmpty(), "The list should not be empty");
        assertEquals(4, results.size());

        results = taxonDao.getTaxaByNameForEditor(doTaxa, doSynonyms, noMisapplied, noCommonNames, false,includeUnpublished, noAuthors,"A", null, subtree, MatchMode.BEGINNING, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(7, results.size());

        results = taxonDao.getTaxaByNameForEditor(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, includeUnpublished,noAuthors, "A", null, subtree, MatchMode.BEGINNING, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(5, results.size());
        assertEquals(results.get(0).getType(), Taxon.class);

        results = taxonDao.getTaxaByNameForEditor(noTaxa, doSynonyms, noMisapplied, noCommonNames, false, includeUnpublished, noAuthors, "A", null, subtree, MatchMode.BEGINNING, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(2, results.size());
        assertEquals(results.get(0).getType(), Synonym.class);

        results = taxonDao.getTaxaByNameForEditor(doTaxa, doSynonyms, noMisapplied, noCommonNames, false,includeUnpublished, noAuthors, "Aus", null, subtree, MatchMode.EXACT, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(1, results.size(), "Results list should contain one entity");

        results = taxonDao.getTaxaByNameForEditor(doTaxa, doSynonyms, doMisapplied, noCommonNames, false, includeUnpublished, noAuthors, "A", null, subtree, MatchMode.BEGINNING, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(8, results.size(), "Results list should contain 8 entities");

        results = taxonDao.getTaxaByNameForEditor(doTaxa, doSynonyms, doMisapplied, noCommonNames, false, includeUnpublished, doAuthors, "Brachyglossa Boisduval", null, subtree, MatchMode.BEGINNING, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(1, results.size(), "Results list should contain 1 entity");

        results = taxonDao.getTaxaByNameForEditor(doTaxa, doSynonyms, doMisapplied, noCommonNames, false, includeUnpublished, noAuthors, "Brachyglossa Boisduval", null, subtree, MatchMode.BEGINNING, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(0, results.size(), "Results list should contain no entities");

        results = taxonDao.getTaxaByNameForEditor(doTaxa, doSynonyms, doMisapplied, noCommonNames, false, includeUnpublished, noAuthors, "Orphaned", null, subtree, MatchMode.BEGINNING, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(1, results.size(), "Results list should contain one entity, the orphaned synonym");

        //TODO: test the search for misapplied names
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}
     * restricting the search by a set of Areas.
     */
    @Test
    @CdmDataSet(cleanSweep = true, value = "TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testGetTaxaByNameAndArea() {
        TaxonNode subtree = null;
        Set<NamedArea> namedAreas = new HashSet<>();
        namedAreas.add((NamedArea)definedTermDao.load(northernAmericaUuid));

        Classification classification = classificationDao.findByUuid(classificationUuid);

        // prepare some synonym relation ships for some tests
        Synonym synAtroposAgassiz = (Synonym)taxonDao.findByUuid(atroposAgassiz);
        Taxon taxonRethera = (Taxon)taxonDao.findByUuid(rethera);
        taxonRethera.addSynonym(synAtroposAgassiz, SynonymType.SYNONYM_OF);
//        logger.warn("addSynonym(..)");

        Synonym synAtroposLeach = (Synonym)taxonDao.findByUuid(atroposLeach);
        Taxon taxonRetheraSecCdmTest = (Taxon)taxonDao.findByUuid(retheraSecCdmtest);
        taxonRetheraSecCdmTest.addSynonym(synAtroposLeach, SynonymType.SYNONYM_OF);
        this.taxonDao.save(taxonRetheraSecCdmTest);

        Taxon test = (Taxon)this.taxonDao.findByUuid(retheraSecCdmtest);
//        Set<Synonym> synonyms3 = test.getSynonyms();
        // 1. searching for a taxon (Rethera)
        //long numberOfTaxa = taxonDao.countTaxaByName(Taxon.class, "Rethera", null, MatchMode.BEGINNING, namedAreas);

        List<TaxonBase> results = taxonDao.getTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "Rethera", null, subtree, MatchMode.BEGINNING, namedAreas,
                includeUnpublished, null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertTrue(results.size() == 2, "expected to find two taxa but found "+results.size());

        results = taxonDao.getTaxaByName(noTaxa, noSynonyms, noMisapplied, doCommonNames, false, "com*", null, subtree, MatchMode.BEGINNING, namedAreas,
                includeUnpublished, null, null, null, null);
            assertNotNull(results, "getTaxaByName should return a List");
            assertTrue(results.size() == 1, "expected to find one taxon but found "+results.size());

        // 2. searching for a taxon (Rethera) contained in a specific classification
        results = taxonDao.getTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "Rethera", classification, subtree, MatchMode.BEGINNING, namedAreas,
                includeUnpublished, null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertTrue(results.size() == 1, "expected to find one taxon but found "+results.size());

        // 3. searching for Synonyms
        results = taxonDao.getTaxaByName(noTaxa, doSynonyms, noMisapplied, noCommonNames, false, "Atropo", null, subtree, MatchMode.ANYWHERE, null,
                includeUnpublished, null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        /*System.err.println(results.get(0).getTitleCache() + " - " +results.get(1).getTitleCache() + " - " +results.get(2).getTitleCache() );

        System.err.println(((Synonym)results.get(0)).getAcceptedTaxa().contains(taxonRethera)+ " - " +((Synonym)results.get(1)).getAcceptedTaxa().contains(taxonRethera)+ " - "  +((Synonym)results.get(2)).getAcceptedTaxa().contains(taxonRethera)+ " - "  );
        */
        assertTrue(results.size() == 3, "expected to find three taxa but found "+results.size());

        // 4. searching for Synonyms
        results = taxonDao.getTaxaByName(noTaxa, doSynonyms, noMisapplied, noCommonNames, false,"Atropo", null, subtree, MatchMode.BEGINNING, null,
                includeUnpublished, null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertTrue(results.size() == 3, "expected to find three taxa but found "+results.size());

        // 5. searching for a Synonyms and Taxa
        results = taxonDao.getTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames, false,"A", null, subtree, MatchMode.BEGINNING, namedAreas,
                includeUnpublished, null, null, null, null);
        //only five taxa have a distribution
        assertNotNull(results, "getTaxaByName should return a List");
        assertTrue(results.size() == 8, "expected to find 8 taxa but found "+results.size());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#findByNameTitleCache(Class<? extends TaxonBase>clazz, String queryString, Classification classification, TaxonNode subtree, MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageNumber, Integer pageSize, List<String> propertyPaths)}
     * restricting the search by a set of Areas.
     */
    @Test
    @CdmDataSet(cleanSweep = true, value = "TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testFindByNameTitleCache() {
        TaxonNode subtree = null;

        Set<NamedArea> namedAreas = new HashSet<>();
        namedAreas.add((NamedArea)definedTermDao.load(northernAmericaUuid));

        Classification classification = classificationDao.findByUuid(classificationUuid);

        // prepare some synonym relation ships for some tests
        Synonym synAtroposAgassiz = (Synonym)taxonDao.findByUuid(atroposAgassiz);
        Taxon taxonRethera = (Taxon)taxonDao.findByUuid(rethera);
        taxonRethera.addSynonym(synAtroposAgassiz, SynonymType.SYNONYM_OF);
        //logger.warn("addSynonym(..)");
        this.taxonDao.clear();
        Synonym synAtroposLeach = (Synonym)taxonDao.findByUuid(atroposLeach);
        Taxon taxonRetheraSecCdmtest = (Taxon)taxonDao.findByUuid(retheraSecCdmtest);
        taxonRetheraSecCdmtest.addSynonym(synAtroposLeach, SynonymType.SYNONYM_OF);
        this.taxonDao.clear();

        // 1. searching for a taxon (Rethera)
        //long numberOfTaxa = taxonDao.countTaxaByName(Taxon.class, "Rethera", null, MatchMode.BEGINNING, namedAreas);

        List<TaxonBase> results = taxonDao.findByNameTitleCache(doTaxa, noSynonyms, includeUnpublished, "Rethera Rothschild & Jordan, 1903", null, subtree, MatchMode.EXACT, namedAreas,
                null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(2, results.size(), "expected to find two taxa but found "+results.size());

        // 2. searching for a taxon (Rethera) contained in a specific classification
        results = taxonDao.findByNameTitleCache(doTaxa, noSynonyms, includeUnpublished, "Rethera Rothschild & Jordan, 1903", classification, subtree, MatchMode.EXACT, namedAreas,
                null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(1, results.size(), "expected to find one taxon but found "+results.size());

        // 3. searching for Synonyms
        results = taxonDao.findByNameTitleCache(noTaxa, doSynonyms, includeUnpublished, "*Atropo", null, subtree, MatchMode.ANYWHERE, null,
                null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(3, results.size(), "expected to find two taxa but found "+results.size());

        // 4. searching for Synonyms
        results = taxonDao.findByNameTitleCache(noTaxa, doSynonyms, includeUnpublished, "Atropo", null, subtree, MatchMode.BEGINNING, null,
                null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(3, results.size(), "expected to find two taxa but found "+results.size());

        // 5. searching for a Synonyms and Taxa
        //   attache a synonym first
        Synonym syn = (Synonym)taxonDao.findByUuid(this.atroposLeach);
        Taxon tax = (Taxon) taxonDao.findByUuid(rethera);
        tax.addSynonym(syn, SynonymType.HETEROTYPIC_SYNONYM_OF);

        taxonDao.save(tax);
        results = taxonDao.findByNameTitleCache(doTaxa, doSynonyms, includeUnpublished, "A", null, subtree, MatchMode.BEGINNING, namedAreas,
                null, null, null, null);
        assertNotNull(results, "getTaxaByName should return a List");
        assertEquals(8, results.size(), "expected to find 8 taxa but found "+results.size());
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testTaxonNameInTwoClassifications(){
        TaxonNode subtree = null;
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("taxonNodes");
        List<TaxonBase> taxa = taxonDao.getTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames, false,"P", null, subtree, MatchMode.BEGINNING,
                null, includeUnpublished, null, null, null, null);
        Taxon taxon = (Taxon)taxa.get(0);
        Set<TaxonNode> nodes = taxon.getTaxonNodes();
        assertTrue(nodes.size() == 1);
        //assertNotNull(taxa);
        //assertTrue(taxa.size() > 0);
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "TaxonDaoHibernateImplTest.publishFlag.xml")
    public void testGetTaxaByProtectedTitleCacheName(){
        boolean includeAuthors = false;

        //MODE BEGINNING
        List<TaxonBase> taxa = taxonDao.getTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames,
                includeAuthors,"Acheronti", null, null, MatchMode.BEGINNING, null, includeUnpublished,
                null, null, null, null);
        assertEquals(2, taxa.size(), "Both taxa have Acherontia");

        taxa = taxonDao.getTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames,
                includeAuthors,"Acherontia la", null, null, MatchMode.BEGINNING, null, includeUnpublished,
                null, null, null, null);
        assertEquals(1, taxa.size(), "Only Acherontia laspeyres Laspey., 1809 should be found as titleCache protected");
        assertEquals(15, taxa.iterator().next().getId());
        assertTrue(taxa.iterator().next().getName().isProtectedTitleCache());

        includeAuthors = true;
        taxa = taxonDao.getTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames,
                includeAuthors,"Acherontia la", null, null, MatchMode.BEGINNING, null, includeUnpublished,
                null, null, null, null);
        assertEquals(2, taxa.size(), "With authors both taxa should be found ");

        //MODE EXACT
        taxa = taxonDao.getTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames,
                includeAuthors,"Acheronti", null, null, MatchMode.EXACT, null, includeUnpublished,
                null, null, null, null);
        assertEquals(1, taxa.size(), "With protected titleCache the  ");

        taxa.iterator().next().getName().setProtectedTitleCache(false);
        taxa = taxonDao.getTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames,
                includeAuthors,"Acheronti", null, null, MatchMode.EXACT, null, includeUnpublished,
                null, null, null, null);
        assertEquals(0, taxa.size(), "No exact match should exist for ");

    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testGetTaxaByNameProParteSynonym(){
        TaxonNode subtree = null;
        List<TaxonBase> taxa = taxonDao.getTaxaByName(noTaxa, doSynonyms, noMisapplied, noCommonNames, false, "A", null, subtree,
                MatchMode.BEGINNING, null, includeUnpublished, null, null, null, null);
        assertEquals(4, taxa.size(), "3 synonyms (1 orphaned) and 1 pro parte synonym should be returned.");
        assertTrue(existsInCollection(taxa, acherontiaLachesis), "Pro parte should exist");
        assertTrue(existsInCollection(taxa, atroposAgassiz), "Normal synonym should exist");
        assertTrue(existsInCollection(taxa, atroposOken), "2. normal synonym should exist");

        taxa = taxonDao.getTaxaByName(noTaxa, doSynonyms, noMisapplied, noCommonNames, false, "A", null,subtree,
                MatchMode.BEGINNING, null, NO_UNPUBLISHED, null, null, null, null);
        assertEquals(2, taxa.size(), "2 synonyms and no pro parte synonym should be returned.");
        assertTrue(existsInCollection(taxa, atroposAgassiz), "Normal synonym should exist");
        assertTrue(existsInCollection(taxa, atroposOken), "2. normal synonym should exist");

        taxa = taxonDao.getTaxaByName(noTaxa, noSynonyms, doMisapplied, noCommonNames, false, "A", null,subtree,
                MatchMode.BEGINNING, null, includeUnpublished, null, null, null, null);
        assertEquals(1, taxa.size(), "1 misapplied name, no pro parte synonym should be returned.");
        assertTrue(existsInCollection(taxa, aus), "Pro parte should exist");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "TaxonDaoHibernateImplTest.publishFlag.xml")
    public void testListByRestritionsPublishFlag(){
        boolean onlyPublished = false;
        boolean includeUnpublished = true;
        List<Restriction<?>> restrictions = new ArrayList<>();
        restrictions.add(new Restriction<>("name.titleCache", MatchMode.BEGINNING, "Acherontia"));
        List<TaxonBase> taxa = taxonDao.list(TaxonBase.class, restrictions, null, null, null, Arrays.asList("name.titleCache"), includeUnpublished);
        assertEquals(2, taxa.size(), "expeting the published and unpublished taxon");
        taxa = taxonDao.list(TaxonBase.class, restrictions, null, null, null, Arrays.asList("name.titleCache"), onlyPublished);
        assertEquals(1, taxa.size(), "expeting the unpublished taxon to be excluded");
        assertTrue(taxa.get(0).isPublish());
    }

    @Test
    @CdmDataSet
    public void testFindByUuid() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(uuid);
        assertNotNull(taxon, "findByUuid should return a taxon");
        assertFalse(Hibernate.isInitialized(taxon.getName()), "findByUuid should not return a taxon with it's name initialized");
    }

    @Test
    @CdmDataSet
    public void testLoad() {
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("name");
        propertyPaths.add("secSource.citation");
        Taxon taxon = (Taxon)taxonDao.load(uuid, propertyPaths);
        assertNotNull(taxon, "findByUuid should return a taxon");
        assertTrue(Hibernate.isInitialized(taxon.getName()), "load should return a taxon with it's name initialized, given that the property was specified in the method");
        assertTrue(Hibernate.isInitialized(taxon.getSec()), "load should return a taxon with it's secundum reference initialized, given that the property was specified in the method");
    }

    @Test
    @CdmDataSet
    public void testCountTaxonRelationshipsByTaxon()	{
        Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon must exist";

        long numberOfRelatedTaxa = taxonDao.countTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                includeUnpublished, TaxonRelationship.Direction.relatedTo);
        assertEquals(8, numberOfRelatedTaxa, "countTaxonRelationships should return 8");
    }

    @Test
    @CdmDataSet
    public void testCountTaxaByName() {
        TaxonNode subtree = null;
        Classification classification= null;
        long numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames,false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(5, numberOfTaxa);
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "S", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(4, numberOfTaxa, "Sphingidae, Smerinthus, Smerinthus kindermannii and Sphingonaepiopsis expected");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "Smerinthus kindermannii", classification, subtree, MatchMode.EXACT, null, includeUnpublished);
        assertEquals(1, numberOfTaxa);
        numberOfTaxa = taxonDao.countTaxaByName(noTaxa, doSynonyms, noMisapplied, noCommonNames, false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(2, numberOfTaxa);
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames, false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(7, numberOfTaxa);
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames, false, "Aasfwerfwf fffe", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(0, numberOfTaxa);

        subtree = taxonNodeDao.findByUuid(UUID_ACHERONTIA_NODE);
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(3, numberOfTaxa, "Acherontia and 2 A. species expected");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "S", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(0, numberOfTaxa, "");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "Smerinthus kindermannii", classification, subtree, MatchMode.EXACT, null, includeUnpublished);
        assertEquals(0, numberOfTaxa, "Smerinthus is not in subtree");
        numberOfTaxa = taxonDao.countTaxaByName(noTaxa, doSynonyms, noMisapplied, noCommonNames, false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(2, numberOfTaxa, "Atropos Agassiz and Atropos Oken expected as Synonyms");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames, false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(5, numberOfTaxa, "The above accepted and synonyms expected");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames, false, "Aasfwerfwf fffe", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(0, numberOfTaxa);

        classification = classificationDao.findByUuid(UUID_CLASSIFICATION2);
        subtree = null;
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(3, numberOfTaxa, "Acherontia and 2 A. species expected");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "S", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(1, numberOfTaxa, "Sphingidae expected");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, noSynonyms, noMisapplied, noCommonNames, false, "Smerinthus kindermannii", classification, subtree, MatchMode.EXACT, null, includeUnpublished);
        assertEquals(0, numberOfTaxa, "Smerinthus is not in subtree");
        numberOfTaxa = taxonDao.countTaxaByName(noTaxa, doSynonyms, noMisapplied, noCommonNames, false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(2, numberOfTaxa, "Atropos Agassiz and Atropos Oken expected as Synonyms");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames, false, "A", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(5, numberOfTaxa, "The above accepted and synonyms expected");
        numberOfTaxa = taxonDao.countTaxaByName(doTaxa, doSynonyms, noMisapplied, noCommonNames, false, "Aasfwerfwf fffe", classification, subtree, MatchMode.BEGINNING, null, includeUnpublished);
        assertEquals(0, numberOfTaxa);
    }

    @Test
    @CdmDataSet
    public void testRelatedTaxa() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon must exist";

        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("fromTaxon");
        propertyPaths.add("fromTaxon.name");
        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("relatedFrom.name.genusOrUninomial", SortOrder.ASCENDING));
        orderHints.add(new OrderHint("relatedFrom.name.specificEpithet", SortOrder.ASCENDING));
        orderHints.add(new OrderHint("relatedFrom.name.infraSpecificEpithet", SortOrder.ASCENDING));

        long count = taxonDao.countTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                includeUnpublished, TaxonRelationship.Direction.relatedTo);
        assertEquals(8, count, "Count should return 8 (related taxa)");

        List<TaxonRelationship> relatedTaxa = taxonDao.getTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                includeUnpublished, null, null, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
        assertNotNull(relatedTaxa, "getRelatedTaxa should return a List");
        assertEquals(8, relatedTaxa.size(), "getRelatedTaxa should return all 8 related taxa");
        assertTrue(Hibernate.isInitialized(relatedTaxa.get(0).getFromTaxon()), "getRelatedTaxa should return TaxonRelationship objects with the relatedFrom taxon initialized");
        assertTrue(Hibernate.isInitialized(relatedTaxa.get(0).getFromTaxon().getName()), "getRelatedTaxa should return TaxonRelationship objects with the relatedFrom taxon initialized");

        assertEquals(relatedTaxa.get(0).getFromTaxon().getTitleCache(), "Acherontia Laspeyres, 1809 sec. cate-sphingidae.org", "Acherontia should appear first in the list of related taxa");
        assertEquals("Sphingonaepiopsis Wallengren, 1858 sec. cate-sphingidae.org", relatedTaxa.get(relatedTaxa.size()-1).getFromTaxon().getTitleCache(), "Sphingonaepiopsis should appear last in the list of related taxa");
    }

    @Test
    @CdmDataSet
    public void testGetRelatedTaxaPaged()	{
        Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon must exist";

        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("fromTaxon");
        propertyPaths.add("fromTaxon.name");

        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

        int pageSize = 3;
        List<TaxonRelationship> firstPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                includeUnpublished, pageSize, 0, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
        List<TaxonRelationship> secondPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                includeUnpublished, pageSize, 1, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
        List<TaxonRelationship> thirdPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                includeUnpublished, pageSize, 2, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);

        assertNotNull(firstPage, "getRelatedTaxa: 3, 0 should return a List");
        assertEquals(pageSize, firstPage.size(), "getRelatedTaxa: 3, 0 should return a List with 3 elements");
        assertNotNull(secondPage, "getRelatedTaxa: 3, 1 should return a List");
        assertEquals(pageSize, secondPage.size(), "getRelatedTaxa: 3, 1 should return a List with 3 elements");
        assertNotNull(thirdPage, "getRelatedTaxa: 3, 2 should return a List");
        assertEquals(2, thirdPage.size(), "getRelatedTaxa: 3, 2 should return a List with 2 elements");
    }

    @Test
    @CdmDataSet
    public void testCountSynonyms() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";

        long numberOfRelatedSynonym = taxonDao.countSynonyms(taxon,null);
        assertEquals(3, numberOfRelatedSynonym, "countSynonyms should return 3");
    }

    @Test
    @CdmDataSet
    public void testGetSynonyms()	{
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";
        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("synonym");
        propertyPaths.add("synonym.name");

        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("titleCache", SortOrder.ASCENDING));

        List<Synonym> synonyms = taxonDao.getSynonyms(taxon, null, null, null,orderHints,propertyPaths);

        assertNotNull(synonyms, "getSynonyms should return a List");
        assertEquals(3, synonyms.size(), "getSynonyms should return 3 synonyms");
        assertTrue(Hibernate.isInitialized(synonyms.get(0)), "getSynonyms should return synonym objects with the synonym initialized");
    }

    @Test
    @CdmDataSet
    public void testCountSynonymsByType()	{
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";

        long numberOfTaxonomicSynonyms = taxonDao.countSynonyms(taxon, SynonymType.HETEROTYPIC_SYNONYM_OF);
        assertEquals(3, numberOfTaxonomicSynonyms, "countSynonyms should return 4");
    }

    @Test
    @CdmDataSet
    public void testSynonymsByType() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";

        List<Synonym> synonyms = taxonDao.getSynonyms(taxon, SynonymType.HETEROTYPIC_SYNONYM_OF, null, null,null,null);

        assertNotNull(synonyms, "getSynonyms should return a List");
        assertEquals(3, synonyms.size(), "getSynonyms should return 4 Synonyms");
    }

    @Test
    @CdmDataSet
    public void testPageSynonyms(){
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";

        int pageSize = 2;
        List<Synonym> firstPage = taxonDao.getSynonyms(taxon, null, pageSize, 0,null,null);
        List<Synonym> secondPage = taxonDao.getSynonyms(taxon, null, pageSize, 1,null,null);

        assertNotNull(firstPage, "getSynonyms: 2, 0 should return a List");
        assertEquals(pageSize, firstPage.size(), "getSynonyms: 2, 0 should return 2 synonyms");
        assertNotNull(secondPage, "getSynonyms: 2, 1 should return a List");
        assertEquals(1, secondPage.size(), "getSynonyms: 2, 1 should return 1 synonym");
    }

    @Test
    @CdmDataSet
    public void testCountTaxonRelationships() {
        long count = taxonDao.countTaxonRelationships(null);
        assertEquals(11, count, "There should be 11 relationships");

        Set<TaxonRelationshipType> types = new HashSet<>();
        count = taxonDao.countTaxonRelationships(types);
        assertEquals(0, count, "Empty filter should return empty result");

        types.add(TaxonRelationshipType.CONGRUENT_TO());
        count = taxonDao.countTaxonRelationships(types);
        assertEquals(0, count, "There should be no congruent relationship");

        types.add(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN());
        count = taxonDao.countTaxonRelationships(types);
        assertEquals(11, count, "There should be 11 tax included relationships");
    }

    @Test
    @CdmDataSet
    public void testlistTaxonRelationships() {
        List<TaxonRelationship> rels = taxonDao.getTaxonRelationships(null, null, null, null, null);
        assertEquals(11, rels.size(), "There should be 11 relationships");

        rels = taxonDao.getTaxonRelationships(null, 2, 3, null, null);
        assertEquals(2, rels.size(), "There should be 11 relationships");

        Set<TaxonRelationshipType> types = new HashSet<>();
        rels = taxonDao.getTaxonRelationships(types, null, null, null, null);
        assertEquals(0, rels.size(), "Empty filter should return empty result");

        types.add(TaxonRelationshipType.CONGRUENT_TO());
        rels = taxonDao.getTaxonRelationships(types, null, null, null, null);
        assertEquals(0, rels.size(), "There should be no congruent relationship");

        types.add(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN());
        rels = taxonDao.getTaxonRelationships(types, null, null, null, null);
        assertEquals(11, rels.size(), "There should be 11 tax included relationships");
    }

    @Test
    @CdmDataSet("TaxonNodeDaoHibernateImplTest.xml")
    public void testListAcceptedTaxaFor()  {
        UUID acheontitia_ciprosus = UUID.fromString("3ef145f7-bd92-4a64-8afd-2b8203e00e02");

        Synonym synonym = (Synonym)taxonDao.findByUuid(acheontitia_ciprosus);
        assertNotNull(synonym, "synonym must exist");

        Taxon taxon = taxonDao.acceptedTaxonFor(synonym, null, null);
        assertNotNull(taxon, "listAcceptedTaxaFor should return a taxon");

        Classification classification = classificationDao.load(classificationUuid);
        assertNotNull(classification, "classification must exist");

        taxon = taxonDao.acceptedTaxonFor(synonym, classification, null);
        assertNull(taxon, "listAcceptedTaxaFor should return not taxon due to classification filter");
    }


    @Test
    @CdmDataSet
    public void testFindTaxaByName_Uninomial() {

        EnumSet<NomenclaturalCode> nameTypes = EnumSet.allOf(NomenclaturalCode.class);
        List<Taxon> result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", "*", "*", "*", "*",
                null, nameTypes, null, null, null);

        assertNotNull(result, "findTaxaByName should return a List");
        assertEquals(2, result.size(), "findTaxaByName should return two Taxa");
        assertEquals(5, result.get(0).getId(), "findTaxaByName should return a Taxon with id 5");
    }

    @Test
    @CdmDataSet
    public void testFindTaxaByName_SpeciesBinomial_And_authorship() {
        EnumSet<NomenclaturalCode> nameTypes = null;
        List<Taxon> result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", null, "kindermannii",
                null, "*", null, nameTypes, null, null, null);

        assertNotNull(result, "findTaxaByName should return a List");
        assertEquals(1, result.size(), "findTaxaByName should return one Taxon");
        assertEquals(8, result.get(0).getId(), "findTaxaByName should return a Taxon with id 8");

        //with author
        result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", null, "kindermannii",
                null, "Lederer, 1853", null, nameTypes, null, null, null);
        assertEquals(1, result.size(), "findTaxaByName should return one Taxon");
        assertEquals(8, result.get(0).getId(), "findTaxaByName should return a Taxon with id 8");

        //with incorrect author
        result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", null, "kindermannii",
                null, "No matching author", null, nameTypes, null, null, null);
        assertTrue(result.isEmpty(), "findTaxaByName should return no result");

        //with incorrect author
        result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", null, "kindermannii",
                null, null, null, nameTypes, null, null, null);
        assertTrue(result.isEmpty(), "findTaxaByName should return no result");

        //with asterisk author
        //TODO asterisk not yet supported
//        result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", null, "kindermannii",
//                null, "Ledere*", null, nameTypes, null, null, null);
//        assertEquals(1, result.size(), "findTaxaByName should return one Taxon");
//        assertEquals(8, result.get(0).getId(), "findTaxaByName should return a Taxon with id 8");

    }

    @Test
    @CdmDataSet
    public void testFindTaxaByName_Trinomial_And_NomCode() {
        EnumSet<NomenclaturalCode> nameTypes = EnumSet.allOf(NomenclaturalCode.class);
        List<Taxon> result = taxonDao.findTaxaByName(Taxon.class, "Cryptocoryne", null, "purpurea", "borneoensis", "*",
                null, nameTypes, null, null, null);

        assertNotNull(result, "findTaxaByName should return a List");
        assertEquals(1, result.size(), "findTaxaByName should return one Taxon");
        assertEquals(38, result.get(0).getId(), "findTaxaByName should return a Taxon with id 38");

        //filter on ICZN
        nameTypes = EnumSet.of(NomenclaturalCode.ICZN);
        result = taxonDao.findTaxaByName(Taxon.class, "Cryptocoryne", null, "purpurea", "borneoensis", "*",
                null, nameTypes, null, null, null);

        assertNotNull(result, "findTaxaByName should return a List");
        assertTrue(result.isEmpty(), "findTaxaByName should be empty as the only matching name is not a zoological name");

        //filter on ICNAFP
        nameTypes = EnumSet.of(NomenclaturalCode.ICNAFP);
        result = taxonDao.findTaxaByName(Taxon.class, "Cryptocoryne", null, "purpurea", "borneoensis", "*",
                null, nameTypes, null, null, null);

        assertNotNull(result, "findTaxaByName should return a List");
        assertEquals(1, result.size(), "findTaxaByName should return one Taxon");
        assertEquals(38, result.get(0).getId(), "findTaxaByName should return a Taxon with id 38");

        //filter on ICZN and ICNAFP
        nameTypes = EnumSet.of(NomenclaturalCode.ICNAFP, NomenclaturalCode.ICZN);
        result = taxonDao.findTaxaByName(Taxon.class, "Cryptocoryne", null, "purpurea", "borneoensis", "*",
                null, nameTypes, null, null, null);

        assertNotNull(result, "findTaxaByName should return a List");
        assertEquals(1, result.size(), "findTaxaByName should return one Taxon");
        assertEquals(38, result.get(0).getId(), "findTaxaByName should return a Taxon with id 38");
    }

    @Test
    @CdmDataSet
    public void testFindTaxaByName_NegativeMatch() {
        EnumSet<NomenclaturalCode> nameTypes = EnumSet.allOf(NomenclaturalCode.class);
        List<Taxon> result = taxonDao.findTaxaByName(Taxon.class, "Acherontia", null, "atropos", "dehli", null,
                null, nameTypes, null, null, null);

        assertNotNull(result, "findTaxaByName should return a List");
        assertTrue(result.isEmpty(), "findTaxaByName should return an empty List");
    }

    @Test
    @CdmDataSet
    public void testCountAllTaxa() {
        long numberOfTaxa = taxonDao.count(Taxon.class);
        assertEquals(14, numberOfTaxa, "count should return 14 taxa");
    }

    @Test
    @CdmDataSet
    public void testListAllTaxa() {
        List<Taxon> taxa = taxonDao.list(Taxon.class,100, 0);
        assertNotNull(taxa, "list should return a List");
        assertEquals(14, taxa.size(), "list should return 14 taxa");
    }

    @Test
    @CdmDataSet
   // @CdmExpectedDataSet
    public void testDelete() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";
        taxonDao.delete(taxon);
        taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon == null : "taxon must not exist";
        setComplete();
        endTransaction();
//        try {
//            printDataSet(new FileOutputStream("test.xml"), TABLE_NAMES);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    @CdmDataSet
    @CdmExpectedDataSet("TaxonDaoHibernateImplTest.testDelete-result.xml")
    public void testDeleteWithMarker() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        taxon.addMarker(Marker.NewInstance(MarkerType.IS_DOUBTFUL(), true));
        taxonDao.save(taxon);
        assert taxon != null : "taxon must exist";

        taxonDao.delete(taxon);
        commitAndStartNewTransaction(null);
        taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon == null : "taxon must not exist";
        setComplete();
        endTransaction();
//        try {
//            printDataSet(new FileOutputStream("test.xml"), TABLE_NAMES);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testFindDeleted() {
        TaxonBase taxon = taxonDao.findByUuid(acherontia);
        assertNull(taxon, "findByUuid should return null in this view");
        assertFalse(taxonDao.exists(acherontia), "exist should return false in this view");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testFindDeletedInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assertNotNull(taxon, "findByUuid should return a taxon in this view");
        assertTrue(taxonDao.exists(acherontia), "exists should return true in this view");

        try{
            assertEquals(3, taxon.getRelationsToThisTaxon().size(), "There should be 3 relations to this taxon in this view");
        } catch(Exception e) {
            fail("We should not experience any problems initializing proxies with envers");
        }
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testGetAuditEvents() {
        TaxonBase taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("name");
        propertyPaths.add("createdBy");
        propertyPaths.add("updatedBy");

        List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(taxon, null,null,null,propertyPaths);
        assertNotNull(auditEvents, "getAuditEvents should return a list");
        assertFalse(auditEvents.isEmpty(), "the list should not be empty");
        assertEquals(2, auditEvents.size(), "There should be two AuditEventRecords in the list");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testGetAuditEventsFromNow() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        TaxonBase taxon =  taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(taxon, null,null,AuditEventSort.FORWARDS,null);
        assertNotNull(auditEvents, "getAuditEvents should return a list");
        assertFalse(auditEvents.isEmpty(), "the list should not be empty");
        assertEquals(1, auditEvents.size(), "There should be one audit event in the list");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testCountAuditEvents() {
        TaxonBase taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        long numberOfAuditEvents = taxonDao.countAuditEvents(taxon, null);
        assertEquals(numberOfAuditEvents, 2, "countAuditEvents should return 2");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getPreviousAuditEvent() {
        TaxonBase taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        AuditEventRecord<TaxonBase> auditEvent = taxonDao.getPreviousAuditEvent(taxon);
        assertNotNull(auditEvent, "getPreviousAuditEvent should not return null as there is at least one audit event prior to the current one");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getPreviousAuditEventAtBeginning() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        TaxonBase taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        AuditEventRecord<TaxonBase> auditEvent = taxonDao.getPreviousAuditEvent(taxon);
        assertNull(auditEvent, "getPreviousAuditEvent should return null if we're at the first audit event anyway");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getNextAuditEvent() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        TaxonBase taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        AuditEventRecord<TaxonBase> auditEvent = taxonDao.getNextAuditEvent(taxon);
        assertNotNull(auditEvent, "getNextAuditEvent should not return null as there is at least one audit event after the current one");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getNextAuditEventAtEnd() {
        AuditEventContextHolder.getContext().setAuditEvent(mostRecentAuditEvent);
        TaxonBase taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        AuditEventRecord<TaxonBase> auditEvent = taxonDao.getNextAuditEvent(taxon);
        assertNull(auditEvent, "getNextAuditEvent should return null as there no more audit events after the current one");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testFind() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";

       assertEquals(1, taxon.getRelationsToThisTaxon().size(), "getRelationsToThisTaxon should contain 1 TaxonRelationship in this view");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testFindInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";

        assertTrue(taxon.getRelationsToThisTaxon().isEmpty(), "getRelationsToThisTaxon should contain 0 TaxonRelationship in this view");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testGetRelations() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";

        List<String> propertyPaths = new ArrayList<>();
         propertyPaths.add("fromTaxon");
         propertyPaths.add("fromTaxon.name");

         List<OrderHint> orderHints = new ArrayList<>();
         orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

        List<TaxonRelationship> taxonRelations = taxonDao.getTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                includeUnpublished, null, null,orderHints,propertyPaths, TaxonRelationship.Direction.relatedFrom);
        assertNotNull(taxonRelations, "getRelatedTaxa should return a list");
        assertEquals(1, taxonRelations.size(), "there should be one TaxonRelationship in the list in the current view");
        assertTrue(Hibernate.isInitialized(taxonRelations.get(0).getFromTaxon()), "TaxonRelationship.relatedFrom should be initialized");
        assertTrue(Hibernate.isInitialized(taxonRelations.get(0).getFromTaxon().getName()), "TaxonRelationship.relatedFrom.name should be initialized");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testCountRelations() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";
        assertEquals(1, taxonDao.countTaxonRelationships(
                taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                includeUnpublished, TaxonRelationship.Direction.relatedTo), "countRelatedTaxa should return 1 in the current view");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testGetRelationsInPreviousView() {
       AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
       Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
       assert taxon != null : "taxon cannot be null";

       List<String> propertyPaths = new ArrayList<String>();
       propertyPaths.add("relatedFrom");
       propertyPaths.add("relatedFrom.name");

       List<OrderHint> orderHints = new ArrayList<>();
       orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

       List<TaxonRelationship> taxonRelations = taxonDao.getTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
               includeUnpublished, null, null, orderHints,propertyPaths, TaxonRelationship.Direction.relatedFrom);
       assertNotNull(taxonRelations, "getRelatedTaxa should return a list");
       assertTrue(taxonRelations.isEmpty(), "there should be no TaxonRelationships in the list in the prior view");
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testCountRelationsInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";
        assertEquals(0, taxonDao.countTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),
                        includeUnpublished, TaxonRelationship.Direction.relatedTo), "countRelatedTaxa should return 0 in the current view");
    }

    @Test
    @CdmDataSet
    public void testGroupTaxa() {
        List<Grouping> groups = new ArrayList<>();
        groups.add(new GroupByCount("count", SortOrder.DESCENDING));
        groups.add(new Grouping("name.genusOrUninomial", "genus", "n", SortOrder.ASCENDING));
        List<Object[]> results = taxonDao.group(null, null, null, groups,null);
        if(logger.isTraceEnabled()){
            System.out.println("count\tname.genuOrUninomial");
            for(Object[] result : results) {
                System.out.println(result[0] + "\t" + result[1]);
            }
        }
    }

    @Test
    @CdmDataSet
    public void testGroupTaxaByClass() {
        List<Grouping> groups = new ArrayList<>();
        groups.add(new GroupByCount("count",SortOrder.DESCENDING));
        groups.add(new Grouping("class", "class",null, SortOrder.ASCENDING));
        List<Object[]> results = taxonDao.group(null, null, null, groups,null);
        if(logger.isTraceEnabled()){
            System.out.println("count\tclass");
            for(Object[] result : results) {
                System.out.println(result[0] + "\t" + result[1]);
            }
        }
    }

    @Test
    @CdmDataSet
    @Disabled //ignored since upgrading to hibernate 6 / using new criteria API
    public void testNativeSQLOrder() {
        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new NativeSqlOrderHint("case when {alias}.titleCache like 'C%' then 0 else 1 end", SortOrder.ASCENDING));

        List<TaxonBase> results = taxonDao.list(null, null, orderHints);
        if(logger.isTraceEnabled()){
            System.out.println("native SQL order");
            for(TaxonBase result : results) {
                System.out.println(result.getTitleCache());
            }
        }
    }

    @Test
    @CdmDataSet
    public void testGroupByDateTaxa() {
        List<Grouping> groups = new ArrayList<>();
        groups.add(new GroupByCount("count",null));
        groups.add(new GroupByDate("created", "dateGroup", SortOrder.ASCENDING, GroupByDate.Resolution.MONTH));
        List<Object[]> results = taxonDao.group(null, null, null, groups, null);
        if(logger.isTraceEnabled()){
            System.out.println("count\tyear\tmonth");
            for(Object[] result : results) {
                System.out.println(result[0] + "\t" + result[1] + "\t" + result[2]);
            }
        }
    }

    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    //NOTE: There is a problem with loading AuditEvents if this test runs
    //stand alone or as first (one of the first) in the test suite. For some reason
    //the AuditEvent records from the @CdmDataSet are not inserted into the database then,
    //while those inserted by the terms dataset are inserted as well as a completely new one.
    //This problem for some reason does not happen if not running at first place
    public void testGetAuditEventsByTypeWithRestrictions() {
        commitAndStartNewTransaction(new String[]{"AUDITEVENT", "TAXONBASE_AUD"});

        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("name");
        propertyPaths.add("createdBy");
        propertyPaths.add("updatedBy");

        List<AuditCriterion> criteria = new ArrayList<>();
        criteria.add(AuditEntity.property("lsid_lsid").isNotNull());

        long count = taxonDao.countAuditEvents(TaxonBase.class, null, null, null);
        assertTrue(count > 0);

        List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(
                TaxonBase.class, previousAuditEvent, mostRecentAuditEvent, criteria,
                null, null, AuditEventSort.FORWARDS, propertyPaths);
        assertNotNull(auditEvents, "getAuditEvents should return a list");
        assertFalse(auditEvents.isEmpty(), "the list should not be empty");
        assertEquals(1, auditEvents.size(), "There should be one AuditEventRecord in the list");
    }


    @Test
    @CdmDataSet(value = {
            "TaxonDaoHibernateImplTest.testFindDeleted.xml",
            "TaxonDaoHibernateImplTest.testFindDeletedAuditEvents.xml"
    })
    //NOTE: There is a problem with loading AuditEvents if this test runs
    //stand alone or as first (one of the first) in the test suite. For some reason
    //the AuditEvent records from the @CdmDataSet are not inserted into the database then,
    //while those inserted by the terms dataset are inserted as well as a completely new one.
    //This problem for some reason does not happen if not running at first place
    public void testGetAuditEventsByTypeWithNoRestrictions() {
//        printDataSet(System.out, new String[]{"AUDITEVENT", "TAXONBASE_AUD"});
        commitAndStartNewTransaction(new String[]{"AUDITEVENT", "TAXONBASE_AUD"});

        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("name");
        propertyPaths.add("createdBy");
        propertyPaths.add("updatedBy");
        long count = taxonDao.countAuditEvents(TaxonBase.class, null, null, null);
        List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(TaxonBase.class, previousAuditEvent, mostRecentAuditEvent, null,null, null, AuditEventSort.FORWARDS, propertyPaths);
        assertNotNull(auditEvents, "getAuditEvents should return a list");
        assertFalse(auditEvents.isEmpty(), "the list should not be empty");
        assertEquals(2, auditEvents.size(), "There should be thirty eight AuditEventRecords in the list");
    }


    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testGetCommonName(){
       List<Taxon> commonNameResults = taxonDao.getTaxaByCommonName("common%", null,
                MatchMode.BEGINNING, null, null, null, null);

        assertNotNull(commonNameResults, "getTaxaByCommonName should return a list");
        assertFalse(commonNameResults.isEmpty(), "the list should not be empty");
        assertEquals(2, commonNameResults.size(), "There should be two taxa with common name");

        List<UuidAndTitleCache<Taxon>> list = taxonDao.getTaxaByCommonNameForEditor(
                "common%", null, MatchMode.BEGINNING, null);

        assertNotNull(list, "getTaxaByCommonName should return a list");
        assertFalse(list.isEmpty(), "the list should not be empty");
        assertEquals(2, list.size(), "There should be two Taxon with common name");

    }

    @Test
    @CdmDataSet
    public void testGetTitleCache(){
        UUID uuid = UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331");
        String titleCache = taxonDao.getTitleCache(uuid, false);
        assertEquals("Acherontia styx Westwood, 1847 sec. cate-sphingidae.org", titleCache);
        titleCache = taxonDao.getTitleCache(uuid, true);
        assertEquals("Acherontia styxx Westwood, 1847 sec. Sphingidae", titleCache);
   }


    @Test
    @CdmDataSet("TaxonDaoHibernateImplTest.testPropertyPath.xml")
    public void testPropertyPath(){
        //Test that BeanInitializer also works on HiberanteProxys
        Classification c = classificationDao.load(UUID.fromString("4bceea53-893f-4685-8c63-6dcec6e85ab1"));
        TaxonNode singleNode = c.getRootNode().getChildNodes().iterator().next();
        Taxon taxonProxy = singleNode.getTaxon();
        assertTrue(taxonProxy instanceof HibernateProxy, "Object to test should be a proxy ");

        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("taxonNodes");
        Taxon taxon = (Taxon)this.taxonDao.load(
                UUID.fromString("4a5bc930-844f-45ec-aea8-dd155e1ab25f"),
                propertyPaths);
        assertSame(taxonProxy, taxon, "Returned object should be the same proxy to assure that we ran initialization on this proxy");
    }


    /**
     * {@inheritDoc}
     */
    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {
//        Classification classification  = Classification.NewInstance("Test");
//        BotanicalName TaxonName = null;
//        Reference sec = null;
//        Taxon taxon = Taxon.NewInstance(taxonName, sec);
//        classification.addChildTaxon(taxon, sec, null);
//
//        classificationDao.save(classification);
//        this.commitAndStartNewTransaction(null);
//
//        writeDbUnitDataSetFile(new String[] {
//                "CLASSIFICATION", "TAXONNAME",
//                "REFERENCE","TAXONNODE",
//                "TAXONBASE","LANGUAGESTRING",
//                "HIBERNATE_SEQUENCES" // IMPORTANT!!!
//                },
//                "testPropertyPath" );
    }

}
