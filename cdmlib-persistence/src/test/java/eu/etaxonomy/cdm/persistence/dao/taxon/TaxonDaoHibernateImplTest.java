/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.taxon;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

import eu.etaxonomy.cdm.datagenerator.TaxonGenerator;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IdentifiableDaoBaseTest;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;

/**
 * @author a.mueller
 *
 */
@TransactionConfiguration(defaultRollback=false)
public class TaxonDaoHibernateImplTest extends CdmUnitTestBase {
	private static final Logger logger = Logger.getLogger(TaxonDaoHibernateImplTest.class);
	
	@Autowired
	private  TaxonDaoHibernateImpl taxonDao;
	private static TaxonGenerator taxGen;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		taxGen = new TaxonGenerator();
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
	
/************ TESTS ********************************/


	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.taxon.TaxonDaoHibernateImpl#TaxonDaoHibernateImpl()}.
	 */
	@Test
	public void testTaxonDaoHibernateImpl() {
		logger.warn("testTaxonDaoHibernateImpl - Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.taxon.TaxonDaoHibernateImpl#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	public void testGetRootTaxa() {
		//taxonDao.getRootTaxa(null);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	public void testGetTaxaByName() {
		//taxonDaoHibernateImplTester.getTaxaByName();
		logger.warn("testGetTaxaByName - Not yet implemented");//TODO
	}

	@Test
	public void testSaveTaxon() {
		Taxon t = taxGen.getTestTaxon();
		taxonDao.save(t);
	}
}
