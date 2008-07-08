package eu.etaxonomy.cdm.jaxb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.model.DataSet;
import eu.etaxonomy.cdm.model.DataSetTest;

public class CdmSchemaGeneratorTest {

	private CdmSchemaGenerator cdmSchemaGenerator;
	
	@Before
	public void onSetUp() throws Exception {
		cdmSchemaGenerator = new CdmSchemaGenerator();
	}
	
/* ******************** TESTS ***********************************************************/
	
	@Test
	public void testCdmSchemaGeneratorInit() {
		Assert.assertNotNull(cdmSchemaGenerator);
	}
	
	@Test
	// buffers the schema files
	public void testCreateOutput() throws JAXBException, IOException, SAXException {

		cdmSchemaGenerator.createOutput("http://etaxonomy.eu/cdm/model/1.0", "mySchema.xsd");
	}

    @Ignore
    // Gives an IllegalArgumentException: argument contains null.
    // Problem is in schema7.xsd.
	@Test
	// writes the schema files
	public void testWriteSchema() throws JAXBException, IOException, SAXException {
		
		cdmSchemaGenerator.writeSchema();
	}
}
