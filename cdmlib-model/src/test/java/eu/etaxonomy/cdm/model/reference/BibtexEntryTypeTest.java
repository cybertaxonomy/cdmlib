// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;

/**
 * @author a.mueller
 * @created 08.06.2009
 * @version 1.0
 */
public class BibtexEntryTypeTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BibtexEntryTypeTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DefaultTermInitializer initializer = new DefaultTermInitializer();
		initializer.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
//****************** TESTS *******************************************************/	
	
	@Test
	public void testBookletValue(){
		UUID uuidBooklet = UUID.fromString("5870e82f-eaa0-4e93-9592-51998fd65c0b");
		BibtexEntryType booklet = BibtexEntryType.getTermByUuid(uuidBooklet);
		Assert.notNull(booklet, "Booklet type must not be null");
		assertEquals("Wrong label for Booklet type", "Booklet", booklet.getRepresentation(Language.ENGLISH()).getLabel());
	}
	
}
