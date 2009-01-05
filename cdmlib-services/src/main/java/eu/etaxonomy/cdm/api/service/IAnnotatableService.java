package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;

public interface IAnnotatableService<T extends AnnotatableEntity> extends IService<T> {
	
	/**
	 * Return a Pager containing Annotation entities belonging to the object supplied, optionally
	 * filtered by MarkerType
	 * 
	 * @param annotatedObj The object that "owns" the annotations returned
	 * @param status Only return annotations which are marked with a Marker of this type (can be null to return all annotations)
	 * @param pageSize The maximum number of terms returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of Annotation entities
	 */
	public Pager<Annotation> getAnnotations(T annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber);
}
