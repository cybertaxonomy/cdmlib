/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.EnumSet;
import java.util.UUID;

import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.model.description.DescriptionType;

/**
 * @author a.mueller
 * @date 09.02.2023
 */
public class DistributionDto extends FactDtoBase {

    private NamedAreaDto area;
    private TermDto status;
    //TODO maybe move up to FactDtoBase
    private EnumSet<DescriptionType> descriptionType;

    public DistributionDto(UUID uuid, int id, NamedAreaDto area, TermDto status) {
        this.setUuid(uuid);
        this.setId(id);
        this.setLastUpdated(null);   //TODO
        this.area = area;
        this.setStatus(status);
    }

// *********************** GETTER / SETTER ***************************/

    public NamedAreaDto getArea() {
        return area;
    }
    public void setArea(NamedAreaDto area) {
        this.area = area;
    }

    public TermDto getStatus() {
        return status;
    }
    public void setStatus(TermDto status) {
        this.status = status;
    }

    public EnumSet<DescriptionType> getDescriptionType() {
        return descriptionType;
    }
    public void setDescriptionType(EnumSet<DescriptionType> descriptionType) {
        this.descriptionType = descriptionType;
    }

//*************************** toString() ********************************/



    @Override
    public String toString() {
        return "DistributionDto [area=" + area + ", status=" + status + "]";
    }
}