/**
* Copyright (C) 2017 EDIT
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

import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.reference.IReference;
import eu.etaxonomy.cdm.persistence.dao.name.IRegistrationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 *
 */
public class RegistrationServiceImpl extends AnnotatableServiceBase<Registration, IRegistrationDao> implements IRegistrationService {

    /**
     * {@inheritDoc}
     */
    @Autowired
    @Override
    protected void setDao(IRegistrationDao dao) {
        this.dao = dao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pager<Registration> page(Integer pageSize, Integer pageIndex, IReference reference,
            Collection<RegistrationStatus> includeStatus, List<OrderHint> orderHints, List<String> propertyPaths) {

        long numberOfResults = dao.count(reference);

        List<Registration> results = new ArrayList<Registration>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageIndex, pageSize)) {
            Integer limit = PagerUtils.limitFor(pageSize);
            Integer start = PagerUtils.startFor(pageSize, pageIndex);
            results = dao.list(limit, start, reference, orderHints, propertyPaths);
        }

         return new DefaultPagerImpl<Registration>(pageIndex, numberOfResults, pageSize, results);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pager<Registration> page(Integer pageSize, Integer pageIndex, User submitter, Collection<RegistrationStatus> includeStatus,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        long numberOfResults = dao.count(Registration.class, "submitter", submitter, null);

        List<Registration> results = new ArrayList<Registration>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageIndex, pageSize)) {
            Integer limit = PagerUtils.limitFor(pageSize);
            Integer start = PagerUtils.startFor(pageSize, pageIndex);
            results = dao.list(Registration.class, "submitter", submitter, null, limit, start, orderHints, propertyPaths);
        }

         return new DefaultPagerImpl<Registration>(pageIndex, numberOfResults, pageSize, results);
    }


}
