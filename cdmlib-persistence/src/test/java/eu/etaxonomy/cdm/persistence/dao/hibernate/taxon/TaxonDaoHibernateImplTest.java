package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
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
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.SelectMode;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 * @author ben.clark
 *
 */
public class TaxonDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType	
	private ITaxonDao taxonDao;
	
	@SpringBeanByType	
	private IReferenceDao referenceDao;
	
	private UUID uuid;
	private UUID sphingidae;
	private UUID acherontia;

	@Before
	public void setUp() {
		uuid = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
		sphingidae = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");
		acherontia = UUID.fromString("c5cc8674-4242-49a4-aada-72d63194f5fa");
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
		
		rootTaxa = taxonDao.getRootTaxa(Rank.GENUS(), sec1, CdmFetch.FETCH_CHILDTAXA(), true, false);
		assertNotNull("getRootTaxa should return a List",rootTaxa);
		assertFalse("The list should not be empty",rootTaxa.isEmpty());
		assertEquals("There should be one root taxon",1, rootTaxa.size());
		
		rootTaxa = taxonDao.getRootTaxa(Rank.FAMILY(), sec2, CdmFetch.FETCH_CHILDTAXA(), true, false);
		if (logger.isDebugEnabled()) {
		logger.debug("Root taxa rank Family (" + rootTaxa.size() + "):");
		for (Taxon taxon: rootTaxa) {
			logger.debug(taxon.getTitleCache());
		}
	}
		assertEquals("There should be one root taxon rank Family",1, rootTaxa.size());
		rootTaxa = taxonDao.getRootTaxa(Rank.GENUS(), sec2, CdmFetch.FETCH_CHILDTAXA(), true, false);
		assertNotNull("getRootTaxa should return a List",rootTaxa);
		assertFalse("The list should not be empty",rootTaxa.isEmpty());
		if (logger.isDebugEnabled()) {
		logger.debug("Root taxa rank Genus (" + rootTaxa.size() + "):");
		for (Taxon taxon: rootTaxa) {
			logger.debug(taxon.getTitleCache());
		}
	}
		assertEquals("There should be 22 root taxa rank Genus",22, rootTaxa.size());
		
		rootTaxa = taxonDao.getRootTaxa(Rank.SPECIES(), sec2, CdmFetch.FETCH_CHILDTAXA(), true, false);
		if (logger.isDebugEnabled()) {
		logger.debug("Root taxa rank Species (" + rootTaxa.size() + "):");
		for (Taxon taxon: rootTaxa) {
			logger.debug(taxon.getTitleCache());
		}
	}
		assertEquals("There should be 4 root taxa rank Species",4, rootTaxa.size());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	@DataSet
//	public void testGetTaxaByName() {
//		ReferenceBase sec = referenceDao.findById(1);
//		assert sec != null : "sec must exist";
//		
//		List<TaxonBase> results = taxonDao.getTaxaByName("Aus", sec);
//		assertNotNull("getTaxaByName should return a List",results);
//		assertFalse("The list should not be empty",results.isEmpty());
//	}	
	
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
				nameCache = ((NonViralName<?>)taxonNameBase).getNameCache();
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
		assertTrue(results.size() == 1);
	}	

	@Test
	@DataSet
	public void testFindByUuid() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(uuid);
		assertNotNull("findByUuid should return a taxon",taxon);
		assertTrue("findByUuid should return a taxon with it's name initialized",Hibernate.isInitialized(taxon.getName()));
	}
	
	@Test
	@DataSet
	public void testCountRelatedTaxa()	{
		Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
		assert taxon != null : "taxon must exist"; 
		
		int numberOfRelatedTaxa = taxonDao.countRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN());
		assertEquals("countRelatedTaxa should return 23", 23, numberOfRelatedTaxa);
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
		
		List<TaxonRelationship> relatedTaxa = taxonDao.getRelatedTaxa(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), null, null);
		assertNotNull("getRelatedTaxa should return a List",relatedTaxa);
		assertEquals("getRelatedTaxa should return all 23 related taxa",relatedTaxa.size(),23);
		assertTrue("getRelatedTaxa should return TaxonRelationship objects with the relatedFrom taxon initialized",Hibernate.isInitialized(relatedTaxa.get(0).getFromTaxon()));
	}
	
	@Test
	@DataSet
	public void testGetRelatedTaxaPaged()	{
		Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
		assert taxon != null : "taxon must exist";
		
		List<TaxonRelationship> firstPage = taxonDao.getRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), 10, 0);
		List<TaxonRelationship> secondPage = taxonDao.getRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),10, 1);
		List<TaxonRelationship> thirdPage = taxonDao.getRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), 10, 2);
		
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
		
		List<SynonymRelationship> synonyms = taxonDao.getSynonyms(taxon, null, null, null);
		
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
		
        List<SynonymRelationship> synonyms = taxonDao.getSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null);
		
        assertNotNull("getSynonyms should return a List",synonyms);
		assertEquals("getSynonyms should return 4 SynonymRelationship entities",synonyms.size(),4);
	}
	
	@Test
	@DataSet
	public void testPageSynonymRelationships(){
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
		List<SynonymRelationship> firstPage = taxonDao.getSynonyms(taxon, null, 4, 0);
		List<SynonymRelationship> secondPage = taxonDao.getSynonyms(taxon, null, 4, 1);
		
		assertNotNull("getSynonyms: 4, 0 should return a List",firstPage);
		assertEquals("getSynonyms: 4, 0 should return 4 SynonymRelationships", firstPage.size(),4);
		assertNotNull("getSynonyms: 4, 1 should return a List",secondPage);
		assertEquals("getSynonyms: 4, 1 should return 1 SynonymRelationship",secondPage.size(),1);
	}
	
	@Test
	@DataSet
	public void testGetTaxonMatchingUninomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(true, "Smerinthus", null, null, null,null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return two Taxa",2,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 5",5,result.get(0).getId());
	}
	
	@Test
	@DataSet
	public void testGetTaxonMatchingSpeciesBinomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(true,"Smerinthus", null, "kindermannii", null,null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return one Taxon",1,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 8",8,result.get(0).getId());
	}
	  
	@Test
	@DataSet
	public void testGetTaxonMatchingTrinomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(true,"Cryptocoryne", null,"purpurea","borneoensis",null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return one Taxon",1,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 38",38,result.get(0).getId());
	}
	
	@Test
	@DataSet
	public void testNegativeMatch() {
		List<TaxonBase> result = taxonDao.findTaxaByName(true,"Acherontia", null,"atropos","dehli",null,null,null);
		
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
	
//	@Test
//	@DataSet
//	public void testPrintDataSet() {
//		printDataSet(System.out);
//	}
}
