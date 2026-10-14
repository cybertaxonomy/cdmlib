/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission;

/**
 * Object authorized by {@link org.springframework.security.authorization.AuthorizationManager}:
 * the target entity plus the required {@link CdmAuthority}.
 * <p>
 * Replaces the former {@code AccessDecisionManager.decide(auth, object, attributes)} triple
 * where {@code attributes} held the {@link CdmAuthority}.
 *
 * @author a.mueller
 * @since 07.10.2026
 */
public final class CdmAuthorizationTarget {

    private final TargetEntityStates targetEntityStates;
    private final CdmAuthority requiredAuthority;

    public CdmAuthorizationTarget(TargetEntityStates targetEntityStates, CdmAuthority requiredAuthority) {
        this.targetEntityStates = targetEntityStates;
        this.requiredAuthority = requiredAuthority;
    }

    public TargetEntityStates getTargetEntityStates() {
        return targetEntityStates;
    }

    public CdmAuthority getRequiredAuthority() {
        return requiredAuthority;
    }
}
