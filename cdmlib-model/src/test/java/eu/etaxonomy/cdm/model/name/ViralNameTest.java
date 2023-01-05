/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 03.01.2011
 */
public class ViralNameTest {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private IViralName viralName1;
	@Before
	public void setUp() throws Exception {
		viralName1 = TaxonNameFactory.NewViralInstance(Rank.SPECIES());
	}

//****************** TESTS ******************************************/

	@Test
	public void testClone() {
		viralName1.setAcronym("MJU455");
		IViralName clone = viralName1.clone();
		Assert.assertEquals("Acronym should be equal", "MJU455", clone.getAcronym());
	}
}