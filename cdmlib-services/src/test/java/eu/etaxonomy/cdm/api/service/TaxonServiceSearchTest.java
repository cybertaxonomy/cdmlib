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
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
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
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBaseTestClass;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 04.02.2009
 * @version 1.0
 */
public class TaxonServiceSearchTest extends CdmIntegrationTest {
	private static Logger logger = Logger.getLogger(TaxonServiceSearchTest.class);
	
	@SpringBeanByType
	private ITaxonService taxonService;
	@SpringBeanByType
	private INameService nameService;
	@SpringBeanByType
	private IAgentService agentService;
//	@SpringBeanByType
//	private CdmEntityDaoBaseTestClass cdmEntityDaoBase;


	@Ignore
	@Test
	public void testDbUnitUsageTest() throws Exception {
		assertNotNull("taxonService should exist", taxonService);
		assertNotNull("nameService should exist", nameService);
		assertNotNull("agentService should exist", agentService);
//		assertNotNull("cdmEntityDaoBase should exist", cdmEntityDaoBase);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)}.
	 */
	//@Ignore
	@Test
	@DataSet
	public final void testFindTaxaAndNames() {

		ITaxonServiceConfigurator configurator = new TaxonServiceConfiguratorImpl();
		configurator.setSearchString("Abies*");
		configurator.setMatchMode(MatchMode.BEGINNING);
		configurator.setDoTaxa(true);
		configurator.setDoSynonyms(true);
		configurator.setDoNamesWithoutTaxa(true);
		configurator.setDoTaxaByCommonNames(true);
		Pager<IdentifiableEntity> pager = taxonService.findTaxaAndNames(configurator);
		List<IdentifiableEntity> list = pager.getRecords();
		if (logger.isDebugEnabled()) {
			for (int i = 0; i < list.size(); i++) {
				String nameCache = "";
				if (list.get(i) instanceof NonViralName) {
					nameCache = ((NonViralName<?>)list.get(i)).getNameCache();
				} else if (list.get(i) instanceof TaxonBase) {
					TaxonNameBase<?,?> taxonNameBase= ((TaxonBase)list.get(i)).getName();
					nameCache = ((NonViralName<?>)taxonNameBase).getNameCache();
				} else {}
				logger.debug(list.get(i).getClass() + "(" + i +")" + 
						": Name Cache = " + nameCache + ", Title Cache = " + list.get(i).getTitleCache());
			}
		}
		assertEquals(list.get(2).getTitleCache(), "Abies mill. sec. ???");
		assertEquals(list.get(5).getTitleCache(), "Abies alba Mill. sec. ???");
	}
	
//	@Test
//	@DataSet
//	public final void testPrintDataSet() {
//		
//		printDataSet(System.out);
//	}
	
}
