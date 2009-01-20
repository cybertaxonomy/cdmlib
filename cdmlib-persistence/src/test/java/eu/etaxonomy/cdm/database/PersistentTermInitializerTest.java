package eu.etaxonomy.cdm.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileOutputStream;

import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class PersistentTermInitializerTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	private PersistentTermInitializer persistentTermInitializer;
	
	@Test
	public void testInit() throws Exception {
		assertNotNull("TermInitializer should exist",persistentTermInitializer);
		assertNotNull("TermInitializer should have initialized Language.DEFAULT",Language.DEFAULT());
		assertEquals("Language.DEFAULT should equal Language.ENGLISH",Language.DEFAULT(),Language.ENGLISH());
		printDataSet(new FileOutputStream("test.xml"));
	}

}
