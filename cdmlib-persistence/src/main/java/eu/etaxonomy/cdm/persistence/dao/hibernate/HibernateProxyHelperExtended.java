/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.hibernate;

import org.hibernate.envers.entities.mapper.relation.lazy.proxy.MapProxy;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * TODO merge with {@link HibernateProxyHelper}
 * 
 * This class extends the {@link org.hibernate.proxy.HibernateProxyHelper}. 
 * Since {@link org.hibernate.proxy.HibernateProxyHelper} is final the {@link #getClassWithoutInitializingProxy(Object)} 
 * is repeated here for convenience.
 * 
 * @author a.kohlbecker
 */
public class HibernateProxyHelperExtended {
	
	/**
	 * Get the class of an instance or the underlying class
	 * of a proxy (without initializing the proxy!). It is
	 * almost always better to use the entity name!
	 * 
	 */
	public static Class getClassWithoutInitializingProxy(Object object) {
		if (object instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy) object;
			LazyInitializer li = proxy.getHibernateLazyInitializer();
			return li.getPersistentClass();
		}
		else {
			return object.getClass();
		}
	}
	
	/**
	 * Unwrap the target instance from the proxy.
	 */
	public static Object getProxyTarget(Object object){
		if(object instanceof HibernateProxy) {
			LazyInitializer lazyInitializer = ((HibernateProxy)object).getHibernateLazyInitializer();
			return lazyInitializer.getImplementation();
		} else {
			return object;
		}
	}

}
