package eu.etaxonomy.cdm.persistence.permission;

import org.springframework.security.access.AccessDeniedException;

import eu.etaxonomy.cdm.database.PermissionDeniedException;

/**
 * This utility class helps finding security related exceptions in Throwables.
 *
 * Note: This class is more or less a clone of the same named class in cdmlib-test.
 * Any changes in either of these classes should be also done in the other class.
 * In future theses classes should be merged.
 *
 * @author a.kohlbecker
 * @author a.mueller
 * @since Jun 05, 2020
 */
public class SecurityExceptionUtils {

   //this is to decouple SecurityExceptionUtils from persistence and spring-security
    public static Class<?> permissionDeniedExceptionClass = PermissionDeniedException.class;
    public static Class<?> accessDeniedException = AccessDeniedException.class;

    /**
     * Finds a nested RuntimeExceptions of the types {@link PermissionDeniedException}, {@link AccessDeniedException}
     * or returns null.
     * @param exception
     * @return
     */
    public static RuntimeException findSecurityRuntimeException(Throwable exception) {
        if( permissionDeniedExceptionClass.isInstance(exception) || accessDeniedException.isInstance(exception) ){
            return (RuntimeException) exception;
        } else if(exception != null ){
            return findSecurityRuntimeException(exception.getCause());
        }
        return null;
    }


    /**
     * Find in the nested <code>exception</code> the exception of type <code>clazz</code>
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
