/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * To be thrown an email address is already bound to an account an can nor be
 * used to create a new user account.
 *
 * @author a.kohlbecker
 * @since Nov 4, 2021
 */
public class EmailAddressAlreadyInUseException extends UsernameNotFoundException {

    private static final long serialVersionUID = 1572067792460999503L;

    public EmailAddressAlreadyInUseException(String msg) {
        super(msg);
    }
}
