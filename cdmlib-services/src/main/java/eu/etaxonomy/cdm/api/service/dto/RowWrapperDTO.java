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

import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
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
    private Map<UUID, DescriptionElementDto> featureToElementMap;
    private Map<UUID, Collection<String>> featureToDisplayDataMap;

    public RowWrapperDTO(DescriptionBaseDto specimenDescription, TaxonNodeDto taxonNode) {
        this.taxonNode = taxonNode;
        this.featureToElementMap = new HashMap<>();
        this.featureToDisplayDataMap = new HashMap<>();
        this.description = specimenDescription;

        List<DescriptionElementDto> elements = specimenDescription.getElements();
        if (elements != null){
            for (DescriptionElementDto descriptionElementBase : elements) {
    //            if(hasData(descriptionElementBase)){
                UUID featureUuid = descriptionElementBase.getFeatureUuid();
                featureToElementMap.put(featureUuid, descriptionElementBase);
                Collection<String> displayData = generateDisplayString(descriptionElementBase);
                if(displayData!=null){
                    featureToDisplayDataMap.put(featureUuid, displayData);
                }

            }
        }
    }

    public QuantitativeDataDto addQuantitativeData(FeatureDto feature){
        QuantitativeDataDto data = new QuantitativeDataDto(feature);
        description.addElement(data);
        featureToElementMap.put(feature.getUuid(), data);
        return data;
    }

    public CategoricalDataDto addCategoricalData(FeatureDto feature){
        CategoricalDataDto data = new CategoricalDataDto(feature);
        description.addElement(data);
        featureToElementMap.put(feature.getUuid(), data);
        return data;
    }

    public DescriptionBaseDto getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(DescriptionBaseDto description) {
        this.description = description;
    }

    public TaxonNodeDto getTaxonNode() {
        return taxonNode;
    }

    public Collection<String> getDisplayDataForFeature(UUID featureUuid){
        return featureToDisplayDataMap.get(featureUuid);
    }

    public DescriptionElementDto getDataValueForFeature(UUID featureUuid){
        DescriptionElementDto descriptionElementBase = featureToElementMap.get(featureUuid);
        return descriptionElementBase;
    }



    private Collection<String> generateDisplayString(DescriptionElementDto descriptionElementBase){
        Collection<String> displayData = new ArrayList<>();
        if(descriptionElementBase instanceof CategoricalDataDto){
            CategoricalDataDto categoricalData = (CategoricalDataDto)descriptionElementBase;
            List<StateDataDto> states = categoricalData.getStates();
            Collections.sort(states, new Comparator<StateDataDto>() {
                @Override
                public int compare(StateDataDto h1, StateDataDto h2) {
                    if (h1.getCount() != null && h2.getCount() != null && h1.getCount() != h2.getCount()){
                        return -h1.getCount().compareTo(h2.getCount());
                    }
                    if (h1.getCount() == h2.getCount()){
                        if (h1.getState() == h2.getState()){
                            return 0;
                        }
                        if (h1.getState() == null){
                            return -1;
                        }
                        if (h2.getState() == null){
                            return 1;
                        }
                        return h1.getState().getTitleCache().compareTo(h2.getState().getTitleCache());
                    }
                    if (h1.getCount() == null){
                        return -1;
                    }
                    if (h2.getCount() == null){
                        return 1;
                    }
                    return h1.toString().compareTo(h2.toString());
                }
            });
            displayData = states.stream()
                    .map(stateData->generateStateDataString(stateData))
                    .collect(Collectors.toList());
        }
        if(descriptionElementBase instanceof QuantitativeDataDto){
            QuantitativeDataDto quantitativeData = (QuantitativeDataDto)descriptionElementBase;
            displayData = Collections.singleton(generateQuantitativeDataString(quantitativeData));
        }
        return displayData;
    }

    private String generateQuantitativeDataString(QuantitativeDataDto quantitativeData) {
        String displayData;
        displayData = "";
        BigDecimal min = quantitativeData.getSpecificStatisticalValue(StatisticalMeasure.MIN().getUuid());
        BigDecimal max = quantitativeData.getSpecificStatisticalValue(StatisticalMeasure.MAX().getUuid());
        if(min!=null||max!=null){
            displayData += "["+(min!=null?min.toString():"?")+"-"+(max!=null?max.toString():"?")+"] ";
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

    private String generateStateDataString(StateDataDto stateData) {
        return (stateData.getState()!=null?stateData.getState().getTitleCache():"[no state]")
                +(stateData.getCount()!=null?" ("+stateData.getCount()+")":"");
    }

    public void setDataValueForCategoricalData(UUID featureUuid, List<TermDto> states){
        DescriptionElementDto descriptionElementBase = featureToElementMap.get(featureUuid);

        if(states.isEmpty()){
            removeFeature(featureUuid, descriptionElementBase);
            return;
        }
        CategoricalDataDto categoricalData = null;
        if(descriptionElementBase!=null && descriptionElementBase instanceof CategoricalDataDto){
            categoricalData = (CategoricalDataDto)descriptionElementBase;
            categoricalData.setStateDataOnly(states);

        }else{
            Feature feature = DefinedTermBase.getTermByClassAndUUID(Feature.class, featureUuid);
            if (feature == null){
                feature = DefinedTermBase.getTermByClassAndUUID(Character.class, featureUuid);
            }
            categoricalData = new CategoricalDataDto(FeatureDto.fromFeature(feature));
            categoricalData.setStateDataOnly(states);
        }
        removeElementForFeature(featureUuid);
        description.getElements().add(categoricalData);
        featureToElementMap.put(featureUuid, categoricalData);
        // update display data cache
        featureToDisplayDataMap.put(featureUuid, generateDisplayString(categoricalData));
    }

    /**
     * @param featureUuid
     * @param oldElement
     */
    private void removeElementForFeature(UUID featureUuid) {
        DescriptionElementDto oldElement = null;
        for (DescriptionElementDto elementDto: description.getElements()){
            if (elementDto.getFeatureUuid()!= null && elementDto.getFeatureUuid().equals(featureUuid)) {
                oldElement = elementDto;
                break;
            }

        }
        description.getElements().remove(oldElement);
    }

    private void removeFeature(UUID featureUuid, DescriptionElementDto descriptionElementBase) {
        DescriptionElementDto element = featureToElementMap.get(featureUuid);
        if (element == null){
            return;
        }
        int i = 0;

        for (DescriptionElementDto dto: description.getElements()){
            if (dto.getFeatureUuid() != null && dto.getFeatureUuid().equals(descriptionElementBase.getFeatureUuid())){
                break;
            }

            i++;
        }

        description.getElements().remove(i);
        featureToElementMap.remove(featureUuid);
        featureToDisplayDataMap.remove(featureUuid);

    }

    public void setDataValueForQuantitativeData(UUID featureUuid, Map<TermDto, List<String>> textFields, TermDto unit){
        DescriptionElementDto descriptionElementBase = featureToElementMap.get(featureUuid);
        if(textFields.values().stream().allMatch(listOfStrings->listOfStrings.isEmpty())){
            removeFeature(featureUuid, descriptionElementBase);
            return;
        }
        QuantitativeDataDto quantitativeData = null;
        if (descriptionElementBase == null){
            Feature feature = DefinedTermBase.getTermByClassAndUUID(Feature.class, featureUuid);
            if (feature == null){
                feature = DefinedTermBase.getTermByClassAndUUID(Character.class, featureUuid);
            }
            quantitativeData = new QuantitativeDataDto(FeatureDto.fromFeature(feature));
        }

        if(descriptionElementBase != null && descriptionElementBase instanceof QuantitativeDataDto){
            quantitativeData = (QuantitativeDataDto)descriptionElementBase;
            //clear values

        }
        quantitativeData.getValues().clear();
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
        description.getElements().add(quantitativeData);
        featureToElementMap.put(featureUuid, quantitativeData);
        featureToDisplayDataMap.put(featureUuid, generateDisplayString(quantitativeData));
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
