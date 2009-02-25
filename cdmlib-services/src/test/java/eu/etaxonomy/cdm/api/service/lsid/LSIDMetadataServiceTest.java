package eu.etaxonomy.cdm.api.service.lsid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import com.ibm.lsid.MetadataResponse;

import eu.etaxonomy.cdm.api.service.lsid.impl.LsidRegistryImpl;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet("LSIDAuthorityServiceTest.testGetAvailableServices.xml")
public class LSIDMetadataServiceTest extends CdmIntegrationTest {

	@SpringBeanByType
	private LSIDMetadataService lsidMetadataService;

	@SpringBeanByType
	private LSIDRegistry lsidRegistry;
	
	private LSID lsid;
	
	@Before	
	public void setUp() throws Exception {
		lsid = new LSID("example.org", "taxonconcepts", "1", null);
	    ((LsidRegistryImpl)lsidRegistry).init();
	}
	
	@Test
	public void testInit()	{		
		assertNotNull("lsidMetadataService should exist",lsidMetadataService);
	} 
	
	@Test
	public void testGetMetadataWithKnownLSID() throws Exception {
		MetadataResponse metadataResponse = lsidMetadataService.getMetadata(lsid,null);
		Taxon taxon = (Taxon)metadataResponse.getValue();
		assertNotNull("getMetadata should return a MetadataResponse",metadataResponse);
		assertNotNull("the metadata response should contain an object",metadataResponse.getValue());
		assertEquals("the object should have an lsid equal to the lsid supplied",lsid.getAuthority(),taxon.getLsid().getAuthority());
		assertEquals("the object should have an lsid equal to the lsid supplied",lsid.getNamespace(),taxon.getLsid().getNamespace());
		assertEquals("the object should have an lsid equal to the lsid supplied",lsid.getObject(),taxon.getLsid().getObject());
	}

}
