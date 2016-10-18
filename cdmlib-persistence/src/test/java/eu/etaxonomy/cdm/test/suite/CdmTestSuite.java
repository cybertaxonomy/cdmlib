/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.suite;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.etaxonomy.cdm.database.CdmDataSourceTest;
import eu.etaxonomy.cdm.database.DatabaseTypeEnumTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DefinedTermDaoImplTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImplTest;


@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
            //database
//			CdmDataSourceTest.class,
//			DatabaseEnumTest.class,

            //dao.common
//			CdmEntityDaoBaseTest.class,
//			DaoBaseTest.class,
            DefinedTermDaoImplTest.class,
            IdentifiableDaoBaseTest.class

            //dao.Taxon
//			TaxonDaoHibernateImplTest.class
        }
    )
public class CdmTestSuite {
    @SuppressWarnings("unused")
    private static final  Logger logger = Logger.getLogger(CdmTestSuite.class);

    // the class remains completely empty,
    // being used only as a holder for the above annotations

    //console test  //TODO test
    public static void consoleRun() {
        org.junit.runner.JUnitCore.runClasses(
                //database
                CdmDataSourceTest.class,
                DatabaseTypeEnumTest.class,
                //dao.common
                CdmEntityDaoBaseTest.class,
                DaoBaseTest.class,
                DefinedTermDaoImplTest.class,
                IdentifiableDaoBaseTest.class,
                //dao.Taxon
                TaxonDaoHibernateImplTest.class
                    );
    }
}