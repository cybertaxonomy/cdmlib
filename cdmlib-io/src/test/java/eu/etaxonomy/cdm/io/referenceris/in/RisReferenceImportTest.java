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
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
//import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.reference.ris.in.RisReferenceImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
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

	@SpringBeanByType
    private IAgentService agentService;

	@Before
	public void setUp() {}

//***************************** TESTS *************************************//

	@Test
	@DataSet( value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	//@Ignore
    public void testShort() {
	    RisReferenceImportConfigurator configurator = getConfigurator("RisReferenceImportTest-input.ris");
		ImportResult result = defaultImport.invoke(configurator);
		String report = result.createReport().toString();
		Assert.assertTrue(report.length() > 0);
//		System.out.println(report);

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
		        Assert.assertEquals("Jongkind, C.C.H.", person.getTitleCache());
		        Assert.assertEquals("Jongkind", person.getFamilyName());
		        Assert.assertEquals("Carel C. H.", person.getGivenName());
		        //date
		        VerbatimTimePeriod date = ref.getDatePublished();
		        Assert.assertEquals(Integer.valueOf(2017), date.getStartYear());
		        //vol
		        Assert.assertEquals("47(1)", ref.getVolume());
                Assert.assertEquals("43-47", ref.getPages());

                //doi
                //Assert.assertEquals(DOI.fromString("10.3372/wi.47.47105"), ref.getDoi());

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
	public void testChapter() {

	    final RisReferenceImportConfigurator configurator = getConfigurator("Arias2012.ris");

        ImportResult result = defaultImport.invoke(configurator);
        String report = result.createReport().toString();
        Assert.assertTrue(report.contains("Reference: 2"));
        Assert.assertTrue(report.contains("Team: 1"));
        Assert.assertTrue(report.contains("Person: 5"));

        Assert.assertEquals(0, result.getErrors().size() + result.getExceptions().size() + result.getWarnings().size());

        Integer expected = 2;
        Assert.assertEquals(expected, result.getNewRecords(Reference.class));

        List<Reference> referenceList = referenceService.list(Reference.class, null, null, null, null);
        Assert.assertEquals("There should be 3 references, the book-section, the book and the source reference",
                3, referenceList.size());

        //book section
        Reference bookSection = referenceList.stream().filter(r->r.getType() == ReferenceType.BookSection).findFirst().get();
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
        Reference book = referenceList.stream().filter(r->r.getType() == ReferenceType.Book).findFirst().get();
        //... title
        Assert.assertEquals("Flora del Valle de Tehuac\u00E1n-Cuicatl\u00E1n", book.getTitle());
        Assert.assertEquals("Fasc\u00EDculo 95", book.getVolume());
        Assert.assertEquals("M\u00E9xico D. F.", book.getPlacePublished());
        Assert.assertEquals("Instituto de Biolog\u00EDa, Universidad Nacional Aut\u00F3noma de M\u00E9xico", book.getPublisher());

        //source reference
        Reference sourceRef = referenceList.stream().filter(r->r.equals(configurator.getSourceReference())).findFirst().get();
        Assert.assertNotNull(sourceRef);
        //TODO cont.

        List<Person> personList = agentService.list(Person.class, null, null, null, null);
        Assert.assertEquals("There should be 5 persons", 5, personList.size());

        List<Team> teamList = agentService.list(Team.class, null, null, null, null);
        Assert.assertEquals("There should be 1 team", 1, teamList.size());


        //test deduplication by running it again
        result = defaultImport.invoke(configurator);
        report = result.createReport().toString();
        Assert.assertTrue(report.contains("Reference: 0"));
        Assert.assertEquals(0, result.getErrors().size() + result.getExceptions().size() + result.getWarnings().size());
        referenceList = referenceService.list(Reference.class, null, null, null, null);
        Assert.assertEquals("There should still be 3 references, the book-section, the book and the source reference",
                3, referenceList.size());

        personList = agentService.list(Person.class, null, null, null, null);
        Assert.assertEquals("There should still be 5 persons", 5, personList.size());

        teamList = agentService.list(Team.class, null, null, null, null);
        Assert.assertEquals("There should still be 1 team", 1, teamList.size());

        //test deduplication by running another chapter
        RisReferenceImportConfigurator configurator2 = getConfigurator("Arias2012_2.ris");
        result = defaultImport.invoke(configurator2);
        report = result.createReport().toString();
//        Assert.assertTrue(report.contains("Reference: 0"));
        Assert.assertEquals(0, result.getErrors().size() + result.getExceptions().size() + result.getWarnings().size());
        referenceList = referenceService.list(Reference.class, null, null, null, null);
        Assert.assertEquals("There should be 5 references, 2 book-sections, the book and 2 source references",
                5, referenceList.size());

        personList = agentService.list(Person.class, null, null, null, null);
        Assert.assertEquals("There should be 6 persons now", 6, personList.size());

        teamList = agentService.list(Team.class, null, null, null, null);
        Assert.assertEquals("There should be 2 teams now", 2, teamList.size());

	}

    private RisReferenceImportConfigurator getConfigurator(String fileName) {
        String inputFile = "/eu/etaxonomy/cdm/io/reference/ris/in/" + fileName;
        URL url = this.getClass().getResource(inputFile);
        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
        try {
            RisReferenceImportConfigurator result = RisReferenceImportConfigurator.NewInstance(url, null);
            result.setDeduplicationMaxCountForFullLoad(1);
            return result;
        } catch (IOException e) {
            Assert.fail("IOException while creating configurator: " + e.getMessage());
            return null;
        }
    }

    @Test
    public void testLongFile() {

        RisReferenceImportConfigurator configurator = getConfigurator("Acantholimon.ris");
        ImportResult result = defaultImport.invoke(configurator);

        @SuppressWarnings("unused")
        String report = result.createReport().toString();
//        System.out.println(report);

//        Integer expectedWithoutDeduplication = 118;  //did not count yet
        Integer expectedDeduplicated = 104;  //did not count yet
        Assert.assertEquals(expectedDeduplicated, result.getNewRecords(Reference.class));
//        System.out.println("Person: "+ result.getNewRecords(Person.class));
//        System.out.println("Team: "+ result.getNewRecords(Team.class));

        List<Reference> refList = referenceService.list(Reference.class, null, null, null, null);
//        Assert.assertEquals("There should be 119 references (still need to count them)", 119, refList.size());
        Collections.sort(refList, (r1,r2) -> r1.getTitleCache().compareTo(r2.getTitleCache()));
        printList(refList);
        List<Person> personList = agentService.list(Person.class, null, null, null, null);
        printList(personList);
        Assert.assertEquals(99, personList.size());
        List<Team> teamList = agentService.list(Team.class, null, null, null, null);
        printList(teamList);
        Assert.assertEquals(33, teamList.size());
        List<Institution> institutionList = agentService.list(Institution.class, null, null, null, null);
        printList(institutionList);
        Assert.assertEquals(0, institutionList.size());


        Reference ref58 = refList.stream().filter(r->hasId(r, "58", false)).findFirst().get();
        Assert.assertNotNull("", ref58);
        Assert.assertEquals((Integer)2003, ref58.getDatePublished().getStartYear());

        Reference ref53 = refList.stream().filter(r->hasId(r, "53", false)).findFirst().get();
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

    private void printList(List<? extends IdentifiableEntity<?>> list) {
        if (!logger.isDebugEnabled()){
            return;
        }
        System.out.println(list.size());
        Collections.sort(list, (p1,p2) -> p1.getTitleCache().compareTo(p2.getTitleCache()));
        list.stream().forEach(r->System.out.println(r.getTitleCache()));
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