// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.json.processor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.processors.JsonVerifier;
import net.sf.json.util.PropertyFilter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.AbstractBeanInitializer;

/**
 * @author a.kohlbecker
 * @date 30.03.2009
 *
 */
public abstract class AbstractCdmBeanProcessor<T extends CdmBase>{
	
	public static final Logger logger = Logger.getLogger(AbstractCdmBeanProcessor.class);
	
	public final JSONObject processBean(Object bean, JsonConfig jsonConfig) {

		if(logger.isDebugEnabled()){
			logger.debug("processing " + bean);
		}
		
		JSONObject json =  new JSONObject();
		
		Set<PropertyDescriptor> props = AbstractBeanInitializer.getProperties(bean, true, true);
		PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
		for(PropertyDescriptor prop: props){
			String key = prop.getName();
			if(getIgnorePropNames().contains(key)){
				if(logger.isDebugEnabled()){
					logger.debug("skipping excluded property " + key);
					continue;
				}
			}
			
			try {
				// ------ reusing snippet from JSONOnbject._fromBean()
				Class type = prop.getPropertyType();
				Object value = PropertyUtils.getProperty( bean, key );
				
	            if( jsonPropertyFilter != null && jsonPropertyFilter.apply( bean, key, value ) ){
	               continue;
	            }
	            JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor(bean.getClass(), type, key );
	            if( jsonValueProcessor != null ){
	               value = jsonValueProcessor.processObjectValue( key, value, jsonConfig );
	               if( !JsonVerifier.isValidJsonValue( value ) ){
	                  throw new JSONException( "Value is not a valid JSON value. " + value );
	               }
	            }
	            // ----- END of snipped
	            if(logger.isDebugEnabled()){
	            	logger.debug("processing " + key + " of " + bean.getClass());
	            }
	            if(Collection.class.isAssignableFrom(type)){
	            	JSONArray jsonList = JSONArray.fromObject(value, jsonConfig);
	            	json.element(key, jsonList, jsonConfig);
	            } else if(Object.class.isAssignableFrom(type)){
	            	JSONObject jsonObj = JSONObject.fromObject(value, jsonConfig);
	            	json.element(key, jsonObj, jsonConfig);
	            } else {
	            	throw new JSONException( "Value " + value + " can not be processed.");
	            }
	            
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		json = processBeanSecondStep((T) bean, json, jsonConfig);
			
		return json;
	}
	
	public abstract JSONObject processBeanSecondStep(T bean, JSONObject json, JsonConfig jsonConfig) ;
	
	public abstract List<String> getIgnorePropNames();
	

}