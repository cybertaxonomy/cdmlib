/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.print;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.TransformerException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.XSLTransformException;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.print.out.odf.OdfOutputModule;
import eu.etaxonomy.cdm.print.out.odf.OdfOutputModuleTest;

/**
 * @author n.hoffmann
 * @since Apr 20, 2010
 */
public class TransformatorTest {

    private Document input;

	@Before
	public void setUp() throws Exception {

		InputStream documentStream = OdfOutputModuleTest.class.getResourceAsStream("single_input.xml");

		 SAXBuilder builder = new SAXBuilder();
		 input = builder.build(documentStream);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.print.Transformator#Transformator(java.io.InputStream)}.
	 * @throws XSLTransformException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
    @Test
	public void testTransformator() throws XSLTransformException, IOException  {
		URL stylesheet = TransformatorTest.class.getResource(OdfOutputModule.STYLESHEET_RESOURCE_DEFAULT);
		new Transformator(stylesheet);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.print.Transformator#transform(org.jdom.Document)}.
	 * @throws JDOMException
	 * @throws TransformerException
	 * @throws IOException
	 */
	@Test
	public void testTransform() throws IOException, TransformerException, JDOMException {
		URL stylesheet = TransformatorTest.class.getResource(OdfOutputModule.STYLESHEET_RESOURCE_DEFAULT);
		Transformator transformator = new Transformator(stylesheet);

		Document output = transformator.transform(input);

//		XMLOutputter outputter = new XMLOutputter();
//		outputter.output(output, System.out);

		assertNotNull("Output document should not be null", output);
	}
}
