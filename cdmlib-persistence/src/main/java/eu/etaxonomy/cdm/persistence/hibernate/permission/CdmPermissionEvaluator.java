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
import java.util.HashSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author k.luther
 * @date 06.07.2011
 */
@Component
public class CdmPermissionEvaluator implements PermissionEvaluator {

    protected static final Logger logger = Logger.getLogger(CdmPermissionEvaluator.class);

    private AccessDecisionManager accessDecisionManager;

    public AccessDecisionManager getAccessDecisionManager() {
        return accessDecisionManager;
    }

    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
        this.accessDecisionManager = accessDecisionManager;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication, java.io.Serializable, java.lang.String, java.lang.Object)
     */
    public boolean hasPermission(Authentication authentication,
            Serializable targetId, String targetType, Object permission) {
        logger.info("UNINMPLEMENTED: hasPermission always returns false");
        // TODO Auto-generated method stub
        return false;
    }


    /* (non-Javadoc)
     * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication, java.lang.Object, java.lang.Object)
     */
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {


        AuthorityPermission evalPermission;
        CdmPermission cdmPermission;

        if(logger.isDebugEnabled()){
            StringBuilder grantedAuthoritiesTxt = new StringBuilder();
            for(GrantedAuthority ga : authentication.getAuthorities()){
                grantedAuthoritiesTxt.append("    - ").append(ga.getAuthority()).append("\n");
            }
            logger.debug("hasPermission()\n"
                    + "  User '" + authentication.getName() + "':\n"
                    + grantedAuthoritiesTxt
                    + "  Object: " + ((CdmBase)targetDomainObject).instanceToString() + "\n"
                    + "  Permission: " + permission);
        }

        if (!(permission instanceof CdmPermission)){
            String permissionString = permission.toString();
            try {
                cdmPermission = CdmPermission.valueOf(permission.toString());
            } catch (IllegalArgumentException e) {
                logger.debug("permission string '"+ permission.toString() + "' not parsable => true");
                return true; // it might be wrong to return true
            }
        }else {
            cdmPermission = (CdmPermission)permission;
        }

        try{
            //evalPermission = new AuthorityPermission(targetDomainObject.getClass().getSimpleName().toUpperCase(), cdmPermission, ((CdmBase)targetDomainObject).getUuid());
            evalPermission = new AuthorityPermission((CdmBase)targetDomainObject, cdmPermission, ((CdmBase)targetDomainObject).getUuid());
        }catch(NullPointerException e){
            //evalPermission = new AuthorityPermission(targetDomainObject.getClass().getSimpleName().toUpperCase(), cdmPermission, null);
            evalPermission = new AuthorityPermission((CdmBase)targetDomainObject, cdmPermission, null);
        }


        if (evalPermission.className != null) {
            logger.debug("starting evaluation => ...");
            return evalPermission(authentication, evalPermission, (CdmBase) targetDomainObject);
        }else{
            logger.debug("skipping evaluation => true");
            return true;
        }

    }

    /**
     * @param targetUuid
     * @param node
     * @return
     */
    private TaxonNode findTargetUuidInTree(UUID targetUuid, TaxonNode node){
        if (targetUuid.equals(node.getUuid()))
            return node;
        else if (node.getParent()!= null){
             return findTargetUuidInTree(targetUuid, node.getParent());
        }
        return null;
    }


    /**
     * @param authorities
     * @param evalPermission
     * @param targetDomainObject
     * @return
     */
    private boolean evalPermission(Authentication authentication, AuthorityPermission evalPermission, CdmBase targetDomainObject){

        //if user has administrator rights return true;
         for (GrantedAuthority authority: authentication.getAuthorities()){
             if (authority.getAuthority().equals("ROLE_ADMIN")){
                 logger.debug("ROLE_ADMIN found => true");
                 return true;
             }
         }

        // === run voters
        Collection<ConfigAttribute> attributes = new HashSet<ConfigAttribute>();
        attributes.add(evalPermission);

        // decide() throws AccessDeniedException, InsufficientAuthenticationException
        logger.debug("AccessDecisionManager will decide ...");
        accessDecisionManager.decide(authentication, targetDomainObject, attributes);

        return true;
    }

}
