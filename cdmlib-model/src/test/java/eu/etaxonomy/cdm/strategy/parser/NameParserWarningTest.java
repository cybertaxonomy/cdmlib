// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.parser;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author a.mueller
 * @created 04.09.2009
 * @version 1.0
 */
public class NameParserWarningTest {
	private static final Logger logger = Logger.getLogger(NameParserWarningTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

/****** TESTS ******************************************	
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.ParserProblem#warningList(int)}.
	 */
	@Test
	public void testWarningList() {
		List<ParserProblem> list = ParserProblem.warningList(5);
		assertEquals("list must include 2 warnings", 2, list.size());
		assertTrue("Warning list should include warning 0", list.contains(ParserProblem.CheckUninomial));
		assertTrue("Warning list should include warning 2", list.contains(ParserProblem.NameReferenceSeparation));
	}
	
	@Test
	public void testAddWarning(){
		int warning = ParserProblem.addWarning(0, ParserProblem.NameReferenceSeparation);
		int expected = (int)Math.pow(2, ParserProblem.NameReferenceSeparation.ordinal()) ;
		assertEquals("Unexpected value for addWarning", expected,warning);
		warning = ParserProblem.addWarning(warning, ParserProblem.CheckDetailOrYear);
		expected = expected + (int)Math.pow(2, ParserProblem.CheckDetailOrYear.ordinal()) ;
		assertEquals("Unexpected value for addWarning", expected,warning);
	}
	
	@Test
	public void testAddWarnings(){
		assertEquals("Unexpected value for addWarning", 23, ParserProblem.addWarnings(21, 6));
	}
	
	@Test
	public void testIsError() {
		assertTrue("NameReferenceSeparation must be error", ParserProblem.NameReferenceSeparation.isError());
	}
	
	@Test
	public void testIsWarning() {
		assertTrue("CheckDetail must be warning", ParserProblem.CheckDetailOrYear.isWarning());
	}
	
	@Test
	public void testHasError() {
		int warning = ParserProblem.addWarning(0, ParserProblem.NameReferenceSeparation);
		warning = ParserProblem.addWarning(warning, ParserProblem.CheckDetailOrYear);
		assertTrue("warning list with NameReferenceSeparation must have error", ParserProblem.hasError(warning));
	}

}
