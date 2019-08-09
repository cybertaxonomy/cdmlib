/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

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
    AGENTBASE,
    ANNOTATION,
    ANNOTATIONTYPE,
    CDMMETADATA,
    CLASSIFICATION,
    COLLECTION,
    DEFINEDTERM,
    DESCRIPTIONBASE,
    DESCRIPTIONELEMENTBASE,
    DESCRIPTIONELEMENTSOURCE,
    DESCRIPTIVEDATASET,
    EXTENSION,
    EXTENSIONTYPE,
    FEATURE,
    TERMNODE,
    FEATURETREE,
    GATHERINGEVENT,
    GRANTEDAUTHORITYIMPL,
    GROUP,
    HOMOTYPICALGROUP,
    HYBRIDRELATIONSHIP,
    IDENTIFIABLESOURCE,
    IDENTIFIER,
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
    PRESENCEABSENCETERM,
    RANK,
    REFERENCE,
    REGISTRATION,
    REPRESENTATION,
    SPECIMENOROBSERVATIONBASE,
    SPECIMENTYPEDESIGNATION,
    TAXONBASE,
    TAXONNAME,
    TAXONNODE,
    TAXONRELATIONSHIP,
    TEAMORPERSONBASE,
    TERMVOCABULARY,
    USER,
    ;

    public static final Logger logger = Logger.getLogger(CdmPermissionClass.class);

    /**
     * return the appropriate CdmPermissionClass for the given Object
     *
     * @param cdmBase
     * @return the CdmPermissionClass or null
     */
    public static CdmPermissionClass getValueOf(CdmBase cdmBase){
        return CdmPermissionClass.getValueOf(cdmBase.getClass());
    }

    /**
     * Return the appropriate CdmPermissionClass for the given Object
     *
     * @param clazz
     * @return the CdmPermissionClass or null
     */
    public static CdmPermissionClass getValueOf(Class clazz){

        CdmPermissionClass permissionClass = doValueOf(clazz);
        if(permissionClass == null) {
            logger.error("Permission class support for " + clazz + " not implemented");
        }
        return permissionClass;

    }



    /**
     * @param o
     * @return
     */
    private static CdmPermissionClass doValueOf(Class<?> clazz) {
        try{
            String normalizedName = clazz.getSimpleName().toUpperCase();
            return CdmPermissionClass.valueOf(normalizedName);
        } catch(IllegalArgumentException e){
            if (CdmBase.class.isAssignableFrom(clazz)){
                return doValueOf(clazz.getSuperclass());
            }

        }
        return null;
    }
}
