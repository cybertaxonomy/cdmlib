package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotationDaoImpl;

@Service
@Transactional
public class AnnotationService extends AnnotatableServiceBase<Annotation, AnnotationDaoImpl> implements
		IAnnotationService {

	@Autowired
	protected void setDao(AnnotationDaoImpl dao) {
		this.dao = dao;
	}

	public int count(Person commentator, MarkerType status) {
		return dao.count(commentator, status);
	}

	public Pager<Annotation> list(Person commentator, MarkerType status,Integer pageSize, Integer pageNumber) {
		Integer numberOfResults = dao.count(commentator, status);
		
		List<Annotation> results = new ArrayList<Annotation>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.list(commentator, status, pageSize, pageNumber);
		}
		
		return new DefaultPagerImpl<Annotation>(pageNumber, numberOfResults, pageSize, results);
	}
}
