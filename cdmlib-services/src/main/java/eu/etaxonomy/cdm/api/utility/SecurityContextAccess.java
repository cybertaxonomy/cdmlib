/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import org.springframework.security.core.context.SecurityContext;

/**
 * Provides access to the current {@link SecurityContext}
 *
 * @author a.kohlbecker
 * @since Jul 12, 2018
 *
 */
public interface SecurityContextAccess {

    public SecurityContext currentSecurityContext();

}
