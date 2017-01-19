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


/**
 * @author n.hoffmann
 * @created Mar 11, 2010
 * @version 1.0
 */
public interface IMethodCache {

	/**
	 * Returns the method that is suitable for the given class and parameter type or
	 * null if no such method exists.
	 * 
	 * @param clazz 
	 * @param methodName
	 * @param parameterType
	 * @return
	 */
	public Method getMethod(Class clazz, String methodName, Class parameterType);
}
