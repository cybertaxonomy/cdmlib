/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import net.sf.json.CycleSetAccess;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * @author a.kohlbecker
 */
public class HibernateProxyBeanProcessor extends CycleSetAccess implements JsonBeanProcessor {

	public static final Logger logger = LogManager.getLogger(HibernateProxyBeanProcessor.class);

	@Override
    public JSONObject processBean(Object bean, JsonConfig jsonConfig) {
		Object target = HibernateProxyHelper.deproxy(bean, Object.class);
		if(logger.isDebugEnabled()){
			logger.debug("deproxying object " + target);
		}
		removeFromCycleSet(target);
		return JSONObject.fromObject(target, jsonConfig);
	}
}
