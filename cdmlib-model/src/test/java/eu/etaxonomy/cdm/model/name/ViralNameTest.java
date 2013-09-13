// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.*;

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
public class ViralNameTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ViralNameTest.class);
	
	private ViralName viralName1;
	
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
		viralName1 = ViralName.NewInstance(Rank.SPECIES());
	}

//****************** TESTS ******************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.ViralName#clone()}.
	 */
	@Test
	public void testClone() {
		viralName1.setAcronym("MJU455");
		ViralName clone = (ViralName)viralName1.clone();
		Assert.assertEquals("Acronym should be equal", "MJU455", clone.getAcronym());
	}

}
