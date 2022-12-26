/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration;

import org.springframework.security.access.AccessDeniedException;
import org.unitils.spring.annotation.SpringApplicationContext;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-securityTest.xml")
public abstract class CdmTransactionalIntegrationTestWithSecurity extends CdmTransactionalIntegrationTest {

    /**
     * Finds a nested RuntimeExceptions of the types {@link PermissionDeniedException}, {@link AccessDeniedException}
     * or returns null.
     */
    public static RuntimeException findSecurityRuntimeException(Throwable exception) {
        return SecurityExceptionUtils.findSecurityRuntimeException(exception);
    }

    /**
     * find in the nested <code>exception</code> the exception of type <code>clazz</code>
     * or returns <code>null</code> if no such exception is found.
     *
     * @param clazz
     * @param exception the nested <code>Throwable</code> to search in.
     * @return
     */
    public static <T extends Throwable> T  findThrowableOfTypeIn(Class<T> clazz, Throwable exception) {
        return SecurityExceptionUtils.findThrowableOfTypeIn(clazz, exception);
    }
}