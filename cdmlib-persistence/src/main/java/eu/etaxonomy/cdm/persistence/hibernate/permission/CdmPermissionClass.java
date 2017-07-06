/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * see also {@link CdmBaseType}
 *
 * @author k.luther
 * @author a.kohlbecker
 * @date 06.07.2011
 */
public enum CdmPermissionClass {
    USER,
    DESCRIPTIONBASE,
    DESCRIPTIONELEMENTBASE,
    TAXONBASE,
    ALL,
    TAXONNODE,
    CLASSIFICATION,
    REFERENCE,
    TAXONNAME,
    TEAMORPERSONBASE;

    /**
     * return the appropriate CdmPermissionClass for the given Object
     *
     * @param o
     * @return the CdmPermissionClass or null
     */
    public static CdmPermissionClass getValueOf(CdmBase o){
        return CdmPermissionClass.getValueOf(o.getClass());
    }



    /**
     * return the appropriate CdmPermissionClass for the given Object
     *
     * @param o
     * @return the CdmPermissionClass or null
     */
    public static CdmPermissionClass getValueOf(Class o){

        try{
            return CdmPermissionClass.valueOf(o.getSimpleName().toUpperCase());
        } catch(IllegalArgumentException e){
            if (CdmBase.class.isAssignableFrom(o)){
                return getValueOf(o.getSuperclass());
            }

        }

        return null;
    }
}
