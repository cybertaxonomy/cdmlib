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
import java.io.IOException;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Initializes a JaxbContext with one class (eu.etaxonomy.cdm.model.DataSet). 
 * 
 * @author a.babadshanjan
 */
//Binds it to XML schemas found in /src/main/resources/schema/cdm (cdm.xsd, common.xsd, name.xsd).
//There is a bit of magic with a resource resolver in eu.etaxonomy.cdm.io.jaxb
//which allows to package the schemas into a jar file.
public class CdmDocumentBuilder {
	
	private static final Logger logger = Logger.getLogger(CdmDocumentBuilder.class);
	
	private JAXBContext jaxbContext;
	private boolean formattedOutput = Boolean.TRUE;
	private String encoding = "UTF-8"; 
	
//	public static String CDM_NAMESPACE = "eu.etaxonomy.cdm.model";
//	public static String[] CDM_SCHEMA_FILES = { "/schema/cdm/common.xsd",
//		                                        "/schema/cdm/name.xsd",
//		                                        "/schema/cdm/cdm.xsd" };
		                                        
	public CdmDocumentBuilder() throws SAXException, JAXBException, IOException {
		
//		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//		schemaFactory.setResourceResolver(new CdmResourceResolver());
//		Source[] sources = new Source[CdmDocumentBuilder.CDM_SCHEMA_FILES.length];
//		
//		for(int i = 0; i < CdmDocumentBuilder.CDM_SCHEMA_FILES.length; i++) {
//			String schemaName = CdmDocumentBuilder.CDM_SCHEMA_FILES[i];
//			sources[i] = new StreamSource(this.getClass().getResourceAsStream(schemaName));
//		}
//		Schema cdmSchema = schemaFactory.newSchema(sources);
					
		jaxbContext = JAXBContext.newInstance(new Class[] {DataSet.class});
		if (logger.isDebugEnabled()) { logger.debug(jaxbContext.toString()); }

	}
	
	public CdmDocumentBuilder(boolean formattedOutput, String encoding) 
	throws SAXException, JAXBException, IOException {
		
		this();
		this.formattedOutput = formattedOutput;
		this.encoding = encoding;
	}
	
	public void marshal(DataSet dataSet, Writer writer) throws JAXBException {
		
		Marshaller marshaller;
		marshaller = jaxbContext.createMarshaller();
		
		// For test purposes insert newlines to make the XML output readable
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
		
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
		
		CdmMarshallerListener marshallerListener = new CdmMarshallerListener();
		marshaller.setListener(marshallerListener);
		
		// validate with explicit schema
		//marshaller.setSchema(cdmSchema);
		
		marshaller.setEventHandler(new DefaultValidationEventHandler());

		logger.info("Start marshalling");
		marshaller.marshal(dataSet, writer);
		
	}

	public DataSet unmarshal(DataSet dataSet, File file) throws JAXBException {
		
		Unmarshaller unmarshaller;
		unmarshaller = jaxbContext.createUnmarshaller();
		
		// DefaultValidationEventHandler implementation is part of the API and convenient for trouble-shooting.
		// It prints errors to System.out.
		//unmarshaller.setEventHandler(new DefaultValidationEventHandler());

		logger.info("Start unmarshalling");
		dataSet = (DataSet) unmarshaller.unmarshal(file);
		return dataSet;
		
	}

//  can only be used with JAXB 2.1
//	public void writeFile(DataSet dataSet, File file) throws JAXBException {
//		marshaller.marshal(dataSet, file);
//	}

}

 
