/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockOptions;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.exception.UnpublishedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class ServiceBase<T extends CdmBase, DAO extends ICdmEntityDao<T>>
            implements IService<T>, ApplicationContextAware {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    protected ApplicationContext appContext;

    public final static boolean NO_UNPUBLISHED = DaoBase.NO_UNPUBLISHED;  //constant for unpublished
    public final static boolean INCLUDE_UNPUBLISHED = DaoBase.INCLUDE_UNPUBLISHED;  //constant for unpublished

    protected DAO dao;

    @Autowired
    protected ICdmGenericDao genericDao;

    @Autowired
    protected ICommonService commonService;

    @Override
    @Transactional(readOnly = true)
    public void lock(T t, LockOptions lockOptions) {
        dao.lock(t, lockOptions);
    }

    @Override
    @Transactional(readOnly = true)
    public void refresh(T t, LockOptions lockOptions, List<String> propertyPaths) {
        dao.refresh(t, lockOptions, propertyPaths);
    }

    @Override
    @Transactional(readOnly = false)
    public void clear() {
        dao.clear();
    }

    @Override
    @Transactional(readOnly = true)
    public int count(Class<? extends T> clazz) {
        return Long.valueOf(dao.count(clazz)).intValue();
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(UUID persistentObjectUUID) {
        T persistentObject = dao.findByUuid(persistentObjectUUID);
        return delete(persistentObject);
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(Collection<UUID> persistentObjectUUIDs) {
        DeleteResult result = new DeleteResult();
        for(UUID persistentObjectUUID : persistentObjectUUIDs) {
            T persistentObject = dao.findByUuid(persistentObjectUUID);
            DeleteResult dr = delete(persistentObject);
            result.includeResult(dr);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(T persistentObject) {
    	DeleteResult result = new DeleteResult();
    	try{
    		dao.delete(persistentObject);
    		result.addDeletedObject(persistentObject);
    	} catch(DataAccessException e){
    		result.setError();
    		result.addException(e);
    	}
        return result;
    }

    /**
     * The basic isDeletable method return false if the object is referenced from any other object.
     * To be used only for the main type of this service.
     */
    @Override
    @Transactional(readOnly = true)
    public DeleteResult isDeletable(UUID baseUUID, DeleteConfiguratorBase config){
        return this.isDeletable(baseUUID, dao.getType(), config);
    }

    /**
     * The basic isDeletable method return false if the object is referenced from
     * any other object. This is a generic method that can be used for any
     * CDM class, not only the main CDM class of the given service.
     */
    protected <S extends CdmBase> DeleteResult isDeletable(UUID baseUUID, Class<S> clazz, DeleteConfiguratorBase config){

        DeleteResult result = new DeleteResult();
        S base = genericDao.find(clazz, baseUUID);
        if (base == null){
            result.setAbort();
            result.addException(new ObjectDeletedException("The object was already deleted.", baseUUID, null));
        }
        Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(base);
        if (references != null){
            result.addRelatedObjects(references);
            Iterator<CdmBase> iterator = references.iterator();
            CdmBase ref;
            while (iterator.hasNext()){
                ref = iterator.next();
                String message = "An object of " + ref.getClass().getName() + " with ID " + ref.getId() + " is referencing the object" ;
                result.addException(new ReferencedObjectUndeletableException(message));
                result.setAbort();
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uuid) {
        return dao.exists(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> find(Set<UUID> uuidSet) {
        return dao.list(uuidSet, null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public <S extends T> List<S> find(Class<S> clazz, Set<UUID> uuidSet) {
        return dao.list(clazz, uuidSet, null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findById(Set<Integer> idSet) {  //can't be called find(Set<Integer>) as this conflicts with find(Set<UUID)
        return dao.loadList(idSet, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> loadByIds(List<Integer> idList, List<String> propertyPaths){
        return dao.loadList(idList, null, propertyPaths);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> loadByIds(List<Integer> idList, List<OrderHint> orderHints, List<String> propertyPaths){
        return dao.loadList(idList, orderHints, propertyPaths);
    }

    @Override
    @Transactional(readOnly = true)
    public T find(UUID uuid) {
        return uuid == null ? null : dao.findByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public T findWithoutFlush(UUID uuid) {
        return uuid == null ? null : dao.findByUuidWithoutFlush(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public T find(int id) {
        return dao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Session getSession() {
        return dao.getSession();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> group(Class<? extends T> clazz,Integer limit, Integer start, List<Grouping> groups, List<String> propertyPaths) {
        return dao.group(clazz, limit, start, groups, propertyPaths);
    }

    @Override
    @Transactional(readOnly = true)
    public <S extends T> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths){
        return dao.list(type, limit, start, orderHints, propertyPaths);
    }

    @Override
    @Transactional(readOnly = true)
    public T load(UUID uuid) {
        return uuid == null ? null : dao.load(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public T loadWithUpdate(UUID uuid) {
        return load(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public T load(int id, List<String> propertyPaths) {
        return dao.load(id, propertyPaths);
    }

    @Override
    @Transactional(readOnly = true)
    public T loadWithoutInitializing(int id){
        return dao.loadWithoutInitializing(id);
    }

    @Override
    @Transactional(readOnly = true)
    public T load(UUID uuid, List<String> propertyPaths){
        return uuid == null ? null : dao.load(uuid, propertyPaths);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> load(List<UUID> uuids, List<String> propertyPaths){
        if(uuids == null) {
            return null;
        }

        List<T> entities = new ArrayList<>();
        for(UUID uuid : uuids) {
            entities.add(uuid == null ? null : dao.load(uuid, propertyPaths));
        }
        return entities;
    }

    @Override
    @Transactional(readOnly = false)
    public T merge(T newInstance) {
        return dao.merge(newInstance);
    }

    /**
     * Same as #merge(T) but with the possibility to fully remove further entities
     * from the database during the same session. This may become necessary if these
     * entities were deleted from the detached object graph and are not handled
     * via Cascade.REMOVE or orphanRemoval, e.g. when children were removed
     * from its parents and not used elsewhere anymore.
     */
    @Override
    public T merge(T detachedObject, CdmBase... removedObjects) {
        return dao.merge(detachedObject, Arrays.asList(removedObjects));
    }

    @Override
    @Transactional(readOnly = false)
    public MergeResult<T> merge(T newInstance, boolean returnTransientEntity) {
        return dao.merge(newInstance, returnTransientEntity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<T> merge(List<T> detachedObjects) {
        List<T> mergedObjects = new ArrayList<T>();
        for(T obj : detachedObjects) {
            mergedObjects.add(dao.merge(obj));
        }
        return mergedObjects;
    }

    @Override
    @Transactional(readOnly = false)
    public List<MergeResult<T>> merge(List<T> detachedObjects, boolean returnTransientEntity) {
        List<MergeResult<T>> mergedObjects = new ArrayList<MergeResult<T>>();
        for(T obj : detachedObjects) {
            mergedObjects.add(dao.merge(obj, returnTransientEntity));
        }
        return mergedObjects;
    }

    @Override
    @Transactional(readOnly = true)
    public  <S extends T> Pager<S> page(Class<S> type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Long numberOfResults = dao.count(type);
        List<S> results = new ArrayList<>();
        pageNumber = pageNumber == null ? 0 : pageNumber;
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            Integer start = pageSize == null ? 0 : pageSize * pageNumber;
            results = dao.list(type, pageSize, start, orderHints, propertyPaths);
        }
        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    @Transactional(readOnly = true)
    public UUID refresh(T persistentObject) {
        return dao.refresh(persistentObject);
    }

    @Override
    @Transactional(readOnly = false)
    public Map<UUID, T> save(Collection<? extends T> newInstances) {
        return dao.saveAll(newInstances);
    }

    @Override
    @Transactional(readOnly = false)
    public <S extends T> S save(S newInstance) {
        return dao.save(newInstance);
    }

    @Override
    public void save(T newInstance1, T newInstance2) {
        save(newInstance1);
        save(newInstance2);
    }

    @Override
    @Transactional(readOnly = false)
    public UUID saveOrUpdate(T transientObject) {
        return dao.saveOrUpdate(transientObject);
    }

    @Override
    @Transactional(readOnly = false)
    public Map<UUID, T> saveOrUpdate(Collection<T> transientInstances) {
        return dao.saveOrUpdateAll(transientInstances);
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext){
        this.appContext = appContext;
    }


    protected abstract void setDao(DAO dao);

    @Override
    @Transactional(readOnly = false)
    public UUID update(T transientObject) {
        return dao.update(transientObject);
    }

    @Override
    @Transactional(readOnly = true)
    public <S extends T> List<S> list(S example, Set<String> includeProperties, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.list(example, includeProperties, limit, start, orderHints, propertyPaths);
    }

    @Override
    @Transactional(readOnly = true)
    public <S extends T> Pager<S> page(Class<S> clazz, String param, String queryString, MatchMode matchmode,
            List<Criterion> criteria, Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths){

        List<S> records;
        long resultSize = dao.countByParam(clazz, param, queryString, matchmode, criteria);
        if(AbstractPagerImpl.hasResultsInRange(resultSize, pageIndex, pageSize)){
            records = dao.findByParam(clazz, param, queryString, matchmode, criteria, pageSize, pageIndex, orderHints, propertyPaths);
        } else {
            records = new ArrayList<>();
        }
        return new DefaultPagerImpl<>(pageIndex, resultSize, pageSize, records);
    }


    @Override
    @Transactional(readOnly = true)
    public <S extends T> Pager<S> pageByParamWithRestrictions(Class<S> clazz, String param, String queryString, MatchMode matchmode, List<Restriction<?>> restrictions, Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths){

        List<S> records;
        long resultSize = dao.countByParamWithRestrictions(clazz, param, queryString, matchmode, restrictions);
        if(AbstractPagerImpl.hasResultsInRange(resultSize, pageIndex, pageSize)){
            records = dao.findByParamWithRestrictions(clazz, param, queryString, matchmode, restrictions, pageSize, pageIndex, orderHints, propertyPaths);
        } else {
            records = new ArrayList<>();
        }
        Pager<S> pager = new DefaultPagerImpl<>(pageIndex, resultSize, pageSize, records);
        return pager;
    }

    @Override
    @Transactional(readOnly = true)
    public <S extends T> Pager<S> page(Class<S> clazz, List<Restriction<?>> restrictions,
                Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths){

        List<S> records;
        long resultSize = dao.count(clazz, restrictions);
        if(AbstractPagerImpl.hasResultsInRange(resultSize, pageIndex, pageSize)){
            //#9943
            pageIndex = pageIndex == null ? 0 : pageIndex;
            Integer start = 0;
            if (pageIndex > 0 && pageSize != null) {
                start = pageIndex * pageSize;
            }
            records = dao.list(clazz, restrictions, pageSize, start, orderHints, propertyPaths);
        } else {
            records = new ArrayList<>();
        }
        Pager<S> pager = new DefaultPagerImpl<>(pageIndex, resultSize, pageSize, records);
        return pager;
    }


    /**
     * Throws an exception if the publishable entity should not be published.
     * @param publishable the publishable entity
     * @param includeUnpublished should publish be checked
     * @param message the error message to include
     * @throws UnpublishedException thrown if entity is not public and unpublished should not be included
     */
    protected void checkPublished(IPublishable publishable, boolean includeUnpublished, String message) throws UnpublishedException {
        if (!(includeUnpublished || publishable.isPublish())){
            throw new UnpublishedException("Access denied. "+  message);
        }
    }



}
