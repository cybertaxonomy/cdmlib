/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 07.08.2009
 * @version 1.0
 */
public class Matching {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Matching.class);
	
	private SortedMap<String, FieldMatcher> fieldMatchers = new TreeMap<String, FieldMatcher>();
	private SortedMap<String, FieldMatcher> tmpFieldMatchers = new TreeMap<String, FieldMatcher>();
	private List<CacheMatcher> cacheMatchers = new ArrayList<CacheMatcher>();
	

	public Matching setFieldMatcher(FieldMatcher fieldMatcher){
		return setFieldMatcher(fieldMatcher, false);
	}
	
	public Matching setFieldMatcher(FieldMatcher fieldMatcher,boolean temporary){
		String propertyName = fieldMatcher.getPropertyName();
		if (temporary && ! fieldMatchers.containsKey(propertyName)){
			tmpFieldMatchers.put(propertyName, fieldMatcher);	
		}else{
			fieldMatchers.put(propertyName, fieldMatcher);
		}
		return this;
	}

	public Matching addCacheMatcher(CacheMatcher cacheMatcher){
		cacheMatchers.add(cacheMatcher);
		return this;
	}

	/**
	 * @return the fieldMatchers
	 */
	public List<FieldMatcher> getFieldMatchers(boolean includeTemporary) {
		List<FieldMatcher> result = new ArrayList<FieldMatcher>();
		for (FieldMatcher fieldMatcher : fieldMatchers.values()){
			result.add(fieldMatcher);
		}
		if (includeTemporary){
			for (FieldMatcher fieldMatcher : tmpFieldMatchers.values()){
				result.add(fieldMatcher);
			}	
		}
		return result;
	}
	
	/**
	 * @return the fieldMatchers
	 */
	public FieldMatcher getFieldMatcher(String propertyName) {
		return fieldMatchers.get(propertyName);
	}
	
	public boolean exists(String propertyName){
		return getFieldMatcher(propertyName) != null;
	}

	/**
	 * @return the groupMatchers
	 */
	public List<CacheMatcher> getCacheMatchers() {
		return cacheMatchers;
	}

	public void deleteTemporaryMatchers(){
		tmpFieldMatchers = new TreeMap<String, FieldMatcher>();
	}
	
	
}
