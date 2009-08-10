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
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;

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
	
	public static DefaultMatchStrategy NewInstance(Class<? extends IMatchable> matchClazz){
		return new DefaultMatchStrategy(matchClazz);
	}
	
//	protected Map<String, MatchMode> matchModeMap = new HashMap<String, MatchMode>();
	protected MatchMode defaultMatchMode = MatchMode.EQUAL;
	protected MatchMode defaultCollectionMatchMode = MatchMode.IGNORE;
	protected MatchMode defaultMatchMatchMode = MatchMode.MATCH;
	
	protected Class<? extends IMatchable> matchClass;
	protected Map<String, Field> matchFields;
	protected Matching matching = new Matching();
	
	protected DefaultMatchStrategy(Class<? extends IMatchable> matchClazz) {
		super();
		if (matchClazz == null){
			throw new IllegalArgumentException("Match class must not be null");
		}
		this.matchClass = matchClazz;
		initMapping();
	}
	
	/**
	 * @return the merge class
	 */
	public Class<? extends IMatchable> getMatchClass() {
		return matchClass;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.match.IMatchStrategy#getMatching()
	 */
	public Matching getMatching() {
		return matching;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.match.IMatchStrategy#setMatchMode(java.lang.String, eu.etaxonomy.cdm.strategy.match.MatchMode)
	 */
	public void setMatchMode(String propertyName, MatchMode matchMode)
			throws MatchException {
		if (matchFields.containsKey(propertyName)){
			FieldMatcher fieldMatcher = FieldMatcher.NewInstance(matchFields.get(propertyName), matchMode);
			matching.setFieldMatcher(fieldMatcher);
		}else{
			throw new MatchException("The class " + matchClass.getName() + " does not contain a field named " + propertyName);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.match.IMatchStrategy#getMatchMode(java.lang.String)
	 */
	public MatchMode getMatchMode(String propertyName) {
		FieldMatcher fieldMatcher = matching.getFieldMatcher(propertyName);
		return fieldMatcher.getMatchMode();
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
	 			for (FieldMatcher fieldMatcher : matching.getFieldMatchers()){
					Field field = fieldMatcher.getField();
					Class<?> fieldType = field.getType();
					if (isPrimitive(fieldType)){
						result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher);
					}else if (fieldType == String.class ){
						result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher);
					}else if(isUserType(fieldType)){
						result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher);
					}else if(fieldType == UUID.class){
						result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher);
					}else if(isSingleCdmBaseObject(fieldType)){
						result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher);
					}else if (isCollection(fieldType)){
						result &= matchCollectionField(matchFirst, matchSecond, fieldMatcher);
					}else if(fieldType.isInterface()){
						result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher);
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
	private <T extends IMatchable> boolean matchPrimitiveField(T mergeFirst, T mergeSecond, FieldMatcher fieldMatcher) throws Exception {
		String propertyName = fieldMatcher.getPropertyName();
		Field field = fieldMatcher.getField();
		Object value1 = checkEmpty(field.get(mergeFirst));
		Object value2 = checkEmpty(field.get(mergeSecond));
		IMatchStrategy matchStrategy = fieldMatcher.getMatchStrategy();
		boolean result = fieldMatcher.getMatchMode().matches(value1, value2, matchStrategy);
		System.out.println(propertyName + ": " + fieldMatcher.getMatchMode() + ", " + field.getType().getName()+ ": " + result);
		return result;
	}

	/**
	 * @param object
	 * @return
	 */
	private Object checkEmpty(Object object) {
		if (object instanceof String){
			if (CdmUtils.isEmpty((String)object)){
				return null;
			}
		}
		return object;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private <T extends IMatchable> boolean matchCollectionField(T mergeFirst, T mergeSecond, FieldMatcher fieldMatcher) throws Exception {
		String propertyName = fieldMatcher.getPropertyName();
		Field field = fieldMatcher.getField();
		Object value1 = field.get(mergeFirst);
		Object value2 = field.get(mergeSecond);
		IMatchStrategy matchStrategy = fieldMatcher.getMatchStrategy();
		boolean result = fieldMatcher.getMatchMode().matches(value1, value2, matchStrategy);
		System.out.println(propertyName + ": " + fieldMatcher.getMatchMode() + ", " + field.getType().getName()+ ": " + result);
		return result;
	}
	
	
	/**
	 * 
	 */
	private void initMapping() {
		boolean includeStatic = false;
		boolean includeTransient = false;
		boolean makeAccessible = true;
		matchFields = CdmUtils.getAllFields(matchClass, CdmBase.class, includeStatic, includeTransient, makeAccessible);
		for (Field field: matchFields.values()){
			MatchMode matchMode = null;
			IMatchStrategy matchStrategy = null;
			for (Annotation annotation : field.getAnnotations()){
				if (annotation.annotationType() == Match.class){
					Match match = ((Match)annotation);
					matchMode = match.value();
					//Class strat = match.matchStrategy();
					Class<CdmBase> fieldType = (Class<CdmBase>)field.getType();
					matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode));
				}
			}
			Class fieldType = field.getType();
			if (matchMode == null){
				if (isCollection(fieldType)){
					matchMode = defaultCollectionMatchMode;
					matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode));
				}else if (isSingleCdmBaseObject(fieldType)){
					if (IMatchable.class.isAssignableFrom(fieldType)){
						matchMode = defaultMatchMatchMode;
						if (matchStrategy == null){
							matchStrategy = DefaultMatchStrategy.NewInstance(fieldType);
						}
						matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode, matchStrategy));
					}else{
						matchMode = defaultMatchMode;
						matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode));
					}
				}else{
					matchMode = defaultMatchMode;
					matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode));
				}	
			}
		}
	}

	
}
