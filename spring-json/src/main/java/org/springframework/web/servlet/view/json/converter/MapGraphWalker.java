/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */	
package org.springframework.web.servlet.view.json.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.sojo.common.ObjectUtil;
import net.sf.sojo.common.WalkerInterceptor;
import net.sf.sojo.core.Constants;
import net.sf.sojo.core.conversion.ComplexBean2MapConversion;
import net.sf.sojo.core.filter.ClassPropertyFilterHandler;
import net.sf.sojo.core.reflect.ReflectionHelper;
import net.sf.sojo.util.ArrayIterator;

/**
 * Walk over (to traverse) object graph and collection information over path and values of the objects.
 * This can use to transform one object graph to other representation (by registration of implementation from the interface <code>WalkerInterceptor</code>).
 * 
 * @author linke
 *
 */
public class MapGraphWalker {

	private List interceptorList = new ArrayList();
	private ObjectUtil objectUtil = new ObjectUtil(false);
	private int numberOfRecursion = 0;
	private boolean ignoreNullValues = true;
	private String excludedProperties[] = null;
	
	public MapGraphWalker() { 
		setIgnoreNullValues(true);
	}
	
	public MapGraphWalker(ClassPropertyFilterHandler pvFilterHandler) {
		this();
		this.objectUtil.setClassPropertyFilterHandler(pvFilterHandler);
	}
	
	public ObjectUtil getObjectUtil() { return objectUtil; }
	
	public void setExcludedProperties(String[] pvExcludedProperties) {
		excludedProperties = pvExcludedProperties;
	}
	
	public void setIgnoreNullValues(boolean pvBoolean) {
		ignoreNullValues = pvBoolean;
		ComplexBean2MapConversion lvBean2MapConversion = new ComplexBean2MapConversion();
		lvBean2MapConversion.setIgnoreNullValues(ignoreNullValues);
		objectUtil.getConverter().replaceConversion(lvBean2MapConversion);		
	}
	
	public void addInterceptor(WalkerInterceptor pvInterceptor) {
		interceptorList.add(pvInterceptor);
	}
	public int getInterceptorSize() {
		return interceptorList.size();
	}
	public void removeInterceptorByNumber(int pvPosition) {
		interceptorList.remove(pvPosition);
	}
	
	private boolean fireVisitElementEvent(Object pvKey, int pvIndex, Object pvValue, int pvType, String pvPath, int pvNumberOfRecursion) {
		for (int i=0; i<interceptorList.size(); i++) {
			boolean b = ((WalkerInterceptor) interceptorList.get(i)).visitElement(pvKey, pvIndex, pvValue, pvType, pvPath, pvNumberOfRecursion);
			if (b == true) {
				return true;
			}
		}
		return false;
	}

	private void fireVisitIterateableElement(Object pvValue, int pvType, String pvPath, int pvBeginEnd) {
		for (int i=0; i<interceptorList.size(); i++) {
			((WalkerInterceptor) interceptorList.get(i)).visitIterateableElement(pvValue, pvType, pvPath, pvBeginEnd);
		}
	}

	
	
	private void fireStartWalk(Object pvStartObject) {
		for (int i=0; i<interceptorList.size(); i++) {
			((WalkerInterceptor) interceptorList.get(i)).startWalk(pvStartObject);
		}
	}

	private void fireEndWalk() {
		for (int i=0; i<interceptorList.size(); i++) {
			((WalkerInterceptor) interceptorList.get(i)).endWalk();
		}
	}
	
	public int getNumberOfRecursion() { return numberOfRecursion; }
	
	public void walk(Object pvObject) {
		numberOfRecursion = 0;
		fireStartWalk(pvObject);
		walkInternal(null, Constants.INVALID_INDEX, pvObject, "");
		fireEndWalk();
	}
	
	public String removeLastPointOnPath(String pvPath) {
		String lvPath = pvPath;
		if (lvPath.endsWith(".")) {
			lvPath = lvPath.substring(0, lvPath.length() - 1);
		} 
		return lvPath; 
	}
	
	private void walkInternal(Object pvKey, int pvIndex, Object pvValue, String pvPath) {
		numberOfRecursion++;

		if (pvValue == null) {
			fireVisitElementEvent(pvKey, pvIndex, null, Constants.TYPE_NULL, removeLastPointOnPath(pvPath), numberOfRecursion);
		} else if (ReflectionHelper.isSimpleType(pvValue)) {
			fireVisitElementEvent(pvKey, pvIndex, pvValue, Constants.TYPE_SIMPLE, removeLastPointOnPath(pvPath), numberOfRecursion);
		}
			
		// --- Map ---
		else if (ReflectionHelper.isMapType(pvValue)) {
			Map lvMap = (Map) pvValue;
			boolean lvCancel = fireVisitElementEvent(pvKey, pvIndex, pvValue, Constants.TYPE_MAP, pvPath + "()", numberOfRecursion);
			if (ReflectionHelper.isComplexMapType(pvValue)) {
				if (pvPath.length() > 0 && pvPath.endsWith(".") == false) { pvPath = pvPath + "."; }
				mapWalker(lvMap, pvPath, false, lvCancel);
			} else {
				mapWalker(lvMap, pvPath, true, lvCancel);
			}
		}
			
		// --- Iterateable ---
		else if (ReflectionHelper.isIterateableType(pvValue)) {
			boolean lvCancel =  fireVisitElementEvent(pvKey, pvIndex, pvValue, Constants.TYPE_ITERATEABLE, pvPath + "[]", numberOfRecursion);
			iteratorWalker(pvValue, pvPath, lvCancel);
		}
			
		// --- Complex ---
		else {
			pvPath = removeLastPointOnPath(pvPath);
			Object lvSimple = objectUtil.makeSimple(pvValue, excludedProperties);
			if("".equals(pvKey) || pvKey == null)
				walkInternal(null, Constants.INVALID_INDEX, lvSimple, pvPath);
			else
				walkInternal(pvKey, Constants.INVALID_INDEX, lvSimple, pvPath);
		}
	}
			
	private void iteratorWalker(Object pvValue , String pvPath, boolean pvCancel) {
		if (pvCancel == false) {
			int lvIndex = Constants.INVALID_INDEX;
			
			Iterator lvIterator = null;
			if (pvValue.getClass().isArray()) {
				lvIterator = new ArrayIterator(pvValue);
			} else {
				Collection lvCollection = (Collection) pvValue;
				lvIterator = lvCollection.iterator();
			}
			
			fireVisitIterateableElement(pvValue, Constants.TYPE_ITERATEABLE, pvPath, Constants.ITERATOR_BEGIN);
			while (lvIterator.hasNext()) {
				Object o = lvIterator.next();
				lvIndex++;
				StringBuffer sb = new StringBuffer(pvPath);
				sb.append("[").append(lvIndex).append("]");
				sb.append(".");
				walkInternal(null, lvIndex, o, sb.toString());
			}
			fireVisitIterateableElement(pvValue, Constants.TYPE_ITERATEABLE, pvPath, Constants.ITERATOR_END);
		}
	}
	
	private void mapWalker(Map pvMap, String pvPath, boolean pvWithBrackets, boolean pvCancel) {
		if (pvCancel == false) {
			fireVisitIterateableElement(pvMap, Constants.TYPE_MAP, pvPath, Constants.ITERATOR_BEGIN);
			Iterator lvIterator = pvMap.entrySet().iterator();
			while (lvIterator.hasNext()) {
				Map.Entry lvEntry = (Map.Entry) lvIterator.next();
				Object lvKey = lvEntry.getKey();
				Object lvValue = lvEntry.getValue();
				StringBuffer sb = new StringBuffer(pvPath);
				if (pvWithBrackets) {
					sb.append("(").append(lvKey).append(")");
					sb.append(".");
				} else {
					sb.append(lvKey);
				}
				walkInternal(lvKey, Constants.INVALID_INDEX, lvValue, sb.toString());
			}
			fireVisitIterateableElement(pvMap, Constants.TYPE_MAP, pvPath, Constants.ITERATOR_END);
		}
	}


}
