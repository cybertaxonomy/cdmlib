/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * @author a.kohlbecker
 *
 */
public class CdmBaseBeanProcessor implements JsonBeanProcessor {

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonBeanProcessor#processBean(java.lang.Object, net.sf.json.JsonConfig)
	 */
	public JSONObject processBean(Object bean, JsonConfig jsonConfig) {
		JSONObject json = new JSONObject();
		//FIXME ... implement
		return null;
	}

}
