// $Id$
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
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class ServiceBase<T extends CdmBase, DAO extends ICdmEntityDao<T>> implements IService<T>, ApplicationContextAware {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ServiceBase.class);

    //flush after saving this number of objects
    int flushAfterNo = 2000;
    protected ApplicationContext appContext;

    protected DAO dao;

    @Override
    @Transactional(readOnly = true)
    public void lock(T t, LockMode lockMode) {
        dao.lock(t, lockMode);
    }

    @Override
    @Transactional(readOnly = true)
    public void refresh(T t, LockMode lockMode, List<String> propertyPaths) {
        dao.refresh(t, lockMode, propertyPaths);
    }

    @Override
    @Transactional(readOnly = false)
    public void clear() {
        dao.clear();
    }

    @Override
    @Transactional(readOnly = true)
    public int count(Class<? extends T> clazz) {
        return dao.count(clazz);
    }

    @Override
    @Transactional(readOnly = false)
    public String delete(T persistentObject) {
        return dao.delete(persistentObject).toString();
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
        return dao.listByIds(idSet, null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public T find(UUID uuid) {
        return dao.findByUuid(uuid);
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
        return dao.list(type,limit, start, orderHints,propertyPaths);
    }

    @Override
    @Transactional(readOnly = true)
    public T load(UUID uuid) {
        return dao.load(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public T load(UUID uuid, List<String> propertyPaths){
        return dao.load(uuid, propertyPaths);
    }

    @Override
    @Transactional(readOnly = false)
    public T merge(T newInstance) {
        return dao.merge(newInstance);
    }

    @Override
    @Transactional(readOnly = true)
    public  <S extends T> Pager<S> page(Class<S> type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.count(type);
        List<S> results = new ArrayList<S>();
        pageNumber = pageNumber == null ? 0 : pageNumber;
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            Integer start = pageSize == null ? 0 : pageSize * pageNumber;
            results = dao.list(type, pageSize, start, orderHints,propertyPaths);
        }
        return new DefaultPagerImpl<S>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    @Transactional(readOnly = true)
    public UUID refresh(T persistentObject) {
        return dao.refresh(persistentObject);
    }

    /**
     * FIXME Candidate for harmonization
     * is this method used, and if so, should it be exposed in the service layer?
     * it seems a bit incongruous that we use an ORM to hide the fact that there is a
     * database, then expose a method that talks about "rows" . . .
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> rows(String tableName, int limit, int start) {
        return dao.rows(tableName, limit, start);
    }

    @Override
    @Transactional(readOnly = false)
    public Map<UUID, T> save(Collection<T> newInstances) {
        return dao.saveAll(newInstances);
    }

    @Override
    @Transactional(readOnly = false)
    public UUID save(T newInstance) {
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
    
    

}
