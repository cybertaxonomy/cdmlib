/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.impl.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBaseTestClass;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 04.02.2009
 * @version 1.0
 */
public class TaxonServiceSearchTest extends CdmIntegrationTest {
	private static Logger logger = Logger.getLogger(TaxonServiceSearchTest.class);
	
	@SpringBeanByType
	private ITaxonService service;
	
	@SpringBeanByType
	private INameService nameService;

	@SpringBeanByType
	private CdmEntityDaoBaseTestClass cdmEntityDaoBase;

	@Test
	public void testDbUnitUsageTest() throws Exception {
		assertNotNull("service should exist", service);
		assertNotNull("nameService should exist", nameService);
		assertNotNull("cdmEntityDaoBase should exist", cdmEntityDaoBase);
	}

	@Test
	public final void testBuildDataSet() {

		BotanicalName abies_Mill, abiesAlba_Michx, abiesAlba_Mill;

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
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)}.
	 */
	@Test
	//@DataSet("TaxonServiceImplTest.xml")
	//@ExpectedDataSet
	public final void testFindTaxaAndNames() {

		ITaxonServiceConfigurator configurator = new TaxonServiceConfiguratorImpl();
		configurator.setDoSynonyms(true);
		configurator.setDoNamesWithoutTaxa(true);
		configurator.setSearchString("Abies");
		Pager<IdentifiableEntity> pager = service.findTaxaAndNames(configurator);
		List<IdentifiableEntity> list = pager.getRecords();
		for (int i = 0; i < list.size(); i++) {
			if (logger.isDebugEnabled()) { logger.debug(i + " = " + list.get(i).getTitleCache()); }
		}

	}
	
	@Ignore
	@Test
	public final void testPrintDataSet() {
		
		printDataSet(System.out);
	}
	
}
