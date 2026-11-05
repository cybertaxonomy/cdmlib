/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.dwca.jaxb.Extension;
import eu.etaxonomy.cdm.io.stream.CsvStream;
import eu.etaxonomy.cdm.io.stream.StreamItem;

/**
 * @author a.mueller
 * @since 17.10.2011
 */
public class DwcaZipToStreamConverterTest {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private URI uri;
	private DwcaZipToStreamConverter<?> converter;

	@BeforeEach
	public void setUp() throws Exception {
		String inputFile = "/eu/etaxonomy/cdm/io/dwca/in/DwcaZipToStreamConverterTest-input.zip";
		URL url = this.getClass().getResource(inputFile);
		uri = URI.fromUrl(url);
		assertNotNull(uri, "URI for the test file '" + inputFile + "' does not exist");
		converter = DwcaZipToStreamConverter.NewInstance(uri);
		assertNotNull(converter, "Converter must be created");
	}

//************* TEST ********************************************

	@Test
	public void testInitMetadata(){
		String coreEncoding = converter.getArchive().getCore().getEncoding();
		assertEquals("UTF-8", coreEncoding, "Encoding for core must be 'UTF-8'");
	}

	@Test
	public void testGetCoreStream(){
		try {
			CsvStream coreStream = converter.getCoreStream(null);
			assertNotNull(coreStream, "core stream should not be null");
			StreamItem next = coreStream.read();
			assertNotNull(next, "Entry should exist in core stream");
			assertEquals("1", next.get("id"), "First entry should be id1");
			assertEquals("1", next.map.get("http://rs.tdwg.org/dwc/terms/acceptedNameUsageID"), "First entries acceptedNameUsage should be ");

		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testGetVernacularStream(){
		try {
			CsvStream vernacularStream = converter.getStream(Extension.VERNACULAR_NAME,null);
			assertNotNull(vernacularStream, "Vernacular stream should not be null");
			StreamItem next = vernacularStream.read();
			assertNotNull(next, "Entry should exist in vernacular name stream");
		} catch (IOException e) {
			fail();
		}
	}

	/**
	 * This test tests the correct handling of core/extension attributes like encoding, fieldsTerminatedBy,
	 * fieldsEnclosdeBy, ignoreHeaderLines, linesTerminatedBy
	 * TODO encoding + linesTerminatedBy not yet tested
	 */
	@Test
	public void testCoreExtensionAttributes(){
		try {
			CsvStream vernacularStream = converter.getStream(Extension.VERNACULAR_NAME,null);
			StreamItem next = vernacularStream.read();
			assertNotNull(next, "Entry should exist in vernacular name stream");
			assertEquals("1", next.get("coreId"), "First entry should be coreid1");
			assertEquals("en", next.map.get("http://purl.org/dc/terms/language"), "First entries language should be 'en' ");

		} catch (IOException e) {
			fail();
		}
	}
}