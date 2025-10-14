/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.occurrence.ICollectionDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = true)
public class CollectionServiceImpl
            extends	IdentifiableServiceBase<Collection, ICollectionDao>
            implements	ICollectionService {

	@SuppressWarnings("unused")
	static private final Logger logger = LogManager.getLogger();

    @Autowired
	@Override
	protected void setDao(ICollectionDao dao) {
		this.dao = dao;
	}

	@Override
	@Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends Collection> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<Collection> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = Collection.class;
		}
		return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
	}

	@Override
    public List<Collection> searchByCode(String code) {
		return this.dao.getCollectionByCode(code);
	}

	@Override
    public List<UuidAndTitleCache<Collection>> getUuidAndTitleCacheByCode(String codePattern){
	    return this.dao.getUuidAndTitleCacheByCode(codePattern);
	}

	@Override
    public List<UuidAndTitleCache<Collection>> getUuidAndTitleCacheByCodeAndTitleCache(String codePattern){
	    Set<UuidAndTitleCache<Collection>> resultByTitleCache = new HashSet<>( dao.getUuidAndTitleCache(null, codePattern));
	    Set<UuidAndTitleCache<Collection>> resultByCode = new HashSet<>(dao.getUuidAndTitleCacheByCode(codePattern));
	    Set<UuidAndTitleCache<Collection>> result = new HashSet<>();
	    Map<Integer, UuidAndTitleCache> map = new HashMap<>();
	    resultByCode.forEach(e-> map.put(e.getId(), e));
	    result.addAll(resultByCode);
	    resultByTitleCache.stream().filter(r -> map.get(r.getId()) == null).forEach(r -> result.add(r));

        return new ArrayList<UuidAndTitleCache<Collection>>(result);
    }
}
