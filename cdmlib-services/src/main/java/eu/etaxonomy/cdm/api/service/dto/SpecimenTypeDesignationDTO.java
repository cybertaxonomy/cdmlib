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

    /**
     * @param uuid
     * @param label
     */
    public SpecimenTypeDesignationDTO(SpecimenTypeDesignation typeDesignation, DerivateDTO derivateDTO)  {

        super(SpecimenTypeDesignation.class, typeDesignation.getUuid());

        if (typeDesignation.getTypeStatus() != null){
            this.typeStatus = typeDesignation.getTypeStatus().generateTitle();
        }
        this.names = new ArrayList();
        for (TaxonName name:typeDesignation.getTypifiedNames()){
            names.add(new EntityReference(name.getUuid(), name.getTitleCache()));
        }
        this.typeSpecimen = derivateDTO;

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

    public String getTypeStatus() {
        return typeStatus;
    }

    public void setTypeStatus(String typeStatus) {
        this.typeStatus = typeStatus;
    }







}
