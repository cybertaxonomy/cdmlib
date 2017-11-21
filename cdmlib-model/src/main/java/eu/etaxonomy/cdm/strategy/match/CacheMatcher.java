/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;

/**
 * @author a.mueller
 * @created 07.08.2009
 */
public class CacheMatcher extends FieldMatcherBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CacheMatcher.class);

	private ReplaceMode cacheReplaceMode;
	private List<String> replacedProperties;
	private MatchMode replaceMatchMode;

	public static CacheMatcher NewInstance(Field field, ReplaceMode replaceMode, String[] replacedProperties, MatchMode replaceMatchMode){
		return new CacheMatcher(field.getName(), field, MatchMode.CACHE, replaceMode, Arrays.asList(replacedProperties), replaceMatchMode);
	}

	private CacheMatcher (String propertyname, Field field, MatchMode matchMode, ReplaceMode replaceMode, List<String> replacedProperties, MatchMode replaceMatchMode){
		super(propertyname, field, matchMode);
		this.replacedProperties = replacedProperties;
		this.cacheReplaceMode = replaceMode;
		this.replaceMatchMode = replaceMatchMode;
	}

	public Field getProtectedField(Matching matching){
		String protectedPropertyName = getProtectedPropertyName();
		FieldMatcher fieldMatcher = matching.getFieldMatcher(protectedPropertyName);
		return fieldMatcher.getField();
	}

	/**
	 * @return
	 */
	public String getProtectedPropertyName() {
		String protectedPropertyName = "protected" + this.getPropertyName().substring(0, 1).toUpperCase() + this.getPropertyName().substring(1);
		return protectedPropertyName;
	}



	public List<DoubleResult<String, MatchMode>> getReplaceMatchModes(Matching matching) throws MatchException{
		List<DoubleResult<String, MatchMode>> result = new ArrayList<>();
		List<FieldMatcher> fieldMatchers = matching.getFieldMatchers(true);
		for (FieldMatcher fieldMatcher : fieldMatchers){
			String propertyName = fieldMatcher.getPropertyName();
			if (isReplaceProperty(propertyName)){
				result.add(new DoubleResult<>(propertyName, replaceMatchMode));
			}
		}
		return result;
	}


	/**
	 * @param fieldMatcher
	 * @return
	 * @throws MatchException
	 */
	private boolean isReplaceProperty(String propertyName) throws MatchException {
		if (this.cacheReplaceMode == ReplaceMode.NONE){
			return false;
		}else if (this.cacheReplaceMode == ReplaceMode.ALL){
			return true;
		}else if (this.cacheReplaceMode == ReplaceMode.DEFINED){
			return this.replacedProperties.contains(propertyName);
		}else if (this.cacheReplaceMode == ReplaceMode.DEFINED_REVERSE){
			return ! this.replacedProperties.contains(propertyName);
		}else{
			throw new MatchException("ReplaceMode not supported: " + this.cacheReplaceMode);
		}
	}

	/**
	 * @return the cacheReplaceMode
	 */
	public ReplaceMode getCacheReplaceMode() {
		return cacheReplaceMode;
	}

	/**
	 * @param cacheReplaceMode the cacheReplaceMode to set
	 */
	public void setCacheReplaceMode(ReplaceMode cacheReplaceMode) {
		this.cacheReplaceMode = cacheReplaceMode;
	}



	/**
	 * @return the replacedProperties
	 */
	public List<String> getReplacedProperties() {
		return replacedProperties;
	}

	/**
	 * @param replacedProperties the replacedProperties to set
	 */
	public void setReplacedProperties(List<String> replacedProperties) {
		this.replacedProperties = replacedProperties;
	}

	/**
	 * @return the replaceMatchMode
	 */
	public MatchMode getReplaceMatchMode() {
		return replaceMatchMode;
	}

	/**
	 * @param replaceMatchMode the replaceMatchMode to set
	 */
	public void setReplaceMatchMode(MatchMode replaceMatchMode) {
		this.replaceMatchMode = replaceMatchMode;
	}



}
