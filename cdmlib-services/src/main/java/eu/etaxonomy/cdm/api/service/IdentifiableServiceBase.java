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
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public abstract class IdentifiableServiceBase<T extends IdentifiableEntity,DAO extends IIdentifiableDao<T>> extends AnnotatableServiceBase<T,DAO> 
						implements IIdentifiableEntityService<T>{
	
	protected static final int UPDATE_TITLE_CACHE_DEFAULT_STEP_SIZE = 1000;
	protected static final  Logger logger = Logger.getLogger(IdentifiableServiceBase.class);

	@Transactional(readOnly = true)
	public Pager<Rights> getRights(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countRights(t);
		
		List<Rights> results = new ArrayList<Rights>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getRights(t, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<Rights>(pageNumber, numberOfResults, pageSize, results);
	}
	
	@Transactional(readOnly = true)
	public Pager<IdentifiableSource> getSources(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		 Integer numberOfResults = dao.countSources(t);
			
			List<IdentifiableSource> results = new ArrayList<IdentifiableSource>();
			if(numberOfResults > 0) { // no point checking again
				results = dao.getSources(t, pageSize, pageNumber,propertyPaths); 
			}
			
			return new DefaultPagerImpl<IdentifiableSource>(pageNumber, numberOfResults, pageSize, results);
	}

	@Transactional(readOnly = true)
	protected List<T> findByTitle(IIdentifiableEntityServiceConfigurator config){
		return ((IIdentifiableDao)dao).findByTitle(config.getTitleSearchString(),
				config.getMatchMode(), 0, -1, null);
		// TODO: Implement parameters pageSize, pageNumber, and criteria
	}
	
	@Transactional(readOnly = false)
	public T replace(T x, T y) {
		return dao.replace(x, y);
	}
	/**
	 * FIXME Candidate for harmonization
	 * Given that this method is strongly typed, and generic, could we not simply expose it as
	 * List<T> findByTitle(String title) as it is somewhat less cumbersome. Admittedly, I don't 
	 * understand what is going on with the configurators etc. so maybe there is a good reason for
	 * the design of this method. 
	 * @param title
	 * @return
	 */
	@Transactional(readOnly = true)
	protected List<T> findCdmObjectsByTitle(String title){
		return ((IIdentifiableDao)dao).findByTitle(title);
	}
	
	@Transactional(readOnly = true)
	protected List<T> findCdmObjectsByTitle(String title, Class<T> clazz){
		return ((IIdentifiableDao)dao).findByTitleAndClass(title, clazz);
	}
	@Transactional(readOnly = true)
	protected List<T> findCdmObjectsByTitle(String title, CdmBase sessionObject){
		return ((IIdentifiableDao)dao).findByTitle(title, sessionObject);
	}
	
	/*
	 * TODO - Migrated from CommonServiceBase
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#getSourcedObjectById(java.lang.String, java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace) {
		ISourceable result = null;

		List<T> list = dao.findOriginalSourceByIdInSource(idInSource, idNamespace);
		if (! list.isEmpty()){
			result = list.get(0);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#getUuidAndTitleCache()
	 */
	@Transactional(readOnly = true)
	public List<UuidAndTitleCache<T>> getUuidAndTitleCache() {
		return dao.getUuidAndTitleCache();
	}
	
	@Transactional(readOnly = true)
	public Pager<T> findByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		 Integer numberOfResults = dao.countByTitle(clazz, queryString, matchmode, criteria);
			
		 List<T> results = new ArrayList<T>();
		 if(numberOfResults > 0) { // no point checking again
				results = dao.findByTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths); 
		 }
			
		  return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}
	
	@Transactional(readOnly = true)
	public T find(LSID lsid) {
		return dao.find(lsid);
	}
	
	@Transactional(readOnly = true)
	public Pager<T> search(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.count(clazz,queryString);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.search(clazz,queryString, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}
	
	@Transactional(readOnly = false)
	public void updateTitleCache(Class<? extends T> clazz) {
		IIdentifiableEntityCacheStrategy<T> cacheStrategy = null;
		updateTitleCache(clazz, UPDATE_TITLE_CACHE_DEFAULT_STEP_SIZE, cacheStrategy);
	}

	
	@Transactional(readOnly = false)
	public void updateTitleCache(Class<? extends T> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<T> cacheStrategy) {
		if (stepSize == null){
			stepSize = UPDATE_TITLE_CACHE_DEFAULT_STEP_SIZE;
		}
		 
		int count = dao.count(clazz);
		for(int i = 0 ; i < count ; i = i + stepSize){
			// not sure if such strict ordering is necessary here, but for safety reasons I do it
			ArrayList<OrderHint> orderHints = new ArrayList<OrderHint>();
			orderHints.add( new OrderHint("id", OrderHint.SortOrder.ASCENDING));
			List<T> list = this.list(clazz, stepSize, i, orderHints, null);
			for (T entity : list){
				if (entity.isProtectedTitleCache() == false){
					if (cacheStrategy != null){
						entity.setCacheStrategy(cacheStrategy);
					}
					entity.setTitleCache(null);
					setOtherCachesNull(entity);
					entity.getTitleCache();
				}
			}
			saveOrUpdateAll(list);
			
		}
	}

	/**
	 * Needs override if not only the title cache should be set to null to
	 * generate the correct new title cache
	 */
	protected void setOtherCachesNull(T entity) {
		return;
	}
}

