/**
 * 
 */
package eu.etaxonomy.cdm.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 *
 */
public class TermLoaderIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermLoaderIntegrationTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
//		CdmPersistentDataSource defaultSource = CdmPersistentDataSource.NewDefaultInstance();
//		CdmApplicationController app = CdmApplicationController.NewInstance(defaultSource, DbSchemaValidation.CREATE);
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

/* ************************* TESTS *************************************************/


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType#TAXONOMICALLY_INCLUDED_IN()}.
	 */
	@Test
	public final void testTermsAreLoaded() {
		TaxonRelationshipType rel = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();
		assertNotNull(rel);
		UUID expectedUuid = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");
		assertEquals(expectedUuid, rel.getUuid());
	}

}
