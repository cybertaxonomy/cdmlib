/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * see also {@link CdmBaseType}
 *
 * @author k.luther
 * @author a.kohlbecker
 * @since 06.07.2011
 */
public enum CdmPermissionClass {
    ALL,
    ANNOTATION,
    ANNOTATIONTYPE,
    CDMMETADATA,
    CLASSIFICATION,
    COLLECTION,
    DESCRIPTIONBASE,
    DESCRIPTIONELEMENTBASE,
    DESCRIPTIONELEMENTSOURCE,
    DESCRIPTIVEDATASET,
    EXTENSION,
    EXTENSIONTYPE,
    FEATURE,
    FEATURENODE,
    FEATURETREE,
    GATHERINGEVENT,
    GRANTEDAUTHORITYIMPL,
    GROUP,
    HOMOTYPICALGROUP,
    HYBRIDRELATIONSHIP,
    IDENTIFIABLESOURCE,
    LANGUAGE,
    LANGUAGESTRING,
    MARKER,
    MARKERTYPE,
    MEDIA,
    MEDIAREPRESENTATION,
    MEDIAREPRESENTATIONPART,
    NAMEDAREA,
    NAMEDAREALEVEL,
    NAMERELATIONSHIP,
    NAMETYPEDESIGNATION,
    NOMENCLATURALSTATUS,
    NOMENCLATURALSTATUSTYPE,
    SPECIMENOROBSERVATIONBASE,
    SPECIMENTYPEDESIGNATION,
    RANK,
    REFERENCE,
    REGISTRATION,
    REPRESENTATION,
    TAXONBASE,
    TAXONNAME,
    TAXONNODE,
    TAXONRELATIONSHIP,
    TEAMORPERSONBASE,
    TERMVOCABULARY,
    USER,
    ;


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

        CdmPermissionClass permissionClass = _valueOf(o);
        if(permissionClass == null) {
            Logger.getLogger(CdmPermissionClass.class).error("Permission class support for " + o + " not implemented");
        }
        return permissionClass;

    }



    /**
     * @param o
     * @return
     */
    protected static CdmPermissionClass _valueOf(Class o) {
        try{
            String normalizedName = o.getSimpleName().toUpperCase();
            return CdmPermissionClass.valueOf(normalizedName);
        } catch(IllegalArgumentException e){
            if (CdmBase.class.isAssignableFrom(o)){
                return _valueOf(o.getSuperclass());
            }

        }
        return null;
    }
}
