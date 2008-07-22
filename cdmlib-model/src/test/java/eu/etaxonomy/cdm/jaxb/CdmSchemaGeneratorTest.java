package eu.etaxonomy.cdm.jaxb;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

public class CdmSchemaGeneratorTest {

	private CdmSchemaGenerator cdmSchemaGenerator;
	
	@Before
	public void onSetUp() throws Exception {
		cdmSchemaGenerator = new CdmSchemaGenerator();
	}
	
/* ******************** TESTS ***********************************************************/
	
	@Ignore //for uuid
	@Test
	public void testCdmSchemaGeneratorInit() {
		Assert.assertNotNull(cdmSchemaGenerator);
	}
	
	@Ignore //for uuid
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
