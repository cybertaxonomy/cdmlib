package eu.etaxonomy.cdm.api.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.DeleteDescriptiveDataSetConfigurator;
import eu.etaxonomy.cdm.api.service.config.IdentifiableServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.config.RemoveDescriptionsFromDescriptiveDataSetConfigurator;
import eu.etaxonomy.cdm.api.service.dto.CategoricalDataDto;
import eu.etaxonomy.cdm.api.service.dto.DescriptionBaseDto;
import eu.etaxonomy.cdm.api.service.dto.DescriptionElementDto;
import eu.etaxonomy.cdm.api.service.dto.QuantitativeDataDto;
import eu.etaxonomy.cdm.api.service.dto.RowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.SpecimenOrObservationDTOFactory;
import eu.etaxonomy.cdm.api.service.dto.SpecimenRowWrapperDTO;
import eu.etaxonomy.cdm.api.service.dto.StateDataDto;
import eu.etaxonomy.cdm.api.service.dto.StatisticalMeasurementValueDto;
import eu.etaxonomy.cdm.api.service.dto.TaxonRowWrapperDTO;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.format.description.DefaultCategoricalDescriptionBuilder;
import eu.etaxonomy.cdm.format.description.DefaultQuantitativeDescriptionBuilder;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.CdmLinkSource;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptiveDataSetDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.DescriptiveDataSetBaseDto;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermTreeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.generate.PolytomousKeyGenerator;
import eu.etaxonomy.cdm.strategy.generate.PolytomousKeyGeneratorConfigurator;

@Service
@Transactional(readOnly=true)
public class DescriptiveDataSetService
        extends IdentifiableServiceBase<DescriptiveDataSet, IDescriptiveDataSetDao>
        implements IDescriptiveDataSetService {

    private static Logger logger = LogManager.getLogger();

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
	public List<RowWrapperDTO<?>> getRowWrapper(UUID descriptiveDataSetUuid, IProgressMonitor monitor) {
	    DescriptiveDataSetBaseDto datasetDto = dao.getDescriptiveDataSetDtoByUuid(descriptiveDataSetUuid);
//	    DescriptiveDataSet descriptiveDataSet = load(descriptiveDataSetUuid);
	    monitor.beginTask("Load row wrapper", datasetDto.getDescriptionUuids().size());
	    List<RowWrapperDTO<?>> wrappers = new ArrayList<>();
	    Set<UUID> descriptions = datasetDto.getDescriptionUuids();
	    for (UUID description : descriptions) {
            if(monitor.isCanceled()){
                return new ArrayList<>();
            }
            DescriptionBaseDto descDto = descriptionService.loadDto(description);
            RowWrapperDTO<?> rowWrapper = null;
            if (descDto != null && descDto.getTaxonDto() != null &&
                    (descDto.getTypes().contains(DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION)
                            || descDto.getTypes().contains(DescriptionType.AGGREGATED_STRUC_DESC)
                            || descDto.getTypes().contains(DescriptionType.SECONDARY_DATA)
                            )){
                rowWrapper = createTaxonRowWrapper(descDto, datasetDto.getUuid());
            }
            else if (descDto != null &&descDto.getSpecimenDto() != null && (descDto.getTypes() == null ||
                    !descDto.getTypes().contains(DescriptionType.CLONE_FOR_SOURCE))){
                rowWrapper = createSpecimenRowWrapper(descDto, descriptiveDataSetUuid);
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
        if(filteredNodes.isEmpty()){
            return Collections.emptySet();
        }
        Collection<SpecimenNodeWrapper> result = occurrenceService.listUuidAndTitleCacheByAssociatedTaxon(filteredNodes, null, null);

        return result;
    }

    @Override
    public Collection<SpecimenNodeWrapper> loadSpecimens(UUID descriptiveDataSetUuid){
        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        return loadSpecimens(dataSet);
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
    public DescriptionBaseDto findDefaultDescription(UUID specimenDescriptionUuid, UUID dataSetUuid){
        DescriptionBaseDto specimenDescription = descriptionService.loadDto(specimenDescriptionUuid);
        DescriptiveDataSetBaseDto dataSet = dao.getDescriptiveDataSetDtoByUuid(dataSetUuid);
        TaxonNodeDto node = findTaxonNodeForDescription(specimenDescription, dataSet);
        return recurseDefaultDescription(node, dataSet);
    }

    private DescriptionBaseDto recurseDefaultDescription(TaxonNodeDto node, DescriptiveDataSetBaseDto dataSet){
        DescriptionBaseDto defaultDescription = null;
        if(node!=null && node.getTaxonUuid()!=null){
            defaultDescription = getTaxonDescriptionForDescriptiveDataSetAndType(dataSet, node.getTaxonUuid(), DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION);
            if(defaultDescription==null && node.getParentUUID()!=null){
                defaultDescription = recurseDefaultDescription(taxonNodeService.dto(node.getParentUUID()), dataSet);
            }
        }
        return defaultDescription;
    }

    private TaxonNodeDto findTaxonNodeForDescription(DescriptionBaseDto description, DescriptiveDataSetBaseDto descriptiveDataSet){
        UuidAndTitleCache<SpecimenOrObservationBase> specimen = description.getSpecimenDto();
        //get taxon node


        return descriptionService.findTaxonNodeDtoForIndividualAssociation(specimen.getUuid(), descriptiveDataSet.getSubTreeFilter().iterator().next().getClassificationUUID());
        //NOTE: don't remove cast as it does not compile on some systems
//        List<DescriptionBaseDto> descDtos = descriptionService.loadDtos(descriptiveDataSet.getDescriptionUuids());
//        descriptionService.
//        Set<IndividualsAssociation> associations = descDtos
//                .stream()
//                .flatMap(desc->desc.getElements().stream())// put all description element in one stream
//                .filter(element->element.get)
//                .map(ia->(IndividualsAssociation)ia)
//                .collect(Collectors.toSet());
//        UUID classification = descriptiveDataSet.getSubTreeFilter().iterator().next().getClassificationUUID();
//        for (IndividualsAssociation individualsAssociation : associations) {
//            if(individualsAssociation.getAssociatedSpecimenOrObservation().equals(specimen)){
//                return ((TaxonDescription) individualsAssociation.getInDescription()).getTaxon().getTaxonNode(classification);
//            }
//        }
//        return null;
    }

    @Override
    public TaxonRowWrapperDTO createTaxonRowWrapper(UUID taxonDescriptionUuid, UUID descriptiveDataSetUuid) {
        DescriptionBaseDto description = descriptionService.loadDto(taxonDescriptionUuid);
        return createTaxonRowWrapper(description, descriptiveDataSetUuid);
    }

    @Override
    @Transactional(readOnly=false)
    public UpdateResult addRowWrapperToDataset(Collection<SpecimenRowWrapperDTO> wrappers, UUID datasetUuid, boolean addDatasetSource){
        UpdateResult result = new UpdateResult();
        DescriptiveDataSet dataSet = load(datasetUuid);
        result.setCdmEntity(dataSet);

        List<UUID> taxonUuids = wrappers.stream().map(wrapper->wrapper.getTaxonNode().getTaxonUuid()).collect(Collectors.toList());
        List<TaxonBase> taxa = taxonService.load(taxonUuids, Arrays.asList(new String[]{"descriptions"}));

        for (SpecimenRowWrapperDTO wrapper : wrappers) {
            Optional<TaxonBase> findAny = taxa.stream().filter(taxon->taxon.getUuid().equals(wrapper.getTaxonNode().getTaxonUuid())).findAny();
            if(!findAny.isPresent()){
                result.addException(new IllegalArgumentException("Could not create wrapper for "+ wrapper.getSpecimenDto().getLabel()));
                continue;
            }
            Taxon taxon = (Taxon) findAny.get();

            SpecimenOrObservationBase<?> specimen = occurrenceService.load(wrapper.getSpecimenDto().getUuid());

            TaxonDescription taxonDescription = taxon.getDescriptions().stream()
                    .filter(desc->desc.getTypes().contains(DescriptionType.INDIVIDUALS_ASSOCIATION))
                    .findFirst().orElseGet(()->{
                        TaxonDescription td = TaxonDescription.NewInstance(taxon);
                        td.addType(DescriptionType.INDIVIDUALS_ASSOCIATION);
                        td.setTitleCache("Specimens used by " + dataSet.getTitleCache() + " for " + getTaxonLabel(taxon), true);
                        return td;});
            IndividualsAssociation association = null;
            for (DescriptionElementBase el:taxonDescription.getElements()){
                if (el instanceof IndividualsAssociation){
                    IndividualsAssociation indAss = (IndividualsAssociation)el;
                    if (indAss.getAssociatedSpecimenOrObservation().getUuid().equals(specimen.getUuid())){
                        association = indAss;
                    }
                }
            }

            if (association == null){
                association = IndividualsAssociation.NewInstance(specimen);
                taxonDescription.addElement(association);
                taxonService.saveOrUpdate(taxon);
                result.addUpdatedObject(taxon);
            }

            UUID specimenDescriptionUuid = wrapper.getDescription().getDescriptionUuid();
            DescriptionBaseDto descriptionDto = wrapper.getDescription();
            DescriptionBase<?> specimenDescription =  descriptionService.load(specimenDescriptionUuid);
            //if description already exist use the loaded one and add changed data otherwise create a new one and add to specimen
            if (specimenDescription == null){
                specimenDescription = SpecimenDescription.NewInstance(specimen);
                specimenDescription.setUuid(specimenDescriptionUuid);
                List<DescriptionElementDto> elementDtos = descriptionDto.getElements();

                for (DescriptionElementDto elementDto: elementDtos){
                    if (elementDto instanceof CategoricalDataDto){
                        Feature feature = DefinedTermBase.getTermByUUID(elementDto.getFeatureUuid(), Feature.class);
                        CategoricalData data = CategoricalData.NewInstance(feature);
                        for (StateDataDto stateDto:((CategoricalDataDto) elementDto).getStates()){
                            DefinedTermBase<?> state = DefinedTermBase.getTermByUUID(stateDto.getState().getUuid(), DefinedTermBase.class);
                            data.addStateData(state);
                            specimenDescription.addElement(data);
                        }
                    }
                    if (elementDto instanceof QuantitativeDataDto){
                        Feature feature = DefinedTermBase.getTermByUUID(elementDto.getFeatureUuid(), Feature.class);
                        QuantitativeData data = QuantitativeData.NewInstance(feature);
                        if (((QuantitativeDataDto) elementDto).getMeasurementUnit() != null){
                            MeasurementUnit unit = DefinedTermBase.getTermByClassAndUUID(MeasurementUnit.class, ((QuantitativeDataDto) elementDto).getMeasurementUnit().getUuid());
                            data.setUnit(unit);
                        }

                        for (StatisticalMeasurementValueDto stateDto:((QuantitativeDataDto) elementDto).getValues()){
                            StatisticalMeasure statMeasure = DefinedTermBase.getTermByClassAndUUID(StatisticalMeasure.class, stateDto.getType().getUuid());
                            StatisticalMeasurementValue value = StatisticalMeasurementValue.NewInstance(statMeasure, stateDto.getValue());
                            data.addStatisticalValue(value);
                            specimenDescription.addElement(data);
                        }
                    }
                }
            }else {
                List<DescriptionElementDto> elementDtos = descriptionDto.getElements();
                for (DescriptionElementDto elementDto: elementDtos){
                    if (elementDto instanceof CategoricalDataDto){
                        Feature feature = DefinedTermBase.getTermByUUID(elementDto.getFeatureUuid(), Feature.class);
                        List<DescriptionElementBase> uniqueElementList = specimenDescription.getElements().stream().filter(element -> element.getUuid().equals(elementDto.getElementUuid())).collect(Collectors.toList());
                        List<DefinedTermBase<?>> allStates = new ArrayList<>();
                        CategoricalData element = null;
                        if (uniqueElementList.size() == 1){
                            element = HibernateProxyHelper.deproxy(uniqueElementList.get(0), CategoricalData.class);
                        }else{
                            element = CategoricalData.NewInstance(feature);
                        }
                        for (StateDataDto stateDto:((CategoricalDataDto) elementDto).getStates()){
                            DefinedTermBase<?> state = DefinedTermBase.getTermByUUID(stateDto.getState().getUuid(), DefinedTermBase.class);
                            allStates.add(state);
                        }
                        element.setStateDataOnly(allStates);
                    }
                    if (elementDto instanceof QuantitativeDataDto){
                        Feature feature = DefinedTermBase.getTermByUUID(elementDto.getFeatureUuid(), Feature.class);
                        QuantitativeData data = QuantitativeData.NewInstance(feature);
                        if (((QuantitativeDataDto) elementDto).getMeasurementUnit() != null){
                            MeasurementUnit unit = DefinedTermBase.getTermByClassAndUUID(MeasurementUnit.class, ((QuantitativeDataDto) elementDto).getMeasurementUnit().getUuid());
                            data.setUnit(unit);
                        }

                        for (StatisticalMeasurementValueDto stateDto:((QuantitativeDataDto) elementDto).getValues()){
                            StatisticalMeasure statMeasure = DefinedTermBase.getTermByClassAndUUID(StatisticalMeasure.class, stateDto.getType().getUuid());
                            StatisticalMeasurementValue value = StatisticalMeasurementValue.NewInstance(statMeasure, stateDto.getValue());
                            data.addStatisticalValue(value);
                            specimenDescription.addElement(data);
                        }
                    }
                }
            }
            if(addDatasetSource){
                for (IdentifiableSource source: dataSet.getSources()) {
                    try {
                        specimenDescription.addSource(source.clone());
                    } catch (CloneNotSupportedException e) {
                        //nothing
                    }
                }

            }

            //add specimen description to data set
            specimenDescription.addDescriptiveDataSet(dataSet);
            //add taxon description with IndividualsAssociation to the specimen to data set
            taxonDescription.addDescriptiveDataSet(dataSet);
            result.addUpdatedObject(specimen);
            result.addUpdatedObject(specimenDescription);
            result.addUpdatedObject(taxonDescription);
        }
        saveOrUpdate(dataSet);
        return result;
    }

    private String getTaxonLabel(Taxon taxon) {
        if (taxon.getName() != null){
            return taxon.getName().getTitleCache();
        }else{
            return taxon.getTitleCache();
        }
    }

    private SpecimenRowWrapperDTO createSpecimenRowWrapper(DescriptionBaseDto description, UUID taxonNodeUuid,
            UUID datasetUuid) {
        TaxonNodeDto taxonNode = taxonNodeService.dto(taxonNodeUuid);
        DescriptiveDataSetBaseDto descriptiveDataSet = getDescriptiveDataSetDtoByUuid(datasetUuid);
//        UuidAndTitleCache<SpecimenOrObservationBase> specimen = description.getSpecimenDto();
        SpecimenOrObservationBase specimen = occurrenceService.find(description.getSpecimenDto().getUuid());

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
        if(fieldUnits.size()>1){
            logger.error("More than one or no field unit found for specimen"); //$NON-NLS-1$
            return null;
        }
        else{
            if (fieldUnits.size()>0){
                fieldUnit = fieldUnits.iterator().next();
            }
        }
        //get identifier

        identifier = occurrenceService.getMostSignificantIdentifier(specimen.getUuid());
        //get country
        if(fieldUnit != null && fieldUnit.getGatheringEvent() != null){
            country = fieldUnit.getGatheringEvent().getCountry();
        }
        //get default taxon description
//        TaxonDescription defaultTaxonDescription = findDefaultDescription(description.getUuid(), descriptiveDataSet.getUuid());
        DescriptionBaseDto defaultTaxonDescription = recurseDefaultDescription(taxonNode, descriptiveDataSet);
        TaxonRowWrapperDTO taxonRowWrapper = defaultTaxonDescription != null
                ? createTaxonRowWrapper(defaultTaxonDescription.getDescriptionUuid(), descriptiveDataSet.getUuid()) : null;
//                use description not specimen for specimenRow
        SpecimenRowWrapperDTO specimenRowWrapperDTO = new SpecimenRowWrapperDTO(description, SpecimenOrObservationDTOFactory.fromEntity(specimen), specimen.getRecordBasis(), taxonNode, fieldUnit, identifier, country);
        specimenRowWrapperDTO.setDefaultDescription(taxonRowWrapper);
        return specimenRowWrapperDTO;
    }

    @Override
    public SpecimenRowWrapperDTO createSpecimenRowWrapper(DescriptionBaseDto description, UUID descriptiveDataSetUuid){
        return createSpecimenRowWrapper(description, null, descriptiveDataSetUuid);
	}

    @Override
    public SpecimenRowWrapperDTO createSpecimenRowWrapper(UUID specimenUuid, UUID taxonNodeUuid, UUID descriptiveDataSetUuid){

        SpecimenOrObservationBase<?> specimen = occurrenceService.load(specimenUuid);
        DescriptionBaseDto specimenDescription = findSpecimenDescription(descriptiveDataSetUuid, specimen);
        return createSpecimenRowWrapper(specimenDescription, taxonNodeUuid, descriptiveDataSetUuid);
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

//    private TaxonDescription findTaxonDescriptionByDescriptionType(DescriptiveDataSetBaseDto dataSet, UUID taxonUuid, DescriptionType descriptionType){
//        descriptionService.find
//        Optional<TaxonDescription> first = taxon.getDescriptions().stream()
//                .filter(desc -> desc.getTypes().stream().anyMatch(type -> type.equals(descriptionType)))
//                .filter(desc -> dataSet.getDescriptions().contains(desc))
//                .findFirst();
//        if(first.isPresent()){
//            return HibernateProxyHelper.deproxy(descriptionService.load(first.get().getUuid(),
//                  Arrays.asList("taxon", "descriptionElements", "descriptionElements.feature")), TaxonDescription.class);
//        }
//        return null;
//    }
//
//    @Override
//    public TaxonDescription findTaxonDescriptionByDescriptionType(UUID dataSetUuid, UUID taxonNodeUuid, DescriptionType descriptionType){
//        DescriptiveDataSet dataSet = load(dataSetUuid);
//        TaxonNode taxonNode = taxonNodeService.load(taxonNodeUuid);
//        return findTaxonDescriptionByDescriptionType(dataSet, taxonNode.getTaxon(), descriptionType);
//    }
    @Override
    public DescriptionBaseDto getTaxonDescriptionForDescriptiveDataSetAndType(DescriptiveDataSetBaseDto dataSet, UUID taxonUuid, DescriptionType descriptionType){
        Session session = getSession();
        String queryString = "SELECT d.uuid FROM DescriptiveDataSet a JOIN a.descriptions as d JOIN d.taxon t WHERE t.uuid = :taxonuuid AND a.uuid = :dataSetUuid ";  // and :descriptionType IN d.types

        Query<UUID> query;
        query = session.createQuery(queryString, UUID.class);
        query.setParameter("taxonuuid", taxonUuid);
        query.setParameter("dataSetUuid", dataSet.getUuid());
//        query.setParameter("descriptionType", descriptionType.getKey());

        List<UUID> result = query.getResultList();
        List<DescriptionBaseDto> list = new ArrayList<>();
        list.addAll(descriptionService.loadDtos(new HashSet<>(result)));

        if (list.isEmpty()){
            return null;
        }else {
    		List<DescriptionBaseDto> correctTypeOnly = new ArrayList<>();
    		for (DescriptionBaseDto dto: list) {
    			if (dto.getTypes().contains(descriptionType)) {
    				correctTypeOnly.add(dto);
    			}
    		}
    		if (correctTypeOnly.isEmpty()) {
    			return null;
    		}else {
    			return correctTypeOnly.get(0);
    		}
        }
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

    @Override
    @Transactional(readOnly=false)
    public DeleteResult removeDescription(UUID descriptionUuid, UUID descriptiveDataSetUuid, RemoveDescriptionsFromDescriptiveDataSetConfigurator config) {
        DeleteResult result = new DeleteResult();
        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        DescriptionBase<?> descriptionBase = descriptionService.load(descriptionUuid);
        if(dataSet==null || descriptionBase==null){
            result.setError();
        }
        else{
            removeDescriptionFromDataSet(result, dataSet, descriptionBase, config);
        }
        return result;
    }


    @Override
    @Transactional(readOnly=false)
    public DeleteResult removeDescriptions(List<UUID> descriptionUuids, UUID descriptiveDataSetUuid, RemoveDescriptionsFromDescriptiveDataSetConfigurator config) {
        DeleteResult result = new DeleteResult();
        DescriptiveDataSet dataSet = load(descriptiveDataSetUuid);
        List<DescriptionBase> descriptions = descriptionService.load(descriptionUuids, null);
        if(dataSet==null || descriptions==null){
            result.setError();
        }
        else{
            for (DescriptionBase<?> description: descriptions){
                removeDescriptionFromDataSet(result, dataSet, description, config);
            }
        }
        return result;
    }

    private void removeDescriptionFromDataSet(DeleteResult result, DescriptiveDataSet dataSet,
            DescriptionBase<?> description, RemoveDescriptionsFromDescriptiveDataSetConfigurator config) {
        if (description == null){
            return;
        }
        boolean success = dataSet.removeDescription(description);
        result.addDeletedObject(description);// remove taxon description with IndividualsAssociation from data set
        if(description instanceof SpecimenDescription){
            @SuppressWarnings({ "unchecked", "cast" })
            //NOTE: don't remove cast as it does not compile on some systems
            Set<IndividualsAssociation> associations = (Set<IndividualsAssociation>)dataSet.getDescriptions()
                    .stream()
                    .flatMap(desc->desc.getElements().stream())// put all description element in one stream
                    .filter(element->element instanceof IndividualsAssociation)
                    .map(ia->(IndividualsAssociation)ia)
                    .collect(Collectors.toSet());

            for (IndividualsAssociation individualsAssociation : associations) {
                if(individualsAssociation.getAssociatedSpecimenOrObservation().equals(description.getDescribedSpecimenOrObservation())){
                    dataSet.removeDescription(individualsAssociation.getInDescription());
                    result.addUpdatedObject(individualsAssociation.getInDescription());
                }
            }
        }
        if (description instanceof TaxonDescription){
            DeleteResult isDeletable = descriptionService.isDeletable(description.getUuid());
            for (CdmBase relatedCdmBase: isDeletable.getRelatedObjects()){
                if (relatedCdmBase instanceof CdmLinkSource){
                    CdmLinkSource linkSource = (CdmLinkSource)relatedCdmBase;
                    if (linkSource.getTarget().equals(this)){

                    }
                }
            }


        }
        if (!config.isOnlyRemoveDescriptionsFromDataSet()){
            DeleteResult deleteResult = descriptionService.deleteDescription(description);
            result.includeResult(deleteResult);
            result.addUpdatedObject(dataSet);
        }else{
            MergeResult<DescriptiveDataSet> mergeResult = dao.merge(dataSet, true);
            result.addUpdatedObject(mergeResult.getMergedEntity());
        }

        result.setStatus(success?Status.OK:Status.ERROR);
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(UUID datasetUuid, DeleteDescriptiveDataSetConfigurator config,  IProgressMonitor monitor){
        DescriptiveDataSet dataSet = dao.load(datasetUuid);
        monitor.beginTask("Delete Descriptive Dataset", dataSet.getDescriptions().size() +1);

        DeleteResult result = new DeleteResult();
        DeleteResult descriptionResult = new DeleteResult();
        if (!dataSet.getDescriptions().isEmpty()){
            Set<DescriptionBase> descriptions = new HashSet<>();;
            for (DescriptionBase<?> desc: dataSet.getDescriptions()){
                descriptions.add(desc);
            }
            monitor.subTask("Delete descriptions");
            for (DescriptionBase<?> desc: descriptions){
                dataSet.removeDescription(desc);
                if (desc instanceof SpecimenDescription && config.isDeleteAllSpecimenDescriptions()){
                    descriptionResult.includeResult(descriptionService.deleteDescription(desc));
                }else if (desc instanceof TaxonDescription){
                    if( desc.getTypes().contains(DescriptionType.DEFAULT_VALUES_FOR_AGGREGATION) && config.isDeleteAllDefaultDescriptions()){
                        descriptionResult.includeResult(descriptionService.deleteDescription(desc));
                    }else if (desc.getTypes().contains(DescriptionType.SECONDARY_DATA) && config.isDeleteAllLiteratureDescriptions()){
                        descriptionResult.includeResult(descriptionService.deleteDescription(desc));
                    }else if (desc.getTypes().contains(DescriptionType.AGGREGATED_STRUC_DESC) && config.isDeleteAllAggregatedDescriptions()){
                        descriptionResult.includeResult(descriptionService.deleteDescription(desc));
                    }
                }
            }
        }
        dao.delete(dataSet);
        monitor.worked(1);
        monitor.done();
        result.includeResult(descriptionResult);
        result.setStatus(Status.OK);
        result.addDeletedObject(dataSet);
        return result;
    }

    @Override
    @Transactional(readOnly=false)
    public TaxonRowWrapperDTO createTaxonDescription(UUID dataSetUuid, UUID taxonNodeUuid, DescriptionType descriptionType){
        DescriptiveDataSet dataSet = load(dataSetUuid);
        TaxonNode taxonNode = taxonNodeService.load(taxonNodeUuid, Arrays.asList("taxon"));
        TaxonDescription newTaxonDescription = TaxonDescription.NewInstance(taxonNode.getTaxon());
        newTaxonDescription.setTitleCache(dataSet.getLabel()+": "+newTaxonDescription.generateTitle(), true); //$NON-NLS-2$
        newTaxonDescription.getTypes().add(descriptionType);
        dataSet.addDescription(newTaxonDescription);
        saveOrUpdate(dataSet);
        return createTaxonRowWrapper(newTaxonDescription.getUuid(), dataSet.getUuid());
    }

    @Override
    public Map<UUID, List<TermDto>> getSupportedStatesForFeature(Set<UUID> featureUuids){
        return termDao.getSupportedStatesForFeature(featureUuids);
    }

    @Override
    @Transactional(readOnly=false)
    public DescriptionBaseDto findSpecimenDescription(UUID descriptiveDataSetUuid, SpecimenOrObservationBase specimen){
        DescriptiveDataSetBaseDto dataSet = this.getDescriptiveDataSetDtoByUuid(descriptiveDataSetUuid);
//        SpecimenOrObservationBase specimen = occurrenceService.load(specimenUuid);

        TermTreeDto datasetFeatures = dataSet.getDescriptiveSystem();
        List<DescriptionElementBase> matchingDescriptionElements = new ArrayList<>();

        for (SpecimenDescription specimenDescription : (Set<SpecimenDescription>) specimen.getDescriptions()) {
            specimenDescription = (SpecimenDescription) descriptionService.load(specimenDescription.getUuid());

            //check if description is already added to data set
            if(dataSet.getDescriptionUuids().contains(specimenDescription.getUuid()) ){
                return DescriptionBaseDto.fromDescription(specimenDescription);
            }

            //gather specimen description features and check for match with dataset features
            Set<Feature> specimenDescriptionFeatures = new HashSet<>();
            for (DescriptionElementBase specimenDescriptionElement : specimenDescription.getElements()) {
                Feature feature = specimenDescriptionElement.getFeature();
                specimenDescriptionFeatures.add(feature);
                boolean contains = false;
                for (TermDto featureDto:datasetFeatures.getTerms()){
                    if (featureDto.getUuid().equals(feature.getUuid())){
                        contains = true;
                        break;
                    }
                }
                if(contains && RowWrapperDTO.hasData(specimenDescriptionElement)){
                    matchingDescriptionElements.add(specimenDescriptionElement);
                }
            }
        }
        //Create new specimen description if description has not already been added to the dataset
        SpecimenDescription newDesription = SpecimenDescription.NewInstance(specimen);
        newDesription.setTitleCache("Dataset "+dataSet.getTitleCache()+": "+newDesription.generateTitle(), true); //$NON-NLS-2$

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
            clone = descriptionElementBase.clone(newDesription);
            clone.getSources().forEach(source -> {
                if(descriptionElementBase instanceof CategoricalData){
                    TextData label = new DefaultCategoricalDescriptionBuilder().build((CategoricalData) descriptionElementBase, Arrays.asList(new Language[]{Language.DEFAULT()}));
                    source.setOriginalInfo(label.getText(Language.DEFAULT()));
                }
                else if(descriptionElementBase instanceof QuantitativeData){
                    TextData label = new DefaultQuantitativeDescriptionBuilder().build((QuantitativeData) descriptionElementBase, Arrays.asList(new Language[]{Language.DEFAULT()}));
                    source.setOriginalInfo(label.getText(Language.DEFAULT()));
                }
            });
        }

        //add sources of data set
//        if(addDatasetSource){
//            dataSet.getSources().forEach(source->{
//                try {
//                    newDesription.addSource(source.clone());
//                } catch (CloneNotSupportedException e) {
//                    //nothing
//                }
//            });
//        }
        return DescriptionBaseDto.fromDescription(newDesription);

    }

    //TODO: this should either be solved in the model class itself
    //OR this should cover all possibilities including modifiers for example
    private class DescriptionElementCompareWrapper {

        private DescriptionElementBase element;
        private Set<UUID> stateUuids = new HashSet<>();
        private Set<BigDecimal> avgs = new HashSet<>();
        private Set<BigDecimal> exacts = new HashSet<>();
        private Set<BigDecimal> maxs = new HashSet<>();
        private Set<BigDecimal> mins = new HashSet<>();
        private Set<BigDecimal> sampleSizes = new HashSet<>();
        private Set<BigDecimal> standardDevs = new HashSet<>();
        private Set<BigDecimal> lowerBounds = new HashSet<>();
        private Set<BigDecimal> upperBounds = new HashSet<>();
        private Set<BigDecimal> variances = new HashSet<>();

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

    @Override
    public DescriptiveDataSetBaseDto getDescriptiveDataSetDtoByUuid(UUID uuid) {
        return dao.getDescriptiveDataSetDtoByUuid(uuid);
    }

    @Override
    public TaxonRowWrapperDTO createTaxonRowWrapper(DescriptionBaseDto description, UUID descriptiveDataSetUuid) {
        Classification classification = null;
        DescriptiveDataSet descriptiveDataSet = dao.load(descriptiveDataSetUuid, null);
        Optional<TaxonNode> first = descriptiveDataSet.getTaxonSubtreeFilter().stream()
                .filter(node->node.getClassification()!=null).findFirst();
        Optional<Classification> classificationOptional = first.map(node->node.getClassification());
        Set<DescriptionBaseDto> descriptions = new HashSet<>();
        TaxonNodeDto nodeDto = null;
        if(classificationOptional.isPresent()){
            classification = classificationOptional.get();
            nodeDto = taxonNodeService.dto(description.getTaxonDto().getUuid(), classification.getUuid());
        }

        return new TaxonRowWrapperDTO(description, nodeDto, descriptions);
    }

//    @Override
//    public DescriptionBaseDto findTaxonDescriptionByDescriptionType(UUID dataSetUuid, UUID taxonNodeUuid, DescriptionType descriptionType){
//      DescriptiveDataSetBaseDto dataSet = getDescriptiveDataSetDtoByUuid(dataSetUuid);
//      TaxonNode taxonNode = taxonNodeService.load(taxonNodeUuid);
//      return findTaxonDescriptionByDescriptionType(dataSet, taxonNode.getTaxon(), descriptionType);
//  }

}
