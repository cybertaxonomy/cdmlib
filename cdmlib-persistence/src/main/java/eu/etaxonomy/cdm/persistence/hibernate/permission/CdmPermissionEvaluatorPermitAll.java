// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

/**
 * FIXME can be replaced by AlwaysGrantVoter and thus remove this class
 * @author k.luther
 * @created 13.07.2011
 * @version 1.0
 */
public class CdmPermissionEvaluatorPermitAll implements PermissionEvaluator {
    private static final Logger logger = Logger.getLogger(CdmPermissionEvaluatorPermitAll.class);

    /* (non-Javadoc)
     * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication, java.lang.Object, java.lang.Object)
     */

    public boolean hasPermission(Authentication authentication,
            Object targetDomainObject, Object permission) {
        //everybody has the permission
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication, java.io.Serializable, java.lang.String, java.lang.Object)
     */

    public boolean hasPermission(Authentication authentication,
            Serializable targetId, String targetType, Object permission) {
        // TODO Auto-generated method stub
        return false;
    }
}
