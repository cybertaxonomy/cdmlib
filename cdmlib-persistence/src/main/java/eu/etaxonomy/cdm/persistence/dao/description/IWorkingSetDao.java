package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IWorkingSetDao extends IAnnotatableDao<WorkingSet> {
    public Map<DescriptionBase,Set<DescriptionElementBase>> getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
}
