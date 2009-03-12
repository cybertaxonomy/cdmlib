package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;

public interface IAnnotationService extends IAnnotatableService<Annotation> {
	
	public int count(Person commentator, MarkerType status);
    
    public Pager<Annotation> list(Person commentator, MarkerType status, Integer pageSize, Integer pageNumber);

}
