/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.json.processor.matcher;

import java.util.Set;

import net.sf.json.processors.JsonValueProcessorMatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.proxy.HibernateProxy;


/**
 * 
 * @author a.kohlbecker
 */
public class HibernateJSONValueProcessorMatcher extends JsonValueProcessorMatcher {
	
	private static Log log = LogFactory.getLog(HibernateJSONValueProcessorMatcher.class);

	@Override
	public Object getMatch(Class target, Set matches) {
		if (HibernateProxy.class.isAssignableFrom(target)) {
            log.debug("Found HibernateProxy " + target.getName());           
            return HibernateProxy.class;
        }
		return DEFAULT.getMatch(target, matches);
	}

}
