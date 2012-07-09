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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author k.luther
 * @date 06.07.2011
 */
public class CdmPermissionEvaluator implements PermissionEvaluator {
    protected static final Logger logger = Logger.getLogger(CdmPermissionEvaluator.class);

	
	

	public boolean hasPermission(Authentication authentication,
			Serializable targetId, String targetType, Object permission) {
		logger.info("hasPermission returns false");
		// TODO Auto-generated method stub
		return false;
	}


    public boolean hasPermission(Authentication authentication,
            Object targetDomainObject, Object permission) {
       
    	
    	AuthorityPermission evalPermission;
        CdmPermission cdmPermission;
		if (!(permission instanceof CdmPermission)){
			String permissionString = (String)permission;
			if (permissionString.equals("changePassword")){
				if (targetDomainObject.equals(((User)authentication.getPrincipal())))return true;
				else{
					cdmPermission = CdmPermission.ADMIN;
				}
			}else{
				cdmPermission = CdmPermission.valueOf(permissionString);
			}
		}else {
			cdmPermission = (CdmPermission)permission;
		}
		
        Collection<GrantedAuthority> authorities = ((User)authentication.getPrincipal()).getAuthorities();
        
        try{
        	//evalPermission = new AuthorityPermission(targetDomainObject.getClass().getSimpleName().toUpperCase(), cdmPermission, ((CdmBase)targetDomainObject).getUuid());
        	evalPermission = new AuthorityPermission(targetDomainObject, cdmPermission, ((CdmBase)targetDomainObject).getUuid());
        }catch(NullPointerException e){
        	//evalPermission = new AuthorityPermission(targetDomainObject.getClass().getSimpleName().toUpperCase(), cdmPermission, null);
        	evalPermission = new AuthorityPermission(targetDomainObject, cdmPermission, null);
        }
        
        	
		if (evalPermission.className != null) {
			return evalPermission(authorities, evalPermission,
					(CdmBase) targetDomainObject);
			
		}else{
			return true;
		}
        
    }

    private TaxonNode findTargetUuidInTree(UUID targetUuid, TaxonNode node){
        if (targetUuid.equals(node.getUuid()))
            return node;
        else if (node.getParent()!= null){
             return findTargetUuidInTree(targetUuid, node.getParent());
        }
        return null;
    }


    public boolean evalPermission(Collection<GrantedAuthority> authorities, AuthorityPermission evalPermission, CdmBase targetDomainObject){

    	//if user has administrator rights return true;
    	 for (GrantedAuthority authority: authorities){
    		 if (authority.getAuthority().equals("ALL.ADMIN"))return true;
    	 }
    	
    	//if targetDomainObject is instance of DescriptionBase or DescriptionElementBase use the DescriptionPermissionEvaluator
    	if (targetDomainObject instanceof DescriptionElementBase || targetDomainObject instanceof DescriptionBase){
    		return DescriptionPermissionEvaluator.hasPermission(authorities, targetDomainObject, evalPermission);
    	}
    	 	
    	
    	
    	
    	
    	
        for (GrantedAuthority authority: authorities){
            AuthorityPermission authorityPermission= new AuthorityPermission(authority.getAuthority());
            //evaluate authorities
           //if classnames match or the authorityClassName is ALL, AND the permission matches or is ADMIN the evaluation is successful 
            if ((authorityPermission.className.equals(evalPermission.className) || authorityPermission.className.equals(CdmPermissionClass.ALL))
            		&& (authorityPermission.permission.equals(evalPermission.permission)|| authorityPermission.permission.equals(CdmPermission.ADMIN))){
               /* if (authorityPermission.targetUuid != null){
                    //TODO

                }else{*/
                	return true;
                //}

            }
            //if authority is restricted to only one object (and the cascaded objects???) 
            if (authorityPermission.targetUuid != null){
                if (authorityPermission.targetUuid.equals(((CdmBase)targetDomainObject).getUuid())){
                    if (authorityPermission.permission.equals(evalPermission.permission)){
                    	return true;
                    }
                }
            }
            //if the user has the rights for a subtree
            if (authorityPermission.className.equals(CdmPermissionClass.TAXONBASE) && targetDomainObject.getClass().getSimpleName().toUpperCase().equals("TaxonNode")){
               
                TaxonNode node = (TaxonNode)targetDomainObject;
                TaxonNode targetNode = findTargetUuidInTree(authorityPermission.targetUuid, node);
                if (targetNode != null){
                    if (evalPermission.permission.equals(authorityPermission.permission) ){
                    	return true;
                    }
                }
            }
           

        }
        return false;
    }

}
