package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IMarkerService extends IVersionableService<Marker> {

	/**
	 * Returns a pager of markers which have the same type
	 *
	 * @param markerType The type of markerType
	 * @param pageSize The maximum number of markers returned (can be null for all markers)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return
	 */
	public Pager<Marker> page(MarkerType markerType, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 *
	 * @param creator the person who created those markers
	 * @param markerType the markerType of those markers (can be null)
	 * @param pageSize The maximum number of markers returned (can be null for all markers)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
	 * @return a paged list of Marker instances
	 */
    public Pager<Marker> list(User creator, MarkerType markerType, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     *
     * @param creator the person who created those markers
     * @param markerType the markerType of those markers (can be null)
     * @return
     */
    public long count(User creator, MarkerType markerType);

}
