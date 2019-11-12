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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
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
 * @author a.mueller
 * @since 03.11.2019
 */
public class StructuredDescriptionAggregation
        extends DescriptionAggregationBase<StructuredDescriptionAggregation, StructuredDescriptionAggregationConfiguration>{

    @Override
    protected UpdateResult doInvoke(){
        TransactionStatus transactionStatus = getRepository().startTransaction(false);

        UpdateResult result = new UpdateResult();

        DescriptiveDataSet dataSet = loadDataSet(getConfig().getDataset().getUuid());
        Set<DescriptionBase> descriptions = dataSet.getDescriptions();

        beginTask("Aggregate data set", descriptions.size()*2);

        result.setCdmEntity(dataSet);

        //TODO AM memory loading of all descriptions not possible
        //TODO why not reusing descriptions and only adding new data and new sources
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
                DescriptionBase<?> sourceClone = getDescriptionService().load(uuid);
                getDescriptionService().deleteDescription(sourceClone);
            } catch (IllegalArgumentException|NullPointerException e) {
                // ignore
            }
        }

        //finally delete the aggregation description itself
        aggregations.forEach(aggDesc->getDescriptionService().delete(aggDesc));

        // sort descriptions by taxa
        Map<TaxonNode, Set<UUID>> taxonNodeToSpecimenDescriptionMap = new HashMap<>();
        for (DescriptionBase<?> descriptionBase : descriptions) {
            if(getConfig().getMonitor().isCanceled()){
                result.setAbort();
                return result;
            }

            if(descriptionBase instanceof SpecimenDescription){
                SpecimenDescription specimenDescription = CdmBase.deproxy(descriptionBase, SpecimenDescription.class);
                if(specimenDescription.getElements().stream().anyMatch(element->hasCharacterData(element))){
                    //TODO AM why taxon node and not only taxon
                    TaxonNode taxonNode = findTaxonNodeForDescription(specimenDescription, dataSet);
                    if(taxonNode!=null){
                        addDescriptionToTaxonNodeMap(specimenDescription.getUuid(), taxonNode, taxonNodeToSpecimenDescriptionMap);
                    }
                }
            }
            getConfig().getMonitor().worked(1);
        }

        //aggregate to higher taxa
        if(getConfig().isAggregateToHigherRanks()){
            propagateDescriptionsToParentNodes(dataSet, taxonNodeToSpecimenDescriptionMap);
        }

        // aggregate per taxon
        Map<UUID, SpecimenDescription> specimenToClonedSourceDescription = new HashMap<>();
        for (TaxonNode node: taxonNodeToSpecimenDescriptionMap.keySet()) {
            if(getConfig().getMonitor().isCanceled()){
                result.setAbort();
                return result;
            }
            UUID taxonUuid = node.getTaxon().getUuid();
            Set<UUID> specimenDescriptionUuids = taxonNodeToSpecimenDescriptionMap.get(node);
            UpdateResult aggregationResult = aggregateDescription(taxonUuid,
                    specimenDescriptionUuids, getConfig().getDataset().getUuid(),
                    specimenToClonedSourceDescription);
            result.includeResult(aggregationResult);
            getConfig().getMonitor().worked(1);
        }

        //done
        getConfig().getMonitor().done();
        getRepository().commitTransaction(transactionStatus);
        return result;
    }

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
                    addDescriptionToTaxonNodeMap(uuid, node.getParent(), parentMap);
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
        Map<Character, List<DescriptionElementBase>> featureToElementMap = new HashMap<>();
        for (DescriptionBase<?> description : specimenDescriptions) {
            description.getElements().stream()
                //TODO AM do we really only allow Characters, no features?
                //filter out elements that do not have a Character as Feature
                .filter(element->HibernateProxyHelper.isInstanceOf(element.getFeature(), Character.class))
                .forEach(ele->addCharacterToMap(featureToElementMap, ele));
        }

        TaxonDescription aggregationDescription = createAggregationDescription(taxon, dataSet);

        aggregateCharacterData(featureToElementMap, aggregationDescription);

        // add sources to aggregation description
        // create a snapshot of those descriptions that were used to create the aggregated descriptions
        specimenDescriptions.forEach(specimenDescription -> addSourceDescription(aggregationDescription, specimenDescription,
                specimenToClonedSourceDescription));

        result.addUpdatedObject(taxon);
        result.addUpdatedObject(aggregationDescription);

        return result;
    }

    private void aggregateCharacterData(Map<Character, List<DescriptionElementBase>> featureToElementMap,
            TaxonDescription aggregationDescription) {
        for(Character character:featureToElementMap.keySet()){
            List<DescriptionElementBase> elements = featureToElementMap.get(character);
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
        aggregationDescription.setTitleCache(dataSet.getTitleCache(), true);
        aggregationDescription.getTypes().add(DescriptionType.AGGREGATED);
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

        description.addElement(aggregate);
    }

    private void addCharacterToMap(Map<Character, List<DescriptionElementBase>> featureToElementMap,
            DescriptionElementBase descriptionElement) {
        List<DescriptionElementBase> list = featureToElementMap.get(descriptionElement.getFeature());
        if(list==null){
            list = new ArrayList<>();
            featureToElementMap.put(HibernateProxyHelper.deproxy(descriptionElement.getFeature(), Character.class), list);
        }
        list.add(descriptionElement);
    }

    @Override
    protected UpdateResult invokeOnSingleTaxon() {
        return null;
    }


    @Override
    protected UpdateResult invokeHigherRankAggregation() {
        return null;
    }

    @Override
    protected UpdateResult removeExistingAggregationOnTaxon() {
        return null;
    }

}
