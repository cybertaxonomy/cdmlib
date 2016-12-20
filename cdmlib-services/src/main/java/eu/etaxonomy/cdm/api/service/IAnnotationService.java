/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IAnnotationService extends IAnnotatableService<Annotation> {
	/**
	 * return a count of the number of annotations made by this person, optionally filtered by the status of those annotations
	 * 
	 * @param commentator the person who created those annotations
	 * @param status the status of those annotations (can be null)
	 * @return an integer
	 */
	public int count(Person commentator, MarkerType status);
    
	/**
	 * 
	 * @param commentator the person who created those annotations
	 * @param status the status of those annotations (can be null)
	 * @param pageSize The maximum number of annotations returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
	 * @return a paged list of Annotation instances
	 */
    public Pager<Annotation> list(Person commentator, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
    
    /**
	 * return a count of the number of annotations created by this user, optionally filtered by the status of those annotations
	 * 
	 * @param creator the user who created those annotations
	 * @param status the status of those annotations (can be null)
	 * @return an integer
	 */
	public int count(User creator, MarkerType status);
    
	/**
	 * 
	 * @param creator the user who created those annotations
	 * @param status the status of those annotations (can be null)
	 * @param pageSize The maximum number of annotations returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
	 * @return a paged list of Annotation instances
	 */
    public Pager<Annotation> list(User creator, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

}
