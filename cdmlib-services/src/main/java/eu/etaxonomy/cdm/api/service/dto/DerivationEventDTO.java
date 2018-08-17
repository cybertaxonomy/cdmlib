/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;

/**
 * @author k.luther
 * @since 22.06.2018
 *
 */
public class DerivationEventDTO {


    private String derivationEventType;
    private String derivationEventActor;
    private String derivationEventInstitute;

    /**
     * @param derivationEventType
     * @param derivationEventActor
     * @param derivationEventInstitute
     */
    public DerivationEventDTO(String derivationEventType, String derivationEventActor,
            String derivationEventInstitute) {

        this.derivationEventType = derivationEventType;
        this.derivationEventActor = derivationEventActor;
        this.derivationEventInstitute = derivationEventInstitute;
    }

    /**
     * @param derivationEvent
     */
    public DerivationEventDTO(DerivationEvent derivationEvent) {

        this(derivationEvent.getType()!= null? derivationEvent.getType().getTitleCache() : null, derivationEvent.getActor() != null ? derivationEvent.getActor().getTitleCache() : null, derivationEvent.getInstitution() != null ? derivationEvent.getInstitution().getTitleCache(): null);

    }

    public String getDerivationEventType() {
        return derivationEventType;
    }
    public void setDerivationEventType(String derivationEventType) {
        this.derivationEventType = derivationEventType;
    }
    public String getDerivationEventActor() {
        return derivationEventActor;
    }
    public void setDerivationEventActor(String derivationEventActor) {
        this.derivationEventActor = derivationEventActor;
    }
    public String getDerivationEventInstitute() {
        return derivationEventInstitute;
    }
    public void setDerivationEventInstitute(String derivationEventInstitute) {
        this.derivationEventInstitute = derivationEventInstitute;
    }

}
