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



@RunWith(Suite.class)
@Suite.SuiteClasses(
		{
		    eu.etaxonomy.cdm.api.service.TransmissionEngineDistributionTest.class,
		    eu.etaxonomy.cdm.api.service.UserAndGroupServiceImplTest.class,
		    eu.etaxonomy.cdm.api.utility.DerivedUnitConverterIntegrationTest.class
		    /*
		     * all test in DerivedUnitConverterIntegrationTest where failing due to a
		     *
		     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		     * org.springframework.transaction.IllegalTransactionStateException:
		     * Pre-bound JDBC Connection found! HibernateTransactionManager does
		     * not support running within DataSourceTransactionManager if told to
		     * manage the DataSource itself. It is recommended to use a single
		     * HibernateTransactionManager for all transactions on a single
		     * DataSource, no matter whether Hibernate or JDBC access
             * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		     */
		}
	)
public class TestsShouldNotFailInSuite_s5 {

	// the class remains completely empty,
	// being used only as a holder for the above annotations

}
