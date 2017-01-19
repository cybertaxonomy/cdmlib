/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.matcher;

import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.json.processors.JsonBeanProcessorMatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CGLibEnhancedBeanProcessorMatcher extends JsonBeanProcessorMatcher {

	private static Log log = LogFactory.getLog(CGLibEnhancedBeanProcessorMatcher.class);
	
	@Override
	public Object getMatch(Class target, Set set) {
		
		if (Enhancer.isEnhanced(target)) {
			log.debug("Found enhanced object of class " + target.getClass() + " returning " + target.getSuperclass());
			return DEFAULT.getMatch(target.getSuperclass(), set);
        }
        return DEFAULT.getMatch(target, set);
	}

}
