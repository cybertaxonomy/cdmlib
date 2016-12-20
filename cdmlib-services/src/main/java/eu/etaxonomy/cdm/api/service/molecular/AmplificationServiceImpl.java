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
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.persistence.dao.molecular.IAmplificationDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
@Service
@Transactional(readOnly = true)
public class AmplificationServiceImpl extends AnnotatableServiceBase<Amplification, IAmplificationDao> implements IAmplificationService{
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AmplificationServiceImpl.class);

    @Override
    @Autowired
    protected void setDao(IAmplificationDao dao) {
        this.dao = dao;
    }

    @Override
    public List<UuidAndTitleCache<Amplification>> getAmplificationUuidAndLabelCache() {
        return dao.getAmplificationUuidAndLabelCache();
    }

    @Override
    public Pager<Amplification> findByLabelCache(String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        long numberOfResults = dao.countByTitle(queryString, matchmode, criteria);

        List<Amplification> results = new ArrayList<Amplification>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)) {
               results = dao.findByTitle(queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
        }

         return new DefaultPagerImpl<Amplification>(pageNumber, numberOfResults, pageSize, results);
    }

}
