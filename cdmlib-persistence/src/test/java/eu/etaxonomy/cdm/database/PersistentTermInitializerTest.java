package eu.etaxonomy.cdm.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@Ignore
@SpringApplicationContext("classpath:eu/etaxonomy/cdm/applicationContext-testPersistentDataSource.xml")
public class PersistentTermInitializerTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	private PersistentTermInitializer persistentTermInitializer;
	
	@Test
	public void testInit() {
		assertNotNull("TermInitializer should exist",persistentTermInitializer);
		assertNotNull("TermInitializer should have initialized Language.DEFAULT",Language.DEFAULT());
		assertEquals("Language.DEFAULT should equal Language.ENGLISH",Language.DEFAULT(),Language.ENGLISH());
	}

	
	@Test
	@DataSet("TermsDataSet.xml")
	public void testFirstPass() {
		Map<UUID, DefinedTermBase> persistedTerms = new HashMap<UUID, DefinedTermBase>();
		persistentTermInitializer.firstPass(Rank.class, persistedTerms);
	}

	@Ignore
	@Test
	public void testPrintData() {
		printDataSet(System.out);
	}
	
}
