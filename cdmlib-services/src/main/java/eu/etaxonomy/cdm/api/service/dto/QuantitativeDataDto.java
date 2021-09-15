/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author k.luther
 * @since Aug 18, 2021
 */
public class QuantitativeDataDto extends DescriptionElementDto {



    private static final long serialVersionUID = 9043099217303520574L;

    private TermDto measurementUnitDto;
//    private String measurementIdInVocabulary;

    private Set<StatisticalMeasurementValueDto> values = new HashSet<>();

    public QuantitativeDataDto(UUID elementUuid, FeatureDto feature){
        super(elementUuid, feature);
    }


    public static QuantitativeDataDto fromQuantitativeData (QuantitativeData data) {
        QuantitativeDataDto dto = new QuantitativeDataDto(data.getUuid(), FeatureDto.fromFeature(data.getFeature()));
        dto.measurementUnitDto = data.getUnit() != null? TermDto.fromTerm(data.getUnit()): null;
//        dto.measurementIdInVocabulary = data.getUnit() != null? data.getUnit().getIdInVocabulary(): null;
        for (StatisticalMeasurementValue value: data.getStatisticalValues()){
            StatisticalMeasurementValueDto statDto = StatisticalMeasurementValueDto.fromStatisticalMeasurementValue(value);
            dto.values.add(statDto);
        }
        return dto;
    }

    public QuantitativeDataDto(FeatureDto feature){
        super(feature);

    }

    public TermDto getMeasurementUnit() {
        return measurementUnitDto;
    }

    public void setMeasurementUnit(TermDto measurementUnit) {
        this.measurementUnitDto = measurementUnit;
    }

    public Set<StatisticalMeasurementValueDto> getValues() {
        return values;
    }

    public void setValues(Set<StatisticalMeasurementValueDto> values) {
        this.values = values;
    }

    public BigDecimal getSpecificStatisticalValue(UUID typeUUID){
        BigDecimal result = null;
        for (StatisticalMeasurementValueDto value : values){
            if (typeUUID.equals(value.getType().getUuid())){
                result = value.getValue();
                break;
            }
        }
        return result;
    }

    public String getMeasurementIdInVocabulary() {
        return measurementUnitDto.getIdInVocabulary();
    }


}
