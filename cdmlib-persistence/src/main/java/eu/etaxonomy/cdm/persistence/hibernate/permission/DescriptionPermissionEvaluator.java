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

import sun.security.provider.PolicyParser.ParsingException;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * Evaluates permissions ...
 *
 * @author k.luther
 * @date 06.07.2011
 *
 */
public class DescriptionPermissionEvaluator {

    public static boolean hasPermission(Collection<GrantedAuthority> authorities,
            Object targetDomainObject, CdmAuthority evalPermission) {
        Feature feature = null;
        String authorityString;
        CdmAuthority CdmAuthority;


        if (targetDomainObject instanceof DescriptionElementBase){
            feature = ((DescriptionElementBase)targetDomainObject).getFeature();
        }

        for (GrantedAuthority authority: authorities){

            authorityString = authority.getAuthority();
            try {
                CdmAuthority = new CdmAuthority(authorityString);
            } catch (ParsingException e1) {
                continue;
            }

            if (targetDomainObject instanceof DescriptionElementBase){
                try{
                    //check for a special feature
                    if (feature != null){
                        if (authorityString.contains(feature.getLabel()) && (evalPermission.operation.equals(CdmAuthority.operation) || CdmAuthority.equals(Operation.ADMIN))){
                            return true;
                        } else if (CdmAuthority.permissionClass.equals(CdmPermissionClass.DESCRIPTIONBASE)) {
                            if (evalPermission.operation.equals(CdmAuthority.operation) ){
                                return true;
                            } else if (CdmAuthority.operation.equals(Operation.ADMIN)){
                                return true;
                            }
                        }
                    }
                }catch(Exception e){
                    //in tests the initialisation of terms like features fails...
                    if (org.hibernate.ObjectNotFoundException.class.isInstance(e)){
                        if (evalPermission.operation.equals(CdmAuthority.operation)|| CdmAuthority.operation.equals(Operation.ADMIN)){
                            return true;
                        }
                    }else {
                        return false;
                    }

                }
                //the user has the general right for descriptions
                if (CdmAuthority.permissionClass.equals(CdmPermissionClass.DESCRIPTIONBASE)){
                    //no special feature
                    if (authority.getAuthority().lastIndexOf(".") == authority.getAuthority().indexOf(".") && (CdmAuthority.permissionClass.equals(evalPermission.operation) || CdmAuthority.equals(Operation.ADMIN))){
                        return true;
                    }
                }
            } else{
                if (CdmAuthority.getPermissionClass().equals(CdmPermissionClass.DESCRIPTIONBASE) && CdmAuthority.operation.equals(evalPermission.operation)){
                    return true;
                }
            }
        }

        return false;
    }


    /*public static boolean hasPermission (Collection<GrantedAuthority> authorities,
            DescriptionBase targetDomainObject, CdmAuthority evalPermission){
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
