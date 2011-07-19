/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 
package eu.etaxonomy.cdm.permission;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author k.luther
 * @date 06.07.2011
 */
public class CdmPermissionEvaluator implements PermissionEvaluator {
    protected static final Logger logger = Logger.getLogger(CdmPermissionEvaluator.class);

	private class AuthorityPermission{
		CdmPermissionClass className;
		CdmPermission permission;
		UUID targetUuid;
		
		public AuthorityPermission(String className, CdmPermission permission, UUID uuid){
			this.className = CdmPermissionClass.getValueOf(className);
			this.permission = permission;
			targetUuid = uuid;
		}
		
		public AuthorityPermission (String authority){
			String permissionString;
			int firstPoint = authority.indexOf(".");
			if (firstPoint == -1){
				className = CdmPermissionClass.valueOf(authority);
			}else{
				className = CdmPermissionClass.valueOf((authority.substring(0, firstPoint)));
				int bracket = authority.indexOf("{");
				if (bracket == -1){
					permissionString = authority.substring(firstPoint+1);
				}else{
					permissionString = authority.substring(firstPoint+1, bracket);
					int secondBracket = authority.indexOf("}");
					String uuid = authority.substring(bracket+1, secondBracket);
					targetUuid = UUID.fromString(uuid);
				}
				permission = CdmPermission.valueOf(permissionString.toUpperCase());
			}
		}
	}
	

	public boolean hasPermission(Authentication authentication,
			Serializable targetId, String targetType, Object permission) {
		logger.info("hasPermission returns false");
		// TODO Auto-generated method stub
		return false;
	}


    public boolean hasPermission(Authentication authentication,
            Object targetDomainObject, Object permission) {
       
        CdmPermission cdmPermission;
		if (!(permission instanceof CdmPermission)){
			String permissionString = (String)permission;
			if (permissionString.equals("changePassword")){
				return (targetDomainObject.equals(((User)authentication.getPrincipal()).getUsername()));
			}
			cdmPermission = CdmPermission.valueOf(permissionString);
		}else {
			cdmPermission = (CdmPermission)permission;
		}
        Collection<GrantedAuthority> authorities = ((User)authentication.getPrincipal()).getAuthorities();
        AuthorityPermission evalPermission;
        try{
        	evalPermission = new AuthorityPermission(targetDomainObject.getClass().getSimpleName().toUpperCase(), cdmPermission, ((CdmBase)targetDomainObject).getUuid());
        }catch(NullPointerException e){
        	evalPermission = new AuthorityPermission(targetDomainObject.getClass().getSimpleName().toUpperCase(), cdmPermission, null);
        }
        	//FIXME this is a workaround until the concept of CdmPermissionClass is finally discussed
		if (evalPermission.className != null) {
			return evalPermission(authorities, evalPermission,
					(CdmBase) targetDomainObject);
			/*if (evalPermission.className.equals(CdmPermissionClass.USER)) {
				return evalPermission(authorities, evalPermission,
						(CdmBase) targetDomainObject);
			} else {
				return true;
			}*/
		}else{
			//FIXME this is a workaround until the concept of CdmPermissionClass is finally discussed
			//see also AuthorityPermission constructor
			return true;
		}
        
    }

    private TaxonNode findTargetUuidInTree(UUID targetUuid, TaxonNode node){
        if (targetUuid.equals(node.getUuid()))
            return node;
        else if (node.getParent()!= null){
            findTargetUuidInTree(targetUuid, node.getParent());
        }
        return null;
    }


    public boolean evalPermission(Collection<GrantedAuthority> authorities, AuthorityPermission evalPermission, CdmBase targetDomainObject){

        for (GrantedAuthority authority: authorities){
            AuthorityPermission authorityPermission= new AuthorityPermission(authority.getAuthority());
            //evaluate authorities
            if (authorityPermission.className.equals(evalPermission.className) && authorityPermission.permission.equals(evalPermission.permission)){
                if (authorityPermission.targetUuid != null){
                    //TODO

                }else{
                    return true;
                }

            }

            if (authorityPermission.targetUuid != null){
                if (authorityPermission.targetUuid.equals(((CdmBase)targetDomainObject).getUuid())){
                    if (authorityPermission.permission.equals(evalPermission.permission)){
                        return true;
                    }
                }
            }

            if (authorityPermission.className.equals(CdmPermissionClass.TAXONNODE) && targetDomainObject.getClass().getSimpleName().equals(CdmPermissionClass.TAXONNODE)){
                //TODO: walk through the tree and look for the uuid
                TaxonNode node = (TaxonNode)targetDomainObject;
                TaxonNode targetNode = findTargetUuidInTree(authorityPermission.targetUuid, node);
                if (targetNode != null){
                    if (evalPermission.permission.equals(authorityPermission.permission)){
                        return true;
                    }
                }
            }

        }
        return false;
    }

}
