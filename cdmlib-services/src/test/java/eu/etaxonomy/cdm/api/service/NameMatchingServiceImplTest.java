/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.NameMatchingResult;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.SingleNameMatchingResult;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * Testclass for {@link NameMatchingServiceImpl}
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/10178
 * @author andreabee90
 * @since 11.07.2023
 */
public class NameMatchingServiceImplTest extends CdmTransactionalIntegrationTest {

	private static final UUID UUID_NAME_NECTONDRA = UUID.fromString("6c4464cc-fc2c-4907-9125-97715a798e0d");
	private static final UUID UUID_NAME_NECTANDRA = UUID.fromString("0e16e411-e472-48ab-8b32-da9d3968092c");
	private static final UUID UUID_NAME_NEXTANDRA = UUID.fromString("afdcdff3-8e8f-4296-aed2-2ad39c1b6bee");
	private static final UUID UUID_NAME_MAGNIFOLIA = UUID.fromString("10989f63-c52f-4704-9574-2cc0676afe01");
	private static final UUID UUID_NAME_SURINAMENSIS1 = UUID.fromString("b184664e-798b-4b50-8807-2163a4de796c");
	private static final UUID UUID_NAME_SURINAMENSIS2 = UUID.fromString("73d9c6ef-4818-4b1c-b5c4-40b1ef5ed250");
	private static final UUID UUID_NAME_NIGRA = UUID.fromString("cae90b7a-5deb-4838-940f-f85bb685286e");
	private static final UUID UUID_NAME_NIGRITA = UUID.fromString("8ad82243-b902-4eb6-990d-59774454b6e7");
	private static final UUID UUID_NAME_NECTRINA = UUID.fromString("08ab2653-a4da-4c9b-8330-1bf4268cab88");
	private static final UUID UUID_NAME_NEXXXINA = UUID.fromString("c955a8ab-8501-421a-bfa3-5748237e8942");
	private static final UUID UUID_NAME_LAUREL = UUID.fromString("25296c78-f62b-4dfa-9cd1-813bc9d1d777");
	private static final UUID UUID_NAME_LAURELI = UUID.fromString("a598ab3f-b33b-4b4b-b237-d616fcb6b5b1");

    @SpringBeanByType
	private INameMatchingService nameMatchingService;

//    @Test
//    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testListMatches.xml")
//    public void testCompareTaxonListNameCache(){
//        List <String> query = new ArrayList<>();
//        query.add("Reynosia mucronata subsp. azulensis ");
//        query.add(" Asemeia hebeclada var. impensa");
//        query.add("Ehretina crebrifolia");
//        query.add("Bectendra nigri");

//        Map<String, List<SingleNameMatchingResult>> m = nameMatchingService.compareTaxonListNameCache(query);
//        assertTrue("key found", m.containsKey("Reynosia mucronata subsp. azulensis"));
//        assertTrue("key found", m.containsKey("Asemeia hebeclada var. impensa"));
//        assertTrue("key found", m.containsKey("Ehretina crebrifolia"));
//        assertTrue("key found", m.containsKey("Bectendra nigri"));
//
//        List<SingleNameMatchingResult> reynosia = m.get("Reynosia mucronata subsp. azulensis");
//        assertEquals("Reynosia", reynosia.get(0).getGenusOrUninomial());
//        assertEquals(0, (int)reynosia.get(0).getDistance());
//
//        List<SingleNameMatchingResult> asemeia = m.get("Asemeia hebeclada var. impensa");
//        assertEquals("Asemeia", asemeia.get(0).getGenusOrUninomial());
//        assertEquals("hebeclada", asemeia.get(0).getSpecificEpithet());
//        assertEquals(0, (int)reynosia.get(0).getDistance());
//
//        List<SingleNameMatchingResult> bectendra = m.get("Bectendra nigri");
//        assertEquals(2, bectendra.size());
//
//        Assert.assertEquals("Nectandra", bectendra.get(0).getGenusOrUninomial());
//        Assert.assertEquals("nigra", bectendra.get(0).getSpecificEpithet());
//        assertEquals(3, (int)bectendra.get(0).getDistance());
//
//        Assert.assertEquals("Nectandra", bectendra.get(1).getGenusOrUninomial());
//        Assert.assertEquals("nigrita", bectendra.get(1).getSpecificEpithet());
//        assertEquals(4, (int)bectendra.get(1).getDistance());
//    }

    @Test
    public void testTrimCommonChar() {

        String query = "Nectandra";
        String document = "Nectalisma";

        Assert.assertEquals("Should return trimmed remaming string ",
                "ndr", NameMatchingServiceImpl.trimCommonChar(query, document).split(" ")[0]);
        Assert.assertEquals("lism", NameMatchingServiceImpl.trimCommonChar(query, document).split(" ")[1]);
        Assert.assertEquals("Equal input should return empty result",
                "", NameMatchingServiceImpl.trimCommonChar(query, query) );
    }

	@Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesGenus() {

		String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        //MONOMIAL: GENUS

        // exact match
        inputName = "Nectandra";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NECTANDRA, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Distance should be 0", 0,(int) matchRes.getDistance());

        // not exact match
        inputName = "Nextondra";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());

        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectondra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NECTONDRA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nextandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NEXTANDRA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(1,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSpecies() {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // exact match
        inputName = "Nectandra magnoliifolia";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("magnoliifolia", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_MAGNIFOLIA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        /* as Author is not evaluated in this version of the algorithm,
        * if the DB contains the species name twice but with different authorities, both names should be returned
        */

        inputName = "Nectandra surinamensis";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(2, matchResult.size());

        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("surinamensis", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_SURINAMENSIS1, matchRes.getTaxonNameUuid());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("surinamensis", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_SURINAMENSIS2, matchRes.getTaxonNameUuid());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        // not exact match
        inputName = "Nectendra nigre";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigra", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NIGRA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(2,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigrita", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NIGRITA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(4,(int) matchRes.getDistance());

        inputName = "Bectendra nigri";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigra", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NIGRA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(3,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigrita", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NIGRITA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(4,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSubgenus() {

    	String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // exact match
        inputName = "Nectandra subgen. Nectrina";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Nectrina", matchRes.getInfraGenericEpithet());
        Assert.assertEquals(UUID_NAME_NECTRINA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        // not exact match
        inputName = "Nectandra subgen. Nextrina";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Nectrina", matchRes.getInfraGenericEpithet());
        Assert.assertEquals(UUID_NAME_NECTRINA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Nexxxina", matchRes.getInfraGenericEpithet());
        Assert.assertEquals(UUID_NAME_NEXXXINA, matchRes.getTaxonNameUuid());
        Assert.assertEquals(2,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSubspecies() {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // exact match
        inputName = "Nectandra mollis subsp. laurel";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAUREL, matchRes.getTaxonNameUuid());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        // not exact match
        inputName = "Nectandra mollis var. laurol";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, false, null);
        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAUREL, matchRes.getTaxonNameUuid());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laureli", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAURELI, matchRes.getTaxonNameUuid());
        Assert.assertEquals(2,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingGenusWithAuthors() {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        inputName = "Nectandra Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getClosestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        inputName = "Nectindra Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getOtherCandidatesResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectondra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSpeciesWithAuthors() {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // the exact match results show all species names that retrieve a distance of 0
        // EXCLUDING the authorship
        inputName = "Nectandra laevis Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getClosestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        inputName = "Nectindra levis Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getOtherCandidatesResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());

        inputName = "Nectindra cinnamomoides Turm. & Kilian";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getOtherCandidatesResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("cinnamomoides", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl. & Kilian", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());

        //TODO matching non-parsable names is still an open issue (#10178)

        inputName = "Nectindra cinnamomoides Turm. and Kilian";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getOtherCandidatesResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("cinnamomoides", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl. & Kilian", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingGenusWithExAuthors() {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // exact match
        inputName = "Nectandra (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getClosestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        inputName = "Nectandra (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getClosestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSpeciesWithExAuthors() {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // exact match
        inputName = "Nectandra laevis (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getClosestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        inputName = "Nectandra laevis (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getClosestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        // not exact match
        inputName = "Mectandra laevis (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getOtherCandidatesResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());

        inputName = "Mectandra laevis (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getOtherCandidatesResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSubspeciesWithExAuthors() {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // exact match
        inputName = "Nectandra mollis subsp. laurel Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAUREL, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        // not exact match
        inputName = "Nectandra mollis var. laurol (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, null, true, null);
        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAUREL, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laureli", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAURELI, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Assert.assertEquals(3,(int) matchRes.getDistance());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingWithoutAuthorComparison() {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // exact match when author is not in the input name and boolean author is false
        nameCache = "Yucca filamentosa";
        matchResults = nameMatchingService.listShaping(nameCache, false, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Assert.assertEquals(0,(int) matchRes.getDistance());

     // exact match when author is given in the input name but boolean is still false
        nameCache = "Yucca filamentosa L.";
        matchResults = nameMatchingService.listShaping(nameCache, false, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        // other candidates results without author input
        nameCache = "Yucca filamentoza";
        matchResults = nameMatchingService.listShaping(nameCache, false, null);

        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());

        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());

     // other candidates results with author
        nameCache = "Yucca filamentoz L.";
        matchResults = nameMatchingService.listShaping(nameCache, false, null);

        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());

        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance());
}

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingWithAuthorComparison() {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        // exact match when author is given in the input name and boolean author is true
        nameCache = "Yucca filamentosa L.";
        matchResults = nameMatchingService.listShaping(nameCache, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Assert.assertEquals(0,(int) matchRes.getDistance());

     // exact match when author is missing in the input name and boolean author is true
////////this is wrong. This should return a warning: please give an author in the input name
        nameCache = "Yucca filamentosa";
        matchResults = nameMatchingService.listShaping(nameCache, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Assert.assertEquals(2,(int) matchRes.getDistance()); // distance is 2 because the abbreviation dot counts as a character too

        nameCache = "Yucca filamentosa M.";
        matchResults = nameMatchingService.listShaping(nameCache, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Assert.assertEquals(1,(int) matchRes.getDistance());
}
    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingDistance() {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;

        // exact match is always a list of matches with distance = 0
        nameCache = "Yucca filamentosa L.";
        matchResults = nameMatchingService.listShaping(nameCache, false, 10);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());
        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(11, matchResult.size());

        nameCache = "Yucca filamentosa Len.";
        matchResults = nameMatchingService.listShaping(nameCache, true, 10);
        matchResult = matchResults.getClosestResults();
        Assert.assertEquals(2, matchResult.size());
        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(4, matchResult.size());

}
    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testWrapperResults() {
        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        nameCache = "Yucca gloriosa subsp. gloriosa";

        // output exact results + relaxed results + other candidates
        matchResults = nameMatchingService.wrapperResults(nameCache, false, 10, true, true);

        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());

        //when relaxed search is set to true, the "closest results" should include all names that matched
        //each component of the name individually. The rank is not included in the distance, hence
        // the distance is 0 (perfect match)

        /**TODO
         * Discuss the value of the distance for a not-matching rank.
         */

        matchResult = matchResults.getClosestResults();
        matchRes = matchResult.get(0);

        Assert.assertEquals(1, matchResult.size());
        Assert.assertEquals("Yucca gloriosa L. var. gloriosa", matchRes.getTitleCache());
        Assert.assertEquals(0, (int)matchRes.getDistance());

        matchResult = matchResults.getOtherCandidatesResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca gloriosa f. genuina Engelm.", matchRes.getTitleCache());
        Assert.assertEquals(5, (int)matchRes.getDistance());

    }

	@Override
	public void createTestDataSet() throws FileNotFoundException {}
}