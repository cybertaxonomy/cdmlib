/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 */
public class LanguageTest extends EntityTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(LanguageTest.class);

// ************************** TESTS **********************************************/

	@Test
	public void testToString() {
		Language lang = Language.ENGLISH();
		Assert.assertEquals("English should return 'English' by toString()", "English", lang.toString());
	}

	@Test
	public void testResetTerms() {
		Language lang = Language.ENGLISH();
		Assert.assertNotNull("'English' should exist", lang);
		Language unknown = Language.ENGLISH();
		Assert.assertNotNull("'Unknown language' should exist", unknown);
		lang.resetTerms();
		Assert.assertNull("No language should exist anymore", Language.ENGLISH());
		Assert.assertNull("No language should exist anymore", Language.UNKNOWN_LANGUAGE());
		new DefaultTermInitializer().initialize();
	}

	@Test
	public void testGetTermByUuid() {
		Language lang = Language.getTermByUuid(Language.uuidZaza_Dimili_Dimli_Kirdki_Kirmanjki_Zazaki);
		Assert.assertNotNull("'Zaza' should exist", lang);
		Assert.assertEquals("Uuid must be the equal", "e4bf2ec8-4c1a-4ece-9df1-4890a7f18457", lang.getUuid().toString());
	}

	@Test
	public void testENGLISH() {
		Language lang = Language.ENGLISH();
		Assert.assertNotNull("'English' should exist", lang);
	}

	@Test
	public void testUNKNOWN_LANGUAGE() {
		Language unknown = Language.UNKNOWN_LANGUAGE();
		Assert.assertNotNull("'Unknown Language' of undefinedLanguages vocabulary should exist", unknown);
	}

	@Test
	public void testORIGINAL_LANGUAGE() {
		Language original = Language.UNKNOWN_LANGUAGE();
		Assert.assertNotNull("'Original Language' of undefinedLanguages vocabulary should exist", original);
	}

	@Test
	public void testDEFAULT() {
		Language lang = Language.DEFAULT();
		Assert.assertNotNull("The default language must not be null", lang);
		Assert.assertEquals("Uuid of defautl language should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'(english)", Language.uuidEnglish, lang.getUuid());
	}

	@Test
	public void testCSV_LANGUAGE() {
		Language lang = Language.CSV_LANGUAGE();
		Assert.assertNotNull("The csv language must not be null", lang);
		Assert.assertEquals("Uuid of csv language should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'(english)", Language.uuidEnglish, lang.getUuid());
	}

	//once the descriptioin for languages are more explaining texts this test must be adapted
	@Test
	public void testGetLanguageByDescription() {
		Language lang = Language.getLanguageByDescription("English");
		Assert.assertNotNull("A language should be found for label 'English'", lang);
		Assert.assertEquals("Uuid of language 'English' should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'", Language.uuidEnglish, lang.getUuid());
	}

	@Test
	public void testGetLanguageByLabel() {
		Language lang = Language.getLanguageByLabel("English");
		Assert.assertNotNull("A language should be found for label 'English'", lang);
		Assert.assertEquals("Uuid of language 'English' should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'", Language.uuidEnglish, lang.getUuid());
	}
}