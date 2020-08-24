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

/**
 * This utility class helps finding security related exceptions in Throwables
 *
 * Note: This class is more or less a clone of the same named class in cdmlib-persistence.
 * Any changes in either of these classes should be also done in the other class.
 * In future theses classes should be merged. But this requires a change in dependencies.
 *
 * @author a.kohlbecker
 * @since Feb 11, 2014
 */
public class SecurityExceptionUtils {

   //this is to decouple SecurityExceptionUtils from persistence and spring-security
    public static Class<?> permissionDeniedExceptionClass;
    public static Class<?> accessDeniedException;

    static {
        try {
            //PermissionDeniedException is not available here yet, therefore we use Class.forName
            permissionDeniedExceptionClass = Class.forName("eu.etaxonomy.cdm.database.PermissionDeniedException");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException ("PermissionDeniedException class could not be found. Propably it moved to another folder", e);
        }
        try {
            accessDeniedException = Class.forName("org.springframework.security.access.AccessDeniedException");
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
