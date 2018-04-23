/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author a.mueller
 * @since 02.03.2017
 *
 */
public class ReferenceTest {

    private Reference genericRef;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        genericRef = ReferenceFactory.newGeneric();
    }

    @Test
    public void testOnlyProtectedTitleCache() {
        String titleCache = "Any reference title cache";
        genericRef.setTitleCache(titleCache, true);
        Assert.assertEquals(titleCache, genericRef.getTitleCache());
        Assert.assertEquals(titleCache, genericRef.getAbbrevTitleCache());
    }

    @Test
    public void testOnlyProtectedAbbrevTitleCache() {
        String titleCache = "A. ref. tit. ca.";
        genericRef.setAbbrevTitleCache(titleCache, true);
        Assert.assertEquals(titleCache, genericRef.getAbbrevTitleCache());
        Assert.assertEquals(titleCache, genericRef.getTitleCache());
    }

    /**
     * Test to avoid infinite recursion here.
     */
    @Test
    public void testTwoEmptyTitleCaches() {
        Assert.assertEquals("", genericRef.getAbbrevTitleCache());
        Assert.assertEquals("", genericRef.getTitleCache());
    }

}
