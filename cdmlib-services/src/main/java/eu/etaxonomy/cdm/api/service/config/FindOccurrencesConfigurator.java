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

    private static final long serialVersionUID = 1L;

    private SpecimenOrObservationType specimenType;
    private UUID associatedTaxonUuid;
    private UUID associatedTaxonNameUuid;
    private String significantIdentifier;
    private boolean retrieveIndirectlyAssociatedSpecimens;
    public static enum AssignmentStatus{
        ALL_SPECIMENS,
        ASSIGNED_SPECIMENS,
        UNASSIGNED_SPECIMENS
    }
    private AssignmentStatus assignmentStatus = AssignmentStatus.ALL_SPECIMENS;

    /**
     * Enum to indicate if the retrieved specimens are associated to a taxon. Default is <code>ALL_SPECIMEN</code>.<br>
     * <br>
     * <b>Note:</b>This status is <b>ignored</b> if the configurator has either a name
     * or a taxon set via {@link #setAssociatedTaxonNameUuid(UUID)} or
     * {@link #setAssociatedTaxonUuid(UUID)}
     *
     * @param associatedWithTaxon
     */

    public void setAssignmentStatus(AssignmentStatus assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }
    public AssignmentStatus getAssignmentStatus() {
        return assignmentStatus;
    }

    public String getSignificantIdentifier() {
        return significantIdentifier;
    }
    public void setSignificantIdentifier(String significantIdentifier) {
        this.significantIdentifier = significantIdentifier;
    }
    public SpecimenOrObservationType getSpecimenType() {
        return specimenType;
    }
    public void setSpecimenType(SpecimenOrObservationType specimenType) {
        this.specimenType = specimenType;
    }
    public UUID getAssociatedTaxonUuid() {
        return associatedTaxonUuid;
    }
    public void setAssociatedTaxonUuid(UUID associatedTaxonUuid) {
        this.associatedTaxonUuid = associatedTaxonUuid;
    }
    public UUID getAssociatedTaxonNameUuid() {
        return associatedTaxonNameUuid;
    }
    public void setAssociatedTaxonNameUuid(UUID associatedTaxonNameUuid) {
        this.associatedTaxonNameUuid = associatedTaxonNameUuid;
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
