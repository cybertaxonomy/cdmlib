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
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.persistence.dao.AbstractBeanInitializer;

/**
 * @author a.kohlbecker
 * @date 25.03.2009
 *
 */
@Component("defaultBeanInitializer")
public class HibernateBeanInitializer extends AbstractBeanInitializer{
	
	public static final Logger logger = Logger.getLogger(HibernateBeanInitializer.class);

	protected void initializeInstance(Object bean) {
		Hibernate.initialize(bean);
	}
	
}
