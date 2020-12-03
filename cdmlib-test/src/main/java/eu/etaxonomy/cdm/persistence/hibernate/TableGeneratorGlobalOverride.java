/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Properties;

import org.springframework.stereotype.Component;

/**
 * This class allows to globally override id generation settings in hibernate
 *
 * @see: eu.etaxonomy.cdm.persistence.hibernate.TableGenerator
 *
 * @author a.kohlbecker
 * @since Feb 23, 2016
 */
//TODO  a copy of this class exists in cdmlib-persistence, see there for further information
//see also TableGenerator
@Component
class TableGeneratorGlobalOverride {

	public TableGeneratorGlobalOverride(){}

//	public static final ThreadLocal<Properties> threadLocalProperties = new ThreadLocal<Properties>();

	public static Properties properties;

	public static Properties getProperties() {
//		return threadLocalProperties.get();
		return TableGeneratorGlobalOverride.properties;
	}

	public void setProperties(Properties properties) {
//		threadLocalProperties.set(properties);
		TableGeneratorGlobalOverride.properties = properties;
	}
}
