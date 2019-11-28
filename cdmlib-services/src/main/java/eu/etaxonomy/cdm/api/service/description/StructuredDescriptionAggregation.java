/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Aggregates the character data for a given {@link DescriptiveDataSet}.<br>
 * <br>
 * For all {@link SpecimenDescription}s belonging to this data set a new
 * aggregated {@link TaxonDescription}s are created for every taxon the
 * specimens are directly associated with.<BR>
 * Also lower rank taxon descriptions are aggregated to upper rank taxa.
 *
 * @author a.mueller
 * @author p.plitzner
 * @since 03.11.2019
 */
public class StructuredDescriptionAggregation
        extends DescriptionAggregationBase<StructuredDescriptionAggregation, StructuredDescriptionAggregationConfiguration>{

    private DescriptiveDataSet dataSet;

    @Override
    protected String pluralDataType(){
        return "structured descriptive data";
    }

    @Override
    protected void preAccumulate() {
        subTask("preAccumulate - nothing to do");

        // take start time for performance testing
        double start = System.currentTimeMillis();

        dataSet = getDescriptiveDatasetService().load(getConfig().getDatasetUuid());

        double end1 = System.currentTimeMillis();
        logger.info("Time elapsed for pre-accumulate() : " + (end1 - start) / (1000) + "s");
    }


//    @Override
//    protected UpdateResult doInvoke(){
//        TransactionStatus transactionStatus = getRepository().startTransaction(false);
//
//        UpdateResult result = super.getResult();
//
//        DescriptiveDataSet dataSet = loadDataSet(getConfig().getDatasetUuid());
//        Set<DescriptionBase> dataSetDescriptions = dataSet.getDescriptions();
//
//        beginTask("Aggregate data set", dataSetDescriptions.size()*2);
//
//        result.setCdmEntity(dataSet);
//
//        subTask("Remove existing aggregations from dataset");
//        //TODO AM memory loading of all descriptions not possible
//        //TODO why not reusing descriptions and only adding new data and new sources
//        // delete all aggregation description of this dataset (DescriptionType.AGGREGATED)
//        Set<TaxonDescription> aggregations = dataSet.getDescriptions().stream()
//                .filter(aggDesc->aggDesc instanceof TaxonDescription)
//                .map(aggDesc->(TaxonDescription)aggDesc)
//                .filter(desc -> desc.isAggregatedStructuredDescription())
//                .collect(Collectors.toSet());
//        aggregations.forEach(aggregation->dataSet.removeDescription(aggregation));
//
//        subTask("Delete cloned sources");
//        // also delete all their cloned source descriptions
//        Set<String> sourceUuids = aggregations.stream()
//                .flatMap(aggDesc->aggDesc.getSources().stream())
//                .filter(source->source.getType().equals(OriginalSourceType.Aggregation))
//                .map(aggSource->aggSource.getIdInSource())
//                .collect(Collectors.toSet());
//
//        for (String string : sourceUuids) {
//            try {
//                UUID uuid = UUID.fromString(string);
//                DescriptionBase<?> sourceClone = getDescriptionService().load(uuid);
//                getDescriptionService().deleteDescription(sourceClone);
//            } catch (IllegalArgumentException|NullPointerException e) {
//                // ignore
//            }
//        }
//
//        subTask("Remove existing aggregations from database");
//        //finally delete the aggregation description itself
//        aggregations.forEach(aggDesc->getDescriptionService().delete(aggDesc));
//
//        // START Aggregation
//
//        // sort descriptions by taxa
//        Map<TaxonNode, Set<UUID>> taxonNodeToSpecimenDescriptionMap = new HashMap<>();
//        for (DescriptionBase<?> descriptionBase : dataSetDescriptions) {
//            subTask("Aggregate description " + descriptionBase.getTitleCache());
//            if(getConfig().getMonitor().isCanceled()){
//                result.setAbort();
//                return result;
//            }
//
//            if(descriptionBase instanceof SpecimenDescription){
//                SpecimenDescription specimenDescription = CdmBase.deproxy(descriptionBase, SpecimenDescription.class);
//                if(specimenDescription.getElements().stream().anyMatch(element->hasCharacterData(element))){
//                    //TODO AM why taxon node and not only taxon
//                    TaxonNode taxonNode = findTaxonNodeForDescription(specimenDescription, dataSet);
//                    if(taxonNode!=null){
//                        addDescriptionToTaxonNodeMap(specimenDescription.getUuid(), taxonNode, taxonNodeToSpecimenDescriptionMap);
//                    }
//                }
//            }
//            worked(1);
//        }
//
//        //aggregate to higher taxa
//        if(getConfig().isAggregateToHigherRanks()){
//            propagateDescriptionsToParentNodes(dataSet, taxonNodeToSpecimenDescriptionMap);
//        }
//
//        // aggregate per taxon
//        Map<UUID, SpecimenDescription> specimenToClonedSourceDescription = new HashMap<>();
//        for (TaxonNode node: taxonNodeToSpecimenDescriptionMap.keySet()) {
//            if(getConfig().getMonitor().isCanceled()){
//                result.setAbort();
//                return result;
//            }
//            subTask("Aggregate taxon " + node.getTaxon().getTitleCache());
//
//            UUID taxonUuid = node.getTaxon().getUuid();
//            Set<UUID> specimenDescriptionUuids = taxonNodeToSpecimenDescriptionMap.get(node);
//            UpdateResult aggregationResult = aggregateDescription(taxonUuid,
//                    specimenDescriptionUuids, getConfig().getDatasetUuid(),
//                    specimenToClonedSourceDescription);
//            result.includeResult(aggregationResult);
//            worked(1);
//        }
//
//        //done
//        done();
//        getRepository().commitTransaction(transactionStatus);
//        return result;
//    }

    private DescriptiveDataSet loadDataSet(UUID uuid) {
        return getDescriptiveDatasetService().load(uuid);
    }

    private boolean hasCharacterData(DescriptionElementBase element) {
        return (element instanceof CategoricalData && !((CategoricalData) element).getStatesOnly().isEmpty())
                || (element instanceof QuantitativeData
                        && !((QuantitativeData) element).getStatisticalValues().isEmpty());
    }

    private TaxonNode findTaxonNodeForDescription(SpecimenDescription description,
            DescriptiveDataSet descriptiveDataSet){

        SpecimenOrObservationBase<?> specimen = description.getDescribedSpecimenOrObservation();

        Set<DescriptionBase<?>> descriptions = (Set)descriptiveDataSet.getDescriptions();
        //get taxon node
        @SuppressWarnings("rawtypes") //on linux the code does not compile if the stream result is not explicitly casted to Set<IndividualsAssociation>, on windows the cast is automatically removed during save due to group code settings, therefore this workaround
        Set elements = descriptions
                .stream()
                .flatMap(desc->desc.getElements().stream())// put all description element in one stream
                .filter(element->element instanceof IndividualsAssociation)
                .map(ia->(IndividualsAssociation)ia)
                .collect(Collectors.toSet());
        @SuppressWarnings({"unchecked"})
        Set<IndividualsAssociation> associations = elements;
        Classification classification = descriptiveDataSet.getTaxonSubtreeFilter().iterator().next().getClassification();
        for (IndividualsAssociation individualsAssociation : associations) {
            if(individualsAssociation.getAssociatedSpecimenOrObservation().equals(specimen)){
                return ((TaxonDescription) individualsAssociation.getInDescription()).getTaxon().getTaxonNode(classification);
            }
        }
        return null;
    }

    private void addDescriptionToTaxonNodeMap(UUID descriptionUuid, TaxonNode taxonNode, Map<TaxonNode, Set<UUID>> taxonNodeToSpecimenDescriptionMap){
        Set<UUID> specimenDescriptionUuids = taxonNodeToSpecimenDescriptionMap.get(taxonNode);
        if(specimenDescriptionUuids==null){
            specimenDescriptionUuids = new HashSet<>();
            taxonNodeToSpecimenDescriptionMap.put(taxonNode, specimenDescriptionUuids);
        }
        specimenDescriptionUuids.add(descriptionUuid);
    }

    private void propagateDescriptionsToParentNodes(DescriptiveDataSet dataSet, Map<TaxonNode, Set<UUID>> taxonNodeToSpecimenDescriptionMap){
        Map<TaxonNode, Set<UUID>> parentMap = new HashMap<>();
        for (TaxonNode node : taxonNodeToSpecimenDescriptionMap.keySet()) {
            Set<UUID> descriptionUuids = taxonNodeToSpecimenDescriptionMap.get(node);
            TaxonNode parentNode = node.getParent();
            while(parentNode != null && isTaxonNodeInDescriptiveDataSet(parentNode, dataSet)){
                for (UUID uuid : descriptionUuids) {
                    addDescriptionToTaxonNodeMap(uuid, parentNode, parentMap);
                }
                parentNode = parentNode.getParent();
            }
        }
        // merge parent map
        for (TaxonNode node: parentMap.keySet()) {
            Set<UUID> descriptionUuids = parentMap.get(node);
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
            //TODO FIXME AM use tree index or TaxonNodeFilter(?)
            List<TaxonNode> allChildren = getTaxonNodeService().loadChildNodesOfTaxonNode(datasetNode, null, true, true, null);
            for (TaxonNode childNode : allChildren) {
                if(childNode.getUuid().equals(taxonNode.getUuid())){
                    return true;
                }
            }
        }
        return false;
    }

    private UpdateResult aggregateDescription(UUID taxonUuid, Set<UUID> specimenDescriptionUuids,
            UUID descriptiveDataSetUuid, Map<UUID, SpecimenDescription> specimenToClonedSourceDescription) {

        UpdateResult result = new UpdateResult();

        TaxonBase<?> taxonBase = getTaxonService().load(taxonUuid);
        if(!(taxonBase instanceof Taxon)){
            result.addException(new ClassCastException("The given taxonUUID does not belong to an accepted taxon"));
            result.setError();
            return result;
        }
        Taxon taxon = (Taxon)taxonBase;
        List<DescriptionBase> descriptions = getDescriptionService().load(new ArrayList<>(specimenDescriptionUuids), null);
        List<SpecimenDescription> specimenDescriptions = descriptions.stream()
                .filter(d -> d instanceof SpecimenDescription)
                .map(d -> (SpecimenDescription) d)
                .collect(Collectors.toList());

        //TODO AM why loading again, is already loaded in transaction
        DescriptiveDataSet dataSet = loadDataSet(descriptiveDataSetUuid);
        if(dataSet == null){
            result.addException(new IllegalArgumentException("Could not find data set for uuid "+descriptiveDataSetUuid));
            result.setAbort();
            return result;
        }

        //extract all character description elements
        Map<Feature, List<DescriptionElementBase>> featureToElementMap = new HashMap<>();
        for (DescriptionBase<?> description : specimenDescriptions) {
            description.getElements().stream()
                //TODO AM do we really only allow Characters, no features?
                //filter out elements that do not have a Character as Feature
//                .filter(element->HibernateProxyHelper.isInstanceOf(element.getFeature(), Character.class))
                .forEach(ele->addCharacterToMap(featureToElementMap, ele));
        }

        TaxonDescription aggregationDescription = createNewDescription(taxon, dataSet);

        aggregateCharacterData(featureToElementMap, aggregationDescription);

        // add sources to aggregation description
        // create a snapshot of those descriptions that were used to create the aggregated descriptions
        specimenDescriptions.forEach(specimenDescription -> addSourceDescription(aggregationDescription, specimenDescription,
                specimenToClonedSourceDescription));

        result.addUpdatedObject(taxon);
        result.addUpdatedObject(aggregationDescription);

        return result;
    }

    private void aggregateCharacterData(Map<Feature, List<DescriptionElementBase>> featureToElementMap,
            TaxonDescription aggregationDescription) {
        for(Feature feature:featureToElementMap.keySet()){
            List<DescriptionElementBase> elements = featureToElementMap.get(feature);
            //aggregate categorical data
            if(feature.isSupportsCategoricalData()){
                aggregateCategoricalData(aggregationDescription, feature, elements);
            }
            //aggregate quantitative data
            else if(feature.isSupportsQuantitativeData()){
                aggregateQuantitativeData(aggregationDescription, feature, elements);
            }
        }
    }

    private TaxonDescription createNewDescription(Taxon taxon, DescriptiveDataSet dataSet) {
        TaxonDescription aggregationDescription = TaxonDescription.NewInstance(taxon);
        aggregationDescription.setTitleCache(dataSet.getTitleCache(), true);
        aggregationDescription.getTypes().add(DescriptionType.AGGREGATED_STRUC_DESC);
        aggregationDescription.addSource(IdentifiableSource.NewInstance(OriginalSourceType.Aggregation));
        aggregationDescription.addDescriptiveDataSet(dataSet);
        return aggregationDescription;
    }

    private void addSourceDescription(TaxonDescription taxonDescription, SpecimenDescription specimenDescription,
            Map<UUID, SpecimenDescription> specimenToClonedSourceDescription) {
        SpecimenDescription sourceClone = specimenToClonedSourceDescription.get(specimenDescription.getUuid());
        if(sourceClone!=null){
            taxonDescription.addAggregationSource(sourceClone);
        }
        else{
            SpecimenOrObservationBase<?> specimenOrObservation = specimenDescription.getDescribedSpecimenOrObservation();
            SpecimenDescription clone = (SpecimenDescription) specimenDescription.clone();
            clone.getTypes().add(DescriptionType.CLONE_FOR_SOURCE);
            specimenOrObservation.addDescription(clone);

            taxonDescription.addAggregationSource(clone);
            specimenToClonedSourceDescription.put(specimenDescription.getUuid(), clone);
        }
    }


    private void aggregateQuantitativeData(TaxonDescription description, Feature character,
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
        if(min.isPresent() || max.isPresent() || avg.isPresent()){
            description.addElement(aggregate);
        }
    }

    private void aggregateCategoricalData(TaxonDescription description, Feature character,
            List<DescriptionElementBase> elements) {
        Map<State, Integer> stateToCountMap = new HashMap<>();
        CategoricalData aggregate = CategoricalData.NewInstance(character);
        for (DescriptionElementBase element: elements) {
            if(element instanceof CategoricalData){
                CategoricalData categoricalData = (CategoricalData)element;
                List<StateData> stateDataList = categoricalData.getStateData();
                for (StateData stateData : stateDataList) {
                    State state = stateData.getState();
                    Integer integer = stateToCountMap.get(state);
                    if(integer==null){
                        integer = 1;
                    }
                    else{
                        integer++;
                    }
                    stateToCountMap.put(state, integer);
                }
            }
        }
        stateToCountMap.forEach((state, count) -> {
            StateData stateData = StateData.NewInstance(state);
            stateData.setCount(count);
            aggregate.addStateData(stateData);
        });

        if(!aggregate.getStatesOnly().isEmpty()){
            description.addElement(aggregate);
        }
    }

    private void addCharacterToMap(Map<Feature, List<DescriptionElementBase>> featureToElementMap,
            DescriptionElementBase descriptionElement) {
        List<DescriptionElementBase> list = featureToElementMap.get(descriptionElement.getFeature());
        if(list==null){
            list = new ArrayList<>();
            featureToElementMap.put(HibernateProxyHelper.deproxy(descriptionElement.getFeature(), Feature.class), list);
        }
        list.add(descriptionElement);
    }

    @Override
    protected void setDescriptionTitle(TaxonDescription description, Taxon taxon) {
        String title = taxon.getName() != null? taxon.getName().getTitleCache() : taxon.getTitleCache();
        description.setTitleCache("Aggregated description for " + title, true);
        return;
    }

    @Override
    protected TaxonDescription createNewDescription(Taxon taxon) {
        String title = taxon.getTitleCache();
        logger.debug("creating new description for " + title);
        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        description.addType(DescriptionType.AGGREGATED_STRUC_DESC);
        setDescriptionTitle(description, taxon);
        return description;
    }

    @Override
    protected boolean hasDescriptionType(TaxonDescription description) {
        return description.isAggregatedStructuredDescription();
    }



    @Override
    protected List<String> descriptionInitStrategy() {
        return new ArrayList<>();
    }

    @Override
    protected void addAggregationResultToDescription(TaxonDescription targetDescription,
            ResultHolder resultHolder) {
        StructuredDescriptionResultHolder structuredResultHolder = (StructuredDescriptionResultHolder)resultHolder;
        structuredResultHolder.categoricalMap.forEach((key, value)->targetDescription.addElement(value));
        structuredResultHolder.quantitativeMap.entrySet().stream()
        .forEach(entry->targetDescription.addElement(convertStatisticalSummaryValuesToQuantitativeData(entry.getKey(), entry.getValue())));
    }

    @Override
    protected void aggregateToParentTaxon(TaxonNode taxonNode,
            ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions) {
        StructuredDescriptionResultHolder descriptiveResultHolder = (StructuredDescriptionResultHolder)resultHolder;
//        Set<SpecimenDescription> specimenDescriptions = getTaxonDescriptions(taxonNode);
//        // TODO AM only basic
//        for (SpecimenDescription desc:specimenDescriptions){
//            for (DescriptionElementBase deb:desc.getElements()){
//                if (deb.isCharacterData()){
//                    if (deb.isInstanceOf(CategoricalData.class)){
//                        addToCategorical(CdmBase.deproxy(deb, CategoricalData.class), descriptiveResultHolder);
//                    }else if (deb.isInstanceOf(QuantitativeData.class)){
//                        addToQuantitative(CdmBase.deproxy(deb, QuantitativeData.class), descriptiveResultHolder);
//                    }
//                }
//            }
//        }

    }

    @Override
    protected void aggregateWithinSingleTaxon(Taxon taxon,
            ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions) {
        StructuredDescriptionResultHolder descriptiveResultHolder = (StructuredDescriptionResultHolder)resultHolder;
        Set<SpecimenDescription> specimenDescriptions = getSpecimenDescriptions(taxon);
        // TODO AM only basic
        for (SpecimenDescription desc:specimenDescriptions){
            for (DescriptionElementBase deb:desc.getElements()){
                if (hasCharacterData(deb)){
                    if (deb.isInstanceOf(CategoricalData.class)){
                        addToCategorical(CdmBase.deproxy(deb, CategoricalData.class), descriptiveResultHolder);
                    }else if (deb.isInstanceOf(QuantitativeData.class)){
                        addToQuantitative(CdmBase.deproxy(deb, QuantitativeData.class), descriptiveResultHolder);
                    }
                }
            }
        }
    }

    private void addToQuantitative(QuantitativeData qd, StructuredDescriptionResultHolder resultHolder) {
        StatisticalSummaryValues aggregatedQuantitativeData = resultHolder.quantitativeMap.get(qd.getFeature());
        if(aggregatedQuantitativeData==null){
            // no QuantitativeData with this feature in aggregation
            aggregatedQuantitativeData = mergeQuantitativeData(convertQuantitativeDataToSummaryStatistics(qd));
        }
        else{
            aggregatedQuantitativeData = mergeQuantitativeData(aggregatedQuantitativeData, convertQuantitativeDataToSummaryStatistics(qd));
        }
        resultHolder.quantitativeMap.put(qd.getFeature(), aggregatedQuantitativeData);
    }

    private StatisticalSummaryValues mergeQuantitativeData(StatisticalSummary... summaryStatistics) {
        Collection<StatisticalSummary> statistics = new ArrayList<>();
        for (StatisticalSummary statistic : summaryStatistics) {
            statistics.add(statistic);
        }
        return AggregateSummaryStatistics.aggregate(statistics);
    }

    private QuantitativeData convertStatisticalSummaryValuesToQuantitativeData(Feature feature,
            StatisticalSummaryValues aggregate) {
        QuantitativeData aggregatedQuantitativeData = QuantitativeData.NewInstance(feature);
        aggregatedQuantitativeData.setMinimum(new Float(aggregate.getMin()), null);
        aggregatedQuantitativeData.setMaximum(new Float(aggregate.getMax()), null);
        aggregatedQuantitativeData.setAverage(new Float(aggregate.getMean()), null);
        aggregatedQuantitativeData.setStandardDeviation(new Float(aggregate.getStandardDeviation()), null);
        aggregatedQuantitativeData.setSampleSize(new Float(aggregate.getN()), null);
        return aggregatedQuantitativeData;
    }

    private StatisticalSummary convertQuantitativeDataToSummaryStatistics(QuantitativeData qd) {
        SummaryStatistics summaryStatistics = new SummaryStatistics();
        qd.getStatisticalValues().stream()
                .filter(value->value.getType().equals(StatisticalMeasure.EXACT_VALUE()))
                .forEach(exact->summaryStatistics.addValue(exact.getValue()));
        if(qd.getMin()!=null){
            summaryStatistics.addValue(qd.getMin());
        }
        if(qd.getMax()!=null){
            summaryStatistics.addValue(qd.getMax());
        }
        return summaryStatistics;
    }

    private void addToCategorical(CategoricalData cd, StructuredDescriptionResultHolder resultHolder) {
        CategoricalData aggregatedCategoricalData = resultHolder.categoricalMap.get(cd.getFeature());
        if(aggregatedCategoricalData==null){
            // no CategoricalData with this feature in aggregation
            aggregatedCategoricalData = (CategoricalData) cd.clone();
            // set count to 1 if not set
            aggregatedCategoricalData.getStateData().stream().filter(sd->sd.getCount()==null).forEach(sd->sd.incrementCount());
            resultHolder.categoricalMap.put(aggregatedCategoricalData.getFeature(), aggregatedCategoricalData);
        }
        else{
            // split all StateData into those where the state already exists and those where it doesn't
            List<State> statesOnly = aggregatedCategoricalData.getStatesOnly();
            List<StateData> sdWithExistingStateInAggregation = cd.getStateData().stream().filter(sd->statesOnly.contains(sd.getState())).collect(Collectors.toList());
            List<StateData> sdWithNoExistingStateInAggregation = cd.getStateData().stream().filter(sd->!statesOnly.contains(sd.getState())).collect(Collectors.toList());

            for (StateData sd : sdWithNoExistingStateInAggregation) {
                StateData clone = (StateData) sd.clone();
                // set count to 1 if not set
                if(clone.getCount()==null){
                    clone.incrementCount();
                }
                aggregatedCategoricalData.addStateData(clone);
            }

            for (StateData sdExist : sdWithExistingStateInAggregation) {
                aggregatedCategoricalData.getStateData().stream()
                .filter(sd->hasSameState(sdExist, sd))
                .forEach(sd->sd.incrementCount());
            }
        }
    }

    private boolean hasSameState(StateData sd1, StateData sd2) {
        return sd2.getState().getUuid().equals(sd1.getState().getUuid());
    }

    private Set<SpecimenDescription> getSpecimenDescriptions(Taxon taxon) {
        Set<SpecimenDescription> result = new HashSet<>();
        for (TaxonDescription taxonDesc: taxon.getDescriptions()){
            for (DescriptionElementBase taxonDeb : taxonDesc.getElements()){
                if (taxonDeb.isInstanceOf(IndividualsAssociation.class)){
                    IndividualsAssociation indAss = CdmBase.deproxy(taxonDeb, IndividualsAssociation.class);
                    SpecimenOrObservationBase<?> spec = indAss.getAssociatedSpecimenOrObservation();
                     Set<SpecimenDescription> descriptions = (Set)spec.getDescriptions();
                     for(SpecimenDescription specimenDescription : descriptions){
                         if(dataSet.getDescriptions().contains(specimenDescription)){
                             result.add(specimenDescription);
                         }
                     }
                }
            }
        }
        return result;
    }

    @Override
    protected StructuredDescriptionResultHolder createResultHolder() {
        return new StructuredDescriptionResultHolder();
    }

    private class StructuredDescriptionResultHolder implements ResultHolder{
        Map<Feature, CategoricalData> categoricalMap = new HashMap<>();
        Map<Feature, StatisticalSummaryValues> quantitativeMap = new HashMap<>();
    }

}
