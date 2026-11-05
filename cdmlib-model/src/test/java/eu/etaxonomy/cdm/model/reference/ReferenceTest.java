/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 02.03.2017
 */
public class ReferenceTest {

    private Reference genericRef;

    @BeforeEach
    public void setUp() throws Exception {
        genericRef = ReferenceFactory.newGeneric();
    }

    @Test
    public void testOnlyProtectedTitleCache() {
        String titleCache = "Any reference title cache";
        genericRef.setTitleCache(titleCache, true);
        assertEquals(titleCache, genericRef.getTitleCache());
        assertEquals(titleCache, genericRef.getAbbrevTitleCache());
    }

    @Test
    public void testOnlyProtectedAbbrevTitleCache() {
        String titleCache = "A. ref. tit. ca.";
        genericRef.setAbbrevTitleCache(titleCache, true);
        assertEquals(titleCache, genericRef.getAbbrevTitleCache());
        assertEquals(titleCache, genericRef.getTitleCache());
    }

    /**
     * Test to avoid infinite recursion here.
     */
    @Test
    public void testTwoEmptyTitleCaches() {
        assertEquals("", genericRef.getAbbrevTitleCache());
        assertEquals("", genericRef.getTitleCache());
    }

}
