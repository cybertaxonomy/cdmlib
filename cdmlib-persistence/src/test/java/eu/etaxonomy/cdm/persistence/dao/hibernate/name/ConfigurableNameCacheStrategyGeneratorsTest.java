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

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
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
		TaxonName acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
		INameCacheStrategy zoologicalStrategy = acherontiaLachesis.getCacheStrategy();
		assertEquals("ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy",TestingZoologicalNameCacheStrategy.class,zoologicalStrategy.getClass());

		TaxonName cryptocoryneGriffithii = taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid);
		INameCacheStrategy botanicalStrategy = cryptocoryneGriffithii.getCacheStrategy();
		assertEquals("BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy",TestingBotanicalNameCacheStrategy.class,botanicalStrategy.getClass());
	}

	@Test
	public void testHibernateProxyEntities() {
		TaxonName acherontiaLachesis = taxonDao.findByUuid(acherontiaLachesisConceptUuid).getName();
		INameCacheStrategy zoologicalStrategy = acherontiaLachesis.getCacheStrategy();
		assertEquals("ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy",TestingZoologicalNameCacheStrategy.class,zoologicalStrategy.getClass());

		TaxonName cryptocoryneGriffithii = taxonDao.findByUuid(cryptocoryneGriffithiiConceptUuid).getName();
		INameCacheStrategy botanicalStrategy = cryptocoryneGriffithii.getCacheStrategy();
		assertEquals("BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy",TestingBotanicalNameCacheStrategy.class,botanicalStrategy.getClass());
	}

	@Test
	public void testNewEntities() {
		TaxonName acherontiaLachesis = TaxonNameFactory.NewZoologicalInstance(null);
		INameCacheStrategy zoologicalStrategy = acherontiaLachesis.getCacheStrategy();
		assertEquals("ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy",TestingZoologicalNameCacheStrategy.class,zoologicalStrategy.getClass());

		TaxonName cryptocoryneGriffithii = TaxonNameFactory.NewBotanicalInstance(null);
		INameCacheStrategy botanicalStrategy = cryptocoryneGriffithii.getCacheStrategy();
		assertEquals("BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy",TestingBotanicalNameCacheStrategy.class,botanicalStrategy.getClass());
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}