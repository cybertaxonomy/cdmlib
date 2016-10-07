/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Level;
import org.hibernate.Hibernate;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.proxy.HibernateProxy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.GroupByCount;
import eu.etaxonomy.cdm.persistence.query.GroupByDate;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.NativeSqlOrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @author ben.clark
 *
 */
public class TaxonDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonDao taxonDao;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    IDefinedTermDao definedTermDao;

    private UUID uuid;
    private UUID sphingidae;
    private UUID acherontia;
    private UUID rethera;
    private UUID retheraSecCdmtest;
    private UUID atroposAgassiz; // a Synonym
    private UUID atroposLeach; // a Synonym
    private UUID acherontiaLachesis;

    private AuditEvent previousAuditEvent;
    private AuditEvent mostRecentAuditEvent;

    private UUID northernAmericaUuid;
    private UUID southernAmericaUuid;
    private UUID antarcticaUuid;

    private UUID classificationUuid;

    private static final String[] TABLE_NAMES = new String[] {
        "HOMOTYPICALGROUP", "HOMOTYPICALGROUP_AUD", "REFERENCE", "REFERENCE_AUD", "SYNONYMRELATIONSHIP", "SYNONYMRELATIONSHIP_AUD", "TAXONBASE", "TAXONBASE_AUD"
        , "TAXONNAMEBASE", "TAXONNAMEBASE_AUD", "TAXONRELATIONSHIP", "TAXONRELATIONSHIP_AUD" };


    @Before
    public void setUp() {

        uuid = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
        sphingidae = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");
        acherontia = UUID.fromString("c5cc8674-4242-49a4-aada-72d63194f5fa");
        acherontiaLachesis = UUID.fromString("b04cc9cb-2b4a-4cc4-a94a-3c93a2158b06");
        atroposAgassiz = UUID.fromString("d75b2e3d-7394-4ada-b6a5-93175b8751c1");
        atroposLeach =  UUID.fromString("3da4ab34-6c50-4586-801e-732615899b07");
        rethera = UUID.fromString("a9f42927-e507-4fda-9629-62073a908aae");
        retheraSecCdmtest = UUID.fromString("a9f42927-e507-4fda-9629-62073a908aae");

        previousAuditEvent = new AuditEvent();
        previousAuditEvent.setRevisionNumber(1025);
        previousAuditEvent.setUuid(UUID.fromString("a680fab4-365e-4765-b49e-768f2ee30cda"));
        mostRecentAuditEvent = new AuditEvent();
        mostRecentAuditEvent.setRevisionNumber(1026);
        mostRecentAuditEvent.setUuid(UUID.fromString("afe8e761-8545-497b-9134-6a6791fc0b0d"));
        AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)

        northernAmericaUuid = UUID.fromString("2757e726-d897-4546-93bd-7951d203bf6f");
        southernAmericaUuid = UUID.fromString("6310b3ba-96f4-4855-bb5b-326e7af188ea");
        antarcticaUuid = UUID.fromString("791b3aa0-54dd-4bed-9b68-56b4680aad0c");

        classificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");
    }

    @After
    public void tearDown() {
        AuditEventContextHolder.clearContext();
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#TaxonDaoHibernateImpl()}.
     */
    @Test
    @DataSet
    public void testInit() {
        logger.warn("testInit()");
        assertNotNull("Instance of ITaxonDao expected",taxonDao);
        assertNotNull("Instance of IReferenceDao expected",referenceDao);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}.
     */
    @Test
    @DataSet
    public void testGetTaxaByName() {
        Reference sec = referenceDao.findById(1);
        assert sec != null : "sec must exist";

        List<TaxonBase> results = taxonDao.getTaxaByName("Aus", sec);
        assertNotNull("getTaxaByName should return a List", results);
        //assertFalse("The list should not be empty", results.isEmpty());
        assertEquals(1, results.size());

        results = taxonDao.getTaxaByName("A*", MatchMode.BEGINNING, true, null, null);
        assertNotNull("getTaxaByName should return a List", results);

        int numberOfTaxaByName_A = 5;

        logger.setLevel(Level.DEBUG); //FIXME #######################
        if (logger.isDebugEnabled()) {
            for (int i = 0; i < results.size(); i++) {
                String nameCache = "";
                TaxonNameBase<?,?> taxonNameBase= results.get(i).getName();
                nameCache = HibernateProxyHelper.deproxy(taxonNameBase, NonViralName.class).getNameCache();
                logger.debug(results.get(i).getClass() + "(" + i +")" +
                        ": Name Cache = " + nameCache + ", Title Cache = " + results.get(i).getTitleCache());
            }
        }

        assertEquals(numberOfTaxaByName_A, results.size());


        //System.err.println("Species group: " + Rank.SPECIESGROUP().getId() + "Species: " + Rank.SPECIES().getId() + "Section Botany: "+ Rank.SECTION_BOTANY());

//		assertEquals(results.get(0).getTitleCache(), "Abies sec. ???");
//		assertEquals(results.get(1).getTitleCache(), "Abies Mill.");
//		assertEquals(results.get(2).getTitleCache(), "Abies mill. sec. ???");
//		assertEquals(results.get(3).getTitleCache(), "Abies alba sec. ???");
//		assertEquals(results.get(4).getTitleCache(), "Abies alba Michx. sec. ???");
//		assertEquals(results.get(5).getTitleCache(), "Abies alba Mill. sec. ???");

        results = taxonDao.getTaxaByName("A*", MatchMode.BEGINNING, true, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertEquals(numberOfTaxaByName_A, results.size());

        results = taxonDao.getTaxaByName("Aus", MatchMode.EXACT, true, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertEquals("Results list should contain one entity",1,results.size());
    }


    @Test
    @DataSet (loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testGetTaxaByNameWithMisappliedNames(){

        Classification classification = classificationDao.load(classificationUuid);

        /* NOTE:
         * The testdata contains 3 misapplied names (1. nameCache = Aus, 2. nameCache = Rethera, 3. nameCache = Daphnis), two contained in the classification used in this test,
         * the other one is not contained in any classification. This latter case is the more general situation.
         * Misapplied names should be found regardless of whether they are contained in a classification or not.
         */
        //two accepted taxa starting with R in classification "TestBaum"
        List<TaxonBase> results = taxonDao.getTaxaByName(true, false, false, "R*", classification, MatchMode.BEGINNING, null, null, null, null);
        Assert.assertEquals("There should be 2 Taxa", 2, results.size());

        //three taxa, 2 accepted and 1 misapplied name starting with R
        results = taxonDao.getTaxaByName(true, false, true, "R*", null, MatchMode.BEGINNING, null, null, null, null);
        Assert.assertEquals("There should be 3 Taxa", 3, results.size());

        //one synonym is not in a synonymrelationship
        results = taxonDao.getTaxaByName(true, true, true, "A*", null, MatchMode.BEGINNING, null, null, null, null);
        Assert.assertEquals("There should be 11 Taxa",11, results.size());

        //two accepted taxa in classification and 1 misapplied name with accepted name in classification
        results = taxonDao.getTaxaByName(true, true, true, "R*", classification, MatchMode.BEGINNING, null, null, null, null);
        Assert.assertEquals("There should be 3 Taxa", 3, results.size());

        //same as above because all taxa, synonyms and misapplied names starting with R are in the classification
        results = taxonDao.getTaxaByName(true, true, true, "R*", null, MatchMode.BEGINNING, null, null, null, null);
        Assert.assertEquals("There should be 3 Taxa", 3, results.size());

        //find misapplied names with accepted taxon in the classification, the accepted taxa of two misapplied names are in the classification
        results = taxonDao.getTaxaByName(false, false, true, "*", classification, MatchMode.BEGINNING, null, null, null, null);
        Assert.assertEquals("There should be 2 Taxa", 2, results.size());

        //find misapplied names beginning with R
        results = taxonDao.getTaxaByName(false, false, true, "R*", null, MatchMode.BEGINNING, null, null, null, null);
        Assert.assertEquals("There should be 1 Taxa", 1, results.size());

        //find all three misapplied names
        results = taxonDao.getTaxaByName(false, false, true, "*", null, MatchMode.BEGINNING, null, null, null, null);
        Assert.assertEquals("There should be 3 Taxa", 3, results.size());

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}.
     */
    @Test
    @DataSet
    public void testGetTaxaByNameForEditor() {
        Reference sec = referenceDao.findById(1);
        assert sec != null : "sec must exist";

        List<UuidAndTitleCache<IdentifiableEntity>> results = taxonDao.getTaxaByNameForEditor(true, true, false,false,"Acher", null, MatchMode.BEGINNING, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertFalse("The list should not be empty", results.isEmpty());
        assertEquals(4, results.size());


        results = taxonDao.getTaxaByNameForEditor(true, true, false, false,"A",null, MatchMode.BEGINNING, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertEquals(7, results.size());


        results = taxonDao.getTaxaByNameForEditor(true, false,false, false,"A", null,MatchMode.BEGINNING, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertEquals(5, results.size());
        assertEquals(results.get(0).getType(), Taxon.class);

        results = taxonDao.getTaxaByNameForEditor(false, true,false,false, "A", null,MatchMode.BEGINNING, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertEquals(2, results.size());
        assertEquals(results.get(0).getType(), Synonym.class);

        results = taxonDao.getTaxaByNameForEditor(true, true,false,false,"Aus", null,MatchMode.EXACT,  null);
        assertNotNull("getTaxaByName should return a List", results);
        assertEquals("Results list should contain one entity",1,results.size());

        results = taxonDao.getTaxaByNameForEditor(true, true,true,false,"A", null,MatchMode.BEGINNING,  null);
        assertNotNull("getTaxaByName should return a List", results);
        assertEquals("Results list should contain one entity", 8, results.size());

        //TODO: test the search for misapplied names

    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}
     * restricting the search by a set of Areas.
     */
    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testGetTaxaByNameAndArea() {

        Set<NamedArea> namedAreas = new HashSet<NamedArea>();
        namedAreas.add((NamedArea)definedTermDao.load(northernAmericaUuid));
        //namedAreas.add((NamedArea)definedTermDao.load(southernAmericaUuid));
        //namedAreas.add((NamedArea)definedTermDao.load(antarcticaUuid));

        Classification taxonmicTree = classificationDao.findByUuid(classificationUuid);

        // prepare some synonym relation ships for some tests
        Synonym synAtroposAgassiz = (Synonym)taxonDao.findByUuid(atroposAgassiz);
        Taxon taxonRethera = (Taxon)taxonDao.findByUuid(rethera);
        taxonRethera.addSynonym(synAtroposAgassiz, SynonymRelationshipType.SYNONYM_OF());
        logger.warn("addSynonym(..)");

        Synonym synAtroposLeach = (Synonym)taxonDao.findByUuid(atroposLeach);
        Taxon taxonRetheraSecCdmtest = (Taxon)taxonDao.findByUuid(retheraSecCdmtest);
        taxonRetheraSecCdmtest.addSynonym(synAtroposLeach, SynonymRelationshipType.SYNONYM_OF());
        this.taxonDao.save(taxonRetheraSecCdmtest);

        Taxon test = (Taxon)this.taxonDao.findByUuid(retheraSecCdmtest);
//        Set<Synonym> synonyms3 = test.getSynonyms();
        // 1. searching for a taxon (Rethera)
        //long numberOfTaxa = taxonDao.countTaxaByName(Taxon.class, "Rethera", null, MatchMode.BEGINNING, namedAreas);

        List<TaxonBase> results = taxonDao.getTaxaByName(true,false, false, "Rethera", null, MatchMode.BEGINNING, namedAreas,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertTrue("expected to find two taxa but found "+results.size(), results.size() == 2);

        // 2. searching for a taxon (Rethera) contained in a specific classification
        results = taxonDao.getTaxaByName(true, false, false, "Rethera", taxonmicTree, MatchMode.BEGINNING, namedAreas,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertTrue("expected to find one taxon but found "+results.size(), results.size() == 1);


        // 3. searching for Synonyms
        results = taxonDao.getTaxaByName(false, true, false, "Atropo", null, MatchMode.ANYWHERE, null,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        /*System.err.println(results.get(0).getTitleCache() + " - " +results.get(1).getTitleCache() + " - " +results.get(2).getTitleCache() );


        System.err.println(((Synonym)results.get(0)).getAcceptedTaxa().contains(taxonRethera)+ " - " +((Synonym)results.get(1)).getAcceptedTaxa().contains(taxonRethera)+ " - "  +((Synonym)results.get(2)).getAcceptedTaxa().contains(taxonRethera)+ " - "  );
        */
        assertTrue("expected to find three taxa but found "+results.size(), results.size() == 3);

        // 4. searching for Synonyms
        results = taxonDao.getTaxaByName(false, true, false, "Atropo", null, MatchMode.BEGINNING, null,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertTrue("expected to find three taxa but found "+results.size(), results.size() == 3);


        // 5. searching for a Synonyms and Taxa
        //   create a synonym relationship first
        results = taxonDao.getTaxaByName(true, true, false, "A", null, MatchMode.BEGINNING, namedAreas,
            null, null, null);
        //only five taxa have a distribution
        assertNotNull("getTaxaByName should return a List", results);
        assertTrue("expected to find 8 taxa but found "+results.size(), results.size() == 8);
    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#findByNameTitleCache(Class<? extends TaxonBase>clazz, String queryString, Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageNumber, Integer pageSize, List<String> propertyPaths)}
     * restricting the search by a set of Areas.
     */
    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testFindByNameTitleCache() {

        Set<NamedArea> namedAreas = new HashSet<NamedArea>();
        namedAreas.add((NamedArea)definedTermDao.load(northernAmericaUuid));
        //namedAreas.add((NamedArea)definedTermDao.load(southernAmericaUuid));
        //namedAreas.add((NamedArea)definedTermDao.load(antarcticaUuid));

        Classification classification = classificationDao.findByUuid(classificationUuid);

        // prepare some synonym relation ships for some tests
        Synonym synAtroposAgassiz = (Synonym)taxonDao.findByUuid(atroposAgassiz);
        Taxon taxonRethera = (Taxon)taxonDao.findByUuid(rethera);
        taxonRethera.addSynonym(synAtroposAgassiz, SynonymRelationshipType.SYNONYM_OF());
        logger.warn("addSynonym(..)");
        this.taxonDao.clear();
        Synonym synAtroposLeach = (Synonym)taxonDao.findByUuid(atroposLeach);
        Taxon taxonRetheraSecCdmtest = (Taxon)taxonDao.findByUuid(retheraSecCdmtest);
        taxonRetheraSecCdmtest.addSynonym(synAtroposLeach, SynonymRelationshipType.SYNONYM_OF());
        this.taxonDao.clear();
        // 1. searching for a taxon (Rethera)
        //long numberOfTaxa = taxonDao.countTaxaByName(Taxon.class, "Rethera", null, MatchMode.BEGINNING, namedAreas);

        List<TaxonBase> results = taxonDao.findByNameTitleCache(true, false, "Rethera Rothschild & Jordan, 1903", null, MatchMode.EXACT, namedAreas,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertTrue("expected to find two taxa but found "+results.size(), results.size() == 2);

        // 2. searching for a taxon (Rethera) contained in a specific classification
        results = taxonDao.findByNameTitleCache(true, false, "Rethera Rothschild & Jordan, 1903", classification, MatchMode.EXACT, namedAreas,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertTrue("expected to find one taxon but found "+results.size(), results.size() == 1);


        // 3. searching for Synonyms
        results = taxonDao.findByNameTitleCache(false, true, "*Atropo", null, MatchMode.ANYWHERE, null,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);

        assertTrue("expected to find two taxa but found "+results.size(), results.size() == 2);

        // 4. searching for Synonyms
        results = taxonDao.findByNameTitleCache(false, true, "Atropo", null, MatchMode.BEGINNING, null,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertTrue("expected to find two taxa but found "+results.size(), results.size() == 2);


        // 5. searching for a Synonyms and Taxa
        //   create a synonym relationship first
        Synonym syn = (Synonym)taxonDao.findByUuid(this.atroposLeach);
        Taxon tax = (Taxon) taxonDao.findByUuid(rethera);
        tax.addSynonym(syn, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

        taxonDao.save(tax);
        results = taxonDao.findByNameTitleCache(true, true, "A", null, MatchMode.BEGINNING, namedAreas,
            null, null, null);
        assertNotNull("getTaxaByName should return a List", results);
        assertTrue("expected to find 8 taxa but found "+results.size(), results.size() == 8);
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testTaxonNameInTwoClassifications(){
        int numberOfClassifications = classificationDao.count();
        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("taxonNodes");
        List<TaxonBase> taxa = taxonDao.getTaxaByName(true, true, false, "P", null, MatchMode.BEGINNING, null, null, null, null);
        Taxon taxon = (Taxon)taxa.get(0);
        Set<TaxonNode> nodes = taxon.getTaxonNodes();
        assertTrue(nodes.size() == 1);
        //assertNotNull(taxa);
        //assertTrue(taxa.size() > 0);
    }

    @Test
    @DataSet
    public void testFindByUuid() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(uuid);
        assertNotNull("findByUuid should return a taxon",taxon);
        assertFalse("findByUuid should not return a taxon with it's name initialized",Hibernate.isInitialized(taxon.getName()));
    }

    @Test
    @DataSet
    public void testLoad() {
        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("name");
        propertyPaths.add("sec");
        Taxon taxon = (Taxon)taxonDao.load(uuid, propertyPaths);
        assertNotNull("findByUuid should return a taxon",taxon);
        assertTrue("load should return a taxon with it's name initialized, given that the property was specified in the method",Hibernate.isInitialized(taxon.getName()));
        assertTrue("load should return a taxon with it's secundum reference initialized, given that the property was specified in the method",Hibernate.isInitialized(taxon.getSec()));
    }

    @Test
    @DataSet
    public void testCountTaxonRelationships()	{
        Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon must exist";

        int numberOfRelatedTaxa = taxonDao.countTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), TaxonRelationship.Direction.relatedTo);
        assertEquals("countTaxonRelationships should return 8", 8, numberOfRelatedTaxa);
    }

    @Test
    @DataSet
    public void testCountTaxaByName() {
        long numberOfTaxa = taxonDao.countTaxaByName(true, false, false, "A", null, MatchMode.BEGINNING, null);
        assertEquals(5, numberOfTaxa);
        numberOfTaxa = taxonDao.countTaxaByName(true, false, false, "Smerinthus kindermannii", null, MatchMode.EXACT, null);
        assertEquals(1, numberOfTaxa);
        numberOfTaxa = taxonDao.countTaxaByName(false, true, false, "A", null, MatchMode.BEGINNING, null);
        assertEquals(2, numberOfTaxa);
        numberOfTaxa = taxonDao.countTaxaByName(true, true, false, "A", null, MatchMode.BEGINNING, null);
        assertEquals(7, numberOfTaxa);
        numberOfTaxa = taxonDao.countTaxaByName(true, true, false, "Aasfwerfwf fffe", null, MatchMode.BEGINNING, null);
        assertEquals(0, numberOfTaxa);
//	FIXME implement test for search in specific classification
//		Reference reference = referenceDao.findByUuid(UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2"));
//		numberOfTaxa = taxonDao.countTaxaByName("A*", MatchMode.BEGINNING, SelectMode.ALL, null, null);
//		assertEquals(numberOfTaxa, 2);
    }

    @Test
    @DataSet
    public void testRelatedTaxa() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon must exist";

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("fromTaxon");
        propertyPaths.add("fromTaxon.name");
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("relatedFrom.name.genusOrUninomial", SortOrder.ASCENDING));
        orderHints.add(new OrderHint("relatedFrom.name.specificEpithet", SortOrder.ASCENDING));
        orderHints.add(new OrderHint("relatedFrom.name.infraSpecificEpithet", SortOrder.ASCENDING));

        List<TaxonRelationship> relatedTaxa = taxonDao.getTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), null, null, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
        assertNotNull("getRelatedTaxa should return a List",relatedTaxa);
        assertEquals("getRelatedTaxa should return all 8 related taxa", 8, relatedTaxa.size());
        assertTrue("getRelatedTaxa should return TaxonRelationship objects with the relatedFrom taxon initialized",Hibernate.isInitialized(relatedTaxa.get(0).getFromTaxon()));
        assertTrue("getRelatedTaxa should return TaxonRelationship objects with the relatedFrom taxon initialized",Hibernate.isInitialized(relatedTaxa.get(0).getFromTaxon().getName()));

        assertEquals("Acherontia should appear first in the list of related taxa", relatedTaxa.get(0).getFromTaxon().getTitleCache(), "Acherontia Laspeyres, 1809 sec. cate-sphingidae.org");
        assertEquals("Sphingonaepiopsis should appear last in the list of related taxa", "Sphingonaepiopsis Wallengren, 1858 sec. cate-sphingidae.org", relatedTaxa.get(relatedTaxa.size()-1).getFromTaxon().getTitleCache());
    }

    @Test
    @DataSet
    public void testGetRelatedTaxaPaged()	{
        Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon must exist";

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("fromTaxon");
        propertyPaths.add("fromTaxon.name");

        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

        int pageSize = 3;
        List<TaxonRelationship> firstPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), pageSize, 0, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
        List<TaxonRelationship> secondPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),pageSize, 1, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
        List<TaxonRelationship> thirdPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), pageSize, 2, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);

        assertNotNull("getRelatedTaxa: 3, 0 should return a List",firstPage);
        assertEquals("getRelatedTaxa: 3, 0 should return a List with 3 elements", pageSize,firstPage.size());
        assertNotNull("getRelatedTaxa: 3, 1 should return a List",secondPage);
        assertEquals("getRelatedTaxa: 3, 1 should return a List with 3 elements", pageSize, secondPage.size());
        assertNotNull("getRelatedTaxa: 3, 2 should return a List",thirdPage);
        assertEquals("getRelatedTaxa: 3, 2 should return a List with 2 elements", 2, thirdPage.size());
    }

    @Test
    @DataSet
    public void testCountSynonymRelationships() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";

        int numberOfSynonymRelationships = taxonDao.countSynonyms(taxon,null);
        assertEquals("countSynonymRelationships should return 3", 3, numberOfSynonymRelationships);
    }

    @Test
    @DataSet
    public void testSynonymRelationships()	{
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";
        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("synonym");
        propertyPaths.add("synonym.name");

        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

        List<SynonymRelationship> synonyms = taxonDao.getSynonyms(taxon, null, null, null,orderHints,propertyPaths);

        assertNotNull("getSynonyms should return a List", synonyms);
        assertEquals("getSynonyms should return 3 SynonymRelationship entities", 3, synonyms.size());
        assertTrue("getSynonyms should return SynonymRelationship objects with the synonym initialized",Hibernate.isInitialized(synonyms.get(0).getSynonym()));
    }

    @Test
    @DataSet
    public void testCountSynonymRelationshipsByType()	{
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";

        int numberOfTaxonomicSynonyms = taxonDao.countSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
        assertEquals("countSynonyms should return 4", 3, numberOfTaxonomicSynonyms);
    }

    @Test
    @DataSet
    public void testSynonymRelationshipsByType() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";

        List<SynonymRelationship> synonyms = taxonDao.getSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null,null,null);

        assertNotNull("getSynonyms should return a List", synonyms);
        assertEquals("getSynonyms should return 4 SynonymRelationship entities", 3, synonyms.size());
    }

    @Test
    @DataSet
    public void testPageSynonymRelationships(){
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assert taxon != null : "taxon must exist";

        int pageSize = 2;
        List<SynonymRelationship> firstPage = taxonDao.getSynonyms(taxon, null, pageSize, 0,null,null);
        List<SynonymRelationship> secondPage = taxonDao.getSynonyms(taxon, null, pageSize, 1,null,null);

        assertNotNull("getSynonyms: 2, 0 should return a List",firstPage);
        assertEquals("getSynonyms: 2, 0 should return 2 SynonymRelationships", pageSize,firstPage.size());
        assertNotNull("getSynonyms: 2, 1 should return a List",secondPage);
        assertEquals("getSynonyms: 2, 1 should return 1 SynonymRelationship", 1, secondPage.size());
    }

    @Test
    @DataSet("TaxonNodeDaoHibernateImplTest.xml")
    public void testListAcceptedTaxaFor()  {
        UUID acheontitia_ciprosus = UUID.fromString("3ef145f7-bd92-4a64-8afd-2b8203e00e02");

        Synonym synonym = (Synonym)taxonDao.findByUuid(acheontitia_ciprosus);
        assertNotNull("synonym must exist", synonym);

        List<Taxon> list = taxonDao.listAcceptedTaxaFor(synonym, null, 4, 0, null, null);
        assertNotNull("listAcceptedTaxaFor should return a List");
        assertEquals("listAcceptedTaxaFor should return 2 Taxa", 2,  list.size());

        Classification classification = classificationDao.load(classificationUuid);
        assertNotNull("classification must exist", classification);

        list = taxonDao.listAcceptedTaxaFor(synonym, classification, 4, 0, null, null);
        assertNotNull("listAcceptedTaxaFor should return a List");
        assertEquals("listAcceptedTaxaFor should return 1 Taxa", 1,  list.size());
    }


    @Test
    @DataSet
    public void testGetTaxonMatchingUninomial() {
        List<TaxonBase> result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", "*", "*", "*","*",null,null,null);

        assertNotNull("findTaxaByName should return a List", result);
        assertEquals("findTaxaByName should return two Taxa",2,result.size());
        assertEquals("findTaxaByName should return a Taxon with id 5",5,result.get(0).getId());
    }

    @Test
    @DataSet
    public void testGetTaxonMatchingSpeciesBinomial() {
        List<TaxonBase> result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", null, "kindermannii", null,"*",null,null,null);

        assertNotNull("findTaxaByName should return a List", result);
        assertEquals("findTaxaByName should return one Taxon",1,result.size());
        assertEquals("findTaxaByName should return a Taxon with id 8",8,result.get(0).getId());
    }

    @Test
    @DataSet
    public void testGetTaxonMatchingTrinomial() {
        List<TaxonBase> result = taxonDao.findTaxaByName(Taxon.class,"Cryptocoryne", null,"purpurea","borneoensis","*",null,null,null);

        assertNotNull("findTaxaByName should return a List", result);
        assertEquals("findTaxaByName should return one Taxon",1,result.size());
        assertEquals("findTaxaByName should return a Taxon with id 38",38,result.get(0).getId());
    }

    @Test
    @DataSet
    public void testNegativeMatch() {
        List<TaxonBase> result = taxonDao.findTaxaByName(Taxon.class,"Acherontia", null,"atropos","dehli",null,null,null,null);

        assertNotNull("findTaxaByName should return a List", result);
        assertTrue("findTaxaByName should return an empty List",result.isEmpty());
    }

    @Test
    @DataSet
    public void testCountAllTaxa() {
        int numberOfTaxa = taxonDao.count(Taxon.class);
        assertEquals("count should return 14 taxa", 14, numberOfTaxa);
    }

    @Test
    @DataSet
    public void testListAllTaxa() {
        List<Taxon> taxa = taxonDao.list(Taxon.class,100, 0);
        assertNotNull("list should return a List", taxa);
        assertEquals("list should return 14 taxa", 14, taxa.size());
    }

    @Test
    @DataSet
   // @ExpectedDataSet
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
    @DataSet
    @ExpectedDataSet("TaxonDaoHibernateImplTest.testDelete-result.xml")
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
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testFindDeleted() {
        TaxonBase<?> taxon = taxonDao.findByUuid(acherontia);
        assertNull("findByUuid should return null in this view", taxon);
        assertFalse("exist should return false in this view",taxonDao.exists(acherontia));
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testFindDeletedInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
        assertNotNull("findByUuid should return a taxon in this view",taxon);
        assertTrue("exists should return true in this view", taxonDao.exists(acherontia));

        try{
            assertEquals("There should be 3 relations to this taxon in this view",3,taxon.getRelationsToThisTaxon().size());
        } catch(Exception e) {
            fail("We should not experience any problems initializing proxies with envers");
        }
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testGetAuditEvents() {
        TaxonBase<?> taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("name");
        propertyPaths.add("createdBy");
        propertyPaths.add("updatedBy");

        List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(taxon, null,null,null,propertyPaths);
        assertNotNull("getAuditEvents should return a list",auditEvents);
        assertFalse("the list should not be empty",auditEvents.isEmpty());
        assertEquals("There should be two AuditEventRecords in the list",2, auditEvents.size());
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testGetAuditEventsFromNow() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        TaxonBase<?> taxon =  taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(taxon, null,null,AuditEventSort.FORWARDS,null);
        assertNotNull("getAuditEvents should return a list",auditEvents);
        assertFalse("the list should not be empty",auditEvents.isEmpty());
        assertEquals("There should be one audit event in the list",1,auditEvents.size());
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testCountAuditEvents() {
        TaxonBase<?> taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        int numberOfAuditEvents = taxonDao.countAuditEvents(taxon,null);
        assertEquals("countAuditEvents should return 2",numberOfAuditEvents,2);
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getPreviousAuditEvent() {
        TaxonBase<?> taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        AuditEventRecord<TaxonBase> auditEvent = taxonDao.getPreviousAuditEvent(taxon);
        assertNotNull("getPreviousAuditEvent should not return null as there is at least one audit event prior to the current one",auditEvent);
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getPreviousAuditEventAtBeginning() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        TaxonBase taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        AuditEventRecord<TaxonBase> auditEvent = taxonDao.getPreviousAuditEvent(taxon);
        assertNull("getPreviousAuditEvent should return null if we're at the first audit event anyway",auditEvent);
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getNextAuditEvent() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        TaxonBase<?> taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        AuditEventRecord<TaxonBase> auditEvent = taxonDao.getNextAuditEvent(taxon);
        assertNotNull("getNextAuditEvent should not return null as there is at least one audit event after the current one",auditEvent);
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getNextAuditEventAtEnd() {
        AuditEventContextHolder.getContext().setAuditEvent(mostRecentAuditEvent);
        TaxonBase<?> taxon = taxonDao.findByUuid(sphingidae);
        assert taxon != null : "taxon cannot be null";

        AuditEventRecord<TaxonBase> auditEvent = taxonDao.getNextAuditEvent(taxon);
        assertNull("getNextAuditEvent should return null as there no more audit events after the current one",auditEvent);
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testFind() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";

       assertEquals("getRelationsToThisTaxon should contain 1 TaxonRelationship in this view",1,taxon.getRelationsToThisTaxon().size());
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testFindInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";

        assertTrue("getRelationsToThisTaxon should contain 0 TaxonRelationship in this view",taxon.getRelationsToThisTaxon().isEmpty());
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testGetRelations() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";

        List<String> propertyPaths = new ArrayList<String>();
         propertyPaths.add("fromTaxon");
         propertyPaths.add("fromTaxon.name");

         List<OrderHint> orderHints = new ArrayList<OrderHint>();
         orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

        List<TaxonRelationship> taxonRelations = taxonDao.getTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), null, null,orderHints,propertyPaths, TaxonRelationship.Direction.relatedFrom);
        assertNotNull("getRelatedTaxa should return a list", taxonRelations);
        assertEquals("there should be one TaxonRelationship in the list in the current view",1,taxonRelations.size());
        assertTrue("TaxonRelationship.relatedFrom should be initialized",Hibernate.isInitialized(taxonRelations.get(0).getFromTaxon()));
        assertTrue("TaxonRelationship.relatedFrom.name should be initialized",Hibernate.isInitialized(taxonRelations.get(0).getFromTaxon().getName()));
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testCountRelations() {
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";
        assertEquals("countRelatedTaxa should return 1 in the current view",1, taxonDao.countTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), TaxonRelationship.Direction.relatedTo));
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testGetRelationsInPreviousView() {
       AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
       Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
       assert taxon != null : "taxon cannot be null";

       List<String> propertyPaths = new ArrayList<String>();
       propertyPaths.add("relatedFrom");
       propertyPaths.add("relatedFrom.name");

       List<OrderHint> orderHints = new ArrayList<OrderHint>();
       orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

       List<TaxonRelationship> taxonRelations = taxonDao.getTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), null, null,orderHints,propertyPaths, TaxonRelationship.Direction.relatedFrom);
       assertNotNull("getRelatedTaxa should return a list",taxonRelations);
       assertTrue("there should be no TaxonRelationships in the list in the prior view",taxonRelations.isEmpty());
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testCountRelationsInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
        assert taxon != null : "taxon cannot be null";
        assertEquals("countRelatedTaxa should return 0 in the current view",0, taxonDao.countTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), TaxonRelationship.Direction.relatedTo));
    }

    @Test
    @DataSet
    public void testGroupTaxa() {
        List<Grouping> groups = new ArrayList<Grouping>();
        groups.add(new GroupByCount("count",SortOrder.DESCENDING));
        groups.add(new Grouping("name.genusOrUninomial", "genus", "n", SortOrder.ASCENDING));
        List<Object[]> results = taxonDao.group(null, null, null, groups,null);
        System.out.println("count\tname.genuOrUninomial");
        for(Object[] result : results) {
            System.out.println(result[0] + "\t" + result[1]);
        }
    }

    @Test
    @DataSet
    public void testGroupTaxaByClass() {
        List<Grouping> groups = new ArrayList<Grouping>();
        groups.add(new GroupByCount("count",SortOrder.DESCENDING));
        groups.add(new Grouping("class", "class",null, SortOrder.ASCENDING));
        List<Object[]> results = taxonDao.group(null, null, null, groups,null);
        System.out.println("count\tclass");
        for(Object[] result : results) {
            System.out.println(result[0] + "\t" + result[1]);
        }
    }

    @Test
    @DataSet
    public void testNativeSQLOrder() {
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new NativeSqlOrderHint("case when {alias}.titleCache like 'C%' then 0 else 1 end",SortOrder.ASCENDING));

        List<TaxonBase> results = taxonDao.list(null, null, orderHints);
        System.out.println("native SQL order");
        for(TaxonBase<?> result : results) {
            System.out.println(result.getTitleCache());
        }
    }

    @Test
    @DataSet
    public void testGroupByDateTaxa() {
        List<Grouping> groups = new ArrayList<Grouping>();
        groups.add(new GroupByCount("count",null));
        groups.add(new GroupByDate("created", "dateGroup", SortOrder.ASCENDING, GroupByDate.Resolution.MONTH));
        List<Object[]> results = taxonDao.group(null, null, null, groups,null);
        System.out.println("count\tyear\tmonth");
        for(Object[] result : results) {
            System.out.println(result[0] + "\t" + result[1] + "\t" + result[2]);
        }
    }

    @Test
    @DataSet ("TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public final void testGetTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(){
        Classification classification = classificationDao.findByUuid(classificationUuid);
        List<UuidAndTitleCache<TaxonNode>> result = taxonDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification,  null, null);
        assertNotNull(result);
        assertEquals(5, result.size());

        //test exclude
        UUID excludeUUID = UUID.fromString("a9f42927-e507-4fda-9629-62073a908aae");
        List<UUID> excludeUUids = new ArrayList<UUID>();
        excludeUUids.add(excludeUUID);
        result = taxonDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification,  null, null);
        assertEquals(5, result.size());

        //test limit
        int limit = 2;
        result = taxonDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification,  limit, null);
        assertEquals(2, result.size());

        //test pattern
        String pattern = "*Rothschi*";
        result = taxonDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification, 2, pattern);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("0b5846e5-b8d2-4ca9-ac51-099286ea4adc", result.get(0).getUuid().toString());

    }


    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    //NOTE: There is a problem with loading AuditEvents if this test runs
    //stand alone or as first (one of the first) in the test suite. For some reason
    //the AuditEvent records from the @DataSet are not inserted into the database then,
    //while those inserted by the terms dataset are inserted as well as a completely new one.
    //This problem for some reason does not happen if not running at first place
    public void testGetAuditEventsByTypeWithRestrictions() {
        commitAndStartNewTransaction(new String[]{"AUDITEVENT", "TAXONBASE_AUD"});

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("name");
        propertyPaths.add("createdBy");
        propertyPaths.add("updatedBy");

        List<AuditCriterion> criteria = new ArrayList<AuditCriterion>();
        criteria.add(AuditEntity.property("lsid_lsid").isNotNull());

        int count = taxonDao.countAuditEvents(TaxonBase.class, null, null, null);

        List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(TaxonBase.class, previousAuditEvent, mostRecentAuditEvent, criteria,null, null, AuditEventSort.FORWARDS, propertyPaths);
        assertNotNull("getAuditEvents should return a list",auditEvents);
        assertFalse("the list should not be empty",auditEvents.isEmpty());
        assertEquals("There should be one AuditEventRecord in the list",1, auditEvents.size());
    }


    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
//    @DataSets({  //for testing only
//        @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml"),
//        @DataSet("TaxonDaoHibernateImplTest.testFindDeletedAuditEvents.xml")
//    })
    //NOTE: There is a problem with loading AuditEvents if this test runs
    //stand alone or as first (one of the first) in the test suite. For some reason
    //the AuditEvent records from the @DataSet are not inserted into the database then,
    //while those inserted by the terms dataset are inserted as well as a completely new one.
    //This problem for some reason does not happen if not running at first place
    public void testGetAuditEventsByTypeWithNoRestrictions() {
        printDataSet(System.out, new String[]{"AUDITEVENT", "TAXONBASE_AUD"});
        commitAndStartNewTransaction(new String[]{"AUDITEVENT", "TAXONBASE_AUD"});

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("name");
        propertyPaths.add("createdBy");
        propertyPaths.add("updatedBy");
        int count = taxonDao.countAuditEvents(TaxonBase.class, null, null, null);
        List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(TaxonBase.class, previousAuditEvent, mostRecentAuditEvent, null,null, null, AuditEventSort.FORWARDS, propertyPaths);
        assertNotNull("getAuditEvents should return a list", auditEvents);
        assertFalse("the list should not be empty", auditEvents.isEmpty());
        assertEquals("There should be thirty eight AuditEventRecords in the list", 2, auditEvents.size());
    }


    @Test
    @DataSet("TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public void testGetCommonName(){
       List<Taxon> commonNameResults = taxonDao.getTaxaByCommonName("common%", null,
                MatchMode.BEGINNING, null, null, null, null);

        assertNotNull("getTaxaByCommonName should return a list", commonNameResults);
        assertFalse("the list should not be empty", commonNameResults.isEmpty());
        assertEquals("There should be one Taxon with common name", 1,commonNameResults.size());
        assertEquals(" sec. ???", ((TaxonBase)commonNameResults.get(0)).getTitleCache());
    }

    @Test
    @DataSet
    public void testGetTitleCache(){
        UUID uuid = UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331");
        String titleCache = taxonDao.getTitleCache(uuid, false);
        Assert.assertEquals("Acherontia styx Westwood, 1847 sec. cate-sphingidae.org", titleCache);
        titleCache = taxonDao.getTitleCache(uuid, true);
        Assert.assertEquals("Acherontia styxx Westwood, 1847 sec. cate-sphingidae.org", titleCache);
   }


    @Test
    @DataSet("TaxonDaoHibernateImplTest.testPropertyPath.xml")
    public void testPropertyPath(){
        //Test that BeanInitializer also works on HiberanteProxys
        Classification c = classificationDao.load(UUID.fromString("4bceea53-893f-4685-8c63-6dcec6e85ab1"));
        TaxonNode singleNode = c.getRootNode().getChildNodes().iterator().next();
        Taxon taxonProxy = singleNode.getTaxon();
        Assert.assertTrue("Object to test should be a proxy ", taxonProxy instanceof HibernateProxy);

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("taxonNodes");
        Taxon taxon = (Taxon)this.taxonDao.load(
                UUID.fromString("4a5bc930-844f-45ec-aea8-dd155e1ab25f"),
                propertyPaths);
        Assert.assertSame("Returned object should be the same proxy to assure that we ran initialization on this proxy", taxonProxy, taxon);
    }

    /**
     * {@inheritDoc}
     */
    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {
//        Classification classification  = Classification.NewInstance("Test");
//        BotanicalName taxonNameBase = null;
//        Reference sec = null;
//        Taxon taxon = Taxon.NewInstance(taxonNameBase, sec);
//        classification.addChildTaxon(taxon, sec, null);
//
//        classificationDao.save(classification);
//        this.commitAndStartNewTransaction(null);
//
//        writeDbUnitDataSetFile(new String[] {
//                "CLASSIFICATION", "TAXONNAMEBASE",
//                "REFERENCE","TAXONNODE",
//                "TAXONBASE","LANGUAGESTRING",
//                "HIBERNATE_SEQUENCES" // IMPORTANT!!!
//                },
//                "testPropertyPath" );
    }

}
