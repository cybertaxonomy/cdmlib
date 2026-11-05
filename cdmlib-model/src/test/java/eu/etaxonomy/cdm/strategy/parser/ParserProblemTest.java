/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

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
		assertEquals(2, list.size(), "list must include 2 warnings");
		assertTrue(list.contains(ParserProblem.CheckRank), "Warning list should include warning 0");
		assertTrue(list.contains(ParserProblem.NameReferenceSeparation), "Warning list should include warning 2");
	}

	@Test
	public void testAddProblem(){
		int warning = ParserProblem.addProblem(0, ParserProblem.NameReferenceSeparation);
		int expected = (int)Math.pow(2, ParserProblem.NameReferenceSeparation.ordinal()) ;
		assertEquals(expected, warning, "Unexpected value for addWarning");
		warning = ParserProblem.addProblem(warning, ParserProblem.CheckDetailOrYear);
		expected = expected + (int)Math.pow(2, ParserProblem.CheckDetailOrYear.ordinal()) ;
		assertEquals(expected, warning, "Unexpected value for addWarning");
	}

	@Test
	public void testRemoveProblem(){
		int warning = ParserProblem.addProblem(0, ParserProblem.NameReferenceSeparation);
		warning = ParserProblem.addProblem(warning, ParserProblem.CheckRank);
		warning = ParserProblem.addProblem(warning, ParserProblem.CheckDetailOrYear);
		assertEquals(3, ParserProblem.warningList(warning).size(), "Number of problems must be 3");
		assertTrue(ParserProblem.warningList(warning).contains(ParserProblem.CheckRank), "Check Rank must be a problem");

		warning = ParserProblem.removeProblem(warning, ParserProblem.CheckRank);
		assertEquals(2, ParserProblem.warningList(warning).size(), "Number of problems must be 2");
		assertFalse(ParserProblem.warningList(warning).contains(ParserProblem.CheckRank), "Check Rank must not be a problem anymore");

		warning = ParserProblem.removeProblem(warning, ParserProblem.CheckRank);
		assertEquals(2, ParserProblem.warningList(warning).size(), "Number of problems must be 2");
		assertFalse(ParserProblem.warningList(warning).contains(ParserProblem.CheckRank), "Check Rank must not be a problem anymore");

		warning = ParserProblem.removeProblem(warning, null);
		assertEquals(2, ParserProblem.warningList(warning).size(), "Number of problems must be 2");
		assertFalse(ParserProblem.warningList(warning).contains(ParserProblem.CheckRank), "Check Rank must not be a problem anymore");
	}

	@Test
	public void testAddProblems(){
		assertEquals(23, ParserProblem.addProblems(21, 6), "Unexpected value for addWarning");
	}

	@Test
	public void testIsError() {
		assertTrue(ParserProblem.NameReferenceSeparation.isError(), "NameReferenceSeparation must be error");
	}

	@Test
	public void testIsWarning() {
		assertTrue(ParserProblem.CheckDetailOrYear.isWarning(), "CheckDetail must be warning");
	}

	@Test
	public void testHasError() {
		int warning = ParserProblem.addProblem(0, ParserProblem.NameReferenceSeparation);
		warning = ParserProblem.addProblem(warning, ParserProblem.CheckDetailOrYear);
		assertTrue(ParserProblem.hasError(warning), "warning list with NameReferenceSeparation must have error");
	}
}
