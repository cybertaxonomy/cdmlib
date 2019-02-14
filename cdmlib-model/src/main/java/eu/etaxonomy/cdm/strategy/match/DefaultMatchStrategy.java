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
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.molecular.SequenceString;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;

/**
 * @author a.mueller
 * @since 06.08.2009
 */
public class DefaultMatchStrategy extends StrategyBase implements IMatchStrategyEqual, IParsedMatchStrategy {
	private static final long serialVersionUID = 5045874493910155162L;
	private static final Logger logger = Logger.getLogger(DefaultMatchStrategy.class);

	final static UUID uuid = UUID.fromString("69467b70-07ec-43a6-b779-3ec8d013837b");

	public static DefaultMatchStrategy NewInstance(Class<? extends IMatchable> matchClazz){
		return new DefaultMatchStrategy(matchClazz);
	}

    protected MatchMode defaultMatchMode = IMatchStrategyEqual.DEFAULT_MATCH_MODE;
    protected MatchMode defaultCollectionMatchMode = IMatchStrategyEqual.DEFAULT_COLLECTION_MATCH_MODE;
    protected MatchMode defaultMatchMatchMode = IMatchStrategyEqual.DEFAULT_MATCH_MATCH_MODE;


	//for some reason this does not work, always has null
//    private MatchMode defaultMatchMode = IMatchStrategy.defaultMatchMode;
//    private MatchMode defaultCollectionMatchMode = IMatchStrategy.defaultCollectionMatchMode;
//    private MatchMode defaultMatchMatchMode = IMatchStrategy.defaultMatchMatchMode;

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

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    public Matching getMatching() {
		return matching;
	}

	@Override
    public void setMatchMode(String propertyName, MatchMode matchMode)
			throws MatchException {
	    setMatchMode(propertyName, matchMode, null);
	}

    @Override
    public void setMatchMode(String propertyName, MatchMode matchMode, IMatchStrategy matchStrategy)
            throws MatchException {
        if (matchFields.containsKey(propertyName)){
            FieldMatcher fieldMatcher = FieldMatcher.NewInstance(matchFields.get(propertyName), matchMode, matchStrategy);
            matching.addFieldMatcher(fieldMatcher);
        }else{
            throw new MatchException("The class " + matchClass.getName() + " does not contain a field named " + propertyName);
        }
    }

//    public MatchMode getMatchMode(String propertyName) {
//		FieldMatcher fieldMatcher = matching.getFieldMatcher(propertyName);
//		return fieldMatcher == null ? getDefaultMatchMode() : fieldMatcher.getMatchMode();
//	}

    @Override
    public <T extends IMatchable> MatchResult invoke(T matchFirst, T matchSecond)
            throws MatchException {
        return invoke(matchFirst, matchSecond, false);
    }

    @Override
    public <T extends IMatchable> MatchResult invoke(T matchFirst, T matchSecond,
            boolean failAll) throws MatchException {
        MatchResult matchResult = new MatchResult();
        invoke(matchFirst, matchSecond, matchResult, failAll);
        return matchResult;
    }

	@Override
    public <T extends IMatchable> void invoke(T matchFirst, T matchSecond,
            MatchResult matchResult, boolean failAll) throws MatchException {
		if (matchFirst == null || matchSecond == null){
			matchResult.addNullMatching(matchFirst, matchSecond);
		    return;
		}else if (matchFirst == matchSecond){
			return;
		}else if (matchFirst.getClass() != matchSecond.getClass()){
			matchFirst = HibernateProxyHelper.deproxy(matchFirst);
			matchSecond = HibernateProxyHelper.deproxy(matchSecond);
			if (matchFirst.getClass() != matchSecond.getClass()){
				matchResult.addNoClassMatching(matchFirst.getClass(), matchSecond.getClass());
			    return;
			}
		}
		matching.deleteTemporaryMatchers(); //just in case they are not yet deleted during last invoke
		if (! matchClass.isAssignableFrom(matchFirst.getClass()) ){
			throw new MatchException("Match object are of different type than the match class (" + matchClass + ") this match strategy was created with");
		}else if (matchClass != matchFirst.getClass()){
			initializeSubclass(matchFirst.getClass());
		}
		try {
			invokeChecked(matchFirst, matchSecond, matchResult, failAll);
		}catch (MatchException e) {
			throw e;
		}finally{
			matching.deleteTemporaryMatchers();
		}
		return;
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
	private <T extends IMatchable> void invokeChecked(T matchFirst, T matchSecond,
			MatchResult result, boolean failAll) throws MatchException {
		//matchFirst != matchSecond != null
		try {
			Map<String, List<MatchMode>> replaceMatchers = new HashMap<>();
			for (CacheMatcher cacheMatcher: matching.getCacheMatchers()){
				matchSingleCache(matchFirst, matchSecond, result, replaceMatchers, cacheMatcher, failAll);
				if (result.isFailed() && !failAll){
				    return;
				}
			}
			for (FieldMatcher fieldMatcher : matching.getFieldMatchers(true)){
				matchSingleField(matchFirst, matchSecond, result,
				        replaceMatchers, fieldMatcher, failAll);
				if (result.isFailed() && !failAll){
				    break;
				}
			}
		} catch (Exception e) {
			throw new MatchException("Match Exception in invoke", e);
		}
		return;
	}

    /**
     * @param matchFirst
     * @param matchSecond
     * @param result
     * @param replaceMatchers
     * @param cacheMatcher
     * @return
     * @throws IllegalAccessException
     * @throws MatchException
     */
    protected <T extends IMatchable> void matchSingleCache(T matchFirst, T matchSecond,
            MatchResult result,
            Map<String, List<MatchMode>> replaceMatchers, CacheMatcher cacheMatcher, boolean failAll)
            throws IllegalAccessException, MatchException {

        FieldMatcher protectedFieldMatcher = cacheMatcher.getProtectedFieldMatcher(matching);
        Field protectedField = protectedFieldMatcher.getField();
        boolean protected1 = protectedField.getBoolean(matchFirst);
        boolean protected2 = protectedField.getBoolean(matchFirst);
        if (protected1 != protected2){
        	result.addNonMatching(protectedFieldMatcher, protected1, protected2);
            return;
        }else if (protected1 == false){
        	//ignore
        }else{
        	String cache1 = (String)cacheMatcher.getField().get(matchFirst);
        	String cache2 = (String)cacheMatcher.getField().get(matchSecond);
        	MatchResult matches = cacheMatcher.getMatchMode().matches(cache1, cache2, null, cacheMatcher.getPropertyName(), failAll);
			if (matches.isFailed()){
			    result.addSubResult(cacheMatcher.getPropertyName(), matches);
			    //addNonMatching(cacheMatcher);
				return;
			}
        	List<DoubleResult<String, MatchMode>> replacementModes = cacheMatcher.getReplaceMatchModes(matching);
        	for (DoubleResult<String, MatchMode> replacementMode: replacementModes ){
        		String propertyName = replacementMode.getFirstResult();
        		List<MatchMode> replaceMatcherList = replaceMatchers.get(propertyName);
        		if (replaceMatcherList == null){
        			replaceMatcherList = new ArrayList<>();
        			replaceMatchers.put(propertyName, replaceMatcherList);
        		}
        		replaceMatcherList.add(replacementMode.getSecondResult());
        	}
        }
        return;
    }

    /**
     * @param matchFirst
     * @param matchSecond
     * @param result
     * @param replaceMatchers
     * @param fieldMatcher
     * @return
     * @throws Exception
     */
    protected <T extends IMatchable> void matchSingleField(T matchFirst, T matchSecond,
            MatchResult matchResult,
            Map<String, List<MatchMode>> replaceMatchers, FieldMatcher fieldMatcher,
            boolean failAll) throws Exception {
        MatchResult fieldResult;
        Field field = fieldMatcher.getField();
        List<MatchMode> replaceModeList = replaceMatchers.get(fieldMatcher.getPropertyName());
        if (replaceModeList == null){
        	replaceModeList = new ArrayList<>();
        }
        Class<?> fieldType = field.getType();
        logger.debug(field.getName() + ": ");
        if (isPrimitive(fieldType)){
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else if (fieldType == String.class ){
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else if (fieldType == Integer.class ){
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else if(isUserType(fieldType)){
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else if(fieldType == UUID.class){
            fieldResult = MatchResult.SUCCESS();
        	//result &= matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList);
        }else if(fieldType == URI.class){
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else if(fieldType == DOI.class){
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else if(isSingleCdmBaseObject(fieldType)){
            matchResult.addPath(fieldMatcher.getPropertyName());
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
            matchResult.removePath();
        }else if (isCollection(fieldType)){
            fieldResult = matchCollectionField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else if(fieldType.isInterface()){
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else if(fieldType.isEnum()){
            fieldResult = matchPrimitiveField(matchFirst, matchSecond, fieldMatcher, replaceModeList, failAll);
        }else{
        	throw new RuntimeException("Unknown Object type for matching: " + fieldType);
        }
        if (fieldResult.isFailed()){
//					System.out.println(field.getName());
            matchResult.addSubResult(fieldResult);
            return;
        }
        return;
    }


	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMatchable> MatchResult matchPrimitiveField(T matchFirst, T matchSecond,
	        FieldMatcher fieldMatcher, List<MatchMode> replaceModeList, boolean failAll) throws Exception {
		Field field = fieldMatcher.getField();
		Object value1 = checkEmpty(field.get(matchFirst));
		Object value2 = checkEmpty(field.get(matchSecond));
		IMatchStrategy matchStrategy = fieldMatcher.getMatchStrategy();
		MatchMode matchMode = fieldMatcher.getMatchMode();
		MatchResult fieldResult = matchMode.matches(value1, value2, matchStrategy, fieldMatcher.getPropertyName(), failAll);
		fieldResult = makeReplaceModeMatching(fieldResult, replaceModeList, value1, value2, fieldMatcher, failAll);
		if (fieldResult.isFailed()){
		    if (logger.isDebugEnabled()){logger.debug(fieldMatcher.getMatchMode() + ", " + field.getType().getName()+ ": " + fieldResult.isSuccessful());}
		}
		return fieldResult;
	}

	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMatchable> MatchResult matchCollectionField(T matchFirst, T matchSecond,
	        FieldMatcher fieldMatcher, List<MatchMode> replaceModeList, boolean failAll) throws Exception {

	    MatchResult fieldResult = new MatchResult();
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
			    return fieldMatcher.getMatchMode().matches(value1, value2, matchStrategy, fieldMatcher.getPropertyName(), failAll);
			}
			if (matchStrategy == null){
				matchStrategy = DefaultMatchStrategy.NewInstance(collectionType);
			}
			if (Set.class.isAssignableFrom(fieldType)){
				matchSet(value1, value2, matchStrategy, fieldResult, fieldMatcher, failAll);
			}else if (List.class.isAssignableFrom(fieldType)){
				matchList(value1, value2, matchStrategy, fieldResult, fieldMatcher, failAll);
			}else{
				throw new MatchException("Collection type not yet supported: " + fieldType);
			}
		}else{
			fieldResult = fieldMatcher.getMatchMode().matches(value1, value2, matchStrategy, fieldMatcher.getPropertyName(), failAll);
		}
		fieldResult = makeReplaceModeMatching(fieldResult, replaceModeList, value1, value2, fieldMatcher, failAll);

		if (logger.isDebugEnabled()){logger.debug(fieldMatcher.getMatchMode() + ", " + field.getType().getName()+ ": " + fieldResult.isSuccessful());}
		return fieldResult;
	}


	/**
     * @param fieldResult
     * @param replaceModeList
	 * @param fieldMatcher
	 * @param value2
	 * @param value1
     * @return
	 * @throws MatchException
     */
    private MatchResult makeReplaceModeMatching(MatchResult fieldResult,
            List<MatchMode> replaceModeList, Object value1, Object value2, FieldMatcher fieldMatcher, boolean failAll) throws MatchException {
        if (fieldResult.isFailed()){
            for (MatchMode replaceMode : replaceModeList){
                //TODO is the property name correct here?
                MatchResult replaceResult = replaceMode.matches(value1, value2, null, fieldMatcher.getPropertyName(), failAll);
                if(replaceResult.isSuccessful()){
                    fieldResult = replaceResult;
                    break;
                }
            }
        }
        return fieldResult;
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
		}else if (object instanceof TimePeriod){
		    if ( ((TimePeriod)object).isEmpty()){
		        return null;
		    }
		}else if (object instanceof Contact){
            if ( ((Contact)object).isEmpty()){
                return null;
            }
        }else if (object instanceof Point){
            if ( ((Point)object).isEmpty()){
                return null;
            }
        }else if (object instanceof SequenceString){
            if ( ((SequenceString)object).isEmpty()){
                return null;
            }
        }else if (object instanceof LSID){
            if ( ((LSID)object).isEmpty()){
                return null;
            }
        }
		return HibernateProxyHelper.deproxy(object);
	}

	/**
	 * @param value1
	 * @param matchStrategy
	 * @param fieldMatcher
	 * @return
	 * @throws MatchException
	 */
	private void matchSet(Collection value1, Collection value2,
	        IMatchStrategy matchStrategy, MatchResult matchResult,
	        FieldMatcher fieldMatcher, boolean failAll)
            throws MatchException {

	    Set<IMatchable> set1 = (Set<IMatchable>)value1;
		Set<IMatchable> set2 = (Set<IMatchable>)value2;
		if (set1.size()!= set2.size()){
			matchResult.addNonMatching(fieldMatcher, set1.size(), set2.size());
		    return;
		}
		for (IMatchable setItem1: set1){
			boolean matches = false;
			for (IMatchable setItem2: set2){
			    MatchResult collectionResult = new MatchResult();
			    matchStrategy.invoke(setItem1, setItem2, collectionResult, failAll);
                matches |= collectionResult.isSuccessful();
			}
			if (matches == false){
				matchResult.addNonMatching(fieldMatcher, setItem1, "No match");
			    return;
			}
		}
		return;
	}

	/**
	 * @param value1
	 * @param matchStrategy
	 * @param fieldMatcher
	 * @return
	 * @throws MatchException
	 */
	private void matchList(Collection value1, Collection value2,
	        IMatchStrategy matchStrategy, MatchResult matchResult,
	        FieldMatcher fieldMatcher, boolean failAll)
	        throws MatchException {

		List<IMatchable> list1 = (List<IMatchable>)value1;
		List<IMatchable> list2 = (List<IMatchable>)value2;
		if(list1 == null && list2 == null) {
			return;
		}

		if ((list1 != null && list2 == null) || (list1 == null && list2 != null) || (list1.size()!= list2.size())){
		    matchResult.addNonMatching(fieldMatcher, listSize(list1), listSize(list2));
		    return;
		}

		matchResult.addPath(fieldMatcher.getPropertyName());
		for (int i = 0; i < list1.size(); i++){
			IMatchable listObject1 = list1.get(i);
			IMatchable listObject2 = list2.get(i);
			matchStrategy.invoke(listObject1, listObject2, matchResult, failAll);
			if (matchResult.isFailed()&& !failAll){
			    break;
			}
		}
		matchResult.removePath();
		return;
	}

	/**
     * @param list1
     * @return
     */
    private Object listSize(List<IMatchable> list) {
        return list == null? 0 :list.size();
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
	    preInitMapping();
		boolean includeStatic = false;
		boolean includeTransient = false;
		boolean makeAccessible = true;
		matchFields = CdmUtils.getAllFields(matchClass, CdmBase.class, includeStatic,
		        includeTransient, makeAccessible, true);
		for (Field field: matchFields.values()){
			initField(field, false);
		}
	}

	/**
     *
     */
    protected void preInitMapping() {}

    /**
	 * Initializes the matching for a single field
	 * @param field
	 */
	private void initField(Field field, boolean temporary) {
		MatchMode matchMode = null;
		IMatchStrategyEqual matchStrategy = null;
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
					matching.addFieldMatcher(FieldMatcher.NewInstance(field, matchMode), temporary);
				}
			}
		}
		if (matchMode == null){
		    Class fieldType = field.getType();
			if (isCollection(fieldType)){
				matchMode = getDefaultCollectionMatchMode();
				matching.addFieldMatcher(FieldMatcher.NewInstance(field, matchMode), temporary);
			}else if (fieldType.isInterface()){
				//TODO could be handled more sophisticated
				matchMode = getDefaultMatchMatchMode();
				matching.addFieldMatcher(FieldMatcher.NewInstance(field, matchMode, matchStrategy), temporary);
			}else if (isSingleCdmBaseObject(fieldType)){
				if (IMatchable.class.isAssignableFrom(fieldType)){
					matchMode = getDefaultMatchMatchMode();
					if (matchStrategy == null){
						if (fieldType == this.matchClass){
							matchStrategy = this;
						}else{
							matchStrategy = DefaultMatchStrategy.NewInstance(fieldType);
						}
					}
					matching.addFieldMatcher(FieldMatcher.NewInstance(field, matchMode, matchStrategy), temporary);
				}else{
					matchMode = getDefaultMatchMode();
					matching.addFieldMatcher(FieldMatcher.NewInstance(field, matchMode), temporary);
				}
			}else{
				matchMode = getDefaultMatchMode();
				matching.addFieldMatcher(FieldMatcher.NewInstance(field, matchMode), temporary);
			}
		}
	}

//
//	@Override
//	public Set<String> getMatchFieldPropertyNames() {
//		return matchFields.keySet();
//	}
//
//	@Override
//    public void setDefaultMatchMode(MatchMode defaultMatchMode){
//	    this.defaultMatchMatchMode = defaultMatchMode;
//	}
//
//	@Override
//    public void setDefaultCollectionMatchMode(MatchMode defaultCollectionMatchMode){
//        this.defaultCollectionMatchMode = defaultCollectionMatchMode;
//    }
//
//	@Override
//    public void setDefaultMatchMatchMode(MatchMode defaultMatchMatchMode){
//        this.defaultMatchMatchMode = defaultMatchMatchMode;
//    }

    protected MatchMode getDefaultMatchMode(){
        return this.defaultMatchMode;
    }

    protected MatchMode getDefaultCollectionMatchMode(){
        return this.defaultCollectionMatchMode;
    }

    protected MatchMode getDefaultMatchMatchMode(){
        return this.defaultMatchMatchMode;
    }

}
