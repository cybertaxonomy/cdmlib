/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;

public class SimpleMarshalTest {
	private String documentText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	"<DataSet xmlns=\"http://etaxonomy.eu/cdm/model/1.0\">\n" +
	"</DataSet>";

	private CdmDocumentBuilder cdmDocumentBuilder;

	@BeforeEach
	public void setUp() {

		 try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * test the parsing of a simple document
     * @throws JAXBException
     * @throws SAXException
     * @throws JAXBException
     * @throws IOException
     */
	@Test
    public void testUnmarshalEmptyDocument() throws JAXBException
    {
		Reader reader = new StringReader(documentText);
		DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, reader);
    }

    /**
     * Test the creation of the document builder
     * @throws ParserConfigurationException
     * @throws IOException
     */
	@Test
    public void testCreation() throws JAXBException, SAXException, IOException, ParserConfigurationException
    {
    	assertNotNull(cdmDocumentBuilder, "cdmDocumentBuilder should be initialized without any problems");
    }

	/**
	 * Check that we can marshal an empty document and that the result validates against the CDM schemas
	 * (JAXP {@code javax.xml.validation}, formerly xmlunit {@code Validator}).
	 */
	@Test
	public void testMarshalEmptyDocument() throws Exception {
		DataSet dataSet = new DataSet();
		StringWriter writer = new StringWriter();
		cdmDocumentBuilder.marshal(dataSet, writer);
		String xml = writer.toString();
		assertNotNull(xml, "marshalled XML should not be null");

		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setResourceResolver(new CdmResourceResolver());

		StreamSource[] schemaSources = new StreamSource[CdmDocumentBuilder.CDM_SCHEMA_FILES.length];
		for (int i = 0; i < CdmDocumentBuilder.CDM_SCHEMA_FILES.length; i++) {
			String schemaName = CdmDocumentBuilder.CDM_SCHEMA_FILES[i];
			schemaSources[i] = new StreamSource(this.getClass().getResourceAsStream(schemaName));
		}
		Schema schema = schemaFactory.newSchema(schemaSources);
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(new StringReader(xml)));
	}
}
