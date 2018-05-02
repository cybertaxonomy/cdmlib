/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * @author n.hoffmann
 * @since Mar 11, 2010
 * @version 1.0
 */
@Component
public class MethodCacheImpl implements IMethodCache {
	
//	MethodUtils
	
	private Map<MethodDescriptor, Method> methodMap = new HashMap<MethodDescriptor, Method>();

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IMethodCache#getMethod(java.lang.Class, java.lang.String, java.lang.Class)
	 */
	public Method getMethod(Class clazz, String methodName, Class parameterType) {
		MethodDescriptor methodDescriptor = new MethodDescriptor(clazz, methodName, new Class[]{parameterType}); 
		
		if(methodMap.containsKey(methodDescriptor)){
			return methodMap.get(methodDescriptor);
		}
		
		Method method = getMethodInternal(clazz, methodName, parameterType);
		if(method != null){
			method.setAccessible(true);
		}
		// we also put null methods into the map to benefit from caching
		put(methodDescriptor, method);
		
		return method; 
	}
	
	/**
	 * Checks class hierarchy of the given class for a method that fits to the given name and parameter type
	 * 
	 * @param clazz
	 * @param methodName
	 * @param parameterType
	 * @return
	 */
	private Method getMethodInternal(Class clazz, String methodName,
			Class parameterType){
		// stop recursing when there are no more superclasses
		if(clazz == null){
			return null;
		}
		
		Method method = null;
		
		for(Class includedType : getIncludedTypes(parameterType, new ArrayList<Class>())){
			try {
				method = clazz.getDeclaredMethod(methodName, includedType);
			}catch (NoSuchMethodException e) {
				;
			} 
		}
		
		// if we have a method return it
		if(method != null){
			return method;
		}
			
		// recurse into superclass if no method was found
		return getMethodInternal(clazz.getSuperclass(), methodName, parameterType);
	}
	
	/**
	 * Create a list containing the type and all supertypes of a given type
	 * 
	 * @param clazz
	 * @param classList
	 * @return
	 */
	private List<Class> getIncludedTypes(Class clazz, List<Class> classList){
		if(clazz == null){
			return classList;
		}
		classList.add(clazz);
		Class[] interfaces = clazz.getInterfaces();
		if(interfaces != null){
			classList.addAll(Arrays.asList(interfaces));
		}
		return getIncludedTypes(clazz.getSuperclass(), classList);
	}
	
	/**
	 * Fill the cache
	 * 
	 * @param methodDescriptor
	 * @param method
	 */
	private void put(MethodDescriptor methodDescriptor, Method method) {
		methodMap.put(methodDescriptor, method);
	}
	
	/**
	 * 
	 * @author n.hoffmann
	 * @since Mar 11, 2010
	 * @version 1.0
	 */
	private static class MethodDescriptor{
		private static final Logger logger = Logger
				.getLogger(MethodDescriptor.class);
		
		/** An empty class array */
	    private static final Class[] emptyClassArray = new Class[0];
		
		private Class clazz;
	    private String methodName;
	    private Class[] parameterTypes;
	    private int hashCode;

	    /**
	     * The sole constructor.
	     *
	     * @param clazz  the class to reflect, must not be null
	     * @param methodName  the method name to obtain
	     * @param paramTypes the array of classes representing the paramater types
	     * @param exact whether the match has to be exact.
	     */
	    public MethodDescriptor(Class clazz, String methodName, Class[] paramTypes) {
	        if (clazz == null) {
	            throw new IllegalArgumentException("Class cannot be null");
	        }
	        if (methodName == null) {
	            throw new IllegalArgumentException("Method Name cannot be null");
	        }
	        if (paramTypes == null) {
	            paramTypes = emptyClassArray;
	        }

	        this.clazz = clazz;
	        this.methodName = methodName;
	        this.parameterTypes = paramTypes;

	        this.hashCode = methodName.length();
	    }
	    /**
	     * Checks for equality.
	     * @param object object to be tested for equality
	     * @return true, if the object describes the same Method.
	     */
	    public boolean equals(Object object) {
	        if (!(object instanceof MethodDescriptor)) {
	            return false;
	        }
	        MethodDescriptor methodDescriptor = (MethodDescriptor)object;

	        return (
	            methodName.equals(methodDescriptor.methodName) &&
	            clazz.equals(methodDescriptor.clazz) &&
	            java.util.Arrays.equals(parameterTypes, methodDescriptor.parameterTypes)
	        );
	    }
	    /**
	     * Returns the string length of method name. I.e. if the
	     * hashcodes are different, the objects are different. If the
	     * hashcodes are the same, need to use the equals method to
	     * determine equality.
	     * @return the string length of method name.
	     */
	    public int hashCode() {
	        return hashCode;
	    }		
	}	
}
