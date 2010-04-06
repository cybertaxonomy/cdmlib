/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import eu.etaxonomy.cdm.jaxb.CdmNamespacePrefixMapper;
import eu.etaxonomy.cdm.jaxb.FormattedText;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextElement;

/**
 * Initializes a JaxbContext with one class (eu.etaxonomy.cdm.model.DataSet). 
 * 
 * @author a.babadshanjan, ben.clark
 */
//Binds it to XML schemas found in /src/main/resources/schema/cdm (cdm.xsd, common.xsd, name.xsd).
//There is a bit of magic with a resource resolver in eu.etaxonomy.cdm.io.jaxb
//which allows to package the schemas into a jar file.
public class CdmDocumentBuilder {
	
	private static final Logger logger = Logger.getLogger(CdmDocumentBuilder.class);
	
	private JAXBContext jaxbContext;
	private boolean formattedOutput = Boolean.TRUE;
	private String encoding = "UTF-8";
	private Schema schema;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;
    private XMLReader xmlReader;
	
	public static String CDM_NAMESPACE = "eu.etaxonomy.cdm.model";
	public static String[] CDM_SCHEMA_FILES = { "/schema/cdm/agent.xsd",
		                                        "/schema/cdm/cdm.xsd",
		                                        "/schema/cdm/common.xsd",
		                                        "/schema/cdm/description.xsd",
		                                        "/schema/cdm/location.xsd",
		                                        "/schema/cdm/media.xsd",
		                                        "/schema/cdm/molecular.xsd",
		                                        "/schema/cdm/name.xsd",
		                                        "/schema/cdm/occurrence.xsd",
		                                        "/schema/cdm/reference.xsd",
		                                        "/schema/cdm/taxon.xsd"};
	public static Class[] CONTEXT_CLASSES = {DataSet.class,FormattedText.class,MultilanguageTextElement.class};
	
	protected String[] getSchemaFiles() {
		return CDM_SCHEMA_FILES;
	}
	
	protected Class[] getContextClasses() {
		return CONTEXT_CLASSES;
	}
	
	protected void constructSchema() throws IOException, SAXException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setResourceResolver(new CdmResourceResolver());
		String[] schemaFiles = getSchemaFiles();
		
		Source[] sources = new Source[schemaFiles.length];
		
		for(int i = 0; i < schemaFiles.length; i++) {
			String schemaName = schemaFiles[i];
			sources[i] = new StreamSource(this.getClass().getResourceAsStream(schemaName));
		}

		schema = schemaFactory.newSchema(sources);
	}
	
	protected void constructUnmarshaller() throws ParserConfigurationException, SAXException, JAXBException {
		unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
	    
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	
        saxParserFactory.setNamespaceAware(true);
        saxParserFactory.setXIncludeAware(true);
        saxParserFactory.setValidating(true);
        
        SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
    			              "http://www.w3.org/2001/XMLSchema");
	    xmlReader = saxParser.getXMLReader();
	    xmlReader.setEntityResolver(new CatalogResolver());
	    xmlReader.setErrorHandler(new DefaultErrorHandler());
        unmarshaller.setEventHandler(new WarningTolerantValidationEventHandler());
	}
		                                        
	public CdmDocumentBuilder() throws SAXException, JAXBException, IOException, ParserConfigurationException {
		constructSchema();
		
	    jaxbContext = JAXBContext.newInstance(getContextClasses());
		
        constructUnmarshaller();
        constructMarshaller();
	}
	
	protected void constructMarshaller() throws JAXBException {
		marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CdmNamespacePrefixMapper() );
		marshaller.setSchema(schema);
        
		// For test purposes insert newlines to make the XML output readable
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
//		marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,"http://etaxonomy.eu/cdm/model/1.0 schema/cdm/cdm.xsd");
		//marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,"http://etaxonomy.eu/cdm/model/1.0 cdm.xsd");
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
		
		CdmMarshallerListener marshallerListener = new CdmMarshallerListener();
		marshaller.setListener(marshallerListener);		
		marshaller.setEventHandler(new WarningTolerantValidationEventHandler());
		System.out.println(marshaller.toString());
		
	}

	public CdmDocumentBuilder(boolean formattedOutput, String encoding) throws SAXException, JAXBException, IOException, ParserConfigurationException {
		this.formattedOutput = formattedOutput;
		this.encoding = encoding;
        constructSchema();
		
	    jaxbContext = JAXBContext.newInstance(getContextClasses());
		
        constructUnmarshaller();
        constructMarshaller();
	}
	
	public void marshal(DataSet dataSet, Writer writer) throws JAXBException {
		
		logger.info("Start marshalling");
		marshaller.marshal(dataSet, writer);
		
	}
	
	public <T> T unmarshal(Class<T> clazz,Reader reader) throws JAXBException {
		InputSource input = new InputSource(reader);
		SAXSource saxSource = new SAXSource( xmlReader, input);
		logger.info("Start unmarshalling");
		T t = (T) unmarshaller.unmarshal(saxSource);
		return t;
	} 
	
	public <T> T unmarshal(Class<T> clazz,Reader reader, String systemId) throws JAXBException {
		InputSource input = new InputSource(reader);
		input.setSystemId(systemId);
		SAXSource saxSource = new SAXSource( xmlReader, input);
		logger.info("Start unmarshalling");
		T t = (T) unmarshaller.unmarshal(saxSource);
		return t;
	} 

	public <T> T unmarshal(Class<T> clazz, File file) throws JAXBException, UnsupportedEncodingException, FileNotFoundException {

		InputSource input = new InputSource(new InputStreamReader(new FileInputStream(file),encoding));
		input.setSystemId(file.toURI().toString());
		SAXSource saxSource = new SAXSource( xmlReader, input);
		logger.info("Start unmarshalling");
		T t = (T) unmarshaller.unmarshal(saxSource);
		return t;
		
	}

	public void marshal(DataSet dataSet, SAXResult result) throws JAXBException {
		logger.info("Start marshalling");
		marshaller.marshal(dataSet, result);
		
	}

//	public void marshal(DataSet dataSet, File file) throws JAXBException {
//		marshaller.marshal(dataSet, file);
//	}

}

 
