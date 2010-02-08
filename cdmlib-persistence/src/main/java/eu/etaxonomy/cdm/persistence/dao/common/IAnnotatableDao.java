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
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author n.hoffmann
 * @created 24.09.2008
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
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
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
}
