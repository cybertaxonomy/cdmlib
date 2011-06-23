package eu.etaxonomy.cdm.permission;

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
	CdmPermissionClass className;
	CdmPermission permission;
	UUID targetUuid;
	
	public AuthorityPermission(String className, CdmPermission permission, UUID uuid){
		this.className = CdmPermissionClass.valueOf(className);
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
			Object targetDomainObject, Object permission) {
		
		String permissionString = (String)permission;
		
		Collection<GrantedAuthority> authorities = authentication.getAuthorities();
		Set<Group> groups =((User)authentication.getPrincipal()).getGroups();
		Set<GrantedAuthority> groupAuthorities = new HashSet<GrantedAuthority>();
		for (Group group: groups){
			groupAuthorities.addAll(group.getGrantedAuthorities());
		}
		groupAuthorities.addAll(authorities);
		
		
		AuthorityPermission evalPermission = new AuthorityPermission(targetDomainObject.getClass().getSimpleName().toUpperCase(), CdmPermission.valueOf(permissionString), ((CdmBase)targetDomainObject).getUuid());
		
		return evalPermission(groupAuthorities, evalPermission, (CdmBase)targetDomainObject);
				
		// TODO Auto-generated method stub
		//return false;
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
	
	public boolean evalPermission(Set<GrantedAuthority> authorities, AuthorityPermission evalPermission, CdmBase targetDomainObject){
		
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
