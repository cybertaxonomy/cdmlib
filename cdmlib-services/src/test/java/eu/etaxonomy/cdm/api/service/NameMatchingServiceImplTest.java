/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.NameMatchingResult;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.SingleNameMatchingResult;
import eu.etaxonomy.cdm.api.service.exception.NameMatchingParserException;
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
    private static final UUID UUID_NAME_GENTIANA = UUID.fromString("049f6d47-f056-4915-814b-aa7289d3320d");
    private static final UUID UUID_NAME_PASSIFLORAFO = UUID.fromString("9d12d1ad-24f9-46a8-b6a4-33c241424a07");
    private static final UUID UUID_NAME_ASTERELLA = UUID.fromString("6b0f5e36-c00a-4297-967b-6f0d7a98c8f3");
    private static final UUID UUID_NAME_PASSIFLORABR = UUID.fromString("3a103ea2-c2ec-4449-ba7d-cf4495fdfb32");

    @SpringBeanByType
	private INameMatchingService nameMatchingService;

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
    public void testFindingMatchingNamesGenus() throws NameMatchingParserException {

		String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');

        //MONOMIAL: GENUS

        // exact match
        inputName = "Nectandra";
        matchResults = nameMatchingService.findMatchingNames(inputName, false, true, true, null);
        matchResult = matchResults.getExactResults();

        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NECTANDRA, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Distance should be 1", 1,matchRes.getDistance().intValue());

        // not exact match
        inputName = "Nextondra";
        matchResults = nameMatchingService.findMatchingNames(inputName, false, true, true, null);
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(3, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectondra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NECTONDRA, matchRes.getTaxonNameUuid());
        Double distance =  matchRes.getDistance();
        DecimalFormat decimalFormat = new DecimalFormat (pattern,otherSymbols);
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.889", formmattedDouble);

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nextandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NEXTANDRA, matchRes.getTaxonNameUuid());
        distance =  matchRes.getDistance();
        decimalFormat = new DecimalFormat (pattern,otherSymbols);
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.889", formmattedDouble);

        matchRes = matchResult.get(2);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NECTANDRA, matchRes.getTaxonNameUuid());
        distance =  matchRes.getDistance();
        decimalFormat = new DecimalFormat (pattern,otherSymbols);
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.778", formmattedDouble);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSpecies() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern,otherSymbols);

        // exact match
        inputName = "Nectandra magnoliifolia";
        matchResults = nameMatchingService.findMatchingNames(inputName, false,true, true,  null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("magnoliifolia", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_MAGNIFOLIA, matchRes.getTaxonNameUuid());
        String formmattedDouble = decimalFormat.format(matchRes.getDistance());
        assertEquals("1", formmattedDouble);

        /* as Author is not evaluated in this version of the algorithm,
        * if the DB contains the species name twice but with different authorities, both names should be returned
        */

        inputName = "Nectandra surinamensis";
        matchResults = nameMatchingService.findMatchingNames(inputName, false, true, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());

        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("surinamensis", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_SURINAMENSIS1, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("1",formmattedDouble);

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("surinamensis", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_SURINAMENSIS2, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("1",formmattedDouble);

        // not exact match
        inputName = "Nectendra nigre";
        matchResults = nameMatchingService.findMatchingNames(inputName, false, true, true, null);
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigra", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NIGRA, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("0.857",formmattedDouble);

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigrita", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NIGRITA, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("0.75",formmattedDouble);

        inputName = "Bectendra nigri";
        matchResults = nameMatchingService.findMatchingNames(inputName, false,true, true,  null);
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigra", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NIGRA, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("0.786",formmattedDouble);

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigrita", matchRes.getSpecificEpithet());
        Assert.assertEquals(UUID_NAME_NIGRITA, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("0.75",formmattedDouble);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSubgenus() throws NameMatchingParserException {

    	String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern,otherSymbols);

        // exact match
        inputName = "Nectandra subgen. Nectrina";
        matchResults = nameMatchingService.findMatchingNames(inputName, false, true, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(1, matchResult.size());
        String formmattedDouble = decimalFormat.format(matchResult.get(0).getDistance());
        Assert.assertEquals("1", formmattedDouble);

        matchResult = matchResults.getBestResults();
        Assert.assertEquals(1, matchResult.size());
        formmattedDouble = decimalFormat.format(matchResult.get(0).getDistance());
        Assert.assertEquals("0.833", formmattedDouble);

        // not exact match
        inputName = "Nectandra subgen. Nextrina";
        matchResults = nameMatchingService.findMatchingNames(inputName, false, true, true, null);
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Nectrina", matchRes.getInfraGenericEpithet());
        Assert.assertEquals(UUID_NAME_NECTRINA, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("0.944", formmattedDouble);

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Nexxxina", matchRes.getInfraGenericEpithet());
        Assert.assertEquals(UUID_NAME_NEXXXINA, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("0.889", formmattedDouble);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNamesSubspecies() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern,otherSymbols);

        // exact match
        inputName = "Nectandra mollis subsp. laurel";
        matchResults = nameMatchingService.findMatchingNames(inputName, false, true, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAUREL, matchRes.getTaxonNameUuid());
        String formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("1", formmattedDouble);

        // not exact match
        inputName = "Nectandra mollis var. laurol";
        matchResults = nameMatchingService.findMatchingNames(inputName, false, true, true, null);
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAUREL, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("0.909", formmattedDouble);

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laureli", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAURELI, matchRes.getTaxonNameUuid());
        formmattedDouble = decimalFormat.format(matchRes.getDistance());
        Assert.assertEquals("0.87", formmattedDouble);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingGenusWithAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern,otherSymbols);

        inputName = "Nectandra Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true,  null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Double distance =  matchRes.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.969", formmattedDouble);

        inputName = "Nectindra Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectondra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.875", formmattedDouble);

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        pattern = "#.###";
        decimalFormat = new DecimalFormat (pattern, otherSymbols);
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.875", formmattedDouble);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSpeciesWithAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern,otherSymbols);

        // the exact match results show all species names that retrieve a distance of 0
        // EXCLUDING the authorship
        inputName = "Nectandra laevis Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Double distance =  matchRes.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.98", formmattedDouble);

        inputName = "Nectindra lxevis Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true,null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.86", formmattedDouble);

        inputName = "Nectindra cinnamomoides Turm. & Kilian";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true,null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("cinnamomoides", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl. & Kilian", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.948", formmattedDouble);

        //TODO matching non-parsable names is still an open issue (#10178)

        inputName = "Nectindra cinnamomoides Turm. and Kilian";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("cinnamomoides", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl. & Kilian", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.948", formmattedDouble);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingGenusWithExAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern, otherSymbols);

        // exact match
        inputName = "Nectandra (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Double distance =  matchRes.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.969", formmattedDouble);

        inputName = "Nectandra (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.969", formmattedDouble);

    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSpeciesWithExAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern, otherSymbols);

        // exact match
        inputName = "Nectandra laevis (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Double distance =  matchRes.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.98", formmattedDouble);

        inputName = "Nectandra laevis (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.98", formmattedDouble);

        // not exact match
        inputName = "Mectandra laevis (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.92", formmattedDouble);

        inputName = "Mectandra laevis (Kilian ex Turm.) Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("laevis", matchRes.getSpecificEpithet());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.92", formmattedDouble);

    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingSubspeciesWithExAuthors() throws NameMatchingParserException {

        String inputName;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern, otherSymbols);

        // exact match
        inputName = "Nectandra mollis subsp. laurel Kilian ex Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAUREL, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        Double distance =  matchRes.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.986", formmattedDouble);

        // not exact match
        inputName = "Nectandra mollis var. laurol (Kilian) Turm.";
        matchResults = nameMatchingService.findMatchingNames(inputName, true, true, true, null);
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAUREL, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.901", formmattedDouble);

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
        Assert.assertEquals("laureli", matchRes.getInfraSpecificEpithet());
        Assert.assertEquals(UUID_NAME_LAURELI, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Turl.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.865", formmattedDouble);
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingWithoutAuthorComparison() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern, otherSymbols);

        // exact match when author is not in the input name and boolean author is false
        nameCache = "Yucca filamentosa";
        matchResults = nameMatchingService.findMatchingNames(nameCache, false, true, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Double distance =  matchRes.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);

     // exact match when author is given in the input name but boolean is still false
        nameCache = "Yucca filamentosa M.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, false, true, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);

     // other candidates results without author input
        nameCache = "Yucca filamentoza";
        matchResults = nameMatchingService.findMatchingNames(nameCache, false, true, true,null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());

        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.938", formmattedDouble);

     // other candidates results with author
        nameCache = "Yucca filamentoz L.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, false, true, true,null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.875", formmattedDouble);
}

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingWithAuthorComparison() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern, otherSymbols);

        // exact match when author is given in the input name and boolean author is true
        nameCache = "Yucca filamentosa L.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, true, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        Double distance =  matchRes.getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);

     // exact match when author is missing in the input name and boolean author is true
     // this is wrong. This should return a warning: please give an author in the input name
        nameCache = "Yucca filamentosa";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, true, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.96", formmattedDouble);

        nameCache = "Yucca filamentosa M.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, true, true, null);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(0, matchResult.size());
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Yucca", matchRes.getGenusOrUninomial());
        Assert.assertEquals("filamentosa", matchRes.getSpecificEpithet());
        Assert.assertEquals("L.", matchRes.getAuthorshipCache());
        distance =  matchRes.getDistance();
        formmattedDouble = decimalFormat.format(distance);
        assertEquals("0.98", formmattedDouble);
}

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testListShapingDistance() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;

        // exact match is always a list of matches with distance = 0
        nameCache = "Yucca filamentosa L.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, false, true, true, 10.0);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(2, matchResult.size());
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(11, matchResult.size());

        nameCache = "Yucca filamentosa Len.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, true, true, 10.0);
        matchResult = matchResults.getBestResults();
        Assert.assertEquals(9, matchResult.size());
}

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testIncludeAllAuthors() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;

        // exact match is always a list of matches with distance = 0
        nameCache = "Gentiana affinis subsp. rusbyi (Greene ex Kusn.) Halda";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, false, false, 0.0);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(1, matchResult.size());
        Assert.assertEquals(UUID_NAME_GENTIANA, matchResults.getExactResults().get(0).getTaxonNameUuid());
}

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testExcludeBasionymAuthors() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern, otherSymbols);

        nameCache = "Passiflora foetida var. hispida (Corda. ex Triana & Planch.) Killip ex Gleason";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, true, false, 0.0);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(1, matchResult.size());
        Assert.assertEquals(UUID_NAME_PASSIFLORAFO, matchResults.getExactResults().get(0).getTaxonNameUuid());
        Assert.assertEquals("basionym authors are excluded", "Passiflora foetida var. hispida (DC. ex Triana & Planch.) Killip ex Gleason", matchResult.get(0).getTitleCache());
        Double distance =matchResult.get(0).getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);

        nameCache = "Asterella lindenbergiana (Nees ex Corda) Lindb. ex Arnell";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, true, false, 0.0);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(1, matchResult.size());
        Assert.assertEquals(UUID_NAME_ASTERELLA, matchResults.getExactResults().get(0).getTaxonNameUuid());
        Assert.assertEquals("basionym authors are excluded", "Asterella lindenbergiana (Corda ex Nees) Lindb. ex Arnell", matchResult.get(0).getTitleCache());
}
    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testExcludeExAuthors() throws NameMatchingParserException {

        String nameCache;
        NameMatchingResult matchResults;
        List<SingleNameMatchingResult> matchResult;
        String pattern = "#.###";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat (pattern, otherSymbols);

        nameCache = "Passiflora bracteosa Linden & Planch. ex Triana & Planch.";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, true, true, 0.0);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(1, matchResult.size());
        Assert.assertEquals(UUID_NAME_PASSIFLORABR, matchResults.getExactResults().get(0).getTaxonNameUuid());
        Assert.assertEquals("ex authors are ignored", "Passiflora bracteosa Planch. & Linden ex Triana & Planch.", matchResult.get(0).getTitleCache());

        nameCache = "Passiflora foetida var. hispida (Corda. ex Triana & Planch.) Cuatrec. ex Gleason";
        matchResults = nameMatchingService.findMatchingNames(nameCache, true, false, true, 0.0);
        matchResult = matchResults.getExactResults();
        Assert.assertEquals(1, matchResult.size());
        Assert.assertEquals(UUID_NAME_PASSIFLORAFO, matchResults.getExactResults().get(0).getTaxonNameUuid());
        Assert.assertEquals("ex authors are ignored", "Passiflora foetida var. hispida (DC. ex Triana & Planch.) Killip ex Gleason", matchResult.get(0).getTitleCache());
        Double distance =matchResult.get(0).getDistance();
        String formmattedDouble = decimalFormat.format(distance);
        assertEquals("1", formmattedDouble);
}

    @Override
	public void createTestDataSet() throws FileNotFoundException {}
}