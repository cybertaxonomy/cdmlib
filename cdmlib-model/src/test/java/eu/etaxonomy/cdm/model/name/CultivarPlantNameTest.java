/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 03.01.2011
 */
public class CultivarPlantNameTest {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CultivarPlantNameTest.class);

	private static ICultivarPlantName name1;

	@Before
	public void setUp() throws Exception {
		name1 = TaxonNameFactory.NewCultivarInstance(Rank.SPECIES());
		name1.setGenusOrUninomial("Aus");
		name1.setSpecificEpithet("bus");
		name1.setCultivarEpithet("cultivarus");
		name1.setCultivarGroupEpithet("Cult Group");
	}

// ******************* TESTS *************************************/

	@Test
	public void testClone(){
		ICultivarPlantName clone = name1.clone();
		Assert.assertEquals("Cultivar string should be equal", "cultivarus", clone.getCultivarEpithet());
		Assert.assertEquals("Group string should be equal", "Cult Group", clone.getCultivarGroupEpithet());
		Assert.assertSame("Cultivar string should even be same as String is unmutable", name1.getCultivarEpithet(), clone.getCultivarEpithet());
	}
}