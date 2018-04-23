/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import static org.junit.Assert.fail;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author a.mueller
 \* @since 12.08.2011
 *
 */
public class GeoServiceAreaTest {
	private static final Logger logger = Logger.getLogger(GeoServiceAreaTest.class);

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
	}

// ********************************** TESTS ****************************************	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.geo.GeoServiceArea#add(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAdd() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.geo.GeoServiceArea#getAreasMap()}.
	 */
	@Test
	public void testGetAreas() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.geo.GeoServiceArea#valueOf(java.lang.String)}.
	 */
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

	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.geo.GeoServiceArea#toXml()}.
	 */
	@Test
	public void testToXml() {
		logger.warn("Not yet implemented");
	}

}
