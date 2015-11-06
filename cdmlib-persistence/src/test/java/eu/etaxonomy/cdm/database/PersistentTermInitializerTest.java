/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
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
	}

//  As firstPass is not used anymore we also do not need this test
//	@Test
//	@DataSet("TermsDataSet.xml")
//	public void testFirstPass() {
//		Map<UUID, DefinedTermBase> persistedTerms = new HashMap<UUID, DefinedTermBase>();
//		persistentTermInitializer.firstPass(VocabularyEnum.Rank, persistedTerms);
//	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.DefaultTermInitializer#initialize()}.
	 */
	@Test
	@Ignore // does not run yet in a test suite as the Language.DEFAULT() is not null then
	public void testInitialize() {
		assertNull("At the beginning of the initialization test the default language should still be null but is not", Language.DEFAULT());
		persistentTermInitializer.initialize();
		assertNotNull("TermInitializer should exist",persistentTermInitializer);
		assertNotNull("TermInitializer should have initialized Language.DEFAULT",Language.DEFAULT());
		assertEquals("Language.DEFAULT should equal Language.ENGLISH",Language.DEFAULT(),Language.ENGLISH());
		TermVocabulary<Language> voc = Language.DEFAULT().getVocabulary();
		assertNotNull("language for language vocabulary representation was null but must be default language", voc.getRepresentation(Language.DEFAULT()));
	}

	@Ignore //please commit only with ignore
	@Test
	public void testPrintData() {
		printDataSet(System.out);
	}

	@Test
	public void testGetRepresentations() {
		assertNotNull("Rank.SPECIES() should not be null", Rank.SPECIES());
		assertFalse("Rank.SPECIES().getRepresentations() should not be empty",Rank.SPECIES().getRepresentations().isEmpty());
		assertEquals("Rank.SPECIES().getLabel() should return \"Species\"","Species",Rank.SPECIES().getLabel());

	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
