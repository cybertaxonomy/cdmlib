package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.DescriptionAggregationConfiguration;
import eu.etaxonomy.cdm.api.service.config.IdentifiableServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.dto.RowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.SpecimenRowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonRowWrapperDTO;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptiveDataSetDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.generate.PolytomousKeyGenerator;
import eu.etaxonomy.cdm.strategy.generate.PolytomousKeyGeneratorConfigurator;

@Service
@Transactional(readOnly=true)
public class DescriptiveDataSetService
        extends IdentifiableServiceBase<DescriptiveDataSet, IDescriptiveDataSetDao>
        implements IDescriptiveDataSetService {

    private static Logger logger = Logger.getLogger(DescriptiveDataSetService.class);

    @Autowired
    private IOccurrenceService occurrenceService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IPolytomousKeyService polytomousKeyService;

    @Autowired
    private IDefinedTermDao termDao;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

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
	public ArrayList<RowWrapperDTO> getRowWrapper(UUID descriptiveDataSetUuid, IProgressMonitor monitor) {
	    DescriptiveDataSet descriptiveDataSet = load(descriptiveDataSetUuid);
	    monitor.beginTask("Load row wrapper", descriptiveDataSet.getDescriptions().size());
	    ArrayList<RowWrapperDTO> wrappers = new ArrayList<>();
	    Set<DescriptionBase> descriptions = descriptiveDataSet.getDescriptions();
	    for (DescriptionBase description : descriptions) {
            if(monitor.isCanceled()){
                return new ArrayList<>();
            }
            RowWrapperDTO rowWrapper = null;
            // only viable descriptions are aggregated, literature or default descriptions
            if(HibernateProxyHelper.isInstanceOf(description, TaxonDescription.class) &&
                    (description.getTypes().contains(DescriptionType.AGGREGATED)
                            || description.getTypes().contains(DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION)
                            || description.getTypes().contains(DescriptionType.SECONDARY_DATA)
                            )){
                rowWrapper = createTaxonRowWrapper(description.getUuid(), descriptiveDataSet.getUuid());
            }
            else if (HibernateProxyHelper.isInstanceOf(description, SpecimenDescription.class)&&
                    !description.getTypes().contains(DescriptionType.CLONE_FOR_SOURCE)){
                rowWrapper = createSpecimenRowWrapper(HibernateProxyHelper.deproxy(description, SpecimenDescription.class), descriptiveDataSetUuid);
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

    @Override
    public TaxonDescription findDefaultDescription(UUID specimenDescriptionUuid, UUID dataSetUuid){
        SpecimenDescription specimenDescription = (SpecimenDescription) descriptionService.load(specimenDescriptionUuid);
        DescriptiveDataSet dataSet = load(dataSetUuid);
        TaxonNode node = findTaxonNodeForDescription(specimenDescription, dataSet);
        return recurseDefaultDescription(node, dataSet);
    }

    private TaxonDescription recurseDefaultDescription(TaxonNode node, DescriptiveDataSet dataSet){
        TaxonDescription defaultDescription = findTaxonDescriptionByDescriptionType(dataSet, node.getTaxon(), DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION);
        if(defaultDescription==null && node.getParent()!=null){
            defaultDescription = recurseDefaultDescription(node.getParent(), dataSet);
        }
        return defaultDescription;
    }

    private TaxonNode findTaxonNodeForDescription(SpecimenDescription description, DescriptiveDataSet descriptiveDataSet){
        SpecimenOrObservationBase specimen = description.getDescribedSpecimenOrObservation();
        TaxonNode taxonNode = null;
        //get taxon node

        Set<IndividualsAssociation> associations = (Set<IndividualsAssociation>) descriptiveDataSet.getDescriptions()
                .stream()
                .flatMap(desc->desc.getElements().stream())// put all description element in one stream
                .filter(element->element instanceof IndividualsAssociation)
                .map(ia->(IndividualsAssociation)ia)
                .collect(Collectors.toSet());
        Classification classification = descriptiveDataSet.getTaxonSubtreeFilter().iterator().next().getClassification();
        for (IndividualsAssociation individualsAssociation : associations) {
            if(individualsAssociation.getAssociatedSpecimenOrObservation().equals(specimen)){
                return ((TaxonDescription) individualsAssociation.getInDescription()).getTaxon().getTaxonNode(classification);
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
        DescriptiveDataSet descriptiveDataSet = dao.load(descriptiveDataSetUuid, null);
        Optional<TaxonNode> first = descriptiveDataSet.getTaxonSubtreeFilter().stream()
                .filter(node->node.getClassification()!=null).findFirst();
        Optional<Classification> classificationOptional = first.map(node->node.getClassification());
        if(classificationOptional.isPresent()){
            classification = classificationOptional.get();
            Taxon taxon = (Taxon) taxonService.load(description.getTaxon().getId(), Arrays.asList("taxonNodes", "taxonNodes.classification"));
            taxonNode = taxon.getTaxonNode(classification);
        }
        return new TaxonRowWrapperDTO(description, new TaxonNodeDto(taxonNode));
    }

    @Override
    @Transactional(readOnly=false)
    public UpdateResult addRowWrapperToDataset(Collection<SpecimenNodeWrapper> wrappers, UUID datasetUuid){
        UpdateResult result = new UpdateResult();
        DescriptiveDataSet dataSet = load(datasetUuid);
        result.setCdmEntity(dataSet);
        for (SpecimenNodeWrapper wrapper : wrappers) {
            UUID taxonDescriptionUuid = wrapper.getTaxonDescriptionUuid();
            TaxonDescription taxonDescription = null;
            if(taxonDescriptionUuid!=null){
                taxonDescription = (TaxonDescription) descriptionService.load(taxonDescriptionUuid);
            }
            if(taxonDescription==null){
                Optional<TaxonDescription> associationDescriptionOptional = wrapper.getTaxonNode().getTaxon().getDescriptions().stream()
                        .filter(desc->desc.getTypes().contains(DescriptionType.INDIVIDUALS_ASSOCIATION))
                        .findFirst();
                Taxon taxon = wrapper.getTaxonNode().getTaxon();
                if(!associationDescriptionOptional.isPresent()){
                    taxonDescription = TaxonDescription.NewInstance(taxon);
                }
                else{
                    taxonDescription = associationDescriptionOptional.get();
                }

                SpecimenOrObservationBase specimen = occurrenceService.load(wrapper.getUuidAndTitleCache().getUuid());
                IndividualsAssociation association = IndividualsAssociation.NewInstance(specimen);
                taxonDescription.addElement(association);
                taxonService.saveOrUpdate(taxon);
                result.addUpdatedObject(taxon);
            }
            SpecimenDescription specimenDescription = findSpecimenDescription(datasetUuid, wrapper.getUuidAndTitleCache().getUuid(), true);
            SpecimenRowWrapperDTO rowWrapper = createSpecimenRowWrapper(specimenDescription, wrapper.getTaxonNode().getUuid(), datasetUuid);
            if(rowWrapper==null){
                result.addException(new IllegalArgumentException("Could not create wrapper for "+specimenDescription));
                continue;
            }
            //add specimen description to data set
            rowWrapper.getDescription().addDescriptiveDataSet(dataSet);
            //add taxon description with IndividualsAssociation to the specimen to data set
            taxonDescription.addDescriptiveDataSet(dataSet);

            result.addUpdatedObject(rowWrapper.getDescription());
            result.addUpdatedObject(taxonDescription);
        }
        saveOrUpdate(dataSet);
        return result;
    }

    private SpecimenRowWrapperDTO createSpecimenRowWrapper(SpecimenDescription description, UUID taxonNodeUuid,
            UUID datasetUuid) {
        TaxonNode taxonNode = taxonNodeService.load(taxonNodeUuid);
        DescriptiveDataSet descriptiveDataSet = load(datasetUuid);
        SpecimenOrObservationBase specimen = description.getDescribedSpecimenOrObservation();
        //supplemental information
        if(taxonNode==null){
            taxonNode = findTaxonNodeForDescription(description, descriptiveDataSet);
        }
        FieldUnit fieldUnit = null;
        String identifier = null;
        NamedArea country = null;
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
        TaxonDescription defaultTaxonDescription = findTaxonDescriptionByDescriptionType(descriptiveDataSet.getUuid(),
                taxonNode.getUuid(), DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION);
        TaxonRowWrapperDTO taxonRowWrapper = defaultTaxonDescription != null
                ? createTaxonRowWrapper(defaultTaxonDescription.getUuid(), descriptiveDataSet.getUuid()) : null;
        SpecimenRowWrapperDTO specimenRowWrapperDTO = new SpecimenRowWrapperDTO(description, new TaxonNodeDto(taxonNode), fieldUnit, identifier, country);
        specimenRowWrapperDTO.setDefaultDescription(taxonRowWrapper);
        return specimenRowWrapperDTO;
    }

    @Override
    public SpecimenRowWrapperDTO createSpecimenRowWrapper(SpecimenDescription description, UUID descriptiveDataSetUuid){
        return createSpecimenRowWrapper(description, null, descriptiveDataSetUuid);
	}

    @Override
    @Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends DescriptiveDataSet> clazz, Integer stepSize,
            IIdentifiableEntityCacheStrategy<DescriptiveDataSet> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null) {
            clazz = DescriptiveDataSet.class;
        }
        return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
    }

    private TaxonDescription findTaxonDescriptionByDescriptionType(DescriptiveDataSet dataSet, Taxon taxon, DescriptionType descriptionType){
        Optional<TaxonDescription> first = taxon.getDescriptions().stream()
                .filter(desc -> desc.getTypes().stream().anyMatch(type -> type.equals(descriptionType)))
                .filter(desc -> dataSet.getDescriptions().contains(desc))
                .findFirst();
        if(first.isPresent()){
            return HibernateProxyHelper.deproxy(descriptionService.load(first.get().getUuid(),
                  Arrays.asList("taxon", "descriptionElements", "descriptionElements.feature")), TaxonDescription.class);
        }
        return null;
    }

    @Override
    public TaxonDescription findTaxonDescriptionByDescriptionType(UUID dataSetUuid, UUID taxonNodeUuid, DescriptionType descriptionType){
        DescriptiveDataSet dataSet = load(dataSetUuid);
        TaxonNode taxonNode = taxonNodeService.load(taxonNodeUuid);
        return findTaxonDescriptionByDescriptionType(dataSet, taxonNode.getTaxon(), descriptionType);
    }

    @Override
    @Transactional(readOnly=false)
    public UpdateResult aggregate(UUID descriptiveDataSetUuid, DescriptionAggregationConfiguration config, IProgressMonitor monitor) {
        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        Set<DescriptionBase> descriptions = dataSet.getDescriptions();

        monitor.beginTask("Aggregate data set", descriptions.size()*2);

        UpdateResult result = new UpdateResult();
        result.setCdmEntity(dataSet);

        // delete all aggregation description of this dataset (DescriptionType.AGGREGATED)
        Set<TaxonDescription> aggregations = dataSet.getDescriptions().stream()
        .filter(aggDesc->aggDesc instanceof TaxonDescription)
        .map(aggDesc->(TaxonDescription)aggDesc)
        .filter(desc -> desc.getTypes().contains(DescriptionType.AGGREGATED))
        .collect(Collectors.toSet());
        aggregations.forEach(aggregation->dataSet.removeDescription(aggregation));
        // also delete all their cloned source descriptions
        Set<String> sourceUuids = aggregations.stream()
        .flatMap(aggDesc->aggDesc.getSources().stream())
        .filter(source->source.getType().equals(OriginalSourceType.Aggregation))
        .map(aggSource->aggSource.getIdInSource())
        .collect(Collectors.toSet());
        for (String string : sourceUuids) {
            try {
                UUID uuid = UUID.fromString(string);
                DescriptionBase sourceClone = descriptionService.load(uuid);
                descriptionService.deleteDescription(sourceClone);
            } catch (IllegalArgumentException|NullPointerException e) {
                // ignore
            }
        }
        //finally delete the aggregation description itself
        aggregations.forEach(aggDesc->descriptionService.delete(aggDesc));

        // sort descriptions by taxa
        Map<TaxonNode, Set<UUID>> taxonNodeToSpecimenDescriptionMap = new HashMap<>();
        for (DescriptionBase descriptionBase : descriptions) {
            if(monitor.isCanceled()){
                result.setAbort();
                return result;
            }

            if(descriptionBase instanceof SpecimenDescription){
                SpecimenDescription specimenDescription = HibernateProxyHelper.deproxy(descriptionBase, SpecimenDescription.class);
                if(specimenDescription.getElements().stream().anyMatch(element->hasCharacterData(element))){
                    TaxonNode taxonNode = findTaxonNodeForDescription(specimenDescription, dataSet);
                    if(taxonNode!=null){
                        addDescriptionToTaxonNodeMap(specimenDescription.getUuid(), taxonNode, taxonNodeToSpecimenDescriptionMap);
                    }
                }
            }
            monitor.worked(1);
        }
        if(config.isRecursiveAggregation()){
            propagateDescriptionsToParentNodes(dataSet, taxonNodeToSpecimenDescriptionMap);
        }
        // aggregate per taxa
        Map<UUID, UUID> specimenToClonedSourceDescription = new HashMap<>();
        for (Entry<TaxonNode, Set<UUID>> entry: taxonNodeToSpecimenDescriptionMap.entrySet()) {
            if(monitor.isCanceled()){
                result.setAbort();
                return result;
            }
            UUID taxonUuid = entry.getKey().getTaxon().getUuid();
            Set<UUID> specimenDescriptionUuids = entry.getValue();
            result.includeResult(aggregateDescription(taxonUuid, specimenDescriptionUuids, descriptiveDataSetUuid,
                    specimenToClonedSourceDescription));
            monitor.worked(1);
        }
        monitor.done();
        return result;
    }


    private boolean hasCharacterData(DescriptionElementBase element) {
        return (element instanceof CategoricalData && !((CategoricalData) element).getStatesOnly().isEmpty())
                || (element instanceof QuantitativeData
                        && !((QuantitativeData) element).getStatisticalValues().isEmpty());
    }

    private void addDescriptionToTaxonNodeMap(UUID descriptionUuid, TaxonNode taxonNode, Map<TaxonNode, Set<UUID>> taxonNodeToSpecimenDescriptionMap){
        Set<UUID> specimenDescriptionUuids = taxonNodeToSpecimenDescriptionMap.get(taxonNode);
        if(specimenDescriptionUuids==null){
            specimenDescriptionUuids = new HashSet<>();
        }
        specimenDescriptionUuids.add(descriptionUuid);
        taxonNodeToSpecimenDescriptionMap.put(taxonNode, specimenDescriptionUuids);
    }

    private void propagateDescriptionsToParentNodes(DescriptiveDataSet dataSet, Map<TaxonNode, Set<UUID>> taxonNodeToSpecimenDescriptionMap){
        Map<TaxonNode, Set<UUID>> parentMap = new HashMap<>();
        for (Entry<TaxonNode, Set<UUID>> entry: taxonNodeToSpecimenDescriptionMap.entrySet()) {
            Set<UUID> descriptionUuids = entry.getValue();
            TaxonNode node = entry.getKey();
            TaxonNode parentNode = node.getParent();
            while(parentNode!=null && isTaxonNodeInDescriptiveDataSet(parentNode, dataSet)){
                for (UUID uuid : descriptionUuids) {
                    addDescriptionToTaxonNodeMap(uuid, node.getParent(), parentMap);
                }
                parentNode = parentNode.getParent();
            }
        }
        // merge parent map
        for (Entry<TaxonNode, Set<UUID>> entry: parentMap.entrySet()) {
            Set<UUID> descriptionUuids = entry.getValue();
            TaxonNode node = entry.getKey();
            for (UUID uuid : descriptionUuids) {
                addDescriptionToTaxonNodeMap(uuid, node, taxonNodeToSpecimenDescriptionMap);
            }
        }
    }

    private boolean isTaxonNodeInDescriptiveDataSet(TaxonNode taxonNode, DescriptiveDataSet dataSet){
        Set<TaxonNode> taxonSubtreeFilter = dataSet.getTaxonSubtreeFilter();
        for (TaxonNode datasetNode : taxonSubtreeFilter) {
            if(datasetNode.getUuid().equals(taxonNode.getUuid())){
                return true;
            }
            List<TaxonNode> allChildren = taxonNodeService.loadChildNodesOfTaxonNode(datasetNode, null, true, true, null);
            for (TaxonNode childNode : allChildren) {
                if(childNode.getUuid().equals(taxonNode.getUuid())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly=false)
    public UpdateResult generatePolytomousKey(UUID descriptiveDataSetUuid, UUID taxonUuid) {
        UpdateResult result = new UpdateResult();

        PolytomousKeyGeneratorConfigurator keyConfig = new PolytomousKeyGeneratorConfigurator();
        DescriptiveDataSet descriptiveDataSet = load(descriptiveDataSetUuid);
        keyConfig.setDataSet(descriptiveDataSet);
        PolytomousKey key = new PolytomousKeyGenerator().invoke(keyConfig);
        IdentifiableServiceConfiguratorImpl<PolytomousKey> serviceConfig= new IdentifiableServiceConfiguratorImpl<>();
        serviceConfig.setTitleSearchString(descriptiveDataSet.getTitleCache());
        List<PolytomousKey> list = polytomousKeyService.findByTitle(serviceConfig).getRecords();
        if(list!=null){
            list.forEach(polytomousKey->polytomousKeyService.delete(polytomousKey));
        }
        key.setTitleCache(descriptiveDataSet.getTitleCache(), true);

        Taxon taxon = (Taxon) taxonService.load(taxonUuid);
        key.addTaxonomicScope(taxon);

        polytomousKeyService.saveOrUpdate(key);

        result.setCdmEntity(key);
        result.addUpdatedObject(taxon);
        return result;
    }

    @SuppressWarnings("unchecked")
    private UpdateResult aggregateDescription(UUID taxonUuid, Set<UUID> specimenDescriptionUuids,
            UUID descriptiveDataSetUuid, Map<UUID, UUID> specimenToClonedSourceDescription) {
        UpdateResult result = new UpdateResult();

        TaxonBase taxonBase = taxonService.load(taxonUuid);
        if(!(taxonBase instanceof Taxon)){
            result.addException(new ClassCastException("The given taxonUUID does not belong to a taxon"));
            result.setError();
            return result;
        }
        Taxon taxon = (Taxon)taxonBase;
        List<DescriptionBase> descriptions = descriptionService.load(new ArrayList<>(specimenDescriptionUuids), null);
        List<SpecimenDescription> specimenDescriptions = descriptions.stream()
                .filter(d -> d instanceof SpecimenDescription)
                .map(d -> (SpecimenDescription) d)
                .collect(Collectors.toList());
        Map<Character, List<DescriptionElementBase>> featureToElementMap = new HashMap<>();

        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        if(dataSet==null){
            result.addException(new IllegalArgumentException("Could not find data set for uuid "+descriptiveDataSetUuid));
            result.setAbort();
            return result;
        }

        //extract all character description elements
        for (DescriptionBase<?> description : specimenDescriptions) {
            description.getElements().stream()
            //filter out elements that do not have a Character as Feature
            .filter(element->HibernateProxyHelper.isInstanceOf(element.getFeature(), Character.class))
            .forEach(ele->addCharacterToMap(featureToElementMap, ele));
        }

        TaxonDescription aggregationDescription = createAggregationDescription(taxon, dataSet);

        aggregateCharacterData(featureToElementMap, aggregationDescription);

        // add sources to aggregation description
        // create a snapshot of those descriptions that were used to create the aggregated descriptions
        // TODO implement when the clones descriptions can be attached to taxon
        // descriptions as sources
        specimenDescriptions.forEach(specimenDescription -> addSourceDescription(aggregationDescription, specimenDescription,
                specimenToClonedSourceDescription));

        result.addUpdatedObject(taxon);
        result.addUpdatedObject(aggregationDescription);

        return result;
    }

    private void aggregateCharacterData(Map<Character, List<DescriptionElementBase>> featureToElementMap,
            TaxonDescription aggregationDescription) {
        for(Entry<Character, List<DescriptionElementBase>> entry:featureToElementMap.entrySet()){
            Character character = entry.getKey();
            List<DescriptionElementBase> elements = entry.getValue();
            //aggregate categorical data
            if(character.isSupportsCategoricalData()){
                aggregateCategoricalData(aggregationDescription, character, elements);
            }
            //aggregate quantitative data
            else if(character.isSupportsQuantitativeData()){
                aggregateQuantitativeData(aggregationDescription, character, elements);
            }
        }
    }

    private TaxonDescription createAggregationDescription(Taxon taxon, DescriptiveDataSet dataSet) {
        TaxonDescription aggregationDescription = TaxonDescription.NewInstance(taxon);
        aggregationDescription.setTitleCache("[Aggregation] "+dataSet.getTitleCache(), true);
        aggregationDescription.getTypes().add(DescriptionType.AGGREGATED);
        aggregationDescription.addSource(IdentifiableSource.NewInstance(OriginalSourceType.Aggregation));
        aggregationDescription.addDescriptiveDataSet(dataSet);
        return aggregationDescription;
    }

    private void addSourceDescription(TaxonDescription taxonDescription, SpecimenDescription specimenDescription,
            Map<UUID, UUID> specimenToClonedSourceDescription) {
        UUID sourceCloneUuid = specimenToClonedSourceDescription.get(specimenDescription.getUuid());
        if(sourceCloneUuid!=null){
            addAggregationSource(taxonDescription, sourceCloneUuid);
        }
        else{
            SpecimenOrObservationBase<?> specimenOrObservation = specimenDescription.getDescribedSpecimenOrObservation();
            SpecimenDescription clone = (SpecimenDescription) specimenDescription.clone();
            clone.getTypes().add(DescriptionType.CLONE_FOR_SOURCE);
            specimenOrObservation.addDescription(clone);

            addAggregationSource(taxonDescription, clone.getUuid());
            specimenToClonedSourceDescription.put(specimenDescription.getUuid(), clone.getUuid());
        }
    }

    private void addAggregationSource(TaxonDescription taxonDescription, UUID cloneUuid) {
        IdentifiableSource source = IdentifiableSource.NewInstance(OriginalSourceType.Aggregation);
        source.setIdInSource(cloneUuid.toString());
        source.setIdNamespace("SpecimenDescription");
        taxonDescription.addSource(source);
    }

    private void aggregateQuantitativeData(TaxonDescription description, Character character,
            List<DescriptionElementBase> elements) {
        QuantitativeData aggregate = QuantitativeData.NewInstance(character);
        List<Float> values = new ArrayList<>();
        float sampleSize = 0;
        for (DescriptionElementBase element : elements) {
            if(element instanceof QuantitativeData){
                QuantitativeData quantitativeData = (QuantitativeData)element;
                values.addAll(quantitativeData.getStatisticalValues().stream()
                .filter(value->value.getType().equals(StatisticalMeasure.EXACT_VALUE()))
                .map(exact->exact.getValue())
                .collect(Collectors.toList()));
                if(quantitativeData.getMin()!=null){
                    values.add(quantitativeData.getMin());
                }
                if(quantitativeData.getMax()!=null){
                    values.add(quantitativeData.getMax());
                }
                sampleSize++;
            }
        }
        aggregate.setSampleSize(sampleSize, null);
        OptionalDouble min = values.stream().mapToDouble(value->(double)value).min();
        OptionalDouble max = values.stream().mapToDouble(value->(double)value).max();
        OptionalDouble avg = values.stream().mapToDouble(value->(double)value).average();
        if(min.isPresent()){
            aggregate.setMinimum((float)min.getAsDouble(), null);
        }
        if(max.isPresent()){
            aggregate.setMaximum((float)max.getAsDouble(), null);
        }
        if(avg.isPresent()){
            aggregate.setAverage((float)avg.getAsDouble(), null);
        }
        description.addElement(aggregate);
    }

    private void aggregateCategoricalData(TaxonDescription description, Character character,
            List<DescriptionElementBase> elements) {
        CategoricalData aggregate = CategoricalData.NewInstance(character);
        elements.stream()
        .filter(element->element instanceof CategoricalData)
        .flatMap(categoricalData->((CategoricalData)categoricalData).getStateData().stream())
        .forEach(stateData->aggregate.addStateData((StateData) stateData.clone()));
        description.addElement(aggregate);
    }

    private void addCharacterToMap(Map<Character, List<DescriptionElementBase>> featureToElementMap, DescriptionElementBase descriptionElement) {
        List<DescriptionElementBase> list = featureToElementMap.get(descriptionElement.getFeature());
        if(list==null){
            list = new ArrayList<>();
        }
        list.add(descriptionElement);
        featureToElementMap.put(HibernateProxyHelper.deproxy(descriptionElement.getFeature(), Character.class), list);
    }

    @Override
    @Transactional(readOnly=false)
    public DeleteResult removeDescription(UUID descriptionUuid, UUID descriptiveDataSetUuid) {
        DeleteResult result = new DeleteResult();
        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        DescriptionBase descriptionBase = descriptionService.load(descriptionUuid);
        if(dataSet==null || descriptionBase==null){
            result.setError();
        }
        else{
            boolean success = dataSet.removeDescription(descriptionBase);
            result.addDeletedObject(descriptionBase);
            // remove taxon description with IndividualsAssociation from data set
            if(descriptionBase instanceof SpecimenDescription){
                Set<IndividualsAssociation> associations = (Set<IndividualsAssociation>) dataSet.getDescriptions()
                        .stream()
                        .flatMap(desc->desc.getElements().stream())// put all description element in one stream
                        .filter(element->element instanceof IndividualsAssociation)
                        .map(ia->(IndividualsAssociation)ia)
                        .collect(Collectors.toSet());
                Classification classification = dataSet.getTaxonSubtreeFilter().iterator().next().getClassification();
                for (IndividualsAssociation individualsAssociation : associations) {
                    if(individualsAssociation.getAssociatedSpecimenOrObservation().equals(descriptionBase.getDescribedSpecimenOrObservation())){
                        dataSet.removeDescription(individualsAssociation.getInDescription());
                        result.addDeletedObject(individualsAssociation.getInDescription());
                    }
                }
            }
            result.addUpdatedObject(dataSet);
            result.setStatus(success?Status.OK:Status.ERROR);
        }
        return result;
    }

    @Override
    @Transactional(readOnly=false)
    public TaxonRowWrapperDTO createTaxonDescription(UUID dataSetUuid, UUID taxonNodeUuid, DescriptionType descriptionType){
        DescriptiveDataSet dataSet = load(dataSetUuid);
        TaxonNode taxonNode = taxonNodeService.load(taxonNodeUuid, Arrays.asList("taxon"));
        TaxonDescription newTaxonDescription = TaxonDescription.NewInstance(taxonNode.getTaxon());
        String tag = "";
        if(descriptionType.equals(DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION)){
            tag = "[Default]";
        }
        else if(descriptionType.equals(DescriptionType.SECONDARY_DATA)){
            tag = "[Literature]";
        }
        newTaxonDescription.setTitleCache(tag+" "+dataSet.getLabel()+": "+newTaxonDescription.generateTitle(), true); //$NON-NLS-2$
        newTaxonDescription.getTypes().add(descriptionType);

        dataSet.getDescriptiveSystem().getDistinctTerms().forEach(wsFeature->{
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
    public List<TermDto> getSupportedStatesForFeature(UUID featureUuid){
        return termDao.getSupportedStatesForFeature(featureUuid);
    }

    @Override
    @Transactional(readOnly=false)
    public SpecimenDescription findSpecimenDescription(UUID descriptiveDataSetUuid, UUID specimenUuid, boolean addDatasetSource){
        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        SpecimenOrObservationBase specimen = occurrenceService.load(specimenUuid);

        Set<? extends Feature> datasetFeatures = dataSet.getDescriptiveSystem().getDistinctTerms();
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
