package eu.etaxonomy.cdm.jaxb;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.DataSet;
import eu.etaxonomy.cdm.model.DataSetTest;

public class CdmDocumentBuilderTest {

	private DataSet dataSet;
	private DataSetTest dataSetTest;
	private CdmDocumentBuilder cdmDocumentBuilder;
	private String userHome = System.getProperty("user.home");
	private String marshOut = new String( userHome + File.separator + "cdm_marshalled.xml");
	private String unmarshOut = new String( userHome + File.separator + "cdm_roundtrip.xml");
	
	@Before
	public void onSetUp() throws Exception {
		cdmDocumentBuilder = new CdmDocumentBuilder();
		dataSetTest = new DataSetTest();
		dataSetTest.onSetUp();
		dataSet = dataSetTest.buildDataSet(false);
	}
	
/* ******************** TESTS ***********************************************************/
	
	@Test
	public void testCdmDocumentBuilderInit() {
		Assert.assertNotNull(cdmDocumentBuilder);
	}
	
	@Ignore
	@Test
	public void marshalDataSet2Stdout() throws Exception {
		Writer writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
		cdmDocumentBuilder.marshal(dataSet, writer);
	}
	
//  can only be used with JAXB 2.1
//  @Test
//	public void marshalDataSet2File() throws Exception {
//		cdmDocumentBuilder.writeFile(dataSet, new File(filename));
//	}


//	@Test
//	public void marshalDataSet2FileWriter() throws Exception {
//		cdmDocumentBuilder.write(dataSet, new FileWriter(filename));
//	}

	@Test
	public void testMarshal() throws Exception {
		cdmDocumentBuilder.marshal(dataSet, new FileWriter(marshOut));
	}
	
	@Test
	public void testUnmarshal() throws Exception {
		DataSet newDataSet = new DataSet();
		newDataSet = cdmDocumentBuilder.unmarshal(newDataSet, new File(marshOut));
		cdmDocumentBuilder.marshal(newDataSet, new FileWriter(unmarshOut));
	}
	
}
