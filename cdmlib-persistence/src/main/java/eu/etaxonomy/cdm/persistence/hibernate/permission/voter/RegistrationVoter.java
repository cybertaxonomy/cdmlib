/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.TargetEntityStates;

/**
 * @author a.kohlbecker
 * @since Feb 24, 2014
 *
 */
public class RegistrationVoter extends CdmPermissionVoter {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#getResponsibilityClass()
     */
    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return Registration.class;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#isOrpahn(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public boolean isOrpahn(CdmBase object) {
        return ((Registration)object).getTypeDesignations().size() > 0 && ((Registration)object).getName() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer furtherVotingDescisions(CdmAuthority cdmAuthority, TargetEntityStates targetEntityStates, Collection<ConfigAttribute> attributes, ValidationResult vr) {

        // we only need to implement the case where a property is contained in the authority
        // the other case is covered by the CdmPermissionVoter
        if(cdmAuthority.hasProperty() && targetEntityStates.getEntity() instanceof Registration){

            RegistrationStatus status = ((Registration)targetEntityStates.getEntity()).getStatus();
            vr.isPropertyMatch = cdmAuthority.getProperty().contains(status.name());
            logger.debug("property is matching");

            if(vr.isPropertyMatch){
                if(vr.isIgnoreUuidMatch){
                    logger.debug("ignoring the uuid match result");
                    return ACCESS_GRANTED;
                }
                if(vr.isUuidMatch){
                    return ACCESS_GRANTED;
                } else {
                    return ACCESS_DENIED;
                }
            } else {
                return ACCESS_DENIED;
            }

        }

        return ACCESS_ABSTAIN; // ignore my further vote
    }



}
