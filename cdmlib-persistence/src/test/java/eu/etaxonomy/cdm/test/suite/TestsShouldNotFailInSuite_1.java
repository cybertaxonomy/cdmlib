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

import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.term.DefinedTermDaoImplTest;


@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
            DefinedTermDaoImplTest.class,
            IdentifiableDaoBaseTest.class
            /* FIXED:
             * eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException:
             *    IdentifiableDaoBase.findByTitle(String queryString, CdmBase sessionObject)
             * 	  at eu.etaxonomy.cdm.persistence.dao.hibernate.common.VersionableDaoBase.checkNotInPriorView(VersionableDaoBase.java:63)
             */
        }
    )
public class TestsShouldNotFailInSuite_1 {

    // the class remains completely empty,
    // being used only as a holder for the above annotations

}