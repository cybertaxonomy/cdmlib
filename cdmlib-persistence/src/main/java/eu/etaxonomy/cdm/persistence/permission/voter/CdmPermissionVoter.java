/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

import java.util.EnumSet;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthorityParsingException;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthorizationTarget;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;

/**
 * Provides access control votes for {@link CdmBase} objects.
 * <p>
 * Implements {@link AuthorizationManager}: {@link CdmVote#ABSTAIN} is returned as
 * {@code null} (Spring convention), grant/deny as {@link AuthorizationDecision}.
 *
 * @author andreas kohlbecker
 * @since Sep 4, 2012
 */
public abstract class CdmPermissionVoter implements AuthorizationManager<CdmAuthorizationTarget> {

    private static final Logger logger = LogManager.getLogger();

    private static final EnumSet<CRUD> DELETE = EnumSet.of(CRUD.DELETE);

    /**
     * Sets the Cdm type, or super type this Voter is responsible for.
     */
    public abstract Class<? extends CdmBase> getResponsibilityClass();

    protected boolean isResponsibleFor(Object securedObject) {
        return getResponsibilityClass().isAssignableFrom(securedObject.getClass());
    }

    protected boolean isResponsibleFor(PermissionClass permissionClass) {
        return getResponsibility().equals(permissionClass);
    }

    /**
     * Get the according CdmPermissionClass matching {@link #getResponsibilityClass()} the cdm class this voter is responsible for.
     */
    protected PermissionClass getResponsibility() {
        return PermissionClass.getValueOf(getResponsibilityClass());
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, CdmAuthorizationTarget object) {
        CdmVote vote = vote(authentication.get(), object.getTargetEntityStates(), object.getRequiredAuthority());
        if (vote == CdmVote.ABSTAIN) {
            return null;
        }
        return new AuthorizationDecision(vote == CdmVote.GRANTED);
    }

    /**
     * Vote on whether {@code authentication} may perform {@code requiredAuthority} on {@code targetEntityStates}.
     */
    public CdmVote vote(Authentication authentication, TargetEntityStates targetEntityStates,
            CdmAuthority requiredAuthority) {

        if (!isResponsibleFor(targetEntityStates.getEntity())) {
            if (logger.isDebugEnabled()) {
                logger.debug(voterLoggingLabel() + " class missmatch => ACCESS_ABSTAIN");
            }
            return CdmVote.ABSTAIN;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(voterLoggingLabel() + " voting for authentication: " + authentication.getName()
                    + ", object : " + targetEntityStates.getEntity().toString()
                    + ", required:" + requiredAuthority.getAttribute());
        }

        CdmVote fallThroughVote = CdmVote.DENIED;
        boolean deniedByPreviousFurtherVoting = false;

        CdmAuthority evalPermission = requiredAuthority;

        for (GrantedAuthority authority : authentication.getAuthorities()) {

            CdmAuthority auth;
            try {
                auth = CdmAuthority.fromGrantedAuthority(authority);
            } catch (CdmAuthorityParsingException e) {
                logger.debug(voterLoggingLabel() + " skipping " + authority.getAuthority()
                        + " due to CdmAuthorityParsingException");
                continue;
            }

            // check if the voter is responsible for the permission to be evaluated
            if (!isResponsibleFor(evalPermission.getPermissionClass())) {
                logger.debug(voterLoggingLabel() + " not responsible for " + evalPermission.getPermissionClass()
                        + " -> skipping");
                continue;
            }

            ValidationResult vr = new ValidationResult();

            boolean isALL = auth.getPermissionClass().equals(PermissionClass.ALL);

            vr.isClassMatch = isALL || auth.getPermissionClass().equals(evalPermission.getPermissionClass());
            vr.isPermissionMatch = auth.getOperation().containsAll(evalPermission.getOperation());
            vr.isUuidMatch = auth.hasTargetUuid()
                    && auth.getTargetUUID().equals(targetEntityStates.getEntity().getUuid());
            vr.isIgnoreUuidMatch = !auth.hasTargetUuid();

            if (logger.isDebugEnabled()) {
                logger.debug(voterLoggingLabel() + " " + vr);
            }

            // first of all, always allow deleting orphan entities
            if (vr.isClassMatch && evalPermission.getOperation().equals(DELETE)
                    && isOrpahn(targetEntityStates.getEntity())) {
                if (logger.isDebugEnabled()) {
                    logger.debug(voterLoggingLabel() + " entity is considered orphan => ACCESS_GRANTED");
                }
                return CdmVote.GRANTED;
            }

            if (!auth.hasProperty()) {
                if (vr.isIgnoreUuidMatch && vr.isClassMatch && vr.isPermissionMatch) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(voterLoggingLabel()
                                + " no targetUuid, class & permission match => ACCESS_GRANTED");
                    }
                    return CdmVote.GRANTED;
                }
                if (vr.isUuidMatch && vr.isClassMatch && vr.isPermissionMatch) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(voterLoggingLabel()
                                + " permission, class and uuid are matching => ACCESS_GRANTED");
                    }
                    return CdmVote.GRANTED;
                }
            } else {
                // If the authority contains a property AND the voter is responsible for this class
                // we must change the fallThroughVote to ABSTAIN; decision is delegated to furtherVotingDescisions()
                if (vr.isClassMatch) {
                    fallThroughVote = CdmVote.ABSTAIN;
                }
            }

            CdmVote furtherVotingResult = furtherVotingDescisions(auth, targetEntityStates, evalPermission, vr);
            if (furtherVotingResult != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(voterLoggingLabel() + " furtherVotingResult => " + furtherVotingResult);
                }
                switch (furtherVotingResult) {
                case GRANTED:
                    return CdmVote.GRANTED;
                case DENIED:
                    deniedByPreviousFurtherVoting = true;
                    break;
                case ABSTAIN:
                default:
                    break;
                }
            }
        }

        CdmVote votingResult = deniedByPreviousFurtherVoting ? CdmVote.DENIED : fallThroughVote;
        if (logger.isDebugEnabled()) {
            logger.debug(voterLoggingLabel() + " fallThroughVote => " + fallThroughVote);
            logger.debug(voterLoggingLabel() + " ##votingResult## => " + votingResult);
        }
        return votingResult;
    }

    /**
     * Indicates that an entity has become orphan in order to allow deleting it.
     * In case the implementing method returns {@code false}, deleting of the entity will be denied.
     * <p>
     * This is important in the context of hierarchic permission propagation (e.g. trees)
     * where the permission to delete an entity is given based on a parent object.
     *
     * @return whether the cdm entity is orphan
     */
    public abstract boolean isOrpahn(CdmBase object);

    /**
     * Override to implement type-specific decisions.
     * {@link CdmVote#ABSTAIN} or {@code null} are ignored in {@link #vote}.
     */
    protected CdmVote furtherVotingDescisions(CdmAuthority userAuthority, TargetEntityStates targetEntityStates,
            CdmAuthority requiredAuthority, ValidationResult validationResult) {
        return null;
    }

    protected String voterLoggingLabel() {
        return "(" + getResponsibilityClass().getSimpleName() + "-Voter)";
    }

    /**
     * Holds various flags with validation results.
     * Passed from {@link #vote} to {@link #furtherVotingDescisions}.
     */
    protected class ValidationResult {

        /**
         * ignore the result of the uuid match test completely
         * this flag becomes true when the authority given to
         * an authentication has no uuid part
         */
        public boolean isIgnoreUuidMatch;
        boolean isPermissionMatch = false;
        boolean isPropertyMatch = false;
        boolean isUuidMatch = false;
        boolean isClassMatch = false;

        @Override
        public String toString() {
            return "isClassMatch: " + Boolean.toString(isClassMatch) + ", "
                    + "isUuidMatch: " + Boolean.toString(isUuidMatch) + ", "
                    + "isPermissionMatch: " + Boolean.toString(isPermissionMatch) + ", "
                    + "isPropertyMatch: " + Boolean.toString(isPropertyMatch);
        }
    }
}
