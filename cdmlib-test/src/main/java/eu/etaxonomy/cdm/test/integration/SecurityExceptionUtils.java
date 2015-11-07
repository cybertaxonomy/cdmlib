// $Id$
package eu.etaxonomy.cdm.test.integration;

import org.springframework.security.access.AccessDeniedException;

/**
 * This utility class helps finding security related exceptions in Throwables
 *
 * @author a.kohlbecker
 * @date Feb 11, 2014
 *
 */
public class SecurityExceptionUtils {

    public static Class<?> permissionDeniedExceptionClass;

    static {
        try {
            permissionDeniedExceptionClass = Class.forName("eu.etaxonomy.cdm.database.PermissionDeniedException");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException ("PermissionDeniedException class could not be found. Propably it moved to another folder", e);
        }
    }

    /**
     * Finds a nested RuntimeExceptions of the types {@link PermissionDeniedException}, {@link AccessDeniedException}
     * or returns null.
     * @param exception
     * @return
     */
    public static RuntimeException findSecurityRuntimeException(Throwable exception) {
        if( permissionDeniedExceptionClass.isInstance(exception) || AccessDeniedException.class.isInstance(exception) ){
            return (RuntimeException) exception;
        } else if(exception != null ){
            return findSecurityRuntimeException(exception.getCause());
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
    public static <T extends Throwable> T  findThrowableOfTypeIn(Class<T> clazz, Throwable exception) {
        if( permissionDeniedExceptionClass.isInstance(exception) ){
            return (T)exception;
        } else if(exception != null ){
            return findThrowableOfTypeIn(clazz, exception.getCause());
        }
        return null;
    }

}
