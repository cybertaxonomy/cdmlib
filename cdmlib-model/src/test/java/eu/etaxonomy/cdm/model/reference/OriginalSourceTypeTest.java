/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 6.6.2013
 */
public class OriginalSourceTypeTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testKindOf(){
		assertSame(OriginalSourceType.Lineage, OriginalSourceType.Import.getKindOf());
		assertSame(OriginalSourceType.Lineage, OriginalSourceType.Transformation.getKindOf());
		assertSame(OriginalSourceType.Lineage, OriginalSourceType.Aggregation.getKindOf());
	}
}