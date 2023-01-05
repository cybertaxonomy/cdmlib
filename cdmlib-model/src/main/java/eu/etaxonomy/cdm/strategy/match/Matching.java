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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 07.08.2009
 */
public class Matching {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private SortedMap<String, FieldMatcher> fieldMatchers = new TreeMap<>();
	private SortedMap<String, FieldMatcher> tmpFieldMatchers = new TreeMap<>();
	private List<CacheMatcher> cacheMatchers = new ArrayList<>();


	public Matching addFieldMatcher(FieldMatcher fieldMatcher){
		return addFieldMatcher(fieldMatcher, false);
	}

	public Matching addFieldMatcher(FieldMatcher fieldMatcher, boolean temporary){
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
		List<FieldMatcher> result = new ArrayList<>();
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
		tmpFieldMatchers = new TreeMap<>();
	}


}
