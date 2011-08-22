/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 
package eu.etaxonomy.cdm.permission;

import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.User;

/**
 * @author k.luther
 * @date 06.07.2011
 *
 */
public class UserPermissionEvaluator extends CdmPermissionEvaluator {
	
	public boolean hasPermission(Authentication authentication,
			User targetDomainObject, Object permission) {
		
		if (permission.getClass().isEnum()){
			
		}else if (permission.getClass().equals(String.class)){
			String permissionString = (String)permission;
			if (permissionString.equals("changePassword") && targetDomainObject.equals(authentication.getPrincipal())){
				return true;
			}
		}
		
		
		
		return false;
	}
}
