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
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 03.01.2011
 */
public class CultivarPlantNameTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private static ICultivarPlantName name1;

	@BeforeEach
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
		assertEquals("cultivarus", clone.getCultivarEpithet(), "Cultivar string should be equal");
		assertEquals("Cult Group", clone.getCultivarGroupEpithet(), "Group string should be equal");
		assertSame(name1.getCultivarEpithet(), clone.getCultivarEpithet(), "Cultivar string should even be same as String is unmutable");
	}
}