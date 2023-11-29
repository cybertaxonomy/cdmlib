/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission;

import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.Operation;

/**
 * @author andreas
 * @since Sep 4, 2012
 */
public class PermissionDeniedException extends HibernateException {

    private static final long serialVersionUID = 6993452039967589921L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(Authentication authentication, CdmBase entity, Operation requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    public PermissionDeniedException(Authentication authentication, CdmBase entity, EnumSet<CRUD> requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    public PermissionDeniedException(Authentication authentication, CdmBase entity, String requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    public PermissionDeniedException(Authentication authentication, Role[] roles) {
        super("Permission denied for '" + authentication.getName()
                + "' none of the roles '" + roles + "' found in authentication.");
    }

    public PermissionDeniedException(Throwable cause) {
        super(cause);
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}