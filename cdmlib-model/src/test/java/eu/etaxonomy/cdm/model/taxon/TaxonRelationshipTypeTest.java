/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;

/**
 * @author a.mueller
 * @since 26.02.2019
 *
 */
public class TaxonRelationshipTypeTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (Language.ENGLISH() == null){
            new DefaultTermInitializer().initialize();
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRepresentations() {
        //these tests mostly test that UTF8 symbols have not bee manipulated in the csv files by encoding changes.
        //They are not meant for defining correct symbols over long time. Many of the symbols are not finally discussed.

        //MISAPPLIED_NAME_FOR
        TaxonRelationshipType type = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
        Assert.assertEquals(UTF8.EM_DASH.toString(), inverseAbbrev(type));
        Assert.assertEquals(UTF8.EM_DASH_DOUBLE.toString(), abbrev(type));

        //PRO PARTE MISAPPLIED_NAME_FOR
        type = TaxonRelationshipType.PRO_PARTE_MISAPPLIED_NAME_FOR();
        Assert.assertEquals(UTF8.EM_DASH + "(p.p.)", inverseAbbrev(type));
        Assert.assertEquals(UTF8.EM_DASH_DOUBLE + "(p.p.)", abbrev(type));

        type = TaxonRelationshipType.PARTIAL_MISAPPLIED_NAME_FOR();
        Assert.assertEquals(UTF8.EM_DASH + "(part.)", inverseAbbrev(type));
        Assert.assertEquals(UTF8.EM_DASH_DOUBLE + "(part.)", abbrev(type));

    }

    private String inverseAbbrev(TaxonRelationshipType relType) {
        return relType.getPreferredInverseRepresentation(null).getAbbreviatedLabel();
    }

    private String abbrev(TaxonRelationshipType relType) {
        return relType.getPreferredRepresentation((Language)null).getAbbreviatedLabel();
    }

}
