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

import eu.etaxonomy.cdm.test.integration.SecurityExceptionUtils;

/**
 * Security-enabled transactional Spring base for cdmlib-services
 * (Unitils {@code CdmTransactionalIntegrationTestWithSecurity} + services).
 */
@ContextConfiguration(
        locations = "classpath:/eu/etaxonomy/cdm/applicationContext-springServicesSecurityTest.xml",
        inheritLocations = false)
public abstract class CdmServicesTransactionalSpringIntegrationTestWithSecurity
        extends CdmTransactionalSpringIntegrationTest {

    public static RuntimeException findSecurityRuntimeException(Throwable exception) {
        return SecurityExceptionUtils.findSecurityRuntimeException(exception);
    }

    public static <T extends Throwable> T findThrowableOfTypeIn(Class<T> clazz, Throwable exception) {
        return SecurityExceptionUtils.findThrowableOfTypeIn(clazz, exception);
    }
}
