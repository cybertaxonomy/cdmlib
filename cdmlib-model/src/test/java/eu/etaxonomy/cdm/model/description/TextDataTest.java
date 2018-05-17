/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;

/**
 * @author a.mueller
 * @since 23.04.2008
 */
public class TextDataTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TextDataTest.class);


	private TextData textDataLeer;
	private TextData textData1;
	private TextFormat format1;
	private LanguageString languageString1;


	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		textDataLeer = TextData.NewInstance();
		format1 = TextFormat.NewInstance();
		textData1 = TextData.NewInstance("testText", Language.DEFAULT(), format1);
		languageString1 = LanguageString.NewInstance("langText", Language.GERMAN());
	}

/* ************************** TESTS **********************************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#NewInstance()}.
	 */
	@Test
	public void testNewInstance() {
		assertNotNull(textDataLeer);
		assertNotNull(textDataLeer.getMultilanguageText());
		assertEquals(0, textDataLeer.size());
		assertEquals(0, textDataLeer.countLanguages());
		assertNull(textDataLeer.getFormat());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#NewInstance()}.
	 */
	@Test
	public void testNewInstanceStringLanguageTextFormat() {
		assertNotNull(textData1);
		assertNotNull(textData1.getMultilanguageText());
		assertEquals(1, textData1.size());
		assertEquals(1, textData1.countLanguages());
//		assertEquals("testText", textData1.getMultilanguageText().getText(Language.DEFAULT()));
		assertNotNull(textData1.getFormat());
		assertSame(format1, textData1.getFormat());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#TextData()}.
	 */
	@Test
	public void testTextData() {
		textDataLeer = new TextData();
		assertNotNull(textDataLeer);
		assertNotNull(textDataLeer.getMultilanguageText());
		assertEquals(0, textDataLeer.getMultilanguageText().size());
		assertEquals(0, textDataLeer.countLanguages());
		assertNull(textDataLeer.getFormat());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#getTexts()}.
	 */
	@Test
	public void testGetText() {
		assertNotNull(textData1.getText(Language.DEFAULT()));
		assertNull(textDataLeer.getText(Language.DEFAULT()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#getMultilanguageText()}.
	 */
	@Test
	public void testGetMultilanguageText() {
		assertNotNull(textData1.getMultilanguageText());
//		assertEquals("testText", textData1.getMultilanguageText().getText(Language.DEFAULT()));
		assertNotNull(textDataLeer.getMultilanguageText());
//		assertNull(textDataLeer.getMultilanguageText().getText(Language.DEFAULT()));
		assertEquals(0, textDataLeer.getMultilanguageText().size());
	}

	/**
	 * This test reproduces a bug in java runtime.
	 * The HashMap used to implement the MultilanguageText fails in jre1.6_11 b03 win32 to
	 * to find existing Language keys.
	 * FIXME this test fails to reproduce the bug -> integration test needed?
	 */
	@Test
	public void testPreferredLanguageString() {
		//FIXME move to integration test: List<Language> languages = termService.getLanguagesByLocale(locales.elements());
		List<Language> preferredLanguages = Arrays.asList(new Language[]{Language.DEFAULT()});
		assertNotNull(textData1.getPreferredLanguageString(preferredLanguages));
	}

//	/**
//	 *
//	Test method for {@link eu.etaxonomy.cdm.model.description.TextData#setMultilanguageText()}.
//	 */
//	@Test
//	public void testSetMultilanguageText() {
//		MultilanguageText multilanguageText = MultilanguageText.NewInstance();
//		assertFalse(multilanguageText.equals(textData1.getMultilanguageText()));
//		Map<Language, LanguageString> texts = textData1.getMultilanguageText();
//		LanguageString text = texts.get(Language.DEFAULT());
//		text.setText("This is a test");
//		textData1.getMultilanguageText().clear();
//		assertNotNull(textData1.getMultilanguageText());
//		assertEquals(0, textData1.getMultilanguageText().size());
//	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#addText(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testPutTextStringLanguage() {
		textDataLeer.putText(Language.GERMAN(), "xx");
		assertEquals(textDataLeer.putText(Language.FRENCH(), "francais").getLanguage(), Language.FRENCH());
		textDataLeer.putText(null, "nothing");
		textDataLeer.putText(Language.CHINESE(), null);
		assertEquals(4 , textDataLeer.size());
		assertEquals("deutsch", textDataLeer.putText(Language.GERMAN(), "deutsch").getText());
		assertEquals(4 , textDataLeer.getMultilanguageText().size());
		assertEquals("deutsch", textDataLeer.getText(Language.GERMAN()));
		assertEquals("francais", textDataLeer.getText(Language.FRENCH()));
		assertEquals("nothing", textDataLeer.getText(null));
		assertEquals(null, textDataLeer.getText(Language.CHINESE()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#addText(eu.etaxonomy.cdm.model.common.LanguageString)}.
	 */
	@Test
	public void testPutTextLanguageString() {
		LanguageString deutsch = LanguageString.NewInstance("xx", Language.GERMAN());
		textDataLeer.putText(deutsch);
		assertNull(textDataLeer.putText(LanguageString.NewInstance("francais", Language.FRENCH())));
		textDataLeer.putText(LanguageString.NewInstance("nothing", null));
		textDataLeer.putText(LanguageString.NewInstance(null, Language.CHINESE()));
		assertNotNull(textDataLeer.getMultilanguageText());
		assertEquals(4 , textDataLeer.getMultilanguageText().size());
		assertEquals(deutsch, textDataLeer.putText(LanguageString.NewInstance("deutsch", Language.GERMAN())));
		assertEquals(4 , textDataLeer.getMultilanguageText().size());

		assertEquals("deutsch", textDataLeer.getText(Language.GERMAN()));
		assertEquals("francais", textDataLeer.getText(Language.FRENCH()));
		assertEquals("nothing", textDataLeer.getText(null));
		assertEquals(null, textDataLeer.getText(Language.CHINESE()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#removeText(eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testRemoveText() {
		assertEquals(1, textData1.countLanguages());
		assertNull(textData1.removeText(Language.CHINESE()));
		assertEquals(1, textData1.countLanguages());
		LanguageString deutsch = LanguageString.NewInstance("xx", Language.GERMAN());
		textData1.putText(deutsch);
		textData1.putText(LanguageString.NewInstance("nothing", null));
		assertEquals(3, textData1.countLanguages());
		assertEquals(deutsch, textData1.removeText(Language.GERMAN()));
		assertEquals(2, textData1.countLanguages());
		textData1.removeText(null);
		assertEquals(1, textData1.countLanguages());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#getFormat()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#setFormat(eu.etaxonomy.cdm.model.description.TextFormat)}.
	 */
	@Test
	public void testGetSetFormat() {
		textDataLeer.setFormat(format1);
		assertSame(format1, textDataLeer.getFormat());
		textDataLeer.setFormat(null);
		assertNull(textDataLeer.getFormat());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#countLanguages()}.
	 */
	@Test
	public void testCountLanguages() {
		assertEquals(1, textData1.countLanguages());
		textData1.putText(LanguageString.NewInstance("nothing", null));
		assertEquals(2, textData1.countLanguages());
		textData1.removeText(null);
		assertEquals(1, textData1.countLanguages());
		textData1.removeText(Language.FRENCH());
		assertEquals(1, textData1.countLanguages());
		textData1.removeText(Language.DEFAULT());
		assertEquals(0, textData1.countLanguages());

	}

	@Test
	public void testClone(){
		TextData clone = (TextData) textData1.clone();
		LanguageString langStringClone = clone.getLanguageText(Language.DEFAULT());
		LanguageString langString = textData1.getLanguageText(Language.DEFAULT());
		assertEquals(langStringClone.getText(), langString.getText());
		//assertEquals();
	}

	@Test
	public void testCloneMultiLanguageText() {
		MultilanguageText test = MultilanguageText.NewInstance();
		LanguageString testString = LanguageString.NewInstance("test", Language.ENGLISH());
		test.put(testString);
		MultilanguageText clone = test.clone();
		assertNotSame(clone.get(Language.ENGLISH()), test.get(Language.ENGLISH()));
		assertEquals(clone.get(Language.ENGLISH()).getText(), test.get(Language.ENGLISH()).getText());
	}


}
