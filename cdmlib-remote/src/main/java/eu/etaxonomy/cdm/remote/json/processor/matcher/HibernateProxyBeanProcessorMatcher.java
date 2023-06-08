/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.json.processor.matcher;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;

import net.sf.json.processors.JsonBeanProcessorMatcher;

/**
 * @author a.kohlbecker
 */
public class HibernateProxyBeanProcessorMatcher extends
		JsonBeanProcessorMatcher {

    private static final Logger logger = LogManager.getLogger();

	@Override
	public Object getMatch(Class target, Set set) {

		if (HibernateProxy.class.isAssignableFrom(target)) {
		    if (logger.isDebugEnabled()) {
		        logger.debug("Found HibernateProxy object of class " + target.getClass() + " returning " + HibernateProxy.class);
		    }
			return HibernateProxy.class;
        }

        return DEFAULT.getMatch(target, set);
	}
}