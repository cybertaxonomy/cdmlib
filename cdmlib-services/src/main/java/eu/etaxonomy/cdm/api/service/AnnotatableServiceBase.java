package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;

public abstract class AnnotatableServiceBase<T extends AnnotatableEntity,DAO extends IAnnotatableDao<T>> extends ServiceBase<T, DAO>
		implements IAnnotatableService<T> {
	
	public Pager<Annotation> getAnnotations(T annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber) {
		Integer numberOfResults = dao.countAnnotations(annotatedObj, status);
		
		List<Annotation> results = new ArrayList<Annotation>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getAnnotations(annotatedObj, status, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<Annotation>(pageNumber, numberOfResults, pageSize, results);
	}
}
