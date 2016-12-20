/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.value;

import java.net.URI;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import org.apache.log4j.Logger;

public class URIJSONValueProcessor implements JsonValueProcessor {
	
	private static final Logger logger = Logger.getLogger(URIJSONValueProcessor.class);

	public Object processArrayValue(Object obj, JsonConfig jsonConfig) {
		logger.debug("Processing URL");
		if(obj == null){
			return "";
		}
		URI uri = (URI) obj;
		return uri.toString();
	}

	public Object processObjectValue(String key, Object obj,
			JsonConfig jsonConfig) {
		logger.debug("Processing URL");
		if(obj == null){
			return "";
		}
		URI uri = (URI) obj;
		return uri.toString();
	};

}
