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

import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class AnnotatableServiceBase<T extends AnnotatableEntity,DAO extends IAnnotatableDao<T>> extends VersionableServiceBase<T, DAO>
		implements IAnnotatableService<T> {
	@Override
    @Transactional(readOnly = true)
	public Pager<Annotation> getAnnotations(T annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		Integer numberOfResults = dao.countAnnotations(annotatedObj, status);

		List<Annotation> results = new ArrayList<Annotation>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getAnnotations(annotatedObj, status, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<Annotation>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
    @Transactional(readOnly = true)
    public Pager<Marker> getMarkers(T annotatableEntity, Boolean technical, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countMarkers(annotatableEntity, technical);

		List<Marker> results = new ArrayList<Marker>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMarkers(annotatableEntity, technical, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<Marker>(pageNumber, numberOfResults, pageSize, results);
    }


	@Override
    @Transactional(readOnly = true)
	public List<Object[]> groupMarkers(Class<? extends T> clazz, Boolean technical, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		return dao.groupMarkers(clazz, technical, pageSize, pageNumber, propertyPaths);
	}

	@Override
    @Transactional(readOnly = true)
	public int countMarkers(Class<? extends T> clazz, Boolean technical) {
		return dao.countMarkers(clazz, technical);
	}


}
