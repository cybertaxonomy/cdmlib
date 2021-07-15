/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;

/**
 * see  https://dev.e-taxonomy.eu/redmine/issues/7018
 *
 * @author a.kohlbecker
 * @since Feb 24, 2014
 */
public class SpecimenOrObservationBaseVoter extends CdmPermissionVoter {

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return SpecimenOrObservationBase.class;
    }

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
                //TODO AM: what if original.size()> 1?
            }
        }
        return false;
    }

    @Override
    public boolean isOrpahn(CdmBase object) {
        // we always return true here to allow deleting the reference
        return true;
    }

}
