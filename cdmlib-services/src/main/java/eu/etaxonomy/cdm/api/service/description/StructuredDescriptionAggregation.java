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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
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
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
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

        dataSet = getDescriptiveDatasetService().load(getConfig().getDatasetUuid(), Arrays.asList(new String[] {
                "descriptions", //$NON-NLS-1$
                "descriptions.descriptionElements", //$NON-NLS-1$
                "descriptions.descriptionElements.stateData", //$NON-NLS-1$
                "descriptions.descriptionElements.stateData.state", //$NON-NLS-1$
                "descriptions.descriptionElements.feature", //$NON-NLS-1$
        }));

        double end1 = System.currentTimeMillis();
        logger.info("Time elapsed for pre-accumulate() : " + (end1 - start) / (1000) + "s");
    }


    private boolean hasCharacterData(DescriptionElementBase element) {
        return (element instanceof CategoricalData && !((CategoricalData) element).getStatesOnly().isEmpty())
                || (element instanceof QuantitativeData
                        && !((QuantitativeData) element).getStatisticalValues().isEmpty());
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
        dataSet.addDescription(targetDescription);
    }

    @Override
    protected void aggregateToParentTaxon(TaxonNode taxonNode,
            ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions) {
        StructuredDescriptionResultHolder descriptiveResultHolder = (StructuredDescriptionResultHolder)resultHolder;
        Set<TaxonDescription> childTaxonDescriptions = getChildTaxonDescriptions(taxonNode, dataSet);
        for (TaxonDescription desc:childTaxonDescriptions){
            for (DescriptionElementBase deb:desc.getElements()){
                if (deb.isCharacterData()){
                    if (deb.isInstanceOf(CategoricalData.class)){
                        addToCategorical(CdmBase.deproxy(deb, CategoricalData.class), descriptiveResultHolder);
                    }else if (deb.isInstanceOf(QuantitativeData.class)){
                        addToQuantitative(CdmBase.deproxy(deb, QuantitativeData.class), descriptiveResultHolder);
                    }
                }
            }
        }
    }

    @Override
    protected void aggregateWithinSingleTaxon(Taxon taxon,
            ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions) {
        StructuredDescriptionResultHolder descriptiveResultHolder = (StructuredDescriptionResultHolder)resultHolder;
        Set<SpecimenDescription> specimenDescriptions = getSpecimenDescriptions(taxon, dataSet);
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
        Map<Feature, StatisticalSummaryValues> quantitativeMap = new HashMap<>();
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

    private static StatisticalSummaryValues mergeQuantitativeData(StatisticalSummary... summaryStatistics) {
        Collection<StatisticalSummary> statistics = new ArrayList<>();
        for (StatisticalSummary statistic : summaryStatistics) {
            statistics.add(statistic);
        }
        return AggregateSummaryStatistics.aggregate(statistics);
    }

    private static QuantitativeData convertStatisticalSummaryValuesToQuantitativeData(Feature feature,
            StatisticalSummaryValues aggregate) {
        QuantitativeData aggregatedQuantitativeData = QuantitativeData.NewInstance(feature);
        aggregatedQuantitativeData.setMinimum(new Float(aggregate.getMin()), null);
        aggregatedQuantitativeData.setMaximum(new Float(aggregate.getMax()), null);
        aggregatedQuantitativeData.setAverage(new Float(aggregate.getMean()), null);
        aggregatedQuantitativeData.setStandardDeviation(new Float(aggregate.getStandardDeviation()), null);
        aggregatedQuantitativeData.setSampleSize(new Float(aggregate.getN()), null);
        return aggregatedQuantitativeData;
    }

    private static StatisticalSummary convertQuantitativeDataToSummaryStatistics(QuantitativeData qd) {
        SummaryStatistics summaryStatistics = new SummaryStatistics();
        List<StatisticalMeasurementValue> exactValues = qd.getStatisticalValues().stream()
        .filter(value->value.getType().equals(StatisticalMeasure.EXACT_VALUE()))
        .collect(Collectors.toList());
        // has exact values -> ignore statistical values
        if(!exactValues.isEmpty()){
            exactValues.forEach(exact->summaryStatistics.addValue(exact.getValue()));
        }
        // has statistical values
        else if(qd.getSampleSize()!=null
                && qd.getSampleSize()!=0f
                && qd.getMin()!=null
                && qd.getMin()!=null
                && qd.getAverage()!=null){
            Float count = qd.getSampleSize();
            if(count==1f){
                // sample size == 1 -> only add average
                summaryStatistics.addValue(qd.getAverage());
            }
            else {
                float min = qd.getMin();
                float max = qd.getMax();
                float average = qd.getAverage();
                float sampleSize = qd.getSampleSize();

                summaryStatistics.addValue(min);
                summaryStatistics.addValue(max);
                count -= 2;
                float averageFiller = ((average*sampleSize)-(min+max))/(sampleSize-2);
                while(count>0){
                    // fill with dummy values that do not change the average
                    // to assert the correct sample size
                    summaryStatistics.addValue(averageFiller);
                    count -= 1;
                }
            }
        }
        return summaryStatistics;
    }

    private static boolean hasSameState(StateData sd1, StateData sd2) {
        return sd2.getState().getUuid().equals(sd1.getState().getUuid());
    }

}
