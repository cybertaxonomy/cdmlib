/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author k.luther
 * @since Aug 18, 2021
 */
public class StatisticalMeasurementValueDto implements Serializable {

    private static final long serialVersionUID = -7366908849667176718L;

    private TermDto type;
    private BigDecimal value;

    public StatisticalMeasurementValueDto(TermDto typeDto, BigDecimal value){
        type = typeDto;
        this.value = value;
    }

    public static StatisticalMeasurementValueDto fromStatisticalMeasurementValue(StatisticalMeasurementValue statValue){
        StatisticalMeasurementValueDto result = new StatisticalMeasurementValueDto(TermDto.fromTerm(statValue.getType()), statValue.getValue());
        return result;
    }

    public TermDto getType() {
        return type;
    }
    public void setType(TermDto typeDto) {
        this.type = typeDto;
    }
    public BigDecimal getValue() {
        return value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }

}
