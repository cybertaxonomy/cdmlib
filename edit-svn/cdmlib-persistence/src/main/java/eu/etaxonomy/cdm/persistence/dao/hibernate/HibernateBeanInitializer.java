// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.envers.entities.mapper.relation.lazy.proxy.CollectionProxy;
import org.hibernate.envers.entities.mapper.relation.lazy.proxy.MapProxy;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.persistence.dao.AbstractBeanInitializer;

/**
 * @author a.kohlbecker
 * @date 25.03.2009
 *
 */
//@Component("defaultBeanInitializer")
public class HibernateBeanInitializer extends AbstractBeanInitializer{
	
	public static final Logger logger = Logger.getLogger(HibernateBeanInitializer.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.AbstractBeanInitializer#initializeInstance(java.lang.Object)
	 */
	@Override
	protected Object initializeInstance(Object bean) {
		initialize(bean);
		return HibernateProxyHelperExtended.getProxyTarget(bean);
	}
	
	public static void initialize(Object proxy) throws HibernateException {
		if ( proxy == null ) {
			return;
		}
		else if ( proxy instanceof HibernateProxy ) {
			( ( HibernateProxy ) proxy ).getHibernateLazyInitializer().initialize();
		} else if ( proxy instanceof PersistentCollection ) {
			( ( PersistentCollection ) proxy ).forceInitialization();
		} else if(proxy instanceof CollectionProxy) {
			( ( CollectionProxy ) proxy ).isEmpty(); // checkInit is protected, unfortunately;
		} else if(proxy instanceof MapProxy) {
			( ( MapProxy ) proxy ).isEmpty(); // checkInit is protected, unfortunately;
		}
	}
	
}
