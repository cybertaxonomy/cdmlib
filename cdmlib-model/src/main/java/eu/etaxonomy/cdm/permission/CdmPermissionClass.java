/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 
package eu.etaxonomy.cdm.permission;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author k.luther
 * @date 06.07.2011
 */
public enum CdmPermissionClass {
	USER, TAXONNODE, DESCRIPTIONBASE, DESCRIPTIONELEMENTBASE, TAXONBASE, ALL;
	
	
	//TODO: how to handle the problem of the subclasses of descriptions and it 
	//should be better to have TaxonBase as PermissionClassName instead of Taxon because an editor of taxa should be able to edit synonyms as well
	public static CdmPermissionClass getValueOf(Object s){
		String permissionClassString ;
		if (s instanceof String){
			permissionClassString = (String)s;
		}else if (s instanceof CdmBase){
			permissionClassString = s.getClass().getSimpleName().toUpperCase(); 
		} else if(s instanceof Class){
			permissionClassString = ((Class) s).getSimpleName().toUpperCase();
		}else{
			
			return null;
		}
		try{
			return CdmPermissionClass.valueOf(permissionClassString);
		}catch(IllegalArgumentException e){
			if (s instanceof CdmBase){
				s = s.getClass().getSuperclass();
				
				return getValueOf(s);
			}
			
		}
		return null;
	}
}
