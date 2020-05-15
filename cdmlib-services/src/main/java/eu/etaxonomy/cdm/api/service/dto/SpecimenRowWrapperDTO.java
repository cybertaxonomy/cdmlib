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

import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author pplitzner
 * @since 16.04.2018
 *
 */
public class SpecimenRowWrapperDTO extends RowWrapperDTO<SpecimenDescription> {

    private static final long serialVersionUID = 5198447592554976471L;

    private TaxonRowWrapperDTO defaultDescription;
    private UuidAndTitleCache<SpecimenOrObservationBase> specimen;
    private UuidAndTitleCache<FieldUnit> fieldUnit;
    private SpecimenOrObservationType type;
    private String identifier;
    private NamedArea country;

    public SpecimenRowWrapperDTO(SpecimenDescription description, TaxonNodeDto taxonNode, FieldUnit fieldUnit, String identifier,
                NamedArea country) {
        super(description, taxonNode);
        this.fieldUnit = new UuidAndTitleCache<>(fieldUnit.getUuid(), fieldUnit.getId(), fieldUnit.getTitleCache());
        this.identifier = identifier;
        this.country = country;
        this.specimen = new UuidAndTitleCache<>(description.getDescribedSpecimenOrObservation().getUuid(), description.getDescribedSpecimenOrObservation().getId(), description.getDescribedSpecimenOrObservation().getTitleCache()) ;
        this.type = description.getDescribedSpecimenOrObservation().getRecordBasis();
    }


    public SpecimenRowWrapperDTO(SpecimenOrObservationBase specimen, TaxonNodeDto taxonNode, FieldUnit fieldUnit, String identifier,
            NamedArea country) {
        super(SpecimenDescription.NewInstance(specimen), taxonNode);
        this.fieldUnit = new UuidAndTitleCache<>(fieldUnit.getUuid(), fieldUnit.getId(), fieldUnit.getTitleCache());
        this.identifier = identifier;
        this.country = country;
        this.specimen = new UuidAndTitleCache<SpecimenOrObservationBase>(specimen.getUuid(), specimen.getId(), specimen.getTitleCache());
        this.type = specimen.getRecordBasis();
    }

    public SpecimenRowWrapperDTO(UuidAndTitleCache<SpecimenOrObservationBase> specimen, SpecimenOrObservationType type, TaxonNodeDto taxonNode, UuidAndTitleCache<FieldUnit> fieldUnit, String identifier,
            NamedArea country) {
    super(SpecimenDescription.NewInstance(), taxonNode);
    this.fieldUnit = fieldUnit;
    this.identifier = identifier;
    this.country = country;
    this.specimen = specimen;
    this.type = type;

}
    public UuidAndTitleCache<SpecimenOrObservationBase> getSpecimen() {
        return specimen;
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

    public void setDefaultDescription(TaxonRowWrapperDTO defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public TaxonRowWrapperDTO getDefaultDescription() {
        return defaultDescription;
    }

    public SpecimenOrObservationType getType(){
        return type;
    }
}
