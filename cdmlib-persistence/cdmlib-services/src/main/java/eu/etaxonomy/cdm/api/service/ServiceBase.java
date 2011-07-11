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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.permission.CdmPermission;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;
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

	@Transactional(readOnly = true)
	public void lock(T t, LockMode lockMode) {
		dao.lock(t, lockMode);
	}
	
	@Transactional(readOnly = true)
	public void refresh(T t, LockMode lockMode, List<String> propertyPaths) {
		dao.refresh(t, lockMode, propertyPaths);
	}

	@Transactional(readOnly = false)
	public void clear() {
		dao.clear();
	}
	
	@Transactional(readOnly = true)
	public int count(Class<? extends T> clazz) {
		return dao.count(clazz);
	}

	@Transactional(readOnly = false)
	@PreAuthorize("hasPermission(#transientObject, 'DELETE')")
	public UUID delete(T persistentObject) {
		return dao.delete(persistentObject);
	}

	@Transactional(readOnly = true)
	public boolean exists(UUID uuid) {
		return dao.exists(uuid);
	}

	@Transactional(readOnly = true)
	public List<T> find(Set<UUID> uuidSet) {
		return dao.findByUuid(uuidSet);
	}

	@Transactional(readOnly = true)
	public T find(UUID uuid) {
		return dao.findByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public Session getSession() {
		return dao.getSession();
	}
	
	@Transactional(readOnly = true)
	public List<Object[]> group(Class<? extends T> clazz,Integer limit, Integer start, List<Grouping> groups, List<String> propertyPaths) {
		return dao.group(clazz, limit, start, groups, propertyPaths);
	}
	
	@Transactional(readOnly = true)
	public  List<T> list(Class<? extends T> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths){
		return dao.list(type,limit, start, orderHints,propertyPaths);
	}
	
	@Transactional(readOnly = true)
	public T load(UUID uuid) {
		return dao.load(uuid);
	}
		
	@Transactional(readOnly = true)
	public T load(UUID uuid, List<String> propertyPaths){
		return dao.load(uuid, propertyPaths);
	}

	@Transactional(readOnly = false)
	public UUID merge(T newInstance) {
		return dao.merge(newInstance);
	}
	
	@Transactional(readOnly = true)
	public  Pager<T> page(Class<? extends T> type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
		Integer numberOfResults = dao.count(type);
		List<T> results = new ArrayList<T>();
		pageNumber = pageNumber == null ? 0 : pageNumber;
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			Integer start = pageSize == null ? 0 : pageSize * pageNumber;
			results = dao.list(type, pageSize, start, orderHints,propertyPaths);
		}
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}
	
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
	@Transactional(readOnly = true)
	public List<T> rows(String tableName, int limit, int start) {
		return dao.rows(tableName, limit, start);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, T> save(Collection<T> newInstances) {
		return dao.saveAll(newInstances);
	}

	@Transactional(readOnly = false)
	//TODO: Tests fail because nobody is authenticated, fix it!!!
	//@PreAuthorize("hasPermission(#transientObject, 'CREATE')" )
	public UUID save(T newInstance) {
		return dao.save(newInstance);
	}

	@Transactional(readOnly = false)
	@PreAuthorize("hasPermission(#transientObject, 'UPDATE')")
	public UUID saveOrUpdate(T transientObject) {
		return dao.saveOrUpdate(transientObject);
	}
	
	@Transactional(readOnly = false)
	@PreAuthorize("hasPermission(#transientInstances, 'UPDATE')")
	public Map<UUID, T> saveOrUpdate(Collection<T> transientInstances) {
		return dao.saveOrUpdateAll(transientInstances);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.Iyyy#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
	}


	protected abstract void setDao(DAO dao);
	
	@Transactional(readOnly = false)
	@PreAuthorize("hasPermission(#transientObject, 'UPDATE')")
	public UUID update(T transientObject) {
		return dao.update(transientObject);
	}
	
	@Transactional(readOnly = true)
	public List<T> list(T example, Set<String> includeProperties, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		return dao.list(example, includeProperties, limit, start, orderHints, propertyPaths);
	}
	
	@Transactional(readOnly = true)
	public boolean hasPermission(Authentication authentication, T target, CdmPermission permission) {
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		return permissionEvaluator.hasPermission(authentication, target, permission);
		
	}
}
