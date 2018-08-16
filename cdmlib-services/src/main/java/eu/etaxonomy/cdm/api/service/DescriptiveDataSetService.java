package eu.etaxonomy.cdm.api.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.dto.RowWrapperDTO;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitorThread;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.DescriptiveSystemRole;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
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
    @Transactional
    public UUID monitGetRowWrapper(DescriptiveDataSet descriptiveDataSet) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                return getRowWrapper(descriptiveDataSet, monitor);
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(3);
        monitorThread.start();
        return uuid;
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
            RowWrapperDTO rowWrapper = createRowWrapper(null, description, descriptiveDataSet);
            if(rowWrapper!=null){
                wrappers.add(rowWrapper);
            }
            monitor.worked(1);
        }
	    return wrappers;
	}

    @Override
    public Collection<SpecimenNodeWrapper> loadSpecimens(DescriptiveDataSet descriptiveDataSet){
        //set filter parameters
        TaxonNodeFilter filter = TaxonNodeFilter.NewRankInstance(descriptiveDataSet.getMinRank(), descriptiveDataSet.getMaxRank());
        descriptiveDataSet.getGeoFilter().forEach(area -> filter.orArea(area.getUuid()));
        descriptiveDataSet.getTaxonSubtreeFilter().forEach(node -> filter.orSubtree(node));
        filter.setIncludeUnpublished(true);

        List<UUID> filteredNodes = taxonNodeService.uuidList(filter);
        return occurrenceService.listUuidAndTitleCacheByAssociatedTaxon(filteredNodes, null, null);
    }

    private TaxonNode findTaxonNodeForDescription(TaxonNode taxonNode, DescriptionBase description){
        List<DerivedUnit> units = occurrenceService.listByAssociatedTaxon(DerivedUnit.class, null, taxonNode.getTaxon(), null, null, null, null, Arrays.asList("descriptions"));
        for (DerivedUnit unit : units) {
            if(unit.getDescriptions().contains(description)){
                return taxonNode;
            }
        }
        return null;
    }

    @Override
    public RowWrapperDTO createRowWrapper(TaxonNode taxonNode, DescriptionBase description, DescriptiveDataSet descriptiveDataSet){
	    SpecimenOrObservationBase specimen = description.getDescribedSpecimenOrObservation();
        FieldUnit fieldUnit = null;
        String identifier = null;
        NamedArea country = null;
        //supplemental information
        if(specimen!=null){
            if(taxonNode==null){
                //get taxon node
                Set<TaxonNode> taxonSubtreeFilter = descriptiveDataSet.getTaxonSubtreeFilter();
                for (TaxonNode node : taxonSubtreeFilter) {
                    //check for node
                    node = taxonNodeService.load(node.getId(), Arrays.asList("taxon"));
                    taxonNode = findTaxonNodeForDescription(node, description);
                    if(taxonNode!=null){
                        break;
                    }
                    else{
                        //check for child nodes
                        List<TaxonNode> allChildren = taxonNodeService.loadChildNodesOfTaxonNode(node, Arrays.asList("taxon"), true, true, null);
                        for (TaxonNode child : allChildren) {
                            taxonNode = findTaxonNodeForDescription(child, description);
                            if(taxonNode!=null){
                                break;
                            }
                        }
                    }
                }
                if(taxonNode==null){
                    return null;
                }
            }
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
            if(specimen instanceof DerivedUnit){
                identifier = occurrenceService.getMostSignificantIdentifier(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class));
            }
            if(fieldUnit!=null && fieldUnit.getGatheringEvent()!=null){
                country = fieldUnit.getGatheringEvent().getCountry();
            }
        }
        return new RowWrapperDTO(description, specimen, taxonNode, fieldUnit, identifier, country);
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

    @Override
    public SpecimenDescription findDescriptionForDescriptiveDataSet(UUID descriptiveDataSetUuid, UUID specimenUuid){
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
//                MessagingUtils.error(CharacterMatrix.class, e);
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
