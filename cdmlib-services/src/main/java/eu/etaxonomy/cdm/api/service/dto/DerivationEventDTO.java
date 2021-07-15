/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;

/**
 * @author k.luther
 * @since 22.06.2018
 */
public class DerivationEventDTO extends EventDTO<DerivationEvent> {

    private static final long serialVersionUID = 6338657672281702600L;

    private String institute;

    protected DerivationEventType eventType;

    private DerivationEventDTO(DerivationEvent entity) {
        super(entity);
        eventType = entity.getType();
        if(eventType != null) {
            eventType.getRepresentations(); // force initialization
        }
        if(entity.getActor() != null) {
            this.actor = entity.getActor().getTitleCache();
        }
        if(entity.getInstitution() != null) {
            this.institute = entity.getInstitution().getTitleCache();
        }
    }

    public static EventDTO<DerivationEvent> fromEntity(DerivationEvent entity) {
        if (entity != null) {
            entity = HibernateProxyHelper.deproxy(entity, DerivationEvent.class);
            return new DerivationEventDTO(entity);
        } else {
            return null;
        }
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public DerivationEventType getEventType() {
        return eventType;
    }

    public void setEventType(DerivationEventType eventType) {
        this.eventType = eventType;
    }

}
