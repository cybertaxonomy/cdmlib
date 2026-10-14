/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;

/**
 * @author a.kohlbecker
 * @since Feb 24, 2014
 */
public class RegistrationVoter extends CdmPermissionVoter {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return Registration.class;
    }

    @Override
    public boolean isOrpahn(CdmBase object) {
        return ((Registration) object).getTypeDesignations().size() > 0
                && ((Registration) object).getName() == null;
    }

    @Override
    protected CdmVote furtherVotingDescisions(CdmAuthority cdmAuthority, TargetEntityStates targetEntityStates,
            CdmAuthority requiredAuthority, ValidationResult vr) {

        // we only need to implement the case where a property is contained in the authority
        // the other case is covered by the CdmPermissionVoter
        if (cdmAuthority.hasProperty() && targetEntityStates.getEntity() instanceof Registration) {

            RegistrationStatus status;
            if (targetEntityStates.propertyChanged("status")) {
                status = targetEntityStates.previousPropertyState("status", RegistrationStatus.class);
            } else {
                status = ((Registration) targetEntityStates.getEntity()).getStatus();
            }
            vr.isPropertyMatch = cdmAuthority.getProperty().contains(status.name());
            logger.debug("property is matching");

            if (vr.isPropertyMatch) {
                if (vr.isIgnoreUuidMatch) {
                    logger.debug("ignoring the uuid match result");
                    return CdmVote.GRANTED;
                }
                if (vr.isUuidMatch) {
                    return CdmVote.GRANTED;
                } else {
                    return CdmVote.DENIED;
                }
            } else {
                return CdmVote.DENIED;
            }
        }

        return CdmVote.ABSTAIN; // ignore my further vote
    }
}
