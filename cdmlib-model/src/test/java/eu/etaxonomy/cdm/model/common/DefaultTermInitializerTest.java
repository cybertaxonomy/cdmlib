/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author a.mueller
 * @since 02.03.2009
 */
public class DefaultTermInitializerTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefaultTermInitializerTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.DefaultTermInitializer#initialize()}.
	 */
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
