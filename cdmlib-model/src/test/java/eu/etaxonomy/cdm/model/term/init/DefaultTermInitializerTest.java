/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;

/**
 * @author AM
 */
public class DefaultTermInitializerTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private DefaultTermInitializer defaultVocabularyStore;
	private UUID uuidEnglish;
	private UUID uuidGerman;

	@BeforeEach
	public void setUp() {
		defaultVocabularyStore = new DefaultTermInitializer();
		uuidEnglish = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		uuidGerman = UUID.fromString("d1131746-e58b-4e80-a865-f5182c9c3073");
	}

//*********************** TEST *************************************************/

	@Test
	public void testInitialize() {
		defaultVocabularyStore.initialize();

		assertNotNull(Language.ENGLISH(), "ENGLISH should not be null");
		assertEquals(uuidEnglish, Language.ENGLISH().getUuid(), "The uuid of ENGLISH should equal e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		assertNotNull(Language.ENGLISH().getRepresentation(Language.ENGLISH()), "ENGLISH should have an ENGLISH representation");
		assertNotNull(Language.GERMAN(), "GERMAN should not be null");
		assertEquals(uuidGerman, Language.GERMAN().getUuid(), "The uuid of GERMAN should equal d1131746-e58b-4e80-a865-f5182c9c3073");
		assertNotNull(Language.GERMAN().getRepresentation(Language.ENGLISH()), "GERMAN should have an ENGLISH representation");
	}
}
