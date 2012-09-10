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

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.AuthorityPermission;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermission;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;

/**
 * The <code>CdmPermissionVoter</code> provides access control votes for {@link CdmBase} objects.
 *
 * @author andreas kohlbecker
 * @date Sep 4, 2012
 *
 */
public abstract class CdmPermissionVoter implements AccessDecisionVoter {

    public static final Logger logger = Logger.getLogger(CdmPermissionVoter.class);

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#supports(org.springframework.security.access.ConfigAttribute)
     */
    @Override
    public boolean supports(ConfigAttribute attribute) {
        // all CdmPermissionVoter support AuthorityPermission
        return attribute instanceof AuthorityPermission;
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
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {

        if(!isResponsibleFor(object)){
            logger.debug("class missmatch => ACCESS_ABSTAIN");
            return ACCESS_ABSTAIN;
        }

        if (logger.isDebugEnabled()){
            logger.debug("authentication: " + authentication.getName() + ", object : " + object.toString() + ", attribute[0]:" + ((AuthorityPermission)attributes.iterator().next()).getAttribute());
        }

        // loop over all attributes = permissions of which at least one must match
        // usually there is only one element in the collection!
        for(ConfigAttribute attribute : attributes){
            if(!(attribute instanceof AuthorityPermission)){
                throw new RuntimeException("attributes must contain only AuthorityPermission");
            }
            AuthorityPermission evalPermission = (AuthorityPermission)attribute;

            for (GrantedAuthority authority: authentication.getAuthorities()){
                AuthorityPermission authorityPermission= new AuthorityPermission(authority.getAuthority());

                // check if the voter is responsible for the permission to be evaluated
                if(!evalPermission.getClassName().equals(getResponsibility())){
                    logger.debug("not responsible for " + evalPermission.getClassName() + " -> skipping");
                    continue;
                }

//                CdmPermissionVoter impliedVoter = findImpliedVoter(authorityPermission.getClassName());
//                if(impliedVoter != null){
//                    Set<CdmBase> targetObjects = findTargetObjectsForVoter(impliedVoter, (CdmBase) object);
//                }
                ValidationResult validationResult = new ValidationResult();

                boolean isALL = authorityPermission.getClassName().equals(CdmPermissionClass.ALL);
                validationResult.isClassMatch = isALL || authorityPermission.getClassName().equals(evalPermission.getClassName());

                boolean isADMIN = authorityPermission.getPermission().equals(CdmPermission.ADMIN);
                validationResult.isPermissionMatch = isADMIN || authorityPermission.getPermission().equals(evalPermission.getPermission());

                validationResult.hasTargetUuid = authorityPermission.getTargetUUID() != null;
                validationResult.isUuidMatch = validationResult.hasTargetUuid && authorityPermission.getTargetUUID().equals(((CdmBase)object).getUuid());

                if ( !validationResult.hasTargetUuid && validationResult.isClassMatch && validationResult.isPermissionMatch){
                    logger.debug("no tragetUuid, class & permission match => ACCESS_GRANTED");
                    return ACCESS_GRANTED;
                }
                if ( validationResult.isUuidMatch  && validationResult.isClassMatch && validationResult.isPermissionMatch){
                    logger.debug("permission, class and uuid are matching => ACCESS_GRANTED");
                    return ACCESS_GRANTED;
                }

                // ask subclasses for further voting decisions
                Integer furtherVotingResult = furtherVotingDescisions(authorityPermission, object, attributes, validationResult);
                if(furtherVotingResult != null){
                    return furtherVotingResult;
                }

            } // END Authorities loop
        } // END attributes loop

        logger.debug("ACCESS_DENIED");
        return ACCESS_DENIED;
    }

    /**
     * Override this method to implement specific decisions.
     *
     * @param authorityPermission
     * @param object
     * @param attributes
     * @param validationResult
     * @return
     */
    protected Integer furtherVotingDescisions(AuthorityPermission authorityPermission, Object object, Collection<ConfigAttribute> attributes,
            ValidationResult validationResult) {
        return null;
    }

    protected class ValidationResult {
        boolean isPermissionMatch = false;
        boolean isUuidMatch = false;
        boolean isClassMatch = false;
        boolean hasTargetUuid = false;
    }

    /* ===================== implicitPermission a nd voters disabled ================ */
    /*implicitPermission
    protected Set<CdmPermissionVoter> implicitPermissionClasses = new HashSet<CdmPermissionVoter>();
    */
    /**
     * see {@link #setImplicitPermissionClasses(Set)}
     *
     * @return
    public Set<CdmPermissionVoter> getImplicitPermissionClasses() {
        return implicitPermissionClasses;
    }
     */

    /**
     * If the {@link GrantedAuthority} of an authentication contains {@link CdmPermission}s with
     * at least one of the CdmPermissionClass in {@link #implicitPermissionClasses}, this means that
     * this CdmPermission is treated as if it was a permission for the class this voter is responsible
     * for (see {@link #responsibilityClass}).
     * <p>
     * <h4>Schematic example<h4>
     * <ol>
     * <li>required CdmPermission: TAXON.UPDATE => requited CdmPermission class is TAXON</li>
     * <li>implicitPermissionClasses: {TAXONNODE}</li>
     * <li>user has CdmPermission: TAXONNODE.UPDATE</li>
     * <li>=> thus user also has implied permission TAXON.UPDATE</li>
     * </ol>
     *
     *
     * @param implicitPermissionClasses
    public void setImplicitPermissionClasses(Set<CdmPermissionVoter> implicitPermissionClasses) {
        this.implicitPermissionClasses = implicitPermissionClasses;
    }
     */

    /*
    protected CdmPermissionVoter findImpliedVoter(CdmPermissionClass assigned) {
        for(CdmPermissionVoter impliedVoter : implicitPermissionClasses){
            if(impliedVoter.isResponsibleFor(assigned)){
                if(logger.isDebugEnabled()){
                    logger.debug("* implicitClassMatch of " + assigned);
                }
                return impliedVoter;
            }
        }
        return null;
    }

    protected Set<CdmBase> findTargetObjectsForVoter(CdmPermissionVoter impliedVoter, CdmBase object){

        Set<CdmBase> targetObjects = new HashSet<CdmBase>();
        if(impliedVoter.isResponsibleFor(Taxon.class)){
            targetObjects.addAll(Taxon.class.cast(object).getTaxonNodes());
        } else {
            throw new RuntimeException("Not implemented for " + impliedVoter.getClass());
        }
        return targetObjects;
    }
    */



}
