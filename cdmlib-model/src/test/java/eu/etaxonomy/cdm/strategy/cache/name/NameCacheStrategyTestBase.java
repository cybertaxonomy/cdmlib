/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * Base class for testing name cache strategies
 * @author a.mueller
 */
public class NameCacheStrategyTestBase {
	private static final Logger logger = Logger.getLogger(ZoologicalNameCacheStrategyTest.class);



	protected Method getMethod(Class<?> clazz, String methodName, Class<?> paramClazzes){
		Method method;
		try {
			method = clazz.getDeclaredMethod(methodName, paramClazzes);
		} catch (SecurityException e) {
			logger.error("SecurityException " + e.getMessage());
			return null;
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException " + e.getMessage());
			return null;
		}
		return method;
	}


	protected String getStringValue(Method method, Object object,Object parameter){
		try {
			List<TaggedText> list = (List<TaggedText>)method.invoke(object, parameter);
			return TaggedCacheHelper.createString(list);
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException " + e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException " + e.getMessage());
			return null;
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException " + e.getMessage());
			return null;
		}
	}
}
