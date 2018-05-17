/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.AbstractBeanInitializer;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.processors.JsonVerifier;
import net.sf.json.util.PropertyFilter;

/**
 * @author a.kohlbecker
 * @since 30.03.2009
 *
 */
public abstract class AbstractBeanProcessor<T extends Object> implements JsonBeanProcessor{

    public static final Logger logger = Logger.getLogger(AbstractBeanProcessor.class);

    private Set<String> excludes = new HashSet<String>();

    private Set<String> mergedExcludes = null;

    public Set<String> getExcludes() {
        return excludes;
    }

    /**
     * This method allows supplying a List of property names to be ignored
     * during the serialization to JSON. The <code>excludes</code> will be
     * merged with the property names configured by subclasses which override
     * {@link {@link #getIgnorePropNames()}.
     *
     * @param excludes
     */
    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    /**
     * Implementations of this abstract class may override this method in order
     * to supply a List of property names to be ignored in
     * {@link #processBean(Object, JsonConfig)}. This feature generally is used
     * when {@link #processBeanSecondStep(CdmBase, JSONObject, JsonConfig)} is
     * implemented. such that this method is responsible of serializing this
     * property.
     *
     * @return a List of property names.
     */
    public abstract List<String> getIgnorePropNames();

    /**
     * merges and returns {@link {@link #getIgnorePropNames()} with
     * {@link #excludes}
     *
     * @return
     */
    protected Set<String> getMergedExcludes(){
        if(mergedExcludes  == null) {
            mergedExcludes = new HashSet<String>(excludes);
            if(getIgnorePropNames() != null){
                mergedExcludes.addAll(getIgnorePropNames());
            }
        }

        return mergedExcludes;
    }

    /**
     *
     * @param json
     * @param jsonConfig
     * @param fieldName
     * @param fieldObject
     */
    protected void addJsonElement(JSONObject json, JsonConfig jsonConfig, String fieldName,	Object fieldObject) {
        if(Hibernate.isInitialized(fieldObject)){
            json.element(fieldName, fieldObject, jsonConfig);
        }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public final JSONObject processBean(Object bean, JsonConfig jsonConfig) {

        if(logger.isDebugEnabled()){
            logger.debug("processing " + bean.getClass());
        }

        JSONObject json =  new JSONObject();
        Collection<?> exclusions = jsonConfig.getMergedExcludes( bean.getClass() );
        Set<PropertyDescriptor> props = AbstractBeanInitializer.getProperties(bean, null);
        PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
        for(PropertyDescriptor prop: props){
            String key = prop.getName();
            if(getMergedExcludes().contains(key) || exclusions.contains(key)){
                if(logger.isDebugEnabled()){
                    logger.debug("skipping excluded property " + key);
                }
                continue;
            }

            try {
                // ------ reusing snippet from JSONOnbject._fromBean()
                Class<?> type = prop.getPropertyType();
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
                if(CdmBase.class.isAssignableFrom(type)){
                    json.element(key, value, jsonConfig);
                } else if(Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)){
                    json.element(key, value, jsonConfig);
                }  else if(Object.class.isAssignableFrom(type)){
                    json.element(key, value, jsonConfig);
                } else {
                    json.element(key, value);
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

    /**
     * This method is called at the end of {@link #processBean(Object, JsonConfig)} just before the JSONObject is returned.
     * By overriding this method it is possible to do further processing.
     * <p>
     * <b>See also {@link #getIgnorePropNames()}!</b>
     *
     * @param bean
     * @param json
     * @param jsonConfig
     * @return
     */
    public abstract JSONObject processBeanSecondStep(T bean, JSONObject json, JsonConfig jsonConfig) ;


}
