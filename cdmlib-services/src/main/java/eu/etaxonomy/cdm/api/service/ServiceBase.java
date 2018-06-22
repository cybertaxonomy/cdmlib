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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.exception.UnpublishedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class ServiceBase<T extends CdmBase, DAO extends ICdmEntityDao<T>>
            implements IService<T>, ApplicationContextAware {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ServiceBase.class);

    protected ApplicationContext appContext;

    public final static boolean NO_UNPUBLISHED = DaoBase.NO_UNPUBLISHED;  //constant for unpublished
    public final static boolean INCLUDE_UNPUBLISHED = DaoBase.INCLUDE_UNPUBLISHED;  //constant for unpublished

    protected DAO dao;

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
    public List<T> findById(Set<Integer> idSet) {  //can't be called find(Set<Integer>) as this conflicts with find(Set<UUID)
        return dao.loadList(idSet, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> loadByIds(List<Integer> idList, List<String> propertyPaths){
        return dao.loadList(idList, propertyPaths);
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
        return dao.list(type,limit, start, orderHints, propertyPaths);
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
    public Map<UUID, T> save(Collection<T> newInstances) {
        return dao.saveAll(newInstances);
    }

    @Override
    @Transactional(readOnly = false)
    public T save(T newInstance) {
        return dao.save(newInstance);
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
    public List<T> list(T example, Set<String> includeProperties, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.list(example, includeProperties, limit, start, orderHints, propertyPaths);
    }


    /**
     * Throws an exception if the publishable entity should not be published.
     * @param publishable the publishable entity
     * @param includeUnpublished should publish be checked
     * @param message the error message to include
     * @throws UnpublishedException thrown if entity is not public and unpublished should not be included
     */
    protected void checkPublished(IPublishable publishable, boolean includeUnpublished, String message) throws UnpublishedException {
        if (!(includeUnpublished || !publishable.isPublish())){
            throw new UnpublishedException("Access denied. "+  message);
        }
    }



}
