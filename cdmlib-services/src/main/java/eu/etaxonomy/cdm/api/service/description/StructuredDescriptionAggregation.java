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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
import eu.etaxonomy.cdm.model.taxon.Taxon;
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

        getResult().setCdmEntity(getDescriptiveDatasetService().load(getConfig().getDatasetUuid()));

        double end1 = System.currentTimeMillis();
        logger.info("Time elapsed for pre-accumulate() : " + (end1 - start) / (1000) + "s");
    }


    private boolean hasCharacterData(DescriptionElementBase element) {
        return hasCategoricalData(element) || hasQuantitativeData(element);
    }

    private boolean hasQuantitativeData(DescriptionElementBase element) {
        if(element instanceof QuantitativeData
                && !((QuantitativeData) element).getStatisticalValues().isEmpty()){
            QuantitativeData quantitativeData = (QuantitativeData)element;
            return !getExactValues(quantitativeData).isEmpty()
                    || (quantitativeData.getMin()!=null && quantitativeData.getMax()!=null);
        }
        return false;
    }

    private boolean hasCategoricalData(DescriptionElementBase element) {
        return element instanceof CategoricalData && !((CategoricalData) element).getStatesOnly().isEmpty();
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
        return dataSet.getDescriptions().contains(description) && description.isAggregatedStructuredDescription();
    }

    @Override
    protected List<String> descriptionInitStrategy() {
        return new ArrayList<>();
    }

    @Override
    protected void addAggregationResultToDescription(TaxonDescription targetDescription,
            ResultHolder resultHolder) {
        StructuredDescriptionResultHolder structuredResultHolder = (StructuredDescriptionResultHolder)resultHolder;

        replaceExistingDescriptionElements(targetDescription, structuredResultHolder.categoricalMap);
        replaceExistingDescriptionElements(targetDescription, structuredResultHolder.quantitativeMap);
        addAggregationSources(targetDescription, structuredResultHolder);

        if(!targetDescription.getElements().isEmpty()){
            dataSet.addDescription(targetDescription);
        }
    }

    private void addAggregationSources(TaxonDescription targetDescription,
            StructuredDescriptionResultHolder structuredResultHolder) {
        //FIXME Re-use sources if possible
        //Remove sources from description
        Set<IdentifiableSource> sourcesToRemove = targetDescription.getSources().stream()
                .filter(source->source.getType().equals(OriginalSourceType.Aggregation))
                .collect(Collectors.toSet());

        for (IdentifiableSource source : sourcesToRemove) {
            targetDescription.removeSource(source);
        }

        Set<DescriptionBase> sourceDescriptions = structuredResultHolder.sourceDescriptions;
        for (DescriptionBase descriptionBase : sourceDescriptions) {
            DescriptionBase sourceDescription = null;
            if(descriptionBase.isInstanceOf(SpecimenDescription.class)){
                DescriptionBase clone = (DescriptionBase)descriptionBase.clone();
                clone.removeDescriptiveDataSet(dataSet);
                clone.getTypes().add(DescriptionType.CLONE_FOR_SOURCE);
                SpecimenOrObservationBase specimen = CdmBase.deproxy(descriptionBase, SpecimenDescription.class).getDescribedSpecimenOrObservation();
                specimen.addDescription(CdmBase.deproxy(clone, SpecimenDescription.class));
                sourceDescription=clone;
            }
            else if(descriptionBase.isInstanceOf(TaxonDescription.class)){
                Taxon taxon = CdmBase.deproxy(descriptionBase, TaxonDescription.class).getTaxon();
                taxon.addDescription(CdmBase.deproxy(descriptionBase, TaxonDescription.class));
                sourceDescription=descriptionBase;
            }
            if(sourceDescription!=null){
                targetDescription.addAggregationSource(sourceDescription);
            }
        }
    }

    private void replaceExistingDescriptionElements(TaxonDescription targetDescription,
            Map<Feature, ? extends DescriptionElementBase> elementMap) {
        for (Entry<Feature, ? extends DescriptionElementBase> entry : elementMap.entrySet()) {
            DescriptionElementBase elementToRemove = null;
            DescriptionElementBase elementReplacement = null;
            for (DescriptionElementBase descriptionElementBase : targetDescription.getElements()) {
                if(descriptionElementBase.getFeature().equals(entry.getKey())){
                    elementToRemove = descriptionElementBase;
                    elementReplacement = entry.getValue();
                    break;
                }
            }
            if(elementToRemove!=null && elementReplacement!=null){
                targetDescription.removeElement(elementToRemove);
                targetDescription.addElement(elementReplacement);
            }
            else{
                targetDescription.addElement(entry.getValue());
            }
        }
    }

    @Override
    protected void initTransaction() {
        dataSet = getDescriptiveDatasetService().load(getConfig().getDatasetUuid());
    }

    @Override
    protected void removeDescriptionIfEmpty(TaxonDescription description) {
        super.removeDescriptionIfEmpty(description);
        if (description.getElements().isEmpty()){
            dataSet.removeDescription(description);
        }
    }

    @Override
    protected void aggregateToParentTaxon(TaxonNode taxonNode,
            ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions) {
        StructuredDescriptionResultHolder descriptiveResultHolder = (StructuredDescriptionResultHolder)resultHolder;
        addDescriptionElement(descriptiveResultHolder, getChildTaxonDescriptions(taxonNode, dataSet));
    }

    @Override
    protected void aggregateWithinSingleTaxon(Taxon taxon,
            ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions) {
        StructuredDescriptionResultHolder descriptiveResultHolder = (StructuredDescriptionResultHolder)resultHolder;
        addDescriptionElement(descriptiveResultHolder, getSpecimenDescriptions(taxon, dataSet));
    }

    private void addDescriptionElement(StructuredDescriptionResultHolder descriptiveResultHolder,
            Set<? extends DescriptionBase> descriptions) {
        boolean descriptionWasUsed = false;
        for (DescriptionBase desc:descriptions){
            for (DescriptionElementBase deb:(Set<DescriptionElementBase>)desc.getElements()){
                if (hasCharacterData(deb)){
                    if (deb.isInstanceOf(CategoricalData.class)){
                        addToCategorical(CdmBase.deproxy(deb, CategoricalData.class), descriptiveResultHolder);
                        descriptionWasUsed = true;
                    }else if (deb.isInstanceOf(QuantitativeData.class)){
                        addToQuantitative(CdmBase.deproxy(deb, QuantitativeData.class), descriptiveResultHolder);
                        descriptionWasUsed = true;
                    }
                }
            }
            if(descriptionWasUsed){
                descriptiveResultHolder.sourceDescriptions.add(desc);
            }
        }
    }

    private void addToQuantitative(QuantitativeData qd, StructuredDescriptionResultHolder resultHolder) {
        QuantitativeData aggregatedQuantitativeData = resultHolder.quantitativeMap.get(qd.getFeature());
        if(aggregatedQuantitativeData==null){
            // no QuantitativeData with this feature in aggregation
            aggregatedQuantitativeData = aggregateSingleQuantitativeData(qd);
        }
        else{
            aggregatedQuantitativeData = mergeQuantitativeData(aggregatedQuantitativeData, qd);
        }
        resultHolder.quantitativeMap.put(qd.getFeature(), aggregatedQuantitativeData);
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
                List<StateData> aggregatedSameStateData = aggregatedCategoricalData.getStateData().stream()
                .filter(sd->hasSameState(sdExist, sd))
                .collect(Collectors.toList());
                for (StateData stateData : aggregatedSameStateData) {
                    if(sdExist.getCount()==null){
                        stateData.incrementCount();
                    }
                    else{
                        stateData.setCount(stateData.getCount()+sdExist.getCount());
                    }
                }
            }
        }
    }

    @Override
    protected StructuredDescriptionResultHolder createResultHolder() {
        return new StructuredDescriptionResultHolder();
    }

    private class StructuredDescriptionResultHolder implements ResultHolder{
        Map<Feature, CategoricalData> categoricalMap = new HashMap<>();
        Map<Feature, QuantitativeData> quantitativeMap = new HashMap<>();
        Set<DescriptionBase> sourceDescriptions = new HashSet<>();
    }

    /*
     * Static utility methods
     */
    private static Set<TaxonDescription> getChildTaxonDescriptions(TaxonNode taxonNode, DescriptiveDataSet dataSet) {
        Set<TaxonDescription> result = new HashSet<>();
        List<TaxonNode> childNodes = taxonNode.getChildNodes();
        for (TaxonNode childNode : childNodes) {
            result.addAll(childNode.getTaxon().getDescriptions().stream()
            .filter(desc->desc.getTypes().contains(DescriptionType.AGGREGATED_STRUC_DESC))
            .filter(desc->dataSet.getDescriptions().contains(desc))
            .collect(Collectors.toSet()));
        }
        return result;
    }

    private static Set<SpecimenDescription> getSpecimenDescriptions(Taxon taxon, DescriptiveDataSet dataSet) {
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

    private static QuantitativeData aggregateSingleQuantitativeData(QuantitativeData qd){
        QuantitativeData aggQD = QuantitativeData.NewInstance(qd.getFeature());
        List<Float> exactValues = getExactValues(qd);
        if(!exactValues.isEmpty()){
            // qd is not already aggregated
            float exactValueSampleSize = exactValues.size();
            float exactValueMin = new Float(exactValues.stream().mapToDouble(value->(double)value).min().getAsDouble());
            float exactValueMax = new Float(exactValues.stream().mapToDouble(value->(double)value).max().getAsDouble());
            float exactValueAvg = new Float(exactValues.stream().mapToDouble(value->(double)value).average().getAsDouble());
            aggQD.setSampleSize(exactValueSampleSize, null);
            aggQD.setMinimum(exactValueMin, null);
            aggQD.setMaximum(exactValueMax, null);
            aggQD.setAverage(exactValueAvg, null);
        }
        else{
            // qd is already aggregated
            aggQD = (QuantitativeData) qd.clone();
            if(aggQD.getMin()==null){
                aggQD.setMinimum(aggQD.getMax(), null);
            }
            if(aggQD.getMax()==null){
                aggQD.setMaximum(aggQD.getMin(), null);
            }
            if(aggQD.getSampleSize()==null){
                aggQD.setSampleSize(1f, null);
            }
            if(aggQD.getAverage()==null){
                aggQD.setAverage((aggQD.getMax()-aggQD.getMin()), null);
            }
        }
        return aggQD;
    }

    private static QuantitativeData mergeQuantitativeData(QuantitativeData aggregatedQD, QuantitativeData qd) {
        List<Float> exactValues = getExactValues(qd);

        Float min = null;
        Float max = null;
        Float average = null;
        Float sampleSize = null;
        if(!exactValues.isEmpty()){
            // qd is not already aggregated
            float exactValueSampleSize = exactValues.size();
            float exactValueMin = new Float(exactValues.stream().mapToDouble(value->(double)value).min().getAsDouble());
            float exactValueMax = new Float(exactValues.stream().mapToDouble(value->(double)value).max().getAsDouble());
            float exactValueAvg = new Float(exactValues.stream().mapToDouble(value->(double)value).average().getAsDouble());

            min = Math.min(exactValueMin, aggregatedQD.getMin());
            max = Math.max(exactValueMax, aggregatedQD.getMax());
            average = new Float(((aggregatedQD.getAverage()*aggregatedQD.getSampleSize())+exactValueAvg*exactValueSampleSize)/(aggregatedQD.getSampleSize()+exactValueSampleSize));
            sampleSize = exactValueSampleSize+aggregatedQD.getSampleSize();
        }
        else{
            // qd is already aggregated
            min = Math.min(aggregatedQD.getMin(), qd.getMin());
            max = Math.max(aggregatedQD.getMax(), qd.getMax());
            average = new Float(((aggregatedQD.getAverage()*aggregatedQD.getSampleSize())+qd.getAverage()*qd.getSampleSize())/(aggregatedQD.getSampleSize()+qd.getSampleSize()));
            sampleSize = qd.getSampleSize()+aggregatedQD.getSampleSize();
        }
        aggregatedQD.setAverage(average, null);
        aggregatedQD.setMinimum(min, null);
        aggregatedQD.setMaximum(max, null);
        aggregatedQD.setSampleSize(sampleSize, null);
        return aggregatedQD;
    }

    private static List<Float> getExactValues(QuantitativeData qd) {
        List<Float> exactValues = qd.getStatisticalValues().stream()
                .filter(value->value.getType().equals(StatisticalMeasure.EXACT_VALUE()))
                .map(exact->exact.getValue())
                .collect(Collectors.toList());
        return exactValues;
    }


    private static boolean hasSameState(StateData sd1, StateData sd2) {
        return sd2.getState().getUuid().equals(sd1.getState().getUuid());
    }

}
