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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.dto.CdmEntityIdentifier;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.hibernate.HibernateBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.initializer.AutoPropertyInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategyEqual;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

public abstract class IdentifiableServiceBase<T extends IdentifiableEntity, DAO extends IIdentifiableDao<T>>
        extends AnnotatableServiceBase<T,DAO>
		implements IIdentifiableEntityService<T>{

	protected static final int UPDATE_TITLE_CACHE_DEFAULT_STEP_SIZE = 1000;
	protected static final  Logger logger = Logger.getLogger(IdentifiableServiceBase.class);

	@Override
	@Transactional(readOnly = true)
	public Pager<Rights> getRights(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countRights(t);

		List<Rights> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getRights(t, pageSize, pageNumber,propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	@Transactional(readOnly = true)
	public Pager<IdentifiableSource> getSources(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		 long numberOfResults = dao.countSources(t);

		 List<IdentifiableSource> results = new ArrayList<>();
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			 results = dao.getSources(t, pageSize, pageNumber,propertyPaths);
		 }

		 return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}


	@Transactional(readOnly = false)
	@Override
	public T replace(T x, T y) {
		return dao.replace(x, y);
	}

	/*
	 * TODO - Migrated from CommonServiceBase
	 */
	@Transactional(readOnly = true)
	@Override
	public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace) {
		ISourceable<?> result = null;

		List<T> list = dao.findOriginalSourceByIdInSource(idInSource, idNamespace);
		if (! list.isEmpty()){
			result = list.get(0);
		}
		return result;
	}

	@Transactional(readOnly = true)
	@Override
	public List<UuidAndTitleCache<T>> getUuidAndTitleCache(Integer limit, String pattern) {
		return dao.getUuidAndTitleCache(limit, pattern);
	}

    @Transactional(readOnly = true)
    @Override
    public <S extends T> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz,Integer limit, String pattern) {
        return dao.getUuidAndTitleCache(clazz, limit, pattern);
    }

    @Transactional(readOnly = true)
    @Override
    public String getTitleCache(UUID uuid, boolean refresh){
        return dao.getTitleCache(uuid, refresh);
    }

    @Transactional(readOnly = true)
    @Override
    public <S extends T> Pager<S> findByTitle(Class<S> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
         long numberOfResults = dao.countByTitle(clazz, queryString, matchmode, criteria);

         List<S> results = new ArrayList<>();
         if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
                results = dao.findByTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
         }

         return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

	@Transactional(readOnly = true)
	@Override
	public <S extends T> Pager<S> findByTitleWithRestrictions(Class<S> clazz, String queryString, MatchMode matchmode, List<Restriction<?>> restrictions, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		 long numberOfResults = dao.countByTitleWithRestrictions(clazz, queryString, matchmode, restrictions);

		 List<S> results = new ArrayList<>();
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
				results = dao.findByTitleWithRestrictions(clazz, queryString, matchmode, restrictions, pageSize, pageNumber, orderHints, propertyPaths);
		 }

		 return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}


	@Transactional(readOnly = true)
	@Override
	public <S extends T> Pager<S> findByTitle(IIdentifiableEntityServiceConfigurator<S> config){

	    boolean withRestrictions = config.getRestrictions() != null && !config.getRestrictions().isEmpty();
	    boolean withCriteria = config.getCriteria() != null && !config.getCriteria().isEmpty();

	    if(withCriteria && withRestrictions){
	        throw new RuntimeException("Restrictions and Criteria can not be used at the same time");
	    } else if(withRestrictions){
	        return findByTitleWithRestrictions(config.getClazz(), config.getTitleSearchStringSqlized(), config.getMatchMode(), config.getRestrictions(), config.getPageSize(), config.getPageNumber(), config.getOrderHints(), config.getPropertyPaths());
	    } else {
	        return findByTitle(config.getClazz(), config.getTitleSearchStringSqlized(), config.getMatchMode(), config.getCriteria(), config.getPageSize(), config.getPageNumber(), config.getOrderHints(), config.getPropertyPaths());
	    }
	}

   @Transactional(readOnly = true)
    @Override
    public <S extends T> List<S> listByTitle(Class<S> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
         long numberOfResults = dao.countByTitle(clazz, queryString, matchmode, criteria);

         List<S> results = new ArrayList<>();
         if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
                results = dao.findByTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
         }
         return results;
    }

	@Transactional(readOnly = true)
	@Override
	public <S extends T> List<S> listByTitleWithRestrictions(Class<S> clazz, String queryString,MatchMode matchmode, List<Restriction<?>> restrictions, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		 long numberOfResults = dao.countByTitleWithRestrictions(clazz, queryString, matchmode, restrictions);

		 List<S> results = new ArrayList<>();
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
				results = dao.findByTitleWithRestrictions(clazz, queryString, matchmode, restrictions, pageSize, pageNumber, orderHints, propertyPaths);
		 }
		 return results;
	}

	@Transactional(readOnly = true)
	@Override
	public  Pager<String> findTitleCache(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, MatchMode matchMode){
		long numberOfResults = dao.countTitleCache(clazz, queryString, matchMode);

		 List<String> results = new ArrayList<>();
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
				results = dao.findTitleCache(clazz, queryString, pageSize, pageNumber, orderHints, matchMode);
		 }
		 long r = 0;
		 r += numberOfResults;

		 return new DefaultPagerImpl<>(pageNumber, r , pageSize, results);
	}

    @Transactional(readOnly = true)
    @Override
    public <S extends T> List<S> listByReferenceTitle(Class<S> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
         long numberOfResults = dao.countByReferenceTitle(clazz, queryString, matchmode, criteria);

         List<S> results = new ArrayList<>();
         if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
             results = dao.findByReferenceTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
         }
         return results;
    }

	@Transactional(readOnly = true)
	@Override
	public <S extends T> List<S> listByReferenceTitleWithRestrictions(Class<S> clazz, String queryString,MatchMode matchmode, List<Restriction<?>> restrictions, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		 long numberOfResults = dao.countByReferenceTitleWithRestrictions(clazz, queryString, matchmode, restrictions);

		 List<S> results = new ArrayList<>();
		 if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
		     results = dao.findByReferenceTitleWithRestrictions(clazz, queryString, matchmode, restrictions, pageSize, pageNumber, orderHints, propertyPaths);
		 }
		 return results;
	}

	@Transactional(readOnly = true)
	@Override
	public T find(LSID lsid) {
		return dao.find(lsid);
	}

	@Transactional(readOnly = true)
	@Override
	public Pager<T> search(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
//	public <S extends T> Pager<S> search(Class<S> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        long numberOfResults = dao.count(clazz,queryString);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.search(clazz,queryString, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	@Transactional(readOnly = false)
	public UpdateResult updateCaches() {
		return updateCaches(null, null, null, null);
	}

	@Transactional(readOnly = false)  //TODO check transactional behavior, e.g. what happens with the session if count is very large
	protected <S extends T > UpdateResult updateCachesImpl(Class<S> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<T> cacheStrategy, IProgressMonitor subMonitor) {
		if (stepSize == null){
			stepSize = UPDATE_TITLE_CACHE_DEFAULT_STEP_SIZE;
		}
		if (subMonitor == null){
		    subMonitor = DefaultProgressMonitor.NewInstance();
		}
		UpdateResult result = new UpdateResult();
		long count = dao.count(clazz);

		int worked = 0;
		Set<CdmEntityIdentifier> updatedCdmIds = new HashSet<>();
		for(int i = 0 ; i < count ; i = i + stepSize){
			// not sure if such strict ordering is necessary here, but for safety reasons I do it
			ArrayList<OrderHint> orderHints = new ArrayList<>();
			orderHints.add( new OrderHint("id", OrderHint.SortOrder.ASCENDING));

			Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> oldAutoInit = switchOfAutoinitializer();
			List<S> list = this.list(clazz, stepSize, i, orderHints, null);
			switchOnOldAutoInitializer(oldAutoInit);

			for (T entity : list){
				entity = HibernateProxyHelper.deproxy(entity);
			    if (entity.updateCaches(cacheStrategy)){
			        updatedCdmIds.add(CdmEntityIdentifier.NewInstance(entity.getId(), clazz));
			    }
				worked++;
				subMonitor.internalWorked(1);
			}

			if (subMonitor.isCanceled()){
				break;
			}
		}
		result.addUpdatedCdmIds(updatedCdmIds);
		return result;
	}

	/**
	 * Brings back all auto initializers to the bean initializer
	 * @see #switchOfAutoinitializer()
	 * @param oldAutoInit
	 */
	protected void switchOnOldAutoInitializer(
			Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> oldAutoInit) {
		HibernateBeanInitializer initializer = (HibernateBeanInitializer<?>)this.appContext.getBean("defaultBeanInitializer");
		initializer.setBeanAutoInitializers(oldAutoInit);
	}

	/**
	 * Removes all auto initializers from the bean initializer
	 *
	 * @see #switchOnOldAutoInitializer(Map)
	 * @return
	 */
	protected Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> switchOfAutoinitializer() {
		HibernateBeanInitializer initializer = (HibernateBeanInitializer<?>)this.appContext.getBean("defaultBeanInitializer");
		Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> oldAutoInitializers = initializer.getBeanAutoInitializers();
		Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> map = new HashMap<>();
		initializer.setBeanAutoInitializers(map);
		return oldAutoInitializers;
	}

	private class DeduplicateState{
		String lastTitleCache;
		Integer pageSize = 50;
		int nPages = 3;
		int startPage = 0;
		boolean isCompleted = false;
		int result;
	}

	@Override
	@Transactional(readOnly = false)
	public int deduplicate(Class<? extends T> clazz, IMatchStrategyEqual matchStrategy, IMergeStrategy mergeStrategy) {
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
		List<T> nextGroup = new ArrayList<>();

		int result = 0;
//		double countTotal = count(clazz);
//
//		Number countPagesN = Math.ceil(countTotal/dedupState.pageSize.doubleValue()) ;
//		int countPages = countPagesN.intValue();
//

		List<OrderHint> orderHints = Arrays.asList(new OrderHint[]{new OrderHint("titleCache", SortOrder.ASCENDING)});

		while (! dedupState.isCompleted){
			//get x page sizes
			List<? extends T> objectList = getPages(clazz, dedupState, orderHints);
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


	private int handleAllPages(List<? extends T> objectList, DeduplicateState dedupState, List<T> nextGroup, IMatchStrategyEqual matchStrategy, IMergeStrategy mergeStrategy) {
		int nUnEqual = 0;
		for (T object : objectList){
			String currentTitleCache = object.getTitleCache();
			if (currentTitleCache != null && currentTitleCache.equals(dedupState.lastTitleCache)){
				//=titleCache
				nextGroup.add(object);
			}else{
				//<> titleCache
				dedupState.result += handleLastGroup(nextGroup, matchStrategy, mergeStrategy);
				nextGroup = new ArrayList<>();
				nextGroup.add(object);
				nUnEqual++;
			}
			dedupState.lastTitleCache = currentTitleCache;
		}
		handleLastGroup(nextGroup, matchStrategy, mergeStrategy);
		return nUnEqual;
	}

	private <S extends T> List<S> getPages(Class<S> clazz, DeduplicateState dedupState, List<OrderHint> orderHints) {
		List<S> result = new ArrayList<>();
		for (int pageNo = dedupState.startPage; pageNo < dedupState.startPage + dedupState.nPages; pageNo++){
			List<S> objectList = this.list(clazz, dedupState.pageSize, pageNo, orderHints, null);
			result.addAll(objectList);
		}
		if (result.size()< dedupState.nPages * dedupState.pageSize ){
			dedupState.isCompleted = true;
		}
		return result;
	}

	private int handleLastGroup(List<T> group, IMatchStrategyEqual matchStrategy, IMergeStrategy mergeStrategy) {
		int result = 0;
		int size = group.size();
		Set<Integer> exclude = new HashSet<>();  //set to collect all objects, that have been merged already
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
					if (matchStrategy.invoke((IMatchable)firstObject, (IMatchable)secondObject).isSuccessful()){
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

    @Transactional(readOnly = true)
    @Override
    public long countByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria){
         long numberOfResults = dao.countByTitle(clazz, queryString, matchmode, criteria);

         return numberOfResults;
    }

	@Transactional(readOnly = true)
	@Override
	public long countByTitleWithRestrictions(Class<? extends T> clazz, String queryString, MatchMode matchmode,  List<Restriction<?>> restrictions){
		 long numberOfResults = dao.countByTitleWithRestrictions(clazz, queryString, matchmode, restrictions);

		 return numberOfResults;
	}

	@Transactional(readOnly = true)
	@Override
	public long countByTitle(IIdentifiableEntityServiceConfigurator<T> config){

        boolean withRestrictions = config.getRestrictions() != null && !config.getRestrictions().isEmpty();
        boolean withCriteria = config.getCriteria() != null && !config.getCriteria().isEmpty();

        if(withCriteria && withRestrictions){
            throw new RuntimeException("Restrictions and Criteria can not be used at the same time");
        } else if(withRestrictions){
            return countByTitleWithRestrictions(config.getClazz(), config.getTitleSearchStringSqlized(), config.getMatchMode(), config.getRestrictions());
        } else {
            return countByTitle(config.getClazz(), config.getTitleSearchStringSqlized(), config.getMatchMode(), config.getCriteria());
        }

	}

	@Override
	@Transactional(readOnly = true)
	public <S extends T> Pager<IdentifiedEntityDTO<S>> findByIdentifier(
			Class<S> clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode,
			boolean includeEntity, Integer pageSize,
			Integer pageNumber,	List<String> propertyPaths) {

		long numberOfResults = dao.countByIdentifier(clazz, identifier, identifierType, matchmode);
        List<Object[]> daoResults = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
        	daoResults = dao.findByIdentifier(clazz, identifier, identifierType,
    				matchmode, includeEntity, pageSize, pageNumber, propertyPaths);
        }

        List<IdentifiedEntityDTO<S>> result = new ArrayList<>();
        for (Object[] daoObj : daoResults){
        	if (includeEntity){
        		result.add(new IdentifiedEntityDTO<>((DefinedTerm)daoObj[0], (String)daoObj[1], (S)daoObj[2]));
        	}else{
        		result.add(new IdentifiedEntityDTO<>((DefinedTerm)daoObj[0], (String)daoObj[1], (UUID)daoObj[2], (String)daoObj[3], null));
        	}
        }
		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, result);
	}

	@Override
    @Transactional(readOnly = true)
    public <S extends T> List<IdentifiedEntityDTO<S>> listByIdentifier(
            Class<S> clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode,
            boolean includeEntity, List<String> propertyPaths, Integer limit) {

        long numberOfResults = dao.countByIdentifier(clazz, identifier, identifierType, matchmode);
        List<Object[]> daoResults = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            daoResults = dao.findByIdentifier(clazz, identifier, identifierType,
                    matchmode, includeEntity, limit, 0, propertyPaths);
        }

        List<IdentifiedEntityDTO<S>> result = new ArrayList<>();
        for (Object[] daoObj : daoResults){
            if (includeEntity){
                result.add(new IdentifiedEntityDTO<>((DefinedTerm)daoObj[0], (String)daoObj[1], (S)daoObj[2]));
            }else{
                result.add(new IdentifiedEntityDTO<>((DefinedTerm)daoObj[0], (String)daoObj[1], (UUID)daoObj[2], (String)daoObj[3], null));
            }
        }
        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public <S extends T> Pager<MarkedEntityDTO<S>> findByMarker(
            Class<S> clazz, MarkerType markerType, Boolean markerValue,
            boolean includeEntity, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {

        Long numberOfResults = dao.countByMarker(clazz, markerType, markerValue);
        List<Object[]> daoResults = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            daoResults = dao.findByMarker(clazz, markerType, markerValue, includeEntity,
                    pageSize, pageNumber, propertyPaths);
        }

        List<MarkedEntityDTO<S>> result = new ArrayList<>();
        for (Object[] daoObj : daoResults){
            if (includeEntity){
                result.add(new MarkedEntityDTO<>((MarkerType)daoObj[0], (Boolean)daoObj[1], (S)daoObj[2]));
            }else{
                result.add(new MarkedEntityDTO<>((MarkerType)daoObj[0], (Boolean)daoObj[1], (UUID)daoObj[2], (String)daoObj[3]));
            }
        }
        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UuidAndTitleCache<T>> findUuidAndTitleCacheByMarker(Integer limit, String pattern,
            MarkerType markerType){

        Long numberOfResults = dao.countByMarker(null, markerType, null);
        List<UuidAndTitleCache<T>> daoResults = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            daoResults = dao.getUuidAndTitleCacheByMarker(limit, pattern, markerType);
        }


        return daoResults;
    }



}

