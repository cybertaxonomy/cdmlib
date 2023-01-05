/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 04.09.2009
 */
public class ParserProblemTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

//****** TESTS ******************************************

	@Test
	public void testWarningList() {
		List<ParserProblem> list = ParserProblem.warningList(5);
		assertEquals("list must include 2 warnings", 2, list.size());
		assertTrue("Warning list should include warning 0", list.contains(ParserProblem.CheckRank));
		assertTrue("Warning list should include warning 2", list.contains(ParserProblem.NameReferenceSeparation));
	}

	@Test
	public void testAddProblem(){
		int warning = ParserProblem.addProblem(0, ParserProblem.NameReferenceSeparation);
		int expected = (int)Math.pow(2, ParserProblem.NameReferenceSeparation.ordinal()) ;
		assertEquals("Unexpected value for addWarning", expected,warning);
		warning = ParserProblem.addProblem(warning, ParserProblem.CheckDetailOrYear);
		expected = expected + (int)Math.pow(2, ParserProblem.CheckDetailOrYear.ordinal()) ;
		assertEquals("Unexpected value for addWarning", expected,warning);
	}

	@Test
	public void testRemoveProblem(){
		int warning = ParserProblem.addProblem(0, ParserProblem.NameReferenceSeparation);
		warning = ParserProblem.addProblem(warning, ParserProblem.CheckRank);
		warning = ParserProblem.addProblem(warning, ParserProblem.CheckDetailOrYear);
		assertEquals("Number of problems must be 3", 3, ParserProblem.warningList(warning).size());
		assertTrue("Check Rank must be a problem", ParserProblem.warningList(warning).contains(ParserProblem.CheckRank));

		warning = ParserProblem.removeProblem(warning, ParserProblem.CheckRank);
		assertEquals("Number of problems must be 2", 2, ParserProblem.warningList(warning).size());
		assertFalse("Check Rank must not be a problem anymore", ParserProblem.warningList(warning).contains(ParserProblem.CheckRank));

		warning = ParserProblem.removeProblem(warning, ParserProblem.CheckRank);
		assertEquals("Number of problems must be 2", 2, ParserProblem.warningList(warning).size());
		assertFalse("Check Rank must not be a problem anymore", ParserProblem.warningList(warning).contains(ParserProblem.CheckRank));

		warning = ParserProblem.removeProblem(warning, null);
		assertEquals("Number of problems must be 2", 2, ParserProblem.warningList(warning).size());
		assertFalse("Check Rank must not be a problem anymore", ParserProblem.warningList(warning).contains(ParserProblem.CheckRank));
	}

	@Test
	public void testAddProblems(){
		assertEquals("Unexpected value for addWarning", 23, ParserProblem.addProblems(21, 6));
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
		int warning = ParserProblem.addProblem(0, ParserProblem.NameReferenceSeparation);
		warning = ParserProblem.addProblem(warning, ParserProblem.CheckDetailOrYear);
		assertTrue("warning list with NameReferenceSeparation must have error", ParserProblem.hasError(warning));
	}
}