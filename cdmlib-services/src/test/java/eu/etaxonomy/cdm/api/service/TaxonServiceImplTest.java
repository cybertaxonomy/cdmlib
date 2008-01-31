/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 *
 */
@Entity
public class TaxonServiceImplTest {
	private static final Logger logger = Logger.getLogger(TaxonServiceImplTest.class);
	
	//@Autowired
	static ITaxonService service;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CdmApplicationController app = new CdmApplicationController();
		service = app.getTaxonService();
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

/****************** TESTS *****************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao)}.
	 */
	@Test
	public final void testSetDao() {
		logger.warn("Not implemented yet");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getTaxonByUuid(java.util.UUID)}.
	 */
	@Test
	public final void testGetTaxonByUuid() {
		Taxon expectedTaxon = Taxon.NewInstance(null, null);
		UUID uuid = service.saveTaxon(expectedTaxon);
		TaxonBase actualTaxon = service.getTaxonByUuid(uuid);
		assertEquals(expectedTaxon, actualTaxon);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#saveTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public final void testSaveTaxon() {
		Taxon expectedTaxon = Taxon.NewInstance(null, null);
		UUID uuid = service.saveTaxon(expectedTaxon);
		TaxonBase actualTaxon = service.getTaxonByUuid(uuid);
		assertEquals(expectedTaxon, actualTaxon);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#removeTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public final void testRemoveTaxon() {
		Taxon taxon = Taxon.NewInstance(null, null);
		UUID uuid = service.saveTaxon(taxon);
		service.removeTaxon(taxon);
		TaxonBase actualTaxon = service.getTaxonByUuid(uuid);
		assertNull(actualTaxon);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#searchTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	public final void testSearchTaxaByName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	public final void testGetRootTaxa() {
		logger.warn("Not yet implemented"); // TODO
	}

}
