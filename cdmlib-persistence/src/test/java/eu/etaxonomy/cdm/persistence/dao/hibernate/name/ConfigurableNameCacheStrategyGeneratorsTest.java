/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

@Disabled
@CdmDataSet("TaxonNameDaoHibernateImplTest.xml")
public class ConfigurableNameCacheStrategyGeneratorsTest extends CdmSpringIntegrationTest {

	@Autowired
	private ITaxonNameDao taxonNameDao;

	@Autowired
	private ITaxonDao taxonDao;

	private UUID cryptocoryneGriffithiiUuid;
	private UUID acherontiaLachesisUuid;
	private UUID acherontiaLachesisConceptUuid;
	private UUID cryptocoryneGriffithiiConceptUuid;

	@BeforeEach
	public void setUp() {
		cryptocoryneGriffithiiUuid = UUID.fromString("497a9955-5c5a-4f2b-b08c-2135d336d633");
		cryptocoryneGriffithiiConceptUuid = UUID.fromString("e110d2c6-fa07-4459-bb7c-269fa0d8f052");
	    acherontiaLachesisUuid = UUID.fromString("7969821b-a2cf-4d01-95ec-6a5ed0ca3f69");
	    acherontiaLachesisConceptUuid = UUID.fromString("258e28a3-c4e5-4b87-823e-2963c7831ce3");
	}

	@Test
	public void testPersistentEntities() {
		TaxonName acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
		INameCacheStrategy zoologicalStrategy = acherontiaLachesis.cacheStrategy();
		assertEquals(TestingZoologicalNameCacheStrategy.class, zoologicalStrategy.getClass(), "ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy");

		TaxonName cryptocoryneGriffithii = taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid);
		INameCacheStrategy botanicalStrategy = cryptocoryneGriffithii.cacheStrategy();
		assertEquals(TestingBotanicalNameCacheStrategy.class, botanicalStrategy.getClass(), "BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy");
	}

	@Test
	public void testHibernateProxyEntities() {
		TaxonName acherontiaLachesis = taxonDao.findByUuid(acherontiaLachesisConceptUuid).getName();
		INameCacheStrategy zoologicalStrategy = acherontiaLachesis.cacheStrategy();
		assertEquals(TestingZoologicalNameCacheStrategy.class, zoologicalStrategy.getClass(), "ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy");

		TaxonName cryptocoryneGriffithii = taxonDao.findByUuid(cryptocoryneGriffithiiConceptUuid).getName();
		INameCacheStrategy botanicalStrategy = cryptocoryneGriffithii.cacheStrategy();
		assertEquals(TestingBotanicalNameCacheStrategy.class, botanicalStrategy.getClass(), "BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy");
	}

	@Test
	public void testNewEntities() {
		TaxonName acherontiaLachesis = TaxonNameFactory.NewZoologicalInstance(null);
		INameCacheStrategy zoologicalStrategy = acherontiaLachesis.cacheStrategy();
		assertEquals(TestingZoologicalNameCacheStrategy.class, zoologicalStrategy.getClass(), "ZoologicalName.cacheStrategy should be TestingZoologicalNameCacheStrategy");

		TaxonName cryptocoryneGriffithii = TaxonNameFactory.NewBotanicalInstance(null);
		INameCacheStrategy botanicalStrategy = cryptocoryneGriffithii.cacheStrategy();
		assertEquals(TestingBotanicalNameCacheStrategy.class, botanicalStrategy.getClass(), "BotanicalName.cacheStrategy should be TestingBotanicalNameCacheStrategy");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}