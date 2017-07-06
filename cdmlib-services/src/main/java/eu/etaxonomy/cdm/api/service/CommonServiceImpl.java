/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyConfigurator;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyConfigurator.MatchStrategy;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;


@Service
@Transactional(readOnly = true)
public class CommonServiceImpl /*extends ServiceBase<OriginalSourceBase,IOriginalSourceDao>*/ implements ICommonService {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CommonServiceImpl.class);


    @Autowired
    private IOriginalSourceDao originalSourceDao;


    @Autowired
    private ICdmGenericDao genericDao;


    @Override
    public CdmBase findWithUpdate(Class<? extends CdmBase> clazz, int id){
        return genericDao.find(clazz, id);
    }

    @Override
    public CdmBase find(Class<? extends CdmBase> clazz, int id){
        return genericDao.find(clazz, id);
    }

    @Override
    public CdmBase find(Class<? extends CdmBase> clazz, int id, List<String> propertyPaths){
        return  genericDao.find(clazz, id, propertyPaths);
    }

    @Override
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid) {
        return uuid == null ? null : genericDao.find(clazz, uuid);
    }

    @Override
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid, List<String> propertyPaths) {
        return uuid == null ? null : genericDao.find(clazz, uuid, propertyPaths);
    }

    @Override
    public Map<String, ? extends ISourceable> getSourcedObjectsByIdInSource(Class clazz, Set<String> idInSourceSet, String idNamespace) {
        Map<String, ? extends ISourceable> list = originalSourceDao.findOriginalSourcesByIdInSource(clazz, idInSourceSet, idNamespace);
        return list;
    }

    @Override
    public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace) {
        ISourceable<?> result = null;
        List<IdentifiableEntity> list = originalSourceDao.findOriginalSourceByIdInSource(clazz, idInSource, idNamespace);
        if (! list.isEmpty()){
            result = list.get(0);
        }return result;
    }


    @Override
    public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase){
        return this.genericDao.getReferencingObjects(referencedCdmBase);
    }

    @Override
    public Integer getReferencingObjectsCount(CdmBase referencedCdmBase){
        return this.genericDao.getReferencingObjectsCount(referencedCdmBase);
    }

    @Override
    public Set<CdmBase> getReferencingObjectsForDeletion(CdmBase referencedCdmBase){
        return this.genericDao.getReferencingObjectsForDeletion(referencedCdmBase);
    }
    //		try {
    //			Set<Class<? extends CdmBase>> allCdmClasses = genericDao.getAllCdmClasses(false); //findAllCdmClasses();
    //
    //			referencedCdmBase = (CdmBase)HibernateProxyHelper.deproxy(referencedCdmBase);
    //			Class referencedClass = referencedCdmBase.getClass();
    //			Set<CdmBase> result = new HashSet<>();
    //			logger.debug("Referenced Class: " + referencedClass.getName());
    //
    //			for (Class<? extends CdmBase> cdmClass : allCdmClasses){
    //				Set<Field> fields = getFields(cdmClass);
    //				for (Field field: fields){
    //					Class<?> type = field.getType();
    //					//class
    //					if (! type.isInterface()){
    //						if (referencedClass.isAssignableFrom(type)||
    //								type.isAssignableFrom(referencedClass) && CdmBase.class.isAssignableFrom(type)){
    //							handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, false);
    //						}
    //					//interface
    //					}else if (type.isAssignableFrom(referencedClass)){
    //							handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, false);
    //					}else if (Collection.class.isAssignableFrom(type)){
    //
    //						if (checkIsSetOfType(field, referencedClass, type) == true){
    //							handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, true);
    //						}
    //					}
    ////				Class[] interfaces = referencedClass.getInterfaces();
    ////				for (Class interfaze: interfaces){
    ////					if (interfaze == type){
    //////					if(interfaze.isAssignableFrom(returnType)){
    ////						handleSingleClass(interfaze, type, field, cdmClass, result, referencedCdmBase);
    ////					}
    ////				}
    //				}
    //			}
    //			return result;
    //		} catch (Exception e) {
    //			e.printStackTrace();
    //			throw new RuntimeException(e);
    //		}
    //
    //	}
    //
    //	private boolean checkIsSetOfType(Field field, Class referencedClass, Class<?> type){
    //		Type genericType = (ParameterizedTypeImpl)field.getGenericType();
    //		if (genericType instanceof ParameterizedTypeImpl){
    //			ParameterizedTypeImpl paraType = (ParameterizedTypeImpl)genericType;
    //			paraType.getRawType();
    //			Type[] arguments = paraType.getActualTypeArguments();
    //			//logger.debug(arguments.length);
    //			if (arguments.length == 1){
    //				Class collectionClass;
    //				try {
    //					if (arguments[0] instanceof Class){
    //						collectionClass = (Class)arguments[0];
    //					}else if(arguments[0] instanceof TypeVariableImpl){
    //						TypeVariableImpl typeVariable = (TypeVariableImpl)arguments[0];
    //						GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
    //						collectionClass = (Class)genericDeclaration;
    //					}else{
    //						logger.warn("Unknown Type");
    //						return false;
    //					}
    //					if (CdmBase.class.isAssignableFrom(collectionClass) && collectionClass.isAssignableFrom(referencedClass)  ){
    //						return true;
    //					}
    //				} catch (Exception e) {
    //					logger.warn(e.getMessage());
    //				}
    //			}else{
    //				logger.warn("Length of arguments <> 1");
    //			}
    //		}else{
    //			logger.warn("Not a generic type of type ParameterizedTypeImpl");
    //		}
    //		return false;
    //	}
    //
    //
    //
    //
    //	private boolean handleSingleClass(Class itemClass, Class type, Field field, Class cdmClass, Set<CdmBase> result,CdmBase value, boolean isCollection){
    //		if (! Modifier.isStatic(field.getModifiers())){
    //			String methodName = StringUtils.rightPad(field.getName(), 30);
    //			String className = StringUtils.rightPad(cdmClass.getSimpleName(), 30);
    //			String returnTypeName = StringUtils.rightPad(type.getSimpleName(), 30);
    //
    //			logger.debug(methodName +   "\t\t" + className + "\t\t" + returnTypeName);
    ////			result_old.add(method);
    //			result.addAll(getCdmBasesByFieldAndClass(field, itemClass, cdmClass, value, isCollection));
    //		}
    //		return true;
    //	}
    //
    //	private Set<Field> getFields(Class clazz){
    //		Set<Field> result = new HashSet<>();
    //		for (Field field: clazz.getDeclaredFields()){
    //			if (!Modifier.isStatic(field.getModifiers())){
    //				result.add(field);
    //			}
    //		}
    //		Class superclass = clazz.getSuperclass();
    //		if (CdmBase.class.isAssignableFrom(superclass)){
    //			result.addAll(getFields(superclass));
    //		}
    //		return result;
    //	}
    //
    //	private Set<CdmBase> getCdmBasesByFieldAndClass(Field field, Class itemClass, Class otherClazz, CdmBase item, boolean isCollection){
    //		Set<CdmBase> result = new HashSet<>();
    //		if (isCollection){
    //			result.addAll(genericDao.getCdmBasesWithItemInCollection(itemClass, otherClazz, field.getName(), item));
    //		}else{
    //			result.addAll(genericDao.getCdmBasesByFieldAndClass(otherClazz, field.getName(), item));
    //		}
    //		return result;
    //	}

    @Override
    public List getHqlResult(String hqlQuery){
        return genericDao.getHqlResult(hqlQuery);
    }

    @Override
    public <T extends IMergable> void merge(T mergeFirst, T mergeSecond, IMergeStrategy mergeStrategy) throws MergeException {
        if (mergeStrategy == null){
            mergeStrategy = DefaultMergeStrategy.NewInstance(((CdmBase)mergeFirst).getClass());
        }
        genericDao.merge((CdmBase)mergeFirst, (CdmBase)mergeSecond, mergeStrategy);
    }

    @Override
    public <T extends IMergable> void merge(T mergeFirst, T mergeSecond, Class<? extends CdmBase> clazz) throws MergeException {
        IMergeStrategy mergeStrategy;
        if (clazz == null){
            mergeStrategy = DefaultMergeStrategy.NewInstance(((CdmBase)mergeFirst).getClass());
        } else {
            mergeStrategy = DefaultMergeStrategy.NewInstance(clazz);
        }
        merge(mergeFirst, mergeSecond, mergeStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    @Deprecated
    public <T extends IMergable> void merge(int mergeFirstId, int mergeSecondId, Class<? extends CdmBase> clazz) throws MergeException {
        IMergeStrategy mergeStrategy;
        T mergeFirst = (T) genericDao.find(clazz, mergeFirstId);
        T mergeSecond = (T) genericDao.find(clazz, mergeSecondId);
        mergeStrategy = DefaultMergeStrategy.NewInstance(clazz);
        merge(mergeFirst, mergeSecond, mergeStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends IMergable> void merge(UUID mergeFirstUuid, UUID mergeSecondUuid, Class<? extends CdmBase> clazz) throws MergeException {
        IMergeStrategy mergeStrategy;
        T mergeFirst = (T) genericDao.find(clazz, mergeFirstUuid);
        T mergeSecond = (T) genericDao.find(clazz, mergeSecondUuid);
        if (mergeFirst == null){
            throw new MergeException("The merge target is not available anymore.");
        }
        if (mergeSecond == null){
            throw new MergeException("The merge candidate is not available anymore.");
        }
        mergeStrategy = DefaultMergeStrategy.NewInstance(clazz);
        merge(mergeFirst, mergeSecond, mergeStrategy);
    }

    @Override
    public <T extends IMergable> void merge(T mergeFirst, T mergeSecond) throws MergeException {
        IMergeStrategy mergeStrategy = DefaultMergeStrategy.NewInstance(((CdmBase)mergeFirst).getClass());
        merge(mergeFirst, mergeSecond, mergeStrategy);
    }


    @Override
    public <T extends IMatchable> List<T> findMatching(T objectToMatch, IMatchStrategy matchStrategy) throws MatchException {
        if (matchStrategy == null){
            matchStrategy = DefaultMatchStrategy.NewInstance(((objectToMatch).getClass()));
        }
        return genericDao.findMatching(objectToMatch, matchStrategy);
    }



    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ICommonService#findMatching(eu.etaxonomy.cdm.strategy.match.IMatchable, eu.etaxonomy.cdm.strategy.match.MatchStrategyConfigurator.MatchStrategy)
     */
    @Override
    public <T extends IMatchable> List<T> findMatching(T objectToMatch, MatchStrategy strategy) throws MatchException {
        return findMatching(objectToMatch, MatchStrategyConfigurator.getMatchStrategy(strategy));
    }

    //	/* (non-Javadoc)
    //	 * @see eu.etaxonomy.cdm.api.service.IService#list(java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
    //	 */
    //	@Override
    //	public <TYPE extends OriginalSourceBase> Pager<TYPE> list(Class<TYPE> type,
    //			Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
    //			List<String> propertyPaths) {
    //		logger.warn("Not yet implemented");
    //		return null;
    //	}


    @Transactional(readOnly = false)
    @Override
    public void saveAllMetaData(Collection<CdmMetaData> metaData) {
        Iterator<CdmMetaData> iterator = metaData.iterator();
        while(iterator.hasNext()){
            CdmMetaData cdmMetaData = iterator.next();
            genericDao.saveMetaData(cdmMetaData);
        }
    }

    @Override
    public Map<CdmMetaDataPropertyName, CdmMetaData> getCdmMetaData() {
        Map<CdmMetaDataPropertyName, CdmMetaData> result = new HashMap<>();
        List<CdmMetaData> metaDataList = genericDao.getMetaData();
        for (CdmMetaData metaData : metaDataList){
            CdmMetaDataPropertyName propertyName = metaData.getPropertyName();
            result.put(propertyName, metaData);
        }
        return result;
    }

    @Override
    public Object initializeCollection(UUID ownerUuid, String fieldName) {
        return genericDao.initializeCollection(ownerUuid, fieldName);

    }

    @Override
    public Object initializeCollection(UUID ownerUuid, String fieldName, List<String> propertyPaths) {
        return genericDao.initializeCollection(ownerUuid, fieldName, propertyPaths);

    }

    @Override
    public boolean isEmpty(UUID ownerUuid, String fieldName) {
        return genericDao.isEmpty(ownerUuid, fieldName);

    }

    @Override
    public int size(UUID ownerUuid, String fieldName) {
        return genericDao.size(ownerUuid, fieldName);
    }


    @Override
    public Object get(UUID ownerUuid, String fieldName, int index) {
        return genericDao.get(ownerUuid, fieldName, index);
    }

    @Override
    public boolean contains(UUID ownerUuid, String fieldName, Object element) {
        return genericDao.contains(ownerUuid, fieldName, element);
    }

    @Override
    public boolean containsKey(UUID ownerUuid, String fieldName, Object key) {
        return genericDao.containsKey(ownerUuid, fieldName, key);
    }

    @Override
    public boolean containsValue(UUID ownerUuid, String fieldName, Object value) {
        return genericDao.containsValue(ownerUuid, fieldName, value);
    }

    @Override
    @Transactional(readOnly = false)
    public void createFullSampleData() {
        genericDao.createFullSampleData();
    }



    @Override
    public <S extends CdmBase> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths){
        return genericDao.list(type,limit, start, orderHints,propertyPaths);
    }

    @Override
    public <S extends CdmBase> int count(Class<S> type) {
        return genericDao.count(type);
    }

    @Override
    @Transactional(readOnly = false)
    public CdmBase save(CdmBase newInstance) {
        return genericDao.save(newInstance);
    }

    @Override
    @Transactional(readOnly = false)
    public UUID delete(CdmBase instance) {
        return genericDao.delete(instance);
    }

    @Override
    @Transactional(readOnly = false)
    public UUID saveOrUpdate(CdmBase newInstance) {
        return genericDao.saveOrUpdate(newInstance);
    }


    @Override
    @Transactional(readOnly = false)
    public <T extends CdmBase> Map<UUID,T> save(Collection<T> newInstances) {
        //this is very ugly, I know, but for now I do not want to copy the saveAll method from CdmEntityDaoBase to genericDao
        //and generally the saveAll method should work for other CdmBase types with generics removed
        return (Map)originalSourceDao.saveAll((Collection)newInstances);
    }

    @Override
    @Transactional(readOnly = false)
    public <T extends CdmBase> Map<UUID,T> saveOrUpdate(Collection<T> newInstances) {
        //this is very ugly, I know, but for now I do not want to copy the saveAll method from CdmEntityDaoBase to genericDao
        //and generally the saveAll method should work for other CdmBase types with generics removed
        return (Map)originalSourceDao.saveOrUpdateAll((Collection)newInstances);
    }


    @Override
    public <T extends CdmBase> boolean isMergeable(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException {
        return genericDao.isMergeable(cdmBase1, cdmBase2, mergeStrategy);
    }

}
