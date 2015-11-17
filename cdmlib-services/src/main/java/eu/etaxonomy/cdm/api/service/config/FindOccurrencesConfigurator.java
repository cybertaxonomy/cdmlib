// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * @author pplitzner
 * @date Apr 13, 2015
 *
 */
public class FindOccurrencesConfigurator extends IdentifiableServiceConfiguratorImpl<SpecimenOrObservationBase>{

    private SpecimenOrObservationType specimenType;
    private UUID associatedTaxonUuid;
    private UUID associatedTaxonNameUuid;
    private boolean isDetermined;
    private String significantIdentifier;
    private boolean retrieveIndirectlyAssociatedSpecimens;
    public String getSignificantIdentifier() {
        return significantIdentifier;
    }
    public void setSignificantIdentifier(String significantIdentifier) {
        this.significantIdentifier = significantIdentifier;
    }
    public synchronized SpecimenOrObservationType getSpecimenType() {
        return specimenType;
    }
    public synchronized void setSpecimenType(SpecimenOrObservationType specimenType) {
        this.specimenType = specimenType;
    }
    public synchronized UUID getAssociatedTaxonUuid() {
        return associatedTaxonUuid;
    }
    public synchronized void setAssociatedTaxonUuid(UUID associatedTaxonUuid) {
        this.associatedTaxonUuid = associatedTaxonUuid;
    }
    public UUID getAssociatedTaxonNameUuid() {
        return associatedTaxonNameUuid;
    }
    public void setAssociatedTaxonNameUuid(UUID associatedTaxonNameUuid) {
        this.associatedTaxonNameUuid = associatedTaxonNameUuid;
    }
    public synchronized boolean isDetermined() {
        return isDetermined;
    }
    public synchronized void setDetermined(boolean isDetermined) {
        this.isDetermined = isDetermined;
    }

    /**
     * if set to <code>true</code> the complete derivative hierarchy including
     * all parent and child derivatives is returned of the associated specimen
     * is retrieved
     *
     * @return the value of this flag
     */
    public boolean isRetrieveIndirectlyAssociatedSpecimens() {
        return retrieveIndirectlyAssociatedSpecimens;
    }
    /**
     * if set to <code>true</code> the complete derivative hierarchy including
     * all parent and child derivatives is returned of the associated specimen
     * is retrieved
     *
     * @param retrieveIndirectlyAssociatedSpecimens the value of this flag
     */
    public void setRetrieveIndirectlyAssociatedSpecimens(boolean retrieveIndirectlyAssociatedSpecimens) {
        this.retrieveIndirectlyAssociatedSpecimens = retrieveIndirectlyAssociatedSpecimens;
    }

}
