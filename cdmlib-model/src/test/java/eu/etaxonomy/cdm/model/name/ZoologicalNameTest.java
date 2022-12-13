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
public class ZoologicalNameTest {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private IZoologicalName zooName1;

	@Before
	public void setUp() throws Exception {
		zooName1 = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES(), "Aus", null, "bus", "infracus", null, null, null, null);
	}

//****************** TESTS ******************************************/

	@Test
	public void testClone() {
		zooName1.setBreed("breed");
		zooName1.setPublicationYear(1956);
		zooName1.setOriginalPublicationYear(1867);
		IZoologicalName clone = zooName1.clone();
		Assert.assertEquals("Breed should be equal", "breed", clone.getBreed());
		Assert.assertEquals("Publication year should be equal", Integer.valueOf(1956), clone.getPublicationYear());
		Assert.assertEquals("Original publication year should be equal", Integer.valueOf(1867), clone.getOriginalPublicationYear());
	}
}