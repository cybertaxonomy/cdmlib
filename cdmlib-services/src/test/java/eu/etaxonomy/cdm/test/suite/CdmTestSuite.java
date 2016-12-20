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
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.etaxonomy.cdm.api.service.TaxonServiceImplTest;
import eu.etaxonomy.cdm.api.service.TermServiceImplTest;
import eu.etaxonomy.cdm.test.integration.TermLoaderIntegrationTest;


@Ignore
@RunWith(Suite.class)
@Suite.SuiteClasses(
		{
			TaxonServiceImplTest.class
			,TermServiceImplTest.class
			,TermLoaderIntegrationTest.class
			//,CreateDataTest.class
		}
	)
public class CdmTestSuite {
	static Logger logger = Logger.getLogger(CdmTestSuite.class);

	// the class remains completely empty,
	// being used only as a holder for the above annotations

	//console test  //TODO test
	public static void consoleRun() {
		org.junit.runner.JUnitCore.runClasses(
				TaxonServiceImplTest.class
				,TermServiceImplTest.class
				,TermLoaderIntegrationTest.class
				//,CreateDataTest.class
			);
	}
}
