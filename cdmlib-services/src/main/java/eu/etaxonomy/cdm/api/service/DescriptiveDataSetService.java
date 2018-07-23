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
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
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
            wrappers.add(createRowWrapper(null, description, null, descriptiveDataSet));
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

    @Override
    public RowWrapperDTO createRowWrapper(DescriptionBase description, DescriptiveDataSet descriptiveDataSet){
        return createRowWrapper(null, description, null, descriptiveDataSet);
    }

    @Override
    public RowWrapperDTO createRowWrapper(SpecimenOrObservationBase specimen, DescriptiveDataSet descriptiveDataSet){
        return createRowWrapper(specimen, null, null, descriptiveDataSet);
    }

	private RowWrapperDTO createRowWrapper(SpecimenOrObservationBase specimen, DescriptionBase description, TaxonNode taxonNode, DescriptiveDataSet descriptiveDataSet){
	    if(description!=null){
	        specimen = description.getDescribedSpecimenOrObservation();
	    }
        FieldUnit fieldUnit = null;
        String identifier = null;
        NamedArea country = null;
        //supplemental information
        if(specimen!=null){
            if(taxonNode==null){
                Collection<TaxonBase<?>> associatedTaxa = occurrenceService.listAssociatedTaxa(specimen, null, null, null,
                        Arrays.asList(new String[]{
                                "taxonNodes",
                                "taxonNodes.classification",
                        }));
                if(associatedTaxa!=null){
                    //FIXME: what about multiple associated taxa
                    Set<TaxonNode> taxonSubtreeFilter = descriptiveDataSet.getTaxonSubtreeFilter();
                    if(taxonSubtreeFilter!=null && !taxonSubtreeFilter.isEmpty()){
                        Taxon taxon = HibernateProxyHelper.deproxy(associatedTaxa.iterator().next(), Taxon.class);
                        taxonNode = taxon.getTaxonNode(taxonSubtreeFilter.iterator().next().getClassification());
                    }
                }
            }
            Collection<FieldUnit> fieldUnits = occurrenceService.getFieldUnits(specimen.getUuid(),
                    Arrays.asList(new String[]{
                            "gatheringEvent",
                            "gatheringEvent.country"
                            }));
            if(fieldUnits.size()!=1){
                logger.error("More than one or no field unit found for specimen"); //$NON-NLS-1$
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
            Set<Feature> specimenDescriptionFeatures = new HashSet<>();
            //gather specimen description features and check for match with dataset features
            for (DescriptionElementBase specimenDescriptionElement : specimenDescription.getElements()) {
                Feature feature = specimenDescriptionElement.getFeature();
                specimenDescriptionFeatures.add(feature);
                if(datasetFeatures.contains(feature)){
                    matchingDescriptionElements.add(specimenDescriptionElement);
                }
            }
            //if description with the exact same features is found return the description
            if(specimenDescriptionFeatures.equals(datasetFeatures)){
                return specimenDescription;
            }
        }
        //Create new specimen description if no match was found
        SpecimenDescription newDesription = SpecimenDescription.NewInstance(specimen);
        newDesription.setTitleCache("Dataset"+dataSet.getLabel()+": "+newDesription.generateTitle(), true); //$NON-NLS-2$

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

    private class DescriptionElementCompareWrapper {

        private DescriptionElementBase element;
        private Set<UUID> stateUuids = new HashSet<>();
        private Float min = null;
        private Float max = null;

        public DescriptionElementCompareWrapper(DescriptionElementBase element) {
            this.element = element;
            if(element.isInstanceOf(CategoricalData.class)){
                CategoricalData elementData = (CategoricalData)element;
                elementData.getStatesOnly().forEach(state->stateUuids.add(state.getUuid()));
            }
            else if(element.isInstanceOf(QuantitativeData.class)){
                QuantitativeData elementData = (QuantitativeData)element;
                min = elementData.getMin();
                max = elementData.getMax();
            }
        }

        public DescriptionElementBase unwrap() {
            return element;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((max == null) ? 0 : max.hashCode());
            result = prime * result + ((min == null) ? 0 : min.hashCode());
            result = prime * result + ((stateUuids == null) ? 0 : stateUuids.hashCode());
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
            if (max == null) {
                if (other.max != null) {
                    return false;
                }
            } else if (!max.equals(other.max)) {
                return false;
            }
            if (min == null) {
                if (other.min != null) {
                    return false;
                }
            } else if (!min.equals(other.min)) {
                return false;
            }
            if (stateUuids == null) {
                if (other.stateUuids != null) {
                    return false;
                }
            } else if (!stateUuids.equals(other.stateUuids)) {
                return false;
            }
            return true;
        }
    }

}
