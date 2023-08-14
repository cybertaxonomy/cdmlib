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

import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author andreabee90
 * @since 11.07.2023
 */
public class NameMatchingServiceImplTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	private INameMatchingService nameMatchingService;

    @Test
//    public void testTrimCommonChar() {
//
//        String query = "Nectandra";
//        String document = "Nectalisma";
//
//        Assert.assertEquals("returns strings after trimming shared characters among query and document",
//                "ndr", NameMatchingServiceImpl.trimCommonChar(query, document).split(" ")[0]);
//        Assert.assertEquals("lism", NameMatchingServiceImpl.trimCommonChar(query, document).split(" ")[1]);
//        Assert.assertEquals("Equal input should return empty result",
//                "", NameMatchingServiceImpl.trimCommonChar(query, query) );
//    }
//
//	@Test
//    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "NameMatchingServiceImplTest.testFindMatchingNames.xml")
//    public void testFindingMatchingNames () {
//
//		String inputName;
//        List<DoubleResult<NameMatchingParts, Integer>> matchResult;
//        DoubleResult<NameMatchingParts, Integer> matchRes;
//
//        // if the query has an exact match on the DB, return the exact match
//        inputName = "Nectandra magnoliifolia";
//        matchResult = nameMatchingService.findMatchingNames(inputName, null, null);
////        Assert.assertEquals(1, matchResult.size());
//        matchRes = matchResult.get(0);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("magnoliifolia", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(20, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("10989f63-c52f-4704-9574-2cc0676afe01", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(0,(int) matchRes.getSecondResult());
//
//        inputName = "Nectandra surinamensis";
//        matchResult = nameMatchingService.findMatchingNames(inputName, null, null);
//        Assert.assertEquals(2, matchResult.size());
//        matchRes = matchResult.get(0);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("surinamensis", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(27, (int) matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("b184664e-798b-4b50-8807-2163a4de796c", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(0,(int) matchRes.getSecondResult());
//
//        matchRes = matchResult.get(1);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("surinamensis", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(28, (int) matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("b9c8c3ba-bc78-4229-ae7d-b3f7bf23ec85", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(0,(int) matchRes.getSecondResult());
//
//        // if the query does not have an exact match on the DB, return the best matches
//        inputName = "Nectendra nigre";
//        matchResult = nameMatchingService.findMatchingNames(inputName, null, null);
//        Assert.assertEquals(2, matchResult.size());
//        matchRes = matchResult.get(0);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("nigra", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(21, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("cae90b7a-5deb-4838-940f-f85bb685286e", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(2,(int) matchRes.getSecondResult());
//
//        matchRes = matchResult.get(1);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("nigrita", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(22, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("8ad82243-b902-4eb6-990d-59774454b6e7", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(4,(int) matchRes.getSecondResult());
//
//        inputName = "Bectendra nigri";
//        matchResult = nameMatchingService.findMatchingNames(inputName, null, null);
//        Assert.assertEquals(2, matchResult.size());
//        matchRes = matchResult.get(0);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("nigra", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(21, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("cae90b7a-5deb-4838-940f-f85bb685286e", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(3,(int) matchRes.getSecondResult());
//
//        matchRes = matchResult.get(1);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("nigrita", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(22, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("8ad82243-b902-4eb6-990d-59774454b6e7", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(4,(int) matchRes.getSecondResult());
//
//        // if the query does not include an epithet (exact match)
//        inputName = "Nectandra";
//        matchResult = nameMatchingService.findMatchingNames(inputName, null, null);
//        Assert.assertEquals(1, matchResult.size());
//        matchRes = matchResult.get(0);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(33, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("0e16e411-e472-48ab-8b32-da9d3968092c", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(0,(int) matchRes.getSecondResult());
//
//        // if the query does not include an epithet (not exact match)
//        inputName = "Nextondra";
//        matchResult = nameMatchingService.findMatchingNames(inputName, null, null);
//        Assert.assertEquals(3, matchResult.size());
//
//        matchRes = matchResult.get(0);
//        Assert.assertEquals("Nectondra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(34, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("6c4464cc-fc2c-4907-9125-97715a798e0d", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(1,(int) matchRes.getSecondResult());
//
//        matchRes = matchResult.get(1);
//        Assert.assertEquals("Nectandra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(33, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("0e16e411-e472-48ab-8b32-da9d3968092c", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(2,(int) matchRes.getSecondResult());
//
//        matchRes = matchResult.get(2);
//        Assert.assertEquals("Nectwsdra", matchRes.getFirstResult().getGenusOrUninomial());
//        Assert.assertEquals("", matchRes.getFirstResult().getSpecificEpithet());
//        Assert.assertEquals(35, (int)matchRes.getFirstResult().getTaxonNameId());
//        Assert.assertEquals("afdcdff3-8e8f-4296-aed2-2ad39c1b6bee", matchRes.getFirstResult().getTaxonNameUuid().toString());
//        Assert.assertEquals(3,(int) matchRes.getSecondResult());
//    }

	@Override
	public void createTestDataSet() throws FileNotFoundException {}
}