/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.impl.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class TaxonServiceImplTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(TaxonServiceImplTest.class);
	
	@SpringBeanByType
	private ITaxonService service;
	
	@SpringBeanByType
	private INameService nameService;
	
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
		Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(null), null);
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
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)}.
	 */
	@Ignore
	@Test
	@DataSet("TaxonServiceImplTest.xml")
	//@ExpectedDataSet
	public final void testFindTaxaAndNames() {

		ITaxonServiceConfigurator configurator = new TaxonServiceConfiguratorImpl();
		configurator.setDoSynonyms(true);
		configurator.setDoNamesWithoutTaxa(true);
		Pager<IdentifiableEntity> pager = service.findTaxaAndNames(configurator);
		List<IdentifiableEntity> list = pager.getRecords();

	}
	
	@Test
	public final void testBuildDataSet() {

		BotanicalName abies_Mill, abiesAlba_Michx, abiesAlba_Mill;
//		BotanicalName abies, abies_Mill, abiesAlba, abiesAlba_Michx, abiesAlba_Mill;
//		Synonym s_abiesAlba_Michx;
//		Taxon t_abies_Mill, t_abiesAlba_Mill;

		Person mill = Person.NewInstance();
		mill.setTitleCache("Mill.");
		Person michx = Person.NewInstance();
		michx.setTitleCache("Michx.");

		nameService.saveTaxonName(BotanicalName.NewInstance(Rank.GENUS(), "Abies", null, null, null, null, null, null, null));
		abies_Mill = BotanicalName.NewInstance(Rank.GENUS(), "Abies", null, null, null, mill, null, null, null);
		nameService.saveTaxonName(BotanicalName.NewInstance(Rank.SPECIES(), "Abies", null, "alba", null, null, null, null, null));
		abiesAlba_Michx = BotanicalName.NewInstance(Rank.SPECIES(), "Abies", null, "alba", null, michx, null, null, null);
		abiesAlba_Mill = BotanicalName.NewInstance(Rank.SPECIES(), "Abies", null, "alba", null, mill, null, null, null);

		service.saveTaxon(Taxon.NewInstance(abies_Mill, null));
		service.saveTaxon(Taxon.NewInstance(abiesAlba_Mill, null));
		service.saveTaxon(Synonym.NewInstance(abiesAlba_Michx, null));

	}
	
	@Test
	public final void testPrintDataSet() {
		
		printDataSet(System.out);
	}
	
}
