/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * @author a.mueller
 * @since 04.10.2016
 */
public class CdmPreDataChangeListenerTest /*extends CdmIntegrationTest*/ {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGenerateCaches() {
        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        CdmPreDataChangeListener.generateCaches(derivedUnit);
        derivedUnit.setProtectedTitleCache(true);
        String cache = derivedUnit.getTitleCache();
        System.out.println(cache);
    }

//    @Override
//    public void createTestDataSet() throws FileNotFoundException {}

}
