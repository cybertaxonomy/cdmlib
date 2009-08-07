// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeException;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * @author a.mueller
 * @created 06.08.2009
 * @version 1.0
 */
public class DefaultMatchStrategy extends StrategyBase implements IMatchStrategy {
	private static final long serialVersionUID = 5045874493910155162L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefaultMatchStrategy.class);

	final static UUID uuid = UUID.fromString("69467b70-07ec-43a6-b779-3ec8d013837b");
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid;
	}
	
	/**
	 * @return the merge class
	 */
	public Class<? extends CdmBase> getMatchClass() {
		return matchClass;
	}
	
//	abstract protected UUID getUuid();
	
	public static DefaultMatchStrategy NewInstance(Class<? extends CdmBase> matchClazz){
		return new DefaultMatchStrategy(matchClazz);
	}
	
	protected Map<String, MatchMode> matchModeMap = new HashMap<String, MatchMode>();
	protected MatchMode defaultMatchMode = MatchMode.EQUAL;
	protected MatchMode defaultCollectionMatchMode = MatchMode.IGNORE;
	
	protected Class<? extends CdmBase> matchClass;
	protected Map<String, Field> matchFields;
	
	protected DefaultMatchStrategy(Class<? extends CdmBase> matchClazz) {
		super();
		if (matchClazz == null){
			throw new IllegalArgumentException("Match class must not be null");
		}
		this.matchClass = matchClazz;
		initMatchModeMap();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.match.IMatchStrategy#setMatchMode(java.lang.String, eu.etaxonomy.cdm.strategy.match.MatchMode)
	 */
	public void setMatchMode(String propertyName, MatchMode matchMode)
			throws MatchException {
		if (matchFields.containsKey(propertyName)){
			//checkIdentifier(propertyName, matchMode);
			matchModeMap.put(propertyName, matchMode);
		}else{
			throw new MatchException("The class " + matchClass.getName() + " does not contain a field named " + propertyName);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.match.IMatchStrategy#getMatchMode(java.lang.String)
	 */
	public MatchMode getMatchMode(String propertyName) {
		MatchMode result = matchModeMap.get(propertyName);
		if (result == null){
			Field field = matchFields.get(propertyName);
			if (isCollection(field.getType())){
				return defaultCollectionMatchMode;
			}else{
				return defaultMatchMode;
			}
		}else{
			return result;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.match.IMatchStrategy#invoke(eu.etaxonomy.cdm.strategy.match.IMatchable, eu.etaxonomy.cdm.strategy.match.IMatchable)
	 */
	public <T extends IMatchable> boolean invoke(T matchFirst, T matchSecond)
			throws MatchException {
		boolean result = true;
		if (matchFirst == null || matchSecond == null){
			result = false;
		}else if (matchFirst == matchSecond){
			result = true;
		}else{
			//matchFirst != matchSecond != null
			try {
	 			for (Field field : matchFields.values()){
					Class<?> fieldType = field.getType();
					if (isPrimitive(fieldType)){
						result &= matchPrimitiveField(matchFirst, matchSecond, field);
					}else if (fieldType == String.class ){
						result &= matchPrimitiveField(matchFirst, matchSecond, field);
					}else if(isUserType(fieldType)){
						result &= matchPrimitiveField(matchFirst, matchSecond, field);
					}else if(fieldType == UUID.class){
						result &= matchPrimitiveField(matchFirst, matchSecond, field);
					}else if(isSingleCdmBaseObject(fieldType)){
						result &= matchPrimitiveField(matchFirst, matchSecond, field);
					}else if (isCollection(fieldType)){
						result &= matchCollectionField(matchFirst, matchSecond, field);
					}else if(fieldType.isInterface()){
						result &= matchInterfaceField(matchFirst, matchSecond, field);
					}else{
						throw new RuntimeException("Unknown Object type for matching: " + fieldType);
					}
				}
			} catch (Exception e) {
				throw new MatchException("Merge Exception in invoke", e);
			}
		}
		return result;

	}

		
	/**
	 * @throws Exception 
	 * 
	 */
	private <T extends IMatchable> boolean matchPrimitiveField(T mergeFirst, T mergeSecond, Field field) throws Exception {
		String propertyName = field.getName();
		Class<?> fieldType = field.getType();
		MatchMode matchMode =  this.getMatchMode(propertyName);
		
		Object value1 = field.get(mergeFirst);
		Object value2 = field.get(mergeSecond);
		boolean result = matchMode.matches(value1, value2);
		System.out.println(propertyName + ": " + matchMode + ", " + fieldType.getName()+ ": " + result);
		return result;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private <T extends IMatchable> boolean matchCollectionField(T mergeFirst, T mergeSecond, Field field) throws Exception {
		String propertyName = field.getName();
		Class<?> fieldType = field.getType();
		MatchMode matchMode =  this.getMatchMode(propertyName);
		
		Object value1 = field.get(mergeFirst);
		Object value2 = field.get(mergeSecond);
		boolean result = matchMode.matches(value1, value2);
		System.out.println(propertyName + ": " + matchMode + ", " + fieldType.getName());
		return result;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private <T extends IMatchable> boolean matchInterfaceField(T mergeFirst, T mergeSecond, Field field) throws Exception {
		String propertyName = field.getName();
		Class<?> fieldType = field.getType();
		MatchMode matchMode =  this.getMatchMode(propertyName);
		
		Object value1 = field.get(mergeFirst);
		Object value2 = field.get(mergeSecond);
		boolean result = matchMode.matches(value1, value2);
		System.out.println(propertyName + ": " + matchMode + ", " + fieldType.getName());
		return result;
	}
	
	
	/**
	 * 
	 */
	private void initMatchModeMap() {
		boolean includeStatic = false;
		boolean includeTransient = false;
		boolean makeAccessible = true;
		this.matchFields = getAllNonStaticNonTransientFields(matchClass, includeStatic, includeTransient, makeAccessible);
		for (Field field: matchFields.values()){
			MatchMode matchMode = null;
			for (Annotation annotation : field.getAnnotations()){
				if (annotation.annotationType() == Match.class){
					matchMode = ((Match)annotation).value();
				}
			}
			if (matchMode == null){
				if (isCollection(field.getType())){
					matchMode = defaultCollectionMatchMode;
				}else{
					matchMode = defaultMatchMode;
				}	
			}
			matchModeMap.put(field.getName(), matchMode);
		}
	}

	
}
