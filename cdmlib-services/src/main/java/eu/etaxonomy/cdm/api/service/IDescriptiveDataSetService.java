package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.DeleteDescriptiveDataSetConfigurator;
import eu.etaxonomy.cdm.api.service.config.RemoveDescriptionsFromDescriptiveDataSetConfigurator;
import eu.etaxonomy.cdm.api.service.dto.RowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.SpecimenRowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonRowWrapperDTO;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;


public interface IDescriptiveDataSetService extends IIdentifiableEntityService<DescriptiveDataSet> {
	/**
	 * Returns a Map of descriptions each with the description elements that match
	 * the supplied features (or all description elements if no features are supplied)
	 *
	 * @param descriptiveDataSet the data set which the descriptions belong to
	 * @param features restrict the returned description elements to those which have features in this set
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions that belong to the data set)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based,
	 *                   can be null, equivalent of starting at the beginning of the recordset). Descriptions are sorted by titleCache
	 * @param propertyPaths properties to be initialized (applied to the descriptionElements)
	 * @return
	 */
	public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(DescriptiveDataSet descriptiveDataSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,	List<String> propertyPaths);

	public <T extends DescriptionElementBase> Map<UuidAndTitleCache, Map<UUID, Set<T>>> getTaxonFeatureDescriptionElementMap(Class<T> clazz, UUID descriptiveDataSetUuid, DescriptiveSystemRole role);

    /**
     * Returns a list of {@link UuidAndTitleCache} elements for all {@link DescriptiveDataSet}s in the data base
     * @param limitOfInitialElements
     * @param pattern
     * @return a list of UuidAndTitleCache element
     */
    public List<UuidAndTitleCache<DescriptiveDataSet>> getDescriptiveDataSetUuidAndTitleCache(Integer limitOfInitialElements, String pattern);

    /**
     * Returns a collection of {@link RowWrapperDTO} objects for the given {@link DescriptiveDataSet}.<br>
     * A RowWrapper represents on row in the character matrix.
     * @param descriptiveDataSetUuid the working set for which the row wrapper objects should be fetched
     * @param the progress monitor
     * @return a list of row wrapper objects
     */
    public ArrayList<RowWrapperDTO> getRowWrapper(UUID descriptiveDataSetUuid, IProgressMonitor monitor);

    /**
     * Loads all available specimens wrapped in a {@link SpecimenNodeWrapper} object for
     * a given {@link DescriptiveDataSet} according to the filters set in the working set
     * @param descriptiveDataSet the data set for which the specimens should be fetched
     * @return a collection of wrapper objects
     */
    public Collection<SpecimenNodeWrapper> loadSpecimens(DescriptiveDataSet descriptiveDataSet);

    /**
     * Lists all taxon nodes that match the filter set defined in the
     * {@link DescriptiveDataSet} given.
     * @param the data set which defined the taxon node filter
     * @return a list of {@link UUID}s from the filtered nodes
     *
     */
    public List<UUID> findFilteredTaxonNodes(DescriptiveDataSet descriptiveDataSet);

    /**
     * Creates a {@link SpecimenRowWrapperDTO} from the given SpecimenNodeWrapper.<br>
     * This service method is used when adding new specimen to the character matrix resp.
     * to the {@link DescriptiveDataSet}.
     * @param wrapper the specimen wrapper to use for creating the row wrapper
     * @param datasetUuid the target dataset
     * @return the result of the operation
     */
    public UpdateResult addRowWrapperToDataset(Collection<SpecimenRowWrapperDTO> wrapper, UUID datasetUuid);

    /**
     * Creates a specimen row wrapper object for the given description
     * @param description the specimen description for which the wrapper should be created
     * @param descriptiveDataSetUuid the data set it should be used in
     * @return the created row wrapper
     */
    public SpecimenRowWrapperDTO createSpecimenRowWrapper(SpecimenDescription description, UUID descriptiveDataSetUuid);

    /**
     * Creates a specimen row wrapper object for the given description
     * @param uuid of the specimen for which the wrapper should be created
     * @param descriptiveDataSetUuid the data set it should be used in
     * @return the created row wrapper
     */
    public SpecimenRowWrapperDTO createSpecimenRowWrapper(UUID specimenUuid, UUID taxonNodeUuid, UUID descriptiveDataSetUuid);

    /**
     * Returns a {@link TaxonDescription} for a given taxon node with corresponding
     * features according to the {@link DescriptiveDataSet} and the having the given {@link DescriptionType}.<br>
     * @param descriptiveDataSetUuid the uuid of the dataset defining the features
     * @param taxonNodeUuid the uuid of the taxon node that links to the taxon
     * @param descriptionType the {@link DescriptionType} that the description should have
     * @return the found taxon description or <code>null</code>
     */
    public TaxonDescription findTaxonDescriptionByDescriptionType(UUID dataSetUuid, UUID taxonNodeUuid, DescriptionType descriptionType);

    /**
     * Creates a taxon row wrapper object for the given description
     * @param taxonDescriptionUuid the taxon description for which the wrapper should be created
     * @param descriptiveDataSet the data set it should be used in
     * @return the created row wrapper
     */
    public TaxonRowWrapperDTO createTaxonRowWrapper(UUID taxonDescriptionUuid, UUID descriptiveDataSetUuid);

    /**
     * Returns a {@link SpecimenDescription} for a given specimen with corresponding
     * features according to the {@link DescriptiveDataSet}.<br>
     * If a description is found that matches all features of the data set this description
     * will be returned. A new one will be created otherwise.
     * @param descriptiveDataSetUuid the uuid of the dataset defining the features
     * @param specimenUuid the uuid of the specimen
     * @param addDatasetSource if <code>true</code> the source(s) of the descriptive dataset
     * will be added to the description <b>if</b> a new one is created
     * @return either the found specimen description or a newly created one
     */
    public SpecimenDescription findSpecimenDescription(UUID descriptiveDataSetUuid, SpecimenOrObservationBase specimenUuid, boolean addDatasetSource);

    /**
     * Returns all states for all supportedCategoricalEnumeration of this categorical feature
     * @param featureUuid the feature which has to support categorical data
     * @return list of all supported states
     */
    public List<TermDto> getSupportedStatesForFeature(UUID featureUuid);

    /**
     * Creates a new taxon description with the features defined in the dataset for the
     * taxon associated with the given taxon node.
     * @param descriptiveDataSetUuid the uuid of the dataset defining the features
     * @param taxonNodeUuid the uuid of the taxon node that links to the taxon
     * @param descriptionType the type of the description
     * @return a taxon row wrapper of the description with the features defined in the data set
     */
    public TaxonRowWrapperDTO createTaxonDescription(UUID dataSetUuid, UUID taxonNodeUuid, DescriptionType descriptionType);

    /**
     * Loads all taxon nodes that match the filter set defined in the
     * {@link DescriptiveDataSet} given.
     * @param the data set which defined the taxon node filter
     * @return a list of {@link TaxonNode}s from the filtered nodes
     *
     */
    public List<TaxonNode> loadFilteredTaxonNodes(DescriptiveDataSet descriptiveDataSet, List<String> propertyPaths);

    /**
     * Generates a {@link PolytomousKey} for the given {@link DescriptiveDataSet} and sets
     * the given taxon as the taxonomic scope
     * @param datasetUuid the data set
     * @param taxonUuid the taxonomic scope of the key
     * @return the uuid of the monitor
     */
    UpdateResult generatePolytomousKey(UUID descriptiveDataSetUuid, UUID taxonUuid);

    /**
     * Returns the first {@link TaxonDescription} with {@link DescriptionType#DEFAULT_VALUES_FOR_AGGREGATION}
     * found in the taxon node hierarchy of the associated taxon
     * @param specimenDescriptionUuid the specimen description
     * @param dataSetUuid the data set
     * @return the first found default description or <code>null</code>
     */
    public TaxonDescription findDefaultDescription(UUID specimenDescriptionUuid, UUID dataSetUuid);

    public Collection<SpecimenNodeWrapper> loadSpecimens(UUID descriptiveDataSetUuid);

    /**
     * @param datasetUuid
     * @param monitor
     * @return
     */
    DeleteResult delete(UUID datasetUuid, DeleteDescriptiveDataSetConfigurator config, IProgressMonitor monitor);

    /**
     * Removes the descriptions specified by the given {@link UUID} from the given {@link DescriptiveDataSet}.
     * @param descriptionUuid
     * @param descriptiveDataSetUuid
     * @return
     */
    DeleteResult removeDescriptions(List<UUID> descriptionUuids, UUID descriptiveDataSetUuid, RemoveDescriptionsFromDescriptiveDataSetConfigurator config);

    /**
     * Removes the description specified by the given {@link UUID} from the given {@link DescriptiveDataSet}.
     * @param descriptionUuid
     * @param descriptiveDataSetUuid
     * @param config
     * @return
     */
    DeleteResult removeDescription(UUID descriptionUuid, UUID descriptiveDataSetUuid,
            RemoveDescriptionsFromDescriptiveDataSetConfigurator config);

}
