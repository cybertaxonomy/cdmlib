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

    private static final UUID UUID_NAME_NECTANDRA = UUID.fromString("0e16e411-e472-48ab-8b32-da9d3968092c");

    @SpringBeanByType
	private INameMatchingService nameMatchingService;

    @Test
    public void testTrimCommonChar() {

        String query = "Nectandra";
        String document = "Nectalisma";

        Assert.assertEquals("returns strings after trimming shared characters among query and document",
                "ndr", NameMatchingServiceImpl.trimCommonChar(query, document).split(" ")[0]);
        Assert.assertEquals("lism", NameMatchingServiceImpl.trimCommonChar(query, document).split(" ")[1]);
        Assert.assertEquals("Equal input should return empty result",
                "", NameMatchingServiceImpl.trimCommonChar(query, query) );
    }

	@Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
    public void testFindingMatchingNames () {

		String inputName;
        List<SingleNameMatchingResult> matchResult;
        SingleNameMatchingResult matchRes;

        //test genus only

     // if the query does not include an epithet (exact match)
        inputName = "Nectandra";
        matchResult = nameMatchingService.findMatchingNames(inputName, null);
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(33, (int)matchRes.getTaxonNameId());
        Assert.assertEquals(UUID_NAME_NECTANDRA, matchRes.getTaxonNameUuid());
        Assert.assertEquals("Distance should be 0", 0,(int) matchRes.getDistance());

        // if the query does not include an epithet (not exact match)
        inputName = "Nextondra";
        matchResult = nameMatchingService.findMatchingNames(inputName, null);
        Assert.assertEquals(2, matchResult.size());

        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectondra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(34, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("6c4464cc-fc2c-4907-9125-97715a798e0d", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nextandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("", matchRes.getSpecificEpithet());
        Assert.assertEquals(35, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("afdcdff3-8e8f-4296-aed2-2ad39c1b6bee", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(1,(int) matchRes.getDistance());

//        matchRes = matchResult.get(2);
//        Assert.assertEquals("Nectwsdra", matchRes.getGenusOrUninomial());
//        Assert.assertEquals("", matchRes.getSpecificEpithet());
//        Assert.assertEquals(35, (int)matchRes.getTaxonNameId());
//        Assert.assertEquals("afdcdff3-8e8f-4296-aed2-2ad39c1b6bee", matchRes.getTaxonNameUuid().toString());
//        Assert.assertEquals(3,(int) matchRes.getDistance());

        //SPECIES
        // if the query has an exact match on the DB, return the exact match
        inputName = "Nectandra magnoliifolia";
        matchResult = nameMatchingService.findMatchingNames(inputName, null);
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("magnoliifolia", matchRes.getSpecificEpithet());
        Assert.assertEquals(20, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("10989f63-c52f-4704-9574-2cc0676afe01", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        inputName = "Nectandra surinamensis";
        matchResult = nameMatchingService.findMatchingNames(inputName, null);
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("surinamensis", matchRes.getSpecificEpithet());
        Assert.assertEquals(27, (int) matchRes.getTaxonNameId());
        Assert.assertEquals("b184664e-798b-4b50-8807-2163a4de796c", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("surinamensis", matchRes.getSpecificEpithet());
        Assert.assertEquals(48, (int) matchRes.getTaxonNameId());
        Assert.assertEquals("73d9c6ef-4818-4b1c-b5c4-40b1ef5ed250", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        // if the query does not have an exact match on the DB, return the best matches
        inputName = "Nectendra nigre";
        matchResult = nameMatchingService.findMatchingNames(inputName, null);
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigra", matchRes.getSpecificEpithet());
        Assert.assertEquals(21, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("cae90b7a-5deb-4838-940f-f85bb685286e", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(2,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigrita", matchRes.getSpecificEpithet());
        Assert.assertEquals(22, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("8ad82243-b902-4eb6-990d-59774454b6e7", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(4,(int) matchRes.getDistance());

        inputName = "Bectendra nigri";
        matchResult = nameMatchingService.findMatchingNames(inputName, null);
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigra", matchRes.getSpecificEpithet());
        Assert.assertEquals(21, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("cae90b7a-5deb-4838-940f-f85bb685286e", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(3,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("nigrita", matchRes.getSpecificEpithet());
        Assert.assertEquals(22, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("8ad82243-b902-4eb6-990d-59774454b6e7", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(4,(int) matchRes.getDistance());

        // if the binomial query comprises genus and subgenus

//        	 for exact match return exact matching name
        inputName = "Nectandra subgen. Nectrina";
        matchResult = nameMatchingService.findMatchingNames(inputName, null);
        Assert.assertEquals(1, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Nectrina", matchRes.getInfraGenericEpithet());
        Assert.assertEquals(41, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("08ab2653-a4da-4c9b-8330-1bf4268cab88", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(0,(int) matchRes.getDistance());

        	// non exact match return the best results
        inputName = "Nectandra subgen. Nextrina";
        matchResult = nameMatchingService.findMatchingNames(inputName, null);
        Assert.assertEquals(2, matchResult.size());
        matchRes = matchResult.get(0);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Nectrina", matchRes.getInfraGenericEpithet());
        Assert.assertEquals(41, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("08ab2653-a4da-4c9b-8330-1bf4268cab88", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(1,(int) matchRes.getDistance());

        matchRes = matchResult.get(1);
        Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
        Assert.assertEquals("Nexxxina", matchRes.getInfraGenericEpithet());
        Assert.assertEquals(42, (int)matchRes.getTaxonNameId());
        Assert.assertEquals("c955a8ab-8501-421a-bfa3-5748237e8942", matchRes.getTaxonNameUuid().toString());
        Assert.assertEquals(2,(int) matchRes.getDistance());

		inputName = "Nectandra subg. Nectrina";
		matchResult = nameMatchingService.findMatchingNames(inputName, null);
		Assert.assertEquals(1, matchResult.size());
		matchRes = matchResult.get(0);
		Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
		Assert.assertEquals("Nectrina", matchRes.getInfraGenericEpithet());
		Assert.assertEquals(41, (int)matchRes.getTaxonNameId());
		Assert.assertEquals("08ab2653-a4da-4c9b-8330-1bf4268cab88", matchRes.getTaxonNameUuid().toString());
		Assert.assertEquals(0,(int) matchRes.getDistance());

  	// non exact match return the best results
		inputName = "Nectandra subg. Nextrina";
		matchResult = nameMatchingService.findMatchingNames(inputName, null);
		Assert.assertEquals(2, matchResult.size());
		matchRes = matchResult.get(0);
		Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
		Assert.assertEquals("Nectrina", matchRes.getInfraGenericEpithet());
		Assert.assertEquals(41, (int)matchRes.getTaxonNameId());
		Assert.assertEquals("08ab2653-a4da-4c9b-8330-1bf4268cab88", matchRes.getTaxonNameUuid().toString());
		Assert.assertEquals(1,(int) matchRes.getDistance());

		// if it is a trinomial name (genus species and subspecies)
		// exact match
		inputName = "Nectandra mollis subsp. laurel";
		matchResult = nameMatchingService.findMatchingNames(inputName, null);
		Assert.assertEquals(1, matchResult.size());
		matchRes = matchResult.get(0);
		Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
		Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
		Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
		Assert.assertEquals(55, (int)matchRes.getTaxonNameId());
		Assert.assertEquals("25296c78-f62b-4dfa-9cd1-813bc9d1d777", matchRes.getTaxonNameUuid().toString());
		Assert.assertEquals(0,(int) matchRes.getDistance());

		// if it is a trinomial name (genus species and subspecies)
		// exact match
		inputName = "Nectandra mollis var. laurol";
		matchResult = nameMatchingService.findMatchingNames(inputName, null);
		Assert.assertEquals(2, matchResult.size());
		matchRes = matchResult.get(0);
		Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
		Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
		Assert.assertEquals("laurel", matchRes.getInfraSpecificEpithet());
		Assert.assertEquals(55, (int)matchRes.getTaxonNameId());
		Assert.assertEquals("25296c78-f62b-4dfa-9cd1-813bc9d1d777", matchRes.getTaxonNameUuid().toString());
		Assert.assertEquals(1,(int) matchRes.getDistance());

		matchRes = matchResult.get(1);
		Assert.assertEquals("Nectandra", matchRes.getGenusOrUninomial());
		Assert.assertEquals("mollis", matchRes.getSpecificEpithet());
		Assert.assertEquals("laureli", matchRes.getInfraSpecificEpithet());
		Assert.assertEquals(58, (int)matchRes.getTaxonNameId());
		Assert.assertEquals("a598ab3f-b33b-4b4b-b237-d616fcb6b5b1", matchRes.getTaxonNameUuid().toString());
		Assert.assertEquals(2,(int) matchRes.getDistance());
    }


	@Override
	public void createTestDataSet() throws FileNotFoundException {}
}