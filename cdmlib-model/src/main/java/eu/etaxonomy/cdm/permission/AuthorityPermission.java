
package eu.etaxonomy.cdm.permission;

import java.util.UUID;

public class AuthorityPermission{
	CdmPermissionClass className;
	CdmPermission permission;
	UUID targetUuid;
	
	public AuthorityPermission(Object targetDomainObject, CdmPermission permission, UUID uuid){
		this.className = CdmPermissionClass.getValueOf(targetDomainObject);
		this.permission = permission;
		targetUuid = uuid;
	}
	
	public CdmPermissionClass getClassName(){
		return className;
	}
	
	public CdmPermission getPermission(){
		return permission;
	}
	
	public UUID getTargetUUID(){
		return targetUuid;
	}
	public AuthorityPermission (String authority){
		String permissionString;
		int firstPoint = authority.indexOf(".");
		if (firstPoint == -1){
			className = CdmPermissionClass.valueOf(authority);
		}else{
			className = CdmPermissionClass.valueOf((authority.substring(0, firstPoint)));
			int bracket = authority.indexOf("{");
			permissionString = getPermissionString(authority);
			if (bracket != -1){
				int secondBracket = authority.indexOf("}");
				String uuid = authority.substring(bracket+1, secondBracket);
				targetUuid = UUID.fromString(uuid);
			}
			permission = CdmPermission.valueOf(permissionString.toUpperCase());
		}
	}
	
	private static String getPermissionString(String authority){
		int lastPoint = authority.lastIndexOf(".");
		int bracket = authority.indexOf("{");
		if (bracket == -1){
			return authority.substring(lastPoint+1);
		}else{
			return authority.substring(lastPoint+1, bracket);
		}
	}
	
}