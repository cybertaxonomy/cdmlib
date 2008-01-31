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

import eu.etaxonomy.cdm.persistence.dao.common.IdentifiableDaoBaseTest;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;

/**
 * @author a.mueller
 *
 */
public class TaxonDaoHibernateImplTest extends CdmUnitTestBase {
	private static final Logger logger = Logger.getLogger(TaxonDaoHibernateImplTest.class);
	
	@Autowired
	private  TaxonDaoHibernateImpl taxonDaoHibernateImplTester;

	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
		//taxonDaoHibernateImplTester.getRootTaxa(sec);
		logger.warn("testGetRootTaxa - Not yet implemented");//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	public void testGetTaxaByName() {
		//taxonDaoHibernateImplTester.getTaxaByName();
		logger.warn("testGetTaxaByName - Not yet implemented");//TODO
	}

}
