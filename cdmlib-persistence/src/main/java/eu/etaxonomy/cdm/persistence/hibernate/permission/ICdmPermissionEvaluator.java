/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.EnumSet;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 \* @since Feb 3, 2014
 *
 */
public interface ICdmPermissionEvaluator extends PermissionEvaluator {

    public boolean hasOneOfRoles(Authentication authentication, Role ... roles);

    public <T extends CdmBase> boolean hasPermission(Authentication authentication, Class<T> targetDomainObjectClass,
            EnumSet<CRUD> requiredOperations);

    boolean hasPermission(Authentication authentication, CdmBase targetDomainObject, EnumSet<CRUD> requiredOperation);

}
