package eu.etaxonomy.cdm.permission;

import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.User;

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
