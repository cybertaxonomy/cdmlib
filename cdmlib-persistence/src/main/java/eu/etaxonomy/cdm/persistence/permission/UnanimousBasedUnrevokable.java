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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

/**
 * Based on the former {@link UnanimousBased} AccessDecisionManager, now as
 * {@link AuthorizationManager} (Spring Security 5.8+).
 * <p>
 * In contrast to UnanimousBased a voter which voted once with
 * {@code ACCESS_GRANTED} can not revoke this decision again.
 * Any {@code ACCESS_DENIED} denies authorization. If all voters abstain,
 * access is denied (same as {@code allowIfAllAbstainDecisions=false}).
 *
 * @author a.kohlbecker
 * @since Oct 11, 2013
 */
public class UnanimousBasedUnrevokable implements AuthorizationManager<CdmAuthorizationTarget> {

    private static final Logger logger = LogManager.getLogger();

    private final List<AccessDecisionVoter<? extends Object>> decisionVoters;

    /**
     * Constructor, called by Spring (see security_base.xml)
     */
    public UnanimousBasedUnrevokable(List<AccessDecisionVoter<? extends Object>> decisionVoters) {
        this.decisionVoters = Collections.unmodifiableList(new ArrayList<>(decisionVoters));
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication,
            CdmAuthorizationTarget object) {

        Authentication auth = authentication.get();
        Collection<ConfigAttribute> attributes = Collections.singletonList(object.getRequiredAuthority());

        int grant = 0;
        List<ConfigAttribute> singleAttributeList = new ArrayList<>(1);
        singleAttributeList.add(null);

        Map<AccessDecisionVoter<?>, Integer> voteMap = new HashMap<>();

        for (ConfigAttribute attribute : attributes) {
            singleAttributeList.set(0, attribute);

            for (AccessDecisionVoter voter : decisionVoters) {

                Integer lastResult = voteMap.get(voter);
                if (lastResult != null && lastResult == AccessDecisionVoter.ACCESS_GRANTED) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                int result = voter.vote(auth, object.getTargetEntityStates(), singleAttributeList);

                voteMap.put(voter, result);

                if (logger.isDebugEnabled()) {
                    logger.debug("Voter: " + voter + ", returned: " + result);
                }
            }
        }

        for (Integer result : voteMap.values()) {
            switch (result) {
            case AccessDecisionVoter.ACCESS_GRANTED:
                grant++;
                break;

            case AccessDecisionVoter.ACCESS_DENIED:
                return new AuthorizationDecision(false);

            default:
                // abstain
                break;
            }
        }

        // To get this far, there were no deny votes
        if (grant > 0) {
            return new AuthorizationDecision(true);
        }

        // Every AccessDecisionVoter abstained → deny (legacy allowIfAllAbstainDecisions=false)
        return new AuthorizationDecision(false);
    }
}
