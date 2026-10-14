/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;

/**
 * This voter always returns {@link CdmVote#GRANTED}.
 * It is needed as default voter for {@link eu.etaxonomy.cdm.persistence.permission.UnanimousBasedUnrevokable}.
 *
 * @author andreas kohlbecker
 * @since Sep 4, 2012
 */
public class GrantAlwaysVoter extends CdmPermissionVoter {

    @Override
    public CdmVote vote(Authentication authentication, TargetEntityStates object, CdmAuthority requiredAuthority) {
        return CdmVote.GRANTED;
    }

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return CdmBase.class;
    }

    @Override
    public boolean isOrpahn(CdmBase object) {
        return false;
    }
}
