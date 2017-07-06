/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.reference;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IReferenceService;
//import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.reference.ris.in.RisReferenceImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 *
 */
public class RisReferenceImportTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByName
	private CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	private IReferenceService referenceService;


	private RisReferenceImportConfigurator configurator;
    private RisReferenceImportConfigurator configLong;

	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/reference/RisReferenceImportTest-input.ris";

        try {
            URL url = this.getClass().getResource(inputFile);
            assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

            String inputFileLong = "/eu/etaxonomy/cdm/io/reference/Acantholimon.ris";
            URL urlLong = this.getClass().getResource(inputFileLong);
            assertNotNull("URL for the test file '" + inputFileLong + "' does not exist", urlLong);

			configurator = RisReferenceImportConfigurator.NewInstance(url, null);
			configLong = RisReferenceImportConfigurator.NewInstance(urlLong, null);


		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		assertNotNull("Configurator could not be created", configurator);
	    assertNotNull("Configurator could not be created", configLong);
	    assertNotNull("nameService should not be null", referenceService);
	}

//***************************** TESTS *************************************//

	@Test
	@DataSet( value="/eu/etaxonomy/cdm/database/BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	//@Ignore
    public void testShort() {

		ImportResult result = defaultImport.invoke(configurator);
		String report = result.createReport().toString();
		Assert.assertTrue(report.length() > 0);
		System.out.println(report);

		Integer expected = 2;
		Assert.assertEquals(expected, result.getNewRecords(Reference.class));

		List<Reference> list = referenceService.list(Reference.class, null, null, null, null);
		Assert.assertEquals("There should be 3 references, the article and the journal and the source reference",
		        3, list.size());
		for (Reference ref : list){
		    if (ref.equals(configurator.getSourceReference())){
		        continue;
		    }
		    Assert.assertTrue(ref.getType() == ReferenceType.Article || ref.getType() == ReferenceType.Journal);
		    if (ref.getType() == ReferenceType.Article){
		        //title
		        Assert.assertEquals("Decorsella arborea, a second species in Decorsella (Violaceae), and Decorsella versus Rinorea",
		                ref.getTitle());
		        //author
		        TeamOrPersonBase<?> author = ref.getAuthorship();
		        Assert.assertNotNull(author);
		        Assert.assertTrue(author.isInstanceOf(Person.class));
		        Person person = CdmBase.deproxy(author, Person.class);
		        //this may change in future depending on the correct formatting strategy
		        Assert.assertEquals("Jongkind, C.C.H." ,person.getTitleCache());
		        Assert.assertEquals("Jongkind" ,person.getLastname());
		        Assert.assertEquals("Carel C. H." ,person.getFirstname());
		        //date
		        TimePeriod date = ref.getDatePublished();
		        Assert.assertEquals(Integer.valueOf(2017) ,date.getStartYear());
		        //vol
		        Assert.assertEquals("47(1)" ,ref.getVolume());
                Assert.assertEquals("43-47" ,ref.getPages());

                //doi
                //Assert.assertEquals(DOI.fromString("10.3372/wi.47.47105"),ref.getDoi());

                //Abstract
                Assert.assertEquals("Abstract: A new species of Violaceae, Decorsella arborea Jongkind, is described and illustrated. The new species differs from the only other species in the genus, D. paradoxa A. Chev., by the larger size of the plants, smaller leaves, more slender flowers, and stamen filaments that are free for a much larger part. Both species are from the Guineo-Congolian forest of tropical Africa. The differences between Decorsella and Rinorea are discussed. Confirming recent reports, some species of Rinorea can have zygomorphic flowers and some of these can be almost equal in shape to Decorsella flowers. Citation: Jongkind C. C. H. 2017: Decorsella arborea, a second species in Decorsella (Violaceae), and Decorsella versus Rinorea. ? Willdenowia 47: 43?47. doi: https://doi.org/10.3372/wi.47.47105 Version of record first published online on 13 February 2017 ahead of inclusion in April 2017 issue.",
                        ref.getReferenceAbstract());

                //TODO still missing Y1, Y2, M3, UR

		    }else if (ref.getType() == ReferenceType.Journal){
		        Assert.assertEquals("Willdenowia", ref.getTitle());
		        //or is this part of article?
		        Assert.assertEquals("Botanic Garden and Botanical Museum Berlin (BGBM)", ref.getPublisher());

		        //ISSN
                Assert.assertEquals("0511-9618" ,ref.getIssn());

		    }else{
		        Assert.fail("Only an article and a journal should exist");
		    }
		}

	}

	@Test
	//@Ignore
    public void testLongFile() {
        ImportResult result = defaultImport.invoke(configLong);
        String report = result.createReport().toString();
        System.out.println(report);

        Integer expected = 118;  //did not count yet
        Assert.assertEquals(expected, result.getNewRecords(Reference.class));

//        List<Reference> list = referenceService.list(Reference.class, null, null, null, null);
//        Assert.assertEquals("There should be 2 references, the article and the journal", 2, list.size());
//        for (Reference ref : list){
//            Assert.assertTrue(ref.getType() == ReferenceType.Article || ref.getType() == ReferenceType.Journal);
//            if (ref.getType() == ReferenceType.Article){
//                //title
//                Assert.assertEquals("Decorsella arborea, a second species in Decorsella (Violaceae), and Decorsella versus Rinorea",
//                        ref.getTitle());
//                //author
//                TeamOrPersonBase<?> author = ref.getAuthorship();
//                Assert.assertNotNull(author);
//                Assert.assertTrue(author.isInstanceOf(Person.class));
//                Person person = CdmBase.deproxy(author, Person.class);
//                //this may change in future depending on the correct formatting strategy
//                Assert.assertEquals("Carel C. H. Jongkind" ,person.getTitleCache());
//                Assert.assertEquals("Jongkind" ,person.getLastname());
//                Assert.assertEquals("Carel C. H." ,person.getFirstname());
//                //date
//                TimePeriod date = ref.getDatePublished();
//                Assert.assertEquals(Integer.valueOf(2017) ,date.getStartYear());
//                //vol
//                Assert.assertEquals("47(1)" ,ref.getVolume());
//                Assert.assertEquals("43-47" ,ref.getPages());
//
//                //doi
//                Assert.assertEquals(DOI.fromString("10.3372/wi.47.47105"),ref.getDoi());
//
//                //Abstract
//                Assert.assertEquals("Abstract: A new species of Violaceae, Decorsella arborea Jongkind, is described and illustrated. The new species differs from the only other species in the genus, D. paradoxa A. Chev., by the larger size of the plants, smaller leaves, more slender flowers, and stamen filaments that are free for a much larger part. Both species are from the Guineo-Congolian forest of tropical Africa. The differences between Decorsella and Rinorea are discussed. Confirming recent reports, some species of Rinorea can have zygomorphic flowers and some of these can be almost equal in shape to Decorsella flowers. Citation: Jongkind C. C. H. 2017: Decorsella arborea, a second species in Decorsella (Violaceae), and Decorsella versus Rinorea. ? Willdenowia 47: 43?47. doi: https://doi.org/10.3372/wi.47.47105 Version of record first published online on 13 February 2017 ahead of inclusion in April 2017 issue.",
//                        ref.getReferenceAbstract());
//
//                //TODO still missing Y1, Y2, M3, UR
//
//            }else if (ref.getType() == ReferenceType.Journal){
//                Assert.assertEquals("Willdenowia", ref.getTitle());
//                //or is this part of article?
//                Assert.assertEquals("Botanic Garden and Botanical Museum Berlin (BGBM)", ref.getPublisher());
//
//                //ISSN
//                Assert.assertEquals("0511-9618" ,ref.getIssn());
//
//            }else{
//                Assert.fail("Only an article and a journal should exist");
//            }
//        }

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
