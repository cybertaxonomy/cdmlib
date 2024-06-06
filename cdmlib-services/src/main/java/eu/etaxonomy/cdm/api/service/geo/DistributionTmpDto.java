/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.util.EnumSet;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author muellera
 * @since 28.02.2024
 */
public class DistributionTmpDto {

    private int id;
    private UUID uuid;
    private NamedArea area;
    private PresenceAbsenceTerm status;
    private EnumSet<DescriptionType> descriptionTypes;
    private TimePeriod timePeriod;

    public DistributionTmpDto(UUID uuid, int id, NamedArea area,
            PresenceAbsenceTerm status, EnumSet<DescriptionType> descriptionTypes,
            TimePeriod timePeriod) {
        this.uuid = uuid;
        this.id = id;
//        this.setLastUpdated(null);   //TODO
        this.area = area;
        this.status = status;
        this.descriptionTypes = descriptionTypes;
        this.timePeriod = timePeriod;
    }


    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public NamedArea getArea() {
        return area;
    }

    public PresenceAbsenceTerm getStatus() {
        return status;
    }

    public EnumSet<DescriptionType> getDescriptionType() {
        return descriptionTypes;
    }

    public TimePeriod getTimeperiod() {
        return timePeriod;
    }
}