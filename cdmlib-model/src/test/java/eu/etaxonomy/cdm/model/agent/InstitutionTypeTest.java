// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


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

/**
 * @author a.mueller
 * @created 08.06.2009
 * @version 1.0
 */
public class InstitutionTypeTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(InstitutionTypeTest.class);

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
	public void testDummyValue(){
		UUID uuidDummy = UUID.fromString("bea94a6c-472b-421c-abc1-52f797c51dbf");
		InstitutionType dummy = InstitutionType.getTermByUuid(uuidDummy);
		Assert.notNull(dummy, "Dummy value must not be null");
		assertEquals("Wrong label for dummy InstitutionType value", "institution_type_dummy", dummy.getRepresentation(Language.ENGLISH()).getLabel());
		assertEquals("Wrong abbreviated label for dummy InstitutionType value", "i", dummy.getRepresentation(Language.ENGLISH()).getAbbreviatedLabel());
		assertEquals("Wrong text for dummy InstitutionType value", "itdummy", dummy.getRepresentation(Language.ENGLISH()).getText());
	}
	
}
