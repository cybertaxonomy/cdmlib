/**
 *  This class is responsible for generating a set of XML schemas for the CDM.
 *  It generates one XML schema per namespace from the JAXB annotations of the
 *  model classes. This class might be used in future to generate the
 *  initial version of the CDM schemas. Once the initially generated schemas 
 *  have been manually customized, CdmSchemaGenerator could be used to compare
 *  the customized XML schemas against the generated ones.
 */
package eu.etaxonomy.cdm.jaxb;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.model.DataSet;

/**
 * @author a.babadshanjan
 *
 */
public class CdmSchemaGenerator extends SchemaOutputResolver {
	
	private JAXBContext jaxbContext;
	StringWriter out = new StringWriter();

	public CdmSchemaGenerator() throws SAXException, JAXBException, IOException {

		jaxbContext = JAXBContext.newInstance(new Class[] {DataSet.class});
	}

	/** 
     * Prints one single generated schema on console.
	 * @see javax.xml.bind.SchemaOutputResolver#createOutput(java.lang.String, java.lang.String)
	 */
	@Override
	public Result createOutput(String namespaceUri, String filename) throws IOException {

		StreamResult res = new StreamResult(System.out);
		res.setSystemId(filename);
		return res;
	}
	
	/** 
     * Buffers one single generated schema.
	 * @see javax.xml.bind.SchemaOutputResolver#createOutput(java.lang.String, java.lang.String)
	 */
	public Schema createSchema() throws SAXException {
		
		Schema schema = 
		SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(
				new StreamSource( new StringReader(out.toString())));
		return schema;
	}

	public void writeSchema(DataSet dataSet, Writer writer) throws JAXBException, IOException, SAXException {

		CdmSchemaGenerator out = new CdmSchemaGenerator();
		jaxbContext.generateSchema(out);
		Schema implicitSchema = out.createSchema();
	}
}
