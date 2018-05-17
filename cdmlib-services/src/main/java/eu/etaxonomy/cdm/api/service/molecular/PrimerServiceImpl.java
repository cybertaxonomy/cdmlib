/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.molecular;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.PreferenceServiceImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.persistence.dao.molecular.IPrimerDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author pplitzner
 * @since 11.03.2014
 *
 */
@Service
@Transactional(readOnly = true)
public class PrimerServiceImpl extends AnnotatableServiceBase<Primer, IPrimerDao> implements IPrimerService{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PreferenceServiceImpl.class);

    @Override
    public List<UuidAndTitleCache<Primer>> getPrimerUuidAndTitleCache() {
        return dao.getPrimerUuidAndTitleCache();
    }
    @Override
    public List<UuidAndTitleCache<Primer>> getPrimerUuidAndTitleCache(Integer limitOfInitialElements, String pattern) {
        return dao.getPrimerUuidAndTitleCache( limitOfInitialElements, pattern);
    }
    @Override
    @Autowired
    protected void setDao(IPrimerDao dao) {
        this.dao = dao;
    }

    @Override
    public Pager<Primer> findByLabel(String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        long numberOfResults = dao.countByTitle(queryString, matchmode, criteria);

        List<Primer> results = new ArrayList<Primer>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)) {
               results = dao.findByTitle(queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
        }

         return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }
}
