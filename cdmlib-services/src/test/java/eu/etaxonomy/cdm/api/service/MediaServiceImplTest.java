
package eu.etaxonomy.cdm.api.service;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class MediaServiceImplTest extends CdmIntegrationTest{
	private static final Logger logger = Logger
			.getLogger(MediaServiceImplTest.class);

	@SpringBeanByType
	private IMediaService service;
	
	@Before
	public void setUp() throws Exception {
	}
	

	@Test
	public void testGetImageMetaData() throws IOException {
		File imageFile;
		imageFile = new File("./src/test/resources/eu/etaxonomy/cdm/api/service/OregonScientificDS6639-DSC_0307-small.jpg");
		URI uri = imageFile.toURI();
		Map<String,String> metaData = service.getImageMetaData(uri, 0);
		
		
		assertEquals("The list of metaData should contain 49 entries",49, metaData.size());
		
		imageFile = new File("./src/test/resources/eu/etaxonomy/cdm/api/service/OregonScientificDS6639-DSC_0307-small.tif");
		uri = imageFile.toURI();
		metaData = service.getImageMetaData(uri, 0);
			
		assertEquals("The list of metaData should contain 15 entries",15, metaData.size());
	}
}
