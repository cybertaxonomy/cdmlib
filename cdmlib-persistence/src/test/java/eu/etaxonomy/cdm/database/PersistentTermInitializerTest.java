/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

@Disabled
public class PersistentTermInitializerTest extends CdmSpringIntegrationTest {

	@Autowired
	private PersistentTermInitializer persistentTermInitializer;

	@Test
	public void testInit() {
		assertNotNull(persistentTermInitializer, "TermInitializer should exist");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.term.DefaultTermInitializer#initialize()}.
	 */
	@Test
	@Disabled // does not run yet in a test suite as the Language.DEFAULT() is not null then
	public void testInitialize() {
		assertNull(Language.DEFAULT(), "At the beginning of the initialization test the default language should still be null but is not");
		persistentTermInitializer.initialize();
		assertNotNull(persistentTermInitializer, "TermInitializer should exist");
		assertNotNull(Language.DEFAULT(), "TermInitializer should have initialized Language.DEFAULT");
		assertEquals(Language.DEFAULT(), Language.ENGLISH(), "Language.DEFAULT should equal Language.ENGLISH");
		TermVocabulary<Language> voc = Language.DEFAULT().getVocabulary();
		assertNotNull(voc.getRepresentation(Language.DEFAULT()), "language for language vocabulary representation was null but must be default language");
	}

	@Disabled //please commit only with ignore
	@Test
	public void testPrintData() {
		printDataSet(System.out);
	}

	@Test
	public void testGetRepresentations() {
		assertNotNull(Rank.SPECIES(), "Rank.SPECIES() should not be null");
		assertFalse(Rank.SPECIES().getRepresentations().isEmpty(), "Rank.SPECIES().getRepresentations() should not be empty");
		assertEquals("Species", Rank.SPECIES().getLabel(), "Rank.SPECIES().getLabel() should return \"Species\"");

	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
