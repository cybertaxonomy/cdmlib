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

import eu.etaxonomy.cdm.api.service.ClassificationServiceImplTest;
import eu.etaxonomy.cdm.api.service.TaxonServiceImplBusinessTest;
import eu.etaxonomy.cdm.api.service.TaxonServiceImplTest;


@RunWith(Suite.class)
@Suite.SuiteClasses(
		{
			ClassificationServiceImplTest.class,
			TaxonServiceImplBusinessTest.class,
			/*
			 *  ClassificationServiceImplTest.testTaxonNodeByNameComparator()
			 *  TaxonServiceImplBusinessTest.*
			 *
			 *  the above two tests in combination let
			 * 	TaxonServiceImplTest fail
			 *  with
			 * 	  org.hibernate.TransientObjectException:
			 *    object references an unsaved transient instance - save the transient instance before
			 *    flushing: eu.etaxonomy.cdm.model.name.Rank
			 */
			TaxonServiceImplTest.class

		}
	)
public class TestsShouldNotFailInSuite_s1 {

	// the class remains completely empty,
	// being used only as a holder for the above annotations

}
