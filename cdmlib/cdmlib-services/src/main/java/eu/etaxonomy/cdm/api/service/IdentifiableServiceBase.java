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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.HibernateBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.initializer.AutoPropertyInitializer;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

public abstract class IdentifiableServiceBase<T extends IdentifiableEntity,DAO extends IIdentifiableDao<T>> extends AnnotatableServiceBase<T,DAO> 
						implements IIdentifiableEntityService<T>{
	
//    @Autowired
//    protected ICommonService commonService;

	
	protected static final int UPDATE_TITLE_CACHE_DEFAULT_STEP_SIZE = 1000;
	protected static final  Logger logger = Logger.getLogger(IdentifiableServiceBase.class);

	@Transactional(readOnly = true)
	public Pager<Rights> getRights(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countRights(t);
		
		List<Rights> results = new ArrayList<Rights>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getRights(t, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<Rights>(pageNumber, numberOfResults, pageSize, results);
	}
	
	@Transactional(readOnly = true)
	public Pager<IdentifiableSource> getSources(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		 Integer numberOfResults = dao.countSources(t);
			
			List<IdentifiableSource> results = new ArrayList<IdentifiableSource>();
			if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
				results = dao.getSources(t, pageSize, pageNumber,propertyPaths); 
			}
			
			return new DefaultPagerImpl<IdentifiableSource>(pageNumber, numberOfResults, pageSize, results);
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
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
				results = dao.findByTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths); 
		 }
			
		  return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}
	
	@Transactional(readOnly = true)
	public Pager<T> findByTitle(IIdentifiableEntityServiceConfigurator<T> config){
		return findByTitle(config.getClazz(), config.getTitleSearchStringSqlized(), config.getMatchMode(), config.getCriteria(), config.getPageSize(), config.getPageNumber(), config.getOrderHints(), config.getPropertyPaths());
	}
	
	@Transactional(readOnly = true)
	public List<T> listByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		 Integer numberOfResults = dao.countByTitle(clazz, queryString, matchmode, criteria);
			
		 List<T> results = new ArrayList<T>();
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
				results = dao.findByTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths); 
		 }
		 return results;
	}
	
	@Transactional(readOnly = true)
	public Pager<T> findTitleCache(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, MatchMode matchMode){
		long numberOfResults = dao.countTitleCache(clazz, queryString, matchMode);
			
		 List<T> results = new ArrayList<T>();
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
				results = dao.findTitleCache(clazz, queryString, pageSize, pageNumber, orderHints, matchMode);
		 }
		 int r = 0;
		 r += numberOfResults;
			
		  return new DefaultPagerImpl<T>(pageNumber, r , pageSize, results);
	}

	@Transactional(readOnly = true)
	public List<T> listByReferenceTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		 Integer numberOfResults = dao.countByReferenceTitle(clazz, queryString, matchmode, criteria);
			
		 List<T> results = new ArrayList<T>();
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
				results = dao.findByReferenceTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths); 
		 }
		 return results;
	}
	
	@Transactional(readOnly = true)
	public T find(LSID lsid) {
		return dao.find(lsid);
	}
	
	@Transactional(readOnly = true)
	public Pager<T> search(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.count(clazz,queryString);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.search(clazz,queryString, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache()
	 */
	@Override
	@Transactional(readOnly = false)
	public void updateTitleCache() {
		updateTitleCache(null, null, null, null);
	}
	
	@Transactional(readOnly = false)  //TODO check transactional behaviour, e.g. what happens with the session if count is very large 
	protected <S extends T > void updateTitleCacheImpl(Class<S> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<T> cacheStrategy, IProgressMonitor monitor) {
		if (stepSize == null){
			stepSize = UPDATE_TITLE_CACHE_DEFAULT_STEP_SIZE;
		}
		if (monitor == null){
			monitor = DefaultProgressMonitor.NewInstance();
		}
		
		int count = dao.count(clazz);
		monitor.beginTask("update titles", count);
		int worked = 0;
		for(int i = 0 ; i < count ; i = i + stepSize){
			// not sure if such strict ordering is necessary here, but for safety reasons I do it
			ArrayList<OrderHint> orderHints = new ArrayList<OrderHint>();
			orderHints.add( new OrderHint("id", OrderHint.SortOrder.ASCENDING));
			
			
			Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> oldAutoInit = switchOfAutoinitializer();
			List<S> list = this.list(clazz, stepSize, i, orderHints, null);
			switchOnOldAutoInitializer(oldAutoInit);
			
			List<T> entitiesToUpdate = new ArrayList<T>();
			for (T entity : list){
				if (entity.isProtectedTitleCache() == false){
					updateTitleCacheForSingleEntity(cacheStrategy, entitiesToUpdate, entity);
				}
				worked++;
			}
			for (T entity: entitiesToUpdate){
				if (entity.getTitleCache() != null){
					//System.err.println(entity.getTitleCache());
				}else{
					//System.err.println("no titleCache" + ((NonViralName)entity).getNameCache());
				}
			}
			saveOrUpdate(entitiesToUpdate);
			monitor.worked(list.size());
			if (monitor.isCanceled()){
				monitor.done();
				return;
			}
		}
		monitor.done();
	}

	/**
	 * Brings back all auto initializers to the bean initializer
	 * @see #switchOfAutoinitializer()
	 * @param oldAutoInit
	 */
	protected void switchOnOldAutoInitializer(
			Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> oldAutoInit) {
		HibernateBeanInitializer initializer = (HibernateBeanInitializer)this.appContext.getBean("defaultBeanInitializer");
		initializer.setBeanAutoInitializers(oldAutoInit);
	}

	/**
	 * Removes all auto initializers from the bean initializer
	 * 
	 * @see #switchOnOldAutoInitializer(Map)
	 * @return 
	 */
	protected Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> switchOfAutoinitializer() {
		HibernateBeanInitializer initializer = (HibernateBeanInitializer)this.appContext.getBean("defaultBeanInitializer");
		Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> oldAutoInitializers = initializer.getBeanAutoInitializers();
		Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> map = new HashMap<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>>();
		initializer.setBeanAutoInitializers(map);
		return oldAutoInitializers;
	}

	/**
	 * @param cacheStrategy
	 * @param entitiesToUpdate
	 * @param entity
	 */
	/**
	 * @param cacheStrategy
	 * @param entitiesToUpdate
	 * @param entity
	 */
	private void updateTitleCacheForSingleEntity(
			IIdentifiableEntityCacheStrategy<T> cacheStrategy,
			List<T> entitiesToUpdate, T entity) {
		
		assert (entity.isProtectedTitleCache() == false );
		
		//exclude recursive inreferences
		if (entity.isInstanceOf(Reference.class)){
			Reference<?> ref = CdmBase.deproxy(entity, Reference.class);
			if (ref.getInReference() != null && ref.getInReference().equals(ref)){
				return;
			}
		}
		
		
		//define the correct cache strategy
		IIdentifiableEntityCacheStrategy entityCacheStrategy = cacheStrategy;
		if (entityCacheStrategy == null){
			entityCacheStrategy = entity.getCacheStrategy();
			//FIXME find out why the wrong cache strategy is loaded here, see #1876 
			if (entity instanceof Reference){
				entityCacheStrategy = ReferenceFactory.newReference(((Reference)entity).getType()).getCacheStrategy();
			}
		}
		entity.setCacheStrategy(entityCacheStrategy);
		
		
		//old titleCache
		entity.setProtectedTitleCache(true);
		String oldTitleCache = entity.getTitleCache();
		entity.setTitleCache(oldTitleCache, false);   //before we had entity.setProtectedTitleCache(false) but this deleted the titleCache itself
		
		//NonViralNames have more caches //TODO handle in NameService
		String oldNameCache = null;
		String oldFullTitleCache = null;
		if (entity instanceof NonViralName ){
			NonViralName<?> nvn = (NonViralName) entity;
			if (!nvn.isProtectedNameCache()){
				nvn.setProtectedNameCache(true);
				oldNameCache = nvn.getNameCache();
				nvn.setProtectedNameCache(false);
			}
			if (!nvn.isProtectedFullTitleCache()){
				nvn.setProtectedFullTitleCache(true);
				oldFullTitleCache = nvn.getFullTitleCache();
				nvn.setProtectedFullTitleCache(false);
			}
		}
		setOtherCachesNull(entity); //TODO find better solution
		
		String newTitleCache = entityCacheStrategy.getTitleCache(entity);
		if (oldTitleCache == null || oldTitleCache != null && ! oldTitleCache.equals(newTitleCache) ){
			entity.setTitleCache(null, false);
			String newCache = entity.getTitleCache();
			if (newCache == null){
				logger.warn("newCache should never be null");
			}
			if (oldTitleCache == null){
				logger.info("oldTitleCache should never be null");
			}
			if (entity instanceof NonViralName){
				NonViralName<?> nvn = (NonViralName) entity;
				nvn.getNameCache();
				nvn.getFullTitleCache();
			}
			entitiesToUpdate.add(entity);
		}else if (entity instanceof NonViralName){
			NonViralName<?> nvn = (NonViralName) entity;
			String newnameCache = nvn.getNameCache();
			String newFullTitleCache = nvn.getFullTitleCache();
			if (oldNameCache == null || (oldNameCache != null && !oldNameCache.equals(newnameCache))){
				entitiesToUpdate.add(entity);
			}else if (oldFullTitleCache == null || (oldFullTitleCache != null && !oldFullTitleCache.equals(newFullTitleCache))){
				entitiesToUpdate.add(entity);
			}
		};
	}
	
	

	/**
	 * Needs override if not only the title cache should be set to null to
	 * generate the correct new title cache
	 */
	protected void setOtherCachesNull(T entity) {
		return;
	}
	

	
	private class DeduplicateState{
		String lastTitleCache;
		Integer pageSize = 50;
		int nPages = 3;
		int startPage = 0;
		boolean isCompleted = false;
		int result; 
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#deduplicate(java.lang.Class, eu.etaxonomy.cdm.strategy.match.IMatchStrategy, eu.etaxonomy.cdm.strategy.merge.IMergeStrategy)
	 */
	@Override
	@Transactional(readOnly = false)
	public int deduplicate(Class<? extends T> clazz, IMatchStrategy matchStrategy, IMergeStrategy mergeStrategy) {
		DeduplicateState dedupState = new DeduplicateState();
		
		if (clazz == null){
			logger.warn("Deduplication clazz must not be null!");
			return 0;
		}
		if (! ( IMatchable.class.isAssignableFrom(clazz) && IMergable.class.isAssignableFrom(clazz) )  ){
			logger.warn("Deduplication implemented only for classes implementing IMatchable and IMergeable. No deduplication performed!");
			return 0;
		}
		Class matchableClass = clazz;
		if (matchStrategy == null){
			matchStrategy = DefaultMatchStrategy.NewInstance(matchableClass);
		}
		List<T> nextGroup = new ArrayList<T>();
		
		int result = 0;
//		double countTotal = count(clazz);
//		
//		Number countPagesN = Math.ceil(countTotal/dedupState.pageSize.doubleValue()) ; 
//		int countPages = countPagesN.intValue();
//		
		
		List<OrderHint> orderHints = Arrays.asList(new OrderHint[]{new OrderHint("titleCache", SortOrder.ASCENDING)});
		
		while (! dedupState.isCompleted){
			//get x page sizes
			List<T> objectList = getPages(clazz, dedupState, orderHints);
			//after each page check if any changes took place
			int nUnEqualPages = handleAllPages(objectList, dedupState, nextGroup, matchStrategy, mergeStrategy);
			nUnEqualPages = nUnEqualPages + dedupState.pageSize * dedupState.startPage;
			//refresh start page counter
			int finishedPages = nUnEqualPages / dedupState.pageSize;
			dedupState.startPage = finishedPages;
		}
				
		result += handleLastGroup(nextGroup, matchStrategy, mergeStrategy);
		return result;
	}


	private int handleAllPages(List<T> objectList, DeduplicateState dedupState, List<T> nextGroup, IMatchStrategy matchStrategy, IMergeStrategy mergeStrategy) {
		int nUnEqual = 0;
		for (T object : objectList){
			String currentTitleCache = object.getTitleCache();
			if (currentTitleCache != null && currentTitleCache.equals(dedupState.lastTitleCache)){
				//=titleCache
				nextGroup.add(object);
			}else{
				//<> titleCache
				dedupState.result += handleLastGroup(nextGroup, matchStrategy, mergeStrategy);
				nextGroup = new ArrayList<T>();
				nextGroup.add(object);
				nUnEqual++;	
			}
			dedupState.lastTitleCache = currentTitleCache;
		}
		handleLastGroup(nextGroup, matchStrategy, mergeStrategy);
		return nUnEqual;
	}

	private List<T> getPages(Class<? extends T> clazz, DeduplicateState dedupState, List<OrderHint> orderHints) {
		List<T> result = new ArrayList<T>();
		for (int pageNo = dedupState.startPage; pageNo < dedupState.startPage + dedupState.nPages; pageNo++){
			List<T> objectList = listByTitle(clazz, null, null, null, dedupState.pageSize, pageNo, orderHints, null);
			result.addAll(objectList);
		}
		if (result.size()< dedupState.nPages * dedupState.pageSize ){
			dedupState.isCompleted = true;
		}
		return result;
	}

	private int handleLastGroup(List<T> group, IMatchStrategy matchStrategy, IMergeStrategy mergeStrategy) {
		int result = 0;
		int size = group.size();
		Set<Integer> exclude = new HashSet<Integer>();  //set to collect all objects, that have been merged already
		for (int i = 0; i < size - 1; i++){
			if (exclude.contains(i)){
				continue;
			}
			for (int j = i + 1; j < size; j++){
				if (exclude.contains(j)){
					continue;
				}
				T firstObject = group.get(i);
				T secondObject = group.get(j);
				
				try {
					if (matchStrategy.invoke((IMatchable)firstObject, (IMatchable)secondObject)){
						commonService.merge((IMergable)firstObject, (IMergable)secondObject, mergeStrategy);
						exclude.add(j);
						result++;
					}
				} catch (MatchException e) {
					logger.warn("MatchException when trying to match " + firstObject.getTitleCache());
					e.printStackTrace();
				} catch (MergeException e) {
					logger.warn("MergeException when trying to merge " + firstObject.getTitleCache());
					e.printStackTrace();
				}
			}
		}
		return result;
	}	
	
	 public Integer countByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria){
		 Integer numberOfResults = dao.countByTitle(clazz, queryString, matchmode, criteria);
		 
		 return numberOfResults;
	 }
	 
	@Transactional(readOnly = true)
	public Integer countByTitle(IIdentifiableEntityServiceConfigurator<T> config){
		return countByTitle(config.getClazz(), config.getTitleSearchStringSqlized(),
				config.getMatchMode(), config.getCriteria());
		
	}
	
	


}

