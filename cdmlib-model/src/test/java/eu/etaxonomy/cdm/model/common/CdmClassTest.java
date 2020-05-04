/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 30.04.2020
 */
public class CdmClassTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDescribedClasses() {
        Assert.assertEquals(3, CdmClass.DESCRIBED().size());
        Assert.assertTrue(CdmClass.DESCRIBED().contains(CdmClass.TAXON));
        Assert.assertFalse(CdmClass.DESCRIBED().contains(CdmClass.DISTRIBUTION));
    }

    @Test
    public void testDescribtionElementClasses() {
        Assert.assertEquals(8, CdmClass.DESCRIPTION_ELEMENT_SUB().size());
        Assert.assertTrue(CdmClass.DESCRIPTION_ELEMENT_SUB().contains(CdmClass.DISTRIBUTION));
        Assert.assertFalse(CdmClass.DESCRIPTION_ELEMENT_SUB().contains(CdmClass.TAXON));
    }

}
