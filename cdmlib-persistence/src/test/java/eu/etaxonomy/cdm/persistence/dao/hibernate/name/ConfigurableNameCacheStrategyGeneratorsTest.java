/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@Ignore
@DataSet("TaxonNameDaoHibernateImplTest.xml")
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-testWithConfigurableNameCacheStrategyGenerators.xml")
public class ConfigurableNameCacheStrategyGeneratorsTest extends CdmIntegrationTest {

	@SpringBeanByType
	ITaxonNameDao taxonNameDao;

	@SpringBeanByType
	ITaxonDao taxonDao;

	private UUID cryptocoryneGriffithiiUuid;
	private UUID acherontiaLachesisUuid;
	private UUID acherontiaLachesisConceptUuid;
	private UUID cryptocoryneGriffithiiConceptUuid;

	@Before
	public void setUp() {
		cryptocoryneGriffithiiUuid = UUID.fromString("497a9955-5c5a-4f2b-b08c-2135d336d633");
		cryptocoryneGriffithiiConceptUuid = UUID.fromString("e110d2c6-fa07-4459-bb7c-269fa0d8f052");
	    acherontiaLachesisUuid = UUID.fromString("7969821b-a2cf-4d01-95ec-6a5ed0ca3f69");
	    acherontiaLachesisConceptUuid = UUID.fromString("258e28a3-c4e5-4b87-823e-2963c7831ce3");
	}

	@Test
	public void testPersistentEntities() {
		TaxonNameBase acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
		INameCacheStrategy zoologicalStrategy = (INameCacheStrategy)acherontiaLachesis.getCacheStrategy();
		assertEquals("ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy",TestingZoologicalNameCacheStrategy.class,zoologicalStrategy.getClass());

		TaxonNameBase cryptocoryneGriffithii = taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid);
		INameCacheStrategy botanicalStrategy = (INameCacheStrategy)cryptocoryneGriffithii.getCacheStrategy();
		assertEquals("BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy",TestingBotanicalNameCacheStrategy.class,botanicalStrategy.getClass());
	}

	@Test
	public void testHibernateProxyEntities() {
		TaxonNameBase acherontiaLachesis = taxonDao.findByUuid(acherontiaLachesisConceptUuid).getName();
		INameCacheStrategy zoologicalStrategy = (INameCacheStrategy)acherontiaLachesis.getCacheStrategy();
		assertEquals("ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy",TestingZoologicalNameCacheStrategy.class,zoologicalStrategy.getClass());

		TaxonNameBase cryptocoryneGriffithii = taxonDao.findByUuid(cryptocoryneGriffithiiConceptUuid).getName();
		INameCacheStrategy botanicalStrategy = (INameCacheStrategy)cryptocoryneGriffithii.getCacheStrategy();
		assertEquals("BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy",TestingBotanicalNameCacheStrategy.class,botanicalStrategy.getClass());
	}

	@Test
	public void testNewEntities() {
		TaxonNameBase acherontiaLachesis = TaxonNameBase.NewZoologicalInstance(null);
		INameCacheStrategy zoologicalStrategy = (INameCacheStrategy)acherontiaLachesis.getCacheStrategy();
		assertEquals("ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy",TestingZoologicalNameCacheStrategy.class,zoologicalStrategy.getClass());

		TaxonNameBase cryptocoryneGriffithii = BotanicalName.NewInstance(null);
		INameCacheStrategy botanicalStrategy = (INameCacheStrategy)cryptocoryneGriffithii.getCacheStrategy();
		assertEquals("BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy",TestingBotanicalNameCacheStrategy.class,botanicalStrategy.getClass());
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
