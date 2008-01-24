/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.common;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author a.mueller
 *
 */
public class DaoBaseTest {
	private static final Logger logger = Logger.getLogger(DaoBaseTest.class);
	
	private DaoBaseTester daoBaseTester;
	
	public class DaoBaseTester extends DaoBase{
	}
	
	
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
		daoBaseTester = new DaoBaseTester();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	
/************ TESTS ********************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.common.DaoBase#getSession()}.
	 */
	@Test
	public void testGetSession() {
		logger.warn(daoBaseTester.getSession());
		assertNotNull(daoBaseTester.getSession());
	}

}
