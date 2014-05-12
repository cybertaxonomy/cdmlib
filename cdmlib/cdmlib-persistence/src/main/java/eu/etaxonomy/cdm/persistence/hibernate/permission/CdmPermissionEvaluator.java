/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author k.luther
 * @author a.kohlbecker
 * @date 06.07.2011
 */
@Component
public class CdmPermissionEvaluator implements ICdmPermissionEvaluator {

    protected static final Logger logger = Logger.getLogger(CdmPermissionEvaluator.class);

    private AccessDecisionManager accessDecisionManager;

    public AccessDecisionManager getAccessDecisionManager() {
        return accessDecisionManager;
    }

    private CdmPermissionEvaluator() {

    }

    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
        this.accessDecisionManager = accessDecisionManager;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication, java.io.Serializable, java.lang.String, java.lang.Object)
     */
    @Override
    public boolean hasPermission(Authentication authentication,
            Serializable targetId, String targetType, Object permission) {
        logger.info("UNINMPLEMENTED: hasPermission always returns false");
        // TODO Auto-generated method stub
        return false;
    }


    /* (non-Javadoc)
     * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {


        CdmAuthority evalPermission;
        EnumSet<CRUD> requiredOperation;

        if(authentication == null) {
            return false;
        }

        if(logger.isDebugEnabled()){
            StringBuilder grantedAuthoritiesTxt = new StringBuilder();
            for(GrantedAuthority ga : authentication.getAuthorities()){
                grantedAuthoritiesTxt.append("    - ").append(ga.getAuthority()).append("\n");
            }
            if(grantedAuthoritiesTxt.length() == 0){
                grantedAuthoritiesTxt.append("    - ").append("<No GrantedAuthority given>").append("\n");
            }
            logger.debug("hasPermission()\n"
                    + "  User '" + authentication.getName() + "':\n"
                    + grantedAuthoritiesTxt
                    + "  Object: " + ((CdmBase)targetDomainObject).instanceToString() + "\n"
                    + "  Permission: " + permission);
        }
        try {
            // FIXME refactor into Operation ======
            if (Operation.isOperation(permission)){
                requiredOperation = (EnumSet<CRUD>)permission;
            } else {
                // try to treat as string
                requiredOperation = Operation.fromString(permission.toString());
            }
            // =======================================

        } catch (IllegalArgumentException e) {
            logger.debug("permission string '"+ permission.toString() + "' not parsable => true");
            return true; // it might be wrong to return true
        }

        evalPermission = authorityRequiredFor((CdmBase)targetDomainObject, requiredOperation);


        if (evalPermission.permissionClass != null) {
            logger.debug("starting evaluation => ...");
            return evalPermission(authentication, evalPermission, (CdmBase) targetDomainObject);
        }else{
            logger.debug("skipping evaluation => true");
            return true;
        }

    }

    /**
     * @param targetEntity
     * @param requiredOperation
     * @return
     */
    private CdmAuthority authorityRequiredFor(CdmBase targetEntity, EnumSet<CRUD> requiredOperation) {
        CdmAuthority evalPermission;
        try{
            //evalPermission = new CdmAuthority(targetDomainObject.getClass().getSimpleName().toUpperCase(), cdmPermission, (targetDomainObject).getUuid());
            evalPermission = new CdmAuthority(targetEntity, requiredOperation, (targetEntity).getUuid());
        }catch(NullPointerException e){
            // TODO document where the NPE is coming from

            //evalPermission = new CdmAuthority(targetDomainObject.getClass().getSimpleName().toUpperCase(), cdmPermission, null);
            evalPermission = new CdmAuthority(targetEntity, requiredOperation, null);
        }
        return evalPermission;
    }


    /**
     * @param authorities
     * @param evalPermission
     * @param targetDomainObject
     * @return
     */
    private boolean evalPermission(Authentication authentication, CdmAuthority evalPermission, CdmBase targetDomainObject){

        //if user has administrator rights return true;
        if( hasOneOfRoles(authentication, Role.ROLE_ADMIN)){
            return true;
        }

        // === run voters
        Collection<ConfigAttribute> attributes = new HashSet<ConfigAttribute>();
        attributes.add(evalPermission);

        logger.debug("AccessDecisionManager will decide ...");
        try {
            accessDecisionManager.decide(authentication, targetDomainObject, attributes);
        } catch (InsufficientAuthenticationException e) {
            logger.debug("AccessDecisionManager denied by " + e, e);
            return false;
        } catch (AccessDeniedException e) {
            logger.debug("AccessDecisionManager denied by " + e, e);
            return false;
        }

        return true;
    }

    /**
     * @param authentication
     */
    @Override
    public boolean hasOneOfRoles(Authentication authentication, Role ... roles) {
        for (GrantedAuthority authority: authentication.getAuthorities()){
            for(Role role : roles){
                 if (role != null && authority.getAuthority().equals(role.getAuthority())){
                     if(logger.isDebugEnabled()){
                         logger.debug(role.getAuthority() + " found => true");
                     }
                     return true;
                 }
            }
         }
        return false;
    }

}
