/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author n.hoffmann
 * @since 24.09.2008
 * @version 1.0
 */
public interface IAnnotatableDao<T extends AnnotatableEntity> extends IVersionableDao<T>{

	/**
	 * Returns a List of Annotations belonging to the supplied AnnotatableEntity
	 *
	 * @param annotatableEntity the entity which is annotated
	 * @param status The status of the annotations (null to return annotations regardless of status)
	 * @param pageSize The maximum number of annotations returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of Annotation instances
	 */
    public List<Annotation> getAnnotations(T annotatableEntity, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
	 * Returns a count of Annotations belonging to the supplied AnnotatableEntity
	 *
	 * @param annotatableEntity the entity which is annotated
	 * @param status The status of the annotations (null to count all annotations regardless of status)
	 * @return a count of Annotation instances
	 */
	public int countAnnotations(T annotatableEntity, MarkerType status);

	/**
	 * Returns a count of Markers belonging to the supplied AnnotatableEntity
	 *
	 * @param annotatableEntity the entity which is marked
	 * @param technical The type of MarkerTypes to consider (null to count all markers, regardless of whether the makerType is technical or not)
	 * @return a count of Marker instances
	 */
	public int countMarkers(T annotatableEntity, Boolean technical);

	/**
	 *
	 * @param annotatableEntity the entity which is marked
	 * @param technical The type of MarkerTypes to consider (null to count all markers, regardless of whether the makerType is technical or not)
	 * @param pageSize The maximum number of markers returned (can be null for all markers)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of Marker instances
	 */
	public List<Marker> getMarkers(T annotatableEntity, Boolean technical, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
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


}
