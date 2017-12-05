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
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IAnnotatableService<T extends AnnotatableEntity>
            extends IVersionableService<T> {

	/**
	 * Return a Pager containing Annotation entities belonging to the object supplied, optionally
	 * filtered by MarkerType
	 *
	 * @param annotatedObj The object that "owns" the annotations returned
	 * @param status Only return annotations which are marked with a Marker of this type (can be null to return all annotations)
	 * @param pageSize The maximum number of terms returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of Annotation entities
	 */
	public Pager<Annotation> getAnnotations(T annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a Pager containing Marker entities belonging to the object supplied, optionally filtered by
	 * whether they are technical or non-technical markers
	 *
	 * @param annotatableEntity the entity which is marked
	 * @param technical The type of MarkerTypes to consider (null to count all markers, regardless of whether the makerType is technical or not)
	 * @param pageSize The maximum number of markers returned (can be null for all markers)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of Marker instances
	 */
	public Pager<Marker> getMarkers(T annotatableEntity, Boolean technical, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a list of arrays representing counts of entities of type clazz, grouped by their markerTypes. The arrays have two elements.
	 * The first element is the MarkerType, initialized using the propertyPaths parameter. The second element is the count of all markers of Objects
	 * of type clazz with that MarkerType. The boolean technical can be used to choose only technical or only non-technical marker types. The list is sorted by
	 * titleCache of the markerType, in ascending order.
	 *
	 * @param clazz optionally restrict the markers to those belonging to this class
	 * @param technical The type of MarkerTypes to consider (null to count all markers, regardless of whether the makerType is technical or not)
	 * @param pageSize The maximum number of arrays returned (can be null for all arrays)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return
	 */
	public List<Object[]> groupMarkers(Class<? extends T> clazz, Boolean technical, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
	 * returns a count of all markers belonging to that clazz, optionally filtered to include only technical or only non-technical markers.
	 *
	 * @param clazz optionally restrict the markers to those belonging to this class
	 * @param technical The type of MarkerTypes to consider (null to count all markers, regardless of whether the makerType is technical or not)
	 * @return a count of markers
	 */
	public int countMarkers(Class<? extends T> clazz, Boolean technical);
	 /**
     * Like {@link #getUuidAndTitleCache(Integer, String)} but searching only on a subclass
     * of the type handled by the DAO.
     *
     * @param clazz the (sub)class
     * @param limit max number of results
     * @param pattern search pattern

     * @see #getUuidAndTitleCache(Integer, String)
     */
    public <S extends T> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz, Integer limit, String pattern);

    /**
     * Return a list of all uuids mapped to titleCache in the convenient <code>UuidAndTitleCache</code> object.
     * Retrieving this list is considered to be significantly faster than initializing the fully fledged business
     * objects. To be used in cases where you want to present large amount of data and provide details after
     * a selection has been made.
     *
     * @return a list of <code>UuidAndTitleCache</code> instances
     *
     * @see #getUuidAndTitleCache(Class, Integer, String)
     */
    public List<UuidAndTitleCache<T>> getUuidAndTitleCache(Integer limit, String pattern);

}
