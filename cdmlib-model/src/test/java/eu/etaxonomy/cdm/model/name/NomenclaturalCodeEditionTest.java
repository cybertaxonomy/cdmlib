/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 23.07.2019
 *
 */
public class NomenclaturalCodeEditionTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition#getTitleCache()}.
     */
    @Test
    public void testGetTitleCache() {
        //no exception should be thrown and no title cache should be null
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            Assert.assertNotNull(edition.getTitleCache());
        }
        Assert.assertEquals("Shenzhen 2017", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getTitleCache());
        Assert.assertEquals("ICZN 1961", NomenclaturalCodeEdition.ICZN_1961.getTitleCache());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition#getCode()}.
     */
    @Test
    public void testGetCode() {
        //no exception should be thrown and no code should be null
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            Assert.assertNotNull(edition.getCode());
        }
        Assert.assertSame(NomenclaturalCode.ICNAFP, NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getCode());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition#getWikiDataUri()}.
     */
    @Test
    public void testGetWikiDataUri() {
        //no exception should be thrown
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            edition.getWikiDataUri();
        }
        Assert.assertEquals("https://www.wikidata.org/wiki/Q56701992", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getWikiDataUri().toString());
        Assert.assertEquals("As long as ICVCN_2011 code has no doi no exception should be thrown", null, NomenclaturalCodeEdition.ICVCN_2011.getWikiDataUri());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition#getDoi()}.
     */
    @Test
    public void testGetDoi() {
        //no exception should be thrown
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            edition.getDoi();
        }
        Assert.assertEquals("http://doi.org/10.12705/Code.2018", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getDoi().asURI().toString());
        Assert.assertEquals("As long as Tokyo code has no doi no exception should be thrown", null, NomenclaturalCodeEdition.ICN_1993_TOKYO.getDoi());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition#getKey()}.
     */
    @Test
    public void testGetKey() {
        //no exception should be thrown and no key should be null
        for (NomenclaturalCodeEdition edition: NomenclaturalCodeEdition.values()){
            Assert.assertNotNull(edition.getKey());
        }
        Assert.assertEquals("ICNAFP2017", NomenclaturalCodeEdition.ICN_2017_SHENZHEN.getKey());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition#getByKey(java.lang.String)}.
     */
    @Test
    public void testGetByKey() {
        Assert.assertSame(NomenclaturalCodeEdition.ICN_2017_SHENZHEN, NomenclaturalCodeEdition.getByKey("ICNAFP2017"));
    }

    @Test
    public void testForCode() {
        List<NomenclaturalCodeEdition> editions = NomenclaturalCodeEdition.forCode(NomenclaturalCode.ICNAFP);
        Assert.assertTrue(editions.contains(NomenclaturalCodeEdition.ICN_2011_MELBOURNE));
        Assert.assertEquals(5, editions.size());
        Assert.assertFalse(editions.contains(NomenclaturalCodeEdition.ICZN_1999));

        editions = NomenclaturalCodeEdition.forCode(NomenclaturalCode.NonViral);
        Assert.assertTrue(editions.contains(NomenclaturalCodeEdition.ICN_2011_MELBOURNE));
        Assert.assertTrue(editions.contains(NomenclaturalCodeEdition.ICZN_1999));
        Assert.assertEquals(15, editions.size());
        Assert.assertFalse(editions.contains(NomenclaturalCodeEdition.ICVCN_2018));

    }

}
