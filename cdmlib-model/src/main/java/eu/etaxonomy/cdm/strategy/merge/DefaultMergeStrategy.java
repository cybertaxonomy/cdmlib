// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.merge;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author a.mueller
 * @created 31.07.2009
 */
public class DefaultMergeStrategy extends StrategyBase implements IMergeStrategy  {
	private static final long serialVersionUID = -8513956338156791995L;
	private static final Logger logger = Logger.getLogger(DefaultMergeStrategy.class);
	final static UUID uuid = UUID.fromString("d85cd6c3-0147-452c-8fed-bbfb82f392f6");

	public static DefaultMergeStrategy NewInstance(Class<? extends CdmBase> mergeClazz){
		return new DefaultMergeStrategy(mergeClazz);
	}

	private boolean onlyReallocateReferences = false;

	protected MergeMode defaultMergeMode = MergeMode.FIRST;
	protected MergeMode defaultCollectionMergeMode = MergeMode.ADD;

	protected Map<String, MergeMode> mergeModeMap = new HashMap<String, MergeMode>();
	protected Class<? extends CdmBase> mergeClass;
	protected Map<String, Field> mergeFields;

	protected DefaultMergeStrategy(Class<? extends CdmBase> mergeClazz) {
		super();
		if (mergeClazz == null){
			throw new IllegalArgumentException("Merge class must not be null");
		}
		this.mergeClass = mergeClazz;
		boolean includeStatic = false;
		boolean includeTransient = false;
		boolean makeAccessible = true;
		this.mergeFields = CdmUtils.getAllFields(mergeClass, CdmBase.class, includeStatic, includeTransient, makeAccessible, true);
		initMergeModeMap();
	}


	@Override
	public boolean isOnlyReallocateReferences() {
		return this.onlyReallocateReferences;
	}

//	@Override
//	public void setOnlyReallocateLinks(boolean onlyReallocateReferences) {
//		this.onlyReallocateReferences = onlyReallocateReferences;
//	}


	/**
	 *
	 */
	private void initMergeModeMap() {
		for (Field field: mergeFields.values()){
			for (Annotation annotation : field.getAnnotations()){
				if (annotation.annotationType() == Merge.class){
					MergeMode mergeMode = ((Merge)annotation).value();
					mergeModeMap.put(field.getName(), mergeMode);
				}
			}
		}
	}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	/**
	 * @return the merge class
	 */
	public Class<? extends CdmBase> getMergeClass() {
		return mergeClass;
	}



	/**
	 * @param mergeClazz the mergeClazz to set
	 */
	public void setMergeClazz(Class<? extends CdmBase> mergeClazz) {
		this.mergeClass = mergeClazz;
	}


	@Override
	public MergeMode getMergeMode(String propertyName){
		MergeMode result = mergeModeMap.get(propertyName);
		if (result == null){
			Field field = mergeFields.get(propertyName);
			if (isCollection(field.getType())){
				return defaultCollectionMergeMode;
			}else{
				return defaultMergeMode;
			}
		}else{
			return result;
		}
	}

	@Override
	public void setMergeMode(String propertyName, MergeMode mergeMode) throws MergeException{
		if (mergeFields.containsKey(propertyName)){
			checkIdentifier(propertyName, mergeMode);
			mergeModeMap.put(propertyName, mergeMode);
		}else{
			throw new MergeException("The class " + mergeClass.getName() + " does not contain a field with name " + propertyName);
		}
	}


	@Override
	public void setDefaultMergeMode(MergeMode defaultMergeMode) {
		this.defaultMergeMode = defaultMergeMode;
	}


	@Override
	public void setDefaultCollectionMergeMode(MergeMode defaultCollectionMergeMode) {
		this.defaultCollectionMergeMode = defaultCollectionMergeMode;
	}


	/**
	 * Tests if a property is an identifier property
	 * @param propertyName
	 * @param mergeMode
	 * @throws MergeException
	 */
	private void checkIdentifier(String propertyName, MergeMode mergeMode) throws MergeException {
		if (mergeMode != MergeMode.FIRST){
			if ("id".equalsIgnoreCase(propertyName) || "uuid".equalsIgnoreCase(propertyName)){
				throw new MergeException("Identifier must always have merge mode MergeMode.FIRST");
			}
		}
	}

	@Override
	public <T extends IMergable> Set<ICdmBase> invoke(T mergeFirst, T mergeSecond) throws MergeException {
		return this.invoke(mergeFirst, mergeSecond, null);
	}

	@Override
	public <T extends IMergable> Set<ICdmBase> invoke(T mergeFirst, T mergeSecond, Set<ICdmBase> clonedObjects) throws MergeException {
		Set<ICdmBase> deleteSet = new HashSet<ICdmBase>();
		if (clonedObjects == null){
			clonedObjects = new HashSet<ICdmBase>();
		}
		deleteSet.add(mergeSecond);
		try {
 			for (Field field : mergeFields.values()){
				Class<?> fieldType = field.getType();
				if (isIdentifier(field)){
					//do nothing (id and uuid stay with first object)
				}else if (isPrimitive(fieldType)){
					mergePrimitiveField(mergeFirst, mergeSecond, field);
				}else if (fieldType == String.class ){
					mergeStringField(mergeFirst, mergeSecond, field);
				}else if (isCollection(fieldType)){
					mergeCollectionField(mergeFirst, mergeSecond, field, deleteSet, clonedObjects);
				}else if(isUserType(fieldType)){
					mergeUserTypeField(mergeFirst, mergeSecond, field);
				}else if(isSingleCdmBaseObject(fieldType)){
					mergeSingleCdmBaseField(mergeFirst, mergeSecond, field, deleteSet);
				}else if(fieldType.isInterface()){
					mergeInterfaceField(mergeFirst, mergeSecond, field, deleteSet);
				}else if(fieldType.isEnum()){
					mergeEnumField(mergeFirst, mergeSecond, field, deleteSet);
				}else{
					throw new RuntimeException("Unknown Object type for merging: " + fieldType);
				}
			}
 			return deleteSet;
		} catch (Exception e) {
			throw new MergeException("Merge Exception in invoke", e);
		}
	}


	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMergable> void mergeInterfaceField(T mergeFirst, T mergeSecond, Field field, Set<ICdmBase> deleteSet) throws Exception {
		String propertyName = field.getName();
		MergeMode mergeMode =  this.getMergeMode(propertyName);
		if (mergeMode != MergeMode.FIRST){
			mergeCdmBaseValue(mergeFirst, mergeSecond, field, deleteSet);
		}
		logger.debug(propertyName + ": " + mergeMode + ", " + field.getType().getName());

	}

	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMergable> void mergeEnumField(T mergeFirst, T mergeSecond, Field field, Set<ICdmBase> deleteSet) throws Exception {
		String propertyName = field.getName();
		MergeMode mergeMode =  this.getMergeMode(propertyName);
		if (mergeMode != MergeMode.FIRST){
			mergeCdmBaseValue(mergeFirst, mergeSecond, field, deleteSet);
		}
		logger.debug(propertyName + ": " + mergeMode + ", " + field.getType().getName());

	}

	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMergable> void mergeSingleCdmBaseField(T mergeFirst, T mergeSecond, Field field, Set<ICdmBase> deleteSet) throws Exception {
		String propertyName = field.getName();
		MergeMode mergeMode =  this.getMergeMode(propertyName);
		if (mergeMode != MergeMode.FIRST){
			mergeCdmBaseValue(mergeFirst, mergeSecond, field, deleteSet);
		}
		logger.debug(propertyName + ": " + mergeMode + ", " + field.getType().getName());

	}

	private <T extends IMergable> void mergeCdmBaseValue(T mergeFirst, T mergeSecond, Field field, Set<ICdmBase> deleteSet) throws Exception {
		if (true){
			Object value = getMergeValue(mergeFirst, mergeSecond, field);
			if (value instanceof ICdmBase || value == null){
				field.set(mergeFirst, value);
			}else{
				throw new MergeException("Merged value must be of type CdmBase but is not: " + value.getClass());
			}
		}else{
			throw new MergeException("Not supported mode");
		}
	}

	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMergable> void mergeUserTypeField(T mergeFirst, T mergeSecond, Field field) throws Exception {
		String propertyName = field.getName();
		Class<?> fieldType = field.getType();
		MergeMode mergeMode =  this.getMergeMode(propertyName);
		if (mergeMode == MergeMode.MERGE){
			Method mergeMethod = getMergeMethod(fieldType);
			Object firstObject = field.get(mergeFirst);
			if (firstObject == null){
				firstObject = fieldType.newInstance();
			}
			Object secondObject = field.get(mergeSecond);
			mergeMethod.invoke(firstObject, secondObject);
		}else if (mergeMode != MergeMode.FIRST){
			Object value = getMergeValue(mergeFirst, mergeSecond, field);
			field.set(mergeFirst, value);
		}
		logger.debug(propertyName + ": " + mergeMode + ", " + fieldType.getName());
	}

	/**
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private Method getMergeMethod(Class<?> fieldType) throws SecurityException, NoSuchMethodException {
		Method mergeMethod = fieldType.getDeclaredMethod("merge", fieldType);
		return mergeMethod;
	}



	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMergable> void mergeCollectionField(T mergeFirst, T mergeSecond, Field field, Set<ICdmBase> deleteSet, Set<ICdmBase> clonedObjects) throws Exception {
		String propertyName = field.getName();
		Class<?> fieldType = field.getType();
		MergeMode mergeMode =  this.getMergeMode(propertyName);
		if (mergeMode != MergeMode.FIRST){
			mergeCollectionFieldNoFirst(mergeFirst, mergeSecond, field, mergeMode, deleteSet, clonedObjects);
		}
		logger.debug(propertyName + ": " + mergeMode + ", " + fieldType.getName());
	}

	protected <T extends IMergable> void mergeCollectionFieldNoFirst(T mergeFirst, T mergeSecond, Field field, MergeMode mergeMode, Set<ICdmBase> deleteSet, Set<ICdmBase> clonedObjects) throws Exception{
		Class<?> fieldType = field.getType();
		if (mergeMode == MergeMode.ADD || mergeMode == MergeMode.ADD_CLONE){
			//FIXME
			Method addMethod = getAddMethod(field);
			Method removeMethod = getAddMethod(field, true);

			if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)){
				Collection<ICdmBase> secondCollection = (Collection<ICdmBase>)field.get(mergeSecond);
				List<ICdmBase> removeList = new ArrayList<ICdmBase>();
				if(secondCollection != null) {
				for (ICdmBase obj : secondCollection){
					Object objectToAdd;
					if (mergeMode == MergeMode.ADD){
						objectToAdd = obj;
					}else if(mergeMode == MergeMode.ADD_CLONE){
						Method cloneMethod = obj.getClass().getDeclaredMethod("clone");
						objectToAdd = cloneMethod.invoke(obj);
						clonedObjects.add(obj);
					}else{
						throw new MergeException("Unknown collection merge mode: " + mergeMode);
					}
					addMethod.invoke(mergeFirst, objectToAdd);
					removeList.add(obj);
				}
				}
				for (ICdmBase removeObj : removeList ){
					//removeMethod.invoke(mergeSecond, removeObj);
					if ((removeObj instanceof CdmBase)&& mergeMode == MergeMode.ADD_CLONE) {
						deleteSet.add(removeObj);
					}
				}
			}else{
				throw new MergeException("Merge for collections other than sets and lists not yet implemented");
			}
		}else if (mergeMode == MergeMode.RELATION){
			if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)){
				Collection<RelationshipBase<?,?,?>> secondCollection = (Collection<RelationshipBase<?,?,?>>)field.get(mergeSecond);
				List<ICdmBase> removeList = new ArrayList<ICdmBase>();
				for (RelationshipBase<?,?,?> relation : secondCollection){
					Method relatedFromMethod = RelationshipBase.class.getDeclaredMethod("getRelatedFrom");
					relatedFromMethod.setAccessible(true);
					Object relatedFrom = relatedFromMethod.invoke(relation);

					Method relatedToMethod = RelationshipBase.class.getDeclaredMethod("getRelatedTo");
					relatedToMethod.setAccessible(true);
					Object relatedTo = relatedToMethod.invoke(relation);

					if (relatedFrom.equals(mergeSecond)){
						Method setRelatedMethod = RelationshipBase.class.getDeclaredMethod("setRelatedFrom", IRelated.class);
						setRelatedMethod.setAccessible(true);
						setRelatedMethod.invoke(relation, mergeFirst);
					}
					if (relatedTo.equals(mergeSecond)){
						Method setRelatedMethod = RelationshipBase.class.getDeclaredMethod("setRelatedTo", IRelated.class);
						setRelatedMethod.setAccessible(true);
						setRelatedMethod.invoke(relation, mergeFirst);
					}
					((IRelated)mergeFirst).addRelationship(relation);
					removeList.add(relation);
				}
				for (ICdmBase removeObj : removeList){
					//removeMethod.invoke(mergeSecond, removeObj);
					if (removeObj instanceof CdmBase){
						deleteSet.add(removeObj);
					}
				}
			}else{
				throw new MergeException("Merge for collections other than sets and lists not yet implemented");
			}
		}else{
			throw new MergeException("Other merge modes for collections not yet implemented");
		}
	}


	private Method getAddMethod(Field field) throws MergeException{
		return getAddMethod(field, false);
	}

	public static Method getAddMethod(Field field, boolean remove) throws MergeException{
		Method result;
		Class<?> parameterClass = getCollectionType(field);
		String fieldName = field.getName();
		String firstCapital = fieldName.substring(0, 1).toUpperCase();
		String rest = fieldName.substring(1);
		String prefix = remove? "remove": "add";
		String methodName = prefix + firstCapital + rest;
		boolean endsWithS = parameterClass.getSimpleName().endsWith("s");
		if (! endsWithS && ! fieldName.equals("media")){
			methodName = methodName.substring(0, methodName.length() -1); //remove 's' at end
		}
		Class<?> methodClass = field.getDeclaringClass();
		try {
			result = methodClass.getMethod(methodName, parameterClass);
		}catch (NoSuchMethodException e1) {
			try {
				result = methodClass.getDeclaredMethod(methodName, parameterClass);
				result.setAccessible(true);
			} catch (NoSuchMethodException e) {
				logger.warn(methodName);
				throw new IllegalArgumentException("Default adding method for collection field ("+field.getName()+") does not exist");
			}
		} catch (SecurityException e) {
			throw e;
		}
		return result;
	}

	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMergable> void mergePrimitiveField(T mergeFirst, T mergeSecond, Field field) throws Exception {
		String propertyName = field.getName();
		Class<?> fieldType = field.getType();
		MergeMode mergeMode =  this.getMergeMode(propertyName);
		if (mergeMode != MergeMode.FIRST){
			Object value = getMergeValue(mergeFirst, mergeSecond, field);
			field.set(mergeFirst, value);
		}
		logger.debug(propertyName + ": " + mergeMode + ", " + fieldType.getName());

	}

	/**
	 * @throws Exception
	 *
	 */
	private <T extends IMergable> void mergeStringField(T mergeFirst, T mergeSecond, Field field) throws Exception {
		String propertyName = field.getName();
		Class<?> fieldType = field.getType();
		MergeMode mergeMode =  this.getMergeMode(propertyName);
		if (mergeMode != MergeMode.FIRST){
			Object value = getMergeValue(mergeFirst, mergeSecond, field);
			field.set(mergeFirst, value);
		}
		logger.debug(propertyName + ": " + mergeMode + ", " + fieldType.getName());

	}

	/**
	 * @param fieldType
	 * @return
	 */
	private boolean isIdentifier(Field field) {
		Class<?> fieldType = field.getType();
		if ("id".equals(field.getName()) && fieldType == int.class ){
			return true;
		}else if ("uuid".equals(field.getName()) && fieldType == UUID.class ){
			return true;
		}else{
			return false;
		}
	}


	/**
	 * @param cdmBase
	 * @param toMerge
	 * @param field
	 * @param mergeMode
	 * @throws Exception
	 */
	protected <T extends IMergable> Object getMergeValue(T mergeFirst, T mergeSecond,
			Field field) throws Exception {
		MergeMode mergeMode =  this.getMergeMode(field.getName());
		try {
			if (mergeMode == MergeMode.FIRST){
				return field.get(mergeFirst);
			}else if (mergeMode == MergeMode.SECOND){
				return field.get(mergeSecond);
			}else if (mergeMode == MergeMode.NULL){
				return null;
			}else if (mergeMode == MergeMode.CONCAT){
				return ((String)field.get(mergeFirst) + (String)field.get(mergeSecond));
			}else if (mergeMode == MergeMode.AND){
				return ((Boolean)field.get(mergeFirst) && (Boolean)field.get(mergeSecond));
			}else if (mergeMode == MergeMode.OR){
				return ((Boolean)field.get(mergeFirst) || (Boolean)field.get(mergeSecond));
			}else{
				throw new IllegalStateException("Unknown MergeMode");
			}
		} catch (IllegalArgumentException e) {
			throw new Exception(e);
		}
	}


	private static Class getCollectionType(Field field) throws MergeException{
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType/*Impl*/){
			ParameterizedType paraType = (ParameterizedType)genericType;
			Type rawType = paraType.getRawType();
			Type[] arguments = paraType.getActualTypeArguments();

			if (arguments.length == 1){
				Class collectionClass;
				if (arguments[0] instanceof Class){
					collectionClass = (Class)arguments[0];
				}else if(arguments[0] instanceof TypeVariable/*Impl*/){
					TypeVariable typeVariable = (TypeVariable)arguments[0];
					GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
					collectionClass = (Class)genericDeclaration;
				}else{
					throw new MergeException("Collection with other types than TypeVariableImpl are not yet supported");
				}
				return collectionClass;
			}else{
				throw new MergeException("Collection with multiple types not supported");
			}
		}else{
			throw new MergeException("Collection has no generic type of type ParameterizedTypeImpl. Unsupport case.");
		}
	}

}
