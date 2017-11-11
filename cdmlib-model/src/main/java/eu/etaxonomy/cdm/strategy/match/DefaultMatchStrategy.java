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
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;

/**
 * @author a.mueller
 * @created 06.08.2009
 * @version 1.0
 */
public class DefaultMatchStrategy extends StrategyBase implements IMatchStrategy {
	private static final long serialVersionUID = 5045874493910155162L;
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
	@Override
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
	@Override
    public Matching getMatching() {
		return matching;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.match.IMatchStrategy#setMatchMode(java.lang.String, eu.etaxonomy.cdm.strategy.match.MatchMode)
	 */
	@Override
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
	@Override
    public MatchMode getMatchMode(String propertyName) {
		FieldMatcher fieldMatcher = matching.getFieldMatcher(propertyName);
		return fieldMatcher == null ? defaultMatchMode : fieldMatcher.getMatchMode();
	}

	@Override
    public <T extends IMatchable> boolean invoke(T matchFirst, T matchSecond)
			throws MatchException {
		boolean result = true;
		if (matchFirst == null || matchSecond == null){
			return false;
		}else if (matchFirst == matchSecond){
			return true;
		}else if (matchFirst.getClass() != matchSecond.getClass()){
			matchFirst = HibernateProxyHelper.deproxy(matchFirst);
			matchSecond = HibernateProxyHelper.deproxy(matchSecond);
			if (matchFirst.getClass() != matchSecond.getClass()){
				return false;
			}
		}
		matching.deleteTemporaryMatchers(); //just in case they are not yet deleted during last invoke
		if (! matchClass.isAssignableFrom(matchFirst.getClass()) ){
			throw new MatchException("Match object are of different type than the match class (" + matchClass + ") this match strategy was created with");
		}else if (matchClass != matchFirst.getClass()){
			initializeSubclass(matchFirst.getClass());
		}
		try {
			result = invokeChecked(matchFirst, matchSecond, result);
		}catch (MatchException e) {
			throw e;
		}finally{
			matching.deleteTemporaryMatchers();
		}
		return result;
	}

	/**
	 * @param class1
	 */
	private void initializeSubclass(Class<? extends IMatchable> instanceClass) {
		Map<String, Field> subClassFields = CdmUtils.getAllFields(instanceClass, matchClass, false, false, true, false);
		for (Field field: subClassFields.values()){
			initField(field, true);
		}
	}

	/**
	 * @param <T>
	 * @param matchFirst
	 * @param matchSecond
	 * @param result
	 * @return
	 * @throws MatchException
	 */
	private <T extends IMatchable> boolean invokeChecked(T matchFirst, T matchSecond,
			boolean result) throws MatchException {
		//matchFirst != matchSecond != null
		try {
			Map<String, List<MatchMode>> replaceMatchers = new HashMap<>();
			for (CacheMatcher cacheMatcher: matching.getCacheMatchers()){
				Field protectedField = cacheMatcher.getProtectedField(matching);
				boolean protected1 = protectedField.getBoolean(matchFirst);
				boolean protected2 = protectedField.getBoolean(matchFirst);
				if (protected1 != protected2){
					return false;
				}else if (protected1 == false){
					//ignore
				}else{
					String cache1 = (String)cacheMatcher.getField().get(matchFirst);
					String cache2 = (String)cacheMatcher.getField().get(matchSecond);
					result = cacheMatcher.getMatchMode().matches(cache1, cache2, null);
					if (result == false){
						return false;
					}
					List<DoubleResult<String, MatchMode>> replacementModes = cacheMatcher.getReplaceMatchModes(matching);
					for (DoubleResult<String, MatchMode> replacementMode: replacementModes ){
						String propertyName = replacementMode.getFirstResult();
						List<MatchMode> replaceMatcherList = replaceMatchers.get(propertyName);
						if (replaceMatcherList == null){
							replaceMatcherList = new ArrayList<MatchMode>();
							replaceMatchers.put(propertyName, replaceMatcherList);
						}
						replaceMatcherList.add(replacementMode.getSecondResult());
					}
				}
			}
			for (FieldMatcher fieldMatcher : matching.getFieldMatchers(true)){
				Field field = fieldMatcher.getField();
				List<MatchMode> replaceModeList = replaceMatchers.get(fieldMatcher.getPropertyName());
				if (replaceModeList == null){
					replaceModeList = new ArrayList<>();
				}
				Class<?> fieldType = field.getType();
				logger.debug(field.getName() + ": ");
				if (isPrimitive(fieldType)){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if (fieldType == String.class ){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if (fieldType == Integer.class ){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if(isUserType(fieldType)){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if(fieldType == UUID.class){
					//result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if(fieldType == URI.class){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if(fieldType == DOI.class){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if(isSingleCdmBaseObject(fieldType)){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if (isCollection(fieldType)){
					result &= matchCollectionField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if(fieldType.isInterface()){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else if(fieldType.isEnum()){
					result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
				}else{
					throw new RuntimeException("Unknown Object type for matching: " + fieldType);
				}
//				if (result == false){
//					return result;
//				}
			}
		} catch (Exception e) {
			throw new MatchException("Match Exception in invoke", e);
		}
		return result;
	}


	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMatchable> boolean matchPrimitiveField(T matchFirst, T matchSecond, FieldMatcher fieldMatcher, List<MatchMode> replaceModeList) throws Exception {
		Field field = fieldMatcher.getField();
		Object value1 = checkEmpty(field.get(matchFirst));
		Object value2 = checkEmpty(field.get(matchSecond));
		IMatchStrategy matchStrategy = fieldMatcher.getMatchStrategy();
		boolean result = fieldMatcher.getMatchMode().matches(value1, value2, matchStrategy);
		for (MatchMode replaceMode : replaceModeList){
			if (result == true){
				break;
			}
			result |= replaceMode.matches(value1, value2, null);
		}
		if (logger.isDebugEnabled()){logger.debug(fieldMatcher.getMatchMode() + ", " + field.getType().getName()+ ": " + result);}
		return result;
	}

	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMatchable> boolean matchCollectionField(T matchFirst, T matchSecond, FieldMatcher fieldMatcher, List<MatchMode> replaceModeList) throws Exception {
		boolean result;
		Field field = fieldMatcher.getField();
		Collection<?> value1 = (Collection)field.get(matchFirst);
		Collection<?> value2 = (Collection)field.get(matchSecond);
		MatchMode matchMode = fieldMatcher.getMatchMode();
		Class<?> fieldType = fieldMatcher.getField().getType();
		IMatchStrategy matchStrategy = fieldMatcher.getMatchStrategy();
		if (matchMode.isMatch()){
			Class collectionType = getTypeOfSet(field);
			if (! IMatchable.class.isAssignableFrom(collectionType)){
				//TODO
				return fieldMatcher.getMatchMode().matches(value1, value2, matchStrategy);
			}
			if (matchStrategy == null){
				matchStrategy = DefaultMatchStrategy.NewInstance(collectionType);
			}
			if (Set.class.isAssignableFrom(fieldType)){
				result = matchSet(value1, value2, matchStrategy);
			}else if (List.class.isAssignableFrom(fieldType)){
				result = matchList(value1, value2, matchStrategy);
			}else{
				throw new MatchException("Collection type not yet supported: " + fieldType);
			}
		}else{
			result = fieldMatcher.getMatchMode().matches(value1, value2, matchStrategy);
		}
		//cache replace modes
		for (MatchMode replaceMode : replaceModeList){
			if (result == true){
				break;
			}
			result |= replaceMode.matches(value1, value2, null);
		}
		logger.debug(fieldMatcher.getMatchMode() + ", " + field.getType().getName()+ ": " + result);
		return result;
	}


	/**
	 * @param object
	 * @return
	 */
	private Object checkEmpty(Object object) {
		if (object instanceof String){
			if (StringUtils.isBlank((String)object)){
				return null;
			}
		}
		return HibernateProxyHelper.deproxy(object);
	}

	/**
	 * @param value1
	 * @param matchStrategy
	 * @return
	 * @throws MatchException
	 */
	private boolean matchSet(Collection value1, Collection value2, IMatchStrategy matchStrategy)
			throws MatchException {
		boolean result;
		Set<IMatchable> set1 = (Set<IMatchable>)value1;
		Set<IMatchable> set2 = (Set<IMatchable>)value2;
		if (set1.size()!= set2.size()){
			return false;
		}
		result = true;
		for (IMatchable setItem1: set1){
			boolean matches = false;
			for (IMatchable setItem2: set2){
				matches |= matchStrategy.invoke(setItem1, setItem2);
			}
			if (matches == false){
				return false;
			}
		}
		return result;
	}

	/**
	 * @param value1
	 * @param matchStrategy
	 * @return
	 * @throws MatchException
	 */
	private boolean matchList(Collection value1, Collection value2, IMatchStrategy matchStrategy)
			throws MatchException {
		boolean result;
		List<IMatchable> list1 = (List<IMatchable>)value1;
		List<IMatchable> list2 = (List<IMatchable>)value2;
		if(list1 == null && list2 == null) {
			return true;
		}

		if ((list1 != null && list2 == null) || (list1 == null && list2 != null) || (list1.size()!= list2.size())){
			return false;
		}

		result = true;
		for (int i = 0; i < list1.size(); i++){
			IMatchable listObject1 = list1.get(i);
			IMatchable listObject2 = list2.get(i);
			if (! matchStrategy.invoke(listObject1, listObject2)){
				return false;
			}
		}
		return result;
	}

	private Class<?> getTypeOfSet(Field field) throws MatchException{
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType/*Impl*/){
			ParameterizedType paraType = (ParameterizedType)genericType;
			paraType.getRawType();
			Type[] arguments = paraType.getActualTypeArguments();
			if (arguments.length == 1){
				Class<?> collectionClass;
				try {
					if (arguments[0] instanceof Class){
						return (Class)arguments[0];
					}else if(arguments[0] instanceof TypeVariable/*Impl*/){
						TypeVariable typeVariable = (TypeVariable)arguments[0];
						GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
						return (Class)genericDeclaration;
					}else{
						logger.warn("Unknown Type");
						throw new MatchException("");
					}
				} catch (Exception e) {
					logger.warn(e.getMessage());
					throw new MatchException("");
				}
			}else{
				logger.warn("Length of arguments <> 1");
				throw new MatchException("");
			}
		}else{
			logger.warn("Not a generic type of type ParameterizedTypeImpl");
			throw new MatchException("Collection type could not be determined. Generic type is not of type ParamterizedTypeImpl");
		}
	}


	/**
	 *
	 */
	private void initMapping() {
		boolean includeStatic = false;
		boolean includeTransient = false;
		boolean makeAccessible = true;
		matchFields = CdmUtils.getAllFields(matchClass, CdmBase.class, includeStatic, includeTransient, makeAccessible, true);
		for (Field field: matchFields.values()){
			initField(field, false);
		}
	}

	/**
	 * Initializes the matching for a single field
	 * @param field
	 */
	private void initField(Field field, boolean temporary) {
		MatchMode matchMode = null;
		IMatchStrategy matchStrategy = null;
		for (Annotation annotation : field.getAnnotations()){
			if (annotation.annotationType() == Match.class){
				Match match = ((Match)annotation);
				matchMode = match.value();
				if (matchMode == MatchMode.CACHE){
					ReplaceMode replaceMode = match.cacheReplaceMode();
					String[] cachePropertyReplaces = match.cacheReplacedProperties();
					MatchMode replaceMatchMode = match.replaceMatchMode();
					matching.addCacheMatcher(CacheMatcher.NewInstance(field, replaceMode, cachePropertyReplaces, replaceMatchMode));

				}else{
					matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode), temporary);
				}
			}
		}
		Class fieldType = field.getType();
		if (matchMode == null){
			if (isCollection(fieldType)){
				matchMode = defaultCollectionMatchMode;
				matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode), temporary);
			}else if (fieldType.isInterface()){
				//TODO could be handled more sophisticated
				matchMode = defaultMatchMatchMode;
				matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode, matchStrategy), temporary);
			}else if (isSingleCdmBaseObject(fieldType)){
				if (IMatchable.class.isAssignableFrom(fieldType)){
					matchMode = defaultMatchMatchMode;
					if (matchStrategy == null){
						if (fieldType == this.matchClass){
							matchStrategy = this;
						}else{
							matchStrategy = DefaultMatchStrategy.NewInstance(fieldType);
						}
					}
					matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode, matchStrategy), temporary);
				}else{
					matchMode = defaultMatchMode;
					matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode), temporary);
				}
			}else{
				matchMode = defaultMatchMode;
				matching.setFieldMatcher(FieldMatcher.NewInstance(field, matchMode), temporary);
			}
		}
	}


	@Override
	public Set<String> getMatchFieldPropertyNames() {
		return matchFields.keySet();
	}


}
