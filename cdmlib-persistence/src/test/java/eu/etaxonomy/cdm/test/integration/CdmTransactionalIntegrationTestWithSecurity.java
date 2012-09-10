package eu.etaxonomy.cdm.test.integration;

import org.junit.After;
import org.junit.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitils.spring.annotation.SpringApplicationContext;

import eu.etaxonomy.cdm.database.EvaluationFailedException;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-securityTest.xml")
public abstract class CdmTransactionalIntegrationTestWithSecurity extends  CdmTransactionalIntegrationTest {

    /**
     * Finds a nested {@link EvaluationFailedException} or returns <code>null</code>
     * @param exception
     * @return
     */
    public static EvaluationFailedException findEvaluationFailedExceptionIn(Throwable exception) {
        if( EvaluationFailedException.class.isInstance(exception) ){
            return (EvaluationFailedException)exception;
        } else if(exception != null ){
            return findEvaluationFailedExceptionIn(exception.getCause());
        }
        return null;
    }

    /**
     * find in the nested <code>exception</code> the exception of type <code>clazz</code>
     * or returns <code>null</code> if no such exception is found.
     *
     * @param clazz
     * @param exception the nested <code>Throwable</code> to search in.
     * @return
     */
    public <T extends Throwable> T  findThrowableOfTypeIn(Class<T> clazz, Throwable exception) {
        if( EvaluationFailedException.class.isInstance(exception) ){
            return (T)exception;
        } else if(exception != null ){
            return findThrowableOfTypeIn(clazz, exception.getCause());
        }
        return null;
    }

}
