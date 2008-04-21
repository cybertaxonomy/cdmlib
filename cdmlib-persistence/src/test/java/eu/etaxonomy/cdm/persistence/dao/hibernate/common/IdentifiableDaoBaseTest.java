/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;

/**
 * @author a.mueller
 *
 */
public class IdentifiableDaoBaseTest extends CdmUnitTestBase {
	private static final Logger logger = Logger.getLogger(IdentifiableDaoBaseTest.class);
	
	@Autowired
	private  TaxonDaoHibernateImpl identifiableDaoBaseTester;
	
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
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase#IdentifiableDaoBase(java.lang.Class)}.
	 */
	@Test
	public void testIdentifiableDaoBase() {
		logger.warn("testIdentifiableDaoBase - Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase#findByTitle(java.lang.String)}.
	 */
	@Test
	public void testFindByTitle() {
		logger.warn("testFindByTitle - Not yet implemented");
	}

}
