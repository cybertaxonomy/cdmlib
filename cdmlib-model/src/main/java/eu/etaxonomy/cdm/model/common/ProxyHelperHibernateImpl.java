/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author a.mueller
 * @created 03.03.2009
 * @version 1.0
 */
public class ProxyHelperHibernateImpl implements IProxyHelper {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ProxyHelperHibernateImpl.class);
	
	
	// ************************** Hibernate proxies *******************/
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IProxyHelper#deproxy(java.lang.Object, java.lang.Class)
	 */
	 public <T> T deproxy(Object object, Class<T> clazz) throws ClassCastException {
	     if (object instanceof HibernateProxy) {
	         return clazz.cast(((HibernateProxy) object).getHibernateLazyInitializer().getImplementation());
	     } else {
	         return clazz.cast(object);
	     }
	 }
	        
	 /* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IProxyHelper#isInstanceOf(java.lang.Object, java.lang.Class)
	 */
	public boolean isInstanceOf(Object object, Class clazz) throws ClassCastException {
	     if (clazz == null){
	    	 return false;
	     }
		 if (object instanceof HibernateProxy) {
	    	 Object impl =  ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
	         Class implClass = impl.getClass();
	         return clazz.isAssignableFrom(implClass);
	     } else {
	         return clazz.isAssignableFrom(object.getClass());
	     }
	 }
}
