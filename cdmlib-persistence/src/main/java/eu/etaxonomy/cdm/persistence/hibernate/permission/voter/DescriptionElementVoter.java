package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.TargetEntityStates;

public class DescriptionElementVoter extends CdmPermissionVoter {

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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#isOrpahn(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public boolean isOrpahn(CdmBase object) {
        if(object instanceof DescriptionElementBase){
            return ((DescriptionElementBase)object).getInDescription() == null;
        }
        return false;
    }

}
