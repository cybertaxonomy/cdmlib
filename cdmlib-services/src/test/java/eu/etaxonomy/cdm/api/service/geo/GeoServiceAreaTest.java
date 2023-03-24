/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import static org.junit.Assert.fail;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 12.08.2011
 */
public class GeoServiceAreaTest {

    private static final Logger logger = LogManager.getLogger();

// ********************************** TESTS ****************************************

	@Test
	public void testAdd() {
		logger.warn("testAdd not yet implemented");
	}

	@Test
	public void testGetAreas() {
		logger.warn("testGetAreas not yet implemented");
	}

	@Test
	public void testValueOf() {
		String input = "<?xml version=\"1.0\" ?><mapService xmlns=\"http://www.etaxonomy.eu/cdm\" type=\"editMapService\"><area><layer>vmap0_as_bnd_political_boundary_a</layer><field>nam</field><value>PULAU BANGKA#SUMATERA SELATAN</value></area></mapService>";
		GeoServiceArea areas = GeoServiceArea.valueOf(input);
		try {
			Assert.assertNotNull("Result must not be null", areas);
			Assert.assertEquals("Input string must be equal to output string", input, areas.toXml());
		} catch (XMLStreamException e) {
			fail();
		}
	}

	@Test
	public void testToXml() {
		logger.warn("testToXml not yet implemented");
	}
}