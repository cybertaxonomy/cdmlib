/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.api.dto.EventDTO;
import eu.etaxonomy.cdm.model.common.EventBase;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class EventDtoLoaderBase {

    protected void load(EventDTO dto, EventBase entity) {

        dto.setTimePeriod(entity.getTimeperiod());

        if(entity.getActor() != null) {
            dto.setActor(entity.getActor().getTitleCache());
        }
    }
}
