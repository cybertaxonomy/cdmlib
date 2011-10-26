// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;


import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author a.mueller
 * @date 17.10.2011
 *
 */
public class DwcaZipToStreamConverterTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaZipToStreamConverterTest.class);
	
	URI uri;
	DwcaZipToStreamConverter converter;
	
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
		String inputFile = "/eu/etaxonomy/cdm/io/dwca/in/DwcaZipToStreamConverterTest-input.zip";
		URL url = this.getClass().getResource(inputFile);
		uri = url.toURI();
		assertNotNull("URI for the test file '" + inputFile + "' does not exist", uri);
		converter = DwcaZipToStreamConverter.NewInstance(uri);
		assertNotNull("Converter must be created",converter);
	}
	
//************* TEST ********************************************

	@Test
	public void testInitMetadata(){
		String coreEncoding = converter.getArchive().getCore().getEncoding();
		Assert.assertEquals("Encoding for core must be 'UTF-8'", "UTF-8", coreEncoding);
	}
	
	@Test 
	public void testGetCoreStream(){
		try {
			CsvStream coreStream = converter.getCoreStream();
			Assert.assertNotNull("core stream should not be null", coreStream);
			Map<String, String> next = coreStream.read();
			Assert.assertNotNull("Entry should exist in core stream", next);
			Assert.assertEquals("First entry should be id1", "1", next.get("id"));
			Assert.assertEquals("First entries acceptedNameUsage should be ", "accNameUsageId1", next.get("http://rs.tdwg.org/dwc/terms/acceptedNameUsageID"));
			
		} catch (IOException e) {
			Assert.fail();
		}
		
	}
	
	
}
