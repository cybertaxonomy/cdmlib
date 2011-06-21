package eu.etaxonomy.cdm.model.permissionEval;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
public class CdmPermissionEvaluator implements PermissionEvaluator {

private class AuthorityPermission{
	String className;
	CdmPermission permission;
	UUID targetUuid;
	
	public AuthorityPermission(String className, CdmPermission permission, UUID uuid){
		this.className = className;
		this.permission = permission;
		targetUuid = uuid;
	}
	
	public AuthorityPermission (String authority){
		String permissionString;
		int firstPoint = authority.indexOf(".");
		if (firstPoint == -1){
			className = authority;
		}else{
			className = authority.substring(0, firstPoint);
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
			Object targetDomainObject, Object permission) {
		
		String permissionString = (String)permission;
		
		Collection<GrantedAuthority> authorities = authentication.getAuthorities();
		Set<Group> groups =((User)authentication.getPrincipal()).getGroups();
		Set<GrantedAuthority> groupAuthorities = new HashSet<GrantedAuthority>();
		for (Group group: groups){
			groupAuthorities.addAll(group.getGrantedAuthorities());
		}
		groupAuthorities.addAll(authorities);
		
		
		AuthorityPermission evalPermission = new AuthorityPermission(targetDomainObject.getClass().getSimpleName(), CdmPermission.valueOf(permissionString), ((CdmBase)targetDomainObject).getUuid());
		
		for (GrantedAuthority authority: groupAuthorities){
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
					if (authorityPermission.permission.equals(permission)){
						return true;
					}
				}
			}else{
				if ((authorityPermission.className.equals(targetDomainObject.getClass().getName())|| (authorityPermission.className.equals(targetDomainObject.getClass().getSuperclass().getName()) )&& authorityPermission.permission.equals(CdmPermission.valueOf(permissionString)))){
					return true;
				}
			}
			if (authorityPermission.className.equals("TaxonNode") && targetDomainObject.getClass().equals(TaxonNode.class)){
				//TODO: walk through the tree and look for the uuid
				TaxonNode node = (TaxonNode)targetDomainObject;
				TaxonNode targetNode = findTargetUuidInTree(authorityPermission.targetUuid, node);
				if (targetNode != null){
					if (permission.equals(authorityPermission.permission)){
						return true;
					}
				}
			}
			
			
			
			
				
		}
				
		// TODO Auto-generated method stub
		return false;
	}

	private TaxonNode findTargetUuidInTree(UUID targetUuid, TaxonNode node){
		if (targetUuid.equals(node.getUuid()))
			return node;
		else if (node.getParent()!= null){
			findTargetUuidInTree(targetUuid, node.getParent());
		}
		return null;
	}

	public boolean hasPermission(Authentication authentication,
			Serializable targetId, String targetType, Object permission) {
		System.out.println("hasPermission returns false");
		// TODO Auto-generated method stub
		return false;
	}

}
