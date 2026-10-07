/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

/**
 * Vote of a {@link CdmPermissionVoter}. Replaces the int constants of the
 * deprecated {@code AccessDecisionVoter} ({@code ACCESS_GRANTED}/{@code ACCESS_DENIED}/{@code ACCESS_ABSTAIN}).
 *
 * @author a.mueller
 * @since 08.10.2026
 */
public enum CdmVote {

    GRANTED,
    DENIED,
    ABSTAIN;

    @Override
    public String toString() {
        return "ACCESS_" + name();
    }
}
