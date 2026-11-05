/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.ipni;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.ext.ipni.IIpniService.DelimitedFormat;
import eu.etaxonomy.cdm.ext.ipni.IpniService.IpniRank;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 */
@Disabled //preliminary, fix tests before unignore
public class IpniServiceTest extends TermTestBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private IpniService service1;
	private static boolean internetIsAvailable = true;

	@BeforeAll
	public static void setUpClass() throws Exception {
		internetIsAvailable = true;
	}

	@BeforeEach
	public void setUp() throws Exception {
		service1 = new IpniService();
	}

// ******************************* TESTS ******************************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.ext.ipni.IpniService#IpniService(java.net.URL)}.
	 */
	@Test
	public void testIpniService() {
		assertNotNull(service1, "Service should not be null");
		assertNotNull(service1.getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL), "URL1 should not be null");
	}

	@Test
	public void testGetAuthors(){
		ICdmRepository services = null;
		IpniServiceAuthorConfigurator config = new IpniServiceAuthorConfigurator();
		config.setFormat(DelimitedFormat.EXTENDED);
		List<Person> authorList = service1.getAuthors(null, "Greuter", null, null, services, config);
		//List<Person> authorList = service1.getAuthors(null, "Greu*", null, null, null, config);
		//expected web service result: 3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo
		if(authorList == null){
            fail("No results.");
            return;
		}
		if (testInternetConnectivity(authorList)){

			assertEquals(1, authorList.size(), "There should be exactly 1 result for 'Greuter'");
			Person author = authorList.get(0);
			//full title
			assertEquals("Werner Rodolfo Greuter", author.getFullTitle(), "Full title for Greuter should be 'Werner Rodolfo Greuter'");
            //title cache
            assertEquals("Greuter, W.R.", author.getTitleCache(), "Title cache for Greuter should be 'Greuter, W.R.'");
			//alternative names
			assertEquals(1, author.getExtensions().size(), "One extension for the alternative name should exist");
			Extension alternativeName = author.getExtensions().iterator().next();
			assertEquals("Greuter, Werner Rodolfo", alternativeName.getValue(), "Alternative name should be ");
			//dates
			String year = author.getLifespan().getYear();
			assertNotNull(year, "Year should be not null");
			assertEquals("1938", year, "Year should be 1938");

			authorList = service1.getAuthors(null, "Greu*", null, null, null, config);

			//29367-1%1.1%Greuet%Claude%Greuet%A%%>Greuet, Claude
			//20000981-1%1.1%Greuning%J.V. van%Greuning%M%1993%>Greuning, J.V. van
			//3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo
			if(authorList == null){
			    fail("No results.");
			}else{
			    assertEquals(3, authorList.size(), "There should be exactly 3 result for 'Greu*'. But maybe this changes over time");
			}
	//			for (Person person : authorList){
	//			System.out.println(person.getTitleCache() + ";  " + person.getNomenclaturalTitle());
	//		}
	//
			config.setFormat(DelimitedFormat.MINIMAL);
			authorList = service1.getAuthors(null, "Greuter", null, null, services, config);
			assertEquals(1, authorList.size(), "There should be exactly 1 result for 'Greuter'");
			author = authorList.get(0);
			assertTrue(author.getExtensions().isEmpty(), "No alternative names should exist in the minimal version");
		}
	}


	@Test
	public void testGetNamesSimple(){
		ICdmRepository services = null;
		IpniServiceNamesConfigurator config = null;
		List<IBotanicalName> nameList = service1.getNamesSimple("Abies albertiana", services, config);
		//expected web service result: 3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo

		if (testInternetConnectivity(nameList)){
		    if(nameList == null){
                fail("No results.");
                return;
            }
			assertEquals(1, nameList.size(), "There should be exactly 1 result for 'Abies albertiana'");
			IBotanicalName name = nameList.get(0);
			//title cache
			assertEquals("Abies albertiana A.Murray bis", name.getTitleCache(), "Title Cache for Abies albertiana should be 'Abies albertiana'");

//			for (IBotanicalName listName : nameList){
//				System.out.println(name.getFullTitleCache());
//			}
		}
	}

	@Test
	public void testGetNamesAdvanced(){
		ICdmRepository services = null;
		IpniServiceNamesConfigurator config = IpniServiceNamesConfigurator.NewInstance();

		//http://www.uk.ipni.org/ipni/advPlantNameSearch.do?find_family=&find_genus=Abies&find_species=alba&find_infrafamily=&find_infragenus=&find_infraspecies=&find_authorAbbrev=B*&find_includePublicationAuthors=on&find_includePublicationAuthors=off&find_includeBasionymAuthors=on&find_includeBasionymAuthors=off&find_publicationTitle=&find_isAPNIRecord=on&find_isAPNIRecord=false&find_isGCIRecord=on&find_isGCIRecord=false&find_isIKRecord=on&find_isIKRecord=false&find_rankToReturn=infraspec&output_format=normal&find_sortByFamily=on&find_sortByFamily=off&query_type=by_query&back_page=plantsearch
		String family = "";
		String genus = "Abies";
		String species = "alba";
		String infraFamily = "";
		String infraGenus = "";
		String infraSpecies = "";
		String authorAbbrev = "B*";
		String publicationTitle = "";
		IpniRank rankToReturn = IpniRank.valueOf(Rank.SUBSPECIES());

		List<IBotanicalName> nameList = service1.getNamesAdvanced(family, genus, species, infraFamily, infraGenus, infraSpecies, authorAbbrev, publicationTitle, rankToReturn, config, services);
		//expected web service result: 3379-1%1.1%Greuter%Werner Rodolfo%Greuter%PS%1938-%>Greuter, Werner Rodolfo


		if (testInternetConnectivity(nameList)){
		    if(nameList == null){
                fail("No results.");
                return;
            }
			assertEquals(1, nameList.size(), "There should be exactly 1 result for 'Abies', 'alba', 'B*', Infraspecific ");
			IBotanicalName name = nameList.get(0);
			//title cache
			assertEquals("Abies alba subsp. apennina Brullo, Scelsi & Spamp.", name.getTitleCache(), "Title Cache for 'Abies', 'alba', 'ap*' should be 'Abies alba subsp. apennina Brullo, Scelsi & Spamp.'");

	//		for (IBotanicalName listName : nameList){
	//			System.out.println(name.getFullTitleCache());
	//		}
		}
	}

	@Test
	public void testPublications(){
		ICdmRepository services = null;
		IpniServicePublicationConfigurator config = null;
		List<Reference> refList = service1.getPublications("Species Plantarum, Edition 3", "Sp. Pl.", services, config);
		//20009158-1%1.2%Pinaceae%%N%Abies%%N%alba%apennina%subsp.%Brullo, Scelsi & Spamp.%%Brullo, Scelsi & Spamp.%Abies alba subsp. apennina%Vegetaz. Aspromonte%41 (2001)%2001%%%%%%Italy%tax. nov.

		if (testInternetConnectivity(refList)){
		    if (refList == null){
	            fail("The list is empty, maybe the ipni service is not available.");
	            return;
	        }
			assertEquals(1, refList.size(), "There should be exactly 1 result for 'Species Plantarum, Edition 3'");
			Reference ref = refList.get(0);
			//title cache
			//the author title may be improved in future
			assertEquals("Linnaeus, Carl, Species Plantarum, Edition 3. 1764", ref.getTitleCache(), "Title Cache should be 'Linnaeus, Carl, Species Plantarum, Edition 3'");


			refList = service1.getPublications("Flora of Macar", null, services, config);
			if (refList == null){
                fail("The list is empty, maybe the ipni service is not available.");
                return;
            }
			assertNotNull(refList, "Empty resultset should not throw exception and should not be null");

			refList = service1.getPublications("Flora Europaea [ed. 2]", null, services, config);
			if (refList == null){
			    fail("The list is empty, maybe the ipni service is not available.");
			    return;
			}
			assertEquals(1, refList.size(), "There should be exactly 1 result for 'Flora Europaea [ed. 2]'");
			ref = refList.get(0);
			assertEquals("Tutin, Thomas Gaskell", ref.getAuthorship().getTitleCache(), "");


	//		for (Reference ref : refList){
	//			System.out.println(ref.getTitleCache());
	//		}
		}

	}

	@Test
	public void testNameID(){

		InputStream content = service1.getNamesById("416415-1");
		assertNotNull(content);
	}


	@Test
	public void testGetServiceUrl() {
		assertNotNull(service1, "Service should not be null");
		assertNotNull(service1.getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL), "URL1 should not be null");
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
