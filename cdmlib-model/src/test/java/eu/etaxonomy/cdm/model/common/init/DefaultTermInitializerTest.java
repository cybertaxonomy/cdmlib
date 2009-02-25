/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.model.common.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author AM
 *
 */
public class DefaultTermInitializerTest {
	static Logger logger = Logger.getLogger(DefaultTermInitializerTest.class);

	private DefaultTermInitializer defaultVocabularyStore;
	private UUID uuidEnglish;
	private UUID uuidGerman;
	
	@Before
	public void setUp() {
		defaultVocabularyStore = new DefaultTermInitializer();
		uuidEnglish = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		uuidGerman = UUID.fromString("d1131746-e58b-4e80-a865-f5182c9c3073");
	}

/*********************** TEST *************************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.DefaultTermInitializer#loadBasicTerms()}.
	 */
	@Test
	public void testInitialize() {
		defaultVocabularyStore.initialize();
		
		assertNotNull("ENGLISH should not be null",Language.ENGLISH());
		assertEquals("The uuid of ENGLISH should equal e9f8cdb7-6819-44e8-95d3-e2d0690c3523",uuidEnglish,Language.ENGLISH().getUuid());
		assertNotNull("ENGLISH should have an ENGLISH representation",Language.ENGLISH().getRepresentation(Language.ENGLISH()));
		assertNotNull("GERMAN should not be null",Language.GERMAN());
		assertEquals("The uuid of GERMAN should equal d1131746-e58b-4e80-a865-f5182c9c3073",uuidGerman,Language.GERMAN().getUuid());
		assertNotNull("GERMAN should have an ENGLISH representation",Language.GERMAN().getRepresentation(Language.ENGLISH()));
	}
}
