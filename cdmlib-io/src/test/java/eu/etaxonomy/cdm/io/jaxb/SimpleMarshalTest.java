/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.custommonkey.xmlunit.jaxp13.Validator;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SimpleMarshalTest {
	private String documentText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	"<DataSet xmlns=\"http://etaxonomy.eu/cdm/model/1.0\">\n" +
	"</DataSet>";
	
	private CdmDocumentBuilder cdmDocumentBuilder;
    
	@Before
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
    	assertNotNull("cdmDocumentBuilder should be initialized without any problems",cdmDocumentBuilder);
    }
	
	/**
	 * Check that we can marshal an empty document
	 * @throws JAXBException
	 * @throws IOException 
	 */
	@Test
	public void testMarshalEmptyDocument() throws JAXBException, IOException {
		DataSet dataSet = new DataSet();
		StringWriter writer = new StringWriter();
		cdmDocumentBuilder.marshal(dataSet, writer);
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    schemaFactory.setResourceResolver(new CdmResourceResolver());
	    
		Validator validator = new Validator(schemaFactory);
		for(String schemaName : CdmDocumentBuilder.CDM_SCHEMA_FILES) {
		    validator.addSchemaSource(new StreamSource(this.getClass().getResourceAsStream(schemaName)));
	    }
		StreamSource streamSource = new StreamSource(new StringReader(writer.toString()));
		//assertTrue("CdmDocumentBuilder.marshal should produce valid xml",validator.isInstanceValid(streamSource));
	}
}
