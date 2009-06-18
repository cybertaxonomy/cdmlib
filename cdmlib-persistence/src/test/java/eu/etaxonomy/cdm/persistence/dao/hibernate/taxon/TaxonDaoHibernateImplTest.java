package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.SelectMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @author ben.clark
 *
 */
public class TaxonDaoHibernateImplTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByType	
	private ITaxonDao taxonDao;
	
	@SpringBeanByType	
	private IReferenceDao referenceDao;
	
	private UUID uuid;
	private UUID sphingidae;
	private UUID acherontia;
	private UUID mimas;
	private UUID acherontiaLachesis;
	private AuditEvent previousAuditEvent;
	private AuditEvent mostRecentAuditEvent;

	@Before
	public void setUp() {
		uuid = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
		sphingidae = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");
		acherontia = UUID.fromString("c5cc8674-4242-49a4-aada-72d63194f5fa");
		acherontiaLachesis = UUID.fromString("b04cc9cb-2b4a-4cc4-a94a-3c93a2158b06");
		mimas = UUID.fromString("900052b7-b69c-4e26-a8f0-01c215214c40");
		previousAuditEvent = new AuditEvent();
		previousAuditEvent.setRevisionNumber(1025);
		previousAuditEvent.setUuid(UUID.fromString("a680fab4-365e-4765-b49e-768f2ee30cda"));
		mostRecentAuditEvent = new AuditEvent();
		mostRecentAuditEvent.setRevisionNumber(1026);
		mostRecentAuditEvent.setUuid(UUID.fromString("afe8e761-8545-497b-9134-6a6791fc0b0d"));
		AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)
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
		assertNotNull("Instance of ITaxonDao expected",taxonDao);
		assertNotNull("Instance of IReferenceDao expected",referenceDao);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	@DataSet
	public void testGetRootTaxa() { 
		ReferenceBase sec1 = referenceDao.findById(1);
		assert sec1 != null : "sec1 must exist";
		ReferenceBase sec2 = referenceDao.findById(2);
		assert sec2 != null : "sec2 must exist";
		
		List<Taxon> rootTaxa = taxonDao.getRootTaxa(sec1);
		assertNotNull("getRootTaxa should return a List",rootTaxa);
		assertFalse("The list should not be empty",rootTaxa.isEmpty());
		assertEquals("There should be one root taxon",1, rootTaxa.size());
		
		rootTaxa = taxonDao.getRootTaxa(sec1, CdmFetch.FETCH_CHILDTAXA(), true, false);
		assertNotNull("getRootTaxa should return a List",rootTaxa);
		assertFalse("The list should not be empty",rootTaxa.isEmpty());
		assertEquals("There should be one root taxon",1, rootTaxa.size());
		
		rootTaxa = taxonDao.getRootTaxa(Rank.GENUS(), sec1, CdmFetch.FETCH_CHILDTAXA(), true, false, null);
		assertNotNull("getRootTaxa should return a List",rootTaxa);
		assertFalse("The list should not be empty",rootTaxa.isEmpty());
		assertEquals("There should be one root taxon",1, rootTaxa.size());
		
		rootTaxa = taxonDao.getRootTaxa(Rank.FAMILY(), sec2, CdmFetch.FETCH_CHILDTAXA(), true, false, null);
		if (logger.isDebugEnabled()) {
		logger.debug("Root taxa rank Family (" + rootTaxa.size() + "):");
		for (Taxon taxon: rootTaxa) {
			logger.debug(taxon.getTitleCache());
		}
	}
		assertEquals("There should be one root taxon rank Family",1, rootTaxa.size());
		rootTaxa = taxonDao.getRootTaxa(Rank.GENUS(), sec2, CdmFetch.FETCH_CHILDTAXA(), true, false, null);
		assertNotNull("getRootTaxa should return a List",rootTaxa);
		assertFalse("The list should not be empty",rootTaxa.isEmpty());
		if (logger.isDebugEnabled()) {
		logger.debug("Root taxa rank Genus (" + rootTaxa.size() + "):");
		for (Taxon taxon: rootTaxa) {
			logger.debug(taxon.getTitleCache());
		}
	}
		assertEquals("There should be 22 root taxa rank Genus",22, rootTaxa.size());
		
		rootTaxa = taxonDao.getRootTaxa(Rank.SPECIES(), sec2, CdmFetch.FETCH_CHILDTAXA(), true, false, null);
		if (logger.isDebugEnabled()) {
		logger.debug("Root taxa rank Species (" + rootTaxa.size() + "):");
		for (Taxon taxon: rootTaxa) {
			logger.debug(taxon.getTitleCache());
		}
	}
		assertEquals("There should be 4 root taxa rank Species",3, rootTaxa.size());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	@DataSet
	public void testGetTaxaByName() {
		ReferenceBase sec = referenceDao.findById(1);
		assert sec != null : "sec must exist";

		List<TaxonBase> results = taxonDao.getTaxaByName("Aus", sec);
		assertNotNull("getTaxaByName should return a List", results);
		//assertFalse("The list should not be empty", results.isEmpty());
		assertTrue(results.size() == 1);

		results = taxonDao.getTaxaByName("A*", MatchMode.BEGINNING, 
				true, null, null);
		assertNotNull("getTaxaByName should return a List", results);
		assertTrue(results.size() == 9);

		if (logger.isDebugEnabled()) {
			for (int i = 0; i < results.size(); i++) {
				String nameCache = "";
				TaxonNameBase<?,?> taxonNameBase= ((TaxonBase)results.get(i)).getName();
				nameCache = ((NonViralName)taxonNameBase).getNameCache();
				logger.debug(results.get(i).getClass() + "(" + i +")" + 
						": Name Cache = " + nameCache + ", Title Cache = " + results.get(i).getTitleCache());
			}
		}
//		assertEquals(results.get(0).getTitleCache(), "Abies sec. ???");
//		assertEquals(results.get(1).getTitleCache(), "Abies Mill.");
//		assertEquals(results.get(2).getTitleCache(), "Abies mill. sec. ???");
//		assertEquals(results.get(3).getTitleCache(), "Abies alba sec. ???");
//		assertEquals(results.get(4).getTitleCache(), "Abies alba Michx. sec. ???");
//		assertEquals(results.get(5).getTitleCache(), "Abies alba Mill. sec. ???");

		results = taxonDao.getTaxaByName("A", MatchMode.BEGINNING, 
				true, null, null);
		assertNotNull("getTaxaByName should return a List", results);
		assertTrue(results.size() == 9);

		results = taxonDao.getTaxaByName("Aus", MatchMode.EXACT, 
				true, null, null);
		assertNotNull("getTaxaByName should return a List", results);
		assertEquals("Results list should contain one entity",1,results.size());
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
		
		int numberOfRelatedTaxa = taxonDao.countTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), TaxonRelationship.Direction.relatedTo);
		assertEquals("countTaxonRelationships should return 23", 23, numberOfRelatedTaxa);
	}
	
	@Test
	@DataSet
	public void testCountTaxaByName() {
		int numberOfTaxa = taxonDao.countTaxaByName("A*", MatchMode.BEGINNING, true);
		assertEquals(numberOfTaxa, 9);
		numberOfTaxa = taxonDao.countTaxaByName("A*", MatchMode.BEGINNING, SelectMode.TAXA);
		assertEquals(numberOfTaxa, 9);
		numberOfTaxa = taxonDao.countTaxaByName("A*", MatchMode.BEGINNING, false);
		assertEquals(numberOfTaxa, 3);
		numberOfTaxa = taxonDao.countTaxaByName("A*", MatchMode.BEGINNING, SelectMode.SYNONYMS);
		assertEquals(numberOfTaxa, 3);
		numberOfTaxa = taxonDao.countTaxaByName("A*", MatchMode.BEGINNING, SelectMode.ALL);
		assertEquals(numberOfTaxa, 12);
		ReferenceBase reference = referenceDao.findByUuid(UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2"));
		numberOfTaxa = taxonDao.countTaxaByName("A*", MatchMode.BEGINNING, SelectMode.ALL, reference);
		assertEquals(numberOfTaxa, 2);
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
		orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));
		
		List<TaxonRelationship> relatedTaxa = taxonDao.getTaxonRelationships(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), null, null, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
		assertNotNull("getRelatedTaxa should return a List",relatedTaxa);
		assertEquals("getRelatedTaxa should return all 23 related taxa",relatedTaxa.size(),23);
		assertTrue("getRelatedTaxa should return TaxonRelationship objects with the relatedFrom taxon initialized",Hibernate.isInitialized(relatedTaxa.get(0).getFromTaxon()));
		assertTrue("getRelatedTaxa should return TaxonRelationship objects with the relatedFrom taxon initialized",Hibernate.isInitialized(relatedTaxa.get(0).getFromTaxon().getName()));
		
		assertEquals("Acherontia should appear first in the list of related taxa", relatedTaxa.get(0).getFromTaxon().getTitleCache(), "Acherontia Laspeyres, 1809 sec. cate-sphingidae.org");
		assertEquals("Sphingonaepiopsis should appear last in the list of related taxa", relatedTaxa.get(22).getFromTaxon().getTitleCache(), "Sphinx Linnaeus, 1758 sec. cate-sphingidae.org");
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
		
		List<TaxonRelationship> firstPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), 10, 0, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
		List<TaxonRelationship> secondPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),10, 1, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
		List<TaxonRelationship> thirdPage = taxonDao.getTaxonRelationships(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), 10, 2, orderHints,propertyPaths, TaxonRelationship.Direction.relatedTo);
		
		assertNotNull("getRelatedTaxa: 10, 0 should return a List",firstPage);
		assertEquals("getRelatedTaxa: 10, 0 should return a List with 10 elements",10,firstPage.size());
		assertNotNull("getRelatedTaxa: 10, 1 should return a List",secondPage);
		assertEquals("getRelatedTaxa: 10, 1 should return a List with 10 elements",secondPage.size(),10);
		assertNotNull("getRelatedTaxa: 10, 2 should return a List",thirdPage);
		assertEquals("getRelatedTaxa: 10, 2 should return a List with 3 elements",thirdPage.size(),3);
	}
	
	@Test
	@DataSet
	public void testCountSynonymRelationships() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
		int numberOfSynonymRelationships = taxonDao.countSynonyms(taxon,null);
		assertEquals("countSynonymRelationships should return 5",5,numberOfSynonymRelationships);
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
		
		assertNotNull("getSynonyms should return a List",synonyms);
		assertEquals("getSynonyms should return 5 SynonymRelationship entities",synonyms.size(),5);
		assertTrue("getSynonyms should return SynonymRelationship objects with the synonym initialized",Hibernate.isInitialized(synonyms.get(0).getSynonym()));
	}
	
	@Test
	@DataSet
	public void testCountSynonymRelationshipsByType()	{
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
		int numberOfTaxonomicSynonyms = taxonDao.countSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		assertEquals("countSynonyms should return 4",numberOfTaxonomicSynonyms, 4);
	}
	
	@Test
	@DataSet
	public void testSynonymRelationshipsByType() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
        List<SynonymRelationship> synonyms = taxonDao.getSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null,null,null);
		
        assertNotNull("getSynonyms should return a List",synonyms);
		assertEquals("getSynonyms should return 4 SynonymRelationship entities",synonyms.size(),4);
	}
	
	@Test
	@DataSet
	public void testPageSynonymRelationships(){
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
		List<SynonymRelationship> firstPage = taxonDao.getSynonyms(taxon, null, 4, 0,null,null);
		List<SynonymRelationship> secondPage = taxonDao.getSynonyms(taxon, null, 4, 1,null,null);
		
		assertNotNull("getSynonyms: 4, 0 should return a List",firstPage);
		assertEquals("getSynonyms: 4, 0 should return 4 SynonymRelationships", firstPage.size(),4);
		assertNotNull("getSynonyms: 4, 1 should return a List",secondPage);
		assertEquals("getSynonyms: 4, 1 should return 1 SynonymRelationship",secondPage.size(),1);
	}
	
	@Test
	@DataSet
	public void testGetTaxonMatchingUninomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(Taxon.class, "Smerinthus", "*", "*", "*",null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return two Taxa",2,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 5",5,result.get(0).getId());
	}
	
	@Test
	@DataSet
	public void testGetTaxonMatchingSpeciesBinomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(Taxon.class,"Smerinthus", null, "kindermannii", null,null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return one Taxon",1,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 8",8,result.get(0).getId());
	}
	  
	@Test
	@DataSet
	public void testGetTaxonMatchingTrinomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(Taxon.class,"Cryptocoryne", null,"purpurea","borneoensis",null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return one Taxon",1,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 38",38,result.get(0).getId());
	}
	
	@Test
	@DataSet
	public void testNegativeMatch() {
		List<TaxonBase> result = taxonDao.findTaxaByName(Taxon.class,"Acherontia", null,"atropos","dehli",null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertTrue("findTaxaByName should return an empty List",result.isEmpty());
	}
	
	@Test
	@DataSet
	public void testCountAllTaxa() {
		int numberOfTaxa = taxonDao.count(Taxon.class);
		assertEquals("count should return 33 taxa",33, numberOfTaxa);
	}
	
	@Test
	@DataSet
	public void testListAllTaxa() {
		List<Taxon> taxa = taxonDao.list(Taxon.class,100, 0);
		assertNotNull("list should return a List",taxa);
		assertEquals("list should return 33 taxa",33, taxa.size());
	}
	
	@Test
	@DataSet
	@ExpectedDataSet
	public void testDelete() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		taxonDao.delete(taxon);
		setComplete();
		endTransaction();
		//try {printDataSet(new FileOutputStream("test.xml"));} catch(Exception e) { } 
	}
	
	@Test
	@DataSet
	public void testDeleteWithChildren() {
		Taxon taxonWithChildren = (Taxon)taxonDao.findByUuid(mimas);
		assert taxonWithChildren != null : "taxon must exist";
		assertEquals(taxonWithChildren.getTaxonomicChildrenCount(), 2);
		Taxon parent = (Taxon)taxonDao.findByUuid(sphingidae);
		assertSame(taxonWithChildren.getTaxonomicParent(), parent);
		assertEquals(parent.getTaxonomicChildrenCount(), 204);
		taxonDao.delete(taxonWithChildren);
		assertEquals(parent.getTaxonomicChildrenCount(), 203);
	}
	
	@Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testFindDeleted() {
    	TaxonBase taxon = taxonDao.findByUuid(acherontia);
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
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(taxon, null,null,null);
    	assertNotNull("getAuditEvents should return a list",auditEvents);
    	assertFalse("the list should not be empty",auditEvents.isEmpty());
    	assertEquals("There should be two AuditEventRecords in the list",2, auditEvents.size());
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testGetAuditEventsFromNow() {
    	AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
    	TaxonBase taxon =  taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(taxon, null,null,AuditEventSort.FORWARDS);
    	assertNotNull("getAuditEvents should return a list",auditEvents);
    	assertFalse("the list should not be empty",auditEvents.isEmpty());
    	assertEquals("There should be one audit event in the list",1,auditEvents.size());
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testCountAuditEvents() {
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	int numberOfAuditEvents = taxonDao.countAuditEvents(taxon,null);
    	assertEquals("countAuditEvents should return 2",numberOfAuditEvents,2);
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getPreviousAuditEvent() {
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
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
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	AuditEventRecord<TaxonBase> auditEvent = taxonDao.getNextAuditEvent(taxon);
    	assertNotNull("getNextAuditEvent should not return null as there is at least one audit event after the current one",auditEvent);
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getNextAuditEventAtEnd() {
    	AuditEventContextHolder.getContext().setAuditEvent(mostRecentAuditEvent);
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	AuditEventRecord<TaxonBase> auditEvent = taxonDao.getNextAuditEvent(taxon);
    	assertNull("getNextAuditEvent should return null as there no more audit events after the current one",auditEvent);
    }
    
	@Test
	@DataSet
	@ExpectedDataSet
	@Ignore
	public void testAddChild() throws Exception {
		Taxon parent = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
		assert parent != null : "taxon cannot be null";
		Taxon child = Taxon.NewInstance(null, null);
		child.setTitleCache("Acherontia lachesis diehli Eitschberger, 2003", true);
		child.addTaxonRelation(parent, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),null, null);
		taxonDao.save(child);
		setComplete();
		endTransaction();
	}
	
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testFind() {
    	Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
    	assert taxon != null : "taxon cannot be null";
    	
    	assertEquals("getTaxonomicChildrenCount should return 1 in this view",1,taxon.getTaxonomicChildrenCount());
    	assertEquals("getRelationsToThisTaxon should contain 1 TaxonRelationship in this view",1,taxon.getRelationsToThisTaxon().size());
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testFindInPreviousView() {
    	AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
    	Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
    	assert taxon != null : "taxon cannot be null";
    	
    	assertEquals("getTaxonomicChildrenCount should return 0 in this view",0,taxon.getTaxonomicChildrenCount());
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
}
