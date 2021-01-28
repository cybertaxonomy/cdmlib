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
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 23.04.2018
 */
public class IndividualsAssociationTest extends EntityTestBase {

    private IndividualsAssociation indAssociation;

    @Before
    public void setUp() throws Exception {
        indAssociation = IndividualsAssociation.NewInstance();

        DerivedUnit associatedSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
        associatedSpecimen.setIndividualCount("2");
        indAssociation.setAssociatedSpecimenOrObservation(associatedSpecimen);

        LanguageString langString = LanguageString.NewInstance("Test", Language.ENGLISH());
        indAssociation.putDescription(langString);
    }

    @Test
    public void testClone() {
        IndividualsAssociation clone = indAssociation.clone();
        assertEquals(clone.getFeature(), indAssociation.getFeature());
        assertNotSame(clone.getDescription().get(Language.ENGLISH()), indAssociation.getDescription().get(Language.ENGLISH()));
    }
}