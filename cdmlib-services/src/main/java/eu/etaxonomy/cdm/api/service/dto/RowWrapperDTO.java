// $Id$
/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.api.service.description.MissingMaximumMode;
import eu.etaxonomy.cdm.api.service.description.MissingMinimumMode;
import eu.etaxonomy.cdm.api.service.description.StructuredDescriptionAggregation;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author pplitzner
 * @since 16.04.2018
 */
public abstract class RowWrapperDTO <T extends DescriptionBase> implements Serializable {

    private static final long serialVersionUID = -7817164423660563673L;

    protected DescriptionBaseDto description;

    private TaxonNodeDto taxonNode;
    private Map<Feature, DescriptionElementBase> featureToElementMap;
    private Map<Feature, Collection<String>> featureToDisplayDataMap;

    public RowWrapperDTO(DescriptionBaseDto specimenDescription, TaxonNodeDto taxonNode) {
        this.taxonNode = taxonNode;
        this.featureToElementMap = new HashMap<>();
        this.featureToDisplayDataMap = new HashMap<>();
        this.description = specimenDescription;

        Set<DescriptionElementBase> elements = specimenDescription.getDescription().getElements();
        for (DescriptionElementBase descriptionElementBase : elements) {
            if(hasData(descriptionElementBase)){
                Feature feature = descriptionElementBase.getFeature();
                featureToElementMap.put(feature, descriptionElementBase);
                Collection<String> displayData = generateDisplayString(descriptionElementBase);
                if(displayData!=null){
                    featureToDisplayDataMap.put(feature, displayData);
                }
            }
        }
    }

    public QuantitativeData addQuantitativeData(Feature feature){
        QuantitativeData data = QuantitativeData.NewInstance(feature);
        description.getDescription().addElement(data);
        featureToElementMap.put(feature, data);
        return data;
    }

    public CategoricalData addCategoricalData(Feature feature){
        CategoricalData data = CategoricalData.NewInstance(feature);
        description.getDescription().addElement(data);
        featureToElementMap.put(feature, data);
        return data;
    }

    public DescriptionBaseDto getDescription() {
        return description;
    }

    public TaxonNodeDto getTaxonNode() {
        return taxonNode;
    }

    public Collection<String> getDisplayDataForFeature(Feature feature){
        return featureToDisplayDataMap.get(feature);
    }

    public DescriptionElementBase getDataValueForFeature(Feature feature){
        DescriptionElementBase descriptionElementBase = featureToElementMap.get(feature);
        return descriptionElementBase;
    }

    private Collection<String> generateDisplayString(DescriptionElementBase descriptionElementBase){
        Collection<String> displayData = new ArrayList<>();
        if(descriptionElementBase instanceof CategoricalData){
            CategoricalData categoricalData = (CategoricalData)descriptionElementBase;
            displayData = categoricalData.getStateData().stream()
                    .map(stateData->generateStateDataString(stateData))
                    .collect(Collectors.toList());
        }
        if(descriptionElementBase instanceof QuantitativeData){
            QuantitativeData quantitativeData = HibernateProxyHelper.deproxy(descriptionElementBase, QuantitativeData.class);
            displayData = Collections.singleton(generateQuantitativeDataString(quantitativeData));
        }
        return displayData;
    }

    private String generateQuantitativeDataString(QuantitativeData quantitativeData) {
        String displayData;
        displayData = "";
        Float min = quantitativeData.getMin();
        Float max = quantitativeData.getMax();
        if(min!=null||max!=null){
            displayData += "["+(min!=null?min.toString():"?")+"-"+(max!=null?max.toString():"?")+"] ";
        }
        displayData += quantitativeData.getStatisticalValues().stream().
        filter(value->value.getType().equals(StatisticalMeasure.EXACT_VALUE()))
        .map(exact->Float.toString(exact.getValue()))
        .collect(Collectors.joining(", "));
        if (quantitativeData.getUnit() != null){
            displayData += " "+ quantitativeData.getUnit().getIdInVocabulary();
        }
        return displayData;
    }

    private String generateStateDataString(StateData stateData) {
        return (stateData.getState()!=null?stateData.getState().getLabel():"[no state]")
                +(stateData.getCount()!=null?" ("+stateData.getCount()+")":"");
    }

    public void setDataValueForCategoricalData(Feature feature, List<State> states){
        DescriptionElementBase descriptionElementBase = featureToElementMap.get(feature);
        if(states.isEmpty()){
            removeFeature(feature, descriptionElementBase);
            return;
        }
        if(descriptionElementBase!=null && descriptionElementBase.isInstanceOf(CategoricalData.class)){
            CategoricalData categoricalData = HibernateProxyHelper.deproxy(descriptionElementBase, CategoricalData.class);
            categoricalData.setStateDataOnly(states);
            // update display data cache
            featureToDisplayDataMap.put(feature, generateDisplayString(categoricalData));
        }
    }

    private void removeFeature(Feature feature, DescriptionElementBase descriptionElementBase) {
        featureToElementMap.remove(feature);
        featureToDisplayDataMap.remove(feature);
        if(descriptionElementBase!=null){
            description.getDescription().removeElement(descriptionElementBase);
        }
    }

    public void setDataValueForQuantitativeData(Feature feature, Map<StatisticalMeasure, List<String>> textFields, MeasurementUnit unit){
        DescriptionElementBase descriptionElementBase = featureToElementMap.get(feature);
        if(textFields.values().stream().allMatch(listOfStrings->listOfStrings.isEmpty())){
            removeFeature(feature, descriptionElementBase);
            return;
        }
        if(descriptionElementBase.isInstanceOf(QuantitativeData.class)){
            QuantitativeData quantitativeData = HibernateProxyHelper.deproxy(descriptionElementBase, QuantitativeData.class);
            //clear values
            quantitativeData.getStatisticalValues().clear();
            //add back all values from text fields
            textFields.forEach((measure, texts)->{
                texts.forEach(text->{
                    String string = text;
                    try {
                        float exactValue = Float.parseFloat(string);
                        quantitativeData.addStatisticalValue(StatisticalMeasurementValue.NewInstance(measure, exactValue));
                    } catch (NumberFormatException e) {
                    }
                });
            });
            QuantitativeData fixedQuantitativeData = StructuredDescriptionAggregation.handleMissingMinOrMax(quantitativeData,
                    MissingMinimumMode.MinToZero, MissingMaximumMode.MaxToMin);
            // update display data cache
            fixedQuantitativeData.setUnit(unit);
            featureToDisplayDataMap.put(feature, generateDisplayString(fixedQuantitativeData));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((featureToElementMap == null) ? 0 : featureToElementMap.hashCode());
        result = prime * result + ((taxonNode == null) ? 0 : taxonNode.hashCode());
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
        RowWrapperDTO<?> other = (RowWrapperDTO<?>) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (featureToElementMap == null) {
            if (other.featureToElementMap != null) {
                return false;
            }
        } else if (!featureToElementMap.equals(other.featureToElementMap)) {
            return false;
        }
        if (taxonNode == null) {
            if (other.taxonNode != null) {
                return false;
            }
        } else if (!taxonNode.equals(other.taxonNode)) {
            return false;
        }
        return true;
    }

    public static boolean hasData(DescriptionElementBase element){
        if(element.isInstanceOf(CategoricalData.class)){
            CategoricalData categoricalData = HibernateProxyHelper.deproxy(element, CategoricalData.class);
            return !categoricalData.getStatesOnly().isEmpty();
        }
        else if(element.isInstanceOf(QuantitativeData.class)){
            QuantitativeData quantitativeData = HibernateProxyHelper.deproxy(element, QuantitativeData.class);
            return !quantitativeData.getStatisticalValues().isEmpty();
        }
        return false;
    }

}
