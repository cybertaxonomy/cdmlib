// $Id$
/**
 *
 */
package eu.etaxonomy.cdm.remote.json.processor.bean;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import net.sf.json.CycleSetAcess;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * @author a.kohlbecker
 */
public class HibernateProxyBeanProcessor extends CycleSetAcess implements JsonBeanProcessor {

	public static final Logger logger = Logger.getLogger(HibernateProxyBeanProcessor.class);

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
