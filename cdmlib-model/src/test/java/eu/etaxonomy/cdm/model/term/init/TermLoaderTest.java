/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term.init;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.model.term.VocabularyEnum;

/**
 * @author a.mueller
 */
public class TermLoaderTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private TermLoader termLoader;
	private Map<UUID,DefinedTermBase> terms;

	private UUID uuidGenus;
	private UUID uuidTautonymy;

	@BeforeEach
	public void setUp() throws Exception {
		termLoader = new TermLoader();
		terms = new HashMap<>();
		uuidGenus = UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a");
		uuidTautonymy = UUID.fromString("84521f09-3e10-43f5-aa6f-2173a55a6790");
	}

//************** TESTS **********************************/

	@Test
	public void testLoadRanks() {
        TermVocabulary<Rank> defaultRanks = termLoader.loadTerms(VocabularyEnum.Rank, terms);
		assertNotNull(defaultRanks.findTermByUuid(uuidGenus));
		assertTrue(terms.containsKey(uuidGenus));
	}

	@Test
	public void testLoadNameTypeDesignationStatus() {
        TermVocabulary<NameTypeDesignationStatus> defaultStatus = termLoader.loadTerms(VocabularyEnum.NameTypeDesignationStatus, terms);
		assertNotNull(defaultStatus.findTermByUuid(uuidTautonymy));
		assertTrue(terms.containsKey(uuidTautonymy));
	}

	@Test
	public void testUnloadAllTerms(){
		DefaultTermInitializer initializer = new DefaultTermInitializer();
		initializer.initialize();
		assertNotNull(AnnotationType.EDITORIAL(), "Editorial annotation type should be loaded");
		termLoader.unloadAllTerms();
		assertNull(AnnotationType.EDITORIAL(), "Editorial annotation type should be unloaded now");
	}
}
