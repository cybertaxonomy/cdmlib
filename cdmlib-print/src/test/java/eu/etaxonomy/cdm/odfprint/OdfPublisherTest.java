package eu.etaxonomy.cdm.odfprint;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.print.PublisherTest;

public class OdfPublisherTest {

	private final Logger logger = Logger.getLogger(OdfPublisherTest.class);

	private static OdfConfigurator configurator; 
	
	private OdfPublisher publisher;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		configurator = OdfConfigurator.getDefaultConfigurator();
		//configurator.setWebserviceUrl("http://localhost:8080/");
		configurator.setWebserviceUrl("http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/");
		configurator.setExportFile("/home/sybille/tmp/odfOutput.odf");
		// add one root node:
		configurator.addNodesToPublish(UUID.fromString("051d35ee-22f1-42d8-be07-9e9bfec5bcf7"));// Ericaceae
	}

	@Test
	public void testLocalOdfPublisher(){
		configurator.setLocal();
//		collector=configurator.getEntityCollector();
		
		publisher= new OdfPublisher(configurator);
		publisher.publish();
	}
	
	@Test
	public void testRemoteOdfPublisher(){
		configurator.setRemote();	
	}
	
}
