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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;

import eu.etaxonomy.cdm.remote.json.processor.bean.AbstractCdmBeanProcessor;
import net.sf.json.JsonConfig;
import net.sf.json.processors.DefaultValueProcessor;
import net.sf.json.processors.DefaultValueProcessorMatcher;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.processors.JsonBeanProcessorMatcher;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.processors.JsonValueProcessorMatcher;
import net.sf.json.util.CycleDetectionStrategy;
import net.sf.json.util.PropertyFilter;

/**
 * @author ben.clark
 * @author a.kohlbecker
 */
public class JsonConfigFactoryBean implements FactoryBean<JsonConfig> {

    private static final Logger logger = LogManager.getLogger();

	private JsonConfig jsonConfig = null;

	private CycleDetectionStrategy cycleDetectionStrategy = CycleDetectionStrategy.LENIENT;

	/**
	 * Default is true, to avoid LayzyLoadingExceptions. See
	 * {@link #setIgnoreJPATransient(boolean)}
	 */
	private boolean ignoreJPATransient = true;

	private Map<Class<?>,JsonBeanProcessor> jsonBeanProcessors = new HashMap<>();
	private PropertyFilter jsonPropertyFilter = null;
	private Map<Class<?>,JsonValueProcessor> jsonValueProcessorsByClass = new HashMap<>();
	private DefaultValueProcessorMatcher defaultValueProcessorMatcher = null;
	private Map<Class<?>, DefaultValueProcessor> defaultValueProcessorMap = new HashMap<>();
	private Map<String, JsonValueProcessor> jsonValueProcessorsByProperty = new HashMap<>();
	private JsonBeanProcessorMatcher jsonBeanProcessorMatcher = JsonBeanProcessorMatcher.DEFAULT;
	private JsonValueProcessorMatcher jsonValueProcessorMatcher = JsonValueProcessorMatcher.DEFAULT;
	private boolean ignoreDefaultExcludes = false;
	private List<String> excludes = new ArrayList<>();


	public void setCycleDetectionStrategy(CycleDetectionStrategy cycleDetectionStrategy) {
		this.cycleDetectionStrategy = cycleDetectionStrategy;
	}

	/**
	 * Default is true, to avoid LayzyLoadingExceptions.
	 * <p>
	 *
	 * @deprecated Setting this property to false will cause
	 *             LazyLoadingExceptions and will thus completely break the JSON
	 *             serialization!! <br>
	 *             In the cdm model all getters returning cdm entity or product
	 *             of cdm entities which are not directly returning a
	 *             HibernateProxy are annotated as @Transient. In order to
	 *             serialize these properties you have to do two things:
	 *             <ol>
	 *             <li>Explicitly serialize the property by overriding
	 *             {@link AbstractCdmBeanProcessor#processBeanSecondStep(eu.etaxonomy.cdm.model.common.CdmBase, net.sf.json.JSONObject, JsonConfig)}
	 *             in the according {AbstractCdmBeanProcessor} implementation.
	 *             If there is no matching {AbstractCdmBeanProcessor}
	 *             implementation you would have to create one. for example:
	 *             <pre>
	 	@Override
		public JSONObject processBeanSecondStep(TaxonName bean, JSONObject json, JsonConfig jsonConfig) {
		  json.element("taggedName", getTaggedName(bean), jsonConfig);
		  return json;
		}
	 *             </pre>
	 *             </li> <li>Provide the service method which is used to
	 *             retrieve the object graph in question with an appropriate
	 *             initialization strategy.</li>
	 *             </ol>
	 * <strong>please see also https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/CdmEntityInitalization</strong>
	 *
	 * @param ignoreJPATransient
	 */
	@Deprecated
	public void setIgnoreJPATransient(boolean ignoreJPATransient) {
		if(!ignoreJPATransient){
			logger.error("ignoreJPATransient must not be set to false. Doing so will cause LazyLoadingExceptions and will thus completely break the JSON serialization.");
		}
		this.ignoreJPATransient = ignoreJPATransient;
	}

	public void setJsonBeanProcessors(Map<Class<?>, JsonBeanProcessor> jsonBeanProcessors) {
		this.jsonBeanProcessors = jsonBeanProcessors;
	}

	public void setJsonPropertyFilter(PropertyFilter jsonPropertyFilter) {
		this.jsonPropertyFilter = jsonPropertyFilter;
	}

	public void setJsonBeanProcessorMatcher(JsonBeanProcessorMatcher jsonBeanProcessorMatcher) {
		this.jsonBeanProcessorMatcher = jsonBeanProcessorMatcher;
	}

	public void setJsonValueProcessorMatcher(JsonValueProcessorMatcher jsonValueProcessorMatcher ) {
		this.jsonValueProcessorMatcher = jsonValueProcessorMatcher;
	}

	public void setDefaultValueProcessorMap(Map<Class<?>, DefaultValueProcessor> defaultValueProcessorMap){
	    this.defaultValueProcessorMap = defaultValueProcessorMap;
	}

	public void setDefaultValueProcessorMatcher(DefaultValueProcessorMatcher defaultValueProcessorMatcher){
	    this.defaultValueProcessorMatcher = defaultValueProcessorMatcher;
	}

	public void setJsonValueProcessorsByClass(Map<Class<?>, JsonValueProcessor> jsonValueProcessors) {
		this.jsonValueProcessorsByClass = jsonValueProcessors;
	}

	   public void setJsonValueProcessorsByProperty(Map<String, JsonValueProcessor> jsonValueProcessors) {
	        this.jsonValueProcessorsByProperty = jsonValueProcessors;
	    }

	public void setIgnoreDefaultExcludes(boolean ignoreDefaultExcludes) {
		this.ignoreDefaultExcludes = ignoreDefaultExcludes;
	}

	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}

	public void init() {
		jsonConfig = new JsonConfig();

		jsonConfig.setCycleDetectionStrategy(cycleDetectionStrategy);

		jsonConfig.setIgnoreJPATransient(ignoreJPATransient);

		jsonConfig.setDefaultValueProcessorMatcher(defaultValueProcessorMatcher);

		jsonConfig.setJsonValueProcessorMatcher(jsonValueProcessorMatcher);

		jsonConfig.setJsonBeanProcessorMatcher(jsonBeanProcessorMatcher);

		jsonConfig.setExcludes(excludes.toArray(new String[]{}));

		jsonConfig.setIgnoreDefaultExcludes(ignoreDefaultExcludes);

		jsonConfig.setJsonPropertyFilter(jsonPropertyFilter);

		for(Class<?> clazz : jsonBeanProcessors.keySet()) {
			jsonConfig.registerJsonBeanProcessor(clazz, jsonBeanProcessors.get(clazz));
		}

		for(Class<?> clazz : defaultValueProcessorMap.keySet()) {
            jsonConfig.registerDefaultValueProcessor(clazz, defaultValueProcessorMap.get(clazz));
        }

		for(Class<?> clazz : jsonValueProcessorsByClass.keySet()) {
		    jsonConfig.registerJsonValueProcessor(clazz, jsonValueProcessorsByClass.get(clazz));
		}

		for(String prop : jsonValueProcessorsByProperty.keySet()) {
            jsonConfig.registerJsonValueProcessor(prop, jsonValueProcessorsByClass.get(prop));
        }
	}


	@Override
    public JsonConfig getObject() throws Exception {
		if(jsonConfig == null) {
			init();
		}
		return jsonConfig;
	}

    @Override
    public Class<?> getObjectType() {
		return JsonConfig.class;
	}

	@Override
    public boolean isSingleton() {
		return true;
	}

}
