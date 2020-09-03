/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 02.03.2009
 */
public class DefaultTermInitializerTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefaultTermInitializerTest.class);

	@Test
	@Ignore // does not run yet in a test suite as the Language.DEFAULT() is not null then
	public void testInitialize() {
		assertNull("At the beginning of the initialization test the default language should still be null but is not", Language.DEFAULT());
		DefaultTermInitializer initalizer = new DefaultTermInitializer();
		initalizer.initialize();
		assertNotNull("Default language should be english but is null", Language.DEFAULT());
		TermVocabulary<Language> voc = Language.DEFAULT().getVocabulary();
		assertNotNull("language for language vocabulary representation was null but must be default language", voc.getRepresentation(Language.DEFAULT()));
	}
}