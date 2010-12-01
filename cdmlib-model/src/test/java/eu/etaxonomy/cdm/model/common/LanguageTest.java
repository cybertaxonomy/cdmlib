/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.mueller
 *
 */
public class LanguageTest {
	private static final Logger logger = Logger.getLogger(LanguageTest.class);
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new DefaultTermInitializer().initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

// ************************** TESTS **********************************************/	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#toString()}.
	 */
	//tests needs to be adapted once the labels for languages are changed
	@Test
	public void testToString() {
		Language lang = Language.ENGLISH();
		Assert.assertEquals("English should return 'eng' by toString()", "eng", lang.toString());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#resetTerms()}.
	 */
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


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#getTermByUuid(java.util.UUID)}.
	 */
	@Test
	public void testGetTermByUuid() {
		Language lang = Language.getTermByUuid(Language.uuidZaza_Dimili_Dimli_Kirdki_Kirmanjki_Zazaki);
		Assert.assertNotNull("'Zaza' should exist", lang);
		Assert.assertEquals("Uuid must be the equal", "e4bf2ec8-4c1a-4ece-9df1-4890a7f18457", lang.getUuid().toString());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#ENGLISH()}.
	 */
	@Test
	public void testENGLISH() {
		Language lang = Language.ENGLISH();
		Assert.assertNotNull("'English' should exist", lang);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#UNKNOWN_LANGUAGE()}.
	 */
	@Test
	public void testUNKNOWN_LANGUAGE() {
		Language unknown = Language.UNKNOWN_LANGUAGE();
		Assert.assertNotNull("'Unknown Language' of undefinedLanguages vocabulary should exist", unknown);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#ORIGINAL_LANGUAGE()}.
	 */
	@Test
	public void testORIGINAL_LANGUAGE() {
		Language original = Language.UNKNOWN_LANGUAGE();
		Assert.assertNotNull("'Original Language' of undefinedLanguages vocabulary should exist", original);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT()}.
	 */
	@Test
	public void testDEFAULT() {
		Language lang = Language.DEFAULT();
		Assert.assertNotNull("The default language must not be null", lang);
		Assert.assertEquals("Uuid of defautl language should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'(english)", Language.uuidEnglish, lang.getUuid());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#CSV_LANGUAGE()}.
	 */
	@Test
	public void testCSV_LANGUAGE() {
		Language lang = Language.CSV_LANGUAGE();
		Assert.assertNotNull("The csv language must not be null", lang);
		Assert.assertEquals("Uuid of csv language should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'(english)", Language.uuidEnglish, lang.getUuid());

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#getLanguageByDescription(java.lang.String)}.
	 */
	//once the descriptioin for languages are more explaining texts this test must be adapted
	@Test
	public void testGetLanguageByDescription() {
		Language lang = Language.getLanguageByDescription("English"); 
		Assert.assertNotNull("A language should be found for label 'English'", lang);
		Assert.assertEquals("Uuid of language 'English' should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'", Language.uuidEnglish, lang.getUuid());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Language#getLanguageByLabel(java.lang.String)}.
	 */
	//once the labels for languages are changed from iso 639-2 to textual labels this test must be adapted
	@Test
	public void testGetLanguageByLabel() {
		Language lang = Language.getLanguageByLabel("eng"); 
		Assert.assertNotNull("A language should be found for label 'eng'", lang);
		Assert.assertEquals("Uuid of language 'eng' should be 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523'", Language.uuidEnglish, lang.getUuid());
	}

}
