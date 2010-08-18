/**
 * 
 */
package eu.etaxonomy.cdm.ext.bci;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.ext.ipni.IIpniService;
import eu.etaxonomy.cdm.ext.ipni.IIpniService.DelimitedFormat;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;

/**
 * @author a.mueller
 *
 */
public class BciServiceTest {
	
	static String strUrl1;

	private IBciService service1;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		service1 = new BciService();
	}

// ******************************* TESTS ******************************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.bci.BciService#BciService(java.net.URL)}.
	 */
	@Test
	public void testBciService() {
		Assert.assertNotNull("Service should not be null", service1);
		Assert.assertNotNull("URL1 should not be null", service1.getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL));
	}

	@Test
	public void testGetCollections(){
		ICdmApplicationConfiguration config = null;
		List<Collection> collectionList = service1.getCollectionsByCode("BG", config);
		//expected web service result: urn:lsid:biocol.org:col:15727	http://biocol.org/urn:lsid:biocol.org:col:15727	University of Bergen Herbarium

		Assert.assertEquals("There should be exactly 1 result for 'BG'", 1, collectionList.size());
		Collection collection = collectionList.get(0);
		//title cache
		Assert.assertEquals("Name for BG should be 'University of Bergen Herbarium'", "University of Bergen Herbarium", collection.getName());
		Assert.assertEquals("LSID should be urn:lsid:biocol.org:col:15727", "urn:lsid:biocol.org:col:15727", collection.getLsid().getLsid());
		System.out.println(collection.getLsid().getObject());
		System.out.println(collection.getLsid().getNamespace());
		System.out.println(collection.getLsid().getRevision());
		
//		//alternative names
//		Assert.assertEquals("One extension for the alternative name should exist", 1, author.getExtensions().size());
//		Extension alternativeName = author.getExtensions().iterator().next();
//		Assert.assertEquals("Alternative name should be ", "Greuter, Werner Rodolfo", alternativeName.getValue());
//		//dates
//		String year = author.getLifespan().getYear();
//		Assert.assertNotNull("Year should be not null", year);
//		Assert.assertEquals("Year should be 1938", "1938", year);
//		
//		authorList = service1.getAuthors(null, "Greu*", null, null, null, config);
//		//29367-1%1.1%Greuet%Claude%Greuet%A%%>Greuet, Claude
//		//20000981-1%1.1%Greuning%J.V. van%Greuning%M%1993%>Greuning, J.V. van
//		//3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo
//		Assert.assertEquals("There should be exactly 3 result for 'Greu*'. But maybe this changes over time", 3, authorList.size());
//		
//		
////		for (Person person : authorList){
////			System.out.println(person.getTitleCache() + ";  " + person.getNomenclaturalTitle());
////		}
////		
//		authorList = service1.getAuthors(null, "Greuter", null, null, DelimitedFormat.MINIMAL, config);
//		Assert.assertEquals("There should be exactly 1 result for 'Greuter'", 1, authorList.size());
//		author = authorList.get(0);
//		Assert.assertTrue("No alternative names should exist in the minimal version", author.getExtensions().isEmpty());

		
	}
	

	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.ipni.IpniService#getServiceUrl()}.
	 */
	@Test
	public void testGetServiceUrl() {
		Assert.assertNotNull("Service should not be null", service1);
		Assert.assertNotNull("URL1 should not be null", service1.getServiceUrl(IBciService.LOOKUP_CODE_REST));
	}


}
