/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 03.01.2011
 */
public class BacterialNameTest {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private IBacterialName bacterialName1;

	@BeforeEach
	public void setUp() throws Exception {
		bacterialName1 = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
	}

//****************** TESTS ******************************************/

	@Test
	public void testClone() {
		bacterialName1.setSubGenusAuthorship("Bacter.");
		bacterialName1.setNameApprobation("approb");
		IBacterialName clone = bacterialName1.clone();
		assertEquals("Bacter.", clone.getSubGenusAuthorship(), "SubGenusAuthorship should be equal");
		assertEquals("approb", clone.getNameApprobation(), "Name approbation should be equal");
	}
}