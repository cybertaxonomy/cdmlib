package eu.etaxonomy.cdm.api.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.dto.RowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.SpecimenRowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonRowWrapperDTO;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitorThread;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptiveDataSetDao;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = false)
public class DescriptiveDataSetService
        extends IdentifiableServiceBase<DescriptiveDataSet, IDescriptiveDataSetDao>
        implements IDescriptiveDataSetService {

    private static Logger logger = Logger.getLogger(DescriptiveDataSetService.class);

    @Autowired
    private IOccurrenceService occurrenceService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

    @Autowired
    private IProgressMonitorService progressMonitorService;

	@Override
	@Autowired
	protected void setDao(IDescriptiveDataSetDao dao) {
		this.dao = dao;
	}

	@Override
    public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(DescriptiveDataSet descriptiveDataSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,
			List<String> propertyPaths) {
		return dao.getDescriptionElements(descriptiveDataSet, features, pageSize, pageNumber, propertyPaths);
	}

	@Override
	public <T extends DescriptionElementBase> Map<UuidAndTitleCache, Map<UUID, Set<T>>> getTaxonFeatureDescriptionElementMap(
			Class<T> clazz, UUID descriptiveDataSetUuid, DescriptiveSystemRole role) {
		return dao.getTaxonFeatureDescriptionElementMap(clazz, descriptiveDataSetUuid, role);
	}

	@Override
    public List<UuidAndTitleCache<DescriptiveDataSet>> getDescriptiveDataSetUuidAndTitleCache(Integer limitOfInitialElements, String pattern) {
        return dao.getDescriptiveDataSetUuidAndTitleCache( limitOfInitialElements, pattern);
    }

	@Override
	public ArrayList<RowWrapperDTO> getRowWrapper(DescriptiveDataSet descriptiveDataSet, IProgressMonitor monitor) {
	    monitor.beginTask("Load row wrapper", descriptiveDataSet.getDescriptions().size());
	    ArrayList<RowWrapperDTO> wrappers = new ArrayList<>();
	    Set<DescriptionBase> descriptions = descriptiveDataSet.getDescriptions();
	    for (DescriptionBase description : descriptions) {
            if(monitor.isCanceled()){
                return new ArrayList<>();
            }
            RowWrapperDTO rowWrapper = null;
            if(HibernateProxyHelper.isInstanceOf(description, TaxonDescription.class)){
                rowWrapper = createTaxonRowWrapper(description.getUuid(), descriptiveDataSet.getUuid());
            }
            else if (HibernateProxyHelper.isInstanceOf(description, SpecimenDescription.class)){
                rowWrapper = createSpecimenRowWrapper(HibernateProxyHelper.deproxy(description, SpecimenDescription.class), descriptiveDataSet);
            }
            if(rowWrapper!=null){
                wrappers.add(rowWrapper);
            }
            monitor.worked(1);
        }
	    return wrappers;
	}

    @Override
    public Collection<SpecimenNodeWrapper> loadSpecimens(DescriptiveDataSet descriptiveDataSet){
        List<UUID> filteredNodes = findFilteredTaxonNodes(descriptiveDataSet);
        return occurrenceService.listUuidAndTitleCacheByAssociatedTaxon(filteredNodes, null, null);
    }

    @Override
    public List<UUID> findFilteredTaxonNodes(DescriptiveDataSet descriptiveDataSet){
        TaxonNodeFilter filter = TaxonNodeFilter.NewRankInstance(descriptiveDataSet.getMinRank(), descriptiveDataSet.getMaxRank());
        descriptiveDataSet.getGeoFilter().forEach(area -> filter.orArea(area.getUuid()));
        descriptiveDataSet.getTaxonSubtreeFilter().forEach(node -> filter.orSubtree(node));
        filter.setIncludeUnpublished(true);

        return taxonNodeService.uuidList(filter);
    }

    @Override
    public List<TaxonNode> loadFilteredTaxonNodes(DescriptiveDataSet descriptiveDataSet, List<String> propertyPaths){
        return taxonNodeService.load(findFilteredTaxonNodes(descriptiveDataSet), propertyPaths);
    }

    private TaxonNode findTaxonNodeForDescription(TaxonNode taxonNode, SpecimenOrObservationBase specimen){
        Collection<SpecimenNodeWrapper> nodeWrapper = occurrenceService.listUuidAndTitleCacheByAssociatedTaxon(Arrays.asList(taxonNode.getUuid()), null, null);
        for (SpecimenNodeWrapper specimenNodeWrapper : nodeWrapper) {
            if(specimenNodeWrapper.getUuidAndTitleCache().getId().equals(specimen.getId())){
                return taxonNode;
            }
        }
        return null;
    }

    @Override
    public TaxonRowWrapperDTO createTaxonRowWrapper(UUID taxonDescriptionUuid, UUID descriptiveDataSetUuid) {
        TaxonNode taxonNode = null;
        Classification classification = null;
        TaxonDescription description = (TaxonDescription) descriptionService.load(taxonDescriptionUuid,
                Arrays.asList("taxon", "descriptionElements", "descriptionElements.feature"));
        DescriptiveDataSet descriptiveDataSet = load(descriptiveDataSetUuid);
        Optional<TaxonNode> first = descriptiveDataSet.getTaxonSubtreeFilter().stream()
                .filter(node->node.getClassification()!=null).findFirst();
        Optional<Classification> classificationOptional = first.map(node->node.getClassification());
        if(classificationOptional.isPresent()){
            classification = classificationOptional.get();
            Taxon taxon = (Taxon) taxonService.load(description.getTaxon().getId(), Arrays.asList("taxonNodes", "taxonNodes.classification"));
            taxonNode = taxon.getTaxonNode(classification);
        }
        return new TaxonRowWrapperDTO(description, taxonNode);
    }

    @Override
    public SpecimenRowWrapperDTO createSpecimenRowWrapper(SpecimenDescription description, DescriptiveDataSet descriptiveDataSet){
	    SpecimenOrObservationBase specimen = description.getDescribedSpecimenOrObservation();
	    TaxonNode taxonNode = null;
        FieldUnit fieldUnit = null;
        String identifier = null;
        NamedArea country = null;
        //supplemental information
        //get taxon node
        Set<TaxonNode> taxonSubtreeFilter = descriptiveDataSet.getTaxonSubtreeFilter();
        for (TaxonNode node : taxonSubtreeFilter) {
            //check for node
            List<String> taxonNodePropertyPath = Arrays.asList("taxon", "taxon.descriptions", "taxon.descriptions.markers");
            node = taxonNodeService.load(node.getId(), taxonNodePropertyPath);
            taxonNode = findTaxonNodeForDescription(node, specimen);
            if(taxonNode!=null){
                break;
            }
            else{
                //check for child nodes
                List<TaxonNode> allChildren = taxonNodeService.loadChildNodesOfTaxonNode(node, taxonNodePropertyPath, true, true, null);
                for (TaxonNode child : allChildren) {
                    taxonNode = findTaxonNodeForDescription(child, specimen);
                    if(taxonNode!=null){
                        break;
                    }
                }
            }
        }
        if(taxonNode==null){
            return null;
        }
        //taxon node was found

        //get field unit
        Collection<FieldUnit> fieldUnits = occurrenceService.findFieldUnits(specimen.getUuid(),
                Arrays.asList(new String[]{
                        "gatheringEvent",
                        "gatheringEvent.country"
                }));
        if(fieldUnits.size()!=1){
            logger.error("More than one or no field unit found for specimen"); //$NON-NLS-1$
            return null;
        }
        else{
            fieldUnit = fieldUnits.iterator().next();
        }
        //get identifier
        if(HibernateProxyHelper.isInstanceOf(specimen, DerivedUnit.class)){
            identifier = occurrenceService.getMostSignificantIdentifier(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class));
        }
        //get country
        if(fieldUnit!=null && fieldUnit.getGatheringEvent()!=null){
            country = fieldUnit.getGatheringEvent().getCountry();
        }
        //get default taxon description
        TaxonDescription defaultTaxonDescription = findDefaultTaxonDescription(descriptiveDataSet,
                taxonNode);
        TaxonRowWrapperDTO taxonRowWrapper = defaultTaxonDescription != null
                ? createTaxonRowWrapper(defaultTaxonDescription.getUuid(), descriptiveDataSet.getUuid()) : null;
        return new SpecimenRowWrapperDTO(description, taxonNode, fieldUnit, identifier, country, taxonRowWrapper);
	}

    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends DescriptiveDataSet> clazz, Integer stepSize,
            IIdentifiableEntityCacheStrategy<DescriptiveDataSet> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null) {
            clazz = DescriptiveDataSet.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }
  
    /**
     * Returns a {@link TaxonDescription} for a given taxon node with corresponding
     * features according to the {@link DescriptiveDataSet}.<br>
     * If a description is found that matches all features of the data set this description
     * will be returned.
     * @param descriptiveDataSetUuid the uuid of the dataset defining the features
     * @param taxonNodeUuid the uuid of the taxon node that links to the taxon
     * if none could be found
     * @return the found taxon description or <code>null</code>
     */
    private TaxonDescription findDefaultTaxonDescription(DescriptiveDataSet dataSet, TaxonNode taxonNode){
        Set<DescriptionBase> dataSetDescriptions = dataSet.getDescriptions();
        //filter out COMPUTED descriptions
        List<TaxonDescription> nonComputedDescriptions = taxonNode.getTaxon().getDescriptions().stream()
                .filter(desc -> desc.getMarkers().stream()
                        .noneMatch(marker -> marker.getMarkerType().equals(MarkerType.COMPUTED())))
                .collect(Collectors.toList());
        for (TaxonDescription taxonDescription : nonComputedDescriptions) {
            for (DescriptionBase description : dataSetDescriptions) {
                if(description.getUuid().equals(taxonDescription.getUuid())){
                    return HibernateProxyHelper.deproxy(descriptionService.load(taxonDescription.getUuid(),
                            Arrays.asList("taxon", "descriptionElements", "descriptionElements.feature")), TaxonDescription.class);
                }
            }
        }
            return null;
        }

    @Override
    @Transactional(readOnly=false)
    public UpdateResult aggregateTaxonDescription(UUID taxonNodeUuid, UUID descriptiveDataSetUuid,
            IRemotingProgressMonitor monitor){
        UpdateResult result = new UpdateResult();

        TaxonNode node = taxonNodeService.load(taxonNodeUuid);
        Taxon taxon = HibernateProxyHelper.deproxy(taxonService.load(node.getTaxon().getUuid()), Taxon.class);
        result.setCdmEntity(taxon);

        //get all "computed" descriptions from all sub nodes
        List<TaxonNode> childNodes = taxonNodeService.listChildrenOf(node, null, null, true, false, null);
        List<TaxonDescription> computedDescriptions = new ArrayList<>();

        childNodes.stream().map(childNode -> childNode.getTaxon())
                .forEach(childTaxon -> childTaxon.getDescriptions().stream()
                        // filter out non-computed descriptions
                        .filter(description -> description.getMarkers().stream()
                                .anyMatch(marker -> marker.getMarkerType().equals(MarkerType.COMPUTED())))
                        // add them to the list
                        .forEach(computedDescription -> computedDescriptions.add(computedDescription)));

        UpdateResult aggregateDescription = aggregateDescription(taxon, computedDescriptions,
                "[Taxon Descriptions]"+taxon.getTitleCache(), descriptiveDataSetUuid);
        result.includeResult(aggregateDescription);
        result.setCdmEntity(aggregateDescription.getCdmEntity());
        aggregateDescription.setCdmEntity(null);
        return result;
    }

    @Override
    @Transactional(readOnly=false)
    public UpdateResult aggregateDescription(UUID taxonUuid, List<UUID> descriptionUuids, String descriptionTitle
            , UUID descriptiveDataSetUuid) {
        UpdateResult result = new UpdateResult();

        TaxonBase taxonBase = taxonService.load(taxonUuid);
        if(!(taxonBase instanceof Taxon)){
            result.addException(new ClassCastException("The given taxonUUID does not belong to a taxon"));
            result.setError();
            return result;
        }
        Taxon taxon = (Taxon)taxonBase;

        List<DescriptionBase> descriptions = descriptionService.load(descriptionUuids, null);

        UpdateResult aggregateDescriptionResult = aggregateDescription(taxon, descriptions, descriptionTitle, descriptiveDataSetUuid);
        result.setCdmEntity(aggregateDescriptionResult.getCdmEntity());
        aggregateDescriptionResult.setCdmEntity(null);
        result.includeResult(aggregateDescriptionResult);
        return result;
    }

    @SuppressWarnings("unchecked")
    private UpdateResult aggregateDescription(Taxon taxon, List<? extends DescriptionBase> descriptions, String descriptionTitle
            , UUID descriptiveDataSetUuid) {
        UpdateResult result = new UpdateResult();
        Map<Character, List<DescriptionElementBase>> featureToElementMap = new HashMap<>();

        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        if(dataSet==null){
            result.addException(new IllegalArgumentException("Could not find data set for uuid "+descriptiveDataSetUuid));
            result.setAbort();
            return result;
        }

        //extract all character description elements
        descriptions.forEach(description->{
            description.getElements()
            .stream()
            //filter out elements that do not have a Characters as Feature
            .filter(element->HibernateProxyHelper.isInstanceOf(((DescriptionElementBase)element).getFeature(), Character.class))
            .forEach(ele->{
                DescriptionElementBase descriptionElement = (DescriptionElementBase)ele;
                List<DescriptionElementBase> list = featureToElementMap.get(descriptionElement.getFeature());
                if(list==null){
                    list = new ArrayList<>();
                }
                list.add(descriptionElement);
                featureToElementMap.put(HibernateProxyHelper.deproxy(descriptionElement.getFeature(), Character.class), list);
            });
        });

        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        description.setTitleCache("[Aggregation] "+descriptionTitle, true);
        description.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
        IdentifiableSource source = IdentifiableSource.NewInstance(OriginalSourceType.Aggregation);
        description.addSource(source);
        description.addDescriptiveDataSet(dataSet);

        featureToElementMap.forEach((feature, elements)->{
            //aggregate categorical data
            if(feature.isSupportsCategoricalData()){
                CategoricalData aggregate = CategoricalData.NewInstance(feature);
                elements.stream()
                .filter(element->element instanceof CategoricalData)
                .forEach(categoricalData->((CategoricalData)categoricalData).getStateData()
                        .forEach(stateData->aggregate.addStateData((StateData) stateData.clone())));
                description.addElement(aggregate);
            }
            //aggregate quantitative data
            else if(feature.isSupportsQuantitativeData()){
                QuantitativeData aggregate = QuantitativeData.NewInstance(feature);
                elements.stream()
                .filter(element->element instanceof QuantitativeData)
                .forEach(categoricalData->((QuantitativeData)categoricalData).getStatisticalValues()
                        .forEach(statisticalValue->aggregate.addStatisticalValue((StatisticalMeasurementValue) statisticalValue.clone())));
                description.addElement(aggregate);
            }
        });
        result.addUpdatedObject(taxon);
        result.setCdmEntity(description);
        return result;
    }

    @Override
    public TaxonRowWrapperDTO createTaxonDescription(UUID dataSetUuid, UUID taxonNodeUuid, MarkerType markerType, boolean markerFlag){
        DescriptiveDataSet dataSet = load(dataSetUuid);
        TaxonNode taxonNode = taxonNodeService.load(taxonNodeUuid, Arrays.asList("taxon"));
        TaxonDescription newTaxonDescription = TaxonDescription.NewInstance(taxonNode.getTaxon());
        String tag = "";
        if(markerFlag){
            if(markerType.equals(MarkerType.USE())){
                tag = "[Default]";
            }
            else if(markerType.equals(MarkerType.IN_BIBLIOGRAPHY())){
                tag = "[Literature]";
            }
        }
        newTaxonDescription.setTitleCache(tag+" "+dataSet.getLabel()+": "+newTaxonDescription.generateTitle(), true); //$NON-NLS-2$
        if(markerType!=null){
            newTaxonDescription.addMarker(Marker.NewInstance(markerType, markerFlag));
        }
        dataSet.getDescriptiveSystem().getDistinctFeatures().forEach(wsFeature->{
            if(wsFeature.isSupportsCategoricalData()){
                newTaxonDescription.addElement(CategoricalData.NewInstance(wsFeature));
            }
            else if(wsFeature.isSupportsQuantitativeData()){
                newTaxonDescription.addElement(QuantitativeData.NewInstance(wsFeature));
            }
        });
        dataSet.addDescription(newTaxonDescription);

        return createTaxonRowWrapper(newTaxonDescription.getUuid(), dataSet.getUuid());
    }

    @Override
    public SpecimenDescription findSpecimenDescription(UUID descriptiveDataSetUuid, UUID specimenUuid, boolean addDatasetSource){
        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        SpecimenOrObservationBase specimen = occurrenceService.load(specimenUuid);

        Set<Feature> datasetFeatures = dataSet.getDescriptiveSystem().getDistinctFeatures();
        List<DescriptionElementBase> matchingDescriptionElements = new ArrayList<>();

        for (SpecimenDescription specimenDescription : (Set<SpecimenDescription>) specimen.getDescriptions()) {
            specimenDescription = (SpecimenDescription) descriptionService.load(specimenDescription.getUuid());

            //check if description is already added to data set
            if(dataSet.getDescriptions().contains(specimenDescription)){
                return specimenDescription;
            }

            //gather specimen description features and check for match with dataset features
            Set<Feature> specimenDescriptionFeatures = new HashSet<>();
            for (DescriptionElementBase specimenDescriptionElement : specimenDescription.getElements()) {
                Feature feature = specimenDescriptionElement.getFeature();
                specimenDescriptionFeatures.add(feature);
                if(datasetFeatures.contains(feature)){
                    matchingDescriptionElements.add(specimenDescriptionElement);
                }
            }
        }
        //Create new specimen description if description has not already been added to the dataset
        SpecimenDescription newDesription = SpecimenDescription.NewInstance(specimen);
        newDesription.setTitleCache("Dataset "+dataSet.getLabel()+": "+newDesription.generateTitle(), true); //$NON-NLS-2$

        //check for equals description element (same feature and same values)
        Map<Feature, List<DescriptionElementBase>> featureToElementMap = new HashMap<>();
        for(DescriptionElementBase element:matchingDescriptionElements){
            List<DescriptionElementBase> list = featureToElementMap.get(element.getFeature());
            if(list==null){
                list = new ArrayList<>();
            }
            list.add(element);
            featureToElementMap.put(element.getFeature(), list);
        }
        Set<DescriptionElementBase> descriptionElementsToClone = new HashSet<>();
        for(Feature feature:featureToElementMap.keySet()){
            List<DescriptionElementBase> elements = featureToElementMap.get(feature);
            //no duplicate description elements found for this feature
            if(elements.size()==1){
                descriptionElementsToClone.add(elements.get(0));
            }
            //duplicates found -> check if all are equal
            else{
                DescriptionElementBase match = null;
                for (DescriptionElementBase descriptionElementBase : elements) {
                    if(match==null){
                        match = descriptionElementBase;
                    }
                    else if(!new DescriptionElementCompareWrapper(match).equals(new DescriptionElementCompareWrapper(descriptionElementBase))){
                        match = null;
                        //TODO: propagate message
//                        MessagingUtils.informationDialog(Messages.CharacterMatrix_MULTIPLE_DATA,
//                                String.format(Messages.CharacterMatrix_MULTIPLE_DATA_MESSAGE, feature.getLabel()));
                        break;
                    }
                }
                if(match!=null){
                    descriptionElementsToClone.add(match);
                }
            }
        }
        //clone matching descriptionElements
        for (DescriptionElementBase descriptionElementBase : descriptionElementsToClone) {
            DescriptionElementBase clone;
            try {
                clone = descriptionElementBase.clone(newDesription);
                clone.getSources().forEach(source -> {
                    if(descriptionElementBase instanceof CategoricalData){
                        TextData label = new DefaultCategoricalDescriptionBuilder().build((CategoricalData) descriptionElementBase, null);
                        source.setOriginalNameString(label.getText(Language.DEFAULT()));
                    }
                    else if(descriptionElementBase instanceof QuantitativeData){
                        TextData label = new DefaultQuantitativeDescriptionBuilder().build((QuantitativeData) descriptionElementBase, null);
                        source.setOriginalNameString(label.getText(Language.DEFAULT()));
                    }
                });
            } catch (CloneNotSupportedException e) {
                //nothing
            }
        }

        //add all remaining description elements to the new description
        for(Feature wsFeature:datasetFeatures){
            boolean featureFound = false;
            for(DescriptionElementBase element:newDesription.getElements()){
                if(element.getFeature().equals(wsFeature)){
                    featureFound = true;
                    break;
                }
            }
            if(!featureFound){
                if(wsFeature.isSupportsCategoricalData()){
                    newDesription.addElement(CategoricalData.NewInstance(wsFeature));
                }
                else if(wsFeature.isSupportsQuantitativeData()){
                    newDesription.addElement(QuantitativeData.NewInstance(wsFeature));
                }
            }
        }
        //add sources of data set
        if(addDatasetSource){
            dataSet.getSources().forEach(source->{
                try {
                    newDesription.addSource((IdentifiableSource) source.clone());
                } catch (CloneNotSupportedException e) {
                    //nothing
                }
            });
        }
        return newDesription;

    }

    //TODO: this should either be solved in the model class itself
    //OR this should cover all possibilities including modifiers for example
    private class DescriptionElementCompareWrapper {

        private DescriptionElementBase element;
        private Set<UUID> stateUuids = new HashSet<>();
        private Set<Float> avgs = new HashSet<>();
        private Set<Float> exacts = new HashSet<>();
        private Set<Float> maxs = new HashSet<>();
        private Set<Float> mins = new HashSet<>();
        private Set<Float> sampleSizes = new HashSet<>();
        private Set<Float> standardDevs = new HashSet<>();
        private Set<Float> lowerBounds = new HashSet<>();
        private Set<Float> upperBounds = new HashSet<>();
        private Set<Float> variances = new HashSet<>();

        public DescriptionElementCompareWrapper(DescriptionElementBase element) {
            this.element = element;
            if(element.isInstanceOf(CategoricalData.class)){
                CategoricalData elementData = (CategoricalData)element;
                elementData.getStatesOnly().forEach(state->stateUuids.add(state.getUuid()));
            }
            else if(element.isInstanceOf(QuantitativeData.class)){
                QuantitativeData elementData = (QuantitativeData)element;
                elementData.getStatisticalValues().forEach(value->{
                    if(value.getType().equals(StatisticalMeasure.AVERAGE())){
                        avgs.add(value.getValue());
                    }
                    else if(value.getType().equals(StatisticalMeasure.EXACT_VALUE())){
                        exacts.add(value.getValue());

                    }
                    else if(value.getType().equals(StatisticalMeasure.MAX())){
                        maxs.add(value.getValue());
                    }
                    else if(value.getType().equals(StatisticalMeasure.MIN())){
                        mins.add(value.getValue());
                    }
                    else if(value.getType().equals(StatisticalMeasure.SAMPLE_SIZE())){
                        sampleSizes.add(value.getValue());

                    }
                    else if(value.getType().equals(StatisticalMeasure.STANDARD_DEVIATION())){
                        standardDevs.add(value.getValue());
                    }
                    else if(value.getType().equals(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY())){
                        lowerBounds.add(value.getValue());

                    }
                    else if(value.getType().equals(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY())){
                        upperBounds.add(value.getValue());
                    }
                    else if(value.getType().equals(StatisticalMeasure.VARIANCE())){
                        variances.add(value.getValue());
                    }
                });
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((avgs == null) ? 0 : avgs.hashCode());
            result = prime * result + ((element == null) ? 0 : element.hashCode());
            result = prime * result + ((exacts == null) ? 0 : exacts.hashCode());
            result = prime * result + ((lowerBounds == null) ? 0 : lowerBounds.hashCode());
            result = prime * result + ((maxs == null) ? 0 : maxs.hashCode());
            result = prime * result + ((mins == null) ? 0 : mins.hashCode());
            result = prime * result + ((sampleSizes == null) ? 0 : sampleSizes.hashCode());
            result = prime * result + ((standardDevs == null) ? 0 : standardDevs.hashCode());
            result = prime * result + ((stateUuids == null) ? 0 : stateUuids.hashCode());
            result = prime * result + ((upperBounds == null) ? 0 : upperBounds.hashCode());
            result = prime * result + ((variances == null) ? 0 : variances.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DescriptionElementCompareWrapper other = (DescriptionElementCompareWrapper) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (avgs == null) {
                if (other.avgs != null) {
                    return false;
                }
            } else if (!avgs.equals(other.avgs)) {
                return false;
            }
            if (element == null) {
                if (other.element != null) {
                    return false;
                }
            } else if (!element.equals(other.element)) {
                return false;
            }
            if (exacts == null) {
                if (other.exacts != null) {
                    return false;
                }
            } else if (!exacts.equals(other.exacts)) {
                return false;
            }
            if (lowerBounds == null) {
                if (other.lowerBounds != null) {
                    return false;
                }
            } else if (!lowerBounds.equals(other.lowerBounds)) {
                return false;
            }
            if (maxs == null) {
                if (other.maxs != null) {
                    return false;
                }
            } else if (!maxs.equals(other.maxs)) {
                return false;
            }
            if (mins == null) {
                if (other.mins != null) {
                    return false;
                }
            } else if (!mins.equals(other.mins)) {
                return false;
            }
            if (sampleSizes == null) {
                if (other.sampleSizes != null) {
                    return false;
                }
            } else if (!sampleSizes.equals(other.sampleSizes)) {
                return false;
            }
            if (standardDevs == null) {
                if (other.standardDevs != null) {
                    return false;
                }
            } else if (!standardDevs.equals(other.standardDevs)) {
                return false;
            }
            if (stateUuids == null) {
                if (other.stateUuids != null) {
                    return false;
                }
            } else if (!stateUuids.equals(other.stateUuids)) {
                return false;
            }
            if (upperBounds == null) {
                if (other.upperBounds != null) {
                    return false;
                }
            } else if (!upperBounds.equals(other.upperBounds)) {
                return false;
            }
            if (variances == null) {
                if (other.variances != null) {
                    return false;
                }
            } else if (!variances.equals(other.variances)) {
                return false;
            }
            return true;
        }

        private DescriptiveDataSetService getOuterType() {
            return DescriptiveDataSetService.this;
        }

    }

}
