/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Based on the {@link UnanimousBased} AccessDecisionManager.
 *
 * In contrast to the UnanimousBased a voter which voted once with
 * <code>ACCESS_GRANTED</code> can not revoke this decision again.
 *
 * @author a.kohlbecker
 \* @since Oct 11, 2013
 *
 */
public class UnanimousBasedUnrevokable extends AbstractAccessDecisionManager {

//    /**
//     *
//     * @deprecated Use constructor which takes voter list
//     * This one is for String internal use only
//     */
//    @Deprecated
//    public UnanimousBasedUnrevokable(){
//
//    }

    public UnanimousBasedUnrevokable(List<AccessDecisionVoter<? extends Object>> decisionVoters) {
        super(decisionVoters);
    }


    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> attributes)
            throws AccessDeniedException {

        int grant = 0;
        int abstain = 0;
        List<ConfigAttribute> singleAttributeList = new ArrayList<>(1);
        singleAttributeList.add(null);

        Map<AccessDecisionVoter<CdmBase>, Integer> voteMap = new HashMap<>();

        for (ConfigAttribute attribute : attributes) {
            singleAttributeList.set(0, attribute);

            for(AccessDecisionVoter voter : getDecisionVoters()) {

                Integer lastResult = voteMap.get(voter);
                if(lastResult != null && lastResult == AccessDecisionVoter.ACCESS_GRANTED){
                    continue;
                }

                int result = voter.vote(authentication, object, singleAttributeList);

                voteMap.put(voter, result);

                if (logger.isDebugEnabled()) {
                    logger.debug("Voter: " + voter + ", returned: " + result);
                }

            }
        }

        for(Integer result : voteMap.values()) {
            switch (result) {
            case AccessDecisionVoter.ACCESS_GRANTED:
                grant++;

                break;

            case AccessDecisionVoter.ACCESS_DENIED:
                throw new AccessDeniedException(messages.getMessage("AbstractAccessDecisionManager.accessDenied",
                        "Access is denied"));

            default:
                abstain++;

                break;
            }
        }

        // To get this far, there were no deny votes
        if (grant > 0) {
            return;
        }

        // To get this far, every AccessDecisionVoter abstained
        checkAllowIfAllAbstainDecisions();
    }

}
