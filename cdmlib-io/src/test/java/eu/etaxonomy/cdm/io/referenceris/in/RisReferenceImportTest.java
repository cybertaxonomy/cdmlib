/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.referenceris.in;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
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
		String inputFile = "/eu/etaxonomy/cdm/io/reference/ris/in/RisReferenceImportTest-input.ris";

        try {
            URL url = this.getClass().getResource(inputFile);
            assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

            String inputFileLong = "/eu/etaxonomy/cdm/io/reference/ris/in/Acantholimon.ris";
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
	@DataSet( value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
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
		        Assert.assertEquals("Jongkind" ,person.getFamilyName());
		        Assert.assertEquals("Carel C. H." ,person.getGivenName());
		        //date
		        VerbatimTimePeriod date = ref.getDatePublished();
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
	public void testChapter() throws IOException{
        String inputFileLong = "/eu/etaxonomy/cdm/io/reference/ris/in/Arias2012.ris";
        URL urlLong = this.getClass().getResource(inputFileLong);
        configurator = RisReferenceImportConfigurator.NewInstance(urlLong, null);

        ImportResult result = defaultImport.invoke(configurator);
        String report = result.createReport().toString();
        Assert.assertTrue(report.contains("Reference: 2"));
        Assert.assertEquals(0, result.getErrors().size() + result.getExceptions().size() + result.getWarnings().size());

        Integer expected = 2;
        Assert.assertEquals(expected, result.getNewRecords(Reference.class));

        List<Reference> list = referenceService.list(Reference.class, null, null, null, null);
        Assert.assertEquals("There should be 3 references, the book-section, the book and the source reference",
                3, list.size());

        //book section
        Reference bookSection = list.stream().filter(r->r.getType() == ReferenceType.BookSection).findFirst().get();
        //... title
        Assert.assertEquals("Cactaceae", bookSection.getTitle());
        //... author
        TeamOrPersonBase<?> author = bookSection.getAuthorship();
        Assert.assertNotNull(author);
        Team team = CdmBase.deproxy(author, Team.class);
        Assert.assertEquals(4, team.getTeamMembers().size());
        Person firstPerson = CdmBase.deproxy(team.getTeamMembers().get(0));
        //this may change in future depending on the correct formatting strategy
        Assert.assertEquals("Arias, S." , firstPerson.getTitleCache());
        Assert.assertEquals("Arias" , firstPerson.getFamilyName());
        Assert.assertNull(firstPerson.getGivenName());
        Assert.assertEquals("S." , firstPerson.getInitials());
        Person secondPerson = CdmBase.deproxy(team.getTeamMembers().get(1));
        Assert.assertEquals("Gama-L\u00F3pez, S." , secondPerson.getTitleCache());
        VerbatimTimePeriod date = bookSection.getDatePublished();
        Assert.assertEquals(Integer.valueOf(2012), date.getStartYear());
        //TODO correct?
        Assert.assertEquals("1-235", bookSection.getPages());

        //book
        Reference book = list.stream().filter(r->r.getType() == ReferenceType.Book).findFirst().get();
        //... title
        Assert.assertEquals("Flora del Valle de Tehuac\u00E1n-Cuicatl\u00E1n", book.getTitle());
        Assert.assertEquals("Fasc\u00EDculo 95", book.getVolume());
        Assert.assertEquals("M\u00E9xico D. F.", book.getPlacePublished());
        Assert.assertEquals("Instituto de Biolog\u00EDa, Universidad Nacional Aut\u00F3noma de M\u00E9xico", book.getPublisher());

        //source reference
        Reference sourceRef = list.stream().filter(r->r.equals(configurator.getSourceReference())).findFirst().get();
        Assert.assertNotNull(sourceRef);
        //TODO cont.

	}

	@Test
	//@Ignore
    public void testLongFile() {
        ImportResult result = defaultImport.invoke(configLong);
        String report = result.createReport().toString();
        System.out.println(report);

        Integer expected = 118;  //did not count yet
        Assert.assertEquals(expected, result.getNewRecords(Reference.class));

        List<Reference> list = referenceService.list(Reference.class, null, null, null, null);
//        Assert.assertEquals("There should be 119 references (still need to count them)", 119, list.size());
        //TODO deduplication

        Reference ref58 = list.stream().filter(r->hasId(r, "58", false)).findFirst().get();
        Assert.assertNotNull("", ref58);
        Assert.assertEquals((Integer)2003, ref58.getDatePublished().getStartYear());

        Reference ref53 = list.stream().filter(r->hasId(r, "53", false)).findFirst().get();
        Assert.assertNotNull("", ref53);
        Assert.assertEquals(ReferenceType.BookSection, ref53.getType());
        Assert.assertNotNull("", ref53.getInReference());
        Assert.assertEquals("Tehran", ref53.getInReference().getPlacePublished());


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
//                Assert.assertEquals("Jongkind" ,person.getFamilyName());
//                Assert.assertEquals("Carel C. H." ,person.getGivenName());
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

    private boolean hasId(Reference ref, String idStr, boolean getInRef) {
        if (ref.getSources().size() != 1){
            return false;
        }else{
            String idInSource = ref.getSources().iterator().next().getIdInSource();
            return idStr.equals(idInSource) &&
                    (getInRef && ref.getInReference()== null
                      || !getInRef && ref.getInReference()!= null );
        }
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
