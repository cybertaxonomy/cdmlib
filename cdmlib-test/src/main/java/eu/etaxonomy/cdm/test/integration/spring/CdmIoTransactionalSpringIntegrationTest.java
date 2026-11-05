/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import org.springframework.test.context.ContextConfiguration;

/**
 * Spring Test base for cdmlib-io integration tests
 * (loads io/ext beans on top of {@link CdmServicesTransactionalSpringIntegrationTest}).
 */
@ContextConfiguration(locations = "classpath:/eu/etaxonomy/cdm/applicationContext-springServicesIoTest.xml",
        inheritLocations = false)
public abstract class CdmIoTransactionalSpringIntegrationTest
        extends CdmTransactionalSpringIntegrationTest {
}
