/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 23.07.2019
 */
public class NomenclaturalCodeEditionTest {

    @Test
    public void testGetTitleCache() {
        //no exception should be thrown and no title cache should be null
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            assertNotNull(edition.getTitleCache());
        }
        assertEquals("Shenzhen 2017", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getTitleCache());
        assertEquals("ICZN 1961", NomenclaturalCodeEdition.ICZN_1961.getTitleCache());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition#getCode()}.
     */
    @Test
    public void testGetCode() {
        //no exception should be thrown and no code should be null
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            assertNotNull(edition.getCode());
        }
        assertSame(NomenclaturalCode.ICNAFP, NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getCode());
    }

    @Test
    public void testGetWikiDataUri() {
        //no exception should be thrown
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            edition.getWikiDataUri();
        }
        assertEquals("https://www.wikidata.org/wiki/Q56701992", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getWikiDataUri().toString());
        assertEquals(null, NomenclaturalCodeEdition.ICVCN_2011.getWikiDataUri(), "As long as ICVCN_2011 code has no doi no exception should be thrown");
    }

    @Test
    public void testGetDoi() {
        //no exception should be thrown
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            edition.getDoi();
        }
        assertEquals("https://doi.org/10.12705/Code.2018", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getDoi().asURI().toString());
        assertEquals(null, NomenclaturalCodeEdition.ICN_1993_TOKYO.getDoi(), "As long as Tokyo code has no doi no exception should be thrown");
    }

    @Test
    public void testGetKey() {
        //no exception should be thrown and no key should be null
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            assertNotNull(edition.getKey());
        }
        assertEquals("ICNAFP2017", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getKey());
    }

    @Test
    public void testGetLabel() {
        assertEquals("Shenzhen 2017", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getLabel());
    }

    @Test
    public void testGetByKey() {
        assertSame(NomenclaturalCodeEdition.ICN_2017_SHENZHEN, NomenclaturalCodeEdition.getByKey("ICNAFP2017"));
    }

    @Test
    public void testForCode() {
        List<NomenclaturalCodeEdition> editions = NomenclaturalCodeEdition.forCode(NomenclaturalCode.ICNAFP);
        assertTrue(editions.contains(NomenclaturalCodeEdition.ICN_2011_MELBOURNE));
        assertEquals(20, editions.size());
        assertFalse(editions.contains(NomenclaturalCodeEdition.ICZN_1999));

        editions = NomenclaturalCodeEdition.forCode(NomenclaturalCode.NonViral);
        assertTrue(editions.contains(NomenclaturalCodeEdition.ICN_2011_MELBOURNE));
        assertTrue(editions.contains(NomenclaturalCodeEdition.ICZN_1999));
        assertEquals(30, editions.size());
        assertFalse(editions.contains(NomenclaturalCodeEdition.ICVCN_2018));

    }
}
