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
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author a.mueller
 * @date 03.01.2011
 *
 */
public class CultivarPlantNameTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CultivarPlantName.class);

	private static CultivarPlantName name1;


	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		name1 = TaxonNameBase.NewCultivarInstance(Rank.SPECIES());
		name1.setGenusOrUninomial("Aus");
		name1.setSpecificEpithet("bus");
		name1.setCultivarName("cultivarus");
	}

// ******************* TESTS *************************************/

	@Test
	public void testClone(){
		CultivarPlantName clone = (CultivarPlantName)name1.clone();
		Assert.assertEquals("Cultivar string should be equal", "cultivarus", clone.getCultivarName());
//		Assert.assertNotSame("Cultivar string should be not same (but equal)", name1.getCultivarName(), clone.getCultivarName());
	}


}
