package eu.etaxonomy.cdm.test.integration;

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.unitils.spring.annotation.SpringApplicationContext;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-securityTest.xml")
public abstract class CdmTransactionalIntegrationTestWithSecurity extends  CdmTransactionalIntegrationTest {

    public static final Logger logger = Logger.getLogger(CdmTransactionalIntegrationTestWithSecurity.class);

    /**
     * Finds a nested RuntimeExceptions of the types {@link PermissionDeniedException}, {@link AccessDeniedException}
     * or returns null.
     * @param exception
     * @return
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
