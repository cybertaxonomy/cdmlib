/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.util;

import net.sf.json.util.PropertyFilter;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

public class InitializedHibernatePropertyFilter implements PropertyFilter {

	private static final Logger logger = Logger.getLogger(InitializedHibernatePropertyFilter.class);

	/* (non-Javadoc)
	 * @see net.sf.json.util.PropertyFilter#apply(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public boolean apply(Object source, String name, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("Property " + name + " : Hibernate.isInitialized? " + Hibernate.isInitialized(value));
		}
		boolean skipProperty = !Hibernate.isInitialized(value) || name.equals("hibernateLazyInitializer");
		return skipProperty;
	}
}
