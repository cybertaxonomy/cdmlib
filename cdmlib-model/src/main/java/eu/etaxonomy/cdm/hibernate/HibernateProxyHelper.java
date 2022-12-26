/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.proxy.HibernateProxy;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 03.03.2009
 */
public class HibernateProxyHelper {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	// ************************** Hibernate proxies *******************/
	/**
	 * Deproxy and cast the given object to the given class.
	 * clazz must not be <code>null</code> if object is instance of {@link HibernateProxy}.
	 * If object is not an instance of HibernateProxy no deproxy is performed.
	 *
     * Note AM (2022-06-16): maybe for pure casting this method is not reqired anymore and also
     *       deproxing might be obsolete in most cases since the current bytecode
     *       provider "bytebuddy" probably casts and handles proxies correctly.
     *
     * @see CdmBase#deproxy(Object, Class) for further information
     *
	 * @param object the object to cast
	 * @param clazz the class to cast to
	 * @return the casted and deproxied object
	 * @throws ClassCastException
	 */
	public static <T> T deproxy(Object object, Class<T> clazz) throws ClassCastException {
	     if (object instanceof HibernateProxy) {
	         object = ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
	         return clazz.cast(object);
	     }
	     if (clazz == null){
	         return (T)object;
	     }else {
	         return clazz.cast(object);
	     }
	 }

	 /**
	  * Unwrap the target instance from the proxy.
	  *
	  * @throws org.hibernate.HibernateException
	  */
	 public static <T> T deproxy(T entity){
	     if (entity == null){
	         return null;
	     }
	     if(entity instanceof HibernateProxy) {
	         Hibernate.initialize(entity);
	         entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
	     }
	     return entity;
	}

	 /**
	  * Unwrap the target instance from the proxy if possible otherwise return null.
	  *
	  * @param entity
	  * @return the deproxied entity or null in case it was not initialized.
	  */
	 public static <T> T deproxyOrNull(T entity){
	     try{
	         return deproxy(entity);
	     } catch (HibernateException e) {
            return null;
        }
	 }

	/**
	 * Tests if the entity or hibernate proxy <code>object</code> is assignable from the type
	 * defined as parameter <code>clazz</code>.
	 */
	public static boolean isInstanceOf(Object object, Class clazz) throws ClassCastException {
	     if (clazz == null || object == null){
	    	 return false;
	     }
		 if (object instanceof HibernateProxy) {
	    	 Object impl =  ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
	         Class<?> implClass = impl.getClass();
	         return clazz.isAssignableFrom(implClass);
	     } else {
	         return clazz.isAssignableFrom(object.getClass());
	     }
	 }

	public static Serializable getIdentifierOf(Object object) {
        if (object instanceof HibernateProxy) {
            return  ((HibernateProxy) object).getHibernateLazyInitializer().getIdentifier();
        } else {
            throw new ClassCastException("Cannot cast the given Object to a known Hibernate proxy.");
        }
    }

	/**
	 * Get the class of an instance or the underlying class
	 * of a proxy (without initializing the proxy!). It is
	 * almost always better to use the entity name!
	 *
	 * delegates calls to {@link org.hibernate.proxy.HibernateProxyHelper}
	 */
	public static Class getClassWithoutInitializingProxy(Object object) {
		return org.hibernate.proxy.HibernateProxyHelper.getClassWithoutInitializingProxy(object);
	}
}
