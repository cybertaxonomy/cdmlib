/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

import java.util.Collection;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;

public class DescriptionElementVoter extends CdmPermissionVoter {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return DescriptionElementBase.class;
    }

    @Override
    protected Integer furtherVotingDescisions(CdmAuthority ap, TargetEntityStates targetEntityStates, Collection<ConfigAttribute> attributes,
            ValidationResult vr) {

        // we only need to implement the case where a property is contained in the authority
        // the other case is covered by the CdmPermissionVoter
        if(ap.hasProperty() && targetEntityStates.getEntity() instanceof DescriptionElementBase){

            Feature feature = ((DescriptionElementBase)targetEntityStates.getEntity()).getFeature();

            if(feature == null){
                // if the user is granted for a specific feature
                // he should not be granted for DescriptoinElements without a feature
                return ACCESS_DENIED;
            }
            boolean isPropertyMatch = false;

            try {
                UUID featureUUID = UUID.fromString(ap.getProperty());
                isPropertyMatch = featureUUID.equals(feature.getUuid());
                // FIXME uuids as property not yes supported in AuhorityPermission !!!!
            } catch (IllegalArgumentException e) {
                // Property is not a uuid, so treat is as Label:
                isPropertyMatch = ap.getProperty().equals(feature.getLabel());
            }

            if ( !ap.hasTargetUuid() && vr.isClassMatch && vr.isPermissionMatch && isPropertyMatch){
                logger.debug("no targetUuid, class & permission match => ACCESS_GRANTED");
                return ACCESS_GRANTED;
            }
            if ( vr.isUuidMatch  && vr.isClassMatch && vr.isPermissionMatch && isPropertyMatch){
                logger.debug("permission, class and uuid are matching => ACCESS_GRANTED");
                return ACCESS_GRANTED;
            }

            // the CdmAuthority has a property like but this is not matching the feature name
            // so access is denied
            logger.debug("permission, class and uuid are matching => ACCESS_GRANTED");
            return ACCESS_DENIED;
        }

        // nothing to do here since the CdmAuthority has no property naming a feature
        // this case however should never ever happen if the implementation of the CdmPermissionVoter
        // works correctly.
        return ACCESS_ABSTAIN;
    }

    @Override
    public boolean isOrpahn(CdmBase object) {
        if(object instanceof DescriptionElementBase){
            return ((DescriptionElementBase)object).getInDescription() == null;
        }
        return false;
    }

}
