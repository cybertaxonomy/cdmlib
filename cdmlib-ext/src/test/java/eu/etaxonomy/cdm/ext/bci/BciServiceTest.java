/**
 * 
 */
package eu.etaxonomy.cdm.ext.bci;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.ext.ipni.IIpniService;
import eu.etaxonomy.cdm.model.occurrence.Collection;

/**
 * @author a.mueller
 *
 */
public class BciServiceTest {
	
	static String strUrl1;

	private IBciServiceWrapper service1;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		service1 = new BciServiceWrapper();
	}

// ******************************* TESTS ******************************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.bci.BciServiceWrapper#BciService(java.net.URL)}.
	 */
	@Test
	public void testBciService() {
		Assert.assertNotNull("Service should not be null", service1);
		Assert.assertNotNull("URL1 should not be null", service1.getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL));
	}

	@Test
	@Ignore // service is under refactoring 
	public void testGetCollectionsByCode(){
		ICdmRepository config = null;
		List<Collection> collectionList = service1.getCollectionsByCode("BG", config);
		//expected web service result: urn:lsid:biocol.org:col:15727	http://biocol.org/urn:lsid:biocol.org:col:15727	University of Bergen Herbarium

		Assert.assertEquals("There should be exactly 1 result for 'BG'", 1, collectionList.size());
		Collection collection = collectionList.get(0);
		//title cache
		Assert.assertEquals("Name for BG should be 'University of Bergen Herbarium'", "University of Bergen Herbarium", collection.getName());
		Assert.assertEquals("LSID should be urn:lsid:biocol.org:col:15727", "urn:lsid:biocol.org:col:15727", collection.getLsid().getLsid());
		
		collectionList = service1.getCollectionsByCode("CM", config);
		Assert.assertEquals("There should be exactly 3 result for 'CM'. If not try http://www.biocol.org/rest/lookup/code/CM", 3, collectionList.size());
		
	}
	

	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.ipni.IpniService#getServiceUrl()}.
	 */
	@Test
	public void testGetServiceUrl() {
		Assert.assertNotNull("Service should not be null", service1);
		Assert.assertNotNull("URL1 should not be null", service1.getServiceUrl(IBciServiceWrapper.LOOKUP_CODE_REST));
	}


}
