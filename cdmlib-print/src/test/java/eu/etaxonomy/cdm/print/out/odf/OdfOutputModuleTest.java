/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.print.out.odf;

import java.io.File;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author n.hoffmann
 * @since Apr 23, 2010
 */
public class OdfOutputModuleTest {

	@SuppressWarnings("unused")
    private Document input;

	@Before
	public void setUp() throws Exception {

		InputStream documentStream = getClass().getResourceAsStream("single_input.xml");

		SAXBuilder builder = new SAXBuilder();
		input = builder.build(documentStream);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.print.out.odf.OdfOutputModule#output(org.jdom.Document, java.io.File)}.
	 */
	@Test
	public void testOutputDocumentFile() {
		OdfOutputModule outputModule = new OdfOutputModule();

		File outputFolder = new File(".");

//		outputModule.output(input, outputFolder);
	}
}
