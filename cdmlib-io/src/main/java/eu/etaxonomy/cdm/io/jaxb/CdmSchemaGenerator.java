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
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * This class is responsible for generating a set of XML schemas for the CDM.
 * It generates one XML schema per namespace from the JAXB annotations of the
 * model classes. This class might be used in future to generate the
 * initial version of the CDM schemas. Once the initially generated schemas 
 * have been manually customized, CdmSchemaGenerator could be used to compare
 * the customized XML schemas against the generated ones.
 *  
 * @author a.babadshanjan
 *
 */
public class CdmSchemaGenerator extends SchemaOutputResolver {
	
	private JAXBContext jaxbContext;
	private StringWriter out = new StringWriter();

	public CdmSchemaGenerator() throws SAXException, JAXBException, IOException {

		jaxbContext = JAXBContext.newInstance(new Class[] {DataSet.class});
	}

	/** 
     * Buffers one schema file per namespace.
     * Result here is schema1.xsd, ..., schema7.xsd in C:\Temp.
     * filename param is ignored.
	 * @see javax.xml.bind.SchemaOutputResolver#createOutput(java.lang.String, java.lang.String)
	 */
	@Override
	public Result createOutput(String namespaceUri, String filename) throws IOException {

		String userHome = System.getProperty("user.home");
		StreamResult res = new StreamResult(new File("C:" + File.separator + "Temp", filename));
		//StreamResult res = new StreamResult(new File(filename));
		return res;
	}
	
	/** 
     * Writes one schema file per namespace.
	 * @see javax.xml.bind.SchemaOutputResolver#createOutput(java.lang.String, java.lang.String)
	 */
	public void writeSchema() throws JAXBException, IOException, SAXException {

		jaxbContext.generateSchema(this);
	}

	/** 
	 * Not used
	 * 
     * Buffers one single generated schema.
	 * @see javax.xml.bind.SchemaOutputResolver#createOutput(java.lang.String, java.lang.String)
	 */
	public Schema createSchema() throws SAXException {
		
		Schema schema = 
		SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(
				new StreamSource( new StringReader(out.toString())));
		return schema;
		
	}
	
	// set implicit schema for validation
	// Schema implicitSchema = cdmSchemaGenerator.createSchema();

}
