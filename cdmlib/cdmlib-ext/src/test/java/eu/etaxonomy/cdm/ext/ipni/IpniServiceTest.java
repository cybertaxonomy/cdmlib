/**
 * 
 */
package eu.etaxonomy.cdm.ext.ipni;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.ext.ipni.IIpniService;
import eu.etaxonomy.cdm.ext.ipni.IpniService;
import eu.etaxonomy.cdm.ext.ipni.IpniServiceAuthorConfigurator;
import eu.etaxonomy.cdm.ext.ipni.IpniServiceNamesConfigurator;
import eu.etaxonomy.cdm.ext.ipni.IpniServicePublicationConfigurator;
import eu.etaxonomy.cdm.ext.ipni.IIpniService.DelimitedFormat;
import eu.etaxonomy.cdm.ext.ipni.IpniService.IpniRank;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 *
 */

public class IpniServiceTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IpniServiceTest.class);
	
	private IpniService service1;
	private static boolean internetIsAvailable = true;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		internetIsAvailable = true;
	}
	
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		service1 = new IpniService();
	}

// ******************************* TESTS ******************************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.ipni.IpniService#IpniService(java.net.URL)}.
	 */
	@Test
	public void testIpniService() {
		Assert.assertNotNull("Service should not be null", service1);
		Assert.assertNotNull("URL1 should not be null", service1.getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL));
	}

	@Test
	public void testGetAuthors(){
		ICdmApplicationConfiguration services = null;
		IpniServiceAuthorConfigurator config = new IpniServiceAuthorConfigurator();
		config.setFormat(DelimitedFormat.EXTENDED);
		List<Person> authorList = service1.getAuthors(null, "Greuter", null, null, services, config);
		//expected web service result: 3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo

		if (testInternetConnectivity(authorList)){
			Assert.assertEquals("There should be exactly 1 result for 'Greuter'", 1, authorList.size());
			Person author = authorList.get(0);
			//title cache
			Assert.assertEquals("Title Cache for Greuter should be 'Werner Rodolfo Greuter'", "Werner Rodolfo Greuter", author.getTitleCache());
			//alternative names
			Assert.assertEquals("One extension for the alternative name should exist", 1, author.getExtensions().size());
			Extension alternativeName = author.getExtensions().iterator().next();
			Assert.assertEquals("Alternative name should be ", "Greuter, Werner Rodolfo", alternativeName.getValue());
			//dates
			String year = author.getLifespan().getYear();
			Assert.assertNotNull("Year should be not null", year);
			Assert.assertEquals("Year should be 1938", "1938", year);

			authorList = service1.getAuthors(null, "Greu*", null, null, null, config);

			//29367-1%1.1%Greuet%Claude%Greuet%A%%>Greuet, Claude
			//20000981-1%1.1%Greuning%J.V. van%Greuning%M%1993%>Greuning, J.V. van
			//3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo
			Assert.assertEquals("There should be exactly 3 result for 'Greu*'. But maybe this changes over time", 3, authorList.size());
	//			for (Person person : authorList){
	//			System.out.println(person.getTitleCache() + ";  " + person.getNomenclaturalTitle());
	//		}
	//		
			config.setFormat(DelimitedFormat.MINIMAL);
			authorList = service1.getAuthors(null, "Greuter", null, null, services, config);
			Assert.assertEquals("There should be exactly 1 result for 'Greuter'", 1, authorList.size());
			author = authorList.get(0);
			Assert.assertTrue("No alternative names should exist in the minimal version", author.getExtensions().isEmpty());
		}
	}


	@Test
	public void testGetNamesSimple(){
		ICdmApplicationConfiguration services = null;
		IpniServiceNamesConfigurator config = null;
		List<BotanicalName> nameList = service1.getNamesSimple("Abies albertiana", services, config);
		//expected web service result: 3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo

		if (testInternetConnectivity(nameList)){
			Assert.assertEquals("There should be exactly 1 result for 'Abies albertiana'", 1, nameList.size());
			BotanicalName name = nameList.get(0);
			//title cache
			Assert.assertEquals("Title Cache for Abies albertiana should be 'Abies albertiana'", "Abies albertiana A.Murr.", name.getTitleCache());

//			for (BotanicalName listName : nameList){
//				System.out.println(name.getFullTitleCache());
//			}
		}

	}
	
	@Test
	public void testGetNamesAdvanced(){
		ICdmApplicationConfiguration services = null;
		IpniServiceNamesConfigurator config = new IpniServiceNamesConfigurator();
		
		//http://www.uk.ipni.org/ipni/advPlantNameSearch.do?find_family=&find_genus=Abies&find_species=alba&find_infrafamily=&find_infragenus=&find_infraspecies=&find_authorAbbrev=B*&find_includePublicationAuthors=on&find_includePublicationAuthors=off&find_includeBasionymAuthors=on&find_includeBasionymAuthors=off&find_publicationTitle=&find_isAPNIRecord=on&find_isAPNIRecord=false&find_isGCIRecord=on&find_isGCIRecord=false&find_isIKRecord=on&find_isIKRecord=false&find_rankToReturn=infraspec&output_format=normal&find_sortByFamily=on&find_sortByFamily=off&query_type=by_query&back_page=plantsearch
		String family = "";
		String genus = "Abies";
		String species = "alba";
		String infraFamily = "";
		String infraGenus = "";
		String infraSpecies = "";
		String authorAbbrev = "B*";
		Boolean includePublicationAuthors = null;
		Boolean includeBasionymAuthors = null;
		String publicationTitle = "";
		Boolean isAPNIRecord = null;
		Boolean isGCIRecord = null;
		Boolean isIKRecord = null;
		IpniRank rankToReturn = IpniRank.valueOf(Rank.SUBSPECIES());
		Boolean sortByFamily = null;
		
		List<BotanicalName> nameList = service1.getNamesAdvanced(family, genus, species, infraFamily, infraGenus, infraSpecies, authorAbbrev, includePublicationAuthors, includeBasionymAuthors, publicationTitle, isAPNIRecord, isGCIRecord, isIKRecord, rankToReturn, sortByFamily, config, services);
		//expected web service result: 3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo

		
		if (testInternetConnectivity(nameList)){

			Assert.assertEquals("There should be exactly 1 result for 'Abies', 'alba', 'B*', Infraspecific ", 1, nameList.size());
			BotanicalName name = nameList.get(0);
			//title cache
			Assert.assertEquals("Title Cache for 'Abies', 'alba', 'ap*' should be 'Abies alba subsp. apennina Brullo, Scelsi & Spamp.'", "Abies alba subsp. apennina Brullo, Scelsi & Spamp.", name.getTitleCache());
	
	//		for (BotanicalName listName : nameList){
	//			System.out.println(name.getFullTitleCache());
	//		}
		}
	}

	@Test
	public void testPublications(){
		ICdmApplicationConfiguration services = null;
		IpniServicePublicationConfigurator config = null;
		List<Reference> refList = service1.getPublications("Species Plantarum, Edition 3", "Sp. Pl.", services, config);
		//20009158-1%1.2%Pinaceae%%N%Abies%%N%alba%apennina%subsp.%Brullo, Scelsi & Spamp.%%Brullo, Scelsi & Spamp.%Abies alba subsp. apennina%Vegetaz. Aspromonte%41 (2001)%2001%%%%%%Italy%tax. nov.

		if (testInternetConnectivity(refList)){

			Assert.assertEquals("There should be exactly 1 result for 'Species Plantarum, Edition 3'", 1, refList.size());
			Reference ref = refList.get(0);
			//title cache
			//the author title may be improved in future
			Assert.assertEquals("Title Cache should be 'Linnaeus, Carl, Species Plantarum, Edition 3'", "Linnaeus, Carl, Species Plantarum, Edition 3. 1764", ref.getTitleCache());
	
			
			refList = service1.getPublications("Flora of Macar", null, services, config);
			Assert.assertNotNull("Empty resultset should not throw exception and should not be null", refList);
				
			refList = service1.getPublications("Flora Europaea [ed. 2]", null, services, config);
			Assert.assertEquals("There should be exactly 1 result for 'Flora Europaea [ed. 2]'", 1, refList.size());
			ref = refList.get(0);
			Assert.assertEquals("", "Tutin, Thomas Gaskell", ref.getAuthorTeam().getTitleCache());
			
			
	//		for (Reference ref : refList){
	//			System.out.println(ref.getTitleCache());
	//		}
		}

	}	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.ipni.IpniService#getServiceUrl()}.
	 */
	@Test
	public void testGetServiceUrl() {
		Assert.assertNotNull("Service should not be null", service1);
		Assert.assertNotNull("URL1 should not be null", service1.getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL));
	}

	
	private boolean testInternetConnectivity(List<?> list) {
		if (list == null || list.isEmpty()){
			boolean result = internetIsAvailable && UriUtils.isInternetAvailable(null);
			internetIsAvailable = result;
			return result;
			
		}
		return true;
	}

}
