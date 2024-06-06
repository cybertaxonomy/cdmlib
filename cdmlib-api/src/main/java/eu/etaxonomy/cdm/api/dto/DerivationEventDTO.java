/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;

/**
 * @author k.luther
 * @since 22.06.2018
 */
public class DerivationEventDTO extends EventDTO<DerivationEvent> {

    private static final long serialVersionUID = 6338657672281702600L;

    private String institute;

    private String eventType;

    public DerivationEventDTO(Class<DerivationEvent> clazz, UUID uuid) {
        super(clazz, uuid);
    }

    // ****************** GETTER / SETTER **************************/

    public String getInstitute() {
        return institute;
    }
    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getEventType() {
        return eventType;
    }
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}