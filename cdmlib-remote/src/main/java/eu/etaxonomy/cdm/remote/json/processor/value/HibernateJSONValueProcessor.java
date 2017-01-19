/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.value;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.remote.json.processor.matcher.HibernateJSONValueProcessorMatcher;

/**
 * Used in conjunction with the {@link HibernateJSONValueProcessorMatcher} to unwrap
 * beans from hibernate proxies. Using this Value processor is essential for
 * properly detecting @Transient annotations which are not inherited by subclasses
 * like the proxies
 * 
 * @author a.kohlbecker
 */
public class HibernateJSONValueProcessor implements JsonValueProcessor {
	
	public static final Logger logger = Logger.getLogger(HibernateJSONValueProcessor.class);
	
//	public Object processArrayValue(Object object, JsonConfig jsonConfig) {
//		if(Hibernate.isInitialized(object)) {
//			log.debug("Processing array value " + object);
//			return JSONArray.fromObject(object,jsonConfig);
//		} else{
//		    log.debug("Collection is uninitialized, returning null");
//		    return JSONNull.getInstance();
//		}
//	}
	
	public Object processArrayValue(Object object, JsonConfig jsonConfig) {
		// usage of the InitializedHibernatePropertyFiler is expected !!!
		// retain default processing
		return JSONArray.fromObject(object,jsonConfig);
	}
		

	public Object processObjectValue(String propertyName, Object object, JsonConfig jsonConfig) {
		// deproxy
		Object target = HibernateProxyHelper.deproxy(object, Object.class);
		if(logger.isDebugEnabled()){
			logger.debug("deproxying object " + target);
		}
		return target;
		 
	}

}
