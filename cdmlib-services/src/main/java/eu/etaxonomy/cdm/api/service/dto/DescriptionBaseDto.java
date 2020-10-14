/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author k.luther
 * @since May 15, 2020
 */
public class DescriptionBaseDto implements Serializable{

    private UuidAndTitleCache<Taxon> taxonUuid;
    private DerivateDTO specimenDto;

    private UuidAndTitleCache<TaxonName> nameUuid;
    private DescriptionBase description; // TODO use DTO instead


    public DescriptionBaseDto(DescriptionBase description){
        this.description = description;
        if (description instanceof TaxonDescription){
            Taxon taxon = ((TaxonDescription)description).getTaxon();
            taxonUuid = new UuidAndTitleCache<>(taxon.getUuid(), taxon.getId(), taxon.getTitleCache());
        }else if (description instanceof SpecimenDescription){
            SpecimenOrObservationBase sob = ((SpecimenDescription)description).getDescribedSpecimenOrObservation();
            specimenDto = SpecimenOrObservationDTOFactory.fromEntity((FieldUnit)sob);
        }else if (description instanceof TaxonNameDescription){
            TaxonName name = ((TaxonNameDescription)description).getTaxonName();
            nameUuid = new UuidAndTitleCache<>(name.getUuid(), name.getId(), name.getTitleCache());
        }


    }

    public DescriptionBaseDto(SpecimenOrObservationBase specimen, Set<DescriptiveDataSet> dataSets, boolean isDefault, boolean isImageGallery ){

        description = SpecimenDescription.NewInstance(specimen);
        if(specimen instanceof FieldUnit) {
            specimenDto = FieldUnitDTO.fromEntity((FieldUnit)specimen);
        } else {
            specimenDto = PreservedSpecimenDTO.fromEntity((DerivedUnit)specimen);
        }
    }



    public DescriptionBaseDto(SpecimenOrObservationBase specimen){
        this(specimen, null, false, false);
    }


    public DescriptionBase getDescription() {
        return description;
    }

    public void setDescription(DescriptionBase desc) {
        description = desc;
    }

    public UuidAndTitleCache<Taxon> getTaxonDto() {
        return taxonUuid;
    }

    public DerivateDTO getSpecimenDto() {
        return specimenDto;
    }

    public UuidAndTitleCache<TaxonName> getNameDto() {
        return nameUuid;
    }








}
