package eu.etaxonomy.cdm.jaxb;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.DataSet;

public class CdmDocumentBuilderTest {

	private DataSet dataSet;
	private CdmDocumentBuilder cdmDocumentBuilder;
	
	@Before
	public void onSetUp() throws Exception {
		cdmDocumentBuilder = new CdmDocumentBuilder();
		dataSet = new DataSet();
	}
	
	@Test
	public void testCdmDocumentBuilderInit() {
		Assert.assertNotNull(cdmDocumentBuilder);
	}
	
//	@Test
//	public void marshalDataSet2Stdout() throws Exception {
//		Writer writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
//		cdmDocumentBuilder.write(dataSet, writer);
//	}
	
//	@Test
//	public void marshalDataSet2File() throws Exception {
//		File file = new File(System.getProperty("user.home") + File.separator + "cdm.xml");
//		cdmDocumentBuilder.writeFile(dataSet, file);
//	}
}
