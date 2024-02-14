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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.NoDescriptiveDataStatus;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.dto.CategoricalDataDto;
import eu.etaxonomy.cdm.persistence.dto.DescriptionBaseDto;
import eu.etaxonomy.cdm.persistence.dto.DescriptionElementDto;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
import eu.etaxonomy.cdm.persistence.dto.QuantitativeDataDto;
import eu.etaxonomy.cdm.persistence.dto.StateDataDto;
import eu.etaxonomy.cdm.persistence.dto.StatisticalMeasurementValueDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author pplitzner
 * @since 16.04.2018
 */
public abstract class RowWrapperDTO <T extends DescriptionBase> implements Serializable {

    private static final long serialVersionUID = -7817164423660563673L;

    private DescriptionBaseDto description;

    private TaxonNodeDto taxonNode;
    private Map<UUID, Set<DescriptionElementDto>> featureToElementMap;
    private Map<UUID, Collection<String>> featureToDisplayDataMap;

    public RowWrapperDTO(DescriptionBaseDto specimenDescription, TaxonNodeDto taxonNode, Language lang) {
        this.taxonNode = taxonNode;
        this.featureToElementMap = new HashMap<>();
        this.featureToDisplayDataMap = new HashMap<>();
        this.description = specimenDescription;

        List<DescriptionElementDto> elements = specimenDescription.getElements();
        if (elements != null){
            for (DescriptionElementDto descriptionElementBase : elements) {
    //            if(hasData(descriptionElementBase)){
                UUID featureUuid = descriptionElementBase.getFeatureUuid();
                addToFeatureToElementMap(featureUuid, descriptionElementBase);

                Collection<String> displayData = generateDisplayString(descriptionElementBase, lang);
                if(displayData!=null){
                    addDisplayStringsToMap(featureUuid, displayData);
                }
            }
        }
    }

    private void addDisplayStringsToMap(UUID featureUuid, Collection<String> displayData) {
        if (featureToDisplayDataMap.get(featureUuid) == null){
            featureToDisplayDataMap.put(featureUuid, new HashSet<>());
        }
        featureToDisplayDataMap.get(featureUuid).addAll(displayData);
    }

    public QuantitativeDataDto addQuantitativeData(FeatureDto feature){
        QuantitativeDataDto data = new QuantitativeDataDto(feature);
        removeElementForFeature(feature.getUuid());
        description.addElement(data);
        addToFeatureToElementMap(feature.getUuid(), data);
        return data;
    }

    private void addToFeatureToElementMap(UUID featureUuid, DescriptionElementDto data) {
        if (featureToElementMap.get(featureUuid) == null){
            featureToElementMap.put(featureUuid, new HashSet<>());
        }
        featureToElementMap.get(featureUuid).add(data);
    }

    public CategoricalDataDto addCategoricalData(FeatureDto feature){
        CategoricalDataDto data = new CategoricalDataDto(feature);
        removeElementForFeature(feature.getUuid());
        description.addElement(data);
        addToFeatureToElementMap(feature.getUuid(), data);
        return data;
    }

    public DescriptionBaseDto getDescription() {
        return description;
    }
    public void setDescription(DescriptionBaseDto description) {
        this.description = description;
    }

    public TaxonNodeDto getTaxonNode() {
        return taxonNode;
    }

    public Collection<String> getDisplayDataForFeature(UUID featureUuid){
        return featureToDisplayDataMap.get(featureUuid);
    }

    public Set<DescriptionElementDto> getDataValueForFeature(UUID featureUuid){
        Set<DescriptionElementDto> descriptionElementBase = featureToElementMap.get(featureUuid);
        return descriptionElementBase;
    }


    private Collection<String> generateDisplayString(DescriptionElementDto descriptionElementBase, Language lang){
        Collection<String> displayData = new ArrayList<>();
        if(descriptionElementBase instanceof CategoricalDataDto){
            CategoricalDataDto categoricalData = (CategoricalDataDto)descriptionElementBase;
            if (categoricalData.getNoDataStatus()!= null) {
                displayData.add(categoricalData.getNoDataStatus().getLabel());
            }else {
                List<StateDataDto> states = categoricalData.getStates();
                Collections.sort(states, new Comparator<StateDataDto>() {
                    @Override
                    public int compare(StateDataDto h1, StateDataDto h2) {
                        if (h1.getCount() == null && h2.getCount() != null){
                            return -1;
                        } else if (h2.getCount() == null && h1.getCount() != null){
                            return 1;
                        } else if (h1.getCount() != h2.getCount()){
                            return -h1.getCount().compareTo(h2.getCount());
                        } else {
                            if (h1.getState() == h2.getState()){
                                return h1.toString().compareTo(h2.toString());
                            } else if (h1.getState() == null){
                                return -1;
                            } else if (h2.getState() == null){
                                return 1;
                            }else{
                                return h1.getState().getPreferredRepresentation(lang).getLabel().compareTo(h2.getState().getPreferredRepresentation(lang).getLabel());
                            }
                        }
                    }
                });
                displayData = states.stream()
                        .map(stateData->generateStateDataString(stateData, lang))
                        .collect(Collectors.toList());
            }
        }
        if(descriptionElementBase instanceof QuantitativeDataDto){
            QuantitativeDataDto quantitativeData = (QuantitativeDataDto)descriptionElementBase;
            if (quantitativeData.getNoDataStatus()!= null) {
                displayData.add(quantitativeData.getNoDataStatus().getLabel());
            }else {
                displayData = Collections.singleton(generateQuantitativeDataString(quantitativeData));
            }

        }
        return displayData;
    }

    private String generateQuantitativeDataString(QuantitativeDataDto quantitativeData) {
        String displayData;
        displayData = "";
        BigDecimal min = quantitativeData.getSpecificStatisticalValue(StatisticalMeasure.MIN().getUuid());
        BigDecimal max = quantitativeData.getSpecificStatisticalValue(StatisticalMeasure.MAX().getUuid());
        BigDecimal mean = quantitativeData.getSpecificStatisticalValue(StatisticalMeasure.AVERAGE().getUuid());
        BigDecimal low = quantitativeData.getSpecificStatisticalValue(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY().getUuid());
        BigDecimal high = quantitativeData.getSpecificStatisticalValue(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY().getUuid());
        BigDecimal size = quantitativeData.getSpecificStatisticalValue(StatisticalMeasure.SAMPLE_SIZE().getUuid());
        String typicalValues = "";
        if (low != null || high != null){
            typicalValues += low!= null?low.toString():"";
            typicalValues += high!= null? "-"+ high.toString():"";
        }
        if(min!=null||max!=null){
            if (min!= null && max != null && min.intValue() == max.intValue()){
                displayData += "("+min.toString()+")"+typicalValues;
            }else{
                if (StringUtils.isBlank(typicalValues)){
                    displayData += "("+(min!=null?min.toString():"?")+"-"+(max!=null?max.toString():"?")+") ";
                }else{
                    displayData += "("+(min!=null?min.toString():"?")+ "-)"+typicalValues;
                    displayData += "(-"+(max!=null?max.toString():"?")+") ";
                }
            }
        }
        displayData += quantitativeData.getValues().stream()
                .filter(value->value.getType().getUuid().equals(StatisticalMeasure.EXACT_VALUE().getUuid()))
                .map(exact->exact.getValue().toString())
                .collect(Collectors.joining(", "));
        if (quantitativeData.getMeasurementUnit() != null && StringUtils.isNotBlank(displayData)){
            displayData += " "+ quantitativeData.getMeasurementIdInVocabulary();
        }

        return displayData;
    }

    private String generateStateDataString(StateData stateData) {
        return (stateData.getState()!=null?stateData.getState().getLabel():"[no state]")
                +(stateData.getCount()!=null?" ("+stateData.getCount()+")":"");
    }

    private String generateStateDataString(StateDataDto stateData, Language lang) {
        String result = "";
        if (stateData.getModifiers() != null && !stateData.getModifiers().isEmpty()) {
            if (stateData.getModifiers().iterator().next().getPreferredRepresentation(lang) != null) {
                result = stateData.getModifiers().iterator().next().getPreferredRepresentation(lang).getLabel() + " ";
            }else {
                result = stateData.getModifiers().iterator().next().getLabel();
            }
        }
        if (stateData.getState()!=null ) {
            if (stateData.getState().getPreferredRepresentation(lang) != null) {
                result += stateData.getState().getPreferredRepresentation(lang);
            }else {
                result += stateData.getState().getLabel();
            }
        }else {
            result += "[no state]";
        }
        if (stateData.getCount() != null) {
            result += " ("+stateData.getCount()+")";
        }

        return result;
    }

    public void setDataValueForCategoricalData(UUID featureUuid, List<StateDataDto> states, Language lang){
        Set<DescriptionElementDto> descriptionElementBase = featureToElementMap.get(featureUuid);

        if(states.isEmpty()){
            removeFeature(featureUuid, descriptionElementBase);
            return;
        }
        CategoricalDataDto categoricalData = null;
        if(descriptionElementBase!=null){
            if (descriptionElementBase.size() == 1){
                DescriptionElementDto dto = descriptionElementBase.iterator().next();
                if (dto instanceof CategoricalDataDto){
                    categoricalData = (CategoricalDataDto)dto;
                    categoricalData.getStates().clear();
                    for(StateDataDto state: states) {
                        categoricalData.addStateData(state);
                    }
                }
            }
        }else{
            Feature feature = DefinedTermBase.getTermByUUID(featureUuid, Feature.class);
            categoricalData = new CategoricalDataDto(FeatureDto.fromFeature(feature));
            categoricalData.getStates().clear();
            for(StateDataDto state: states) {
                categoricalData.addStateData(state);
            }
        }
        removeElementForFeature(featureUuid);
        description.getElements().add(categoricalData);
        addToFeatureToElementMap(featureUuid, categoricalData);

        // update display data cache
        addDisplayStringsToMap(featureUuid, generateDisplayString(categoricalData, lang));
//        featureToDisplayDataMap.put(featureUuid, generateDisplayString(categoricalData));
    }

    public void generateNewDisplayString(UUID featureUuid, DescriptionElementDto element, Language lang) {
        addDisplayStringsToMap(featureUuid, generateDisplayString(element, lang));
    }

    private void removeElementForFeature(UUID featureUuid) {
        DescriptionElementDto oldElement = null;
        for (DescriptionElementDto elementDto: description.getElements()){
            if (elementDto.getFeatureUuid()!= null && elementDto.getFeatureUuid().equals(featureUuid)) {
                oldElement = elementDto;
                break;
            }
        }
        description.getElements().remove(oldElement);
        featureToElementMap.remove(featureUuid);
        featureToDisplayDataMap.remove(featureUuid);
    }

    private void removeFeature(UUID featureUuid, Set<DescriptionElementDto> descriptionElementBases) {
        Set<DescriptionElementDto> elements = featureToElementMap.get(featureUuid);
        if (elements == null){
            return;
        }
        Integer i = 0;
        Set<Integer> indices = new  HashSet<>();
        for (DescriptionElementDto dto: description.getElements()){
            for (DescriptionElementDto descElDto: descriptionElementBases) {
                if (dto.getFeatureUuid() != null && dto.getFeatureUuid().equals(descElDto.getFeatureUuid())){
                    indices.add(i);
                    break;
                }
            }
            i++;
        }
        for (int index: indices){
            description.getElements().remove(index);
        }
        featureToElementMap.remove(featureUuid);
        featureToDisplayDataMap.remove(featureUuid);
    }

    public void setDataValueForQuantitativeData(UUID featureUuid, Map<TermDto, List<String>> textFields, TermDto unit, NoDescriptiveDataStatus noDataStatus, Language lang){
        Set<DescriptionElementDto> descriptionElementBase = featureToElementMap.get(featureUuid);

        if(textFields.values().stream().allMatch(listOfStrings->listOfStrings.isEmpty())){
            removeFeature(featureUuid, descriptionElementBase);
            if (noDataStatus == null) {
                return;
            }
        }
        QuantitativeDataDto quantitativeData = null;
        if (descriptionElementBase == null){
            Feature feature = DefinedTermBase.getTermByUUID(featureUuid, Feature.class);
            quantitativeData = new QuantitativeDataDto(FeatureDto.fromFeature(feature));
        }

        if(descriptionElementBase!=null){
            if (descriptionElementBase.size() == 1){
                DescriptionElementDto dto = descriptionElementBase.iterator().next();
                if (dto instanceof QuantitativeDataDto){
                    quantitativeData = (QuantitativeDataDto)dto;
                }
            }
        }
        if (quantitativeData == null){
            //TODO: check whether this is possible
            return;
        }
        quantitativeData.getValues().clear();
        if (noDataStatus != null) {
            quantitativeData.setNoDataStatus(noDataStatus);
        }else {
            quantitativeData.setMeasurementUnit(unit);
            //add back all values from text fields
            Set<StatisticalMeasurementValueDto> tempValues = new HashSet<>();
            textFields.forEach((measure, texts)->{
                texts.forEach(text->{
                    String string = text;
                    try {
                        if (StringUtils.isNotBlank(string)){
                            BigDecimal exactValue = new BigDecimal(string);
    //                        StatisticalMeasurementValue newValue = StatisticalMeasurementValue.NewInstance(measure, exactValue);
                            StatisticalMeasurementValueDto newValueDto = new StatisticalMeasurementValueDto(measure, exactValue, null);
    //                                StatisticalMeasurementValueDto.fromStatisticalMeasurementValue(newValue);
                            tempValues.add(newValueDto);
                        }
                    } catch (NumberFormatException e) {
                    }
                });
            });

            quantitativeData.setValues(tempValues);
            removeElementForFeature(featureUuid);
        }
        description.getElements().add(quantitativeData);
        addToFeatureToElementMap(featureUuid, quantitativeData);
        addDisplayStringsToMap(featureUuid, generateDisplayString(quantitativeData, lang));
//        featureToDisplayDataMap.put(featureUuid, generateDisplayString(quantitativeData));
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

    public static boolean hasData(DescriptionElementDto element){
        if(element instanceof CategoricalDataDto){
            return !((CategoricalDataDto)element).getStates().isEmpty();
        }
        else if(element instanceof QuantitativeDataDto){
            return !((QuantitativeDataDto)element).getValues().isEmpty();
        }
        return false;
    }

    public static boolean hasData(DescriptionElementBase element){
        if(element instanceof CategoricalData){
            return !((CategoricalData)element).getStatesOnly().isEmpty();
        }
        else if(element instanceof QuantitativeData){
            return !((QuantitativeData)element).getStatisticalValues().isEmpty();
        }
        return false;
    }
}