package eu.etaxonomy.cdm.remote.dto;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ResultSetPageSTOTest {
	ResultSetPageSTO rs;

	@Before
	public void setUp() throws Exception {
		rs = new ResultSetPageSTO();
		rs.setTotalResultsCount(37);
	}

	@Test
	public void testSetTotalResultsCount() {
		rs.setTotalResultsCount(237);
	}

	@Test
	public void testSetPageSize() {
		rs.setPageSize(10);
	}

	@Test
	public void testSetPageNumber() {
		rs.setPageNumber(2);
	}

	@Test
	public void testGetTotalPageCount() {
		assertEquals(2, rs.getTotalPageCount());
	}

	@Test
	public void testGetResultsOnPage() {
		assertEquals(12, rs.getResultsOnPage());
	}

}
