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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author a.mueller
 * @created 03.03.2009
 */
public class HibernateProxyHelper {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(HibernateProxyHelper.class);


	// ************************** Hibernate proxies *******************/
	/**
	 * Deproxy and cast the given object to the given class.
	 * If clazz is <code>null</code>. If object is not an instance of HibernateProxy no
	 * deproxy is performed.
	 *
	 * @param object
	 * @param clazz
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
	  * Unwrap the target instance from the proxy if possible otrherwise return null.
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
