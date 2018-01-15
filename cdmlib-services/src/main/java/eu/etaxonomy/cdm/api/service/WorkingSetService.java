package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.persistence.dao.description.IWorkingSetDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

@Service
@Transactional(readOnly = false)
public class WorkingSetService extends
        AnnotatableServiceBase<WorkingSet, IWorkingSetDao> implements IWorkingSetService {

    @Override
    @Autowired
    protected void setDao(IWorkingSetDao dao) {
        this.dao = dao;
    }

    @Override
    public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize, Integer pageNumber,
            List<String> propertyPaths) {
        return dao.getDescriptionElements(workingSet, features, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public <T extends DescriptionElementBase> Map<UuidAndTitleCache, Map<UUID, Set<T>>> getTaxonFeatureDescriptionElementMap(
            Class<T> clazz, UUID workingSetUuid, DescriptiveSystemRole role) {
        return dao.getTaxonFeatureDescriptionElementMap(clazz, workingSetUuid, role);
    }
}
