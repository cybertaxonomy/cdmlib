/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *  Test class for testing the {@link WikiDataItemId} class.
 *
 * For Wikidata item ID syntax see also https://www.wikidata.org/wiki/Wikidata:Identifiers
 *
 * @author muellera
 * @since 17.10.2024
 */
public class WikiDataItemIdTest {

    @Test
    public void testValidParser() {
        String validWikiDataItemId = "12345";
        WikiDataItemId wikiDataItemId = WikiDataItemId.fromString(validWikiDataItemId);
        assertEquals("http://www.wikidata.org/entity/Q12345", wikiDataItemId.asURI());

        validWikiDataItemId = "97399423534634";
        wikiDataItemId = WikiDataItemId.fromString(validWikiDataItemId);
        assertEquals("http://www.wikidata.org/entity/Q97399423534634", wikiDataItemId.asURI());

        validWikiDataItemId = "Q12345";
        wikiDataItemId = WikiDataItemId.fromString(validWikiDataItemId);
        assertEquals("http://www.wikidata.org/entity/Q12345", wikiDataItemId.asURI());

        validWikiDataItemId = "https://www.wikidata.org/entity/Q12345";
        wikiDataItemId = WikiDataItemId.fromString(validWikiDataItemId);
        assertEquals("http://www.wikidata.org/entity/Q12345", wikiDataItemId.asURI());

        validWikiDataItemId = "http://www.wikidata.org/entity/Q12345";
        wikiDataItemId = WikiDataItemId.fromString(validWikiDataItemId);
        assertEquals("http://www.wikidata.org/entity/Q12345", wikiDataItemId.asURI());
    }

    @Test
    public void testParserFail() {
        String invalidWikiDataItemId = "a12354";  //no letters allowed
        testInvalid(invalidWikiDataItemId);
        invalidWikiDataItemId = "12345-9874";  //no other characters allowed
        testInvalid(invalidWikiDataItemId);
    }

    @Test
    public void testEquals() {
        String validWikiDataItemId = "Q12345";
        WikiDataItemId wikiDataItemId1 = WikiDataItemId.fromString(validWikiDataItemId);
        validWikiDataItemId = "https://www.wikidata.org/entity/Q12345";
        WikiDataItemId wikiDataItemId2 = WikiDataItemId.fromString(validWikiDataItemId);
        assertEquals(wikiDataItemId1, wikiDataItemId2, "WikiIDs must be equal. Checksum should not make it different");
        validWikiDataItemId = "Q12346";
        WikiDataItemId wikiDataItemId3 = WikiDataItemId.fromString(validWikiDataItemId);
        assertNotEquals(wikiDataItemId1, wikiDataItemId3, "Different WikiIDs must not be equal");
    }

    @Test
    public void testAsURI() {

        String validWikiDataItemId = "Q23456";
        WikiDataItemId wikiDataItemId = WikiDataItemId.fromString(validWikiDataItemId);
        assertEquals(WikiDataItemId.HTTP_WIKIDATA_ORG + validWikiDataItemId, wikiDataItemId.asURI());
    }


    private void testInvalid(String invalidWikiDataItemId) {
        try {
            WikiDataItemId.fromString(invalidWikiDataItemId);
            fail("WikiDataItemId should not be parsable: " + invalidWikiDataItemId);
        } catch (IllegalArgumentException e) {
            //OK
        }
    }


}
