package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

public interface IDescriptiveDataSetDao extends IIdentifiableDao<DescriptiveDataSet> {

	public Map<DescriptionBase,Set<DescriptionElementBase>> getDescriptionElements(DescriptiveDataSet descriptiveDataSet, Set<Feature> features, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	public <T extends DescriptionElementBase> Map<UuidAndTitleCache, Map<UUID, Set<T>>> getTaxonFeatureDescriptionElementMap(Class<T> clazz, UUID descriptiveDataSetUuid, DescriptiveSystemRole role);

    /**
     * @param limitOfInitialElements
     * @param pattern
     * @return
     * @deprecated since DescriptiveDataSet is not IdentifiableEntity we probably
     *      can use standard {@link IdentifiableDaoBase#getUuidAndTitleCache()} now.
     */
	@Deprecated
    public List<UuidAndTitleCache<DescriptiveDataSet>> getDescriptiveDataSetUuidAndTitleCache(Integer limitOfInitialElements,
            String pattern);
}
