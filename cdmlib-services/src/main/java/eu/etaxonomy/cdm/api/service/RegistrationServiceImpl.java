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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.name.IRegistrationDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 *
 */
@Service
@Transactional(readOnly = true)
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
    public Pager<Registration> page(Optional<Reference> reference, Collection<RegistrationStatus> includedStatus,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths) {

        long numberOfResults = dao.count(reference, includedStatus);

        List<Registration> results = new ArrayList<>();
        Integer [] limitStart = AbstractPagerImpl.limitStartforRange(numberOfResults, pageIndex, pageSize);
        if(limitStart != null) {
            results = dao.list(reference, includedStatus, limitStart[0], limitStart[1], propertyPaths);
        }

        return new DefaultPagerImpl<>(pageIndex, numberOfResults, pageSize, results);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pager<Registration> page(User submitter, Collection<RegistrationStatus> includedStatus,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {

        List<Restriction<? extends Object>> restrictions = new ArrayList<>();
        if(submitter != null){
            restrictions.add(new Restriction<User>("submitter", MatchMode.EXACT, submitter));
        }
        if(includedStatus != null && !includedStatus.isEmpty()){
            restrictions.add(new Restriction<RegistrationStatus>("status", MatchMode.EXACT, includedStatus.toArray(new RegistrationStatus[includedStatus.size()])));
        }

        long numberOfResults = dao.count(Registration.class, restrictions);

        List<Registration> results = new ArrayList<Registration>();
        Integer [] limitStart = AbstractPagerImpl.limitStartforRange(numberOfResults, pageIndex, pageSize);
        if(limitStart != null) {
            results = dao.list(Registration.class, restrictions, limitStart[0], limitStart[1], orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageIndex, numberOfResults, pageSize, results);
    }


}
