package eu.etaxonomy.cdm.jaxb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.model.DataSet;

/*
Initializes a JaxbContext with one class (eu.etaxonomy.cdm.model.DataSet) 
Binds it to XML schemas found in /src/main/resources/schema/cdm (cdm.xsd, common.xsd, name.xsd).
There is a bit of magic with a resource resolver in eu.etaxonomy.cdm.jaxb
which allows to package the schemas into a jar file.
*/
public class CdmDocumentBuilder {
	private static Log log = LogFactory.getLog(CdmDocumentBuilder.class);
	
	private Marshaller marshaller;
	
	public static String[] CDM_SCHEMA_FILES = { "/schema/cdm/common.xsd",
		                                        "/schema/cdm/name.xsd",
		                                        "/schema/cdm/cdm.xsd" };
		                                        
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
					
		JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {DataSet.class});
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		//marshaller.setSchema(cdmSchema);
	}

	public void write(DataSet dataSet, Writer writer) throws JAXBException {
		marshaller.marshal(dataSet, writer);
	}

//	public void writeFile(DataSet dataSet, File file) throws JAXBException {
//		marshaller.marshal(dataSet, file);
//	}

}

 
