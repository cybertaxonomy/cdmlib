/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.NameMatchingResult;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.SingleNameMatchingResult;
import eu.etaxonomy.cdm.api.service.exception.NameMatchingParserException;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * Testclass for {@link NameMatchingServiceImpl}
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/10178
 * @author andreabee90
 * @since 11.07.2023
 */
public class NameMatchingServiceImplTest extends CdmServicesTransactionalSpringIntegrationTest {

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
    private static final UUID UUID_NAME_GENTIANA = UUID.fromString("049f6d47-f056-4915-814b-aa7289d3320d");
    private static final UUID UUID_NAME_PASSIFLORAFO = UUID.fromString("9d12d1ad-24f9-46a8-b6a4-33c241424a07");
    private static final UUID UUID_NAME_ASTERELLA = UUID.fromString("6b0f5e36-c00a-4297-967b-6f0d7a98c8f3");
    private static final UUID UUID_NAME_PASSIFLORABR = UUID.fromString("3a103ea2-c2ec-4449-ba7d-cf4495fdfb32");

    private static final boolean COMPARE_AUTHORS = true;
    private static final boolean EXCLUDE_BASIONYMAUTHORS = true;
    private static final boolean EXCLUDE_EXAUTHORS = true;

    private static DecimalFormat decimalFormat;

    @BeforeAll
    public static void setUp() throws Exception {
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat (pattern, otherSymbols);
    }

    @Autowired
	private INameMatchingService nameMatchingService;

    @Test
    public void testTrimCommonChar() {

        String query = "Nectandra";
        String document = "Nectalisma";
        assertEquals("ndr", NameMatchingServiceImpl.trimCommonChar(query, document).split(" ")[0], "Should return trimmed remaming string ");
        assertEquals("lism", NameMatchingServiceImpl.trimCommonChar(query, document).split(" ")[1]);
        assertEquals("", NameMatchingServiceImpl.trimCommonChar(query, query), "Equal input should return empty result");
    }

	@Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesGenus() throws NameMatchingParserException {

		String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> exactResults;
        List<SingleNameMatchingResult> bestFuzzyResults;

        //MONOMIAL: GENUS

        // exact match
        inputName = "Nectandra";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        SingleNameMatchingResult firstExactResult = exactResults.get(0);
        assertEquals("Nectandra", firstExactResult.getGenusOrUninomial());
        assertEquals("", firstExactResult.getSpecificEpithet());
        assertEquals(UUID_NAME_NECTANDRA, firstExactResult.getTaxonNameUuid());
        assertEquals(1, firstExactResult.getDistance().intValue(), "Distance should be 1");

        // not exact match
        inputName = "Nextondra";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(3, bestFuzzyResults.size());
        SingleNameMatchingResult firstFuzzyResult = bestFuzzyResults.get(0);
        assertEquals("Nectondra", firstFuzzyResult.getGenusOrUninomial());
        assertEquals("", firstFuzzyResult.getSpecificEpithet());
        assertEquals(UUID_NAME_NECTONDRA, firstFuzzyResult.getTaxonNameUuid());
        Double distance =  firstFuzzyResult.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.889", formmattedDouble, "Nectondra and Nextondra have 1 distinct character. "
                + "The length of the string is 9 => 1 - (distChar/totalChar) = 0,888888...");

        SingleNameMatchingResult secondFuzzyResult = bestFuzzyResults.get(1);
        assertEquals("Nextandra", secondFuzzyResult.getGenusOrUninomial());
        assertEquals("", secondFuzzyResult.getSpecificEpithet());
        assertEquals(UUID_NAME_NEXTANDRA, secondFuzzyResult.getTaxonNameUuid());
        distance =  secondFuzzyResult.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.889", formmattedDouble, "Nextandra and Nextondra have 1 distinct character. "
                + "The length of the string is 9 => 1 - (distChar/totalChar) = 0,888888...");

        SingleNameMatchingResult thirdFuzzyResult = bestFuzzyResults.get(2);
        assertEquals("Nectandra", thirdFuzzyResult.getGenusOrUninomial());
        assertEquals("", thirdFuzzyResult.getSpecificEpithet());
        assertEquals(UUID_NAME_NECTANDRA, thirdFuzzyResult.getTaxonNameUuid());
        distance =  thirdFuzzyResult.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.778", formmattedDouble, "Nectandra and Nextondra have 2 distinct characters. "
                + "The length of the string is 9 => 1 - (distChar/totalChar) = 0,77777...");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSpecies() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;

        // exact match
        inputName = "Nectandra magnoliifolia";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        List<SingleNameMatchingResult> exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        SingleNameMatchingResult firstExactMatch = exactResults.get(0);
        assertEquals("Nectandra", firstExactMatch.getGenusOrUninomial());
        assertEquals("magnoliifolia", firstExactMatch.getSpecificEpithet());
        assertEquals(UUID_NAME_MAGNIFOLIA, firstExactMatch.getTaxonNameUuid());
        String formmattedDouble = decimalFormat.format(firstExactMatch.getDistance());
        assertEquals("1", formmattedDouble);

        /* as Author is not evaluated in this version of the algorithm,
        * if the DB contains the species name twice but with different authorities, both names should be returned
        */

        inputName = "Nectandra surinamensis";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(2, exactResults.size());

        firstExactMatch = exactResults.get(0);
        assertEquals("Nectandra", firstExactMatch.getGenusOrUninomial());
        assertEquals("surinamensis", firstExactMatch.getSpecificEpithet());
        assertEquals(UUID_NAME_SURINAMENSIS1, firstExactMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(firstExactMatch.getDistance());
        assertEquals("1",formmattedDouble);

        SingleNameMatchingResult secondExactMatch = exactResults.get(1);
        assertEquals("Nectandra", secondExactMatch.getGenusOrUninomial());
        assertEquals("surinamensis", secondExactMatch.getSpecificEpithet());
        assertEquals(UUID_NAME_SURINAMENSIS2, secondExactMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(secondExactMatch.getDistance());
        assertEquals("1",formmattedDouble);

        // not exact match
        inputName = "Nectendra nigre";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        List<SingleNameMatchingResult> bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("nigra", firstFuzzyMatch.getSpecificEpithet());
        assertEquals(UUID_NAME_NIGRA, firstFuzzyMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(firstFuzzyMatch.getDistance());
        assertEquals("0.857", formmattedDouble, "Nectandra nigra and Nectendra nigre have 2 distinct characters. "
                + "The length of the string is 14 => 1 - (distChar/totalChar) = 0.85714...");

        SingleNameMatchingResult secondFuzzyMatch = bestFuzzyResults.get(1);
        assertEquals("Nectandra", secondFuzzyMatch.getGenusOrUninomial());
        assertEquals("nigrita", secondFuzzyMatch.getSpecificEpithet());
        assertEquals(UUID_NAME_NIGRITA, secondFuzzyMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(secondFuzzyMatch.getDistance());
        assertEquals("0.75", formmattedDouble, "Nectandra nigrita and Nectendra nigre have 4 distinct characters. "
                + "The length of the string is 16 => 1 - (distChar/totalChar) = 0.75");

        inputName = "Bectendra nigri";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS,  null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("nigra", firstFuzzyMatch.getSpecificEpithet());
        assertEquals(UUID_NAME_NIGRA, firstFuzzyMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(firstFuzzyMatch.getDistance());
        assertEquals("0.786", formmattedDouble, "Nectandra nigra and Bectendra nigri have 3 distinct characters. "
                + "The length of the string is 14 => 1 - (distChar/totalChar) = 0.785714...");

        secondFuzzyMatch = bestFuzzyResults.get(1);
        assertEquals("Nectandra", secondFuzzyMatch.getGenusOrUninomial());
        assertEquals("nigrita", secondFuzzyMatch.getSpecificEpithet());
        assertEquals(UUID_NAME_NIGRITA, secondFuzzyMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(secondFuzzyMatch.getDistance());
        assertEquals("0.75", formmattedDouble, "Nectandra nigrita and Bectendra nigri have 4 distinct characters. "
                + "The length of the string is 16 => 1 - (distChar/totalChar) = 0.75");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSubgenus() throws NameMatchingParserException {

    	String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> exactResults;

        // exact match
        inputName = "Nectandra subgen. Nectrina";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        String formmattedDouble = decimalFormat.format(exactResults.get(0).getDistance());
        assertEquals("1", formmattedDouble);

        List<SingleNameMatchingResult> bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(1, bestFuzzyResults.size());
        formmattedDouble = decimalFormat.format(bestFuzzyResults.get(0).getDistance());
        assertEquals("0.833", formmattedDouble, "Nectandra subgen. Nectrina and Nectandra nothosubg. Nexxxina. Same rank, differences in restant string = 3."
                + " Total string length 9 + 1 (rank) + 8 = 18 => 1 - (distChar/totalChar) = 0.83333... ");

        // not exact match
        inputName = "Nectandra subgen. Nextrina";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("Nectrina", firstFuzzyMatch.getInfraGenericEpithet());
        assertEquals(UUID_NAME_NECTRINA, firstFuzzyMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(firstFuzzyMatch.getDistance());
        assertEquals("0.944", formmattedDouble, "Nectandra subgen. Nectrina and Nectandra nothosubg. Nexxxina. Same rank, differences in restant string = 3. "
                + "Total string length 9 + 1 (rank) + 8 = 18 => 1 - (distChar/totalChar) = 0.83333... ");

        SingleNameMatchingResult secondFuzzyMatch = bestFuzzyResults.get(1);
        assertEquals("Nectandra", secondFuzzyMatch.getGenusOrUninomial());
        assertEquals("Nexxxina", secondFuzzyMatch.getInfraGenericEpithet());
        assertEquals(UUID_NAME_NEXXXINA, secondFuzzyMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(secondFuzzyMatch.getDistance());
        assertEquals("0.889", formmattedDouble, "Nectandra subgen. Nectrina and Nectandra nothosubg. Nexxxina. Same rank, differences in restant string = 3. "
                + "Total string length 9 + 1 (rank) + 8 = 18 => 1 - (distChar/totalChar) = 0.83333... ");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSubspecies() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> exactResults;

        // exact match
        inputName = "Nectandra mollis subsp. laurel";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        SingleNameMatchingResult firstExactMatch = exactResults.get(0);
        assertEquals("Nectandra", firstExactMatch.getGenusOrUninomial());
        assertEquals("mollis", firstExactMatch.getSpecificEpithet());
        assertEquals("laurel", firstExactMatch.getInfraSpecificEpithet());
        assertEquals(UUID_NAME_LAUREL, firstExactMatch.getTaxonNameUuid());
        String formmattedDouble = decimalFormat.format(firstExactMatch.getDistance());
        assertEquals("1", formmattedDouble);

        // not exact match
        inputName = "Nectandra mollis var. laurol";
        matchResults = nameMatchingService.findMatchingNames(inputName, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        List<SingleNameMatchingResult> bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("mollis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("laurel", firstFuzzyMatch.getInfraSpecificEpithet());
        assertEquals(763, firstFuzzyMatch.getRank().getId(), "subsp.");
        assertEquals(UUID_NAME_LAUREL, firstFuzzyMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(firstFuzzyMatch.getDistance());
        assertEquals("0.909", formmattedDouble, "Nectandra mollis var. laurol and Nectandra mollis subsp. laurel. Different rank (penalty = 1). Diff restant string = 1 "
                + "Total string length 9 + 6 + 1 (rank) + 6 = 22 => 1 - (distChar/totalChar) = 0.909090...");

        SingleNameMatchingResult secondFuzzyMatch = bestFuzzyResults.get(1);
        assertEquals("Nectandra", secondFuzzyMatch.getGenusOrUninomial());
        assertEquals("mollis", secondFuzzyMatch.getSpecificEpithet());
        assertEquals("laureli", secondFuzzyMatch.getInfraSpecificEpithet());
        assertEquals(UUID_NAME_LAURELI, secondFuzzyMatch.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(secondFuzzyMatch.getDistance());
        assertEquals("0.87", formmattedDouble, "Nectandra mollis var. laurol and Nectandra mollis subsp. laureli. Different rank (penalty = 1). Diff restant string = 2 "
                + "Total string length 9 + 6 + 1 (rank) + 7 = 23 => 1 - (distChar/totalChar) = 0.86956...");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingGenusWithAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> bestFuzzyResults;

        inputName = "Nectandra Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS,  null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        Double distance =  firstFuzzyMatch.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.969", formmattedDouble, "input: Nectandra Turm., match: Nectandra Turl. 1 diff in authors. Author´s score is 1/3 of total score => diffCharAuth/3 = 0.3333333. "
                + "Total string length 9 + (5/3)(author length) = 10.66666... => 1 - (distChar/totalChar) = 0.969...");

        inputName = "Nectindra Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectondra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.875", formmattedDouble, "input: Nectindra Turm., match: Nectondra Turl. 1 diff char in author + 1 diff char in name. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + (5/3)(author length) = 10.66666... => 1 - (distChar/totalChar) = 0.875023...");

        SingleNameMatchingResult secondFuzzyMatch = bestFuzzyResults.get(1);
        assertEquals("Nectandra", secondFuzzyMatch.getGenusOrUninomial());
        assertEquals("Turl.", secondFuzzyMatch.getAuthorshipCache());
        distance =  secondFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.875", formmattedDouble);
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSpeciesWithAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> bestFuzzyResults;

        // the exact match results show all species names that retrieve a distance of 0
        // EXCLUDING the authorship
        inputName = "Nectandra laevis Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("laevis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        Double distance =  firstFuzzyMatch.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.98", formmattedDouble, "input: Nectandra laevis Turm., match: Nectandra laevis Turl. 1 diff char in author. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + 6 + (5/3)(author length) = 16.66666... => 1 - (distChar/totalChar) = 0.98001...");

        inputName = "Nectindra lxevis Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("laevis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.86", formmattedDouble, "input: Nectandra laevis Turm., match: Nectandra laevis Turl. 1 diff char in author. 2 diff in name. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + 6 + (5/3)(author length) = 16.66666... => 1 - (distChar/totalChar) = 0.86019...");

        inputName = "Nectindra cinnamomoides Turm. & Kilian";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("cinnamomoides", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("Turl. & Kilian", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.948", formmattedDouble, "input: Nectindra cinnamomoides Turm. & Kilian, match: Nectandra cinnamomoides Turl. & Kilian. 1 diff char in author. 1 diff in name. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + 13 + (11/3)(author length) = 25.66666... => 1 - (distChar/totalChar) = 0.94805...");

        //TODO matching non-parsable names is still an open issue (#10178)

        inputName = "Nectindra cinnamomoides Turm. and Kilian";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("cinnamomoides", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("Turl. & Kilian", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.948", formmattedDouble);
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingGenusWithExAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> bestFuzzyResults;

        // not exact match
        inputName = "Nectandra (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        Double distance =  firstFuzzyMatch.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.969", formmattedDouble, "input: Nectandra (Kilian) Turm., match: Nectandra Turl. 1 diff char in author. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + (5/3)(author length) = 10.66666... => 1 - (distChar/totalChar) = 0.968779...");

        inputName = "Nectandra (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.969", formmattedDouble);
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSpeciesWithExAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> bestFuzzyResults;

        // exact match
        inputName = "Nectandra laevis (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("laevis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        Double distance =  firstFuzzyMatch.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.98", formmattedDouble, "input: Nectandra laevis (Kilian) Turm., match: Nectandra laevis Turl. 1 diff char in author. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + 6 + (5/3)(author length) = 16.66666... => 1 - (distChar/totalChar) = 0.98000...");

        inputName = "Nectandra laevis (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("laevis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.98", formmattedDouble, "input: Nectandra laevis (Kilian) Turm., match: Nectandra laevis Turl. 1 diff char in author. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + 6 + (5/3)(author length) = 16.66666... => 1 - (distChar/totalChar) = 0.98000...");

        // not exact match
        inputName = "Mectandra laevis (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("laevis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.92", formmattedDouble, "input: Mectandra laevis (Kilian) Turm., match: Nectandra laevis Turl. 1 diff char in author. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + 6 + (5/3)(author length) = 16.66666... => 1 - (distChar/totalChar) = 0.91999...");

        inputName = "Mectandra laevis (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("laevis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.92", formmattedDouble, "input: Mectandra laevis (Kilian) Turm., match: Nectandra laevis Turl. 1 diff char in author. Diff Author´s score is 1/3 of total"
                + " score => diffCharAuth/3 = 0.3333333. Total string length 9 + 6 + (5/3)(author length) = 16.66666... => 1 - (distChar/totalChar) = 0.91999...");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSubspeciesWithExAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> bestFuzzyResults;

        // not exact match
        inputName = "Nectandra mollis subsp. laurel Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("mollis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("laurel", firstFuzzyMatch.getInfraSpecificEpithet());
        assertEquals(763, firstFuzzyMatch.getRank().getId(), "subsp.");
        assertEquals(UUID_NAME_LAUREL, firstFuzzyMatch.getTaxonNameUuid());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        Double distance =  firstFuzzyMatch.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.986", formmattedDouble, "input: Nectandra mollis subsp. laurel Kilian ex Turm., match: Nectandra mollis subsp. laurel Turl. 1 diff char in author. "
                + "Diff Author´s score is 1/3 of total score => diffCharAuth/3 = 0.3333333. Total string length 9 + 6 + 1(rank) + 6 + (5/3)(author length) = 22.66666... "
                + "=> 1 - (distChar/totalChar) = 0.985915...");

        // not exact match
        inputName = "Nectandra mollis var. laurol (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Nectandra", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("mollis", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("laurel", firstFuzzyMatch.getInfraSpecificEpithet());
        assertEquals(763, firstFuzzyMatch.getRank().getId(), "subsp.");
        assertEquals(UUID_NAME_LAUREL, firstFuzzyMatch.getTaxonNameUuid());
        assertEquals("Turl.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.901", formmattedDouble, "input: Nectandra mollis var. laurol (Kilian) Turm., match: Nectandra mollis subsp. laurel Turl. 1 diff char in author. "
                + "Diff Author´s score is 1/3 of total score => diffCharAuth/3 = 0.3333333. Diff in rank is 1. Total string length 9 + 6 + 1(rank) + 6 "
                + "+ (5/3)(author length) = 22.66666... => 1 - (distChar/totalChar) = 0.901408...");

        SingleNameMatchingResult secondFuzzyMatch = bestFuzzyResults.get(1);
        assertEquals("Nectandra", secondFuzzyMatch.getGenusOrUninomial());
        assertEquals("mollis", secondFuzzyMatch.getSpecificEpithet());
        assertEquals("laureli", secondFuzzyMatch.getInfraSpecificEpithet());
        assertEquals(UUID_NAME_LAURELI, secondFuzzyMatch.getTaxonNameUuid());
        assertEquals("Turl.", secondFuzzyMatch.getAuthorshipCache());
        distance =  secondFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.865", formmattedDouble, "1-((3 + (1/3))/(9+6+1+7+(5/3))) = 0,8648648...");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingWithoutAuthorComparison() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;

        // exact match when author is not in the input name and boolean author is false
        nameCache = "Yucca filamentosa";
        matchResults = nameMatchingService.findMatchingNames(nameCache, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        List<SingleNameMatchingResult> exactResults = matchResults.getExactResults();
        assertEquals(2, exactResults.size());
        SingleNameMatchingResult firstExactMatch = exactResults.get(0);
        assertEquals("Yucca", firstExactMatch.getGenusOrUninomial());
        assertEquals("filamentosa", firstExactMatch.getSpecificEpithet());
        assertEquals("L.", firstExactMatch.getAuthorshipCache());
        Double distance =  firstExactMatch.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);

     // exact match when author is given in the input name but boolean is still false
        nameCache = "Yucca filamentosa M.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(2, exactResults.size());
        firstExactMatch = exactResults.get(0);
        assertEquals("Yucca", firstExactMatch.getGenusOrUninomial());
        assertEquals("filamentosa", firstExactMatch.getSpecificEpithet());
        assertEquals("L.", firstExactMatch.getAuthorshipCache());
        distance =  firstExactMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);

     // other candidates results without author input
        nameCache = "Yucca filamentoza";
        matchResults = nameMatchingService.findMatchingNames(nameCache, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(0, exactResults.size());

        List<SingleNameMatchingResult> bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Yucca", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("filamentosa", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("L.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.938", formmattedDouble, "1-(1 /(5+11)) = 0,9375");

     // other candidates results with author
        nameCache = "Yucca filamentoz L.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(0, exactResults.size());
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Yucca", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("filamentosa", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("L.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.875", formmattedDouble, "1-(2 /(5+11)) = 0,875");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingWithAuthorComparison() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;

        // exact match when author is given in the input name and boolean author is true
        nameCache = "Yucca filamentosa L.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        List<SingleNameMatchingResult> exactResults = matchResults.getExactResults();
        assertEquals(2, exactResults.size());
        SingleNameMatchingResult firstExactMatch = exactResults.get(0);
        assertEquals("Yucca", firstExactMatch.getGenusOrUninomial());
        assertEquals("filamentosa", firstExactMatch.getSpecificEpithet());
        assertEquals("L.", firstExactMatch.getAuthorshipCache());
        Double distance =  firstExactMatch.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);

     // exact match when author is missing in the input name and boolean author is true
     // this is wrong. This should return a warning: please give an author in the input name
        nameCache = "Yucca filamentosa";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(0, exactResults.size());
        List<SingleNameMatchingResult> bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        SingleNameMatchingResult firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Yucca", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("filamentosa", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("L.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.96", formmattedDouble, "1-(0+(2/3)/(5+11+(2/3))) = 0,96");

        nameCache = "Yucca filamentosa M.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, null);
        exactResults = matchResults.getExactResults();
        assertEquals(0, exactResults.size());
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(2, bestFuzzyResults.size());
        firstFuzzyMatch = bestFuzzyResults.get(0);
        assertEquals("Yucca", firstFuzzyMatch.getGenusOrUninomial());
        assertEquals("filamentosa", firstFuzzyMatch.getSpecificEpithet());
        assertEquals("L.", firstFuzzyMatch.getAuthorshipCache());
        distance =  firstFuzzyMatch.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.98", formmattedDouble, "1-(0+(1/3)/(5+11+(2/3))) = 0,98");
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingDistance() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;

        // exact match is always a list of matches with distance = 0
        nameCache = "Yucca filamentosa L.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, !COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, 10.0);
        List<SingleNameMatchingResult> exactResults = matchResults.getExactResults();
        assertEquals(2, exactResults.size());
        List<SingleNameMatchingResult> bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(11, bestFuzzyResults.size());

        nameCache = "Yucca filamentosa Len.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, 10.0);
        bestFuzzyResults = matchResults.getBestFuzzyResults();
        assertEquals(9, bestFuzzyResults.size());
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testIncludeAllAuthors() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;

        // exact match is always a list of matches with distance = 0
        nameCache = "Gentiana affinis subsp. rusbyi (Greene ex Kusn.) Halda";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, !EXCLUDE_BASIONYMAUTHORS, !EXCLUDE_EXAUTHORS, 0.0);
        List<SingleNameMatchingResult> exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        assertEquals(UUID_NAME_GENTIANA, matchResults.getExactResults().get(0).getTaxonNameUuid());
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testExcludeBasionymAuthors() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;

        nameCache = "Passiflora foetida var. hispida (Corda. ex Triana & Planch.) Killip ex Gleason";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, !EXCLUDE_EXAUTHORS, 0.0);
        List<SingleNameMatchingResult> exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        assertEquals(UUID_NAME_PASSIFLORAFO, matchResults.getExactResults().get(0).getTaxonNameUuid());
        assertEquals("Passiflora foetida var. hispida (DC. ex Triana & Planch.) Killip ex Gleason", exactResults.get(0).getTitleCache(), "basionym authors are excluded");
        Double distance = exactResults.get(0).getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);

        nameCache = "Asterella lindenbergiana (Nees ex Corda) Lindb. ex Arnell";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, !EXCLUDE_EXAUTHORS, 0.0);
        exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        assertEquals(UUID_NAME_ASTERELLA, matchResults.getExactResults().get(0).getTaxonNameUuid());
        assertEquals("Asterella lindenbergiana (Corda ex Nees) Lindb. ex Arnell", exactResults.get(0).getTitleCache(), "basionym authors are excluded");
    }
    @Test
    @CdmDataSet(cleanSweep = true, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testExcludeExAuthors() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;

        nameCache = "Passiflora bracteosa Linden & Planch. ex Triana & Planch.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, 0.0);
        List<SingleNameMatchingResult> exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        assertEquals(UUID_NAME_PASSIFLORABR, matchResults.getExactResults().get(0).getTaxonNameUuid());
        assertEquals("Passiflora bracteosa Planch. & Linden ex Triana & Planch.", exactResults.get(0).getTitleCache(), "ex authors are ignored");

        nameCache = "Passiflora foetida var. hispida (Corda. ex Triana & Planch.) Cuatrec. ex Gleason";
        matchResults = nameMatchingService.findMatchingNames(nameCache, COMPARE_AUTHORS, !EXCLUDE_BASIONYMAUTHORS, EXCLUDE_EXAUTHORS, 0.0);
        exactResults = matchResults.getExactResults();
        assertEquals(1, exactResults.size());
        assertEquals(UUID_NAME_PASSIFLORAFO, matchResults.getExactResults().get(0).getTaxonNameUuid());
        assertEquals("Passiflora foetida var. hispida (DC. ex Triana & Planch.) Killip ex Gleason", exactResults.get(0).getTitleCache(), "ex authors are ignored");
        Double distance = exactResults.get(0).getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);
    }

    @Override
	public void createTestDataSet() throws FileNotFoundException {}
}