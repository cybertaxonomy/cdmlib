/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 
package eu.etaxonomy.cdm.permission;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * @author k.luther
 * @date 06.07.2011
 *
 */
public class DescriptionPermissionEvaluator {
	
	public static boolean hasPermission(Collection<GrantedAuthority> authorities,
			DescriptionElementBase targetDomainObject, AuthorityPermission evalPermission) {
		
		Feature feature = targetDomainObject.getFeature();
		String authorityString;
		for (GrantedAuthority authority: authorities){
			authorityString = authority.getAuthority();
			AuthorityPermission authorityPermission = new AuthorityPermission(authorityString);
			String label = feature.getLabel();
			if (authorityString.contains(feature.getLabel()) && evalPermission.permission.equals(authorityPermission.permission)){
				return true;
			}
		}
		
		return false;
	}
	
	
	public static boolean hasPermission (Collection<GrantedAuthority> authorities,
			DescriptionBase targetDomainObject, AuthorityPermission evalPermission){
		Set<DescriptionElementBase> elements = targetDomainObject.getElements();
		
		for (GrantedAuthority authority :authorities){
			if (authority.getAuthority().contains(CdmPermissionClass.DESCRIPTIONBASE.toString())){
				if (authority.getAuthority().lastIndexOf(".") == authority.getAuthority().indexOf(".") && authority.getAuthority().contains(evalPermission.permission.toString())){
					return true;
				}else{
					for (DescriptionElementBase element: elements){
						if (authority.getAuthority().contains(element.getFeature().getLabel()) && authority.getAuthority().contains(evalPermission.permission.toString())){
							return true;
						}
					}
				}
			}
		}
		
		
		return false;
		
	}
}
