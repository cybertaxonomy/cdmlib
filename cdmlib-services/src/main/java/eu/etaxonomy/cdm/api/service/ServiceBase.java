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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class ServiceBase<T extends CdmBase, DAO extends ICdmEntityDao<T>> implements IService<T>, ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(ServiceBase.class);
	
	//flush after saving this number of objects
	int flushAfterNo = 2000;
	protected ApplicationContext appContext;

	@Qualifier("baseDao")
	protected DAO dao;

	protected abstract void setDao(DAO dao);
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.Iyyy#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
	}

	public T getCdmObjectByUuid(UUID uuid) {
		return dao.findByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public <TYPE extends T> int count(Class<TYPE> clazz) {
		return dao.count(clazz);
	}
	
	@Transactional(readOnly = true)
	public int count() {
		return dao.count();
	}

	/**
	 * FIXME harmonise with saveOrUpdate
	 * @param cdmObj
	 * @return
	 */
	@Transactional(readOnly = false)
	protected UUID saveCdmObject(T cdmObj){
		if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
		return dao.saveOrUpdate(cdmObj);
	}
	

	/**
	 * FIXME harmonise with saveOrUpdate
	 * @param cdmObj
	 * @return
	 */
	@Transactional(readOnly = false)
	protected UUID saveCdmObject(T cdmObj, TransactionStatus txStatus){
		// TODO: Implement with considering txStatus
		if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
		return dao.saveOrUpdate(cdmObj);
	}
	
	/**
	 * FIXME harmonise with saveAll
	 * @param <S>
	 * @param cdmObjCollection
	 * @return
	 */
	@Transactional(readOnly = false)
	protected <S extends T> Map<UUID, S> saveCdmObjectAll(Collection<? extends S> cdmObjCollection){
		int types = cdmObjCollection.getClass().getTypeParameters().length;
		if (types > 0){
			if (logger.isDebugEnabled()){logger.debug("ClassType: + " + cdmObjCollection.getClass().getTypeParameters()[0]);}
		}
		
		Map<UUID, S> resultMap = new HashMap<UUID, S>();
		Iterator<? extends S> iterator = cdmObjCollection.iterator();
		int i = 0;
			while(iterator.hasNext()){
				if ( ( (i % 5000) == 0) && (i > 0)   ){logger.debug("Saved " + i + " objects" );}
				S cdmObj = iterator.next();
				UUID uuid = saveCdmObject(cdmObj);
//				if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
				resultMap.put(uuid, cdmObj);
				i++;
				if ( (i % flushAfterNo) == 0){
					try{
									logger.debug("flush");
					dao.flush();
					}catch(Exception e){
						logger.error("UUUIIIII");
						e.printStackTrace();
					}
				}
			}

		if ( logger.isInfoEnabled() ){logger.info("Saved " + i + " objects" );}
		return resultMap;
	}

	@Transactional(readOnly = false)
	public UUID delete(T persistentObject) {
		return dao.delete(persistentObject);
	}

	@Transactional(readOnly = true)
	public boolean exists(UUID uuid) {
		return dao.exists(uuid);
	}

	@Transactional(readOnly = true)
	public T findByUuid(UUID uuid) {
		return dao.findByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public T load(UUID uuid) {
		return dao.load(uuid);
	}
	
	@Transactional(readOnly = true)
	public T load(UUID uuid, List<String> propertyPaths){
		return dao.load(uuid, propertyPaths);
	}

	@Transactional(readOnly = true)
	public <TYPE extends T> List<TYPE> list(Class<TYPE> type, int limit,int start) {
		return dao.list(type, limit, start);
	}
	
	@Transactional(readOnly = true)
	public Pager<T> list(Integer pageSize, Integer pageNumber){
		return list(pageSize, pageNumber, null);
	}
	
	@Transactional (readOnly = true)
	public Pager<T> list(Integer pageSize, Integer pageNumber, List<OrderHint> orderHints){
		return list(pageSize,pageNumber,orderHints,null);
	}
	
	@Transactional (readOnly = true)
	public Pager<T> list(Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
		Integer numberOfResults = dao.count();
		List<T> results = new ArrayList<T>();
		pageNumber = pageNumber == null ? 0 : pageNumber;
		if(numberOfResults > 0) { // no point checking again
			Integer start = pageSize == null ? 0 : pageSize * (pageNumber - 1);
			results = dao.list(pageSize, start, orderHints,propertyPaths);
		}
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}
	
	@Transactional (readOnly = true)
	public <TYPE extends T> Pager<TYPE> list(Class<TYPE> type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
		Integer numberOfResults = dao.count(type);
		List<TYPE> results = new ArrayList<TYPE>();
		pageNumber = pageNumber == null ? 0 : pageNumber;
		if(numberOfResults > 0) { // no point checking again
			Integer start = pageSize == null ? 0 : pageSize * (pageNumber - 1);
			results = dao.list(type,pageSize, start, orderHints,propertyPaths);
		}
		return new DefaultPagerImpl<TYPE>(pageNumber, numberOfResults, pageSize, results);
	}

	@Transactional(readOnly = false)
	public UUID save(T newInstance) {
		return dao.save(newInstance);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, T> saveAll(Collection<T> newInstances) {
		return dao.saveAll(newInstances);
	}

	@Transactional(readOnly = false)
	public UUID saveOrUpdate(T transientObject) {
		return dao.saveOrUpdate(transientObject);
	}

	@Transactional(readOnly = false)
	public UUID update(T transientObject) {
		return dao.update(transientObject);
	}

	@Transactional(readOnly = true)
	public UUID refresh(T persistentObject) {
		return dao.refresh(persistentObject);
	}
	
	/**
	 * FIXME harmonise with delete()
	 * @param cdmObj
	 * @return
	 */
	@Transactional(readOnly = false)
	protected UUID removeCdmObject(T cdmObj){
		if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
		return dao.delete(cdmObj);
	}
	
	@Transactional(readOnly = true)
	public List<T> list(int limit, int start) {
		return dao.list(limit, start);
	}

	@Transactional(readOnly = true)
	public List<T> rows(String tableName, int limit, int start) {
		return dao.rows(tableName, limit, start);
	}
}
