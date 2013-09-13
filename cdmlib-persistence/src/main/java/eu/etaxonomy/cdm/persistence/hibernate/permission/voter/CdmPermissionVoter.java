// $Id$
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

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import sun.security.provider.PolicyParser.ParsingException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;

/**
 * The <code>CdmPermissionVoter</code> provides access control votes for {@link CdmBase} objects.
 *
 * @author andreas kohlbecker
 * @date Sep 4, 2012
 *
 */
public abstract class CdmPermissionVoter implements AccessDecisionVoter <CdmBase> {

    public static final Logger logger = Logger.getLogger(CdmPermissionVoter.class);

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#supports(org.springframework.security.access.ConfigAttribute)
     */
    @Override
    public boolean supports(ConfigAttribute attribute) {
        // all CdmPermissionVoter support CdmAuthority
        return attribute instanceof CdmAuthority;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#supports(java.lang.Class)
     */
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

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#vote(org.springframework.security.core.Authentication, java.lang.Object, java.util.Collection)
     */
    @Override
    public int vote(Authentication authentication, CdmBase object, Collection<ConfigAttribute> attributes) {

        if(!isResponsibleFor(object)){
            logger.debug("class missmatch => ACCESS_ABSTAIN");
            return ACCESS_ABSTAIN;
        }

        if (logger.isDebugEnabled()){
            logger.debug("authentication: " + authentication.getName() + ", object : " + object.toString() + ", attribute[0]:" + ((CdmAuthority)attributes.iterator().next()).getAttribute());
        }

        int fallThroughVote = ACCESS_DENIED;

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
                } catch (ParsingException e) {
                    logger.debug("skipping " + authority.getAuthority() + " due to ParsingException");
                    continue;
                }

                // check if the voter is responsible for the permission to be evaluated
                if( ! isResponsibleFor(evalPermission.getPermissionClass())){
                    logger.debug(getResponsibility() + " not responsible for " + evalPermission.getPermissionClass() + " -> skipping");
                    continue;
                }

                ValidationResult vr = new ValidationResult();

                boolean isALL = auth.getPermissionClass().equals(CdmPermissionClass.ALL);

                vr.isClassMatch = isALL || auth.getPermissionClass().equals(evalPermission.getPermissionClass());
                vr.isPermissionMatch = auth.getOperation().containsAll(evalPermission.getOperation());
                vr.isUuidMatch = auth.hasTargetUuid() && auth.getTargetUUID().equals(object.getUuid());

                //
                // only vote if no property is defined.
                // Authorities with properties must be voted by type specific voters.
                //
                if(!auth.hasProperty()){
                    if ( !auth.hasTargetUuid() && vr.isClassMatch && vr.isPermissionMatch){
                        logger.debug("no targetUuid, class & permission match => ACCESS_GRANTED");
                        return ACCESS_GRANTED;
                    }
                    if ( vr.isUuidMatch  && vr.isClassMatch && vr.isPermissionMatch){
                        logger.debug("permission, class and uuid are matching => ACCESS_GRANTED");
                        return ACCESS_GRANTED;
                    }
                } else {
                    //
                    // If the authority contains a property AND the voter is responsible for this class
                    // we must change the fallThroughVote
                    // to ABSTAIN, since no decision can be made in this case at this point
                    //
                    if(vr.isClassMatch){
                        fallThroughVote = ACCESS_ABSTAIN;
                    }
                }

                //
                // ask subclasses for further voting decisions
                // subclasses will cast votes for specific Cdm Types
                //
                Integer furtherVotingResult = furtherVotingDescisions(auth, object, attributes, vr);
                if(furtherVotingResult != null && furtherVotingResult != ACCESS_ABSTAIN){
                    logger.debug("furtherVotingResult => " + furtherVotingResult);
                    return furtherVotingResult;
                }

            } // END Authorities loop
        } // END attributes loop

        // the value of fallThroughVote depends on whether the authority had an property or not, see above
        logger.debug("fallThroughVote => " + fallThroughVote);
        return fallThroughVote;
    }

    /**
     * Override this method to implement specific decisions.
     * Implementations of this method will be executed in {@link #vote(Authentication, Object, Collection)}.
     *
     * @param CdmAuthority
     * @param object
     * @param attributes
     * @param validationResult
     * @return A return value of ACCESS_ABSTAIN or null will be ignored in {@link #vote(Authentication, Object, Collection)}
     */
    protected Integer furtherVotingDescisions(CdmAuthority CdmAuthority, Object object, Collection<ConfigAttribute> attributes,
            ValidationResult validationResult) {
        return null;
    }

    /**
     * Holds various flags with validation results.
     * Is used to pass this information from
     * {@link CdmPermissionVoter#vote(Authentication, Object, Collection)}
     * to {@link CdmPermissionVoter#furtherVotingDescisions(CdmAuthority, Object, Collection, ValidationResult)}
     *
     * @author andreas kohlbecker
     * @date Sep 5, 2012
     *
     */
    protected class ValidationResult {
        boolean isPermissionMatch = false;
        boolean isPropertyMatch = false;
        boolean isUuidMatch = false;
        boolean isClassMatch = false;
    }

}
