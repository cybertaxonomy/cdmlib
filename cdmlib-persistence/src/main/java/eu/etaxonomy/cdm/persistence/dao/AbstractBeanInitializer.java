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
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @date 26.03.2009
 *
 */
public abstract class AbstractBeanInitializer implements BeanInitializer{
	
	public static final Logger logger = Logger.getLogger(AbstractBeanInitializer.class);
	
	/**
	 * Initialize the the proxy, unwrap the target object and return it. 
	 * @param proxy the proxy to initialize
	 * @return the unwrapped target object
	 */
	protected abstract Object initializeInstance(Object proxy);

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
			logger.debug(">> starting initializeBean() of " + bean + " ;class:" + bean.getClass().getSimpleName());
		}
		Set<Class> restrictions = new HashSet<Class>();
		if(cdmEntities){
			restrictions.add(CdmBase.class);
		} 
		if(collections){
			restrictions.add(Collections.class);
		} 
		Set<PropertyDescriptor> props = getProperties(bean, restrictions); 
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
				logger.warn("Property " + prop.getName() + " not found");
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("  completed initializeBean() of " + bean);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.BeanInitializer#initializeProperties(java.lang.Object, java.util.List)
	 */
	//TODO optimize algorithm ..
	public void initialize(Object bean, List<String> propertyPaths) {
		if(propertyPaths == null){
			return;
		}
		
		Collections.sort(propertyPaths);
		if(logger.isDebugEnabled()){
			logger.debug(">> starting initialize() of " + bean + " ;class:" + bean.getClass().getSimpleName());
		}
		for(String propPath : propertyPaths){
			initializePropertyPath(bean, propPath);
		}
		if(logger.isDebugEnabled()){
			logger.debug("  completed initialize() of " + bean);
		}
		
	}
	
	public <T> List<T> initializeAll(List<T> beanList,  List<String> propertyPaths){
		if(propertyPaths != null){			
			for(Object bean : beanList){
				initialize(bean, propertyPaths);
			}
		}
		return beanList;
	}

	/**
	 * @param bean
	 * @param propPath
	 */
	private void initializePropertyPath(Object bean, String propPath) {
		if(logger.isDebugEnabled()){
			logger.debug("processing " + propPath);
		}
		
		// if a wildcard is used for the property do a batch initialization
		if(propPath.equals(LOAD_2ONE_WILDCARD)){
			if(Collection.class.isAssignableFrom(bean.getClass())){
				initializeAllEntries((Collection)bean, true, false);
			} else if(Map.class.isAssignableFrom(bean.getClass())) {
				initializeAllEntries(((Map)bean).values(), true, false);
			} else{
				initializeBean(bean, true, false);
			}
		} else if(propPath.equals(LOAD_2ONE_2MANY_WILDCARD)){
			if(Collection.class.isAssignableFrom(bean.getClass())){
				initializeAllEntries((Collection)bean, true, true);
			} else if(Map.class.isAssignableFrom(bean.getClass())) {
				initializeAllEntries(((Map)bean).values(), true, false);
			} else {
				initializeBean(bean, true, true);				
			}
		} else {
		    // initialize a specific property or property path
			initializeProperty(bean, propPath);
		}
	}

	/**
	 * @param bean
	 * @param property
	 * @param nestedPath
	 */
//	private void initializeUsingLazyInitializer(Object bean, String property, String nestedPath) {
//		if (bean instanceof HibernateProxy) {
//			HibernateProxy proxy = (HibernateProxy) bean;
//			JavassistLazyInitializer li = (JavassistLazyInitializer)proxy.getHibernateLazyInitializer();
//			li.invoke(bean, thisMethod, proceed, args);
//		}
//	}

	/**
	 * @param bean
	 * @param property
	 * @param nestedPath
	 */
	private void initializeProperty(Object bean, String propPath) {
		
		// split next path token of
		String property;
		String nestedPath = null;
		int pos;
		if((pos = propPath.indexOf('.')) > 0){
			nestedPath = propPath.substring(pos + 1);
			property = propPath.substring(0, pos);
		} else {
			property = propPath;
		}
		
		// is the property indexed?
		Integer index = null;
		if((pos = property.indexOf('[')) > 0){
			String indexString = property.substring(pos + 1, property.indexOf(']'));
			index = Integer.valueOf(indexString);
			property = property.substring(0, pos);
		}
		
		try {
			// initialize
			//Class targetClass = HibernateProxyHelper.getClassWithoutInitializingProxy(bean); // used for debugging
			Object proxy = PropertyUtils.getProperty(bean, property);
			Object unwrappedBean = initializeInstance(proxy);
			
			// handle nested properties
			if(proxy != null && nestedPath != null){
				if (Collection.class.isAssignableFrom(proxy.getClass())) {
					int i = 0;
					for (Object entrybean : (Collection) proxy) {
						if(index == null){
							initializePropertyPath(entrybean, nestedPath);
						} else if(index.equals(i)){
							initializePropertyPath(entrybean, nestedPath);
							break;
						}
						i++;
					}
				} else if(Map.class.isAssignableFrom(proxy.getClass())) {
					int i = 0;
					for (Object entrybean : ((Map) proxy).values()) {
						if(index == null){
							initializePropertyPath(entrybean, nestedPath);
						} else if(index.equals(i)){
							initializePropertyPath(entrybean, nestedPath);
							break;
						}
						i++;
					}
				}else {
					initializePropertyPath(unwrappedBean, nestedPath);
				}
			}
			
		} catch (IllegalAccessException e) {
			logger.error("Illegal access on property " + property);
		} catch (InvocationTargetException e) {
			logger.error("Cannot invoke property " + property + " not found");
		} catch (NoSuchMethodException e) {
			logger.warn("Property " + property + " not found");
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
	public static Set<PropertyDescriptor> getProperties(Object bean, Set<Class> typeRestrictions) {
		
		Set<PropertyDescriptor> properties = new HashSet<PropertyDescriptor>();
		PropertyDescriptor[] prop = PropertyUtils.getPropertyDescriptors(bean);
		
		for (int i = 0; i < prop.length; i++) {
			//String propName = prop[i].getName();
			
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
			      if(typeRestrictions != null && typeRestrictions.size() > 1){
			    	  for(Class type : typeRestrictions){
			    		  if(type.isAssignableFrom(prop[i].getPropertyType())){
			    			  properties.add(prop[i]);
			    		  }
			    	  }
			      } else {
			    	  properties.add(prop[i]);
			      }
			}
		}
		return properties;
	}

}