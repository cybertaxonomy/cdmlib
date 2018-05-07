package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.dto.RowWrapperDTO;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;


public interface IWorkingSetService extends IIdentifiableEntityService<WorkingSet> {
	/**
	 * Returns a Map of descriptions each with the description elements that match
	 * the supplied features (or all description elements if no features are supplied)
	 *
	 * @param workingSet the working set which the descriptions belong to
	 * @param features restrict the returned description elements to those which have features in this set
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions that belong to the working set)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based,
	 *                   can be null, equivalent of starting at the beginning of the recordset). Descriptions are sorted by titleCache
	 * @param propertyPaths properties to be initialized (applied to the descriptionElements)
	 * @return
	 */
	public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,	List<String> propertyPaths);

	public <T extends DescriptionElementBase> Map<UuidAndTitleCache, Map<UUID, Set<T>>> getTaxonFeatureDescriptionElementMap(Class<T> clazz, UUID workingSetUuid, DescriptiveSystemRole role);

    /**
     * @param limitOfInitialElements
     * @param pattern
     * @return
     */
    public List<UuidAndTitleCache<WorkingSet>> getWorkingSetUuidAndTitleCache(Integer limitOfInitialElements, String pattern);

    /**
     * Returns a collection of {@link RowWrapperDTO} objects for the given {@link WorkingSet}.<br>
     * A RowWrapper represents on row in the character matrix.
     * @param workingSet the working set for which the row wrapper objects should be fetched
     * @param the progress monitor
     * @return a list of row wrapper objects
     */
    public Collection<RowWrapperDTO> getRowWrapper(WorkingSet workingSet, IProgressMonitor monitor);

    /**
     * Monitored invocation of {@link IWorkingSetService#getRowWrapper(WorkingSet, IProgressMonitor)}
     * @param workingSet the working set for which getRowWrapper() is invoked
     * @return the uuid of the monitor
     */
    public UUID monitGetRowWrapper(WorkingSet workingSet);

    /**
     * Loads all avaliable specimens wrapped in a {@link SpecimenNodeWrapper} object for
     * a given {@link WorkingSet} according to the filters set in the working set
     * @param workingSet the working set for which the specimens should be fetched
     * @return a collection of wrapper objects
     */
    public Collection<SpecimenNodeWrapper> loadSpecimens(WorkingSet workingSet);

    /**
     * Creates a row wrapper object for the given specimen
     * @param specimen
     * @param workingSet
     * @return
     */
    public RowWrapperDTO createRowWrapper(SpecimenOrObservationBase specimen, WorkingSet workingSet);

    /**
     * Creates a row wrapper object for the given description
     * @param specimen
     * @param description
     * @param workingSet
     * @return
     */
    public RowWrapperDTO createRowWrapper(DescriptionBase description, WorkingSet workingSet);

}
