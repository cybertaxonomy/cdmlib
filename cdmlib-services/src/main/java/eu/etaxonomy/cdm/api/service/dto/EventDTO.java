/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

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

    /**
     * @param type
     * @param uuid
     */
    public EventDTO(Class<T> type, UUID uuid) {
        super(type, uuid);
    }

    private TimePeriod timePeriod;
    protected String actor;

    protected EventDTO(T entity) {
        super(entity);
        timePeriod = entity.getTimeperiod();
        if(entity.getActor() != null) {
            actor = entity.getActor().getTitleCache();
        }
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
