/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.persistence.dto.DescriptionBaseDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author pplitzner
 * @since 16.04.2018
 */
public class SpecimenRowWrapperDTO extends RowWrapperDTO<SpecimenDescription> {

    private static final long serialVersionUID = 5198447592554976471L;

    private TaxonRowWrapperDTO defaultDescription;
    private SpecimenOrObservationBaseDTO specimenDto;
    private UuidAndTitleCache<FieldUnit> fieldUnit;
    private SpecimenOrObservationType type;
    private String identifier;
    private NamedArea country;

    public SpecimenRowWrapperDTO(DescriptionBaseDto description, SpecimenOrObservationBaseDTO specimen, SpecimenOrObservationType type, TaxonNodeDto taxonNode, FieldUnit fieldUnit, String identifier,
                NamedArea country, Language lang) {
        super(description, taxonNode, lang);
        if (fieldUnit != null){
            this.fieldUnit = new UuidAndTitleCache<>(fieldUnit.getUuid(), fieldUnit.getId(), fieldUnit.getTitleCache());
        }
        this.identifier = identifier;
        this.country = country;
        this.specimenDto = specimen;
        this.type = type;
    }


    public SpecimenRowWrapperDTO(SpecimenOrObservationBase specimen, TaxonNodeDto taxonNode, FieldUnit fieldUnit, String identifier,
            NamedArea country, Language lang) {
        super(new DescriptionBaseDto(new UuidAndTitleCache<SpecimenOrObservationBase>(specimen.getUuid(), specimen.getId(), specimen.getTitleCache())), taxonNode, lang);
        if (fieldUnit != null){
            this.fieldUnit = new UuidAndTitleCache<>(fieldUnit.getUuid(), fieldUnit.getId(), fieldUnit.getTitleCache());
        }
        this.identifier = identifier;
        this.country = country;
        this.specimenDto = SpecimenOrObservationDTOFactory.fromEntity(specimen);
        this.type = specimen.getRecordBasis();
    }

    public SpecimenRowWrapperDTO(SpecimenOrObservationBase specimen, TaxonNodeDto taxonNode, UuidAndTitleCache<FieldUnit> fieldUnit, String identifier,
            NamedArea country, Language lang) {
        super(new DescriptionBaseDto(new UuidAndTitleCache<SpecimenOrObservationBase>(specimen.getUuid(), specimen.getId(), specimen.getTitleCache())), taxonNode, lang);
        if (fieldUnit != null){
            this.fieldUnit = fieldUnit;
        }
        this.identifier = identifier;
        this.country = country;
        this.specimenDto = SpecimenOrObservationDTOFactory.fromEntity(specimen);
        this.type = specimen.getRecordBasis();

    }
    public SpecimenOrObservationBaseDTO getSpecimenDto() {
        return specimenDto;
    }

    public UuidAndTitleCache<FieldUnit> getFieldUnit() {
        return fieldUnit;
    }

    public String getIdentifier() {
        return identifier;
    }

    public NamedArea getCountry() {
        return country;
    }

    public TaxonRowWrapperDTO getDefaultDescription() {
        return defaultDescription;
    }
    public void setDefaultDescription(TaxonRowWrapperDTO defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public SpecimenOrObservationType getType(){
        return type;
    }
}