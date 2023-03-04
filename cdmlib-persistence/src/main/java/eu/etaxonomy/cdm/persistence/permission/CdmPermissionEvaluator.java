/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.Operation;
import eu.etaxonomy.cdm.model.permission.PermissionClass;

/**
 * @author k.luther
 * @author a.kohlbecker
 * @since 06.07.2011
 */
@Component
public class CdmPermissionEvaluator implements ICdmPermissionEvaluator {

    protected static final Logger logger = LogManager.getLogger();

    private AccessDecisionManager accessDecisionManager;

    public AccessDecisionManager getAccessDecisionManager() {
        return accessDecisionManager;
    }

    public CdmPermissionEvaluator() {

    }

    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
        this.accessDecisionManager = accessDecisionManager;
    }

    @Override
    public boolean hasPermission(Authentication authentication,
            Serializable targetId, String targetType, Object permission) {
        logger.warn("UNINMPLEMENTED: hasPermission always returns false");
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {

        EnumSet<CRUD> requiredOperation = null;

        TargetEntityStates cdmEntitiyStates;
        if(targetDomainObject instanceof CdmBase){
            cdmEntitiyStates = new TargetEntityStates((CdmBase)targetDomainObject);
        } else {
            cdmEntitiyStates = (TargetEntityStates)targetDomainObject;
        }

        if(logger.isDebugEnabled()){
            String targteDomainObjText = "  Object: " + (targetDomainObject == null? "null":cdmEntitiyStates.getEntity().instanceToString());
            logUserAndRequirement(authentication, permission.toString(), targteDomainObjText);
        }
        try {
            requiredOperation = operationFrom(permission);

        } catch (IllegalArgumentException e) {
            logger.debug("permission string '"+ permission.toString() + "' not parsable => true");
            return false;
        }

        return hasPermission(authentication, cdmEntitiyStates, requiredOperation);

    }

    @Override
    public boolean hasPermission(Authentication authentication, CdmBase targetEntity, EnumSet<CRUD> requiredOperation) {
        return hasPermission(authentication, new TargetEntityStates(targetEntity), requiredOperation);
    }

    @Override
    public boolean hasPermission(Authentication authentication, TargetEntityStates targetEntityStates, EnumSet<CRUD> requiredOperation) {

        if(authentication == null) {
            return false;
        }

        CdmAuthority evalPermission = authorityRequiredFor(targetEntityStates.getEntity(), requiredOperation);

        if (evalPermission.getPermissionClass() != null) {
            logger.debug("starting evaluation => ...");
            return evalPermission(authentication, evalPermission, targetEntityStates);
        }else{
            logger.debug("skipping evaluation => true");
            return true;
        }
    }

    @Override
    public <T extends CdmBase> boolean hasPermission(Authentication authentication, Class<T> targetDomainObjectClass,
            EnumSet<CRUD> requiredOperations) {

        if(authentication == null) {
            return false;
        }

        if(logger.isDebugEnabled()){
            String targteDomainObjClassText = "  Cdm-Type: " + targetDomainObjectClass.getSimpleName();
            logUserAndRequirement(authentication, requiredOperations.toString(), targteDomainObjClassText);
        }

        CdmAuthority evalPermission = new CdmAuthority(PermissionClass.getValueOf(targetDomainObjectClass), null, requiredOperations, null);

        T instance;
        try {
            Constructor<T> c = targetDomainObjectClass.getDeclaredConstructor();
            c.setAccessible(true);
            instance = c.newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Error while creating permission test instance ==> will deny", e);
            return false;
        }

        return evalPermission(authentication, evalPermission, new TargetEntityStates(instance));
    }

    protected void logUserAndRequirement(Authentication authentication, String permissions, String targteDomainObjText) {
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
                + targteDomainObjText + "\n"
                + "  Permission: " + permissions);
    }

    protected EnumSet<CRUD> operationFrom(Object permission) {
        EnumSet<CRUD> requiredOperation;
        // FIXME refactor into Operation ======
        if (Operation.isOperation(permission)){
            requiredOperation = (EnumSet<CRUD>)permission;
        } else {
            // try to treat as string
            requiredOperation = Operation.fromString(permission.toString());
        }
        // =======================================
        return requiredOperation;
    }

    private CdmAuthority authorityRequiredFor(CdmBase targetEntity, EnumSet<CRUD> requiredOperation) {
        CdmAuthority evalPermission = new CdmAuthority(targetEntity, requiredOperation);
        return evalPermission;
    }

    private boolean evalPermission(Authentication authentication, CdmAuthority evalPermission,
            TargetEntityStates targetEntityStates){

        //if user has administrator rights return true;
        if( hasOneOfRoles(authentication, Role.ROLE_ADMIN)){
            return true;
        }

        // === run voters
        Collection<ConfigAttribute> attributes = new HashSet<>();
        attributes.add(evalPermission);

        logger.debug("AccessDecisionManager will decide ...");
        try {
            accessDecisionManager.decide(authentication, targetEntityStates, attributes);
        } catch (InsufficientAuthenticationException e) {
            logger.debug("AccessDecisionManager denied by " + e, e);
            return false;
        } catch (AccessDeniedException e) {
            logger.debug("AccessDecisionManager denied by " + e, e);
            return false;
        }

        return true;
    }

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
