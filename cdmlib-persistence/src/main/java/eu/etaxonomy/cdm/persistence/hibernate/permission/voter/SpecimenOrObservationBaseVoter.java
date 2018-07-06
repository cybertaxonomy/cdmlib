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
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.TargetEntityStates;

/**
 * see  https://dev.e-taxonomy.eu/redmine/issues/7018
 *
 * @author a.kohlbecker
 * @since Feb 24, 2014
 *
 */
public class SpecimenOrObservationBaseVoter extends CdmPermissionVoter {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#getResponsibilityClass()
     */
    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return SpecimenOrObservationBase.class;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#furtherVotingDescisions(org.springframework.security.core.Authentication, java.lang.Object, java.util.Collection, eu.etaxonomy.cdm.persistence.hibernate.permission.voter.TaxonBaseVoter.ValidationResult)
     */
    @Override
    protected Integer furtherVotingDescisions(CdmAuthority CdmAuthority, TargetEntityStates targetEntityStates, Collection<ConfigAttribute> attributes,
            ValidationResult validationResult) {

        boolean isUuidMatchInOriginals = CdmAuthority.hasTargetUuid()
                && propagateGrantsFromOriginal(CdmAuthority.getTargetUUID(), (SpecimenOrObservationBase)targetEntityStates.getEntity());
        if ( isUuidMatchInOriginals  && validationResult.isClassMatch && validationResult.isPermissionMatch){
            logger.debug("permission, class and uuid in originals are matching => ACCESS_GRANTED");
            return ACCESS_GRANTED;
        }
        return null;
    }

    /**
     * @param targetUuid
     * @param sob
     * @return
     */
    private boolean propagateGrantsFromOriginal(UUID targetUuid, SpecimenOrObservationBase<?>  sob){

        if (targetUuid.equals(sob.getUuid())) {
            return true;
        } else {
            if(sob instanceof DerivedUnit) {
                Set<SpecimenOrObservationBase> originals = HibernateProxyHelper.deproxy(sob, DerivedUnit.class).getOriginals();
                if(originals != null && originals.size() == 1){
                    SpecimenOrObservationBase original = originals.iterator().next();
                    return  propagateGrantsFromOriginal(targetUuid, original);
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#isOrpahn(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public boolean isOrpahn(CdmBase object) {
        // we always return true here to allow deleting the reference
        return true;
    }

}
