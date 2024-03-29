/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author a.kohlbecker
 * @since Mar 18, 2021
 */
public class EventDTO<T extends EventBase> extends TypedEntityReference<T>{

    private static final long serialVersionUID = -756496997548410660L;

    private TimePeriod timePeriod;
    private String actor;

    @SuppressWarnings("unchecked")
    protected EventDTO(Class<? extends EventBase> clazz, UUID uuid) {
        super((Class<T>)clazz, uuid, null);
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }
    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public String getActor() {
        return actor;
    }
    public void setActor(String actor) {
        this.actor = actor;
    }
}