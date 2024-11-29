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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;

/**
 *
 * @author a.mueller
 * @since 07.08.2009
 */
public class CacheMatcher extends FieldMatcherBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

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
		FieldMatcher fieldMatcher = getProtectedFieldMatcher(matching);
		return fieldMatcher == null? null:fieldMatcher.getField();
	}
    public FieldMatcher getProtectedFieldMatcher(Matching matching){
        String protectedPropertyName = getProtectedPropertyName();
        return matching.getFieldMatcher(protectedPropertyName);
    }

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

	public ReplaceMode getCacheReplaceMode() {
		return cacheReplaceMode;
	}
	public void setCacheReplaceMode(ReplaceMode cacheReplaceMode) {
		this.cacheReplaceMode = cacheReplaceMode;
	}

	public List<String> getReplacedProperties() {
		return replacedProperties;
	}
	public void setReplacedProperties(List<String> replacedProperties) {
		this.replacedProperties = replacedProperties;
	}

	public MatchMode getReplaceMatchMode() {
		return replaceMatchMode;
	}
	public void setReplaceMatchMode(MatchMode replaceMatchMode) {
		this.replaceMatchMode = replaceMatchMode;
	}
}