/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.api.service.l10n.TermRepresentation_L10n;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author k.luther
 * @since 12.04.2019
 *
 */
public class SpecimenTypeDesignationDTO extends TypedEntityReference<SpecimenTypeDesignation>  implements Serializable{
    private static final long serialVersionUID = -2397286652498492934L;

    private List<EntityReference> names;
    private DerivateDTO typeSpecimen;
    private String typeStatus;
    private String typeStatus_L10n;

    /**
     *
     * @param typeDesignation
     * @param typeSpecimenDTO
     *      Can be null
     */
    public SpecimenTypeDesignationDTO(SpecimenTypeDesignation typeDesignation, DerivateDTO typeSpecimenDTO)  {

        super(SpecimenTypeDesignation.class, typeDesignation.getUuid());

        if (typeDesignation.getTypeStatus() != null){
            this.typeStatus = typeDesignation.getTypeStatus().generateTitle();
            TermRepresentation_L10n term_L10n = new TermRepresentation_L10n(typeDesignation.getTypeStatus(), false);
            typeStatus_L10n = term_L10n.getText();

        }
        this.names = new ArrayList<>();
        for (TaxonName name:typeDesignation.getTypifiedNames()){
            names.add(new EntityReference(name.getUuid(), name.getTitleCache()));
        }
        this.typeSpecimen = typeSpecimenDTO;

    }

    public List<EntityReference> getNames() {
        return names;
    }

    public void setNames(List<EntityReference> names) {
        this.names = names;
    }

    public DerivateDTO getTypeSpecimen() {
        return typeSpecimen;
    }

    public void setTypeSpecimen(DerivateDTO typeSpecimen) {
        this.typeSpecimen = typeSpecimen;
    }

    /**
     * @deprecated replaced by getTypeStatus_L10n()
     */
    @Deprecated
    public String getTypeStatus() {
        return typeStatus;
    }

    /**
     * @deprecated replaced by getTypeStatus_L10n()
     */
    @Deprecated
    public void setTypeStatus(String typeStatus) {
        this.typeStatus = typeStatus;
    }


    public void setTypeStatus_L10n(String typeStatus_L10n) {
        this.typeStatus_L10n = typeStatus_L10n;
    }

    public String getTypeStatus_L10n() {
        return typeStatus_L10n;
    }



}
