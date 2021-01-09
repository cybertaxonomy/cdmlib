/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 23.04.2018
 */
public class TaxonInteractionTest extends EntityTestBase {

    private Taxon taxon;
    private TaxonInteraction taxonInteraction;

    @Before
    public void setUp() throws Exception {

        taxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Abies", null, "alba", null, null, null, null, null), null);
        taxonInteraction = TaxonInteraction.NewInstance();
        taxonInteraction.setTaxon2(taxon);
        LanguageString langString = LanguageString.NewInstance("TestTaxonInteraction", Language.ENGLISH());

        taxonInteraction.putDescription(langString);
    }

    @Test
    public void testClone(){
        TaxonInteraction clone = taxonInteraction.clone();
        assertNotEquals(clone.getDescription().get(Language.ENGLISH()), taxonInteraction.getDescription().get(Language.ENGLISH()));
        assertEquals(clone.getDescription(Language.ENGLISH()),taxonInteraction.getDescription(Language.ENGLISH()));
    }
}