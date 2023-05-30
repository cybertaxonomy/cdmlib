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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.term.ITermCollectionDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.mueller
 * @date 30.05.2023
 */
@Service
@Transactional(readOnly = false)
public class TermCollectionServiceImpl
            extends IdentifiableServiceBase<TermCollection, ITermCollectionDao>
            implements ITermCollectionService {

    @Override
    @Autowired
    protected void setDao(ITermCollectionDao dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends TermCollection> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TermCollection> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TermTree.class;
        }
        return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
    }


    /**
     * Returns the term collection specified by the given <code>uuid</code>.
     *
     * @see eu.etaxonomy.cdm.api.service.ServiceBase#load(java.util.UUID, java.util.List)
     */
    @Override
    public TermCollection load(UUID uuid, List<String> propertyPaths) {
        return super.load(uuid, propertyPaths);
    }


    @Override
    public <S extends TermCollection> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(Class<S> clazz, TermType termType, Integer limit,
            String pattern) {
        return dao.getUuidAndTitleCacheByTermType(clazz, termType, limit, pattern);
    }

    @Override
    public List<TermCollection> list(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        return dao.list(null, buildTermTypeFilterRestrictions(termType), limit, start, orderHints, propertyPaths);
    }

    @Override
    public List<TermCollection> list(Set<TermType> termTypes, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        List<TermCollection> result = new ArrayList<>();
        for (TermType termType : termTypes) {
            result.addAll(list(termType, limit, start, orderHints, propertyPaths));
        }
        return result;
    }

    @Override
    public Pager<TermCollection> page(TermType termType, Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {

        return page(null, buildTermTypeFilterRestrictions(termType), pageSize, pageIndex, orderHints, propertyPaths);
    }

    private List<Restriction<?>> buildTermTypeFilterRestrictions(TermType termType) {
        List<Restriction<?>> filterRestrictions = null;
        if(termType != null){
           Set<TermType> termTypes = termType.getGeneralizationOf(true);
           termTypes.add(termType);
           filterRestrictions = Arrays.asList(new Restriction<>("termType", null, termTypes.toArray()));
        }
        return filterRestrictions;
    }

}
