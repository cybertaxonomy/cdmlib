/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

/**
 * @author a.kohlbecker
 * @since Nov 3, 2021
 */
public class PasswordResetException extends Exception {

    private static final long serialVersionUID = -2154469325094431262L;

    public PasswordResetException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordResetException(String message) {
        super(message);
    }

}
