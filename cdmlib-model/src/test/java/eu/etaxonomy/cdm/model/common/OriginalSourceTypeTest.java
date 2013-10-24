/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @created 6.6.2013
 *
 */
public class OriginalSourceTypeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testOrdinal() {
		String message = "the order of the terms in the enumeration must not be changed or changed in ALL existing databases.";
		assertEquals(message, 0, OriginalSourceType.Unknown.ordinal());
		assertEquals(message, 1, OriginalSourceType.PrimaryTaxonomicSource.ordinal());
		assertEquals(message, 2, OriginalSourceType.Lineage.ordinal());
		assertEquals(message, 3, OriginalSourceType.Import.ordinal());
		assertEquals(message, 4, OriginalSourceType.Transformation.ordinal());
		assertEquals(message, 5, OriginalSourceType.Aggregation.ordinal());
		assertEquals(message, 6, OriginalSourceType.Other.ordinal());
	}

}
