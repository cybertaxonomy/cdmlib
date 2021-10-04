/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.common.BigDecimalUtil;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
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
    protected void preAggregate(IProgressMonitor monitor) {
        monitor.subTask("preAccumulate - nothing to do");

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
                    || quantitativeData.getMin()!=null
                    || quantitativeData.getMax()!=null;
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

        Set<DescriptionBase<?>> sourceDescriptions = structuredResultHolder.sourceDescriptions;
        for (DescriptionBase<?> descriptionBase : sourceDescriptions) {
            DescriptionBase<?> sourceDescription = null;
            if(descriptionBase.isInstanceOf(SpecimenDescription.class)){
                DescriptionBase<?> clone = descriptionBase.clone();
                clone.removeDescriptiveDataSet(dataSet);
                clone.getTypes().add(DescriptionType.CLONE_FOR_SOURCE);
                SpecimenOrObservationBase<?> specimen = CdmBase.deproxy(descriptionBase, SpecimenDescription.class).getDescribedSpecimenOrObservation();
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
            Set<DescriptionElementBase> elementsToRemove = new HashSet<>();
            DescriptionElementBase elementReplacement = null;
            for (DescriptionElementBase descriptionElementBase : targetDescription.getElements()) {
                if(descriptionElementBase.getFeature().equals(entry.getKey())){
                    elementsToRemove.add(descriptionElementBase);
                    elementReplacement = entry.getValue();
                }
            }
            if(!elementsToRemove.isEmpty() && elementReplacement!=null){
                for(DescriptionElementBase elementToRemove : elementsToRemove){
                    targetDescription.removeElement(elementToRemove);
                }
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
        Set<TaxonDescription> childDescriptions = getChildTaxonDescriptions(taxonNode, dataSet);
        addDescriptionElement(descriptiveResultHolder, childDescriptions);
    }

    @Override
    protected void aggregateWithinSingleTaxon(Taxon taxon,
            ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions) {
        StructuredDescriptionResultHolder descriptiveResultHolder = (StructuredDescriptionResultHolder)resultHolder;
        Set<SpecimenDescription> specimenDescriptions = getSpecimenDescriptions(taxon, dataSet);
        addDescriptionElement(descriptiveResultHolder, specimenDescriptions);
        if (getConfig().isIncludeLiterature()){
            Set<TaxonDescription> literatureDescriptions = getLiteratureDescriptions(taxon, dataSet);
            addDescriptionElement(descriptiveResultHolder, literatureDescriptions);
        }
        //TODO add default descriptions

    }

    private void addDescriptionElement(StructuredDescriptionResultHolder descriptiveResultHolder,
            Set<? extends DescriptionBase<?>> descriptions) {

        boolean descriptionWasUsed = false;
        for (DescriptionBase<?> desc: descriptions){
            for (DescriptionElementBase deb: desc.getElements()){
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
        if (aggregatedQuantitativeData != null){
            resultHolder.quantitativeMap.put(qd.getFeature(), aggregatedQuantitativeData);
        }
    }

    private void addToCategorical(CategoricalData cd, StructuredDescriptionResultHolder resultHolder) {
        CategoricalData aggregatedCategoricalData = resultHolder.categoricalMap.get(cd.getFeature());
        if(aggregatedCategoricalData==null){
            // no CategoricalData with this feature in aggregation
            aggregatedCategoricalData = cd.clone();
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
                StateData clone = sd.clone();
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
        private Map<Feature, CategoricalData> categoricalMap = new HashMap<>();
        private Map<Feature, QuantitativeData> quantitativeMap = new HashMap<>();
        private Set<DescriptionBase<?>> sourceDescriptions = new HashSet<>();
        @Override
        public String toString() {
            return "SDResultHolder [categoricals=" + categoricalMap.size() + ", quantitatives="
                    + quantitativeMap.size() + ", sourceDescriptions=" + sourceDescriptions.size() + "]";
        }
    }

    private Set<TaxonDescription> getChildTaxonDescriptions(TaxonNode taxonNode, DescriptiveDataSet dataSet) {
        Set<TaxonDescription> result = new HashSet<>();
        List<TaxonNode> childNodes = taxonNode.getChildNodes();
        for (TaxonNode childNode : childNodes) {
            Set<TaxonDescription> childDescriptions = childNode.getTaxon().getDescriptions();
            result.addAll(childDescriptions.stream()
                .filter(desc->desc.getTypes().contains(DescriptionType.AGGREGATED_STRUC_DESC))
                .filter(desc->dataSet.getDescriptions().contains(desc))
                .collect(Collectors.toSet()));
        }
        return result;
    }

    private Set<SpecimenDescription> getSpecimenDescriptions(Taxon taxon, DescriptiveDataSet dataSet) {
        Set<SpecimenDescription> result = new HashSet<>();
        //TODO performance: use DTO service to retrieve specimen descriptions without initializing all taxon descriptions
        for (TaxonDescription taxonDesc: taxon.getDescriptions()){
            for (DescriptionElementBase taxonDeb : taxonDesc.getElements()){
                if (taxonDeb.isInstanceOf(IndividualsAssociation.class)){
                    IndividualsAssociation indAss = CdmBase.deproxy(taxonDeb, IndividualsAssociation.class);
                    SpecimenOrObservationBase<?> specimen = indAss.getAssociatedSpecimenOrObservation();
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    Set<SpecimenDescription> descriptions = (Set)specimen.getDescriptions();
                    for(SpecimenDescription specimenDescription : descriptions){
                        if(dataSet.getDescriptions().contains(specimenDescription) &&
                                specimenDescription.getTypes().stream().noneMatch(type->type.equals(DescriptionType.CLONE_FOR_SOURCE))){
                            result.add(specimenDescription);
                        }
                    }
                }
            }
        }
        return result;
    }

    private Set<TaxonDescription> getLiteratureDescriptions(Taxon taxon, DescriptiveDataSet dataSet) {
        Set<TaxonDescription> result = new HashSet<>();
        //TODO performance: use DTO service to retrieve specimen descriptions without initializing all taxon descriptions
        for(TaxonDescription taxonDescription : taxon.getDescriptions()){
            if(dataSet.getDescriptions().contains(taxonDescription)
                    && taxonDescription.getTypes().stream().anyMatch(type->type.equals(DescriptionType.SECONDARY_DATA))
                    && taxonDescription.getTypes().stream().noneMatch(type->type.equals(DescriptionType.CLONE_FOR_SOURCE)) ){
                result.add(taxonDescription);
            }
        }
        return result;
    }

    private QuantitativeData aggregateSingleQuantitativeData(QuantitativeData sourceQd){
        QuantitativeData aggQD = QuantitativeData.NewInstance(sourceQd.getFeature());
        Set<BigDecimal> exactValues = sourceQd.getExactValues();
        if(!exactValues.isEmpty()){
            Comparator<BigDecimal> comp = Comparator.naturalOrder();
            // qd is not already aggregated
            int exactValueSampleSize = exactValues.size();
            BigDecimal exactValueMin = exactValues.stream().min(comp).get();
            BigDecimal exactValueMax = exactValues.stream().max(comp).get();
            BigDecimal exactValueAvg = BigDecimalUtil.average(exactValues);
            //TODO also check for typical boundary data
            if(sourceQd.getMin() == null && sourceQd.getMax() == null){
                aggQD.setSampleSize(new BigDecimal(exactValueSampleSize), null);
                aggQD.setAverage(exactValueAvg, null);
            }
            aggQD.setMinimum(sourceQd.getMin() == null ? exactValueMin: sourceQd.getMin().min(exactValueMin), null);
            aggQD.setMaximum(sourceQd.getMax() == null ? exactValueMax: sourceQd.getMax().max(exactValueMax), null);
        }
        else{
            // qd has only min, max, ... but no exact values
            aggQD = sourceQd.clone();
            aggQD = handleMissingValues(aggQD);
        }
        return aggQD;
    }

    private QuantitativeData handleMissingValues(QuantitativeData qd) {
        //min max
        qd = handleMissingMinOrMax(qd);
        //average
        if (qd != null && qd.getAverage() == null){
            BigDecimal n = qd.getSampleSize();
            if(n != null && !n.equals(0f)){
                BigDecimal average = (qd.getMax().add(qd.getMin())).divide(n);
                qd.setAverage(average, null);
            }
        }
        return qd;
    }

    private QuantitativeData handleMissingMinOrMax(QuantitativeData qd) {
        return handleMissingMinOrMax(qd, getConfig().getMissingMinimumMode(), getConfig().getMissingMaximumMode());
    }

    public static QuantitativeData handleMissingMinOrMax(QuantitativeData aggQD, MissingMinimumMode missingMinMode,
            MissingMaximumMode missingMaxMode) {
        if(aggQD.getMin() == null && aggQD.getMax() != null){
            if (missingMinMode == MissingMinimumMode.MinToZero) {
                aggQD.setMinimum(BigDecimal.valueOf(0f), null);
            }else if (missingMinMode == MissingMinimumMode.MinToMax){
                aggQD.setMinimum(aggQD.getMax(), null);
            }else if (missingMinMode == MissingMinimumMode.SkipRecord){
                return null;
            }
        }
        if(aggQD.getMax() == null && aggQD.getMin() != null){
            if (missingMaxMode == MissingMaximumMode.MaxToMin){
                aggQD.setMaximum(aggQD.getMin(), null);
            }else if (missingMaxMode == MissingMaximumMode.SkipRecord){
                return null;
            }
        }
        return aggQD;
    }

    private QuantitativeData mergeQuantitativeData(QuantitativeData aggQd, QuantitativeData newQd) {

        newQd = aggregateSingleQuantitativeData(newQd); //alternatively we could check, if newQd is already basically aggregated, but for this we need a clear definition what the minimum requirements are and how ExactValues and MinMax if existing in parallel should be handled.

        BigDecimal min = null;
        BigDecimal max = null;
        BigDecimal average = null;
        BigDecimal sampleSize = null;
        newQd = handleMissingValues(newQd);
        if (newQd == null){
            return aggQd;
        }
        min = aggQd.getMin().min(newQd.getMin());
        max = aggQd.getMax().max(newQd.getMax());
        if (newQd.getSampleSize() != null && aggQd.getSampleSize() != null){
            sampleSize = newQd.getSampleSize().add(aggQd.getSampleSize());
        }
        if (sampleSize != null && !sampleSize.equals(0f) && aggQd.getAverage() != null && newQd.getAverage() != null){
            BigDecimal aggTotalSum = aggQd.getAverage().multiply(aggQd.getSampleSize(), MathContext.DECIMAL32);
            BigDecimal newTotalSum = newQd.getAverage().multiply(newQd.getSampleSize(), MathContext.DECIMAL32);
            BigDecimal totalSum = aggTotalSum.add(newTotalSum);
            average = totalSum.divide(sampleSize, MathContext.DECIMAL32).stripTrailingZeros();  //to be discussed if we really want to reduce precision here, however, due to the current way to compute average we do not have exact precision anyway
        }
        aggQd.setMinimum(min, null);
        aggQd.setMaximum(max, null);
        aggQd.setSampleSize(sampleSize, null);
        aggQd.setAverage(average, null);
        return aggQd;
    }

    private static List<BigDecimal> getExactValues(QuantitativeData qd) {
        List<BigDecimal> exactValues = qd.getStatisticalValues().stream()
                .filter(value->value.getType().equals(StatisticalMeasure.EXACT_VALUE()))
                .map(exact->exact.getValue())
                .collect(Collectors.toList());
        return exactValues;
    }

    private static boolean hasSameState(StateData sd1, StateData sd2) {
        if (sd2.getState() == null || sd1.getState() == null){
            return false;
        }else{
            return sd2.getState().getUuid().equals(sd1.getState().getUuid());
        }
    }
}
