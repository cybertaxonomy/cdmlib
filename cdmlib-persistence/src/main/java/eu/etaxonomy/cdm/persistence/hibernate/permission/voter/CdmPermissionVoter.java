/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthorityParsingException;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;
import eu.etaxonomy.cdm.persistence.hibernate.permission.TargetEntityStates;

/**
 * The <code>CdmPermissionVoter</code> provides access control votes for {@link CdmBase} objects.
 *
 * @author andreas kohlbecker
 * @since Sep 4, 2012
 *
 */
public abstract class CdmPermissionVoter implements AccessDecisionVoter <TargetEntityStates> {

    /**
     *
     */
    private static final EnumSet<CRUD> DELETE = EnumSet.of(CRUD.DELETE);
    public static final Logger logger = Logger.getLogger(CdmPermissionVoter.class);

    @Override
    public boolean supports(ConfigAttribute attribute) {
        // all CdmPermissionVoter support CdmAuthority
        return attribute instanceof CdmAuthority;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        /* NOTE!!!
         * Do not change this, all CdmPermissionVoters must support CdmBase.class
         */
        return clazz.isInstance(CdmBase.class);
    }

    /**
     * Sets the Cdm type, or super type this Voter is responsible for.
     */
    abstract public Class<? extends CdmBase> getResponsibilityClass();


    protected boolean isResponsibleFor(Object securedObject) {
        return getResponsibilityClass().isAssignableFrom(securedObject.getClass());
    }

    protected boolean isResponsibleFor(CdmPermissionClass permissionClass) {
        return getResponsibility().equals(permissionClass);
    }

    /**
     * Get the according CdmPermissionClass matching {@link #getResponsibilityClass()} the cdm class this voter is responsible for.
     * @return
     */
    protected CdmPermissionClass getResponsibility() {
        return CdmPermissionClass.getValueOf(getResponsibilityClass());
    }

    @Override
    public int vote(Authentication authentication, TargetEntityStates targetEntityStates, Collection<ConfigAttribute> attributes) {

        if(!isResponsibleFor(targetEntityStates.getEntity())){
            logger.debug(voterLoggingLabel() + " class missmatch => ACCESS_ABSTAIN");
            return ACCESS_ABSTAIN;
        }

        if (logger.isDebugEnabled()){
            logger.debug(voterLoggingLabel() + " voting for authentication: " + authentication.getName() + ", object : " + targetEntityStates.getEntity().toString() + ", attribute[0]:" + ((CdmAuthority)attributes.iterator().next()).getAttribute());
        }

        int fallThroughVote = ACCESS_DENIED;
        boolean deniedByPreviousFurtherVoting = false;

        // loop over all attributes = permissions of which at least one must match
        // usually there is only one element in the collection!
        for(ConfigAttribute attribute : attributes){
            if(!(attribute instanceof CdmAuthority)){
                throw new RuntimeException("attributes must contain only CdmAuthority");
            }
            CdmAuthority evalPermission = (CdmAuthority)attribute;

            for (GrantedAuthority authority: authentication.getAuthorities()){

                CdmAuthority auth;
                try {
                    auth = CdmAuthority.fromGrantedAuthority(authority);
                } catch (CdmAuthorityParsingException e) {
                    logger.debug(voterLoggingLabel() + " skipping " + authority.getAuthority() + " due to CdmAuthorityParsingException");
                    continue;
                }

                // check if the voter is responsible for the permission to be evaluated
                if( ! isResponsibleFor(evalPermission.getPermissionClass())){
                    logger.debug(voterLoggingLabel() + " not responsible for " + evalPermission.getPermissionClass() + " -> skipping");
                    continue;
                }

                ValidationResult vr = new ValidationResult();

                boolean isALL = auth.getPermissionClass().equals(CdmPermissionClass.ALL);

                vr.isClassMatch = isALL || auth.getPermissionClass().equals(evalPermission.getPermissionClass());
                vr.isPermissionMatch = auth.getOperation().containsAll(evalPermission.getOperation());
                vr.isUuidMatch = auth.hasTargetUuid() && auth.getTargetUUID().equals(targetEntityStates.getEntity().getUuid());
                vr.isIgnoreUuidMatch = !auth.hasTargetUuid();

                if(logger.isDebugEnabled()){
                    logger.debug(voterLoggingLabel() + " " + vr);
                }

                // first of all, always allow deleting orphan entities
                if(vr.isClassMatch && evalPermission.getOperation().equals(DELETE) && isOrpahn(targetEntityStates.getEntity())) {
                    if(logger.isDebugEnabled()){
                        logger.debug(voterLoggingLabel() +" entity is considered orphan => ACCESS_GRANTED");
                    }
                    return ACCESS_GRANTED;
                }

                if(!auth.hasProperty()){
                    if ( vr.isIgnoreUuidMatch && vr.isClassMatch && vr.isPermissionMatch){
                        if(logger.isDebugEnabled()){
                            logger.debug(voterLoggingLabel() +" no targetUuid, class & permission match => ACCESS_GRANTED");
                        }
                        return ACCESS_GRANTED;
                    }
                    if ( vr.isUuidMatch && vr.isClassMatch && vr.isPermissionMatch ){
                        if(logger.isDebugEnabled()){
                            logger.debug(voterLoggingLabel() +" permission, class and uuid are matching => ACCESS_GRANTED");
                        }
                        return ACCESS_GRANTED;
                    }
                } else {
                    //
                    // If the authority contains a property AND the voter is responsible for this class
                    // we must change the fallThroughVote
                    // to ABSTAIN, since no decision can be made in this case at this point
                    // the decision will be delegated to the furtherVotingDescisions() method
                    if(vr.isClassMatch){
                        fallThroughVote = ACCESS_ABSTAIN;
                    }
                }


                //
                // ask subclasses for further voting decisions
                // subclasses will cast votes for specific Cdm Types
                //
                Integer furtherVotingResult = furtherVotingDescisions(auth, targetEntityStates, attributes, vr);
                if(furtherVotingResult != null){
                    if(logger.isDebugEnabled()){
                        logger.debug(voterLoggingLabel() + " furtherVotingResult => " + voteToString(furtherVotingResult));
                    }
                    switch(furtherVotingResult){
                        case ACCESS_GRANTED:
                            // no further check needed
                            return ACCESS_GRANTED;
                        case ACCESS_DENIED:
                            // remember the DENIED vote in case none of
                            // potentially following furtherVotes are
                            // GRANTED
                            deniedByPreviousFurtherVoting = true;
                        //$FALL-THROUGH$
                        case ACCESS_ABSTAIN: /* nothing to do */
                            default: /* nothing to do */
                    }
                }

            } // END Authorities loop
        } // END attributes loop

        int votingResult = deniedByPreviousFurtherVoting ? ACCESS_DENIED : fallThroughVote;
        // the value of fallThroughVote depends on whether the authority had an property or not, see above
        if(logger.isDebugEnabled()){
            logger.debug(voterLoggingLabel() + " fallThroughVote => " + voteToString(fallThroughVote));
            logger.debug(voterLoggingLabel() + " ##votingResult## => " + voteToString(votingResult));
        }
        return votingResult;
    }

    /**
     * The AccessDecisionVoter implementing this method can indicate via this method that
     * an entity has become orphan in order to allow deleting it. In case the implementing method
     * returns <code>false</code> deleting of the entity will be denied.
     * <p>
     * This is important
     * in the context of hierarchic permission propagation like for example in
     * tree structures where the permission to delete an entity is given on base
     * of the permission on an parent object. Entities which become detached
     * from the tree would otherwise no longer be deletable.
     *
     * @param object
     * @return whether the cdm entity is orpahn
     */
    public abstract boolean isOrpahn(CdmBase object);

    /**
     * Override this method to implement specific decisions.
     * Implementations of this method will be executed in {@link #vote(Authentication, TargetEntityStates, Collection)}.
     *
     * @param CdmAuthority
     * @param targetEntityStates
     * @param attributes
     * @param validationResult
     * @return A return value of ACCESS_ABSTAIN or null will be ignored in {@link #vote(Authentication, Object, Collection)}
     */
    protected Integer furtherVotingDescisions(CdmAuthority CdmAuthority, TargetEntityStates targetEntityStates, Collection<ConfigAttribute> attributes,
            ValidationResult validationResult) {
        return null;
    }

    /**
     * returns a label for the logging output
     * @return
     */
    protected String voterLoggingLabel(){
        return "(" + getResponsibilityClass().getSimpleName() + "-Voter)";
    }

    /**
     *
     * @param vote
     * @return string representations for the votes defined in {@link AccessDecisionVoter}
     */
    protected String voteToString(int vote) {
        switch (vote){
            case 1: return "ACCESS_GRANTED";
            case 0: return "ACCESS_ABSTAIN";
            case -1: return "ACCESS_DENIED";
            default: return Integer.toString(vote);
        }
    }


    /**
     * Holds various flags with validation results.
     * Is used to pass this information from
     * {@link CdmPermissionVoter#vote(Authentication, Object, Collection)}
     * to {@link CdmPermissionVoter#furtherVotingDescisions(CdmAuthority, Object, Collection, ValidationResult)}
     *
     * @author andreas kohlbecker
     * @since Sep 5, 2012
     *
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
        public String toString(){
            return "isClassMatch: " + Boolean.toString(isClassMatch) + ", "
                    + "isUuidMatch: " + Boolean.toString(isUuidMatch) + ", "
                    + "isPermissionMatch: " + Boolean.toString(isPermissionMatch) + ", "
                    + "isPropertyMatch: " + Boolean.toString(isPropertyMatch);

        }
    }

}
