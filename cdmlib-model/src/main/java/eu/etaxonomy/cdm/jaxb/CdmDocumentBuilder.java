package eu.etaxonomy.cdm.jaxb;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;


/*
Initializes a JaxbContext with one class (eu.etaxonomy.cdm.model.DataSet) 
Binds it to XML schemas found in /src/main/resources/schema/cdm (cdm.xsd, common.xsd, name.xsd).
There is a bit of magic with a resource resolver in eu.etaxonomy.cdm.jaxb
which allows to package the schemas into a jar file.
*/
public class CdmDocumentBuilder {
	
	private static Log log = LogFactory.getLog(CdmDocumentBuilder.class);
	
	private JAXBContext jaxbContext;
	
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
		log.debug(jaxbContext.toString());

	}
	
	public void marshal(Object object, Writer writer) throws JAXBException {
		
		Marshaller marshaller;
		marshaller = jaxbContext.createMarshaller();
		
		// For test purposes insert newlines to make the XML output readable
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		// UTF-8 encoding delivers error when unmarshalling
		//marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
		
		// validate with explicit schema
		//marshaller.setSchema(cdmSchema);
		
		marshaller.marshal(object, writer);
		
	}

	public void marshal(DataSet dataSet, Writer writer) throws JAXBException {
		
		Marshaller marshaller;
		marshaller = jaxbContext.createMarshaller();
		
		// For test purposes insert newlines to make the XML output readable
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		// UTF-8 encoding delivers error when unmarshalling
		//marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
		
		CdmMarshallerListener marshallerListener = new CdmMarshallerListener();
		marshaller.setListener(marshallerListener);
		
		// validate with explicit schema
		//marshaller.setSchema(cdmSchema);
		
		marshaller.marshal(dataSet, writer);
		
	}

	public DataSet unmarshal(DataSet dataSet, File file) throws JAXBException {
		
		Unmarshaller unmarshaller;
		unmarshaller = jaxbContext.createUnmarshaller();
		
		// DefaultValidationEventHandler implementation is part of the API and convenient for trouble-shooting.
		// It prints errors to System.out.
		//unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

		dataSet = (DataSet) unmarshaller.unmarshal(file);
		return dataSet;
		
	}

//	public void write(DataSet dataSet, Writer writer) throws JAXBException {
//		marshaller.marshal(dataSet, writer);
//	}

//  can only be used with JAXB 2.1
//	public void writeFile(DataSet dataSet, File file) throws JAXBException {
//		marshaller.marshal(dataSet, file);
//	}

}

 
