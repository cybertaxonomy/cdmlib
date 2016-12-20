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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.dozer.ConfigurableCustomConverter;
import org.dozer.Mapper;
import org.dozer.MappingException;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class HibernateProxyNullSafeDeepConverter implements ConfigurableCustomConverter, ApplicationContextAware {
	
	private Mapper mapper;
	
	private ApplicationContext applicationContext;
	
	String parameter = null;

	private List<String> parameterList;
	
	/* (non-Javadoc)
	 * @see org.dozer.ConfigurableCustomConverter#setParameter(java.lang.String)
	 */
	@Override
	public void setParameter(String parameter) {
		this.parameter = parameter;
		if(parameter.indexOf('.') > -1){
			this.parameterList = Arrays.asList(parameter.split("."));
		} else {
			this.parameterList = Arrays.asList(new String[]{parameter});
		}
	}


	protected Mapper getMapper() {
		if(mapper == null) {
			this.setMapper((Mapper)this.applicationContext.getBean("dozerMapper", Mapper.class)); 
		}
		return mapper;
	}
	
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null || !Hibernate.isInitialized(source)) {
		    return null;
	    } else {
	
	    	try {
	    		Object value = invokeProperty(source, sourceClass, parameterList.iterator());
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

	private Object invokeProperty(Object source, Class<?> sourceClass, Iterator<String> parameterIterator) throws IllegalAccessException,
			InvocationTargetException {
		Object value;
		String param = parameterIterator.next();
		PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(sourceClass, param);
		Method method = propertyDescriptor.getReadMethod();
		method.setAccessible(true);	    		
		assert method != null;
		value = method.invoke(source);
		if(parameterIterator.hasNext()){
			return invokeProperty(source, sourceClass, parameterIterator);
		} else {			
			return value;
		}
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	
}
