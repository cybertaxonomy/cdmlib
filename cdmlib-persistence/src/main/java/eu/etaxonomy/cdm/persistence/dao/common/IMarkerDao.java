/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IMarkerDao extends IVersionableDao<Marker> {

	/**
	 * Returns a count of markers which have the same type
	 * @param markerType The type of markerType
	 * @return a count of markers
	 */
	public long count(MarkerType markerType);

	/**
	 * Returns a list of markers which have the same type
	 *
	 * @param markerType The type of markerType
	 * @param pageSize The maximum number of markers returned (can be null for all markers)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return
	 */
	public List<Marker> list(MarkerType markerType, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

}
