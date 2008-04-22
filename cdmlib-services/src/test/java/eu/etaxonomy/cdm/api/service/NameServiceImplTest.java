/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 *
 */
public class NameServiceImplTest {
	private static final Logger logger = Logger.getLogger(NameServiceImplTest.class);

	//@Autowired
	static INameService service;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CdmPersistentDataSource defaultSource = CdmPersistentDataSource.NewDefaultInstance();
		CdmApplicationController app = CdmApplicationController.NewInstance(defaultSource, DbSchemaValidation.CREATE);
		service = app.getNameService();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
/* ******************** TESTS ********************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao)}.
	 */
	@Test
	public void testSetDao() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setVocabularyDao(eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao)}.
	 */
	@Test
	public void testSetVocabularyDao() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#NameServiceImpl()}.
	 */
	@Test
	public void testNameServiceImpl() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getNamesByName(java.lang.String)}.
	 */
	@Test
	public void testGetNamesByName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getTaxonNameByUuid(java.util.UUID)}.
	 */
	@Test
	public void testGetTaxonNameByUuid() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#saveTaxonName(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testSaveTaxonName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#saveTaxonNameAll(java.util.Collection)}.
	 */
	@Test
	public void testSaveTaxonNameAll() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#removeTaxon(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testRemoveTaxon() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getAllNames(int, int)}.
	 */
	@Test
	public void testGetAllNames() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getRankVocabulary()}.
	 */
	@Test
	@Ignore
	public void testGetRankVocabulary() {
		OrderedTermVocabulary<Rank> rankVocabulary = service.getRankVocabulary();
		assertNotNull(rankVocabulary);
		assertEquals(61, rankVocabulary.size());
		Rank highestRank = rankVocabulary.getHighestTerm();
		assertEquals(Rank.EMPIRE(), highestRank);
		assertEquals(Rank.DOMAIN(), rankVocabulary.getNextLowerTerm(highestRank));
		assertSame(Rank.EMPIRE(), highestRank);
		assertSame(Rank.DOMAIN(), rankVocabulary.getNextLowerTerm(highestRank));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testGenerateTitleCache() {
		logger.warn("Not yet implemented");
	}

}
