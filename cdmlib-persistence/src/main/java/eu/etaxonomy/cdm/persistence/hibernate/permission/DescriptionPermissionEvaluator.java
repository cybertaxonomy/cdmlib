/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
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
            Object targetDomainObject, AuthorityPermission evalPermission) {
        Feature feature = null;
        String authorityString;
        AuthorityPermission authorityPermission;


        if (targetDomainObject instanceof DescriptionElementBase){
            feature = ((DescriptionElementBase)targetDomainObject).getFeature();
        }

        for (GrantedAuthority authority: authorities){

            authorityString = authority.getAuthority();
            authorityPermission = new AuthorityPermission(authorityString);

            if (targetDomainObject instanceof DescriptionElementBase){
                try{
                    //check for a special feature
                    if (feature != null){
                        if (authorityString.contains(feature.getLabel()) && (evalPermission.permission.equals(authorityPermission.permission) || authorityPermission.equals(CdmPermission.ADMIN))){
                            return true;
                        } else if (authorityPermission.className.equals(CdmPermissionClass.DESCRIPTIONBASE)) {
                            if (evalPermission.permission.equals(authorityPermission.permission) ){
                                return true;
                            } else if (authorityPermission.permission.equals(CdmPermission.ADMIN)){
                                return true;
                            }
                        }
                    }
                }catch(Exception e){
                    //in tests the initialisation of terms like features fails...
                    if (org.hibernate.ObjectNotFoundException.class.isInstance(e)){
                        if (evalPermission.permission.equals(authorityPermission.permission)|| authorityPermission.permission.equals(CdmPermission.ADMIN)){
                            return true;
                        }
                    }else {
                        return false;
                    }

                }
                //the user has the general right for descriptions
                if (authorityPermission.className.equals(CdmPermissionClass.DESCRIPTIONBASE)){
                    //no special feature
                    if (authority.getAuthority().lastIndexOf(".") == authority.getAuthority().indexOf(".") && (authorityPermission.className.equals(evalPermission.permission) || authorityPermission.equals(CdmPermission.ADMIN))){
                        return true;
                    }
                }
            } else{
                if (authorityPermission.getClassName().equals(CdmPermissionClass.DESCRIPTIONBASE) && authorityPermission.permission.equals(evalPermission.permission)){
                    return true;
                }
            }
        }

        return false;
    }


    /*public static boolean hasPermission (Collection<GrantedAuthority> authorities,
            DescriptionBase targetDomainObject, AuthorityPermission evalPermission){
        Set<DescriptionElementBase> elements = targetDomainObject.getElements();

        for (GrantedAuthority authority :authorities){
            if (authority.getAuthority().contains(CdmPermissionClass.DESCRIPTIONBASE.toString())){
                if (authority.getAuthority().lastIndexOf(".") == authority.getAuthority().indexOf(".") && authority.getAuthority().contains(evalPermission.permission.toString())){
                    return true;
                }else{
                    //TODO: das stimmt noch nicht so ganz!!!
                    for (DescriptionElementBase element: elements){
                        if (authority.getAuthority().contains(element.getFeature().getLabel()) && authority.getAuthority().contains(evalPermission.permission.toString())){
                            return true;
                        }
                    }
                }
            }
        }


        return false;

    }*/
}
