// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.processors.JsonBeanProcessorMatcher;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.processors.JsonValueProcessorMatcher;
import net.sf.json.util.CycleDetectionStrategy;
import net.sf.json.util.PropertyFilter;

import org.springframework.beans.factory.FactoryBean;

/**
 * 
 * @author ben.clark
 * @author a.kohlbecker
 */
public class JsonConfigFactoryBean implements FactoryBean {

	private JsonConfig jsonConfig = null;
	
	private CycleDetectionStrategy cycleDetectionStrategy = CycleDetectionStrategy.LENIENT;
	
	private boolean ignoreJPATransient = false;
	
	private Map<Class,JsonBeanProcessor> jsonBeanProcessors = new HashMap<Class,JsonBeanProcessor>();
	private List<PropertyFilter> jsonPropertyFilters = new ArrayList<PropertyFilter>();
	private List<JsonBeanProcessorMatcher> jsonBeanProcessorMatchers = new ArrayList<JsonBeanProcessorMatcher>();
	private Map<Class,JsonValueProcessor> jsonValueProcessors = new HashMap<Class,JsonValueProcessor>();
	private JsonValueProcessorMatcher jsonValueProcessorMatcher = JsonValueProcessorMatcher.DEFAULT;
	
	public void setCycleDetectionStrategy(CycleDetectionStrategy cycleDetectionStrategy) {
		this.cycleDetectionStrategy = cycleDetectionStrategy;
	}
	
	public void setIgnoreJPATransient(boolean ignoreJPATransient) {
		this.ignoreJPATransient = ignoreJPATransient;
	}

	public void setJsonBeanProcessors(Map<Class, JsonBeanProcessor> jsonBeanProcessors) {
		this.jsonBeanProcessors = jsonBeanProcessors;
	}

	public void setJsonPropertyFilters(List<PropertyFilter> jsonPropertyFilters) {
		this.jsonPropertyFilters = jsonPropertyFilters;
	}

	public void setJsonBeanProcessorMatchers(List<JsonBeanProcessorMatcher> jsonBeanProcessorMatchers) {
		this.jsonBeanProcessorMatchers = jsonBeanProcessorMatchers;
	}
	
	public void setJsonValueProcessorMatcher(JsonValueProcessorMatcher jsonValueProcessorMatcher ) {
		this.jsonValueProcessorMatcher = jsonValueProcessorMatcher;
	}

	public void setJsonValueProcessors(Map<Class, JsonValueProcessor> jsonValueProcessors) {
		this.jsonValueProcessors = jsonValueProcessors;
	}
	
	public void init() {
		jsonConfig = new JsonConfig();
		
		jsonConfig.setCycleDetectionStrategy(cycleDetectionStrategy);
		
		jsonConfig.setIgnoreJPATransient(ignoreJPATransient);
		
		jsonConfig.setJsonValueProcessorMatcher(jsonValueProcessorMatcher);
		
		for(Class clazz : jsonBeanProcessors.keySet()) {
			jsonConfig.registerJsonBeanProcessor(clazz, jsonBeanProcessors.get(clazz));
		}
		
		for(PropertyFilter propertyFilter : jsonPropertyFilters) {
		    jsonConfig.setJsonPropertyFilter(propertyFilter);
		}

		for(JsonBeanProcessorMatcher jsonBeanProcessorMatcher : jsonBeanProcessorMatchers) {
		    jsonConfig.setJsonBeanProcessorMatcher(jsonBeanProcessorMatcher);
		}
		
		for(Class clazz : jsonValueProcessors.keySet()) {
		    jsonConfig.registerJsonValueProcessor(clazz, jsonValueProcessors.get(clazz)); 
		}
		
		
	}
	
	
	public Object getObject() throws Exception {
		if(jsonConfig == null) {
			init();
		}
		return jsonConfig;
	}

    public Class getObjectType() {
		return JsonConfig.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
