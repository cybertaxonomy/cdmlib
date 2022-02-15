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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
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

    public void addValue(StatisticalMeasurementValueDto value) {
        this.values.add(value);
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

    public static String getQuantitativeDataDtoSelect(){
        String[] result = createSqlParts();

        return result[0]+result[1]+result[2] + result[3];
    }

    private static String[] createSqlParts() {
        //featureDto, uuid, states

        String sqlSelectString = ""
                + "select a.uuid, "
                + "feature.uuid, "
                + "statVal.uuid,  "
                + "statVal.value, "
                + "statVal.type, "
                + "unit";

        String sqlFromString =   " FROM QuantitativeData as a ";

        String sqlJoinString =  "LEFT JOIN a.statisticalValues as statVal "
                + "LEFT JOIN a.feature as feature "
                + "LEFT JOIN a.unit as unit ";

        String sqlWhereString =  "WHERE a.inDescription.uuid = :uuid";

        String[] result = new String[4];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        result[3] = sqlWhereString;
        return result;
    }

    /**
     * @param result
     * @return
     */
    public static List<QuantitativeDataDto> quantitativeDataDtoListFrom(List<Object[]> result) {
        List<QuantitativeDataDto> dtoResult = new ArrayList<>();
        QuantitativeDataDto dto = null;

        for (Object[] o: result){
            UUID uuid = (UUID)o[0];
            UUID featureUuid = (UUID)o[1];
            if (dto == null || !dto.getElementUuid().equals(uuid)){
                dto = new QuantitativeDataDto(uuid, new FeatureDto(featureUuid, null, null, null, null, null, null, true, false, true, null, true, false, null, null, null, null));
                dtoResult.add(dto);
            }
            StatisticalMeasurementValueDto statVal = new StatisticalMeasurementValueDto(TermDto.fromTerm((DefinedTermBase)o[4]),(BigDecimal)o[3], (UUID)o[2]) ;
            dto.addValue(statVal);
            if (o[5] != null) {
                dto.setMeasurementUnit(TermDto.fromTerm((DefinedTermBase)o[5]));
            }
        }


        return dtoResult;

    }


}
