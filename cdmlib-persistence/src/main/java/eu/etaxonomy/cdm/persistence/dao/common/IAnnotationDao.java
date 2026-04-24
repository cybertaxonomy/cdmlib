/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @since 12.09.2008
 */
public interface IAnnotationDao extends IVersionableDao<Annotation> {

	/**
	 * Returns a count of Annotations, optionally filtered by commentator
	 *
	 * @param commentator The person who made the annotation (null to count all annotations, regardless of who made the comment)
	 * @return a count of Annotation instances
	 */
    public long count(Person commentator);

    /**
	 * Returns a List of Annotations, optionally filtered by commentator and status
	 *
	 * @param commentator The person who made the annotation (null to list all annotations, regardless of who made the comment)
	 * @param pageSize The maximum number of annotations returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of Annotation instances
	 */
	public List<Annotation> list(Person commentator, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a count of Annotations, optionally filtered by creator
	 *
	 * @param commentator The user that created the annotation (null to count all annotations, regardless of who made the comment)
	 * @return a count of Annotation instances
	 */
    public long count(User creator);

    /**
	 * Returns a List of Annotations, optionally filtered by creator
	 *
	 * @param commentator The user that created the annotation (null to list all annotations, regardless of who made the comment)
	 * @param pageSize The maximum number of annotations returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of Annotation instances
	 */
	public List<Annotation> list(User creator, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
}
