// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import net.sf.dozer.util.mapping.MapperIF;
import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.ConfigurableCustomConverter;

import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class HibernateProxyNullSafeDeepConverter implements ConfigurableCustomConverter, ApplicationContextAware {
	
	private MapperIF mapper;
	
	private ApplicationContext applicationContext;
	
	protected MapperIF getMapper() {
		if(mapper == null) {
			this.setMapper((MapperIF)this.applicationContext.getBean("dozerMapper", MapperIF.class)); 
		}
		return mapper;
	}
	
	public void setMapper(MapperIF mapper) {
		this.mapper = mapper;
	}

	public Object convert(Object destination, Object source, Class destClass, Class sourceClass, String arg) {
		if (source == null || !Hibernate.isInitialized(source)) {
		    return null;
	    } else {
	    	try {
	    		PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(sourceClass, arg);
	    		Method method = propertyDescriptor.getReadMethod();
	    		method.setAccessible(true);	    		
	    		assert method != null;
	    		Object value = method.invoke(source);
	    		if(value == null || !Hibernate.isInitialized(value)) {
	    		    return null;
	    		} else {
	    			if(value instanceof CdmBase) {
	    			return getMapper().map(value, destClass);
	    			} else {
	    				return value;
	    		}
	    		}
			} catch (Exception e) {
				throw new MappingException("Converter HibernateProxyNullSafeDeepConverter used incorrectly. Arguments passed in were:"+ destination + " and " + source + " sourceClass " + sourceClass + " destClass " + destClass, e);
			} 
		} 
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
