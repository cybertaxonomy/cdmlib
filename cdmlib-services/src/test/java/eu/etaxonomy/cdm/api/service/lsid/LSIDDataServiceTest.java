package eu.etaxonomy.cdm.api.service.lsid;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.impl.LsidRegistryImpl;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet("LSIDAuthorityServiceTest.testGetAvailableServices.xml")
public class LSIDDataServiceTest extends CdmIntegrationTest {
	@SpringBeanByType
	private LSIDDataService lsidDataService;
	
	@SpringBeanByType
	private LSIDRegistry lsidRegistry;
	
	private LSID knownLsid;
	private LSID unknownLsid;
	
	@Before	
	public void setUp() throws Exception {
		unknownLsid = new LSID("fred.org", "dagg", "1", null);
		knownLsid = new LSID("example.org", "taxonconcepts", "1", null);
	    ((LsidRegistryImpl)lsidRegistry).init();
	}

	@Test
	public void testInit()	{		
		assertNotNull("lsidDataService should exist",lsidDataService);
	} 
	
	@Test
	public void testGetDataWithKnownLSID() throws Exception {
		Object object = lsidDataService.getData(knownLsid);
		assertNull("getData should return a null response",object);
	}
	
	@Test(expected = LSIDServerException.class)
	public void testGetDataUnknownLSID() throws Exception {
		lsidDataService.getData(unknownLsid);	
	}
}
