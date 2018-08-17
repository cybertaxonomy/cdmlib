/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import org.springframework.security.core.Authentication;

/**
 * @author a.kohlbecker
 * @since Jul 16, 2018
 *
 */
public interface RoleProbe {

    boolean checkForRole(Authentication authentication);

}