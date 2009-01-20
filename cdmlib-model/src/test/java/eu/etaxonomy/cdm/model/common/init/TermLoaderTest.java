/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.model.common.init;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.init.ITermInitializer;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 *
 */
public class TermLoaderTest {
	static Logger logger = Logger.getLogger(TermLoaderTest.class);

	private TermLoader termLoader;
	private Map<UUID,DefinedTermBase> terms;

	private UUID uuidGenus;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		termLoader = new TermLoader();
		terms = new HashMap<UUID,DefinedTermBase>();
		uuidGenus = UUID.fromString("1b11c34c-48a8-4efa-98d5-84f7f66ef43a");
	}
	
/************** TESTS **********************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.TermLoader#loadTerms(java.lang.Class, java.lang.String, boolean)}.
	 */
	@Test
	public void testLoadRanks() {
        TermVocabulary<Rank> defaultRanks = termLoader.loadTerms(Rank.class, terms);
		assertNotNull(defaultRanks.findTermByUuid(uuidGenus));
		assertTrue(terms.containsKey(uuidGenus));
	}

}
