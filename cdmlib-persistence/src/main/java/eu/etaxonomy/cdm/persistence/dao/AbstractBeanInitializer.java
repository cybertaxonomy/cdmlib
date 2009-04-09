// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.ibm.lsid.client.conf.castor.PropertiesDescriptor;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @date 26.03.2009
 *
 */
public abstract class AbstractBeanInitializer implements BeanInitializer{
	
	public static final Logger logger = Logger.getLogger(AbstractBeanInitializer.class);
	
	protected abstract void initializeInstance(Object proxy);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.BeanInitializer#load(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public void load(Object bean) {
		initializeBean(bean, true, false); 
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.BeanInitializer#loadFully(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public void loadFully(Object bean) {
		initializeBean(bean, true, true); 
	}
	
	/**
	 * @param bean
	 * @param cdmEntities
	 * @param collections
	 */
	public void initializeBean(Object bean, boolean cdmEntities, boolean collections){
		
		if(logger.isDebugEnabled()){
			logger.debug("starting initialisation of " + bean + " ;class:" + bean.getClass().getSimpleName());
		}
		Set<PropertyDescriptor> props = getProperties(bean, cdmEntities, collections); 
		for(PropertyDescriptor prop : props){
			try {
				Object proxy = PropertyUtils.getProperty( bean, prop.getName());
				if(proxy == null){
					if(logger.isDebugEnabled()){
						logger.debug("is null: " + prop.getName());
					}
					continue;
				}
				if(logger.isDebugEnabled()){
					logger.debug("initializing: " + prop.getName());
				}
				initializeInstance(proxy);
			} catch (IllegalAccessException e) {
				logger.error("Illegal access on property " + prop.getName());
			} catch (InvocationTargetException e) {
				logger.error("Cannot invoke property " + prop.getName() + " not found");
			} catch (NoSuchMethodException e) {
				logger.error("Property " + prop.getName() + " not found");
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("initialisation of " + bean + " complete");
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.BeanInitializer#initializeProperties(java.lang.Object, java.util.List)
	 */
	//TODO optimize algorithm ..
	public void initialize(Object bean, List<String> propertyPaths) {
		
		Collections.sort(propertyPaths);
	
		for(String propPath : propertyPaths){
			initializePropertyPath(bean, propPath);
		}
		
	}
	
	public void initializeAll(List list,  List<String> propertyPaths){
		for(Object bean : list){
			initialize(bean, propertyPaths);
		}
	}

	/**
	 * @param bean
	 * @param propPath
	 */
	private void initializePropertyPath(Object bean, String propPath) {
		if(logger.isDebugEnabled()){
			logger.debug("processing " + propPath);
		}
		String property;
		String nestedPath = null;
		int pos;
		if((pos = propPath.indexOf('.')) > 0){
			nestedPath = propPath.substring(pos + 1);
			property = propPath.substring(0, pos);
		} else {
			property = propPath;
		}
		
		// if a wildcard is used for the property do a batch initialization
		if(property.equals(LOAD_2ONE_WILDCARD)){
			if(Collection.class.isAssignableFrom(bean.getClass())){
				initializeAllEntries((Collection)bean, true, false);
			} else {				
				initializeBean(bean, true, false);
			}
		} else if(property.equals(LOAD_2ONE_2MANY_WILDCARD)){
			if(Collection.class.isAssignableFrom(bean.getClass())){
				initializeAllEntries((Collection)bean, true, true);
			} else {
				initializeBean(bean, true, true);				
			}
		} else {
		// initialize a specific property or property path
			try {
				Object proxy = PropertyUtils.getProperty( bean, property);
				initializeInstance(proxy);
				if(nestedPath != null){
					if (Collection.class.isAssignableFrom(proxy.getClass())) {
						for (Object entrybean : (Collection) proxy) {
							initializePropertyPath(entrybean, nestedPath);
						}

					} else {
						initializePropertyPath(proxy, nestedPath);
					}
				}
			} catch (IllegalAccessException e) {
				logger.error("Illegal access on property " + property);
			} catch (InvocationTargetException e) {
				logger.error("Cannot invoke property " + property + " not found");
			} catch (NoSuchMethodException e) {
				logger.error("Property " + property + " not found");
			}
		}
		
	}

	/**
	 * @param bean
	 * @param b
	 * @param c
	 */
	private void initializeAllEntries(Collection collection, boolean cdmEntities, boolean collections) {
		for(Object bean : collection){
			initializeBean(bean, cdmEntities, collections);
		}
		
	}

	/**
	 * @param bean
	 * @param cdmEntities
	 * @param collections
	 * @return
	 */
	public static Set<PropertyDescriptor> getProperties(Object bean, boolean cdmEntities, boolean collections) {
		
		Set<PropertyDescriptor> properties = new HashSet<PropertyDescriptor>();
		PropertyDescriptor[] prop = PropertyUtils.getPropertyDescriptors(bean);
		
		for (int i = 0; i < prop.length; i++) {
			String propName = prop[i].getName();
			
	        // only read methods & skip transient getters
			if( prop[i].getReadMethod() != null ){
			      try{
			         Class transientClass = Class.forName( "javax.persistence.Transient" );
			         if( prop[i].getReadMethod().getAnnotation( transientClass ) != null ){
			            continue;
			         }
			      }catch( ClassNotFoundException cnfe ){
			         // ignore
			      }
			      if(cdmEntities && CdmBase.class.isAssignableFrom(prop[i].getPropertyType())){
			    	  properties.add(prop[i]);
			      }
			      if(collections && Collection.class.isAssignableFrom(prop[i].getPropertyType())){
			    	  properties.add(prop[i]);
			      }
			}
		}
		return properties;
	}

}