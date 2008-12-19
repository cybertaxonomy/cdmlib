/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;

/**
 * @author a.babadshanjan
 * @created 12.09.2008
 */
public interface IAnnotationDao extends ILanguageStringBaseDao<Annotation> {
	
	/**
	 * Returns a count of Annotations created by the supplied commentator
	 * 
	 * @param commentator The person who made the annotation
	 * @param status The status of the annotations (null to count all annotations regardless of status)
	 * @return a count of Annotation instances
	 */
    public int count(Person commentator, MarkerType status);
	
    /**
	 * Returns a List of Annotations created by the supplied commentator
	 * 
	 * @param commentator The person who made the annotation
	 * @param status The status of the annotations (null to return annotations regardless of status)
	 * @param pageSize The maximum number of annotations returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of Annotation instances
	 */
	public List<Annotation> list(Person commentator, MarkerType status, Integer pageSize, Integer pageNumber);
}
