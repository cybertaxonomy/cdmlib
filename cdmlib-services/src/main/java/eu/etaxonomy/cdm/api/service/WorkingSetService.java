package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.persistence.dao.description.IWorkingSetDao;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class WorkingSetService extends
		AnnotatableServiceBase<WorkingSet, IWorkingSetDao> implements IWorkingSetService {

	@Override
	@Autowired
	protected void setDao(IWorkingSetDao dao) {
		this.dao = dao;
	}

	public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,
			List<String> propertyPaths) {
		return dao.getDescriptionElements(workingSet, features, pageSize, pageNumber, propertyPaths);
	}
}
