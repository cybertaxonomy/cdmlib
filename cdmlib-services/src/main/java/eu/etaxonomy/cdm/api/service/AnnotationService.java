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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotationDaoImpl;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional(readOnly = true)
public class AnnotationService extends AnnotatableServiceBase<Annotation, AnnotationDaoImpl> implements
		IAnnotationService {

	@Override
    @Autowired
	protected void setDao(AnnotationDaoImpl dao) {
		this.dao = dao;
	}

	@Override
    public long count(Person commentator, MarkerType status) {
		return dao.count(commentator, status);
	}

	@Override
    public Pager<Annotation> list(Person commentator, MarkerType status,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		Long numberOfResults = dao.count(commentator, status);

		List<Annotation> results = new ArrayList<Annotation>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.list(commentator, status, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<Annotation>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
    public long count(User creator, MarkerType status) {
		return dao.count(creator, status);
	}

	@Override
    public Pager<Annotation> list(User creator, MarkerType status,	Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
			List<String> propertyPaths) {
        long numberOfResults = dao.count(creator, status);

		List<Annotation> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.list(creator, status, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<Annotation>(pageNumber, numberOfResults, pageSize, results);
	}
}
