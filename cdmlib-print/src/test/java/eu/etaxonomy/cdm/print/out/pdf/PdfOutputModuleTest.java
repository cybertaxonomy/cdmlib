/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print.out.pdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import eu.etaxonomy.cdm.print.out.PublishOutputModuleBase;


/**
 * @author n.hoffmann
 * @since Jan 11, 2011
 * @version 1.0
 */
 @Ignore
public class PdfOutputModuleTest {


	/**
	 * @throws IOException
	 *
	 */
	@Test
	public void testGetXslt() throws IOException {
		PdfOutputModule outputModule = new PdfOutputModule();

		InputStream xslt = outputModule.getXsltInputStream();

		assertNotNull(xslt);
		assertTrue(xslt.available() > 0);
	}

	@Test
	public void testGetStylesheetByLocation() throws IOException, URISyntaxException{
		PdfOutputModule outputModule = new PdfOutputModule();

		URL shippedStylesheetsResource = PublishOutputModuleBase.class.getResource("/stylesheets/pdf/");
		File shippedStylesheetsDir = new File(shippedStylesheetsResource.toURI());

		List<File> stylesheets = outputModule.getStylesheetsByLocation(shippedStylesheetsDir);

		assertNotNull("There should be stylesheets", stylesheets);
		assertEquals("There should be two stylesheets", 2, stylesheets.size());
	}


}
