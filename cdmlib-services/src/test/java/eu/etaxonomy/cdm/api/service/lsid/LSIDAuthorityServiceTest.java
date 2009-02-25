package eu.etaxonomy.cdm.api.service.lsid;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import com.ibm.lsid.ExpiringResponse;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.impl.LsidAuthorityServiceImpl;
import eu.etaxonomy.cdm.api.service.lsid.impl.LsidRegistryImpl;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet("LSIDAuthorityServiceTest.testGetAvailableServices.xml")
public class LSIDAuthorityServiceTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	private LSIDAuthorityService lsidAuthorityService;
	
	@SpringBeanByType
	private LSIDRegistry lsidRegistry;
	
	private LSID knownLsid;
	private LSID unknownLsid;
	
	@Before	
	public void setUp() throws Exception {
		unknownLsid = new LSID("fred.org", "dagg", "1", null);
		knownLsid = new LSID("example.org", "taxonconcepts", "1", null);
		XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	    XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	    XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
	    XMLUnit.setIgnoreWhitespace(true);
	    ((LsidRegistryImpl)lsidRegistry).init();
	    ((LsidAuthorityServiceImpl)lsidAuthorityService).setLsidDomain("example.org");
	    ((LsidAuthorityServiceImpl)lsidAuthorityService).setLsidPort(80);
	}

	@Test
	public void testInit()	{		
		assertNotNull("lsidAuthorityService should exist",lsidAuthorityService);
	} 
	
	@Test
	public void testGetAuthorityWSDL() throws Exception {	
		ExpiringResponse expiringResponse = lsidAuthorityService.getAuthorityWSDL();	
		String resource = "/eu/etaxonomy/cdm/api/service/lsid/LSIDAuthorityServiceTest.testGetAuthorityWSDL-result.wsdl";
		String result = transformSourceToString((Source) expiringResponse.getValue());
		
		assertXMLEqual("getAuthorityWSDL should return an xml source equal to the test resource",new InputStreamReader(this.getClass().getResourceAsStream(resource)),new StringReader(result));
	}
	
	/**
	 * Unfortunately, the ordering of the services switches round between linux and windows. 
	 * This is why we ignore this test failure.
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testGetAvailableServicesWithKnownLSID() throws Exception {		
		ExpiringResponse expiringResponse = lsidAuthorityService.getAvailableServices(knownLsid);
		
		String resource = "/eu/etaxonomy/cdm/api/service/lsid/LSIDAuthorityServiceTest.testGetAvailableServicesWithKnownLSID-result.wsdl";
		String result = transformSourceToString((Source) expiringResponse.getValue());
		assertXMLEqual("getAvailableServices should return an xml source equal to the test resource",new InputStreamReader(this.getClass().getResourceAsStream(resource)),new StringReader(result));
	}
	
	@Test(expected= LSIDServerException.class)
	public void testGetAvailableServicesWithUnknownLSID() throws Exception {
		lsidAuthorityService.getAvailableServices(unknownLsid);
	}
}
