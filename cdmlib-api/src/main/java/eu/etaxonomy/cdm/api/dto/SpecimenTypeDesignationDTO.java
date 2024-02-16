/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author k.luther
 * @since 12.04.2019
 */
public class SpecimenTypeDesignationDTO
            extends TypedEntityReference<SpecimenTypeDesignation>{

    private static final long serialVersionUID = -2397286652498492934L;

    private List<EntityReference> names;
    private TypedEntityReference<DerivedUnit> typeSpecimen;
    private String typeStatus_L10n;
    private SourceDTO designationSource;
    private List<RegistrationDTO> registrations;

    public SpecimenTypeDesignationDTO(Class<SpecimenTypeDesignation> type, UUID uuid, String label) {
        super(type, uuid, label);
    }

    public List<EntityReference> getNames() {
        return names;
    }

    public void setNames(List<EntityReference> names) {
        this.names = names;
    }

    public TypedEntityReference<DerivedUnit> getTypeSpecimen() {
        return typeSpecimen;
    }

    public void setTypeSpecimen(TypedEntityReference<DerivedUnit> typeSpecimen) {
        this.typeSpecimen = typeSpecimen;
    }

    public void setTypeStatus_L10n(String typeStatus_L10n) {
        this.typeStatus_L10n = typeStatus_L10n;
    }

    public String getTypeStatus_L10n() {
        return typeStatus_L10n;
    }

    public SourceDTO getDesignationSource() {
        return designationSource;
    }

    public void setDesignationSource(SourceDTO source) {
        this.designationSource = source;
    }

    public List<RegistrationDTO> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<RegistrationDTO> registrations) {
        this.registrations = registrations;
    }
}