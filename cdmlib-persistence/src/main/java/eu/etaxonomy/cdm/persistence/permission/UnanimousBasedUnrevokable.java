/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.persistence.permission.voter.CdmPermissionVoter;
import eu.etaxonomy.cdm.persistence.permission.voter.CdmVote;

/**
 * Composite {@link AuthorizationManager} based on the former UnanimousBased AccessDecisionManager.
 * <p>
 * In contrast to plain unanimous voting, a voter which voted once with
 * {@link CdmVote#GRANTED} cannot revoke this decision again.
 * Any {@link CdmVote#DENIED} denies authorization. If all voters abstain,
 * access is denied.
 *
 * @author a.kohlbecker
 * @since Oct 11, 2013
 */
public class UnanimousBasedUnrevokable implements AuthorizationManager<CdmAuthorizationTarget> {

    private static final Logger logger = LogManager.getLogger();

    private final List<CdmPermissionVoter> decisionVoters;

    /**
     * Constructor, called by Spring (see security_base.xml)
     */
    public UnanimousBasedUnrevokable(List<CdmPermissionVoter> decisionVoters) {
        this.decisionVoters = Collections.unmodifiableList(new ArrayList<>(decisionVoters));
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication,
            CdmAuthorizationTarget object) {

        Authentication auth = authentication.get();

        int grant = 0;
        Map<CdmPermissionVoter, CdmVote> voteMap = new HashMap<>();

        for (CdmPermissionVoter voter : decisionVoters) {

            CdmVote lastResult = voteMap.get(voter);
            if (lastResult == CdmVote.GRANTED) {
                continue;
            }

            CdmVote result = voter.vote(auth, object.getTargetEntityStates(), object.getRequiredAuthority());
            voteMap.put(voter, result);

            if (logger.isDebugEnabled()) {
                logger.debug("Voter: " + voter + ", returned: " + result);
            }
        }

        for (CdmVote result : voteMap.values()) {
            switch (result) {
            case GRANTED:
                grant++;
                break;
            case DENIED:
                return new AuthorizationDecision(false);
            case ABSTAIN:
            default:
                break;
            }
        }

        if (grant > 0) {
            return new AuthorizationDecision(true);
        }

        // Every voter abstained → deny
        return new AuthorizationDecision(false);
    }
}
