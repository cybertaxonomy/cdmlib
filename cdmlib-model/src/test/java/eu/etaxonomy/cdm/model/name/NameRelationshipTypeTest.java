/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * Testclass for {@link NameRelationshipType}.
 *
 * @author a.mueller
 * @since 20.11.2011
 */
public class NameRelationshipTypeTest extends EntityTestBase {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NameRelationshipTypeTest.class);

    @Test
    public void testNomenclaturalStanding() {
        assertTrue(NameRelationshipType.CONSERVED_AGAINST().isValidExplicit());
        assertTrue(NameRelationshipType.CONSERVED_AGAINST().isDesignationOnlyInverse());
    }
}
