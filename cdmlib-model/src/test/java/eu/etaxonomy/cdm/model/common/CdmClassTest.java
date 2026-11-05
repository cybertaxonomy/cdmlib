/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 30.04.2020
 */
public class CdmClassTest {

    @Test
    public void testDescribedClasses() {
        assertEquals(3, CdmClass.DESCRIBED().size());
        assertTrue(CdmClass.DESCRIBED().contains(CdmClass.TAXON));
        assertFalse(CdmClass.DESCRIBED().contains(CdmClass.DISTRIBUTION));
    }

    @Test
    public void testDescribtionElementClasses() {
        assertEquals(8, CdmClass.DESCRIPTION_ELEMENT_SUB().size());
        assertTrue(CdmClass.DESCRIPTION_ELEMENT_SUB().contains(CdmClass.DISTRIBUTION));
        assertFalse(CdmClass.DESCRIPTION_ELEMENT_SUB().contains(CdmClass.TAXON));
    }

}
