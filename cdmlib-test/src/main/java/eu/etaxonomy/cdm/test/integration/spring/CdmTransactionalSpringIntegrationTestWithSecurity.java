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
 * Security-enabled transactional Spring integration tests
 * (Unitils {@code CdmTransactionalIntegrationTestWithSecurity} replacement).
 */
@ContextConfiguration("classpath:/eu/etaxonomy/cdm/applicationContext-springSecurityTest.xml")
public abstract class CdmTransactionalSpringIntegrationTestWithSecurity
        extends CdmTransactionalSpringIntegrationTest {

    public static RuntimeException findSecurityRuntimeException(Throwable exception) {
        return SecurityExceptionUtils.findSecurityRuntimeException(exception);
    }

    public static <T extends Throwable> T findThrowableOfTypeIn(Class<T> clazz, Throwable exception) {
        return SecurityExceptionUtils.findThrowableOfTypeIn(clazz, exception);
    }
}
