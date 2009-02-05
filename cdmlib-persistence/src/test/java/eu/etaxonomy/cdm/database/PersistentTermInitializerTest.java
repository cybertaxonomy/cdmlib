/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
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
}
