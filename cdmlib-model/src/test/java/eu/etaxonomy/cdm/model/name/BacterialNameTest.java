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
public class BacterialNameTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BacterialNameTest.class);

	private BacterialName bacterialName1;

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
		bacterialName1 = TaxonNameBase.NewBacterialInstance(Rank.SPECIES());
	}

//****************** TESTS ******************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.BacterialName#clone()}.
	 */
	@Test
	public void testClone() {
		bacterialName1.setSubGenusAuthorship("Bacter.");
		bacterialName1.setNameApprobation("approb");
		BacterialName clone = (BacterialName)bacterialName1.clone();
		Assert.assertEquals("SubGenusAuthorship should be equal", "Bacter.", clone.getSubGenusAuthorship());
		Assert.assertEquals("Name approbation should be equal", "approb", clone.getNameApprobation());
	}

}
