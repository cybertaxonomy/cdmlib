/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.suite;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategyTest;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeFieldObservationCacheStrategyTest;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeTest;
import eu.etaxonomy.cdm.api.service.ClassificationServiceImplTest;
import eu.etaxonomy.cdm.api.service.DescriptionServiceImplTest;
import eu.etaxonomy.cdm.api.service.IdentifiableServiceBaseTest;
import eu.etaxonomy.cdm.api.service.NameServiceImplTest;
import eu.etaxonomy.cdm.api.service.NaturalLanguageGeneratorTest;
import eu.etaxonomy.cdm.api.service.TaxonNodeServiceImplTest;
import eu.etaxonomy.cdm.api.service.TaxonServiceImplBusinessTest;
import eu.etaxonomy.cdm.api.service.TaxonServiceImplTest;
import eu.etaxonomy.cdm.api.service.TaxonServiceSearchTest;
import eu.etaxonomy.cdm.api.service.TermServiceImplTest;
import eu.etaxonomy.cdm.api.service.lsid.LSIDAuthorityServiceTest;
import eu.etaxonomy.cdm.api.service.lsid.LSIDDataServiceTest;
import eu.etaxonomy.cdm.api.service.lsid.LSIDMetadataServiceTest;
import eu.etaxonomy.cdm.api.service.pager.PagerTest;
import eu.etaxonomy.cdm.strategy.generate.IdentificationKeyGeneratorTest;
import eu.etaxonomy.cdm.test.integration.CreateDataTest;
import eu.etaxonomy.cdm.test.integration.TermLoaderIntegrationTest;
import eu.etaxonomy.cdm.validation.ValidationTest;


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