/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for testing the {@link ORCID} class.
 *
 * For ORCID syntax see also https://support.orcid.org/hc/en-us/articles/360006897674
 *
 * @author a.mueller
 * @since 08.11.2019
 */
public class OrcidTest {

    @Test
    public void testValidParser() {
        String validOrcid = "0000-0001-5000-0007";
        ORCID orcid = ORCID.fromString(validOrcid);
        Assert.assertEquals("https://orcid.org/0000-0001-5000-0007", orcid.asURI());
        Assert.assertEquals("7", orcid.checkDigit());

        validOrcid = "0000000150000007";
        orcid = ORCID.fromString(validOrcid);
        Assert.assertEquals("https://orcid.org/0000-0001-5000-0007", orcid.asURI());
        Assert.assertEquals("7", orcid.checkDigit());

        validOrcid = "orcid.org/0000-0001-5000-0007";
        orcid = ORCID.fromString(validOrcid);
        Assert.assertEquals("https://orcid.org/0000-0001-5000-0007", orcid.asURI());
        Assert.assertEquals("7", orcid.checkDigit());

        validOrcid = "https://orcid.org/0000-0001-5000-0007";
        orcid = ORCID.fromString(validOrcid);
        Assert.assertEquals("https://orcid.org/0000-0001-5000-0007", orcid.asURI());
        Assert.assertEquals("7", orcid.checkDigit());

        validOrcid = "http://orcid.org/0000-0001-5000-0015";
        orcid = ORCID.fromString(validOrcid);
        Assert.assertEquals("https://orcid.org/0000-0001-5000-0015", orcid.asURI());
        Assert.assertEquals("5", orcid.checkDigit());
    }

    @Test
    public void testParserFail() {
        String invalidOrcid = "a000-0001-5000-0007";  //no letters allowed
        testInvalid(invalidOrcid);
        invalidOrcid = "0000-0001-5000-0007-9999";  //exactly 4 number blocks needed
        testInvalid(invalidOrcid);
        invalidOrcid = "0000-0001-5000-0007-0008";  //checksum must be correct, for algorithm see https://support.orcid.org/hc/en-us/articles/360006897674-Structure-of-the-ORCID-Identifier
        testInvalid(invalidOrcid);
    }

    @Test
    public void testEquals() {
        String validOrcid = "0000-0001-5000-000";
        ORCID orcid1 = ORCID.fromString(validOrcid);
        validOrcid = "0000000150000007";
        ORCID orcid2 = ORCID.fromString(validOrcid);
        Assert.assertEquals("ORCIDs must be equal. Checksum should not make it different", orcid1, orcid2);
        validOrcid = "0000-0001-5000-001";
        ORCID orcid3 = ORCID.fromString(validOrcid);
        Assert.assertNotEquals("Different ORCIDs must not be equal", orcid1, orcid3);
    }

    @Test
    public void testAsURI() {

        String validOrcid = "0000-0001-5000-0007";
        ORCID orcid1 = ORCID.fromString(validOrcid);
        Assert.assertEquals(ORCID.HTTP_ORCID_ORG + validOrcid, orcid1.asURI());
    }


    private void testInvalid(String invalidOrcid) {
        try {
            ORCID.fromString(invalidOrcid);
            Assert.fail("ORCID should not be parsable: " + invalidOrcid);
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

}
