/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 */
public class LanguageTest extends EntityTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

// ************************** TESTS **********************************************/

	@Test
	public void testToString() {
		Language lang = Language.ENGLISH();
		assertEquals("English", lang.toString(), "English should return 'English' by toString()");
	}

	@Test
	public void testResetTerms() {
		Language lang = Language.ENGLISH();
		assertNotNull(lang, "'English' should exist");
		Language unknown = Language.ENGLISH();
		assertNotNull(unknown, "'Unknown language' should exist");
		lang.resetTerms();
		assertNull(Language.ENGLISH(), "No language should exist anymore");
		assertNull(Language.UNKNOWN_LANGUAGE(), "No language should exist anymore");
		new DefaultTermInitializer().initialize();
	}

	@Test
	public void testGetTermByUuid() {
		Language lang = Language.getTermByUuid(Language.uuidZaza_Dimili_Dimli_Kirdki_Kirmanjki_Zazaki);
		assertNotNull(lang, "'Zaza' should exist");
		assertEquals("e4bf2ec8-4c1a-4ece-9df1-4890a7f18457", lang.getUuid().toString(), "Uuid must be the equal");
	}

	@Test
	public void testENGLISH() {
		Language lang = Language.ENGLISH();
		assertNotNull(lang, "'English' should exist");
	}

	@Test
	public void testUNKNOWN_LANGUAGE() {
		Language unknown = Language.UNKNOWN_LANGUAGE();
		assertNotNull(unknown, "'Unknown Language' of undefinedLanguages vocabulary should exist");
	}

	@Test
	public void testORIGINAL_LANGUAGE() {
		Language original = Language.UNKNOWN_LANGUAGE();
		assertNotNull(original, "'Original Language' of undefinedLanguages vocabulary should exist");
	}

	@Test
	public void testDEFAULT() {
		Language lang = Language.DEFAULT();
		assertNotNull(lang, "The default language must not be null");
		assertEquals(Language.uuidEnglish, lang.getUuid(), "Uuid of defautl language should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'(english)");
	}

	@Test
	public void testCSV_LANGUAGE() {
		Language lang = Language.CSV_LANGUAGE();
		assertNotNull(lang, "The csv language must not be null");
		assertEquals(Language.uuidEnglish, lang.getUuid(), "Uuid of csv language should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'(english)");
	}

	//once the descriptioin for languages are more explaining texts this test must be adapted
	@Test
	public void testGetLanguageByDescription() {
		Language lang = Language.getLanguageByDescription("English");
		assertNotNull(lang, "A language should be found for label 'English'");
		assertEquals(Language.uuidEnglish, lang.getUuid(), "Uuid of language 'English' should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'");
	}

	@Test
	public void testGetLanguageByLabel() {
		Language lang = Language.getLanguageByLabel("English");
		assertNotNull(lang, "A language should be found for label 'English'");
		assertEquals(Language.uuidEnglish, lang.getUuid(), "Uuid of language 'English' should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'");
	}
}