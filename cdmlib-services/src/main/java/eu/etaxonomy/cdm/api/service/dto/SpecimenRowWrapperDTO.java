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
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author pplitzner
 * @since 16.04.2018
 *
 */
public class SpecimenRowWrapperDTO extends RowWrapperDTO<SpecimenDescription> {

    private static final long serialVersionUID = 5198447592554976471L;

    private TaxonRowWrapperDTO defaultDescription;
    private SpecimenOrObservationBase specimen;
    private FieldUnit fieldUnit;
    private String identifier;
    private NamedArea country;

    public SpecimenRowWrapperDTO(SpecimenDescription description, TaxonNodeDto taxonNode, FieldUnit fieldUnit, String identifier,
                NamedArea country) {
        super(description, taxonNode);
        this.fieldUnit = fieldUnit;
        this.identifier = identifier;
        this.country = country;
        this.specimen = description.getDescribedSpecimenOrObservation();
    }

    public SpecimenOrObservationBase getSpecimen() {
        return specimen;
    }

    public FieldUnit getFieldUnit() {
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
}
