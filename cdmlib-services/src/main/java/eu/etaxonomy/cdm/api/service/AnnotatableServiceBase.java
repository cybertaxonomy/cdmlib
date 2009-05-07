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
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Transactional(readOnly = true)
public abstract class AnnotatableServiceBase<T extends AnnotatableEntity,DAO extends IAnnotatableDao<T>> extends VersionableServiceBase<T, DAO>
		implements IAnnotatableService<T> {
	
	@Transactional
	public Pager<Annotation> getAnnotations(T annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		Integer numberOfResults = dao.countAnnotations(annotatedObj, status);
		
		List<Annotation> results = new ArrayList<Annotation>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getAnnotations(annotatedObj, status, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<Annotation>(pageNumber, numberOfResults, pageSize, results);
	}
}
