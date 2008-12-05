/**
 * 
 */
package eu.etaxonomy.cdm.test.integration;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.TermServiceImplTest;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 *
 */
public class TermLoaderIntegrationTest {
	private static final Logger logger = Logger.getLogger(TermLoaderIntegrationTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//CdmPersistentDataSource defaultSource = CdmPersistentDataSource.NewDefaultInstance();
		//CdmApplicationController app = CdmApplicationController.NewInstance(defaultSource, DbSchemaValidation.CREATE);
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
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType#TAXONOMICALLY_INCLUDED_IN()}.
	 */
	@Test
	public final void testUnlazyStatusTerms() {
		CdmApplicationController app;
		try {
			app = CdmApplicationController.NewInstance(DbSchemaValidation.CREATE);
			List<NomenclaturalStatusType> list = new ArrayList<NomenclaturalStatusType>();
			list.add(NomenclaturalStatusType.SUPERFLUOUS());
			list.add(NomenclaturalStatusType.NUDUM());
			list.add(NomenclaturalStatusType.ILLEGITIMATE());
			list.add(NomenclaturalStatusType.INVALID());
			list.add(NomenclaturalStatusType.CONSERVED());
			list.add(NomenclaturalStatusType.ALTERNATIVE());
			list.add(NomenclaturalStatusType.REJECTED());
			list.add(NomenclaturalStatusType.REJECTED_PROP());
			list.add(NomenclaturalStatusType.PROVISIONAL());
			list.add(NomenclaturalStatusType.SUBNUDUM());
			list.add(NomenclaturalStatusType.OPUS_UTIQUE_OPPR());
			list.add(NomenclaturalStatusType.VALID());
			for (NomenclaturalStatusType status : list){
				logger.warn(status.getRepresentations().size());
				logger.warn(status.getLabel());
				
				app.getTermService().saveTerm(status);
			}
		} catch (DataSourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TermNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
