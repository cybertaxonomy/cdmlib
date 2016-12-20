/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeTest;
import eu.etaxonomy.cdm.api.service.NameServiceImplTest;



@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
            DerivedUnitFacadeTest.class,
            NameServiceImplTest.class
        }
    )
public class TestsShouldNotFailInSuite_s2 {

    // the class remains completely empty,
    // being used only as a holder for the above annotations

}
