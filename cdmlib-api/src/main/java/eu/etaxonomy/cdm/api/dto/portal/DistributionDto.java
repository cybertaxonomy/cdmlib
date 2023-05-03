/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.UUID;

import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;

/**
 * @author a.mueller
 * @date 09.02.2023
 */
public class DistributionDto extends FactDtoBase {

    private NamedAreaDto area;
    private LabeledEntityDto status;

    public DistributionDto(UUID uuid, int id, NamedAreaDto area, LabeledEntityDto status) {
        this.setUuid(uuid);
        this.setId(id);
        this.setLastUpdated(null);   //TODO
        this.area = area;
        this.setStatus(status);
    }

    public DistributionDto(Distribution distribution) {
        this.setUuid(distribution.getUuid());
        this.setId(distribution.getId());
        this.setLastUpdated(null);   //TODO
        if (distribution.getArea() != null) {
            this.area = new NamedAreaDto(distribution.getArea(), null);
        }
        if (distribution.getStatus() != null) {
            PresenceAbsenceTerm distStatus = distribution.getStatus();
            //TODO i18n
            this.status = new LabeledEntityDto(distStatus.getUuid(), distStatus.getId(), distStatus.getLabel());
        }
    }

// *********************** GETTER / SETTER ***************************/

    public NamedAreaDto getArea() {
        return area;
    }
    public void setArea(NamedAreaDto area) {
        this.area = area;
    }

    public LabeledEntityDto getStatus() {
        return status;
    }
    public void setStatus(LabeledEntityDto status) {
        this.status = status;
    }

//*************************** toString() ********************************/

    @Override
    public String toString() {
        return "DistributionDto [area=" + area + ", status=" + status + "]";
    }


}